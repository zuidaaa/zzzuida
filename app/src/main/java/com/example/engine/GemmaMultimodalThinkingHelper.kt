package com.example.engine

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gemma 4 Multimodal Reasoning & Thinking Models Configuration and Response Parser.
 * Supports:
 * - google/gemma-4-E2B-it
 * - google/gemma-4-E4B-it
 * - google/gemma-4-31B-it
 * - google/gemma-4-26B-A4B-it
 */
object Gemma4ModelRegistry {
    const val GEMMA_4_E2B_IT = "google/gemma-4-E2B-it"
    const val GEMMA_4_E4B_IT = "google/gemma-4-E4B-it"
    const val GEMMA_4_31B_IT = "google/gemma-4-31B-it"
    const val GEMMA_4_26B_A4B_IT = "google/gemma-4-26B-A4B-it"

    val AVAILABLE_MODELS = listOf(
        GEMMA_4_E2B_IT,
        GEMMA_4_E4B_IT,
        GEMMA_4_31B_IT,
        GEMMA_4_26B_A4B_IT
    )
}

data class GemmaParsedResponse(
    val thinking: String,
    val answer: String,
    val rawText: String,
    val hasThinkingTrace: Boolean
)

data class GemmaMultimodalMessage(
    val role: String = "user",
    val text: String,
    val image: Bitmap? = null,
    val enableThinking: Boolean = true
)

object GemmaResponseParser {
    private val THOUGHT_TAG_REGEX = Regex("<(thought|think|reasoning)>([\\s\\S]*?)</\\1>", RegexOption.IGNORE_CASE)
    private val OPEN_TAG_REGEX = Regex("<(thought|think|reasoning)>", RegexOption.IGNORE_CASE)

    /**
     * Splits combined response into separate thinking and answer blocks.
     * Handles <thought>...</thought>, <think>...</think>, <reasoning>...</reasoning> with full case-insensitivity.
     */
    fun parseResponse(rawResponse: String): GemmaParsedResponse {
        val trimmed = rawResponse.trim()

        val match = THOUGHT_TAG_REGEX.find(trimmed)
        if (match != null) {
            val thinking = match.groupValues[2].trim()
            val answer = (trimmed.substring(0, match.range.first) + trimmed.substring(match.range.last + 1)).trim()
            return GemmaParsedResponse(
                thinking = thinking,
                answer = answer,
                rawText = trimmed,
                hasThinkingTrace = thinking.isNotBlank()
            )
        }

        // Handle unclosed tag (e.g. while streaming generation is still ongoing)
        val openMatch = OPEN_TAG_REGEX.find(trimmed)
        if (openMatch != null && !trimmed.contains("</${openMatch.groupValues[1]}>", ignoreCase = true)) {
            val thinking = trimmed.substring(openMatch.range.last + 1).trim()
            return GemmaParsedResponse(
                thinking = thinking,
                answer = "",
                rawText = trimmed,
                hasThinkingTrace = thinking.isNotBlank()
            )
        }

        // Fallback: Standard plain response without explicit thinking tags
        return GemmaParsedResponse(
            thinking = "",
            answer = trimmed,
            rawText = trimmed,
            hasThinkingTrace = false
        )
    }

    /**
     * Simulates Python `apply_chat_template` formatting with `enable_thinking=True`.
     */
    fun applyChatTemplate(
        message: GemmaMultimodalMessage,
        modelId: String = Gemma4ModelRegistry.GEMMA_4_E2B_IT
    ): String {
        val builder = StringBuilder()
        builder.append("<start_of_turn>user\n")
        if (message.image != null) {
            builder.append("<image>\n")
        }
        builder.append(message.text.trim())
        builder.append("<end_of_turn>\n")
        builder.append("<start_of_turn>model\n")
        if (message.enableThinking) {
            builder.append("<thought>\n")
        }
        return builder.toString()
    }
}
