package com.example

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.engine.EngineMode
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.engine.SearchCitation
import com.example.engine.ThinkingLevel
import com.example.engine.ThoughtTraceExportData
import com.example.ui.components.ExportThoughtTraceDialog
import com.example.ui.components.SearchCitationCard
import com.example.ui.components.ThinkingControlsBar
import com.example.ui.components.ThoughtAccordion
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Extended Widget / Component tests in Jetpack Compose using Robolectric & ComposeTestRule.
 * Tests individual UI components, interactive states, clicks, tab switching, and callbacks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExtendedWidgetComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testThoughtAccordion_expandedAndStepInteractions() {
        var inspectTreeClicked = false
        var debuggedNodeIndex: Int? = null

        val sampleSteps = listOf(
            ReasoningStep(
                stepIndex = 1,
                phase = ReasoningPhase.DECONSTRUCT,
                headline = "Deconstruct Query Semantics",
                details = "Analyzing constraint satisfaction",
                confidence = 0.95f,
                latencyMs = 120L,
                verified = true,
                branchLabel = "root/deconstruct",
                thoughtPayload = "Identified 3 sub-clauses"
            ),
            ReasoningStep(
                stepIndex = 2,
                phase = ReasoningPhase.VERIFY_TRACE,
                headline = "Formal Deduction Graph",
                details = "Iterating topological branch",
                confidence = 0.98f,
                latencyMs = 240L,
                verified = true,
                branchLabel = "branch/deduction_0",
                thoughtPayload = "Q.E.D reached"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface {
                    ThoughtAccordion(
                        thoughtText = "Initiating multi-clause topological verification step...",
                        isThinkingLive = false,
                        durationMs = 1450L,
                        thinkingTokens = 2048,
                        steps = sampleSteps,
                        currentStep = sampleSteps.last(),
                        onInspectTree = { inspectTreeClicked = true },
                        onDebugNodes = { index -> debuggedNodeIndex = index }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify thought metrics and header tag
        composeTestRule.onNodeWithTag("thought_accordion_header").assertIsDisplayed()

        // Click accordion to expand
        composeTestRule.onNodeWithTag("thought_accordion_header").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Step 1: Deconstruct Query Semantics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Step 2: Formal Deduction Graph").assertIsDisplayed()

        // Click Inspect Tree action button
        composeTestRule.onNodeWithTag("inspect_reasoning_tree_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Inspect tree callback should be triggered", inspectTreeClicked)

        // Click Debug CoT Nodes button
        composeTestRule.onNodeWithTag("open_cot_debug_overlay_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, debuggedNodeIndex)
    }

    @Test
    fun testExportThoughtTraceDialog_tabSwitchingAndDismiss() {
        var dismissClicked = false

        val exportData = ThoughtTraceExportData(
            prompt = "Prove convergence of the series using ratio test",
            thoughtProcess = "Applying d'Alembert's ratio test: limit as n approaches infinity...",
            reasoningSteps = listOf(
                ReasoningStep(
                    stepIndex = 1,
                    phase = ReasoningPhase.VERIFY_TRACE,
                    headline = "Ratio Limit Form",
                    details = "lim |a_{n+1}/a_n| < 1",
                    confidence = 0.99f,
                    latencyMs = 90L,
                    verified = true
                )
            ),
            finalResponse = "The series converges absolutely by the Ratio Test.",
            modelName = "Gemini 3.7 Deep Thinking",
            thinkingDurationMs = 1820L,
            thinkingTokens = 4096,
            searchCitations = listOf(
                SearchCitation(
                    title = "Ratio Test Proof",
                    snippet = "Standard absolute convergence criteria",
                    sourceDomain = "mathworld.wolfram.com",
                    url = "https://mathworld.wolfram.com/RatioTest.html",
                    freshness = "Verified"
                )
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ExportThoughtTraceDialog(
                    exportData = exportData,
                    onDismiss = { dismissClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify Dialog is displayed
        composeTestRule.onNodeWithTag("export_dialog_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("export_markdown_tab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_markdown_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("share_markdown_file_button").assertIsDisplayed()

        // Switch to PDF Tab
        composeTestRule.onNodeWithTag("export_pdf_tab").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("share_pdf_button").performScrollTo().assertIsDisplayed()

        // Click close button
        composeTestRule.onNodeWithTag("close_export_dialog_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue("onDismiss should be called on close", dismissClicked)
    }

    @Test
    fun testThinkingControlsBar_selectionInteractions() {
        var selectedLevel: ThinkingLevel = ThinkingLevel.HIGH
        var engineToggled = false
        var searchToggled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface {
                    ThinkingControlsBar(
                        selectedLevel = selectedLevel,
                        engineMode = EngineMode.OFFLINE_GEMINI_37,
                        searchGroundingEnabled = false,
                        activeModelName = "gemini-3.7-deep-think",
                        onLevelSelected = { selectedLevel = it },
                        onToggleEngineMode = { engineToggled = true },
                        onToggleSearchGrounding = { searchToggled = true }
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify controls bar is rendered
        composeTestRule.onNodeWithTag("thinking_level_high").assertIsDisplayed()
        composeTestRule.onNodeWithTag("thinking_level_low").assertIsDisplayed()

        // Click Low Thinking Level Chip
        composeTestRule.onNodeWithTag("thinking_level_low").performClick()
        composeTestRule.waitForIdle()
        assertEquals(ThinkingLevel.LOW, selectedLevel)

        // Click Search Grounding Toggle
        composeTestRule.onNodeWithTag("search_grounding_toggle").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Search grounding toggle callback should be invoked", searchToggled)

        // Click Engine Mode Toggle
        composeTestRule.onNodeWithTag("engine_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Engine toggle callback should be invoked", engineToggled)
    }

    @Test
    fun testSearchCitationCard_rendersSnippetAndSource() {
        val citation = SearchCitation(
            title = "Quantum Supremacy Benchmark",
            snippet = "Experimental realization of random circuit sampling in 53 qubits",
            sourceDomain = "nature.com",
            url = "https://nature.com/articles/s41586-019-1666-5",
            freshness = "2024 Grounding"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Surface {
                    SearchCitationCard(
                        citation = citation
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Quantum Supremacy Benchmark").assertIsDisplayed()
        composeTestRule.onNodeWithText("nature.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024 Grounding").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_citation_card").assertIsDisplayed()
    }
}
