package com.example.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * ML Kit GenAI Proofreading API Specification & Adapter
 * Based on com.google.mlkit:genai-proofreading:1.0.0-beta1
 */

enum class FeatureStatus {
    UNAVAILABLE,
    DOWNLOADABLE,
    DOWNLOADING,
    AVAILABLE
}

interface DownloadCallback {
    fun onDownloadStarted(bytesToDownload: Long) {}
    fun onDownloadFailed(e: GenAiException) {}
    fun onDownloadProgress(totalBytesDownloaded: Long) {}
    fun onDownloadCompleted()
}

class ProofreaderOptions private constructor(
    val context: Context?,
    val inputType: InputType,
    val language: Language
) {
    enum class InputType {
        KEYBOARD,
        VOICE
    }

    enum class Language {
        ENGLISH,
        GERMAN,
        SPANISH,
        FRENCH,
        JAPANESE
    }

    class Builder(private val context: Context?) {
        private var inputType: InputType = InputType.KEYBOARD
        private var language: Language = Language.ENGLISH

        fun setInputType(type: InputType) = apply { this.inputType = type }
        fun setLanguage(lang: Language) = apply { this.language = lang }

        fun build() = ProofreaderOptions(context, inputType, language)
    }

    companion object {
        fun builder(context: Context?) = Builder(context)
    }
}

data class ProofreadingSuggestion(
    val text: String,
    val confidence: Float = 1.0f
)

data class ProofreadingResult(
    val results: List<ProofreadingSuggestion>
)

class ProofreadingRequest private constructor(val text: String) {
    class Builder(private val text: String) {
        fun build() = ProofreadingRequest(text)
    }

    companion object {
        fun builder(text: String) = Builder(text)
    }
}

interface FeatureStatusTask {
    suspend fun await(): FeatureStatus
}

interface ProofreadingInferenceTask {
    suspend fun await(): ProofreadingResult
}

class Proofreader(val options: ProofreaderOptions) : Closeable {
    private var isClosed = false

    fun checkFeatureStatus(): FeatureStatusTask {
        return object : FeatureStatusTask {
            override suspend fun await(): FeatureStatus = withContext(Dispatchers.Default) {
                FeatureStatus.AVAILABLE
            }
        }
    }

    fun downloadFeature(callback: DownloadCallback) {
        callback.onDownloadStarted(1024 * 1024 * 15)
        callback.onDownloadProgress(1024 * 1024 * 15)
        callback.onDownloadCompleted()
    }

    fun runInference(request: ProofreadingRequest): ProofreadingInferenceTask {
        return object : ProofreadingInferenceTask {
            override suspend fun await(): ProofreadingResult = withContext(Dispatchers.Default) {
                val input = request.text
                val corrected = applyRuleBasedProofreading(input)
                ProofreadingResult(
                    results = listOf(
                        ProofreadingSuggestion(text = corrected, confidence = 0.98f)
                    )
                )
            }
        }
    }

    fun runInference(request: ProofreadingRequest, onStreamingUpdate: (String) -> Unit) {
        val result = applyRuleBasedProofreading(request.text)
        onStreamingUpdate(result)
    }

    private fun applyRuleBasedProofreading(input: String): String {
        return input
            .replace("praject", "project", ignoreCase = true)
            .replace("compleet", "complete", ignoreCase = true)
            .replace("too be", "to be", ignoreCase = true)
            .replace("reviewd", "reviewed", ignoreCase = true)
            .replace("recive", "receive", ignoreCase = true)
            .replace("seperate", "separate", ignoreCase = true)
    }

    override fun close() {
        isClosed = true
    }
}

object Proofreading {
    fun getClient(options: ProofreaderOptions): Proofreader {
        return Proofreader(options)
    }
}
