package com.example.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val reasoningDao: ReasoningCacheDao,
    private val benchmarkDao: KnowledgeAndBenchmarkDao,
    private val folderDao: FolderFoundationDao
) {

    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()
    val allLocalModels: Flow<List<LocalModelEntity>> = chatDao.getAllLocalModels()
    val allGeneratedMedia: Flow<List<GeneratedMediaEntity>> = chatDao.getAllGeneratedMedia()
    val allDocuments: Flow<List<DocumentEntity>> = chatDao.getAllDocuments()
    val allBenchmarkResults: Flow<List<BenchmarkResultEntity>> = benchmarkDao.getAllBenchmarkResults()
    val latestBenchmarkResult: Flow<BenchmarkResultEntity?> = benchmarkDao.getLatestBenchmarkResult()
    val allKnowledgeSources: Flow<List<KnowledgeSourceEntity>> = benchmarkDao.getAllKnowledgeSources()
    val thoughtCacheRepository: ThoughtTraceCacheRepository = ThoughtTraceCacheRepository(reasoningDao)

    // Room Reasoning Cache Flows
    val cachedReasoningResults: Flow<List<ReasoningCacheEntity>> = reasoningDao.getAllCachedResults()
    val favoriteReasoningResults: Flow<List<ReasoningCacheEntity>> = reasoningDao.getFavoriteCachedResults()
    val deepThinkingSessions: Flow<List<DeepThinkingSessionEntity>> = reasoningDao.getAllSessions()
    val totalTokensCached: Flow<Long?> = reasoningDao.getTotalTokensCached()
    val totalCacheCount: Flow<Int> = reasoningDao.getCacheCount()
    val allDatasets: Flow<List<LlmDatasetEntity>> = reasoningDao.getAllDatasets()

    // Web Watcher & Wiki Memory Flows
    val allWebWatchers: Flow<List<WebWatcherEntity>> = reasoningDao.getAllWebWatchers()
    val allWikiPages: Flow<List<WikiPageEntity>> = reasoningDao.getAllWikiPages()

    fun searchWikiPages(query: String): Flow<List<WikiPageEntity>> = reasoningDao.searchWikiPages(query)

    suspend fun insertWebWatcher(watcher: WebWatcherEntity) = reasoningDao.insertWebWatcher(watcher)
    suspend fun deleteWebWatcher(id: String) = reasoningDao.deleteWebWatcher(id)
    suspend fun insertWikiPage(page: WikiPageEntity) = reasoningDao.insertWikiPage(page)
    suspend fun deleteWikiPage(id: String) = reasoningDao.deleteWikiPage(id)

    fun getFilesByFolder(folderName: String): Flow<List<FolderFileEntity>> = folderDao.getFilesByFolder(folderName)
    fun searchFolderFiles(query: String): Flow<List<FolderFileEntity>> = folderDao.searchFolderFiles(query)
    fun getAllFolders(): Flow<List<String>> = folderDao.getAllFolders()
    suspend fun insertFolderFile(file: FolderFileEntity) = folderDao.insertFile(file)
    suspend fun deleteFolderFile(file: FolderFileEntity) = folderDao.deleteFile(file)

    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun getMessagesList(conversationId: String): List<ChatMessageEntity> {
        return chatDao.getMessagesListForConversation(conversationId)
    }

    fun searchReasoningCache(query: String, domain: String = "All"): Flow<List<ReasoningCacheEntity>> {
        return if (query.isBlank()) {
            if (domain == "All") {
                reasoningDao.getAllCachedResults()
            } else {
                reasoningDao.getCachedResultsByDomain(domain)
            }
        } else {
            reasoningDao.searchCachedResults(query.trim())
        }
    }

    suspend fun findCachedReasoning(prompt: String, modelId: String, thinkingLevel: String): ReasoningCacheEntity? {
        val normalized = prompt.trim().lowercase()
        val cached = reasoningDao.findMatchingCache(normalized, modelId, thinkingLevel)
            ?: reasoningDao.findMatchingQuery(normalized)
        if (cached != null) {
            reasoningDao.incrementHitCount(cached.cacheKey)
        }
        return cached
    }

    suspend fun getCachedResultByKey(cacheKey: String): ReasoningCacheEntity? {
        return reasoningDao.getCachedResultByKey(cacheKey)
    }

    suspend fun getStepsForCacheKey(cacheKey: String): List<CachedReasoningStepEntity> {
        return reasoningDao.getStepsListForCacheKey(cacheKey)
    }

    suspend fun createNewConversation(title: String = "New Deep Thinking Session", domainTag: String = "General"): ConversationEntity {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastMessagePreview = "",
            domainTag = domainTag
        )
        chatDao.insertConversation(conv)
        return conv
    }

    suspend fun updateConversation(conversation: ConversationEntity) {
        chatDao.updateConversation(conversation)
    }

    suspend fun deleteConversation(id: String) {
        chatDao.deleteMessagesForConversation(id)
        chatDao.deleteConversationById(id)
    }

    suspend fun deleteAllConversations() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllConversations()
    }

    suspend fun saveMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message)
        // Also update conversation timestamp & preview
        chatDao.getConversationById(message.conversationId)?.let { conv ->
            val preview = if (message.content.length > 60) message.content.take(57) + "..." else message.content
            chatDao.updateConversation(
                conv.copy(
                    updatedAt = System.currentTimeMillis(),
                    lastMessagePreview = preview
                )
            )
        }
    }

    suspend fun updateMessageFeedback(messageId: String, rating: Int, feedbackText: String) {
        chatDao.updateMessageFeedback(messageId, rating, feedbackText)
    }

    suspend fun getMessageById(messageId: String): ChatMessageEntity? {
        return chatDao.getMessageById(messageId)
    }

    /**
     * Cache model reasoning result directly into Room Database for instant offline replay & inspection
     */
    suspend fun cacheReasoningResult(
        prompt: String,
        modelId: String,
        modelName: String,
        thinkingLevel: String,
        thinkingBudgetTokens: Int,
        answerContent: String,
        thoughtProcess: String,
        reasoningStepsJson: String = "[]",
        searchCitationsJson: String = "[]",
        durationMs: Long,
        thinkingTokens: Int,
        answerTokens: Int,
        domainCategory: String = "General",
        userNotes: String = ""
    ): ReasoningCacheEntity {
        val cacheKey = generateCacheKey(prompt, modelId, thinkingLevel)
        val normalized = prompt.trim().lowercase()
        val tokensPerSec = if (durationMs > 0) ((thinkingTokens + answerTokens).toDouble() / (durationMs.toDouble() / 1000.0)) else 42.5

        val cacheEntity = ReasoningCacheEntity(
            cacheKey = cacheKey,
            promptQuery = prompt.trim(),
            normalizedQuery = normalized,
            modelId = modelId,
            modelName = modelName,
            thinkingLevel = thinkingLevel,
            thinkingBudgetTokens = thinkingBudgetTokens,
            answerContent = answerContent,
            thoughtProcess = thoughtProcess,
            reasoningStepsJson = reasoningStepsJson,
            searchCitationsJson = searchCitationsJson,
            thinkingDurationMs = durationMs,
            thinkingTokens = thinkingTokens,
            answerTokens = answerTokens,
            tokensPerSecond = tokensPerSec,
            domainCategory = domainCategory,
            cachedAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis(),
            hitCount = 1,
            isFavorite = false,
            userNotes = userNotes,
            offlineAvailable = true,
            isVerified = true
        )
        reasoningDao.insertCachedResult(cacheEntity)

        // Also record a DeepThinkingSessionEntity snapshot for session management
        val sessionEntity = DeepThinkingSessionEntity(
            sessionId = "session_${cacheKey.take(12)}",
            title = if (prompt.length > 45) prompt.take(42) + "..." else prompt,
            initialQuery = prompt,
            modelId = modelId,
            modelName = modelName,
            domainCategory = domainCategory,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            totalThinkingTimeMs = durationMs,
            totalThinkingTokens = thinkingTokens,
            stepsCount = if (reasoningStepsJson != "[]") 5 else 3,
            finalSynthesisPreview = if (answerContent.length > 90) answerContent.take(87) + "..." else answerContent,
            isOfflineCached = true,
            complexityRating = when (thinkingLevel) {
                "EXTENDED" -> 5
                "HIGH" -> 4
                "MEDIUM" -> 3
                else -> 2
            },
            verificationStatus = "VERIFIED",
            userNotes = userNotes,
            fullThoughtTraceMarkdown = thoughtProcess,
            synthesisResult = answerContent,
            datasetAssocId = null
        )
        reasoningDao.insertSession(sessionEntity)

        return cacheEntity
    }

    // Dataset Management Methods
    suspend fun saveDataset(dataset: LlmDatasetEntity) {
        reasoningDao.insertDataset(dataset)
    }

    suspend fun deleteDataset(id: String) {
        reasoningDao.deleteDataset(id)
    }

    suspend fun getDatasetById(id: String): LlmDatasetEntity? {
        return reasoningDao.getDatasetById(id)
    }

    suspend fun clearAllDatasets() {
        reasoningDao.clearAllDatasets()
    }

    suspend fun toggleCacheFavorite(cacheKey: String, isFavorite: Boolean) {
        reasoningDao.toggleFavorite(cacheKey, isFavorite)
    }

    suspend fun updateCacheNotes(cacheKey: String, notes: String) {
        reasoningDao.updateNotes(cacheKey, notes)
    }

    suspend fun deleteCacheItem(cacheKey: String) {
        reasoningDao.deleteStepsForCacheKey(cacheKey)
        reasoningDao.deleteCachedResult(cacheKey)
    }

    suspend fun clearReasoningCache() {
        reasoningDao.clearAllCache()
        reasoningDao.clearAllSessions()
    }

    private fun generateCacheKey(prompt: String, modelId: String, thinkingLevel: String): String {
        val raw = "${prompt.trim().lowercase()}_${modelId}_${thinkingLevel}"
        return try {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(raw.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            UUID.randomUUID().toString().replace("-", "")
        }
    }

    suspend fun initDefaultModelsIfEmpty() {
        val defaultModels = listOf(
            LocalModelEntity(
                id = "gemini-3.7-flash-think-q4",
                name = "Gemini 3.7 Flash Thinking (Q4_K_M)",
                family = "Gemini 3.7 Series",
                parameterSize = "8.2B",
                quantization = "INT4 Hexagon",
                downloadSizeMb = 2840,
                isDownloaded = true,
                isActive = true,
                computeBackend = "S26 Ultra NPU Turbo",
                contextWindow = "128k Tokens",
                description = "Primary ultra-low latency hybrid reasoning model pre-tuned for complex multi-branch hypotheses and real-time self-correction.",
                version = "v3.7.0-RC1",
                trainingData = "Trained on 15T tokens of web content, textbook corpus, math olympiads, and synthetic CoT self-correction traces.",
                releaseDate = "2026-02-15"
            ),
            LocalModelEntity(
                id = "gemini-3.7-pro-reasoner-q5",
                name = "Gemini 3.7 Pro Reasoner (Q5_K_M)",
                family = "Gemini 3.7 Series",
                parameterSize = "14.5B",
                quantization = "INT5 GGML",
                downloadSizeMb = 4680,
                isDownloaded = false,
                isActive = false,
                computeBackend = "Snapdragon X-Elite / NPU",
                contextWindow = "256k Tokens",
                description = "Deep cognitive proof engine for rigorous mathematics, algorithmic formal verification, and multi-agent pipeline synthesis.",
                version = "v3.7.1-Pro",
                trainingData = "Trained on specialized math datasets (GSM8k, MATH), competitive programming repositories, and high-quality synthetic code chains of thought.",
                releaseDate = "2026-02-28"
            ),
            LocalModelEntity(
                id = "deepseek-r1-distill-8b",
                name = "DeepSeek R1 Distill Qwen 8B CoT",
                family = "DeepSeek Reasoning",
                parameterSize = "8.0B",
                quantization = "AWQ 4-Bit",
                downloadSizeMb = 3920,
                isDownloaded = false,
                isActive = false,
                computeBackend = "Vulkan GPU Acceleration",
                contextWindow = "64k Tokens",
                description = "Distilled open reasoning model specializing in competitive coding heuristics, mathematical Olympiad proofs, and logic riddles.",
                version = "v1.1-R1-Distill",
                trainingData = "Distilled from DeepSeek R1 Full on 800k math/code/reasoning instruction chains generated by the RL teacher model.",
                releaseDate = "2025-12-10"
            ),
            LocalModelEntity(
                id = "gemma-2-9b-thinking",
                name = "Gemma 2 9B Deep Thinking Edition",
                family = "Google Gemma",
                parameterSize = "9.2B",
                quantization = "GGUF Q4_K_S",
                downloadSizeMb = 5100,
                isDownloaded = false,
                isActive = false,
                computeBackend = "S26 Ultra NPU + CPU",
                contextWindow = "32k Tokens",
                description = "High-accuracy open architecture tuned for instruction following, factual retrieval grounding, and nuanced summarization.",
                version = "v2.0.5-Think",
                trainingData = "Pretrained on 8T tokens, fine-tuned with KTO (Kahneman-Tversky Optimization) on factual consistency and grounding datasets.",
                releaseDate = "2025-10-05"
            ),
            LocalModelEntity(
                id = "med-reason-7b",
                name = "MedReason 7B Clinical Diagnostic CoT",
                family = "Specialized Domain",
                parameterSize = "7.0B",
                quantization = "INT4 Quantized",
                downloadSizeMb = 3450,
                isDownloaded = false,
                isActive = false,
                computeBackend = "S26 Ultra NPU",
                contextWindow = "32k Tokens",
                description = "Specialized biomedical reasoning model pre-calibrated on differential diagnoses, pharmacology interactions, and clinical protocols.",
                version = "v1.4.0-Bio",
                trainingData = "Trained on PubMed, PMC articles, clinical trial registers, USMLE training questions, and clinical differential reasoning traces.",
                releaseDate = "2025-11-20"
            ),
            LocalModelEntity(
                id = "qwen-3.8-27b-uncensored-gguf",
                name = "Qwen 3.8 27B Uncensored (GGUF)",
                family = "JonathanColetti / Qwen",
                parameterSize = "27B",
                quantization = "GGUF Q4_K_M",
                downloadSizeMb = 16400,
                isDownloaded = false,
                isActive = false,
                computeBackend = "Vulkan GPU + S26 Ultra NPU",
                contextWindow = "128k Tokens",
                description = "Uncensored 27B parameter open-weights reasoning model by JonathanColetti on Hugging Face. High-capacity multi-turn reasoning, creative problem solving, coding, and unrestricted CoT inference.",
                version = "v1.0-Uncensored",
                trainingData = "Pretrained on Qwen2.5-32B base, fine-tuned on custom high-cognition math and reasoning chains with safety filters bypassed.",
                releaseDate = "2026-01-10"
            )
        )
        chatDao.insertLocalModels(defaultModels)

        // Seed initial offline cached reasoning sessions for instant offline exploration
        seedInitialOfflineCacheIfEmpty()
    }

    private suspend fun seedInitialOfflineCacheIfEmpty() {
        val count = try {
            // Check if any cache exists
            reasoningDao.getCachedResultByKey("sample_riemann_proof")
        } catch (e: Exception) {
            null
        }

        if (count == null) {
            val sampleItems = listOf(
                ReasoningCacheEntity(
                    cacheKey = "sample_riemann_proof",
                    promptQuery = "Analyze the non-trivial zeros of the Riemann Zeta Function and functional equation symmetry.",
                    normalizedQuery = "analyze the non-trivial zeros of the riemann zeta function and functional equation symmetry.",
                    modelId = "gemini-3.7-flash-think-q4",
                    modelName = "Gemini 3.7 Flash Thinking (Q4_K_M)",
                    thinkingLevel = "EXTENDED",
                    thinkingBudgetTokens = 16384,
                    answerContent = """### Riemann Zeta Functional Equation Analysis & Zero Trajectories

1. **Analytic Continuation**: The Riemann zeta function \zeta(s) is analytically continued to the entire complex plane \mathbb{C} \setminus {1} with a simple pole at s = 1.
2. **Functional Equation**:
   \xi(s) = (1/2) s (s - 1) \pi^{-s/2} \Gamma(s/2) \zeta(s)
   satisfying the exact reflection symmetry \xi(s) = \xi(1 - s).
3. **Critical Strip & Line**: All non-trivial zeros reside in the critical strip 0 < Re(s) < 1. The Riemann Hypothesis posits Re(s) = 1/2.
4. **Hardy Z-Function & Gram Points**:
   Z(t) = e^{i \theta(t)} \zeta(1/2 + it)
   Over 10^13 numerical zeros verified on the critical line with zero cross-over deviations.
""".trimIndent(),
                    thoughtProcess = """• [PHASE 1: DECONSTRUCTION] Examining analytic continuation of Dirichlet series ζ(s) = ∑ n^(-s).
• [PHASE 2: FUNCTIONAL EQUATION] Applying Mellin transform to Jacobi theta function θ(τ) = ∑ e^(-π n^2 τ).
• [PHASE 3: REFLECTION] Establishing pole removal at s=0,1 via completed xi-function ξ(s) = ξ(1-s).
• [PHASE 4: HARDY THEOREM] Verified infinite zeros on critical line Re(s) = 1/2 using Borel-Carathéodory lemma.
• [PHASE 5: SYNTHESIS] Formulated rigorous proof summary with GUE random matrix eigenvalue correlation context.""".trimIndent(),
                    reasoningStepsJson = """[
                      {"stepIndex":1,"phase":"DECONSTRUCT","headline":"Mellin Transform & Theta Kernel","details":"Verified modular relation θ(1/t) = sqrt(t)θ(t) to eliminate integration singularities.","confidence":0.99,"branchLabel":"Analytic Baseline","verified":true},
                      {"stepIndex":2,"phase":"EXPLORE_BRANCHES","headline":"Hadamard Product Expansion","details":"Deconstructed ξ(s) as entire order 1 genus product over non-trivial zero pairs ρ and 1-ρ.","confidence":0.97,"branchLabel":"Branch A (Entire Expansion)","verified":true},
                      {"stepIndex":3,"phase":"VERIFY_TRACE","headline":"Gram Law & Hardy Z-Function","details":"Computed sign changes of Z(t) between Gram points g_n. Verified absence of off-line roots.","confidence":0.98,"branchLabel":"Verification Loop","verified":true},
                      {"stepIndex":4,"phase":"SELF_CORRECTION","headline":"Checking Trivial Pole Residues","details":"Double checked Gamma function poles at s = -2n generating trivial zeros. Validated.","confidence":0.99,"branchLabel":"Pole Check","verified":true},
                      {"stepIndex":5,"phase":"SYNTHESIS","headline":"Spectral & GUE Statistical Matrix Synthesis","details":"Correlated zero pair spacings with Montgomery-Odlyzko unitary ensemble distribution.","confidence":0.99,"branchLabel":"Final Synthesis","verified":true}
                    ]""",
                    thinkingDurationMs = 1420L,
                    thinkingTokens = 4280,
                    answerTokens = 680,
                    tokensPerSecond = 3492.0,
                    domainCategory = "Mathematics",
                    cachedAt = System.currentTimeMillis() - 86400000L,
                    lastAccessedAt = System.currentTimeMillis() - 3600000L,
                    hitCount = 4,
                    isFavorite = true,
                    userNotes = "Fundamental number theory proof cached for offline reference.",
                    offlineAvailable = true,
                    isVerified = true
                ),
                ReasoningCacheEntity(
                    cacheKey = "sample_p_vs_np_proof",
                    promptQuery = "Deconstruct the barrier theorems (Relativization, Natural Proofs, Algebrization) in P vs NP separation.",
                    normalizedQuery = "deconstruct the barrier theorems (relativization, natural proofs, algebrization) in p vs np separation.",
                    modelId = "gemini-3.7-flash-think-q4",
                    modelName = "Gemini 3.7 Flash Thinking (Q4_K_M)",
                    thinkingLevel = "HIGH",
                    thinkingBudgetTokens = 8192,
                    answerContent = """### The Three Classical Barriers to Resolving P vs NP

1. **Relativization (Baker-Gill-Solovay, 1975)**:
   - There exists an oracle A such that P^A = NP^A, and an oracle B such that P^B != NP^B.
   - **Implication**: Any proof technique that remains valid in the presence of arbitrary oracles cannot resolve P != NP.

2. **Natural Proofs (Razborov-Rudich, 1997)**:
   - Circuit lower bound strategies relying on natural properties (large cardinality + constructibility) would imply efficient breaking of pseudorandom functions (PRFs).
   - **Implication**: Lower bounds against P/poly require non-natural, non-uniform structural invariants.

3. **Algebrization (Aaronson-Wigderson, 2009)**:
   - Extends relativization to low-degree polynomial extensions over finite fields (IP = PSPACE).
   - **Implication**: Interactive proof and algebraic diagonalization methods alone are insufficient.
""".trimIndent(),
                    thoughtProcess = """• [PHASE 1] Identified complexity theory foundations and classical diagonalization limits.
• [PHASE 2] Traced Baker-Gill-Solovay oracle construction using language L_B in NP^B \ P^B.
• [PHASE 3] Tested Razborov-Rudich PRF security assumption contrapositive.
• [PHASE 4] Formulated Aaronson-Wigderson algebraic oracle A^~ extension matrix.
• [PHASE 5] Synthesized modern non-relativizing avenues (Geometric Complexity Theory, meta-complexity).""".trimIndent(),
                    reasoningStepsJson = """[
                      {"stepIndex":1,"phase":"DECONSTRUCT","headline":"Diagnostic of Diagonalization Methods","details":"Showed Cantor diagonalization relativizes unconditionally across Turing tape heads.","confidence":0.99,"branchLabel":"Oracle Analysis","verified":true},
                      {"stepIndex":2,"phase":"EXPLORE_BRANCHES","headline":"Circuit Lower Bounds & Pseudo-Randomness","details":"Proved natural predicate fails unless strong cryptographic one-way functions do not exist.","confidence":0.96,"branchLabel":"Razborov-Rudich Branch","verified":true},
                      {"stepIndex":3,"phase":"VERIFY_TRACE","headline":"Low-Degree Polynomial Extension","details":"Demonstrated algebraic oracle separation where P^A~ != NP^A~ yet IP^A~ = PSPACE^A~.","confidence":0.98,"branchLabel":"Algebrization Proof","verified":true}
                    ]""",
                    thinkingDurationMs = 1180L,
                    thinkingTokens = 3120,
                    answerTokens = 540,
                    tokensPerSecond = 3101.0,
                    domainCategory = "Logic & Proofs",
                    cachedAt = System.currentTimeMillis() - 43200000L,
                    lastAccessedAt = System.currentTimeMillis() - 1200000L,
                    hitCount = 2,
                    isFavorite = false,
                    userNotes = "Essential computational complexity reference.",
                    offlineAvailable = true,
                    isVerified = true
                ),
                ReasoningCacheEntity(
                    cacheKey = "sample_paxos_raft_proof",
                    promptQuery = "Compare Raft vs Multi-Paxos consensus in Byzantine-safe distributed storage.",
                    normalizedQuery = "compare raft vs multi-paxos consensus in byzantine-safe distributed storage.",
                    modelId = "gemini-3.7-flash-think-q4",
                    modelName = "Gemini 3.7 Flash Thinking (Q4_K_M)",
                    thinkingLevel = "MEDIUM",
                    thinkingBudgetTokens = 4096,
                    answerContent = """### Distributed Consensus: Raft vs Multi-Paxos vs BFT

| Dimension | Multi-Paxos | Raft | PBFT / Tendermint |
| :--- | :--- | :--- | :--- |
| **Leader Model** | Weak / Symmetric proposer | Strong Single Leader | Rotating Leader / Validators |
| **Log Invariants** | Hole filling permitted | Strictly contiguous prefix | Cryptographic Merkle Block Chain |
| **Fault Tolerance** | 2f + 1 nodes (f crashes) | 2f + 1 nodes (f crashes) | 3f + 1 nodes (f malicious) |
| **Failover Cost** | 2-phase Prepare phase | Randomized Heartbeat Election | 3-phase Commit (PrePrepare, Prepare, Commit) |

**Key Architectural Insight**: Raft trades out-of-order log pipelining for formal comprehensibility and strict Leader Append-Only invariant.
""".trimIndent(),
                    thoughtProcess = """• [PHASE 1] Deconstructed state machine replication axioms (Fischer-Lynch-Paterson impossibility).
• [PHASE 2] Analyzed quorum intersection properties (Q1 ∩ Q2 ≠ ∅).
• [PHASE 3] Contrasted Raft log matching property with Paxos phase 2b slot acceptance.
• [PHASE 4] Evaluated Byzantine safety under message forging and split-brain partitions.
• [PHASE 5] Output structured benchmark matrix with implementation tradeoffs.""".trimIndent(),
                    reasoningStepsJson = """[
                      {"stepIndex":1,"phase":"DECONSTRUCT","headline":"FLP Impossibility & Quorum Sizing","details":"Derived quorum threshold floor (N/2 + 1) for crash-fault models under asynchronous networks.","confidence":0.99,"branchLabel":"Consensus Baseline","verified":true},
                      {"stepIndex":2,"phase":"VERIFY_TRACE","headline":"Log Invariant Formal Verification","details":"Proved induction on Leader Completeness Property in Raft Term T.","confidence":0.98,"branchLabel":"Verification Branch","verified":true}
                    ]""",
                    thinkingDurationMs = 890L,
                    thinkingTokens = 2450,
                    answerTokens = 490,
                    tokensPerSecond = 3303.0,
                    domainCategory = "Systems",
                    cachedAt = System.currentTimeMillis() - 172800000L,
                    lastAccessedAt = System.currentTimeMillis() - 7200000L,
                    hitCount = 5,
                    isFavorite = true,
                    userNotes = "Distributed systems design cheatsheet.",
                    offlineAvailable = true,
                    isVerified = true
                )
            )

            reasoningDao.insertCachedResults(sampleItems)

            // Also seed sessions
            sampleItems.forEach { item ->
                reasoningDao.insertSession(
                    DeepThinkingSessionEntity(
                        sessionId = "session_${item.cacheKey}",
                        title = item.promptQuery.take(38) + "...",
                        initialQuery = item.promptQuery,
                        modelId = item.modelId,
                        modelName = item.modelName,
                        domainCategory = item.domainCategory,
                        createdAt = item.cachedAt,
                        updatedAt = item.lastAccessedAt,
                        totalThinkingTimeMs = item.thinkingDurationMs,
                        totalThinkingTokens = item.thinkingTokens,
                        stepsCount = 3,
                        finalSynthesisPreview = item.answerContent.take(80) + "...",
                        isOfflineCached = true,
                        complexityRating = 4,
                        verificationStatus = "VERIFIED",
                        userNotes = item.userNotes
                    )
                )
            }
        }
    }

    suspend fun insertModel(model: LocalModelEntity) {
        chatDao.insertLocalModel(model)
    }

    suspend fun setActiveModel(modelId: String) {
        chatDao.setActiveLocalModel(modelId)
    }

    suspend fun updateModel(model: LocalModelEntity) {
        chatDao.updateLocalModel(model)
    }

    suspend fun saveGeneratedMedia(media: GeneratedMediaEntity) {
        chatDao.insertGeneratedMedia(media)
    }

    suspend fun deleteGeneratedMedia(id: String) {
        chatDao.deleteGeneratedMediaById(id)
    }

    suspend fun saveDocument(document: DocumentEntity) {
        chatDao.insertDocument(document)
    }

    suspend fun deleteDocument(id: String) {
        chatDao.deleteDocumentById(id)
    }

    suspend fun updateDocumentRagStatus(id: String, enabled: Boolean) {
        chatDao.updateRagStatus(id, enabled)
    }

    // --- Benchmark Results Operations ---

    suspend fun saveBenchmarkResult(result: BenchmarkResultEntity) {
        benchmarkDao.insertBenchmarkResult(result)
    }

    suspend fun getLatestBenchmarkResult(): BenchmarkResultEntity? {
        return benchmarkDao.getLatestBenchmarkResultSync()
    }

    suspend fun clearBenchmarkHistory() {
        benchmarkDao.clearAllBenchmarkResults()
    }

    // --- Knowledge Sources Operations ---

    suspend fun initDefaultKnowledgeSourcesIfEmpty() {
        val existing = benchmarkDao.getKnowledgeSourcesList()
        if (existing.isEmpty()) {
            val defaults = listOf(
                KnowledgeSourceEntity(
                    id = "src-google-search",
                    name = "Google Search Grounding",
                    sourceType = "WEB_SEARCH",
                    endpointOrPath = "https://generativelanguage.googleapis.com (googleSearch tool)",
                    isEnabled = true,
                    latencyMs = 185L,
                    status = "ACTIVE",
                    description = "Live real-time web indexing and citations via Gemini Flash search grounding.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-google-maps",
                    name = "Google Maps Geo Engine",
                    sourceType = "MAPS_GEO",
                    endpointOrPath = "https://maps.googleapis.com (googleMaps tool)",
                    isEnabled = true,
                    latencyMs = 160L,
                    status = "ACTIVE",
                    description = "POI location resolution, coordinates, museum floor blueprints & directions.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-hf-mcp-hub",
                    name = "Hugging Face MCP Hub",
                    sourceType = "MCP_HUB",
                    endpointOrPath = "https://huggingface.co/mcp (Model & Dataset Server)",
                    isEnabled = true,
                    latencyMs = 145L,
                    status = "ACTIVE",
                    description = "Model registry, daily AI papers, datasets, spaces, and GGUF quantizations.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-arxiv-academic",
                    name = "arXiv Academic Research",
                    sourceType = "ACADEMIC_ARXIV",
                    endpointOrPath = "https://export.arxiv.org/api/query",
                    isEnabled = true,
                    latencyMs = 210L,
                    status = "ACTIVE",
                    description = "Peer-reviewed scientific preprints, math proofs, quantum computing, and ML research.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-local-rag",
                    name = "On-Device Vector RAG Store",
                    sourceType = "LOCAL_RAG",
                    endpointOrPath = "SQLite Room DB (local_documents + Cosine Matrix)",
                    isEnabled = true,
                    latencyMs = 12L,
                    status = "ACTIVE",
                    description = "Local text embeddings, indexed documents, and zero-latency context injection.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-louvre-corpus",
                    name = "Louvre Cultural Knowledge Base",
                    sourceType = "CULTURAL_WING",
                    endpointOrPath = "Curated Artifacts Repository (Denon, Sully, Richelieu Wings)",
                    isEnabled = true,
                    latencyMs = 8L,
                    status = "ACTIVE",
                    description = "High-fidelity historical artwork analysis, museum room navigation, and artist provenance.",
                    allowsOfflineCaching = true
                ),
                KnowledgeSourceEntity(
                    id = "src-firebase-sync",
                    name = "Firebase Firestore Cloud Sync",
                    sourceType = "FIREBASE_SYNC",
                    endpointOrPath = "firestore.googleapis.com (users/{email}/conversations)",
                    isEnabled = true,
                    latencyMs = 230L,
                    status = "ACTIVE",
                    description = "Secure cloud persistence, multi-device backup, and synchronized reasoning trees.",
                    allowsOfflineCaching = false
                )
            )
            benchmarkDao.insertKnowledgeSources(defaults)
        }
    }

    suspend fun getKnowledgeSourcesList(): List<KnowledgeSourceEntity> {
        return benchmarkDao.getKnowledgeSourcesList()
    }

    suspend fun toggleKnowledgeSource(id: String, isEnabled: Boolean) {
        benchmarkDao.toggleSourceEnabled(id, isEnabled)
    }

    suspend fun updateSourceStatus(id: String, status: String, latencyMs: Long) {
        benchmarkDao.updateSourceStatus(id, status, latencyMs, System.currentTimeMillis())
    }

    suspend fun saveKnowledgeSource(source: KnowledgeSourceEntity) {
        benchmarkDao.insertKnowledgeSource(source)
    }

    // --- Interactive Problem Solving Progress ---
    val allProblemProgress: Flow<List<ProblemProgressEntity>> = benchmarkDao.getAllProblemProgress()

    suspend fun getProblemProgress(problemId: String): ProblemProgressEntity? {
        return benchmarkDao.getProblemProgress(problemId)
    }

    suspend fun saveProblemProgress(progress: ProblemProgressEntity) {
        benchmarkDao.saveProblemProgress(progress)
    }

    suspend fun toggleProblemBookmark(problemId: String, isBookmarked: Boolean) {
        benchmarkDao.toggleProblemBookmark(problemId, isBookmarked)
    }

    suspend fun initDefaultWebWatchersIfEmpty() {
        val existing = reasoningDao.getAnyWatcher()
        if (existing == null) {
            val defaults = listOf(
                WebWatcherEntity(
                    id = "watch-passport-slots",
                    siteName = "Passport/Visa Appointments",
                    url = "https://www.visa-scheduler.com/slots",
                    frequencyHours = 12,
                    scheduledTime = "00:00",
                    active = true,
                    lastCheckedValue = "Checked: slots=0 (Waiting for cancel)",
                    watcherCode = "Check slot availability. Look for HTML element '.available-slots'. Parse count of available slots. If slots > 0, grab immediate slot."
                ),
                WebWatcherEntity(
                    id = "watch-concert-resale",
                    siteName = "Concert Resale Tickets",
                    url = "https://www.ticket-resale.com/taylor-swift-london",
                    frequencyHours = 24,
                    scheduledTime = "08:00",
                    active = true,
                    lastCheckedValue = "Checked: price=$150.00 (Grab enabled)",
                    watcherCode = "Sit on resale list page. Monitor pricing elements '.price-value'. If price < $155.00, grab listing immediately."
                ),
                WebWatcherEntity(
                    id = "watch-vintage-lens",
                    siteName = "Vintage Lens Marketplace Tracker",
                    url = "https://www.marketplace.com/search?q=helios-44-2",
                    frequencyHours = 6,
                    scheduledTime = "18:00",
                    active = true,
                    lastCheckedValue = "Checked: No new listings",
                    watcherCode = "Monitor newly added Helios 44-2 f/2 items. Extract photos and listing description. Alert immediately on listing < $50.00."
                ),
                WebWatcherEntity(
                    id = "watch-gym-booking",
                    siteName = "Gym 6AM Slot Grabber",
                    url = "https://www.gym-class-booking.org/gym6am",
                    frequencyHours = 24,
                    scheduledTime = "23:59",
                    active = true,
                    lastCheckedValue = "Checked: Ready for midnight",
                    watcherCode = "Check class scheduling DOM on gym-class-booking.org at 23:59. Grab the 6:00 AM class slot when unlocked at 00:00."
                )
            )
            for (watcher in defaults) {
                reasoningDao.insertWebWatcher(watcher)
            }
        }
    }

    suspend fun initDefaultWikiPagesIfEmpty() {
        val existing = reasoningDao.getAnyWikiPage()
        if (existing == null) {
            val defaults = listOf(
                WikiPageEntity(
                    id = "wiki-andrej-karpathy",
                    title = "Andrej Karpathy",
                    content = "Renowned AI researcher, co-founder of OpenAI, and former Director of AI at Tesla. Popularized the LLM wiki concept, where an on-device personal assistant maintains a persistent and structured wiki of memory about everything it learns. This app's memory system is based directly on this vision.",
                    tags = "AI, Researchers, Founders",
                    connectionsJson = "[\"wiki-deepthink-project\"]"
                ),
                WikiPageEntity(
                    id = "wiki-deepthink-project",
                    title = "DeepThink Project",
                    content = "The custom core workspace project name of this on-device system. Configured on Samsung Exynos 2600 + NPU architecture. Designed as an always-on, autonomous assistant system with on-device memory, browser automation, and background watch algorithms. Keeps all data securely offline on your phone.",
                    tags = "Workspace, Devices",
                    connectionsJson = "[\"wiki-andrej-karpathy\", \"wiki-octopus-tool-routing\"]"
                ),
                WikiPageEntity(
                    id = "wiki-octopus-tool-routing",
                    title = "Octopus Tool Routing",
                    content = "On-device function calling mechanism. Allows natural language mapping of user commands directly into executable Android action tokens. Supports local tools, on-device database reads/writes, and secure browser integrations without cloud leakage.",
                    tags = "Agent, Systems",
                    connectionsJson = "[\"wiki-deepthink-project\"]"
                )
            )
            for (page in defaults) {
                reasoningDao.insertWikiPage(page)
            }
        }
    }
}


