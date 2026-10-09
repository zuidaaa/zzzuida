package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        LocalModelEntity::class,
        GeneratedMediaEntity::class,
        ReasoningCacheEntity::class,
        DeepThinkingSessionEntity::class,
        CachedReasoningStepEntity::class,
        DocumentEntity::class,
        BenchmarkResultEntity::class,
        KnowledgeSourceEntity::class,
        ProblemProgressEntity::class,
        LlmDatasetEntity::class,
        WebWatcherEntity::class,
        WikiPageEntity::class,
        AgentSkillEntity::class,
        SkillSyncLogEntity::class,
        FolderFileEntity::class,
        ProofOfThoughtEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun reasoningCacheDao(): ReasoningCacheDao
    abstract fun knowledgeAndBenchmarkDao(): KnowledgeAndBenchmarkDao
    abstract fun folderFoundationDao(): FolderFoundationDao
    abstract fun proofOfThoughtDao(): ProofOfThoughtDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add columns to chat_messages
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN previousInteractionId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN agentType TEXT NOT NULL DEFAULT 'DEFAULT'")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN interactionStepType TEXT NOT NULL DEFAULT 'CONVERSATION'")
                db.execSQL("ALTER TABLE chat_messages ADD COLUMN interactionMetadataJson TEXT NOT NULL DEFAULT '{}'")

                // 2. Create deep_thinking_sessions table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `deep_thinking_sessions` (
                        `sessionId` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `initialQuery` TEXT NOT NULL, 
                        `modelId` TEXT NOT NULL, 
                        `modelName` TEXT NOT NULL, 
                        `domainCategory` TEXT NOT NULL DEFAULT 'General', 
                        `createdAt` INTEGER NOT NULL, 
                        `updatedAt` INTEGER NOT NULL, 
                        `totalThinkingTimeMs` INTEGER NOT NULL DEFAULT 0, 
                        `totalThinkingTokens` INTEGER NOT NULL DEFAULT 0, 
                        `stepsCount` INTEGER NOT NULL DEFAULT 0, 
                        `finalSynthesisPreview` TEXT NOT NULL DEFAULT '', 
                        `isOfflineCached` INTEGER NOT NULL DEFAULT 1, 
                        `complexityRating` INTEGER NOT NULL DEFAULT 3, 
                        `verificationStatus` TEXT NOT NULL DEFAULT 'VERIFIED', 
                        `userNotes` TEXT NOT NULL DEFAULT '', 
                        PRIMARY KEY(`sessionId`)
                    )
                """.trimIndent())

                // 3. Create cached_reasoning_steps table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cached_reasoning_steps` (
                        `stepId` TEXT NOT NULL, 
                        `cacheKey` TEXT NOT NULL, 
                        `stepIndex` INTEGER NOT NULL, 
                        `phaseName` TEXT NOT NULL, 
                        `phaseBadge` TEXT NOT NULL, 
                        `headline` TEXT NOT NULL, 
                        `details` TEXT NOT NULL, 
                        `confidence` REAL NOT NULL DEFAULT 0.95, 
                        `branchLabel` TEXT NOT NULL DEFAULT 'Main Branch', 
                        `isVerified` INTEGER NOT NULL DEFAULT 1, 
                        PRIMARY KEY(`stepId`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reasoning_cache ADD COLUMN searchQueryExecuted TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE reasoning_cache ADD COLUMN confidenceScore REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE reasoning_cache ADD COLUMN customMetadataJson TEXT NOT NULL DEFAULT '{}'")
            }
        }

        // Migration 10 to 11: Wiki Schema Evolution (Entity Linking, Reasoning Logs, and Metadata)
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure wiki_pages table exists with new columns or safely add columns if table already exists
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `wiki_pages_new` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `tags` TEXT NOT NULL DEFAULT '',
                        `connectionsJson` TEXT NOT NULL DEFAULT '[]',
                        `lastUpdated` INTEGER NOT NULL,
                        `overnightMaintained` INTEGER NOT NULL DEFAULT 0,
                        `entityLinksMetadata` TEXT NOT NULL DEFAULT '{}',
                        `reasoningAuditLog` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Safely copy existing data if wiki_pages table existed
                db.execSQL("""
                    INSERT OR IGNORE INTO `wiki_pages_new` (id, title, content, tags, connectionsJson, lastUpdated, overnightMaintained)
                    SELECT id, title, content, tags, connectionsJson, lastUpdated, overnightMaintained FROM `wiki_pages`
                """.trimIndent())

                db.execSQL("DROP TABLE IF EXISTS `wiki_pages`")
                db.execSQL("ALTER TABLE `wiki_pages_new` RENAME TO `wiki_pages`")
            }
        }

        // Migration 11 to 12: Agent Skills and Sync Logs
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `agent_skills` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `ownerRepo` TEXT NOT NULL,
                        `version` TEXT NOT NULL DEFAULT '1.0.0',
                        `category` TEXT NOT NULL DEFAULT 'CODING',
                        `allowedToolsJson` TEXT NOT NULL DEFAULT '[]',
                        `instructionsMarkdown` TEXT NOT NULL DEFAULT '',
                        `isInstalled` INTEGER NOT NULL DEFAULT 0,
                        `isActive` INTEGER NOT NULL DEFAULT 0,
                        `downloadCount` INTEGER NOT NULL DEFAULT 1200,
                        `stars` INTEGER NOT NULL DEFAULT 145,
                        `author` TEXT NOT NULL DEFAULT 'Community',
                        `sourceUrl` TEXT NOT NULL DEFAULT 'https://skills.sh',
                        `license` TEXT NOT NULL DEFAULT 'MIT',
                        `compatibility` TEXT NOT NULL DEFAULT 'gemini-3.7, claude-code, ollama, cursor',
                        `installedAt` INTEGER NOT NULL,
                        `lastSyncedAt` INTEGER NOT NULL,
                        `securityStatus` TEXT NOT NULL DEFAULT 'VERIFIED_SAFE',
                        `securityChecksum` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skills_sync_logs` (
                        `id` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT NOT NULL DEFAULT 'SUCCESS',
                        `triggerType` TEXT NOT NULL DEFAULT 'PERIODIC_WORKER',
                        `skillsCheckedCount` INTEGER NOT NULL DEFAULT 0,
                        `skillsUpdatedCount` INTEGER NOT NULL DEFAULT 0,
                        `newSkillsAddedCount` INTEGER NOT NULL DEFAULT 0,
                        `securityAuditsPassed` INTEGER NOT NULL DEFAULT 0,
                        `npuOptimizationsApplied` INTEGER NOT NULL DEFAULT 0,
                        `summaryMessage` TEXT NOT NULL DEFAULT '',
                        `rawLogDetails` TEXT NOT NULL DEFAULT '',
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        // Migration 13 to 14: Proof of Thought Cache for Offline Access
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `proof_of_thought_cache` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `premiseOrHypothesis` TEXT NOT NULL,
                        `normalizedQuery` TEXT NOT NULL,
                        `domain` TEXT NOT NULL DEFAULT 'Mathematics',
                        `proofTechnique` TEXT NOT NULL DEFAULT 'Contradiction',
                        `formalProofBody` TEXT NOT NULL,
                        `reasoningStepsJson` TEXT NOT NULL DEFAULT '[]',
                        `qedConclusion` TEXT NOT NULL DEFAULT '',
                        `verificationStatus` TEXT NOT NULL DEFAULT 'VERIFIED_FORMAL',
                        `confidenceScore` REAL NOT NULL DEFAULT 0.99,
                        `thinkingTokens` INTEGER NOT NULL DEFAULT 4096,
                        `thinkingDurationMs` INTEGER NOT NULL DEFAULT 1850,
                        `modelSource` TEXT NOT NULL DEFAULT 'Gemini 3.7 Offline Deep Thinking',
                        `isOfflineAvailable` INTEGER NOT NULL DEFAULT 1,
                        `localCachedTimestamp` INTEGER NOT NULL,
                        `exportFilePath` TEXT DEFAULT NULL,
                        `datasetLinkedId` TEXT DEFAULT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gemini_deepthink_db"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_13_14)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
