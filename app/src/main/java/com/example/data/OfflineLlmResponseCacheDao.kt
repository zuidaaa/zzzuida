package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineLlmResponseCacheDao {

    @Query("""
        SELECT * FROM offline_llm_response_cache
        WHERE cacheKey = :cacheKey
          AND (ttlMs == 0 OR (createdAt + ttlMs >= :currentTime) OR isPinned == 1)
        LIMIT 1
    """)
    suspend fun getValidCachedResponse(cacheKey: String, currentTime: Long): OfflineLlmResponseCacheEntity?

    @Query("""
        SELECT * FROM offline_llm_response_cache
        WHERE normalizedQuery = :normalizedQuery
          AND modelId = :modelId
          AND (ttlMs == 0 OR (createdAt + ttlMs >= :currentTime) OR isPinned == 1)
        ORDER BY lastAccessedAt DESC
        LIMIT 1
    """)
    suspend fun findMatchingResponse(
        normalizedQuery: String,
        modelId: String,
        currentTime: Long
    ): OfflineLlmResponseCacheEntity?

    @Query("SELECT * FROM offline_llm_response_cache ORDER BY lastAccessedAt DESC")
    fun getAllCachedResponses(): Flow<List<OfflineLlmResponseCacheEntity>>

    @Query("SELECT * FROM offline_llm_response_cache WHERE isPinned = 1 ORDER BY lastAccessedAt DESC")
    fun getPinnedResponses(): Flow<List<OfflineLlmResponseCacheEntity>>

    @Query("""
        SELECT * FROM offline_llm_response_cache 
        WHERE conversationId = :conversationId 
        ORDER BY createdAt ASC
    """)
    fun getResponsesByConversation(conversationId: String): Flow<List<OfflineLlmResponseCacheEntity>>

    @Query("""
        SELECT * FROM offline_llm_response_cache
        WHERE promptQuery LIKE '%' || :query || '%'
           OR responseContent LIKE '%' || :query || '%'
           OR modelId LIKE '%' || :query || '%'
        ORDER BY lastAccessedAt DESC
    """)
    fun searchResponses(query: String): Flow<List<OfflineLlmResponseCacheEntity>>

    @Query("SELECT COUNT(*) FROM offline_llm_response_cache")
    fun countTotal(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM offline_llm_response_cache
        WHERE isPinned = 0
          AND (:protectTrainingData = 0 OR isTrainingCandidate = 0)
          AND ttlMs > 0
          AND (createdAt + ttlMs < :currentTime)
    """)
    fun countExpired(currentTime: Long, protectTrainingData: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM offline_llm_response_cache WHERE isPinned = 1")
    fun countPinned(): Flow<Int>

    @Query("SELECT COUNT(*) FROM offline_llm_response_cache WHERE isTrainingCandidate = 1")
    fun countTrainingCandidates(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(entity: OfflineLlmResponseCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponses(entities: List<OfflineLlmResponseCacheEntity>)

    @Query("""
        UPDATE offline_llm_response_cache
        SET lastAccessedAt = :timestamp,
            accessCount = accessCount + 1
        WHERE cacheKey = :cacheKey
    """)
    suspend fun updateAccess(cacheKey: String, timestamp: Long)

    @Query("UPDATE offline_llm_response_cache SET isPinned = :isPinned WHERE cacheKey = :cacheKey")
    suspend fun setPinned(cacheKey: String, isPinned: Boolean)

    @Query("UPDATE offline_llm_response_cache SET isTrainingCandidate = :isCandidate WHERE cacheKey = :cacheKey")
    suspend fun setTrainingCandidate(cacheKey: String, isCandidate: Boolean)

    @Query("DELETE FROM offline_llm_response_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteByKey(cacheKey: String)

    @Query("""
        DELETE FROM offline_llm_response_cache
        WHERE isPinned = 0
          AND (:protectTrainingData = 0 OR isTrainingCandidate = 0)
          AND ttlMs > 0
          AND (createdAt + ttlMs < :currentTime)
    """)
    suspend fun evictExpired(currentTime: Long, protectTrainingData: Int): Int

    @Query("""
        DELETE FROM offline_llm_response_cache
        WHERE cacheKey IN (
            SELECT cacheKey FROM offline_llm_response_cache
            WHERE isPinned = 0 AND isTrainingCandidate = 0
            ORDER BY lastAccessedAt ASC
            LIMIT MAX(0, (SELECT COUNT(*) FROM offline_llm_response_cache WHERE isPinned = 0 AND isTrainingCandidate = 0) - :keepCount)
        )
    """)
    suspend fun evictOldest(keepCount: Int): Int

    @Query("""
        DELETE FROM offline_llm_response_cache
        WHERE isPinned = 0 AND isTrainingCandidate = 0
    """)
    suspend fun clearAllNonProtected(): Int

    @Query("""
        UPDATE offline_llm_response_cache
        SET ttlMs = :newTtlMs
        WHERE isPinned = 0
    """)
    suspend fun updateAllTtl(newTtlMs: Long): Int
}
