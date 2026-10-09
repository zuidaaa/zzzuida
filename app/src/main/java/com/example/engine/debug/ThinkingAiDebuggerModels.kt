package com.example.engine.debug

enum class LlmPipelineSegment(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    CONTEXT_WINDOW("context", "Context Window & KV Cache", "Monitors token count, context decay, and KV memory allocation", "Memory"),
    PROMPT_TEMPLATES("prompt", "Prompt & Schema Construction", "Validates prompt injection, JSON schema bounds, and template parameters", "Code"),
    REASONING_GRAPH("reasoning", "Inference & Thinking Graph", "Inspects thought trace steps, loop detection, and token velocity", "Psychology"),
    GUARDRAILS_SAFETY("guardrails", "Guardrails & Output Validation", "Verifies toxicity scores, hallucination bounds, and tool calling schemas", "Shield"),
    SYSTEM_RESOURCES("resources", "Hardware & Crash Prevention", "Tracks RAM/OOM proximity, thermal throttling, and socket stability", "Speed")
}

enum class SegmentHealthStatus(val label: String) {
    OPTIMAL("Optimal"),
    WARNING("Degraded"),
    CRITICAL_RISK("Critical Risk"),
    CRASH_PREVENTED("Crash Prevented")
}

data class ThinkingDiagnosticStep(
    val stepNumber: Int,
    val segment: LlmPipelineSegment,
    val reasoningText: String,
    val actionTaken: String?,
    val timestamp: Long = System.currentTimeMillis()
)

data class CrashVector(
    val id: String = java.util.UUID.randomUUID().toString(),
    val segment: LlmPipelineSegment,
    val title: String,
    val rootCause: String,
    val severity: String, // "HIGH", "CRITICAL", "CATASTROPIC"
    val remediationStrategy: String,
    val isAutoFixActive: Boolean = true,
    val isMitigated: Boolean = false
)

data class SegmentTelemetry(
    val segment: LlmPipelineSegment,
    val healthStatus: SegmentHealthStatus,
    val metricValue: String,
    val details: Map<String, String>,
    val diagnosticTrace: List<String>,
    val detectedRisks: List<CrashVector>
)

data class ThinkingDebuggerState(
    val isLiveDiagnosticActive: Boolean = true,
    val autoCrashMitigationEnabled: Boolean = true,
    val totalCrashesPrevented: Int = 14,
    val overallSystemHealthScore: Int = 96, // 0 - 100
    val activeThinkingSteps: List<ThinkingDiagnosticStep> = emptyList(),
    val segmentTelemetryList: List<SegmentTelemetry> = emptyList()
)
