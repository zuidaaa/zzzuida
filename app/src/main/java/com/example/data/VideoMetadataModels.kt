package com.example.data

import android.net.Uri

data class VideoChapter(
    val timestamp: String,
    val seconds: Int,
    val title: String,
    val summary: String
)

data class AccountTag(
    val handle: String,
    val role: String,
    val reason: String
)

data class SuggestedLink(
    val label: String,
    val url: String,
    val category: String, // Resource, GitHub, Documentation, Affiliate, Social
    val timestampRef: String? = null
)

data class ThumbnailConcept(
    val title: String,
    val visualPrompt: String,
    val timestamp: String,
    val hookText: String,
    val aestheticScore: Float
)

enum class VideoMetadataCategory {
    ALL,
    DESCRIPTION,
    HASHTAGS,
    CHAPTERS,
    ACCOUNT_TAGS,
    LINKS,
    THUMBNAILS
}

data class SampleVideo(
    val title: String,
    val category: String,
    val durationFormatted: String,
    val resolution: String,
    val simulatedUriString: String,
    val descriptionContext: String
)
