package com.example.data

import com.example.engine.edge.WorkspaceSnapshot
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/**
 * Repository for persisting and retrieving Edge Workspace Snapshots and Code History.
 */
class WorkspaceSnapshotRepository(
    private val workspaceSnapshotDao: WorkspaceSnapshotDao
) {
    val allSnapshots: Flow<List<WorkspaceSnapshotEntity>> = workspaceSnapshotDao.getAllSnapshots()
    val pinnedSnapshots: Flow<List<WorkspaceSnapshotEntity>> = workspaceSnapshotDao.getPinnedSnapshots()
    val allCodeHistory: Flow<List<CodeHistoryEntity>> = workspaceSnapshotDao.getAllCodeHistory()

    fun getHistoryForSnapshot(snapshotId: String): Flow<List<CodeHistoryEntity>> {
        return workspaceSnapshotDao.getHistoryForSnapshot(snapshotId)
    }

    fun searchCodeHistory(query: String): Flow<List<CodeHistoryEntity>> {
        return workspaceSnapshotDao.searchCodeHistory(query)
    }

    suspend fun getSnapshotById(snapshotId: String): WorkspaceSnapshotEntity? {
        return workspaceSnapshotDao.getSnapshotById(snapshotId)
    }

    suspend fun getLatestSnapshot(): WorkspaceSnapshotEntity? {
        return workspaceSnapshotDao.getLatestSnapshot()
    }

    suspend fun saveWorkspaceSnapshot(
        snapshot: WorkspaceSnapshot,
        targetPlatform: String = "Edge llama.cpp / NPU",
        modelTag: String = "Gemma-4-2B-PEFT-INT4",
        isPinned: Boolean = false
    ) {
        val jsonMap = JSONObject()
        snapshot.activeFiles.forEach { (path, content) ->
            jsonMap.put(path, content)
        }
        val totalBytes = snapshot.activeFiles.values.sumOf { it.toByteArray().size.toLong() }

        val entity = WorkspaceSnapshotEntity(
            snapshotId = snapshot.snapshotId,
            timestamp = snapshot.timestamp,
            activeFilesJson = jsonMap.toString(),
            filesCount = snapshot.activeFiles.size,
            totalWorkspaceSizeBytes = totalBytes,
            serializedKvCacheBytes = snapshot.serializedKvCacheBytes,
            vectorEmbeddingChecksum = snapshot.vectorEmbeddingChecksum,
            summary = snapshot.summary,
            targetPlatform = targetPlatform,
            modelTag = modelTag,
            isPinned = isPinned
        )
        workspaceSnapshotDao.insertSnapshot(entity)
    }

    suspend fun saveCodeHistory(
        id: String,
        snapshotId: String?,
        prompt: String,
        language: String,
        generatedCode: String,
        refinementIteration: Int,
        toolActionJson: String,
        sandboxExitCode: Int,
        sandboxOutput: String,
        sandboxExecutionMs: Long,
        astValidationPassed: Boolean,
        astViolationReason: String?,
        modelQuantization: String,
        executionTarget: String,
        hasVisionInput: Boolean,
        visionVectorDim: Int
    ) {
        val entity = CodeHistoryEntity(
            id = id,
            snapshotId = snapshotId,
            prompt = prompt,
            language = language,
            generatedCode = generatedCode,
            refinementIteration = refinementIteration,
            toolActionJson = toolActionJson,
            sandboxExitCode = sandboxExitCode,
            sandboxOutput = sandboxOutput,
            sandboxExecutionMs = sandboxExecutionMs,
            astValidationPassed = astValidationPassed,
            astViolationReason = astViolationReason,
            modelQuantization = modelQuantization,
            executionTarget = executionTarget,
            hasVisionInput = hasVisionInput,
            visionVectorDim = visionVectorDim,
            createdAt = System.currentTimeMillis()
        )
        workspaceSnapshotDao.insertCodeHistory(entity)
    }

    suspend fun togglePinSnapshot(snapshotId: String) {
        val existing = workspaceSnapshotDao.getSnapshotById(snapshotId) ?: return
        workspaceSnapshotDao.updateSnapshot(existing.copy(isPinned = !existing.isPinned))
    }

    suspend fun deleteSnapshot(snapshotId: String) {
        workspaceSnapshotDao.deleteCodeHistoryForSnapshot(snapshotId)
        workspaceSnapshotDao.deleteSnapshotById(snapshotId)
    }

    suspend fun deleteCodeHistory(id: String) {
        workspaceSnapshotDao.deleteCodeHistoryById(id)
    }

    /**
     * Deserializes the JSON map of files stored in a persisted snapshot.
     */
    suspend fun restoreSnapshotFiles(snapshotId: String): Map<String, String> {
        val entity = workspaceSnapshotDao.getSnapshotById(snapshotId) ?: return emptyMap()
        val result = mutableMapOf<String, String>()
        try {
            val json = JSONObject(entity.activeFilesJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = json.optString(key, "")
            }
        } catch (_: Exception) {}
        return result
    }
}
