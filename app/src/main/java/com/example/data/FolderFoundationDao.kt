package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderFoundationDao {
    @Query("SELECT * FROM folder_foundation WHERE folderName = :folderName ORDER BY updatedAt DESC")
    fun getFilesByFolder(folderName: String): Flow<List<FolderFileEntity>>

    @Query("SELECT * FROM folder_foundation WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchFolderFiles(query: String): Flow<List<FolderFileEntity>>

    @Query("SELECT DISTINCT folderName FROM folder_foundation ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FolderFileEntity)

    @Delete
    suspend fun deleteFile(file: FolderFileEntity)
}
