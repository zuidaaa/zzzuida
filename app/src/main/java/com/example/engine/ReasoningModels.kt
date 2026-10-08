package com.example.engine

enum class ThinkingLevel(
    val label: String,
    val tokenBudget: Int,
    val description: String,
    val icon: String
) {
    LOW("Quick (1K)", 1024, "Fast heuristic reasoning for simple queries", "⚡"),
    MEDIUM("Standard (4K)", 4096, "Balanced multi-step reasoning with edge verification", "🧠"),
    HIGH("Deep (8K)", 8192, "Thorough branch exploration, self-correction & proof", "🔬"),
    EXTENDED("Ultra Deep (16K)", 16384, "Exhaustive multi-perspective dialectic & rigorous validation", "🌌")
}

enum class ReasoningPhase(val title: String, val badge: String) {
    DECONSTRUCT("Deconstructing Premises & Constraints", "ANALYSIS"),
    EXPLORE_BRANCHES("Exploring Solution Trajectories", "BRANCHING"),
    VERIFY_TRACE("Formal Execution & Verification", "VERIFICATION"),
    SELF_CORRECTION("Adversarial Critique & Error Checking", "CORRECTION"),
    MCP_TOOL("Hugging Face MCP Hub Tool Execution", "HF MCP"),
    SYNTHESIS("Final Synthesis & Output Construction", "SYNTHESIS")
}

data class ReasoningStep(
    val stepIndex: Int,
    val phase: ReasoningPhase,
    val headline: String,
    val details: String,
    val confidence: Float = 0.95f,
    val branchLabel: String = "Main Branch",
    val verified: Boolean = true,
    val thoughtPayload: String = "",
    val outputPayload: String = "",
    val tokensUsed: Int = 0,
    val latencyMs: Long = 0L,
    val validationRulesChecked: List<String> = emptyList(),
    val nodeId: String = ""
)

data class SearchCitation(
    val title: String,
    val sourceDomain: String,
    val snippet: String,
    val url: String = "",
    val freshness: String = "Live Index"
)

data class ThinkingStreamChunk(
    val isThinking: Boolean, // true if inside <thought>, false if answer text
    val token: String,
    val currentStep: ReasoningStep? = null,
    val thoughtTokenCount: Int = 0,
    val answerTokenCount: Int = 0,
    val searchCitations: List<SearchCitation> = emptyList(),
    val isCacheHit: Boolean = false
)

data class ThinkingResult(
    val answer: String,
    val thoughts: String,
    val steps: List<ReasoningStep>,
    val durationMs: Long,
    val thinkingTokens: Int,
    val answerTokens: Int,
    val modelMode: String,
    val searchCitations: List<SearchCitation> = emptyList()
)

data class AutonomousPipeline(
    val id: String,
    val title: String,
    val category: String,
    val isZeroToken: Boolean,
    val targetPlatform: String,
    val description: String,
    val stages: List<PipelineStage>
)

data class PipelineStage(
    val stageNumber: Int,
    val title: String,
    val assignedAgent: String,
    val status: String = "PENDING", // PENDING, RUNNING, COMPLETED
    val outputPreview: String = ""
)

data class HybridDraft(
    val expertName: String,
    val modelTag: String,
    val focusArea: String,
    val response: String
)

data class HybridCritique(
    val reviewer: String,
    val targetDraft: String,
    val feedback: String
)

data class HybridVotingResult(
    val prompt: String,
    val draftA: HybridDraft,
    val draftB: HybridDraft,
    val critiqueBByA: HybridCritique,
    val critiqueAByB: HybridCritique,
    val judgeModel: String,
    val finalConsolidatedSolution: String,
    val executionTimeMs: Long
)

