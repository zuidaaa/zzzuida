package com.example.engine.edge

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Target device quantizations for Mobile LLMs on Edge runtimes (llama.cpp / MLC / NPU).
 */
enum class ModelQuantization(
    val label: String,
    val bitsPerWeight: Float,
    val memoryFootprintMb: Int,
    val throughputTpsExynos2600: Float,
    val isRecommendedForEdge: Boolean
) {
    INT4("INT4 (Q4_K_M)", 4.5f, 1350, 48.2f, true),
    INT3("INT3 (Q3_K_S)", 3.4f, 980, 56.4f, true),
    INT2("INT2 (IQ2_XXS)", 2.2f, 640, 62.0f, false),
    FP16("FP16 (Half Precision)", 16.0f, 4800, 11.5f, false)
}

/**
 * Execution target for the Edge Orchestration Layer.
 */
enum class ExecutionTarget(val displayName: String, val latencyCategory: String) {
    EDGE_NPU("Local Exynos Cortex-X + NPU (llama.cpp)", "Ultra-Low Latency (<30ms)"),
    EDGE_CPU_SVE2("Local CPU SVE2 SIMD Vector", "Low Latency (<70ms)"),
    CLOUD_FALLBACK("Cloud Gateway (PyTorch/Gemini API)", "High Throughput / Deep Verification")
}

/**
 * Sandbox Execution Tool Actions structured according to Phase 0 Tool-Calling specs:
 * {"action": "...", "path": "...", "content": "..."}
 */
data class AgentToolAction(
    val action: String, // write_file, read_file, execute_code, run_tests, fetch_url, ast_validate
    val path: String = "",
    val content: String = "",
    val arguments: Map<String, String> = emptyMap()
)

/**
 * Outcome of an isolated sandbox command execution.
 */
data class SandboxExecutionResult(
    val action: String,
    val isSuccess: Boolean,
    val exitCode: Int,
    val output: String,
    val durationMs: Long,
    val memoryUsageKb: Long = 0L,
    val astValidationPassed: Boolean = true,
    val astViolationReason: String? = null
)

/**
 * Vision Feature Vector extracted by LiteViT5 / Mobile VLM encoder (Phase 1).
 */
data class VisionFeatureVector(
    val dimension: Int = 512,
    val features: FloatArray,
    val detectedUiElements: List<String> = emptyList(),
    val layoutBoundingBoxes: List<String> = emptyList(),
    val extractionLatencyMs: Long = 0L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as VisionFeatureVector
        return features.contentEquals(other.features)
    }

    override fun hashCode(): Int {
        return features.contentHashCode()
    }
}

/**
 * Telemetry and hardware state for Adaptive Edge Orchestration (Phase 2).
 */
data class EdgeTelemetryState(
    val cpuUtilizationPercent: Float,
    val thermalStatus: String, // "NOMINAL", "FAIR", "SERIOUS", "CRITICAL"
    val batteryTemperatureCelsius: Float,
    val availableRamMb: Long,
    val npuThermalHeadroom: Float,
    val complexityScore: Float, // 0.0 (Trivial) to 1.0 (Extreme)
    val recommendedTarget: ExecutionTarget
)

/**
 * State snapshot of the workspace and KV-Cache to prevent state loss during context switching.
 */
data class WorkspaceSnapshot(
    val snapshotId: String,
    val timestamp: Long,
    val activeFiles: Map<String, String>,
    val serializedKvCacheBytes: Long,
    val vectorEmbeddingChecksum: String,
    val summary: String
)

/**
 * Semantic Vector Embedding for a Workspace File (Phase State Management - Point 2).
 * Captures semantic meaning of code files using a compact local embedding model without transmitting entire source code.
 */
data class FileVectorEmbedding(
    val filePath: String,
    val embeddingVector: FloatArray,
    val dimension: Int = 128,
    val tokenCount: Int,
    val summarySnippet: String,
    val sha256Checksum: String,
    val generatedAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FileVectorEmbedding
        return filePath == other.filePath && embeddingVector.contentEquals(other.embeddingVector)
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + embeddingVector.contentHashCode()
        return result
    }
}

/**
 * Result of a semantic search or relevant context retrieval across workspace vector embeddings.
 */
data class WorkspaceSemanticSearchResult(
    val filePath: String,
    val similarityScore: Float,
    val matchingSnippet: String,
    val vectorDimension: Int = 128
)

/**
 * Real-time Speech-to-Code state (Phase 3).
 */
data class SpeechCodingFrame(
    val transcript: String,
    val isFinal: Boolean,
    val audioDurationMs: Long,
    val acousticConfidence: Float,
    val activeCodeState: String,
    val suggestedRefinements: List<String> = emptyList()
)

/**
 * Pipeline Event for tracking step-by-step progress in the Multimodal Agent.
 */
data class EdgePipelineEvent(
    val phase: Int, // 0, 1, 2, 3
    val stepTitle: String,
    val detail: String,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val payloadPreview: String = ""
)

/**
 * Report containing AST validation and safety inspection results.
 */
data class AstValidationReport(
    val isValid: Boolean,
    val violations: List<String>,
    val warnings: List<String>,
    val astNodeCount: Int,
    val checkedLanguage: String
)

