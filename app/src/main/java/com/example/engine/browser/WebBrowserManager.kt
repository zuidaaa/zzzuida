package com.example.engine.browser

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import com.example.engine.GeminiApiClient
import com.example.engine.ThinkingLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class WebBrowserManager(
    private val application: Application,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "WebBrowserManager"

    private val _currentUrl = MutableStateFlow("https://arxiv.org/list/cs.AI/recent")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _currentTitle = MutableStateFlow("arXiv: Computer Science - Artificial Intelligence")
    val currentTitle: StateFlow<String> = _currentTitle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _pageProgress = MutableStateFlow(0)
    val pageProgress: StateFlow<Int> = _pageProgress.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _canGoForward = MutableStateFlow(false)
    val canGoForward: StateFlow<Boolean> = _canGoForward.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    private val _extractedContent = MutableStateFlow<PageExtractionResult?>(null)
    val extractedContent: StateFlow<PageExtractionResult?> = _extractedContent.asStateFlow()

    private val _bookmarks = MutableStateFlow(BrowserPresets.DEFAULT_BOOKMARKS)
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _downloads = MutableStateFlow<List<BrowserDownloadItem>>(emptyList())
    val downloads: StateFlow<List<BrowserDownloadItem>> = _downloads.asStateFlow()

    private val _activeResearchAnalysis = MutableStateFlow<AiResearchAnalysis?>(null)
    val activeResearchAnalysis: StateFlow<AiResearchAnalysis?> = _activeResearchAnalysis.asStateFlow()

    private val _isAnalyzingWithLlm = MutableStateFlow(false)
    val isAnalyzingWithLlm: StateFlow<Boolean> = _isAnalyzingWithLlm.asStateFlow()

    private val _llmAnalysisError = MutableStateFlow<String?>(null)
    val llmAnalysisError: StateFlow<String?> = _llmAnalysisError.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(listOf("https://arxiv.org/list/cs.AI/recent"))
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun navigateTo(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) return

        val url = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://duckduckgo.com/?q=" + Uri.encode(trimmed)
        }

        _currentUrl.value = url
        _history.update { (listOf(url) + it).distinct().take(100) }
    }

    fun onPageStarted(url: String) {
        _currentUrl.value = url
        _isLoading.value = true
        _pageProgress.value = 10
    }

    fun onPageFinished(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        _currentUrl.value = url
        _currentTitle.value = if (!title.isNullOrBlank()) title else url
        _isLoading.value = false
        _pageProgress.value = 100
        _canGoBack.value = canBack
        _canGoForward.value = canForward
    }

    fun onProgressChanged(newProgress: Int) {
        _pageProgress.value = newProgress
        _isLoading.value = newProgress in 1..99
    }

    fun toggleDesktopMode() {
        _isDesktopMode.update { !it }
    }

    fun toggleBookmark(title: String, url: String) {
        _bookmarks.update { current ->
            val exists = current.any { it.url == url }
            if (exists) {
                current.filter { it.url != url }
            } else {
                current + BookmarkItem(title = title.ifBlank { url }, url = url)
            }
        }
    }

    fun isBookmarked(url: String): Boolean {
        return _bookmarks.value.any { it.url == url }
    }

    fun setExtractedContent(title: String, url: String, text: String) {
        val cleanText = text.trim()
        val wordCount = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val hasCode = cleanText.contains("```") || cleanText.contains("function") || 
                      cleanText.contains("class ") || cleanText.contains("import ") ||
                      cleanText.contains("def ") || cleanText.contains("return ")
        _extractedContent.value = PageExtractionResult(
            url = url,
            title = title,
            extractedText = cleanText,
            wordCount = wordCount,
            hasCode = hasCode
        )
    }

    /**
     * Start a download for a URL with Android DownloadManager, with OkHttp fallback
     */
    fun startDownload(
        url: String,
        userAgent: String? = null,
        contentDisposition: String? = null,
        mimeType: String? = null,
        contentLength: Long = 0L
    ) {
        val guessedFileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val downloadItem = BrowserDownloadItem(
            url = url,
            fileName = guessedFileName,
            mimeType = mimeType ?: "application/octet-stream",
            fileSizeBytes = contentLength,
            status = DownloadStatus.DOWNLOADING
        )

        _downloads.update { listOf(downloadItem) + it }

        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Try Android DownloadManager first
                val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                if (downloadManager != null && url.startsWith("http")) {
                    try {
                        val request = DownloadManager.Request(Uri.parse(url))
                            .setTitle(guessedFileName)
                            .setDescription("AI Browser Research Download")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true)

                        if (!userAgent.isNullOrBlank()) {
                            request.addRequestHeader("User-Agent", userAgent)
                        }

                        // Save to external downloads directory
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, guessedFileName)
                        val dmId = downloadManager.enqueue(request)

                        _downloads.update { list ->
                            list.map {
                                if (it.id == downloadItem.id) {
                                    it.copy(downloadId = dmId, status = DownloadStatus.COMPLETED, progress = 100)
                                } else it
                            }
                        }
                        Log.i(tag, "Enqueued download with DownloadManager ID $dmId for $guessedFileName")
                        return@launch
                    } catch (e: Exception) {
                        Log.w(tag, "DownloadManager failed, falling back to direct HTTP stream: ${e.message}")
                    }
                }

                // Fallback: Direct OkHttp download to app external storage
                val targetDir = application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: application.filesDir
                val targetFile = File(targetDir, guessedFileName)

                val req = Request.Builder().url(url).apply {
                    if (!userAgent.isNullOrBlank()) header("User-Agent", userAgent)
                }.build()

                val response = okHttpClient.newCall(req).execute()
                if (!response.isSuccessful) {
                    throw RuntimeException("HTTP ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw RuntimeException("Empty response body")
                val totalLength = body.contentLength().takeIf { it > 0 } ?: contentLength
                var bytesCopied = 0L

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            if (totalLength > 0) {
                                val currentProg = ((bytesCopied * 100) / totalLength).toInt().coerceIn(0, 99)
                                _downloads.update { list ->
                                    list.map {
                                        if (it.id == downloadItem.id) it.copy(progress = currentProg) else it
                                    }
                                }
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }

                _downloads.update { list ->
                    list.map {
                        if (it.id == downloadItem.id) {
                            it.copy(
                                status = DownloadStatus.COMPLETED,
                                progress = 100,
                                localPath = targetFile.absolutePath,
                                fileSizeBytes = bytesCopied
                            )
                        } else it
                    }
                }
                Log.i(tag, "Direct download completed: ${targetFile.absolutePath} ($bytesCopied bytes)")
            } catch (e: Exception) {
                Log.e(tag, "Download failed for $url: ${e.message}", e)
                _downloads.update { list ->
                    list.map {
                        if (it.id == downloadItem.id) {
                            it.copy(
                                status = DownloadStatus.FAILED,
                                errorMessage = e.message ?: "Download failed"
                            )
                        } else it
                    }
                }
            }
        }
    }

    /**
     * Trigger LLM online research analysis on current extracted content
     */
    fun performAiResearch(
        analysisType: String,
        customQuery: String = ""
    ) {
        val content = _extractedContent.value
        val url = _currentUrl.value
        val title = _currentTitle.value

        val textToAnalyze = if (content != null && content.extractedText.isNotBlank()) {
            content.extractedText.take(12000)
        } else {
            "URL: $url\nTitle: $title\n[No page body extracted; perform search-grounded research on this topic]"
        }

        _isAnalyzingWithLlm.value = true
        _llmAnalysisError.value = null

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val prompt = when (analysisType) {
                    "SUMMARY" -> """
                        You are an expert AI Research Assistant. Analyze the content from the webpage:
                        Page Title: $title
                        Source URL: $url

                        Webpage Content:
                        $textToAnalyze

                        Please provide a structured research brief in Markdown format:
                        ### 📌 Executive Summary
                        A crisp 2-3 paragraph overview of the core topic, thesis, or problem addressed.

                        ### 🔬 Key Findings & Highlights
                        - Bullet points of primary contributions, metrics, or factual arguments.

                        ### 💡 Practical Takeaways & Applications
                        - What a researcher, engineer, or student should immediately remember or apply.

                        ### 🧭 Suggested Follow-up Questions
                        - 3 exploratory research queries for further study.
                    """.trimIndent()

                    "DEEP_RESEARCH" -> """
                        You are a senior principal scientist conducting deep research synthesis.
                        Page Title: $title
                        Source URL: $url

                        Webpage Content:
                        $textToAnalyze

                        Please formulate an in-depth Academic Dossier:
                        1. Theoretical Framework & Architecture
                        2. Rigorous Critique, Assumptions & Potential Caveats
                        3. Comparison with State-of-the-Art (SOTA) or Industry Standards
                        4. Step-by-Step Implementation Outline
                    """.trimIndent()

                    "CODE_EXTRACTION" -> """
                        You are a software engineer analyzing this technical webpage:
                        Page Title: $title
                        Source URL: $url

                        Webpage Content:
                        $textToAnalyze

                        Task:
                        1. Identify and extract all code blocks, command-line snippets, API endpoints, or mathematical algorithms.
                        2. Annotate each snippet with prerequisite libraries and instructions for running it.
                        3. Provide refactored or improved code if the snippet on the page is incomplete.
                    """.trimIndent()

                    "CRITIQUE" -> """
                        You are an analytical peer reviewer. Evaluate this article or research paper:
                        Page Title: $title
                        Source URL: $url

                        Webpage Content:
                        $textToAnalyze

                        Evaluate:
                        1. Logical Consistency & Fallacies
                        2. Robustness of Evidence & Empirical Backing
                        3. Biases or Unsubstantiated Claims
                        4. Final Verdict & Credibility Score (1-10)
                    """.trimIndent()

                    "ASK_QUESTION" -> """
                        You are an intelligent research assistant answering a user query about this webpage:
                        Page Title: $title
                        Source URL: $url
                        User Question: $customQuery

                        Webpage Content:
                        $textToAnalyze

                        Please answer the user's question directly, citing evidence and specific details from the text.
                    """.trimIndent()

                    else -> "Analyze and summarize this webpage: $title ($url)\n\n$textToAnalyze"
                }

                val result = GeminiApiClient.generateWithThinking(
                    prompt = prompt,
                    thinkingLevel = ThinkingLevel.MEDIUM,
                    systemPrompt = "You are an elite autonomous research engine integrated into an AI Web Browser. Deliver high-density, precise, beautifully structured Markdown analysis.",
                    model = GeminiApiClient.MODEL_FLASH
                )

                result.onSuccess { responseText ->
                    val analysis = AiResearchAnalysis(
                        url = url,
                        pageTitle = title,
                        summary = responseText,
                        analysisType = analysisType,
                        timestamp = System.currentTimeMillis()
                    )
                    _activeResearchAnalysis.value = analysis
                    _isAnalyzingWithLlm.value = false
                }.onFailure { err ->
                    _llmAnalysisError.value = err.message ?: "Failed to generate AI research analysis"
                    _isAnalyzingWithLlm.value = false
                }
            } catch (e: Exception) {
                Log.e(tag, "AI Research failed: ${e.message}", e)
                _llmAnalysisError.value = e.message ?: "Error during research synthesis"
                _isAnalyzingWithLlm.value = false
            }
        }
    }

    fun clearAnalysis() {
        _activeResearchAnalysis.value = null
        _llmAnalysisError.value = null
    }

    fun clearDownloads() {
        _downloads.value = emptyList()
    }
}
