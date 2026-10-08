package com.example.engine.problemsolving

enum class ProblemDomain(
    val title: String,
    val icon: String,
    val subtitle: String
) {
    CODING("Coding Challenges", "💻", "Concurrency, lock-free queues, dynamic programming, and tree invariants"),
    LOGIC("Logical Puzzles", "🧩", "Riddles, deductive reasoning, truth paradoxes, and information theory"),
    SCIENCE("Scientific Questions", "🔬", "Quantum decoherence, thermodynamics, entropy, and molecular kinetics"),
    MATH("Mathematical Proofs", "📐", "Combinatorics, modular arithmetic, series convergence, and number theory")
}

enum class ProblemDifficulty(
    val title: String,
    val tokenBudget: Int,
    val badgeLabel: String
) {
    INTERMEDIATE("Intermediate", 4096, "MED"),
    HARD("Hard", 8192, "HARD"),
    OLYMPIAD("Olympiad / Grandmaster", 16384, "GRANDMASTER")
}

enum class ProblemSolvingStage(val title: String, val badge: String) {
    PROBLEM_DECONSTRUCTION("Deconstructing Problem & Boundary Conditions", "DECONSTRUCTION"),
    SOCRATIC_HINT("Generating Socratic Guidance & Clues", "PEDAGOGICAL HINT"),
    APPROACH_COMPARISON("Evaluating Alternative Trajectories & Trade-offs", "APPROACH ANALYSIS"),
    USER_ATTEMPT_EVALUATION("Critiquing User Draft & Verifying Invariants", "EVALUATION"),
    DEEP_THINKING_PROOF("Synthesizing Full Formal Proof & Implementation", "FULL PROOF")
}

data class ProblemChallenge(
    val id: String,
    val title: String,
    val domain: ProblemDomain,
    val difficulty: ProblemDifficulty,
    val subtitle: String,
    val description: String,
    val constraints: List<String>,
    val starterPremiseOrCode: String,
    val hints: List<String>,
    val approach1Summary: String,
    val approach2Summary: String,
    val optimalSolutionSnippet: String,
    val formalProofOrExplanation: String,
    val verificationChecklist: List<String>,
    val tags: List<String>
)

data class ApproachTradeoff(
    val name: String,
    val paradigm: String,
    val timeComplexity: String,
    val spaceComplexity: String,
    val pros: String,
    val cons: String,
    val explanation: String
)

data class UserEvaluationResult(
    val scoreOutOf100: Int,
    val isCorrect: Boolean,
    val summaryReview: String,
    val strengths: List<String>,
    val weaknessesOrBugs: List<String>,
    val missingEdgeCases: List<String>,
    val recommendedNextStep: String
)
