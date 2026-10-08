package com.example.engine.problemsolving

import com.example.engine.OfflineDeepThinkingEngine
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.engine.ThinkingLevel
import com.example.engine.ThinkingStreamChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object OfflineProblemSolvingEngine {

    /**
     * Generates a Socratic hint that guides the user toward discovering the solution themselves.
     */
    fun streamSocraticHint(
        challenge: ProblemChallenge,
        hintIndex: Int, // 0-based
        userDraft: String = ""
    ): Flow<ThinkingStreamChunk> = flow {
        val safeHintIdx = hintIndex.coerceIn(0, challenge.hints.size - 1)
        val selectedHint = challenge.hints[safeHintIdx]

        val prompt = """
            You are a world-class cognitive tutor and Deep Thinking mentor.
            The user is working on the following problem:
            
            PROBLEM TITLE: ${challenge.title} (${challenge.domain.title})
            DIFFICULTY: ${challenge.difficulty.title}
            DESCRIPTION: ${challenge.description}
            
            USER'S CURRENT ATTEMPT / NOTES:
            ${if (userDraft.isNotBlank()) userDraft else "(The user has not yet typed an attempt. They are stuck at the beginning.)"}
            
            PEDAGOGICAL HINT TIER: Hint ${safeHintIdx + 1} of ${challenge.hints.size}
            TARGET CLUE: $selectedHint
            
            INSTRUCTIONS:
            1. Provide an encouraging, insightful Socratic hint based on the Target Clue.
            2. Do NOT give away the complete code or full final derivation.
            3. Ask 1-2 sharp guiding questions about invariants, symmetries, or boundary conditions.
            4. Emphasize why naive intuition fails and where the cognitive breakthrough lies.
        """.trimIndent()

        OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = ThinkingLevel.MEDIUM,
            systemInstruction = "You are an elite Socratic mentor in ${challenge.domain.title}. Guide the student using progressive hints without spoiling the ultimate solution.",
            searchGrounded = false,
            maxTokens = 2048
        ).collect { chunk ->
            emit(chunk)
        }
    }

    /**
     * Compares alternative conceptual approaches and algorithmic trade-offs.
     */
    fun streamApproachAnalysis(
        challenge: ProblemChallenge
    ): Flow<ThinkingStreamChunk> = flow {
        val prompt = """
            DEEP THINKING TRAJECTORY EXPLORATION & PARADIGM COMPARISON:
            
            PROBLEM: ${challenge.title}
            DOMAIN: ${challenge.domain.title}
            DIFFICULTY: ${challenge.difficulty.title}
            
            DESCRIPTION:
            ${challenge.description}
            
            CONSTRAINTS:
            ${challenge.constraints.joinToString("\n- ", prefix = "- ")}
            
            KNOWN APPROACH 1 (Naive or Classical Baseline):
            ${challenge.approach1Summary}
            
            OPTIMAL APPROACH 2 (Deep Thinking Trajectory):
            ${challenge.approach2Summary}
            
            INSTRUCTIONS:
            Provide a structured, deep comparative analysis:
            1. **Approach 1 (Baseline)**: Complexity, failure modes, or bottlenecks.
            2. **Approach 2 (Optimal / Deep Thinking)**: Core invariant, state representation, or mathematical lemma.
            3. **Trade-off Matrix**: Time, space, and implementation complexity comparisons.
            4. **Key Epiphany**: What fundamental symmetry or cancellation enables the leap?
        """.trimIndent()

        OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = ThinkingLevel.HIGH,
            systemInstruction = "You are a master systems architect and mathematician analyzing problem-solving paradigms and complexity trade-offs.",
            searchGrounded = false,
            maxTokens = 4096
        ).collect { chunk ->
            emit(chunk)
        }
    }

    /**
     * Evaluates a user's attempt, scoring correctness and detecting subtle edge cases or bugs.
     */
    fun streamUserDraftEvaluation(
        challenge: ProblemChallenge,
        userDraft: String
    ): Flow<ThinkingStreamChunk> = flow {
        val prompt = """
            CRITIQUE & INVARIANT VERIFICATION OF USER ATTEMPT:
            
            PROBLEM: ${challenge.title} (${challenge.domain.title})
            DESCRIPTION:
            ${challenge.description}
            
            CONSTRAINTS:
            ${challenge.constraints.joinToString("\n- ", prefix = "- ")}
            
            GOLD STANDARD SOLUTION SUMMARY:
            ${challenge.approach2Summary}
            
            USER'S ATTEMPT / PROPOSED SOLUTION:
            \"\"\"
            ${if (userDraft.isNotBlank()) userDraft else "// Empty attempt provided"}
            \"\"\"
            
            INSTRUCTIONS FOR COGNITIVE CRITIQUE:
            1. **Preliminary Assessment**: Is the core direction sound, partially correct, or fundamentally flawed?
            2. **Formal Invariant Checks**: Which constraints or invariants are satisfied? Which are violated?
            3. **Edge Case Analysis**: Provide at least 2 specific edge cases or counterexamples where this draft would break.
            4. **Constructive Recommendation**: The exact next step the user should take to fix the flaws.
            5. **Estimated Score**: Grade the attempt out of 100 based on rigor, soundness, and edge case safety.
        """.trimIndent()

        OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = ThinkingLevel.HIGH,
            systemInstruction = "You are an adversarial proof verifier and code reviewer testing user solutions for correctness and boundary failures.",
            searchGrounded = false,
            maxTokens = 3500
        ).collect { chunk ->
            emit(chunk)
        }
    }

    /**
     * Synthesizes the full deep thinking proof, code implementation, and formal explanation.
     */
    fun streamFullDeepThinkingSolution(
        challenge: ProblemChallenge
    ): Flow<ThinkingStreamChunk> = flow {
        val prompt = """
            SYNTHESIZE FULL DEEP THINKING FORMAL PROOF AND OPTIMAL IMPLEMENTATION:
            
            PROBLEM: ${challenge.title} (${challenge.domain.title})
            DIFFICULTY: ${challenge.difficulty.title}
            
            STATEMENT:
            ${challenge.description}
            
            CONSTRAINTS:
            ${challenge.constraints.joinToString("\n- ", prefix = "- ")}
            
            REFERENCE CODE / DERIVATION:
            ${challenge.optimalSolutionSnippet}
            
            DEEP THINKING PROOF NOTES:
            ${challenge.formalProofOrExplanation}
            
            INSTRUCTIONS:
            Execute the complete 5-stage cognitive pipeline:
            1. **Premise Deconstruction**: Formal axioms, state variables, and bounds.
            2. **Solution Trajectory Exploration**: Why naive approaches fail and how the optimal path emerges.
            3. **Complete Implementation or Proof**: Provide clean, idiomatic, fully commented code or rigorous mathematical derivation with equations.
            4. **Adversarial Verification**: Walk through edge cases and prove termination/correctness.
            5. **Final Synthesis**: Summary of takeaways, algorithmic insights, and generalizable patterns.
        """.trimIndent()

        OfflineDeepThinkingEngine.processPromptStream(
            prompt = prompt,
            thinkingLevel = when (challenge.difficulty) {
                ProblemDifficulty.OLYMPIAD -> ThinkingLevel.EXTENDED
                ProblemDifficulty.HARD -> ThinkingLevel.HIGH
                else -> ThinkingLevel.MEDIUM
            },
            systemInstruction = "You are Gemini 3.7 Deep Thinking on-device engine delivering rigorous, exhaustive, zero-shortcut problem solutions.",
            searchGrounded = false,
            maxTokens = 6000
        ).collect { chunk ->
            emit(chunk)
        }
    }
}
