package com.example.engine.workspace

data class GmailMessageItem(
    val id: String,
    val senderName: String,
    val senderEmail: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val isUnread: Boolean,
    val timestamp: Long,
    val category: String = "Primary", // "Primary", "Family", "Travel", "Orders"
    val aiSummary: String = ""
)

data class CalendarEventItem(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val location: String = "",
    val isAutoBookedByAgent: Boolean = false,
    val status: String = "CONFIRMED"
)

data class DriveFileItem(
    val id: String,
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val modifiedTime: Long,
    val webViewLink: String = "",
    val isWikiBackup: Boolean = false
)

data class GoogleContactItem(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val company: String = "",
    val avatarLetter: String = name.take(1).uppercase()
)

data class GoogleOneStatus(
    val planName: String = "Google One 100 GB Plan",
    val totalBytes: Long = 100L * 1024 * 1024 * 1024, // 100 GB
    val usedBytes: Long = 18_450_000_000L, // ~18.45 GB
    val driveBytes: Long = 8_200_000_000L,
    val gmailBytes: Long = 4_150_000_000L,
    val photosBytes: Long = 5_800_000_000L,
    val deviceBackupBytes: Long = 300_000_000L,
    val isAutoBackupEnabled: Boolean = true,
    val lastBackupTimestamp: Long = System.currentTimeMillis() - 3600_000L * 4,
    val backupStatusMessage: String = "All Karpathy LLM Wiki nodes and on-device SQLite databases backed up securely to Google One."
)

data class DriveSyncResult(
    val isSuccess: Boolean,
    val filesSyncedCount: Int,
    val destinationFolder: String,
    val message: String
)

data class GoogleOneBackupResult(
    val isSuccess: Boolean,
    val timestamp: Long,
    val totalNodesBackedUp: Int,
    val backupSizeBytes: Long,
    val summary: String
)
