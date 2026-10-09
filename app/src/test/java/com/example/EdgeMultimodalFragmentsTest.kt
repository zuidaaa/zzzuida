package com.example

import com.example.engine.edge.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EdgeMultimodalFragmentsTest {

    @Test
    fun astValidator_detectsDangerousPrivilegedCalls() {
        val dangerousCode = """
            fun executeExploit() {
                Runtime.getRuntime().exec("rm -rf /")
            }
        """.trimIndent()

        val report = EdgeAstSafetyValidator.validate(dangerousCode, "kotlin")
        assertFalse("Dangerous call must fail validation", report.isValid)
        assertTrue(report.violations.any { it.contains("Runtime.getRuntime().exec") })
    }

    @Test
    fun astValidator_handlesStringsWithParenthesesWithoutFalsePositives() {
        // String contains parentheses and brackets - naive delimiter counting would fail this!
        val codeWithDelimitersInStrings = """
            fun printStatus() {
                val message = "Status (OK) with [brackets] and {braces}"
                // Comment with unbalanced ( parenthesis
                println(message)
            }
        """.trimIndent()

        val report = EdgeAstSafetyValidator.validate(codeWithDelimitersInStrings, "kotlin")
        assertTrue("Code with delimiters in strings and comments must pass", report.isValid)
        assertEquals("Violations must be empty", 0, report.violations.size)
    }

    @Test
    fun astValidator_catchesUnbalancedDelimitersInCode() {
        val invalidCode = """
            fun broken() {
                val x = (1 + 2
            }
        """.trimIndent()

        val report = EdgeAstSafetyValidator.validate(invalidCode, "kotlin")
        assertFalse("Unbalanced code must fail", report.isValid)
        assertTrue(report.violations.any { it.contains("AST Syntax Violation") })
    }

    @Test
    fun sandboxExecutor_preventsPathTraversal() = runBlocking {
        val tempDir = File.createTempFile("test_sandbox", "").apply {
            delete()
            mkdirs()
        }
        try {
            val executor = EdgeSandboxExecutor(tempDir)
            val files = mutableMapOf<String, String>()

            // Attempt path traversal
            val traversalJson = """
                {
                    "action": "write_file",
                    "path": "../../outside_secret.txt",
                    "content": "Malicious payload"
                }
            """.trimIndent()

            val result = executor.executeToolAction(traversalJson, files)
            assertFalse("Path traversal must be rejected", result.isSuccess)
            assertEquals("Exit code for security error must be 126", 126, result.exitCode)
            assertTrue("Output must state path traversal security error", result.output.contains("Path Traversal"))
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun vectorizationService_computesNormalizedEmbeddings() = runBlocking {
        val service = WorkspaceVectorizationService()
        val embedding = service.generateEmbedding(
            "DashboardView.kt",
            """
                package com.example.ui
                import androidx.compose.runtime.Composable
                import androidx.compose.material3.Text
                @Composable
                fun DashboardView() {
                    Text("Telemetry Dashboard")
                }
            """.trimIndent()
        )

        assertEquals("Dimension must be 128", 128, embedding.dimension)
        assertNotNull("Checksum must not be empty", embedding.sha256Checksum)

        // Verify L2 normalization: sum of squares ≈ 1.0
        var sumSquares = 0f
        for (v in embedding.embeddingVector) {
            sumSquares += (v * v)
        }
        assertTrue("Vector must be normalized (~1.0)", sumSquares in 0.95f..1.05f)

        // Query search
        val matches = service.searchRelevantFiles("dashboard ui screen", listOf(embedding), topK = 1)
        assertEquals("Must match dashboard", 1, matches.size)
        assertTrue("Similarity score should be positive", matches.first().similarityScore > 0f)
    }

    @Test
    fun orchestrator_evaluatesRoutingAndComplexity() {
        val orchestrator = AdaptiveEdgeOrchestrator()
        val simpleDecision = orchestrator.evaluateRouting(
            prompt = "Create a button",
            codeContext = "val x = 1",
            hasVisionInput = false
        )
        assertNotNull(simpleDecision)
        assertTrue("Simple task should have low complexity", simpleDecision.taskComplexityScore < 0.6f)
        assertFalse("Simple task should not detect thermal throttling in normal conditions", simpleDecision.thermalThrottlingDetected)

        val complexDecision = orchestrator.evaluateRouting(
            prompt = "Refactor distributed microservice architecture with cryptography and concurrency pipeline",
            codeContext = "large context ".repeat(300),
            hasVisionInput = true
        )
        assertTrue("Complex task should have high complexity", complexDecision.taskComplexityScore > 0.7f)
        assertEquals("Complex task must route to Cloud Bridge", ExecutionTarget.CLOUD_FALLBACK, complexDecision.selectedTarget)
    }
}
