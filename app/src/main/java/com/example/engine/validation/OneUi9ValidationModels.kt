package com.example.engine.validation

enum class ValidationCategory(val displayName: String, val iconEmoji: String) {
    SECURITY("Sicherheit", "🔒"),
    STABILITY("Stabilität", "🛡️"),
    COMPATIBILITY("Kompatibilität", "📱"),
    CODE_QUALITY("Code & Syntax", "💻"),
    PERFORMANCE("Performance & NPU", "⚡")
}

enum class ValidationStatus(val label: String) {
    PASSED("Bestanden"),
    WARNING("Warnung"),
    FAILED("Kritisch"),
    INFO("Info")
}

data class ValidationItem(
    val id: String,
    val category: ValidationCategory,
    val title: String,
    val status: ValidationStatus,
    val score: Int, // 0..100
    val metric: String,
    val details: String,
    val recommendation: String
)

data class DirectInputValidationResult(
    val timestamp: Long = System.currentTimeMillis(),
    val inputSnippet: String,
    val inputLength: Int,
    val securityScore: Int,
    val stabilityScore: Int,
    val compatibilityScore: Int,
    val codeScore: Int,
    val performanceScore: Int,
    val overallScore: Int,
    val items: List<ValidationItem>,
    val detectedIssues: List<String>,
    val executionSafetyVerdict: String,
    val hardwareTarget: String = "Samsung Galaxy S26 Ultra (Exynos 2600 + NPU Dual-Core 92 TOPS)",
    val osTarget: String = "Android 17 (API 37) / Samsung One UI 9.0",
    val estimatedNpuThroughputTps: Double = 40.8,
    val estimatedTtftMs: Long = 42L,
    val memoryFootprintEstMb: Double = 34.5
)

data class EnvironmentCleanupReport(
    val timestamp: Long = System.currentTimeMillis(),
    val ramFreedMb: Double,
    val cacheEntriesPruned: Int,
    val databaseVacuumed: Boolean,
    val diskBytesSavedKb: Long,
    val orphanVectorsCleared: Int,
    val apkCompressionOptimized: Boolean,
    val memoryBeforeMb: Double,
    val memoryAfterMb: Double,
    val summaryText: String
)
