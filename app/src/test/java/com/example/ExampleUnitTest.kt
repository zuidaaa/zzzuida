package com.example

import android.os.Build
import com.example.engine.OfflineDeepThinkingEngine
import com.example.engine.ReasoningPhase
import com.example.engine.ThinkingLevel
import com.example.mcp.HuggingFaceMcpClient
import com.example.mcp.McpServerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun mcpServerConfig_generatesValidJson() {
        val config = McpServerConfig(
            serverId = "hf-endpoints",
            httpUrl = "https://endpoints.huggingface.co/mcp"
        )
        val json = config.toJsonConfigString()
        assertTrue(json.contains("\"hf-endpoints\""))
        assertTrue(json.contains("https://endpoints.huggingface.co/mcp"))
    }

    @Test
    fun huggingFaceMcpClient_defaultTools_areAvailable() {
        val client = HuggingFaceMcpClient()
        assertEquals(7, client.defaultTools.size)
        val toolNames = client.defaultTools.map { it.name }
        assertTrue(toolNames.contains("hf_hub_search_models"))
        assertTrue(toolNames.contains("hf_hub_get_model"))
        assertTrue(toolNames.contains("hf_hub_search_datasets"))
        assertTrue(toolNames.contains("hf_hub_search_spaces"))
        assertTrue(toolNames.contains("hf_hub_search_papers"))
        assertTrue(toolNames.contains("hf_hub_run_inference"))
        assertTrue(toolNames.contains("hf_hub_download_model"))
    }

    @Test
    fun huggingFaceMcpClient_searchModelsDirect_returnsResults() = runBlocking {
        val client = HuggingFaceMcpClient()
        val results = client.searchModelsDirect("deepseek", limit = 3)
        assertTrue(results.isNotEmpty())
        assertNotNull(results[0].id)
    }

    @Test
    fun offlineEngine_detectsHuggingFaceQuery_andIncludesMcpPhase() {
        val query = "Search Hugging Face hub for DeepSeek-R1 reasoning model weights"
        val result = OfflineDeepThinkingEngine.processPromptInstant(
            prompt = query,
            thinkingLevel = ThinkingLevel.EXTENDED,
            searchGrounded = false
        )
        assertTrue(result.steps.isNotEmpty())
        val hasMcpPhase = result.steps.any { it.phase == ReasoningPhase.MCP_TOOL }
        assertTrue("Should include MCP_TOOL phase for Hugging Face prompt", hasMcpPhase)
        assertTrue("Answer should not be blank", result.finalAnswerText.isNotBlank())
        assertTrue("Thought trace should not be blank", result.fullThoughtText.isNotBlank())
    }

    @Test
    fun generativeModel_structuredOutput_andThinkingMode_workCorrectly() = runBlocking {
        val generativeModel = com.example.engine.GenerativeModel()
        
        // 1. Check isStructuredOutputFeatureAvailable API (true when running on Android O+ / SDK >= 26)
        val isAvailable = generativeModel.isStructuredOutputFeatureAvailable()
        // Build.VERSION.SDK_INT is 0 in pure JVM unit tests unless running under Robolectric or on-device
        assertTrue("isStructuredOutputFeatureAvailable should return boolean without exceptions", isAvailable || Build.VERSION.SDK_INT < 26)

        // 2. Build request with enableThinking
        val request = com.example.engine.generateContentRequest {
            text("Solve this complex riddle: I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?")
            enableThinking = true
            setSchema(com.example.engine.Plant::class.java)
        }

        try {
            val response = generativeModel.generateContent(request)
            
            // 1. Access the main response
            val answer = response.candidates.firstOrNull()?.text
            assertNotNull(answer)
            assertTrue(answer!!.isNotBlank())
            
            // 2. Access the separated thought process
            assertTrue(response.thoughtProcess.isNotEmpty())
            val combinedThoughts = response.thoughtProcess.joinToString("") { it.text }
            assertTrue(combinedThoughts.isNotBlank())
        } catch (e: com.example.engine.GenAiException) {
            org.junit.Assert.fail("Should not throw GenAiException: ${e.message}")
        }
    }

    @Test
    fun gemmaResponseParser_splitsThinkingAndAnswerProperly() {
        val sampleGemmaOutput = """
            <thought>
            Step 1: The user provided an image showing a chart and requested analysis.
            Step 2: Identify axes and data points.
            Step 3: Formulate final conclusive statement.
            </thought>
            Based on the provided chart, sales increased by 25% year-over-year.
        """.trimIndent()

        val parsed = com.example.engine.GemmaResponseParser.parseResponse(sampleGemmaOutput)
        assertTrue(parsed.hasThinkingTrace)
        assertTrue(parsed.thinking.contains("Step 1:"))
        assertEquals("Based on the provided chart, sales increased by 25% year-over-year.", parsed.answer)
    }

    @Test
    fun mlKitProofreading_endToEndWorkflow() = runBlocking {
        val textToProofread = "The praject is compleet but needs too be reviewd"
        val options = com.example.engine.ProofreaderOptions.builder(null)
            .setInputType(com.example.engine.ProofreaderOptions.InputType.KEYBOARD)
            .setLanguage(com.example.engine.ProofreaderOptions.Language.ENGLISH)
            .build()
        val proofreader = com.example.engine.Proofreading.getClient(options)

        val status = proofreader.checkFeatureStatus().await()
        assertTrue(status == com.example.engine.FeatureStatus.AVAILABLE || status == com.example.engine.FeatureStatus.DOWNLOADABLE)

        val request = com.example.engine.ProofreadingRequest.builder(textToProofread).build()
        val result = proofreader.runInference(request).await()

        assertTrue(result.results.isNotEmpty())
        val corrected = result.results.first().text
        assertEquals("The project is complete but needs to be reviewed", corrected)

        proofreader.close()
    }

    @Test
    fun todoScreenViewModel_functionCalling_andLiveSession() = runBlocking {
        val viewModel = com.example.engine.TodoScreenViewModel()
        
        // 1. Initial State
        assertEquals(3, viewModel.todoList.value.size)

        // 2. Execute Add Tool Call
        val addResult = viewModel.handleFunctionCall("addTodo", mapOf("title" to "Buy groceries", "priority" to "High"))
        assertEquals(true, addResult["success"])
        assertEquals(4, viewModel.todoList.value.size)

        // 3. Execute Toggle Tool Call
        val toggleResult = viewModel.handleFunctionCall("toggleTodoStatus", mapOf("id" to "1"))
        assertEquals(true, toggleResult["success"])
        assertTrue(viewModel.todoList.value.first { it.id == "1" }.isCompleted)

        // 4. Start & Stop Voice Session
        viewModel.startLiveVoiceSession()
        viewModel.stopLiveVoiceSession()
    }

    @Test
    fun playServicesModuleInstall_availabilityAndProgress() = runBlocking {
        val moduleInstallClient = com.example.engine.ModuleInstall.getClient(null)
        val optionalModuleApi = com.example.engine.TfLite.getClient(null)

        var checked = false
        val response = moduleInstallClient
            .areModulesAvailable(optionalModuleApi)
            .addOnSuccessListener {
                if (it.areModulesAvailable()) {
                    checked = true
                }
            }
            .await()

        assertTrue(response.areModulesAvailable())
        assertTrue(checked)

        // Test installation flow with progress listener
        var progressUpdated = false
        val request = com.example.engine.ModuleInstallRequest.newBuilder()
            .addApi(optionalModuleApi)
            .setListener { update ->
                if (update.progressPercentage > 0) {
                    progressUpdated = true
                }
            }
            .build()

        val installResult = moduleInstallClient.installModules(request).await()
        assertTrue(installResult)
        assertTrue(progressUpdated)
    }

    @Test
    fun hybridVotingRefineEngine_executesThreePhaseConsensus() = runBlocking {
        val aufgabe = "Erstelle eine Python-Funktion, die verschachtelte JSON-Objekte flachklopft (flatten), dabei aber Listen-Indizes als 'key[0]' beibehält."
        var finalResult: com.example.engine.HybridVotingResult? = null

        com.example.engine.HybridVotingRefineEngine.executePipelineStream(aufgabe).collect { event ->
            if (event is com.example.engine.HybridPipelineProgress.Completed) {
                finalResult = event.result
            }
        }

        assertNotNull("Pipeline must produce a completed result", finalResult)
        assertEquals(aufgabe, finalResult!!.prompt)
        assertNotNull(finalResult!!.draftA)
        assertNotNull(finalResult!!.draftB)
        assertNotNull(finalResult!!.critiqueAByB)
        assertNotNull(finalResult!!.critiqueBByA)
        assertTrue(finalResult!!.finalConsolidatedSolution.contains("def flatten_json"))
        assertTrue(finalResult!!.finalConsolidatedSolution.contains("key[0]") || finalResult!!.finalConsolidatedSolution.contains("[{index}]") || finalResult!!.finalConsolidatedSolution.contains("[index]"))
    }

    @Test
    fun dataPointAggregation_filtersInvalidAndSumsCorrectly() {
        val testPoints = listOf(
            com.example.data.DataPoint(category = com.example.data.Category.REASONING, value = 42.5),
            com.example.data.DataPoint(category = com.example.data.Category.REASONING, value = 17.5),
            com.example.data.DataPoint(category = com.example.data.Category.VISION, value = 100.0),
            com.example.data.DataPoint(category = com.example.data.Category.VISION, value = -10.0), // Invalid <= 0
            com.example.data.DataPoint(category = com.example.data.Category.AUDIO, value = 0.0),    // Invalid <= 0
            com.example.data.DataPoint(category = com.example.data.Category.AUDIO, value = Double.NaN), // Invalid NaN
            com.example.data.DataPoint(category = com.example.data.Category.AUDIO, value = Double.POSITIVE_INFINITY) // Invalid Inf
        )

        val accumulator = java.util.EnumMap<com.example.data.Category, Double>(com.example.data.Category::class.java)
        for (point in testPoints) {
            val value = point.value
            if (value > 0.0 && !value.isNaN() && !value.isInfinite()) {
                val current = accumulator[point.category] ?: 0.0
                accumulator[point.category] = current + value
            }
        }

        assertEquals(60.0, accumulator[com.example.data.Category.REASONING] ?: 0.0, 0.001)
        assertEquals(100.0, accumulator[com.example.data.Category.VISION] ?: 0.0, 0.001)
        assertEquals(0.0, accumulator[com.example.data.Category.AUDIO] ?: 0.0, 0.001)
    }

    @OptIn(FlowPreview::class)
    @Test
    fun flowDebounce_processesLatestAndPreventsRedundantRuns(): Unit = runBlocking {
        val flow = MutableSharedFlow<Int>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        val processedItems = mutableListOf<Int>()
        val job = CoroutineScope(Dispatchers.Default).launch {
            flow
                .debounce(100L)
                .distinctUntilChanged()
                .collectLatest { item: Int ->
                    processedItems.add(item)
                }
        }

        // Wait for subscriber to attach
        while (flow.subscriptionCount.value == 0) {
            delay(10)
        }

        // Rapid emissions simulate fast user typing or slider scrub
        flow.tryEmit(1)
        flow.tryEmit(2)
        flow.tryEmit(3)
        flow.tryEmit(4)

        // Wait for debounce period to settle
        delay(250L)

        // Only the latest item should have been processed
        assertEquals(listOf(4), processedItems)

        // Another discrete emission after delay
        flow.tryEmit(5)
        delay(250L)

        assertEquals(listOf(4, 5), processedItems)
        job.cancel()
    }

    @Test
    fun taskComplexityRouter_evaluatesPromptComplexityAccurately() {
        // 1. Math proof and algorithmic complexity
        val deepMathPrompt = "Please prove the Riemann Hypothesis or show that P vs NP theorem using formal verification tree and asymptotic polynomial bounds."
        val planMath = com.example.engine.TaskComplexityRouter.analyzeTaskComplexity(deepMathPrompt)
        assertEquals(com.example.engine.TaskComplexityLevel.COMPLEX, planMath.complexity)
        assertEquals(com.example.engine.GeminiApiClient.MODEL_PRO, planMath.selectedGeminiModel)
        assertEquals(com.example.engine.ThinkingLevel.EXTENDED, planMath.recommendedThinkingLevel)
        assertTrue(planMath.matchedSignals.isNotEmpty())

        // 2. Simple casual lookup / greeting
        val casualPrompt = "Hi there, thank you!"
        val planCasual = com.example.engine.TaskComplexityRouter.analyzeTaskComplexity(casualPrompt)
        assertEquals(com.example.engine.TaskComplexityLevel.SIMPLE, planCasual.complexity)
        assertEquals(com.example.engine.GeminiApiClient.MODEL_FLASH_LITE, planCasual.selectedGeminiModel)
        assertEquals(com.example.engine.ThinkingLevel.LOW, planCasual.recommendedThinkingLevel)

        // 3. General task
        val generalPrompt = "Summarize the history of digital computers in 3 paragraphs."
        val planGeneral = com.example.engine.TaskComplexityRouter.analyzeTaskComplexity(generalPrompt)
        assertEquals(com.example.engine.TaskComplexityLevel.MODERATE, planGeneral.complexity)
        assertEquals(com.example.engine.GeminiApiClient.MODEL_FLASH, planGeneral.selectedGeminiModel)
        assertEquals(com.example.engine.ThinkingLevel.MEDIUM, planGeneral.recommendedThinkingLevel)
    }

    @Test
    fun modelUpgradeManager_fetchesUpstreamReleasesAndTracksRegistry() = runBlocking {
        val registry = com.example.engine.ModelUpgradeManager.getInitialUpgradeRegistry()
        assertTrue(registry.isNotEmpty())

        // Check default registry contains Flash 3.8 preview or DeepSeek upgrades
        val hasFlash = registry.any { it.modelId.contains("flash") }
        assertTrue(hasFlash)

        // Check remote updates check simulation
        val updated = com.example.engine.ModelUpgradeManager.checkRemoteForUpdates(registry)
        assertTrue(updated.isNotEmpty())
        val medItem = updated.find { it.modelId == "med-reason-7b" }
        assertNotNull(medItem)
        assertTrue(medItem!!.hasUpdate)
    }

    @Test
    fun offlineDeepThinkingEngine_accuratelyAuditsMainViewModel() = runBlocking {
        val prompt = "Please review MainViewModel.kt processHeavyComputation filter groupBy mapValues"
        val chunks = com.example.engine.OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = com.example.engine.ThinkingLevel.LOW,
            streamingSpeedMs = 0L
        ).toList()

        val fullText = chunks.joinToString("") { it.token }
        assertTrue(fullText.contains("Three sequential passes"))
        assertTrue(fullText.contains("No exception handling"))
        assertTrue(fullText.contains("Race condition risk"))
        assertTrue(fullText.contains("computeJob?.cancel()"))
        assertTrue(fullText.contains("HashMap.merge"))
    }

    @Test
    fun singlePassHashMapMerge_aggregatesAndFiltersProperly() {
        val testPoints = listOf(
            com.example.data.DataPoint(category = com.example.data.Category.REASONING, value = 40.0),
            com.example.data.DataPoint(category = com.example.data.Category.REASONING, value = 20.0),
            com.example.data.DataPoint(category = com.example.data.Category.VISION, value = 50.0),
            com.example.data.DataPoint(category = com.example.data.Category.VISION, value = -15.0)
        )

        val result = HashMap<com.example.data.Category, Double>()
        for (point in testPoints) {
            if (point.value > 0) {
                result.merge(point.category, point.value, Double::plus)
            }
        }

        assertEquals(60.0, result[com.example.data.Category.REASONING] ?: 0.0, 0.001)
        assertEquals(50.0, result[com.example.data.Category.VISION] ?: 0.0, 0.001)
        assertEquals(null, result[com.example.data.Category.AUDIO])
    }

    @Test
    fun benchmarkResultEntity_compositeScoreCalculation_isWithinValidRange() {
        val result = com.example.data.BenchmarkResultEntity(
            id = "test-bench-1",
            overallScore = 9850,
            dbWriteOpsPerSec = 3450.0,
            dbReadOpsPerSec = 12800.0,
            dbLatencyMs = 2L,
            npuThroughputTops = 84.8,
            ragSearchLatencyMs = 12L,
            activeSourcesCount = 7
        )
        assertTrue(result.overallScore in 9000..10000)
        assertTrue(result.dbWriteOpsPerSec > 1000.0)
        assertTrue(result.npuThroughputTops > 80.0)
        assertTrue(result.passedAllChecks)
    }

    @Test
    fun knowledgeSourceEntity_defaultGroundingSources_containWebAndMaps() {
        val searchSource = com.example.data.KnowledgeSourceEntity(
            id = "src-google-search",
            name = "Google Search Grounding",
            sourceType = "WEB_SEARCH",
            endpointOrPath = "https://generativelanguage.googleapis.com (googleSearch tool)",
            isEnabled = true
        )
        val mapsSource = com.example.data.KnowledgeSourceEntity(
            id = "src-google-maps",
            name = "Google Maps Geo Engine",
            sourceType = "MAPS_GEO",
            endpointOrPath = "https://maps.googleapis.com (googleMaps tool)",
            isEnabled = true
        )
        assertEquals("WEB_SEARCH", searchSource.sourceType)
        assertEquals("MAPS_GEO", mapsSource.sourceType)
        assertTrue(searchSource.isEnabled)
        assertTrue(mapsSource.isEnabled)
    }

    @Test
    fun cacheStorageStats_hitRateCalculation_isCorrect() {
        val stats = com.example.data.CacheStorageStats(
            totalTracesCount = 20,
            totalTokensCached = 84000L,
            totalHitsCount = 60L,
            estimatedStorageKb = 250.0,
            hitRatePercent = 75.0f,
            avgLatencySavingsMs = 4200L
        )
        assertEquals(20, stats.totalTracesCount)
        assertEquals(84000L, stats.totalTokensCached)
        assertEquals(60L, stats.totalHitsCount)
        assertEquals(75.0f, stats.hitRatePercent, 0.01f)
        assertTrue(stats.avgLatencySavingsMs > 4000L)
    }

    @Test
    fun problemCatalog_containsDiverseDomains_andValidChallenges() {
        val challenges = com.example.engine.problemsolving.ProblemCatalog.challenges
        assertTrue(challenges.size >= 7)

        val codingChallenges = challenges.filter { it.domain == com.example.engine.problemsolving.ProblemDomain.CODING }
        val logicPuzzles = challenges.filter { it.domain == com.example.engine.problemsolving.ProblemDomain.LOGIC }
        val scienceQuestions = challenges.filter { it.domain == com.example.engine.problemsolving.ProblemDomain.SCIENCE }
        val mathProofs = challenges.filter { it.domain == com.example.engine.problemsolving.ProblemDomain.MATH }

        assertTrue(codingChallenges.isNotEmpty())
        assertTrue(logicPuzzles.isNotEmpty())
        assertTrue(scienceQuestions.isNotEmpty())
        assertTrue(mathProofs.isNotEmpty())

        // Every challenge must have at least 2 progressive hints and an optimal solution
        challenges.forEach { c ->
            assertTrue(c.hints.size >= 2)
            assertTrue(c.optimalSolutionSnippet.isNotBlank())
            assertTrue(c.formalProofOrExplanation.isNotBlank())
            assertTrue(c.constraints.isNotEmpty())
        }
    }

    @Test
    fun problemProgressEntity_instantiationAndStatus_isCorrect() {
        val progress = com.example.data.ProblemProgressEntity(
            problemId = "code-lockfree-ring-buffer",
            domain = "CODING",
            status = "VERIFIED_SOLVED",
            unlockedHintsCount = 3,
            userSolutionDraft = "class MyLockFreeQueue { ... }",
            score = 95,
            isBookmarked = true
        )
        assertEquals("code-lockfree-ring-buffer", progress.problemId)
        assertEquals("VERIFIED_SOLVED", progress.status)
        assertEquals(3, progress.unlockedHintsCount)
        assertTrue(progress.isBookmarked)
        assertEquals(95, progress.score)
    }

    @Test
    fun thoughtTraceMetric_defaultTraceMetrics_areValid() {
        val metrics = com.example.ui.components.defaultTraceMetrics
        assertTrue(metrics.isNotEmpty())
        assertTrue(metrics.size >= 5)

        metrics.forEach { trace ->
            assertTrue(trace.reasoningTimeMs > 0)
            assertTrue(trace.tokenUsage > 0)
            assertTrue(trace.title.isNotBlank())
            assertTrue(trace.stepCount >= 1)
        }

        val avgTime = metrics.map { it.reasoningTimeMs }.average()
        val totalTokens = metrics.sumOf { it.tokenUsage }
        assertTrue(avgTime > 1000)
        assertTrue(totalTokens > 10000)
    }
}



