package com.example.engine

import android.content.Context
import android.os.Build
import android.util.Log
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data models for Structured Output demonstrations (e.g., Plant identification / categorization).
 */
@JsonClass(generateAdapter = true)
data class Plant(
    val commonName: String = "",
    val scientificName: String = "",
    val family: String = "",
    val wateringNeeds: String = "Moderate",
    val sunlightRequirement: String = "Full Sun",
    val toxicityToPets: Boolean = false,
    val careNotes: List<String> = emptyList()
)

/**
 * Custom exceptions for GenAI / Structured Output operations.
 */
open class GenAiException(message: String, cause: Throwable? = null) : Exception(message, cause)
class UnsupportedFeatureException(message: String) : GenAiException(message)
class GenerationFailedException(message: String, cause: Throwable? = null) : GenAiException(message, cause)

/**
 * Model candidate representation.
 */
data class Candidate(
    val text: String?
)

/**
 * Thought candidate representation for Gemini Nano / Thinking engines.
 */
data class ThoughtCandidate(
    val text: String
)

/**
 * Unified response model supporting separated thought process and primary text candidates.
 */
data class GenerateContentResponse(
    val candidates: List<Candidate>,
    val thoughtProcess: List<ThoughtCandidate> = emptyList()
)

/**
 * Request Builder supporting prompt text, thinking flags, and structured schema options.
 */
class GenerateContentRequestBuilder {
    private var promptText: String = ""
    var enableThinking: Boolean = false
    var structuredOutputSchema: Class<*>? = null

    fun text(value: String) {
        promptText = value
    }

    fun getPromptText(): String = promptText

    fun setSchema(clazz: Class<*>) {
        structuredOutputSchema = clazz
    }
}

fun generateContentRequest(builderAction: GenerateContentRequestBuilder.() -> Unit): GenerateContentRequestBuilder {
    val builder = GenerateContentRequestBuilder()
    builder.builderAction()
    return builder
}

/**
 * Generative Model wrapper supporting isStructuredOutputFeatureAvailable and enableThinking request handling.
 */
class GenerativeModel(
    private val context: Context? = null,
    val modelName: String = "gemini-nano-v4"
) {

    /**
     * Checks if the Structured Output Feature API is available on this device environment.
     * Requirements: Android API Level 26 or higher (minSdk 26+).
     */
    fun isStructuredOutputFeatureAvailable(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * Generates content with support for enableThinking and separated thought process extraction.
     */
    suspend fun generateContent(request: GenerateContentRequestBuilder): GenerateContentResponse = withContext(Dispatchers.Default) {
        val prompt = request.getPromptText()
        if (prompt.isBlank()) {
            throw GenerationFailedException("Prompt cannot be blank.")
        }

        try {
            if (request.enableThinking) {
                // Execute on-device multi-branch reasoning chain
                val result = OfflineDeepThinkingEngine.processPromptInstant(
                    prompt = prompt,
                    thinkingLevel = ThinkingLevel.EXTENDED,
                    searchGrounded = false
                )

                val thoughts = result.steps.map { step ->
                    ThoughtCandidate(text = "[${step.phase.badge}] ${step.headline}\n${step.details}\n")
                }

                val finalAnswer = result.finalAnswerText
                GenerateContentResponse(
                    candidates = listOf(Candidate(text = finalAnswer)),
                    thoughtProcess = thoughts
                )
            } else {
                // Direct response without extended thinking mode
                val directAnswer = "Structured reasoning output processed for: '$prompt'."
                GenerateContentResponse(
                    candidates = listOf(Candidate(text = directAnswer)),
                    thoughtProcess = emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("GenerativeModel", "Generation failed: ${e.message}", e)
            throw GenerationFailedException("Failed to generate content: ${e.localizedMessage}", e)
        }
    }
}
