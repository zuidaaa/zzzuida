package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessagePreview: String = "",
    val domainTag: String = "General"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user" or "assistant"
    val content: String,
    val thoughtProcess: String = "",
    val thinkingDurationMs: Long = 0L,
    val thinkingTokens: Int = 0,
    val reasoningStepsJson: String = "[]",
    val modelMode: String = "Offline Gemini 3.7 Deep Thinking",
    val timestamp: Long = System.currentTimeMillis(),
    val userFeedbackRating: Int = 0, // 0: None, 1: Thumbs Up, -1: Thumbs Down, 5: 5 Stars
    val userFeedbackText: String = "",
    val searchGrounded: Boolean = false,
    val searchCitationsJson: String = "[]",
    val previousInteractionId: String? = null,
    val agentType: String = "DEFAULT", // "DEEP_RESEARCH", "INFINITE_DATASET_HUB", "GEMINI_STANDARD", "GEMINI_DEEP_THINK", "CODE_ARCHITECT", "DATA_COLLECTOR"
    val interactionStepType: String = "CONVERSATION", // "DATA_COLLECTION", "SUMMARIZATION", "REFORMATTING", "DATASET_GENERATION", "CODE_SYNTHESIS"
    val interactionMetadataJson: String = "{}"
)

@Entity(tableName = "local_models")
data class LocalModelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val family: String,
    val parameterSize: String,
    val quantization: String,
    val downloadSizeMb: Int,
    val isDownloaded: Boolean = false,
    val isActive: Boolean = false,
    val computeBackend: String = "S26 Ultra NPU", // NPU, GPU, CPU
    val contextWindow: String = "128k",
    val description: String = "",
    val version: String = "v1.0.0",
    val trainingData: String = "Trained on standard multi-task corpus (math/logic/code)",
    val releaseDate: String = "2026-01-01"
)

@Entity(tableName = "generated_media")
data class GeneratedMediaEntity(
    @PrimaryKey val id: String,
    val mediaType: String, // "IMAGE" or "VIDEO"
    val prompt: String,
    val style: String,
    val aspectRatio: String,
    val motionPreset: String = "",
    val mediaSeed: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reasoning_cache")
data class ReasoningCacheEntity(
    @PrimaryKey val cacheKey: String, // Hash of query + model + budget
    val promptQuery: String,
    val normalizedQuery: String,
    val modelId: String,
    val modelName: String,
    val thinkingLevel: String = "HIGH",
    val thinkingBudgetTokens: Int = 8192,
    val answerContent: String,
    val thoughtProcess: String,
    val reasoningStepsJson: String = "[]",
    val searchCitationsJson: String = "[]",
    val thinkingDurationMs: Long = 0L,
    val thinkingTokens: Int = 0,
    val answerTokens: Int = 0,
    val tokensPerSecond: Double = 0.0,
    val domainCategory: String = "General", // Mathematics, Logic, Coding, Biomedical, Physics, Systems
    val cachedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val hitCount: Int = 1,
    val isFavorite: Boolean = false,
    val userNotes: String = "",
    val offlineAvailable: Boolean = true,
    val isVerified: Boolean = true,
    val searchQueryExecuted: String? = null,
    val confidenceScore: Double = 1.0,
    val customMetadataJson: String = "{}"
)

@Entity(tableName = "deep_thinking_sessions")
data class DeepThinkingSessionEntity(
    @PrimaryKey val sessionId: String,
    val title: String,
    val initialQuery: String,
    val modelId: String,
    val modelName: String,
    val domainCategory: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val totalThinkingTimeMs: Long = 0L,
    val totalThinkingTokens: Int = 0,
    val stepsCount: Int = 0,
    val finalSynthesisPreview: String = "",
    val isOfflineCached: Boolean = true,
    val complexityRating: Int = 3, // 1 to 5
    val verificationStatus: String = "VERIFIED", // VERIFIED, REVISED, HYPOTHESIS
    val userNotes: String = "",
    val fullThoughtTraceMarkdown: String = "",
    val synthesisResult: String = "",
    val datasetAssocId: String? = null
)

@Entity(tableName = "training_datasets")
data class LlmDatasetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val fileFormat: String = "JSON", // JSON, JSONL, CSV, TXT
    val entryCount: Int = 0,
    val fileSize: Long = 0L,
    val rawContent: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val purpose: String = "Supervised Fine-Tuning (SFT)", // SFT, DPO, Pre-training
    val modelTarget: String = "deepseek-r1-7b"
)

@Entity(tableName = "cached_reasoning_steps")
data class CachedReasoningStepEntity(
    @PrimaryKey val stepId: String,
    val cacheKey: String,
    val stepIndex: Int,
    val phaseName: String,
    val phaseBadge: String,
    val headline: String,
    val details: String,
    val confidence: Float = 0.95f,
    val branchLabel: String = "Main Branch",
    val isVerified: Boolean = true
)

@Entity(tableName = "local_documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val fileSize: Long,
    val mimeType: String = "text/plain",
    val addedAt: Long = System.currentTimeMillis(),
    val isRagEnabled: Boolean = true
)

@Entity(tableName = "benchmark_results")
data class BenchmarkResultEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val benchmarkType: String = "FULL_SYSTEM", // "FULL_SYSTEM", "DATABASE_IO", "SOURCES_LATENCY", "NPU_INFERENCE"
    val overallScore: Int = 9850,
    val dbWriteOpsPerSec: Double = 3450.0,
    val dbReadOpsPerSec: Double = 12800.0,
    val dbLatencyMs: Long = 2L,
    val npuThroughputTops: Double = 84.8,
    val ragSearchLatencyMs: Long = 14L,
    val searchGroundingLatencyMs: Long = 185L,
    val mapsGroundingLatencyMs: Long = 160L,
    val hfMcpLatencyMs: Long = 145L,
    val activeSourcesCount: Int = 7,
    val hardwareDevice: String = "Samsung Galaxy S26 Ultra (Exynos 2600 + 85 TOPS NPU)",
    val detailedSummary: String = "All multi-source grounding channels, Room SQLite transactions, and vector search operations verified at peak performance.",
    val passedAllChecks: Boolean = true
)

@Entity(tableName = "knowledge_sources")
data class KnowledgeSourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sourceType: String, // "WEB_SEARCH", "MAPS_GEO", "MCP_HUB", "ACADEMIC_ARXIV", "LOCAL_RAG", "CULTURAL_WING", "FIREBASE_SYNC"
    val endpointOrPath: String,
    val isEnabled: Boolean = true,
    val latencyMs: Long = 0L,
    val lastCheckedTimestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE", // "ACTIVE", "WARNING", "OFFLINE"
    val totalQueriesServed: Int = 0,
    val description: String = "",
    val allowsOfflineCaching: Boolean = true
)

@Entity(tableName = "problem_progress")
data class ProblemProgressEntity(
    @PrimaryKey val problemId: String,
    val domain: String,
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, HINTED, VERIFIED_SOLVED
    val unlockedHintsCount: Int = 0,
    val userSolutionDraft: String = "",
    val userNotes: String = "",
    val lastAttemptTimestamp: Long = System.currentTimeMillis(),
    val score: Int = 0,
    val isBookmarked: Boolean = false,
    val offlineCacheKey: String = ""
)

@Entity(tableName = "web_watchers")
data class WebWatcherEntity(
    @PrimaryKey val id: String,
    val siteName: String,
    val url: String,
    val frequencyHours: Int = 24,
    val scheduledTime: String = "12:00",
    val active: Boolean = true,
    val lastCheckedValue: String = "No checks run yet",
    val lastCheckedTimestamp: Long = System.currentTimeMillis(),
    val watcherCode: String = "",
    val notificationSentCount: Int = 0
)

@Entity(tableName = "wiki_pages")
data class WikiPageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val tags: String = "",
    val connectionsJson: String = "[]",
    val lastUpdated: Long = System.currentTimeMillis(),
    val overnightMaintained: Boolean = false,
    val entityLinksMetadata: String = "{}",
    val reasoningAuditLog: String = ""
)

@Entity(tableName = "agent_skills")
data class AgentSkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val ownerRepo: String,
    val version: String = "1.0.0",
    val category: String = "CODING",
    val allowedToolsJson: String = "[]",
    val instructionsMarkdown: String = "",
    val isInstalled: Boolean = false,
    val isActive: Boolean = false,
    val downloadCount: Int = 1200,
    val stars: Int = 145,
    val author: String = "Community",
    val sourceUrl: String = "https://skills.sh",
    val license: String = "MIT",
    val compatibility: String = "gemini-3.7, claude-code, ollama, cursor",
    val installedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val securityStatus: String = "VERIFIED_SAFE",
    val securityChecksum: String = ""
)

@Entity(tableName = "skills_sync_logs")
data class SkillSyncLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS", // "SUCCESS", "WARNING", "FAILED"
    val triggerType: String = "PERIODIC_WORKER", // "PERIODIC_WORKER", "MANUAL_UI", "SYSTEM_STARTUP"
    val skillsCheckedCount: Int = 0,
    val skillsUpdatedCount: Int = 0,
    val newSkillsAddedCount: Int = 0,
    val securityAuditsPassed: Int = 0,
    val npuOptimizationsApplied: Int = 0,
    val summaryMessage: String = "",
    val rawLogDetails: String = ""
)

@Entity(tableName = "folder_foundation")
data class FolderFileEntity(
    @PrimaryKey val id: String,
    val folderName: String, // "00_Inbox", "01_Projects", "99_Archive"
    val title: String,
    val content: String,
    val dateString: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "proof_of_thought_cache")
data class ProofOfThoughtEntity(
    @PrimaryKey val id: String,
    val title: String,
    val premiseOrHypothesis: String,
    val normalizedQuery: String,
    val domain: String = "Mathematics", // "Mathematics", "Logic", "Computer Science", "Physics", "Distributed Systems"
    val proofTechnique: String = "Contradiction", // "Contradiction", "Induction", "Invariant Assertion", "Hoare Logic", "Constructive"
    val formalProofBody: String,
    val reasoningStepsJson: String = "[]",
    val qedConclusion: String = "",
    val verificationStatus: String = "VERIFIED_FORMAL", // "VERIFIED_FORMAL", "CHECKED_OFFLINE", "HEURISTIC"
    val confidenceScore: Double = 0.99,
    val thinkingTokens: Int = 4096,
    val thinkingDurationMs: Long = 1850L,
    val modelSource: String = "Gemini 3.7 Offline Deep Thinking",
    val isOfflineAvailable: Boolean = true,
    val localCachedTimestamp: Long = System.currentTimeMillis(),
    val exportFilePath: String? = null,
    val datasetLinkedId: String? = null
)



