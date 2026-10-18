package com.example.engine.browser

import java.util.UUID

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class BrowserDownloadItem(
    val id: String = UUID.randomUUID().toString(),
    val downloadId: Long = -1L,
    val url: String,
    val fileName: String,
    val mimeType: String = "application/octet-stream",
    val fileSizeBytes: Long = 0L,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val localPath: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

data class BookmarkItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val category: String = "Research",
    val timestamp: Long = System.currentTimeMillis()
)

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://duckduckgo.com",
    val title: String = "New Tab",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false
)

data class PageExtractionResult(
    val url: String,
    val title: String,
    val extractedText: String,
    val wordCount: Int,
    val hasCode: Boolean
)

data class AiResearchAnalysis(
    val url: String,
    val pageTitle: String,
    val summary: String,
    val keyFindings: List<String> = emptyList(),
    val critiqueAndLimitations: String = "",
    val codeSnippets: List<String> = emptyList(),
    val suggestedQueries: List<String> = emptyList(),
    val analysisType: String = "SUMMARY",
    val timestamp: Long = System.currentTimeMillis()
)

data class PresetQuickLink(
    val title: String,
    val url: String,
    val badge: String,
    val description: String,
    val category: String
)

object BrowserPresets {
    val QUICK_LINKS = listOf(
        PresetQuickLink(
            title = "arXiv AI & CS",
            url = "https://arxiv.org/list/cs.AI/recent",
            badge = "Papers",
            description = "Latest peer-reviewed machine learning & cognitive research",
            category = "Research"
        ),
        PresetQuickLink(
            title = "Hugging Face Hub",
            url = "https://huggingface.co/models",
            badge = "Models",
            description = "Explore open weights, GGUF quants, and datasets",
            category = "AI & ML"
        ),
        PresetQuickLink(
            title = "GitHub Trending",
            url = "https://github.com/trending?since=daily",
            badge = "Code",
            description = "Trending open-source AI repos and frameworks",
            category = "Code"
        ),
        PresetQuickLink(
            title = "Papers with Code",
            url = "https://paperswithcode.com",
            badge = "Benchmarks",
            description = "State-of-the-art ML leaderboards & code repos",
            category = "Research"
        ),
        PresetQuickLink(
            title = "DuckDuckGo",
            url = "https://duckduckgo.com",
            badge = "Search",
            description = "Privacy-focused web search engine",
            category = "Search"
        ),
        PresetQuickLink(
            title = "Google Scholar",
            url = "https://scholar.google.com",
            badge = "Scholar",
            description = "Academic articles, theses, books and court opinions",
            category = "Research"
        ),
        PresetQuickLink(
            title = "Ollama Library",
            url = "https://ollama.com/library",
            badge = "Local LLMs",
            description = "Browse on-device models: Llama 3, Qwen 2.5, DeepSeek",
            category = "AI & ML"
        ),
        PresetQuickLink(
            title = "Wikipedia",
            url = "https://en.m.wikipedia.org",
            badge = "Knowledge",
            description = "Free online encyclopedia for exploratory grounding",
            category = "Knowledge"
        )
    )

    val DEFAULT_BOOKMARKS = listOf(
        BookmarkItem(
            title = "arXiv cs.AI (Recent)",
            url = "https://arxiv.org/list/cs.AI/recent",
            category = "Research"
        ),
        BookmarkItem(
            title = "Hugging Face Models",
            url = "https://huggingface.co/models",
            category = "AI & ML"
        ),
        BookmarkItem(
            title = "Papers With Code SOTA",
            url = "https://paperswithcode.com/sota",
            category = "Research"
        ),
        BookmarkItem(
            title = "DuckDuckGo Search",
            url = "https://duckduckgo.com",
            category = "Search"
        )
    )
}
