package com.example.engine.ollama

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class OllamaModel(
    val name: String,
    val modifiedAt: String,
    val sizeBytes: Long,
    val sizeFormatted: String,
    val parameterSize: String,
    val quantizationLevel: String,
    val digest: String
)

data class OllamaServerStatus(
    val isConnected: Boolean,
    val version: String = "",
    val activeHost: String = "",
    val latencyMs: Long = 0L,
    val modelCount: Int = 0,
    val errorMessage: String? = null
)

data class OllamaChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
)

data class OllamaChatChunk(
    val token: String,
    val thoughtToken: String = "",
    val isDone: Boolean = false,
    val totalDurationMs: Long = 0L,
    val evalCount: Int = 0,
    val evalRateTokensPerSec: Double = 0.0
)

data class OllamaPullStatus(
    val status: String,
    val digest: String = "",
    val total: Long = 0L,
    val completed: Long = 0L,
    val progressPercent: Float = 0f,
    val isFinished: Boolean = false,
    val error: String? = null
)

class OllamaClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    suspend fun checkHealth(hostUrl: String): OllamaServerStatus = withContext(Dispatchers.IO) {
        val cleanHost = hostUrl.trim().removeSuffix("/")
        val startTime = System.currentTimeMillis()
        try {
            val req = Request.Builder()
                .url("$cleanHost/api/version")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                val latency = System.currentTimeMillis() - startTime
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: "{}"
                    val json = JSONObject(body)
                    val version = json.optString("version", "unknown")
                    val tags = listModels(cleanHost)
                    OllamaServerStatus(
                        isConnected = true,
                        version = version,
                        activeHost = cleanHost,
                        latencyMs = latency,
                        modelCount = tags.size
                    )
                } else {
                    OllamaServerStatus(
                        isConnected = false,
                        activeHost = cleanHost,
                        errorMessage = "HTTP ${resp.code}: ${resp.message}"
                    )
                }
            }
        } catch (e: Exception) {
            OllamaServerStatus(
                isConnected = false,
                activeHost = cleanHost,
                errorMessage = e.localizedMessage ?: "Failed to reach Ollama daemon"
            )
        }
    }

    suspend fun listModels(hostUrl: String): List<OllamaModel> = withContext(Dispatchers.IO) {
        val cleanHost = hostUrl.trim().removeSuffix("/")
        try {
            val req = Request.Builder()
                .url("$cleanHost/api/tags")
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val body = resp.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val modelsArray = json.optJSONArray("models") ?: JSONArray()
                val result = mutableListOf<OllamaModel>()

                for (i in 0 until modelsArray.length()) {
                    val m = modelsArray.getJSONObject(i)
                    val name = m.optString("name", "")
                    val size = m.optLong("size", 0L)
                    val details = m.optJSONObject("details") ?: JSONObject()
                    val paramSize = details.optString("parameter_size", "unknown")
                    val quantLevel = details.optString("quantization_level", "unknown")
                    val modified = m.optString("modified_at", "")
                    val digest = m.optString("digest", "").take(12)

                    result.add(
                        OllamaModel(
                            name = name,
                            modifiedAt = modified,
                            sizeBytes = size,
                            sizeFormatted = formatBytes(size),
                            parameterSize = paramSize,
                            quantizationLevel = quantLevel,
                            digest = digest
                        )
                    )
                }
                result
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun chatStream(
        hostUrl: String,
        model: String,
        messages: List<OllamaChatMessage>,
        temperature: Float = 0.7f,
        numCtx: Int = 4096
    ): Flow<OllamaChatChunk> = flow {
        val cleanHost = hostUrl.trim().removeSuffix("/")
        val messagesArray = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("role", msg.role)
            obj.put("content", msg.content)
            messagesArray.put(obj)
        }

        val requestJson = JSONObject().apply {
            put("model", model)
            put("messages", messagesArray)
            put("stream", true)
            put("options", JSONObject().apply {
                put("temperature", temperature)
                put("num_ctx", numCtx)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$cleanHost/api/chat")
            .post(requestBody)
            .build()

        var isInsideThinkingTag = false
        val call = client.newCall(request)
        val response = call.execute()

        if (!response.isSuccessful) {
            emit(OllamaChatChunk(token = "Error: HTTP ${response.code} ${response.message}", isDone = true))
            return@flow
        }

        val inputStream = response.body?.byteStream()
        if (inputStream == null) {
            emit(OllamaChatChunk(token = "Error: Response body empty", isDone = true))
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        try {
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue

                val obj = JSONObject(currentLine)
                val msg = obj.optJSONObject("message")
                val content = msg?.optString("content", "") ?: ""
                val isDone = obj.optBoolean("done", false)

                // Parsing deep thinking tags like <think> ... </think>
                var thoughtPiece = ""
                var answerPiece = ""

                var remaining = content
                while (remaining.isNotEmpty()) {
                    if (!isInsideThinkingTag) {
                        if (remaining.contains("<think>")) {
                            val before = remaining.substringBefore("<think>")
                            answerPiece += before
                            isInsideThinkingTag = true
                            remaining = remaining.substringAfter("<think>")
                        } else {
                            answerPiece += remaining
                            remaining = ""
                        }
                    } else {
                        if (remaining.contains("</think>")) {
                            val inside = remaining.substringBefore("</think>")
                            thoughtPiece += inside
                            isInsideThinkingTag = false
                            remaining = remaining.substringAfter("</think>")
                        } else {
                            thoughtPiece += remaining
                            remaining = ""
                        }
                    }
                }

                val totalDurationMs = obj.optLong("total_duration", 0L) / 1_000_000L
                val evalCount = obj.optInt("eval_count", 0)
                val evalDurationNs = obj.optLong("eval_duration", 0L)
                val evalRate = if (evalDurationNs > 0) (evalCount.toDouble() / (evalDurationNs / 1_000_000_000.0)) else 0.0

                emit(
                    OllamaChatChunk(
                        token = answerPiece,
                        thoughtToken = thoughtPiece,
                        isDone = isDone,
                        totalDurationMs = totalDurationMs,
                        evalCount = evalCount,
                        evalRateTokensPerSec = evalRate
                    )
                )
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    fun pullModelStream(
        hostUrl: String,
        modelName: String
    ): Flow<OllamaPullStatus> = flow {
        val cleanHost = hostUrl.trim().removeSuffix("/")
        val requestJson = JSONObject().apply {
            put("name", modelName)
            put("stream", true)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$cleanHost/api/pull")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            emit(OllamaPullStatus(status = "error", error = "HTTP ${response.code}: ${response.message}", isFinished = true))
            return@flow
        }

        val inputStream = response.body?.byteStream() ?: run {
            emit(OllamaPullStatus(status = "error", error = "Null body", isFinished = true))
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        try {
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue

                val obj = JSONObject(currentLine)
                val status = obj.optString("status", "")
                val digest = obj.optString("digest", "")
                val total = obj.optLong("total", 0L)
                val completed = obj.optLong("completed", 0L)
                val progress = if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                val isFinished = status.contains("success", ignoreCase = true)

                emit(
                    OllamaPullStatus(
                        status = status,
                        digest = digest.take(12),
                        total = total,
                        completed = completed,
                        progressPercent = progress,
                        isFinished = isFinished
                    )
                )
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteModel(hostUrl: String, modelName: String): Boolean = withContext(Dispatchers.IO) {
        val cleanHost = hostUrl.trim().removeSuffix("/")
        try {
            val requestJson = JSONObject().apply { put("name", modelName) }
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$cleanHost/api/delete")
                .delete(requestBody)
                .build()

            client.newCall(request).execute().use { resp -> resp.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.2f GB", gb)
    }
}
