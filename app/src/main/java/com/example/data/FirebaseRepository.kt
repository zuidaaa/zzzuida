package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    
    private val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User must be signed in with Google")
    }

    suspend fun syncConversation(conv: ConversationEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("conversations").document(conv.id)
            
            val data = mapOf(
                "id" to conv.id,
                "userId" to uid,
                "title" to conv.title,
                "domain" to conv.domainTag,
                "createdAt" to conv.createdAt,
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastMessagePreview" to conv.lastMessagePreview
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync conversation failed", e)
        }
    }

    suspend fun syncMessage(msg: ChatMessageEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("conversations").document(msg.conversationId)
                .collection("messages").document(msg.id)
            
            val data = mapOf(
                "id" to msg.id,
                "conversationId" to msg.conversationId,
                "userId" to uid,
                "role" to if (msg.role == "assistant") "model" else "user",
                "content" to msg.content,
                "thoughtProcess" to msg.thoughtProcess,
                "tokensUsed" to msg.thinkingTokens,
                "latencyMs" to msg.thinkingDurationMs,
                "createdAt" to msg.timestamp
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync message failed", e)
        }
    }

    suspend fun syncReasoningCache(item: ReasoningCacheEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("reasoning_cache").document(item.cacheKey)
            
            val data = mapOf(
                "cacheKey" to item.cacheKey,
                "userId" to uid,
                "promptQuery" to item.promptQuery,
                "modelName" to item.modelName,
                "thinkingLevel" to item.thinkingLevel,
                "answerContent" to item.answerContent,
                "thoughtProcess" to item.thoughtProcess,
                "thinkingDurationMs" to item.thinkingDurationMs,
                "thinkingTokens" to item.thinkingTokens,
                "domainCategory" to item.domainCategory,
                "cachedAt" to item.cachedAt,
                "hitCount" to item.hitCount,
                "isFavorite" to item.isFavorite
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync reasoning cache failed", e)
        }
    }

    suspend fun syncProblemProgress(progress: ProblemProgressEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("problem_progress").document(progress.problemId)
            
            val data = mapOf(
                "problemId" to progress.problemId,
                "userId" to uid,
                "domain" to progress.domain,
                "status" to progress.status,
                "unlockedHintsCount" to progress.unlockedHintsCount,
                "userSolutionDraft" to progress.userSolutionDraft,
                "score" to progress.score,
                "isBookmarked" to progress.isBookmarked,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync problem progress failed", e)
        }
    }

    suspend fun syncSkill(skill: AgentSkillEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("skills").document(skill.id)
            
            val data = mapOf(
                "id" to skill.id,
                "userId" to uid,
                "name" to skill.name,
                "description" to skill.description,
                "category" to skill.category,
                "isEnabled" to (skill.isInstalled && skill.isActive),
                "version" to skill.version,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync skill failed", e)
        }
    }

    suspend fun syncWikiPage(wiki: WikiPageEntity) {
        try {
            val uid = requireUserId()
            val docRef = firestore.collection("users").document(uid)
                .collection("wiki_entities").document(wiki.id)
            
            val data = mapOf(
                "id" to wiki.id,
                "userId" to uid,
                "title" to wiki.title,
                "summary" to wiki.content.take(200),
                "category" to wiki.tags,
                "entityType" to "KNOWLEDGE_NODE",
                "reasoningLogsCount" to if (wiki.reasoningAuditLog.isNotBlank()) 1 else 0,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepo", "Sync wiki entity failed", e)
        }
    }
}
