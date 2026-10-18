package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for local offline caching of LLM responses.
 * Supports multi-turn conversations, multimodal inputs, LRU tracking,
 * configurable TTL expiration, and immunity for pinned/training items.
 */
@Entity(
    tableName = "offline_llm_response_cache",
    indices = [
        Index(value = ["normalizedQuery", "modelId"]),
        Index(value = ["conversationId"]),
        Index(value = ["lastAccessedAt"]),
        Index(value = ["createdAt"]),
        Index(value = ["isPinned"]),
        Index(value = ["isTrainingCandidate"])
    ]
)
data class OfflineLlmResponseCacheEntity(
    @PrimaryKey
    val cacheKey: String, // SHA-256 hash of normalized prompt + model ID + multimodal hash
    val promptQuery: String,
    val normalizedQuery: String,
    val modelId: String,
    val responseContent: String,
    val conversationId: String? = null,
    val multimodalType: String = "TEXT", // TEXT, IMAGE, AUDIO, VIDEO, HYBRID
    val multimodalAttachmentsJson: String = "[]",
    val thoughtTrace: String? = null,
    val tokensUsed: Int = 0,
    val latencyMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 1,
    val ttlMs: Long = 604800000L, // Default 7 days (7 * 24 * 3600 * 1000). 0L = permanent
    val isPinned: Boolean = false, // Pinned/favorited items never expire
    val isTrainingCandidate: Boolean = false, // Protected if candidate for fine-tuning/dataset
    val metadataJson: String = "{}"
)
