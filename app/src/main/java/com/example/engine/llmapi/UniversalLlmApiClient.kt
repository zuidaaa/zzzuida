package com.example.engine.llmapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LlmApiProvider(
    val id: String,
    val name: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val description: String,
    val isReasoningModel: Boolean
)

data class LlmApiConfig(
    val providerId: String = "deepseek",
    val baseUrl: String = "https://api.deepseek.com",
    val apiKey: String = "",
    val model: String = "deepseek-reasoner",
    val temperature: Float = 0.6f,
    val maxTokens: Int = 4096,
    val systemPrompt: String = "You are an expert deep thinking AI assistant. Reason rigorously step-by-step."
)

data class LlmApiResponse(
    val content: String,
    val thoughtProcess: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val latencyMs: Long,
    val modelUsed: String,
    val error: String? = null
)

class UniversalLlmApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        val PRESET_PROVIDERS = listOf(
            LlmApiProvider(
                id = "deepseek",
                name = "DeepSeek AI",
                defaultBaseUrl = "https://api.deepseek.com",
                defaultModel = "deepseek-reasoner",
                description = "Native DeepSeek-R1 deep reasoning engine and DeepSeek-V3 chat.",
                isReasoningModel = true
            ),
            LlmApiProvider(
                id = "openai",
                name = "OpenAI",
                defaultBaseUrl = "https://api.openai.com/v1",
                defaultModel = "gpt-4o-mini",
                description = "Official OpenAI API (GPT-4o, o3-mini, o1-preview).",
                isReasoningModel = true
            ),
            LlmApiProvider(
                id = "groq",
                name = "Groq LPU",
                defaultBaseUrl = "https://api.groq.com/openai/v1",
                defaultModel = "deepseek-r1-distill-llama-70b",
                description = "Ultra-low latency inference engine (500+ tok/s).",
                isReasoningModel = true
            ),
            LlmApiProvider(
                id = "openrouter",
                name = "OpenRouter",
                defaultBaseUrl = "https://openrouter.ai/api/v1",
                defaultModel = "deepseek/deepseek-r1",
                description = "Universal aggregator for 200+ frontier & open-source models.",
                isReasoningModel = true
            ),
            LlmApiProvider(
                id = "lmstudio",
                name = "LM Studio / vLLM (Local)",
                defaultBaseUrl = "http://10.0.2.2:1234/v1",
                defaultModel = "local-model",
                description = "Connects to your local workstation running LM Studio or vLLM.",
                isReasoningModel = false
            )
        )
    }

    suspend fun testEndpoint(config: LlmApiConfig): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val cleanUrl = config.baseUrl.trim().removeSuffix("/")
            val testUrl = if (cleanUrl.endsWith("/v1")) "$cleanUrl/models" else "$cleanUrl/v1/models"

            val reqBuilder = Request.Builder().url(testUrl).get()
            if (config.apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            client.newCall(reqBuilder.build()).execute().use { resp ->
                val duration = System.currentTimeMillis() - startTime
                if (resp.isSuccessful) {
                    Pair(true, "Endpoint operational! Latency: ${duration}ms")
                } else {
                    Pair(false, "HTTP ${resp.code}: ${resp.message}")
                }
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Connection error")
        }
    }

    suspend fun executeChat(
        config: LlmApiConfig,
        messages: List<Pair<String, String>> // role to content
    ): LlmApiResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cleanUrl = config.baseUrl.trim().removeSuffix("/")
        val endpointUrl = if (cleanUrl.endsWith("/v1")) "$cleanUrl/chat/completions" else "$cleanUrl/v1/chat/completions"

        try {
            val messagesArray = JSONArray()
            if (config.systemPrompt.isNotBlank()) {
                messagesArray.put(JSONObject().apply {
                    put("role", "system")
                    put("content", config.systemPrompt)
                })
            }

            for ((role, content) in messages) {
                messagesArray.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }

            val requestJson = JSONObject().apply {
                put("model", config.model)
                put("messages", messagesArray)
                put("temperature", config.temperature)
                put("max_tokens", config.maxTokens)
                put("stream", false)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val reqBuilder = Request.Builder()
                .url(endpointUrl)
                .post(requestJson.toString().toRequestBody(mediaType))

            if (config.apiKey.isNotBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
            }

            client.newCall(reqBuilder.build()).execute().use { resp ->
                val latency = System.currentTimeMillis() - startTime
                val body = resp.body?.string() ?: ""

                if (!resp.isSuccessful) {
                    return@withContext LlmApiResponse(
                        content = "",
                        thoughtProcess = "",
                        promptTokens = 0,
                        completionTokens = 0,
                        totalTokens = 0,
                        latencyMs = latency,
                        modelUsed = config.model,
                        error = "HTTP ${resp.code}: $body"
                    )
                }

                val json = JSONObject(body)
                val choices = json.optJSONArray("choices")
                val firstChoice = choices?.optJSONObject(0)
                val messageObj = firstChoice?.optJSONObject("message")
                val content = messageObj?.optString("content", "") ?: ""

                // Extract reasoning_content if returned by DeepSeek API or o1/o3
                var thought = messageObj?.optString("reasoning_content", "") ?: ""

                var finalContent = content
                if (thought.isBlank() && content.contains("<think>")) {
                    thought = content.substringAfter("<think>").substringBefore("</think>").trim()
                    finalContent = content.substringAfter("</think>").trim()
                }

                val usage = json.optJSONObject("usage")
                val promptTokens = usage?.optInt("prompt_tokens", 0) ?: 0
                val completionTokens = usage?.optInt("completion_tokens", 0) ?: 0
                val totalTokens = usage?.optInt("total_tokens", promptTokens + completionTokens) ?: 0

                LlmApiResponse(
                    content = finalContent,
                    thoughtProcess = thought,
                    promptTokens = promptTokens,
                    completionTokens = completionTokens,
                    totalTokens = totalTokens,
                    latencyMs = latency,
                    modelUsed = config.model
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            LlmApiResponse(
                content = "",
                thoughtProcess = "",
                promptTokens = 0,
                completionTokens = 0,
                totalTokens = 0,
                latencyMs = latency,
                modelUsed = config.model,
                error = e.localizedMessage ?: "Execution failed"
            )
        }
    }
}
