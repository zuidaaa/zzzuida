package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeAndBenchmarkDao {

    // --- Benchmark Results ---

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC")
    fun getAllBenchmarkResults(): Flow<List<BenchmarkResultEntity>>

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBenchmarkResult(): Flow<BenchmarkResultEntity?>

    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBenchmarkResultSync(): BenchmarkResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmarkResult(result: BenchmarkResultEntity)

    @Query("DELETE FROM benchmark_results")
    suspend fun clearAllBenchmarkResults()

    // --- Knowledge & Grounding Sources ---

    @Query("SELECT * FROM knowledge_sources ORDER BY name ASC")
    fun getAllKnowledgeSources(): Flow<List<KnowledgeSourceEntity>>

    @Query("SELECT * FROM knowledge_sources ORDER BY name ASC")
    suspend fun getKnowledgeSourcesList(): List<KnowledgeSourceEntity>

    @Query("SELECT * FROM knowledge_sources WHERE id = :id LIMIT 1")
    suspend fun getKnowledgeSourceById(id: String): KnowledgeSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeSource(source: KnowledgeSourceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertKnowledgeSources(sources: List<KnowledgeSourceEntity>)

    @Update
    suspend fun updateKnowledgeSource(source: KnowledgeSourceEntity)

    @Query("UPDATE knowledge_sources SET status = :status, latencyMs = :latencyMs, lastCheckedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateSourceStatus(id: String, status: String, latencyMs: Long, timestamp: Long)

    @Query("UPDATE knowledge_sources SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleSourceEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE knowledge_sources SET totalQueriesServed = totalQueriesServed + 1 WHERE id = :id")
    suspend fun incrementSourceQueryCount(id: String)

    // --- Interactive Problem Solving Progress ---
    @Query("SELECT * FROM problem_progress ORDER BY lastAttemptTimestamp DESC")
    fun getAllProblemProgress(): Flow<List<ProblemProgressEntity>>

    @Query("SELECT * FROM problem_progress WHERE problemId = :problemId LIMIT 1")
    suspend fun getProblemProgress(problemId: String): ProblemProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProblemProgress(progress: ProblemProgressEntity)

    @Query("UPDATE problem_progress SET isBookmarked = :bookmarked WHERE problemId = :problemId")
    suspend fun toggleProblemBookmark(problemId: String, bookmarked: Boolean)

    @Query("DELETE FROM problem_progress WHERE problemId = :problemId")
    suspend fun deleteProblemProgress(problemId: String)
}
