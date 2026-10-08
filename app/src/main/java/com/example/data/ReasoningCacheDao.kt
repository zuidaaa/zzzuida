package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReasoningCacheDao {

    @Query("SELECT * FROM reasoning_cache ORDER BY lastAccessedAt DESC")
    fun getAllCachedResults(): Flow<List<ReasoningCacheEntity>>

    @Query("SELECT * FROM reasoning_cache WHERE isFavorite = 1 ORDER BY lastAccessedAt DESC")
    fun getFavoriteCachedResults(): Flow<List<ReasoningCacheEntity>>

    @Query("SELECT * FROM reasoning_cache WHERE domainCategory = :domain ORDER BY lastAccessedAt DESC")
    fun getCachedResultsByDomain(domain: String): Flow<List<ReasoningCacheEntity>>

    @Query("""
        SELECT * FROM reasoning_cache 
        WHERE promptQuery LIKE '%' || :query || '%' 
           OR answerContent LIKE '%' || :query || '%' 
           OR thoughtProcess LIKE '%' || :query || '%'
           OR domainCategory LIKE '%' || :query || '%'
        ORDER BY lastAccessedAt DESC
    """)
    fun searchCachedResults(query: String): Flow<List<ReasoningCacheEntity>>

    @Query("SELECT * FROM reasoning_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCachedResultByKey(cacheKey: String): ReasoningCacheEntity?

    @Query("""
        SELECT * FROM reasoning_cache 
        WHERE normalizedQuery = :normalizedQuery 
          AND modelId = :modelId 
          AND thinkingLevel = :thinkingLevel 
        LIMIT 1
    """)
    suspend fun findMatchingCache(
        normalizedQuery: String,
        modelId: String,
        thinkingLevel: String
    ): ReasoningCacheEntity?

    @Query("""
        SELECT * FROM reasoning_cache 
        WHERE normalizedQuery = :normalizedQuery 
        ORDER BY lastAccessedAt DESC 
        LIMIT 1
    """)
    suspend fun findMatchingQuery(normalizedQuery: String): ReasoningCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedResult(cacheItem: ReasoningCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedResults(items: List<ReasoningCacheEntity>)

    @Update
    suspend fun updateCachedResult(cacheItem: ReasoningCacheEntity)

    @Query("UPDATE reasoning_cache SET hitCount = hitCount + 1, lastAccessedAt = :accessTime WHERE cacheKey = :cacheKey")
    suspend fun incrementHitCount(cacheKey: String, accessTime: Long = System.currentTimeMillis())

    @Query("UPDATE reasoning_cache SET isFavorite = :isFavorite WHERE cacheKey = :cacheKey")
    suspend fun toggleFavorite(cacheKey: String, isFavorite: Boolean)

    @Query("UPDATE reasoning_cache SET userNotes = :userNotes WHERE cacheKey = :cacheKey")
    suspend fun updateNotes(cacheKey: String, userNotes: String)

    @Query("DELETE FROM reasoning_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCachedResult(cacheKey: String)

    @Query("DELETE FROM reasoning_cache")
    suspend fun clearAllCache()

    @Query("SELECT COUNT(*) FROM reasoning_cache")
    fun getCacheCount(): Flow<Int>

    @Query("SELECT SUM(thinkingTokens) FROM reasoning_cache")
    fun getTotalTokensCached(): Flow<Long?>

    @Query("SELECT SUM(hitCount) FROM reasoning_cache")
    fun getTotalHitCount(): Flow<Long?>

    @Query("DELETE FROM reasoning_cache WHERE isFavorite = 0 AND cacheKey NOT IN (SELECT cacheKey FROM reasoning_cache ORDER BY lastAccessedAt DESC LIMIT :keepLimit)")
    suspend fun pruneOldCache(keepLimit: Int)

    // Deep Thinking Sessions
    @Query("SELECT * FROM deep_thinking_sessions ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<DeepThinkingSessionEntity>>

    @Query("SELECT * FROM deep_thinking_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): DeepThinkingSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DeepThinkingSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<DeepThinkingSessionEntity>)

    @Update
    suspend fun updateSession(session: DeepThinkingSessionEntity)

    @Query("DELETE FROM deep_thinking_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM deep_thinking_sessions")
    suspend fun clearAllSessions()

    // Step Traces
    @Query("SELECT * FROM cached_reasoning_steps WHERE cacheKey = :cacheKey ORDER BY stepIndex ASC")
    fun getStepsForCacheKey(cacheKey: String): Flow<List<CachedReasoningStepEntity>>

    @Query("SELECT * FROM cached_reasoning_steps WHERE cacheKey = :cacheKey ORDER BY stepIndex ASC")
    suspend fun getStepsListForCacheKey(cacheKey: String): List<CachedReasoningStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<CachedReasoningStepEntity>)

    @Query("DELETE FROM cached_reasoning_steps WHERE cacheKey = :cacheKey")
    suspend fun deleteStepsForCacheKey(cacheKey: String)

    // Training Datasets (Uploads for datasets to train LLM)
    @Query("SELECT * FROM training_datasets ORDER BY uploadedAt DESC")
    fun getAllDatasets(): Flow<List<LlmDatasetEntity>>

    @Query("SELECT * FROM training_datasets WHERE id = :id LIMIT 1")
    suspend fun getDatasetById(id: String): LlmDatasetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: LlmDatasetEntity)

    @Query("DELETE FROM training_datasets WHERE id = :id")
    suspend fun deleteDataset(id: String)

    @Query("DELETE FROM training_datasets")
    suspend fun clearAllDatasets()

    // Web Watchers
    @Query("SELECT * FROM web_watchers ORDER BY lastCheckedTimestamp DESC")
    fun getAllWebWatchers(): Flow<List<WebWatcherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebWatcher(watcher: WebWatcherEntity)

    @Query("DELETE FROM web_watchers WHERE id = :id")
    suspend fun deleteWebWatcher(id: String)

    // Wiki Pages
    @Query("SELECT * FROM wiki_pages ORDER BY lastUpdated DESC")
    fun getAllWikiPages(): Flow<List<WikiPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWikiPage(page: WikiPageEntity)

    @Query("DELETE FROM wiki_pages WHERE id = :id")
    suspend fun deleteWikiPage(id: String)

    @Query("""
        SELECT * FROM wiki_pages 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
           OR tags LIKE '%' || :query || '%'
        ORDER BY lastUpdated DESC
    """)
    fun searchWikiPages(query: String): Flow<List<WikiPageEntity>>

    @Query("SELECT * FROM web_watchers LIMIT 1")
    suspend fun getAnyWatcher(): WebWatcherEntity?

    @Query("SELECT * FROM wiki_pages LIMIT 1")
    suspend fun getAnyWikiPage(): WikiPageEntity?

    @Query("SELECT * FROM wiki_pages WHERE overnightMaintained = 0")
    suspend fun getPendingWikiPages(): List<WikiPageEntity>

    @Query("UPDATE wiki_pages SET overnightMaintained = 1, lastUpdated = :timestamp, entityLinksMetadata = :linksMetadata, reasoningAuditLog = :auditLog WHERE id = :id")
    suspend fun updateWikiPageMaintenance(id: String, timestamp: Long, linksMetadata: String, auditLog: String)

    // Skills & Synchronization
    @Query("SELECT * FROM agent_skills ORDER BY downloadCount DESC, name ASC")
    fun getAllAgentSkills(): Flow<List<AgentSkillEntity>>

    @Query("SELECT * FROM agent_skills WHERE id = :id LIMIT 1")
    suspend fun getAgentSkillById(id: String): AgentSkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentSkills(skills: List<AgentSkillEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentSkill(skill: AgentSkillEntity)

    @Query("UPDATE agent_skills SET isInstalled = :isInstalled, isActive = :isActive WHERE id = :id")
    suspend fun updateAgentSkillStatus(id: String, isInstalled: Boolean, isActive: Boolean)

    @Query("UPDATE agent_skills SET isActive = NOT isActive WHERE id = :id")
    suspend fun toggleAgentSkillActive(id: String)

    @Query("DELETE FROM agent_skills WHERE id = :id")
    suspend fun deleteAgentSkill(id: String)

    @Query("SELECT * FROM skills_sync_logs ORDER BY timestamp DESC")
    fun getAllSkillSyncLogs(): Flow<List<SkillSyncLogEntity>>

    @Query("SELECT * FROM skills_sync_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSkillSyncLogs(limit: Int = 20): Flow<List<SkillSyncLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillSyncLog(log: SkillSyncLogEntity)

    @Query("SELECT * FROM skills_sync_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSkillSyncLog(): SkillSyncLogEntity?

    @Query("DELETE FROM skills_sync_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun clearOldSyncLogs(cutoffTimestamp: Long)
}
