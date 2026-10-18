package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class CacheTelemetryStats(
    val totalEntries: Int = 0,
    val expiredEntries: Int = 0,
    val pinnedEntries: Int = 0,
    val trainingCandidateEntries: Int = 0
)

class OfflineLlmResponseRepository(
    private val dao: OfflineLlmResponseCacheDao,
    val preferences: CachePolicyPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val allResponses: Flow<List<OfflineLlmResponseCacheEntity>> = dao.getAllCachedResponses()
    val pinnedResponses: Flow<List<OfflineLlmResponseCacheEntity>> = dao.getPinnedResponses()
    val totalCount: Flow<Int> = dao.countTotal()
    val pinnedCount: Flow<Int> = dao.countPinned()
    val trainingCount: Flow<Int> = dao.countTrainingCandidates()

    fun getExpiredCountFlow(): Flow<Int> {
        val protectTraining = if (preferences.state.value.protectTrainingDatasets) 1 else 0
        return dao.countExpired(System.currentTimeMillis(), protectTraining)
    }

    fun searchResponses(query: String): Flow<List<OfflineLlmResponseCacheEntity>> {
        return dao.searchResponses(query)
    }

    fun normalizePrompt(rawPrompt: String): String {
        return rawPrompt.trim().lowercase().replace("\\s+".toRegex(), " ")
    }

    fun generateCacheKey(normalizedPrompt: String, modelId: String, multimodalHash: String = ""): String {
        val input = "MODEL:$modelId|PROMPT:$normalizedPrompt|MM:$multimodalHash"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun getCachedResponse(
        prompt: String,
        modelId: String,
        multimodalHash: String = ""
    ): OfflineLlmResponseCacheEntity? {
        val normalized = normalizePrompt(prompt)
        val key = generateCacheKey(normalized, modelId, multimodalHash)
        val now = System.currentTimeMillis()

        val cached = dao.getValidCachedResponse(key, now) ?: dao.findMatchingResponse(normalized, modelId, now)
        if (cached != null) {
            scope.launch {
                dao.updateAccess(cached.cacheKey, System.currentTimeMillis())
            }
        }
        return cached
    }

    suspend fun saveResponse(
        prompt: String,
        responseContent: String,
        modelId: String,
        conversationId: String? = null,
        multimodalType: String = "TEXT",
        multimodalAttachmentsJson: String = "[]",
        thoughtTrace: String? = null,
        tokensUsed: Int = 0,
        latencyMs: Long = 0L,
        isTrainingCandidate: Boolean = false,
        isPinned: Boolean = false
    ): String {
        val normalized = normalizePrompt(prompt)
        val key = generateCacheKey(normalized, modelId, multimodalAttachmentsJson)
        val currentPreset = preferences.state.value.selectedPreset

        val entity = OfflineLlmResponseCacheEntity(
            cacheKey = key,
            promptQuery = prompt,
            normalizedQuery = normalized,
            modelId = modelId,
            responseContent = responseContent,
            conversationId = conversationId,
            multimodalType = multimodalType,
            multimodalAttachmentsJson = multimodalAttachmentsJson,
            thoughtTrace = thoughtTrace,
            tokensUsed = tokensUsed,
            latencyMs = latencyMs,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis(),
            accessCount = 1,
            ttlMs = currentPreset.durationMs,
            isPinned = isPinned,
            isTrainingCandidate = isTrainingCandidate
        )

        dao.insertResponse(entity)
        return key
    }

    suspend fun togglePin(cacheKey: String, currentPinnedState: Boolean) {
        dao.setPinned(cacheKey, !currentPinnedState)
    }

    suspend fun toggleTrainingCandidate(cacheKey: String, currentState: Boolean) {
        dao.setTrainingCandidate(cacheKey, !currentState)
    }

    suspend fun deleteResponse(cacheKey: String) {
        dao.deleteByKey(cacheKey)
    }

    suspend fun applyLaunchCleanupIfEnabled(): Int {
        val policy = preferences.state.value
        if (!policy.autoCleanupOnLaunch) return 0

        val protectFlag = if (policy.protectTrainingDatasets) 1 else 0
        val now = System.currentTimeMillis()
        val evictedCount = dao.evictExpired(now, protectFlag)

        // Also enforce LRU limit if exceeded
        dao.evictOldest(policy.maxLruEntries)

        if (evictedCount > 0) {
            preferences.recordCleanup(evictedCount)
        }
        return evictedCount
    }

    suspend fun manualPurgeExpired(): Int {
        val policy = preferences.state.value
        val protectFlag = if (policy.protectTrainingDatasets) 1 else 0
        val now = System.currentTimeMillis()
        val evictedCount = dao.evictExpired(now, protectFlag)
        preferences.recordCleanup(evictedCount)
        return evictedCount
    }

    suspend fun enforceLruCap(): Int {
        val policy = preferences.state.value
        return dao.evictOldest(policy.maxLruEntries)
    }

    suspend fun clearAllNonProtected(): Int {
        return dao.clearAllNonProtected()
    }

    suspend fun updateTtlPolicy(preset: TtlPreset, applyToExisting: Boolean = false) {
        preferences.setTtlPreset(preset)
        if (applyToExisting) {
            dao.updateAllTtl(preset.durationMs)
        }
    }
}
