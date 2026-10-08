package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesListForConversation(conversationId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET userFeedbackRating = :rating, userFeedbackText = :feedbackText WHERE id = :messageId")
    suspend fun updateMessageFeedback(messageId: String, rating: Int, feedbackText: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    // Local Models
    @Query("SELECT * FROM local_models ORDER BY isDownloaded DESC, name ASC")
    fun getAllLocalModels(): Flow<List<LocalModelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalModel(model: LocalModelEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocalModels(models: List<LocalModelEntity>)

    @Update
    suspend fun updateLocalModel(model: LocalModelEntity)

    @Query("UPDATE local_models SET isActive = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveLocalModel(activeId: String)

    // Generated Media
    @Query("SELECT * FROM generated_media ORDER BY createdAt DESC")
    fun getAllGeneratedMedia(): Flow<List<GeneratedMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedMedia(media: GeneratedMediaEntity)

    @Query("DELETE FROM generated_media WHERE id = :id")
    suspend fun deleteGeneratedMediaById(id: String)

    // Local Documents / RAG
    @Query("SELECT * FROM local_documents ORDER BY addedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM local_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("UPDATE local_documents SET isRagEnabled = :enabled WHERE id = :id")
    suspend fun updateRagStatus(id: String, enabled: Boolean)
}

