package com.example.engine

import com.example.data.LocalModelEntity

/**
 * Task Complexity Levels for dynamic LLM model routing.
 */
enum class TaskComplexityLevel(
    val title: String,
    val shortLabel: String,
    val badgeDescription: String,
    val colorHex: Long
) {
    SIMPLE(
        title = "Simple / Fast",
        shortLabel = "Fast",
        badgeDescription = "Fast tasks: Greetings, brief lookups, translations, concise formatting",
        colorHex = 0xFF00E5FF // Cyan
    ),
    MODERATE(
        title = "Moderate / General",
        shortLabel = "General",
        badgeDescription = "General tasks: Summaries, comparisons, conversational questions, creative writing",
        colorHex = 0xFF818CF8 // Indigo
    ),
    COMPLEX(
        title = "Complex / Deep Thinking",
        shortLabel = "Deep Reasoning",
        badgeDescription = "Complex tasks: Math proofs, algorithm analysis, code architecture, formal logic",
        colorHex = 0xFFA855F7 // Purple
    ),
    SPECIALIZED_BIOMEDICAL(
        title = "Specialized / Clinical",
        shortLabel = "Biomedical",
        badgeDescription = "Clinical diagnostics, pharmacology, medical protocols",
        colorHex = 0xFF10B981 // Emerald
    )
}

/**
 * Result of task complexity evaluation and dynamic model selection.
 */
data class ModelRoutingPlan(
    val complexity: TaskComplexityLevel,
    val selectedGeminiModel: String,
    val selectedLocalModelId: String,
    val selectedLocalModelName: String,
    val recommendedThinkingLevel: ThinkingLevel,
    val decisionReason: String,
    val matchedSignals: List<String>,
    val confidenceScore: Float
)

/**
 * Analyzes prompt text and conversation context to dynamically switch models
 * based on task complexity.
 */
object TaskComplexityRouter {

    private val COMPLEX_MATH_PROOF_KEYWORDS = listOf(
        "proof", "prove", "theorem", "lemma", "riemann", "zeta", "dijkstra",
        "p vs np", "calculus", "matrix", "eigenvalue", "eigenvector", "formal verification",
        "induction", "derivative", "integral", "paxos", "raft", "byzantine", "consensus",
        "concurrency", "ast", "np-hard", "np-complete", "big-o", "time complexity",
        "quantum", "cryptography", "rsa", "elliptic curve", "turing machine", "halting problem",
        "relativization", "natural proof", "algebrization", "fischer-lynch-paterson", "flp"
    )

    private val COMPLEX_CODING_KEYWORDS = listOf(
        "architecture", "refactor", "design pattern", "microservice", "memory leak",
        "profiling", "concurrency bug", "deadlock", "race condition", "coroutine",
        "red-black tree", "binary search tree", "dynamic programming", "graph algorithm",
        "assembler", "sve2", "neon vector", "compiler optimization", "llvm"
    )

    private val BIOMEDICAL_KEYWORDS = listOf(
        "diagnosis", "differential diagnosis", "clinical", "pharmacology", "dosage",
        "pathology", "symptom", "treatment protocol", "biomedical", "contraindication",
        "pharmacokinetics", "oncology", "cardiology", "neurology"
    )

    private val SIMPLE_FAST_KEYWORDS = listOf(
        "hello", "hi", "hey", "hallo", "guten tag", "morgen", "danke", "thanks", "thank you",
        "what is the capital", "who is", "define", "definition of", "translate to",
        "übersetze", "spell check", "fix typo", "synonym", "antonym", "current date",
        "short summary", "in 1 sentence", "in one sentence", "yes or no"
    )

    private fun matchesKeyword(text: String, keyword: String): Boolean {
        return if (keyword.contains(" ") || keyword.contains("-") || keyword.contains("/")) {
            text.contains(keyword)
        } else {
            Regex("\\b${Regex.escape(keyword)}\\b").containsMatchIn(text)
        }
    }

    /**
     * Evaluates task complexity and selects the best model for both Online (Gemini)
     * and Offline (Local on-device) execution.
     */
    fun analyzeTaskComplexity(
        prompt: String,
        conversationTurnCount: Int = 1,
        availableLocalModels: List<LocalModelEntity> = emptyList()
    ): ModelRoutingPlan {
        val lower = prompt.trim().lowercase()
        val wordCount = prompt.split("\\s+".toRegex()).size

        val matchedComplexMath = COMPLEX_MATH_PROOF_KEYWORDS.filter { matchesKeyword(lower, it) }
        val matchedComplexCode = COMPLEX_CODING_KEYWORDS.filter { matchesKeyword(lower, it) }
        val matchedBiomedical = BIOMEDICAL_KEYWORDS.filter { matchesKeyword(lower, it) }
        val matchedSimple = SIMPLE_FAST_KEYWORDS.filter { matchesKeyword(lower, it) }

        // Formula / Math detection (e.g., LaTeX syntax, equations like x^2, O(N log N), \sum)
        val hasMathEquations = lower.contains("o(") || lower.contains("\\") || lower.contains("∑") ||
                lower.contains("∫") || lower.contains("≠") || lower.contains("≤") || lower.contains("≥") ||
                lower.contains("^") || lower.contains("lim ") || lower.contains("->")

        // 1. Biomedical Specialized check
        if (matchedBiomedical.size >= 2 || (matchedBiomedical.isNotEmpty() && lower.contains("patient"))) {
            val localMedModel = availableLocalModels.find { it.id.contains("med", ignoreCase = true) }
            return ModelRoutingPlan(
                complexity = TaskComplexityLevel.SPECIALIZED_BIOMEDICAL,
                selectedGeminiModel = GeminiApiClient.MODEL_PRO,
                selectedLocalModelId = localMedModel?.id ?: "med-reason-7b",
                selectedLocalModelName = localMedModel?.name ?: "MedReason 7B Clinical Diagnostic CoT",
                recommendedThinkingLevel = ThinkingLevel.HIGH,
                decisionReason = "Detected clinical and diagnostic terminology. Switched to domain-specialized reasoner.",
                matchedSignals = matchedBiomedical,
                confidenceScore = 0.94f
            )
        }

        // 2. High Complexity: Math proofs, algorithmic reasoning, deep code architecture
        val complexSignals = matchedComplexMath + matchedComplexCode + (if (hasMathEquations) listOf("Math Formula Syntax") else emptyList())
        if (complexSignals.size >= 2 || (matchedComplexMath.isNotEmpty() && wordCount > 15) || (hasMathEquations && wordCount > 20)) {
            val localProModel = availableLocalModels.find {
                it.isDownloaded && (it.id.contains("pro", ignoreCase = true) || it.id.contains("r1", ignoreCase = true) || it.id.contains("qwen", ignoreCase = true))
            } ?: availableLocalModels.find { it.id.contains("pro", ignoreCase = true) }

            return ModelRoutingPlan(
                complexity = TaskComplexityLevel.COMPLEX,
                selectedGeminiModel = GeminiApiClient.MODEL_PRO,
                selectedLocalModelId = localProModel?.id ?: "gemini-3.7-pro-reasoner-q5",
                selectedLocalModelName = localProModel?.name ?: "Gemini 3.7 Pro Reasoner (Q5_K_M)",
                recommendedThinkingLevel = if (complexSignals.size >= 3) ThinkingLevel.EXTENDED else ThinkingLevel.HIGH,
                decisionReason = "Detected deep reasoning task (Proofs / Formal Algorithms / Architecture). Switched to Pro Reasoner with high thinking budget.",
                matchedSignals = complexSignals,
                confidenceScore = 0.96f
            )
        }

        // 3. Simple / Fast: Short greetings, quick definitions, lightweight translations
        if (matchedSimple.isNotEmpty() && wordCount <= 22 && complexSignals.isEmpty()) {
            val localFastModel = availableLocalModels.find {
                it.isDownloaded && (it.id.contains("flash", ignoreCase = true) || it.parameterSize.contains("8") || it.parameterSize.contains("9"))
            } ?: availableLocalModels.find { it.id.contains("flash", ignoreCase = true) }

            return ModelRoutingPlan(
                complexity = TaskComplexityLevel.SIMPLE,
                selectedGeminiModel = GeminiApiClient.MODEL_FLASH_LITE,
                selectedLocalModelId = localFastModel?.id ?: "gemini-3.7-flash-think-q4",
                selectedLocalModelName = localFastModel?.name ?: "Gemini 3.7 Flash Thinking (Q4_K_M)",
                recommendedThinkingLevel = ThinkingLevel.LOW,
                decisionReason = "Detected lightweight query. Switched to Flash-Lite for ultra-fast latency and minimal token consumption.",
                matchedSignals = matchedSimple,
                confidenceScore = 0.91f
            )
        }

        // 4. Default: Moderate / General
        val localDefaultModel = availableLocalModels.find { it.isDownloaded }
            ?: availableLocalModels.find { it.id.contains("flash", ignoreCase = true) }

        return ModelRoutingPlan(
            complexity = TaskComplexityLevel.MODERATE,
            selectedGeminiModel = GeminiApiClient.MODEL_FLASH,
            selectedLocalModelId = localDefaultModel?.id ?: "gemini-3.7-flash-think-q4",
            selectedLocalModelName = localDefaultModel?.name ?: "Gemini 3.7 Flash Thinking (Q4_K_M)",
            recommendedThinkingLevel = ThinkingLevel.MEDIUM,
            decisionReason = "Detected standard conversational / synthesis inquiry. Switched to Gemini 3.5 Flash for balanced depth and throughput.",
            matchedSignals = if (complexSignals.isNotEmpty()) complexSignals else listOf("General inquiry"),
            confidenceScore = 0.88f
        )
    }
}
