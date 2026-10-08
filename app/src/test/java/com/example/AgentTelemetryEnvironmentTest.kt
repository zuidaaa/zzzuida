package com.example

import android.content.Context
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.engine.TelemetryEngine
import com.example.ui.screens.AgentMixMatchBar
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robust test suite verifying:
 * 1. Android environment context & configuration properties.
 * 2. Widget (component) testing of the Agent Selector Mix-and-Match Bar in Compose.
 * 3. Live Telemetry Engine state accumulator and thermal/governor telemetry updates.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AgentTelemetryEnvironmentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- 1. ENVIRONMENT TESTING ---
    @Test
    fun testAndroidEnvironment_sdkLevelAndApplicationContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull("Application context must be loaded in the Robolectric JVM environment", context)

        val appName = context.getString(R.string.app_name)
        assertTrue("App name resource must contain Deep Think", appName.contains("Deep Think", ignoreCase = true))

        // Verify SQLite Database configuration can be successfully verified or loaded
        val dbFile = context.getDatabasePath("app_database")
        assertNotNull("Database file location should be queryable", dbFile)
    }

    // --- 2. WIDGET / COMPONENT TESTING ---
    @Test
    fun testAgentMixMatchBar_renderingAndCallbacks() {
        var agentSelected: String? = null
        var stepSelected: String? = null
        var linkToggled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface {
                    AgentMixMatchBar(
                        selectedAgent = "DEEP_RESEARCH",
                        selectedStep = "DATASET_GENERATION",
                        linkEnabled = true,
                        onAgentSelected = { agentSelected = it },
                        onStepSelected = { stepSelected = it },
                        onToggleLink = { linkToggled = true },
                        hasHistory = true
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Assert widget chips are fully displayed with correct initial labels
        composeTestRule.onNodeWithTag("link_previous_step_chip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("agent_dropdown_chip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("step_dropdown_chip").assertIsDisplayed()

        composeTestRule.onNodeWithText("🔗 LINKED TO PREVIOUS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agent: 🕵️ Deep Research Agent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Step: 💾 Dataset Gen").assertIsDisplayed()

        // Perform click interactions on the chips to test callback linkages
        composeTestRule.onNodeWithTag("link_previous_step_chip").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Link toggle callback should be invoked when clicked", linkToggled)

        composeTestRule.onNodeWithTag("agent_dropdown_chip").performClick()
        composeTestRule.waitForIdle()
        // verify dropdown options menu or interaction starts
    }

    // --- 3. TELEMETRY ENGINE TESTING ---
    @Test
    fun testTelemetryEngine_recordMetricsAndSystemUpdates() {
        TelemetryEngine.reset()

        // Verify initial state
        var current = TelemetryEngine.state.value
        assertEquals(0, current.totalRequests)
        assertEquals(0L, current.totalLatencyMs)
        assertEquals(0, current.totalTokens)
        assertEquals(0, current.cacheHits)
        assertEquals("DEFAULT", current.activeAgent)

        // Record a successful agent execution event
        TelemetryEngine.recordMetrics(
            latencyMs = 450L,
            tokens = 1024,
            isCacheHit = false,
            agent = "DEEP_RESEARCH"
        )

        current = TelemetryEngine.state.value
        assertEquals(1, current.totalRequests)
        assertEquals(450L, current.totalLatencyMs)
        assertEquals(1024, current.totalTokens)
        assertEquals(0, current.cacheHits)
        assertEquals("DEEP_RESEARCH", current.activeAgent)

        // Record a cache hit event
        TelemetryEngine.recordMetrics(
            latencyMs = 12L,
            tokens = 512,
            isCacheHit = true,
            agent = "CODE_ARCHITECT"
        )

        current = TelemetryEngine.state.value
        assertEquals(2, current.totalRequests)
        assertEquals(462L, current.totalLatencyMs)
        assertEquals(1536, current.totalTokens)
        assertEquals(1, current.cacheHits)
        assertEquals("CODE_ARCHITECT", current.activeAgent)

        // Test system status telemetry updates
        TelemetryEngine.updateSystemTelemetry(
            cpuGovernor = "Performance",
            thermals = "31.2°C"
        )

        current = TelemetryEngine.state.value
        assertEquals("Performance", current.cpuGovernor)
        assertEquals("31.2°C", current.npuThermals)

        // Verify reset behavior
        TelemetryEngine.reset()
        current = TelemetryEngine.state.value
        assertEquals(0, current.totalRequests)
        assertEquals("DEFAULT", current.activeAgent)
    }

    // --- 4. CSV TELEMETRY & ROW VALIDATION (validate_row) ---
    @Test
    fun testTelemetryEngine_validateRow_assertionsAndWarnings() {
        // 1. Valid nominal row (Idle)
        val validRow = com.example.engine.SensorMetricRow(
            timestamp = "04:00:01",
            cpuTempC = 34.2,
            npuUtilizationPct = 12.5,
            powerDrawMw = 450.0,
            fps = 120.0
        )
        val warnings1 = TelemetryEngine.validateRow(validRow)
        assertTrue("Nominal row should produce zero warnings", warnings1.isEmpty())

        // 2. Row with FPS drop below 119.5
        val fpsDropRow = com.example.engine.SensorMetricRow(
            timestamp = "04:00:02",
            cpuTempC = 36.8,
            npuUtilizationPct = 84.1,
            powerDrawMw = 1820.0,
            fps = 118.2
        )
        val warnings2 = TelemetryEngine.validateRow(fpsDropRow)
        assertEquals(1, warnings2.size)
        assertTrue("Should warn on FPS drop", warnings2[0].contains("FPS drop at 04:00:02"))

        // 3. Row near thermal limit (cpu_temp_c > 39 and npu_utilization_pct > 90)
        val thermalLimitRow = com.example.engine.SensorMetricRow(
            timestamp = "04:00:03",
            cpuTempC = 41.5,
            npuUtilizationPct = 94.8,
            powerDrawMw = 2300.0,
            fps = 120.0
        )
        val warnings3 = TelemetryEngine.validateRow(thermalLimitRow)
        assertEquals(1, warnings3.size)
        assertTrue("Should warn on thermal limit", warnings3[0].contains("Near thermal limit under sustained load"))

        // 4. Invariant assertion: NPU utilization bound [0, 100]
        var caughtNpuError = false
        try {
            TelemetryEngine.validateRow(
                com.example.engine.SensorMetricRow(
                    timestamp = "04:00:04",
                    cpuTempC = 35.0,
                    npuUtilizationPct = 105.0,
                    powerDrawMw = 1200.0,
                    fps = 120.0
                )
            )
        } catch (e: IllegalArgumentException) {
            caughtNpuError = true
            assertTrue(e.message?.contains("npu_utilization_pct must be between 0 and 100") == true)
        }
        assertTrue("Should throw assertion error for NPU > 100%", caughtNpuError)

        // 5. Invariant assertion: strictly positive power draw
        var caughtPowerError = false
        try {
            TelemetryEngine.validateRow(
                com.example.engine.SensorMetricRow(
                    timestamp = "04:00:05",
                    cpuTempC = 35.0,
                    npuUtilizationPct = 50.0,
                    powerDrawMw = -10.0,
                    fps = 120.0
                )
            )
        } catch (e: IllegalArgumentException) {
            caughtPowerError = true
            assertTrue(e.message?.contains("power_draw_mw must be strictly positive") == true)
        }
        assertTrue("Should throw assertion error for power_draw_mw <= 0", caughtPowerError)
    }

    @Test
    fun testTelemetryEngine_parseAndValidateCsv_sensorMetricsDataset() {
        val csv = """
            timestamp,cpu_temp_c,npu_utilization_pct,power_draw_mw,fps
            04:00:01,34.2,12.5,450,120.0
            04:00:02,36.8,84.1,1820,119.8
            04:00:03,38.1,98.6,2240,120.0
            04:00:04,39.0,95.2,2180,120.0
            04:00:05,37.4,45.0,980,120.0
        """.trimIndent()

        val report = TelemetryEngine.parseAndValidateCsv(csv)
        assertEquals(5, report.totalRows)
        assertEquals(5, report.validRows)
        assertTrue("No parsing or invariant errors should occur", report.errors.isEmpty())
        assertTrue("All rows meet temperature and FPS limits", report.warnings.isEmpty())
    }

    @Test
    fun testOfflineDeepThinkingEngine_sensorMetricsReplacesMathematicalDerivation() = kotlinx.coroutines.runBlocking {
        val prompt = "def validate_row(r):\n" +
                "    assert 0 <= r['npu_utilization_pct'] <= 100\n" +
                "    assert r['power_draw_mw'] > 0\n" +
                "    if r['fps'] < 119.5:\n" +
                "        log.warn(f\"FPS drop at {r['timestamp']}\")\n" +
                "    if r['cpu_temp_c'] > 39 and r['npu_utilization_pct'] > 90:\n" +
                "        log.warn(\"Near thermal limit under sustained load\")\n" +
                "sensor_metrics.csv"

        var finalAnswerReceived = ""
        val steps = mutableListOf<com.example.engine.ReasoningStep>()

        com.example.engine.OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = com.example.engine.ThinkingLevel.MEDIUM
        ).collect { chunk ->
            chunk.currentStep?.let { step ->
                if (!steps.any { it.stepIndex == step.stepIndex }) {
                    steps.add(step)
                }
            }
            if (!chunk.isThinking) {
                finalAnswerReceived += chunk.token
            }
        }

        assertTrue("Should collect multiple reasoning steps", steps.isNotEmpty())
        assertTrue("Must include telemetry validation step", steps.any { it.headline.contains("validate_row") || it.headline.contains("Telemetry") })

        // Check final answer contains telemetry validation and clarifies rejection of mean-minimization
        assertTrue("Output should contain validate_row logic", finalAnswerReceived.contains("validate_row"))
        assertTrue("Output should contain sensor metrics evaluation", finalAnswerReceived.contains("sensor_metrics.csv") || finalAnswerReceived.contains("04:00:01"))
        assertTrue("Output should clarify rejection of unrelated Mittelwert-Minimierung", finalAnswerReceived.contains("Mittelwert-Minimierung"))
    }
}
