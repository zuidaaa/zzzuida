package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Entity for persistent Workspace Snapshots (Memory State, File Tree, KV Cache Metadata, Vector Checksums).
 */
@Entity(tableName = "workspace_snapshots")
data class WorkspaceSnapshotEntity(
    @PrimaryKey val snapshotId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val activeFilesJson: String, // JSON serialization of Map<String, String> (path -> content)
    val filesCount: Int = 0,
    val totalWorkspaceSizeBytes: Long = 0L,
    val serializedKvCacheBytes: Long = 0L,
    val vectorEmbeddingChecksum: String = "",
    val summary: String = "",
    val targetPlatform: String = "Edge llama.cpp / NPU",
    val modelTag: String = "Gemma-4-2B-PEFT-INT4",
    val isPinned: Boolean = false
)

/**
 * Entity for recording Code Generation History, AST validations, sandbox runs, and refinement loops.
 */
@Entity(tableName = "code_history")
data class CodeHistoryEntity(
    @PrimaryKey val id: String,
    val snapshotId: String? = null,
    val prompt: String,
    val language: String = "kotlin", // kotlin, rust, python, json, bash
    val generatedCode: String,
    val refinementIteration: Int = 1,
    val toolActionJson: String = "{}",
    val sandboxExitCode: Int = 0,
    val sandboxOutput: String = "",
    val sandboxExecutionMs: Long = 0L,
    val astValidationPassed: Boolean = true,
    val astViolationReason: String? = null,
    val modelQuantization: String = "INT4",
    val executionTarget: String = "EDGE_NPU",
    val hasVisionInput: Boolean = false,
    val visionVectorDim: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data Access Object for Workspace Snapshots & Code Generation History.
 */
@Dao
interface WorkspaceSnapshotDao {

    // --- Workspace Snapshots Queries ---
    @Query("SELECT * FROM workspace_snapshots ORDER BY timestamp DESC")
    fun getAllSnapshots(): Flow<List<WorkspaceSnapshotEntity>>

    @Query("SELECT * FROM workspace_snapshots WHERE snapshotId = :snapshotId LIMIT 1")
    suspend fun getSnapshotById(snapshotId: String): WorkspaceSnapshotEntity?

    @Query("SELECT * FROM workspace_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): WorkspaceSnapshotEntity?

    @Query("SELECT * FROM workspace_snapshots WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinnedSnapshots(): Flow<List<WorkspaceSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WorkspaceSnapshotEntity)

    @Update
    suspend fun updateSnapshot(snapshot: WorkspaceSnapshotEntity)

    @Query("DELETE FROM workspace_snapshots WHERE snapshotId = :snapshotId")
    suspend fun deleteSnapshotById(snapshotId: String)

    @Query("DELETE FROM workspace_snapshots WHERE isPinned = 0 AND timestamp < :olderThanTimestamp")
    suspend fun pruneOldUnpinnedSnapshots(olderThanTimestamp: Long): Int

    // --- Code History Queries ---
    @Query("SELECT * FROM code_history ORDER BY createdAt DESC")
    fun getAllCodeHistory(): Flow<List<CodeHistoryEntity>>

    @Query("SELECT * FROM code_history WHERE snapshotId = :snapshotId ORDER BY refinementIteration ASC, createdAt ASC")
    fun getHistoryForSnapshot(snapshotId: String): Flow<List<CodeHistoryEntity>>

    @Query("SELECT * FROM code_history WHERE id = :id LIMIT 1")
    suspend fun getCodeHistoryById(id: String): CodeHistoryEntity?

    @Query("SELECT * FROM code_history WHERE astValidationPassed = 1 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentVerifiedCode(limit: Int = 20): Flow<List<CodeHistoryEntity>>

    @Query("SELECT * FROM code_history WHERE prompt LIKE '%' || :query || '%' OR generatedCode LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchCodeHistory(query: String): Flow<List<CodeHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeHistory(item: CodeHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeHistories(items: List<CodeHistoryEntity>)

    @Query("DELETE FROM code_history WHERE id = :id")
    suspend fun deleteCodeHistoryById(id: String)

    @Query("DELETE FROM code_history WHERE snapshotId = :snapshotId")
    suspend fun deleteCodeHistoryForSnapshot(snapshotId: String)
}
