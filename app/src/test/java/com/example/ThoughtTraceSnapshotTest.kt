package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.engine.SearchCitation
import com.example.ui.components.ThoughtTraceComponent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi snapshot test suite for ThoughtTraceComponent.
 * Verifies visual consistency across diverse reasoning states:
 * 1. Collapsed preview state
 * 2. Live thinking state with active step indicator
 * 3. Long reasoning chains with multi-step proof breakdown & verified citations
 * 4. Truncated thought text state
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ThoughtTraceSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun thoughtTrace_collapsedState() {
        val sampleSteps = listOf(
            ReasoningStep(
                stepIndex = 1,
                phase = ReasoningPhase.DECONSTRUCT,
                headline = "Analyze Input Constraints",
                details = "Extracting problem variables",
                confidence = 0.94f,
                latencyMs = 80L,
                verified = true
            ),
            ReasoningStep(
                stepIndex = 2,
                phase = ReasoningPhase.VERIFY_TRACE,
                headline = "Topological Proof Synthesis",
                details = "Formulating inductive hypothesis",
                confidence = 0.98f,
                latencyMs = 190L,
                verified = true
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(16.dp)
                ) {
                    ThoughtTraceComponent(
                        thoughtText = "Deconstructing graph topology to evaluate cycle existence theorem...",
                        isThinkingLive = false,
                        durationMs = 1250L,
                        thinkingTokens = 1024,
                        steps = sampleSteps,
                        initialExpanded = false
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/thought_trace_collapsed.png")
    }

    @Test
    fun thoughtTrace_liveThinkingState() {
        val activeStep = ReasoningStep(
            stepIndex = 2,
            phase = ReasoningPhase.EXPLORE_BRANCHES,
            headline = "Evaluating Boundary Conditions & Eigenvalues",
            details = "Computing matrix rank and spectral radius for stability bounds...",
            confidence = 0.91f,
            latencyMs = 320L,
            verified = false
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(16.dp)
                ) {
                    ThoughtTraceComponent(
                        thoughtText = "Constructing Jacobian matrix J(x). Checking stability around fixed point x*...\nEigenvalues calculated: lambda_1 = -0.42, lambda_2 = -1.88. Real parts strictly negative.",
                        isThinkingLive = true,
                        durationMs = 2400L,
                        thinkingTokens = 2048,
                        currentStep = activeStep,
                        initialExpanded = true
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/thought_trace_live_thinking.png")
    }

    @Test
    fun thoughtTrace_longReasoningChainState() {
        val comprehensiveSteps = listOf(
            ReasoningStep(
                stepIndex = 1,
                phase = ReasoningPhase.DECONSTRUCT,
                headline = "Problem Statement & Axiom Formulation",
                details = "Let V be a finite-dimensional inner product space over R. Target: Orthogonal projection lemma.",
                confidence = 0.99f,
                latencyMs = 70L,
                verified = true,
                branchLabel = "root/axioms"
            ),
            ReasoningStep(
                stepIndex = 2,
                phase = ReasoningPhase.EXPLORE_BRANCHES,
                headline = "Gram-Schmidt Orthonormal Sequence",
                details = "Construct orthonormal basis {e_1, ..., e_k} spanning subspace W.",
                confidence = 0.97f,
                latencyMs = 150L,
                verified = true,
                branchLabel = "branch/basis_0"
            ),
            ReasoningStep(
                stepIndex = 3,
                phase = ReasoningPhase.VERIFY_TRACE,
                headline = "Projection Operator Decomposition",
                details = "P_W(v) = sum_{i=1}^k <v, e_i> e_i. Verifying idempotent property P^2 = P and self-adjointness P* = P.",
                confidence = 0.98f,
                latencyMs = 280L,
                verified = true,
                branchLabel = "branch/projection_proof"
            ),
            ReasoningStep(
                stepIndex = 4,
                phase = ReasoningPhase.SELF_CORRECTION,
                headline = "Adversarial Stress Test on Degenerate Subspace",
                details = "Testing case where W = {0} and W = V. Norm inequalities hold identically: ||v - P_W(v)|| <= ||v - w|| for all w in W.",
                confidence = 0.99f,
                latencyMs = 120L,
                verified = true,
                branchLabel = "branch/stress_test"
            ),
            ReasoningStep(
                stepIndex = 5,
                phase = ReasoningPhase.SYNTHESIS,
                headline = "Uniqueness of Best Approximation",
                details = "By Pythagorean theorem on orthogonal complement W^perp, solution is strictly unique in Hilbert space.",
                confidence = 1.0f,
                latencyMs = 90L,
                verified = true,
                branchLabel = "branch/synthesis"
            ),
            ReasoningStep(
                stepIndex = 6,
                phase = ReasoningPhase.VERIFY_TRACE,
                headline = "Formal Q.E.D Verification",
                details = "Trace verified against standard spectral theorems. Zero logical contradictions detected.",
                confidence = 1.0f,
                latencyMs = 45L,
                verified = true,
                branchLabel = "root/verified"
            )
        )

        val citations = listOf(
            SearchCitation(
                title = "Hilbert Space Best Approximation Theorem",
                snippet = "Fundamental theorem of orthogonal projections in inner product spaces.",
                sourceDomain = "mathworld.wolfram.com",
                url = "https://mathworld.wolfram.com/ProjectionTheorem.html",
                freshness = "Verified"
            ),
            SearchCitation(
                title = "Linear Algebra Done Right - Axler",
                snippet = "Chapter 6: Inner Product Spaces and Orthogonal Complements.",
                sourceDomain = "linear.axler.net",
                url = "https://linear.axler.net",
                freshness = "Canonical"
            )
        )

        val fullThoughtText = """
            Phase 1: Parse the user's inquiry regarding minimum-distance projection onto a closed subspace W.
            Phase 2: Formalize inner product space axioms and define distance metric d(v, W) = inf_{w in W} ||v - w||.
            Phase 3: Invoke projection theorem. Construct projection P_W(v) via orthonormal sequence {e_i}.
            Phase 4: Demonstrate orthogonality: for any w in W, <v - P_W(v), w> = 0.
            Phase 5: Apply Pythagorean identity: ||v - w||^2 = ||v - P_W(v)||^2 + ||P_W(v) - w||^2 >= ||v - P_W(v)||^2.
            Phase 6: Conclude that equality holds iff w = P_W(v), proving both existence and strict uniqueness.
        """.trimIndent()

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(16.dp)
                ) {
                    ThoughtTraceComponent(
                        thoughtText = fullThoughtText,
                        isThinkingLive = false,
                        durationMs = 3850L,
                        thinkingTokens = 8192,
                        steps = comprehensiveSteps,
                        citations = citations,
                        initialExpanded = true
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/thought_trace_long_reasoning_chain.png")
    }

    @Test
    fun thoughtTrace_truncatedTextState() {
        val longVerboseThought = """
            [Step 001] Initializing cognitive deconstruction pipeline on complex multi-tier logical constraint.
            [Step 002] Parsing predicate logic clauses: forall x exists y such that P(x, y) implies Q(x).
            [Step 003] Constructing Herbrand universe and semantic tableau tree with 128 branches.
            [Step 004] Exploring branch #1: P(a, f(a)) asserted true, checking consistency with negation ~Q(a).
            [Step 005] Exploring branch #2: Skolemization applied successfully with constant 'c' and function symbol 'g'.
            [Step 006] Evaluating resolution refutation steps over 24 clausal forms.
            [Step 007] Clause 12 resolved with Clause 19 to yield intermediate resolvent R_1.
            [Step 008] Unification algorithm unified variables theta = {x/c, y/g(c)}.
            [Step 009] Empty clause Box derived at depth 14, confirming unsatisfiability of negation.
            [Step 010] Synthesizing final constructive proof steps.
        """.trimIndent()

        composeTestRule.setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground)
                        .padding(16.dp)
                ) {
                    ThoughtTraceComponent(
                        thoughtText = longVerboseThought,
                        isThinkingLive = false,
                        durationMs = 1800L,
                        thinkingTokens = 4096,
                        isTruncatedPreview = true,
                        maxPreviewLines = 4,
                        initialExpanded = true
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/thought_trace_truncated_text.png")
    }
}
