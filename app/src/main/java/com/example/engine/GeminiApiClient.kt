package com.example.engine

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = "user",
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiThinkingConfig(
    val thinkingLevel: String = "high"
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    val maxOutputTokens: Int? = null,
    val thinkingConfig: GeminiThinkingConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    val googleSearch: Map<String, String>? = null,
    val googleMaps: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = null,
    val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    const val MODEL_PRO = "gemini-3.1-pro-preview"
    const val MODEL_FLASH = "gemini-3.5-flash"
    const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite"
    const val MODEL_FLASH_38 = "gemini-3.8-flash"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateMultiTurnChat(
        contents: List<GeminiContent>,
        model: String = MODEL_FLASH,
        thinkingLevel: ThinkingLevel = ThinkingLevel.MEDIUM,
        systemPrompt: String = "",
        temperature: Float? = null,
        maxOutputTokens: Int? = null,
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(IllegalStateException("API key not configured in Secrets panel."))
            }

            val systemContent = if (systemPrompt.isNotBlank()) {
                GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            } else null

            // Map thinking level if supported
            val thinkingConfig = when (thinkingLevel) {
                ThinkingLevel.LOW -> GeminiThinkingConfig("low")
                ThinkingLevel.MEDIUM -> GeminiThinkingConfig("medium")
                ThinkingLevel.HIGH, ThinkingLevel.EXTENDED -> GeminiThinkingConfig("high")
            }

            // Normalise model identifier
            val targetModel = when {
                model.contains("pro", ignoreCase = true) -> MODEL_PRO
                model.contains("3.8", ignoreCase = true) -> MODEL_FLASH_38
                model.contains("lite", ignoreCase = true) -> MODEL_FLASH_LITE
                model.startsWith("gemini-", ignoreCase = true) -> model
                else -> MODEL_FLASH
            }

            val toolsList = mutableListOf<GeminiTool>().apply {
                if (enableSearchGrounding) {
                    add(GeminiTool(googleSearch = emptyMap()))
                }
                if (enableMapsGrounding) {
                    add(GeminiTool(googleMaps = emptyMap()))
                }
            }.takeIf { it.isNotEmpty() }

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemContent,
                generationConfig = GeminiGenerationConfig(
                    temperature = temperature ?: 0.7f,
                    maxOutputTokens = maxOutputTokens,
                    thinkingConfig = if (targetModel == MODEL_PRO) thinkingConfig else null
                ),
                tools = toolsList
            )

            var currentDelay = 1000L
            val maxDelay = 10000L
            val backoffFactor = 2.0
            val maxRetries = 3
            var lastException: Exception? = null

            for (attempt in 0..maxRetries) {
                try {
                    val response = service.generateContent(targetModel, apiKey, request)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        return@withContext Result.success(text)
                    } else {
                        return@withContext Result.failure(RuntimeException("Empty response received from Gemini API"))
                    }
                } catch (e: Exception) {
                    lastException = e
                    val isTransient = when (e) {
                        is java.io.IOException -> true
                        is retrofit2.HttpException -> {
                            val code = e.code()
                            code == 429 || code >= 500
                        }
                        else -> false
                    }

                    if (isTransient && attempt < maxRetries) {
                        val jitter = ((-150)..150).random()
                        val delayTime = (currentDelay + jitter).coerceAtLeast(100L)
                        android.util.Log.w("GeminiApiClient", "Transient error on attempt ${attempt + 1}: ${e.message}. Retrying in ${delayTime}ms...")
                        kotlinx.coroutines.delay(delayTime)
                        currentDelay = (currentDelay * backoffFactor).toLong().coerceAtMost(maxDelay)
                    } else {
                        break
                    }
                }
            }
            Result.failure(lastException ?: RuntimeException("Network request failed after retries"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateWithThinking(
        prompt: String,
        thinkingLevel: ThinkingLevel,
        systemPrompt: String = "",
        model: String = MODEL_FLASH
    ): Result<String> {
        val contents = listOf(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )
        val defaultSystem = if (systemPrompt.isNotBlank()) {
            systemPrompt
        } else {
            "You are Gemini AI, a helpful, thoughtful and highly capable conversational assistant."
        }
        return generateMultiTurnChat(
            contents = contents,
            model = model,
            thinkingLevel = thinkingLevel,
            systemPrompt = defaultSystem
        )
    }
}
