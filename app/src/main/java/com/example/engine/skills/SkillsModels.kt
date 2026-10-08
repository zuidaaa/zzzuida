package com.example.engine.skills

enum class SkillCategory(val displayName: String, val iconEmoji: String) {
    ALL("Alle", "🌟"),
    CODING("Coding & Dev", "💻"),
    RESEARCH("Deep Research", "🔬"),
    DESIGN("Design & UI/UX", "🎨"),
    AUTOMATION("Automation & Workspace", "⚡"),
    SECURITY("Security & Audit", "🛡️"),
    AGENTIC("Agentic Tools & RAG", "🤖")
}

data class AgentSkill(
    val id: String, // e.g. "android/jetpack-compose-s26"
    val name: String,
    val description: String,
    val ownerRepo: String, // e.g. "skills-sh/compose-expert"
    val version: String = "1.0.0",
    val category: SkillCategory,
    val allowedTools: List<String> = emptyList(),
    val instructionsMarkdown: String, // The full SKILL.md body
    val isInstalled: Boolean = false,
    val isActive: Boolean = false,
    val downloadCount: Int = 1200,
    val stars: Int = 145,
    val author: String = "Community",
    val sourceUrl: String = "https://skills.sh",
    val license: String = "MIT",
    val compatibility: String = "gemini-3.7, claude-code, ollama, cursor",
    val installedAt: Long = System.currentTimeMillis()
) {
    val icon: String get() = category.iconEmoji
    val securityScore: Int get() = if (allowedTools.contains("run_command") && instructionsMarkdown.contains("root")) 82 else if (allowedTools.contains("run_command")) 94 else 99
    val securityChecksum: String get() = (id + version + instructionsMarkdown.length).hashCode().toString().take(12)
    val npuOffloaded: Boolean get() = allowedTools.any { it.contains("npu") || it.contains("sve2") || it.contains("vulkan") || it.contains("hardware") }
}

data class SkillExecutionResult(
    val skillName: String,
    val enrichedPrompt: String,
    val simulatedThought: String,
    val simulatedResponse: String,
    val matchedTools: List<String>,
    val executionTimeMs: Long = 28L,
    val npuOffloaded: Boolean = true,
    val output: String = simulatedResponse,
    val timestamp: Long = System.currentTimeMillis()
)

data class SkillsShCliCommand(
    val command: String, // e.g. "npx skills add vercel/nextjs-app-router"
    val targetRepo: String,
    val status: String = "READY"
)

data class SkillSafetyReport(
    val skillId: String,
    val isSafe: Boolean,
    val securityScore: Int,
    val stabilityScore: Int,
    val compatibilityScore: Int,
    val knoxCertified: Boolean = true,
    val npuOptimized: Boolean = false,
    val concerns: List<String> = emptyList()
)

data class SkillsAuditSummary(
    val totalSkills: Int,
    val verifiedSafeCount: Int,
    val npuAcceleratedCount: Int,
    val averageSecurityScore: Int,
    val lastSyncTimestamp: Long,
    val syncStatus: String,
    val installedCount: Int = 0,
    val activeCount: Int = 0,
    val reports: List<SkillSafetyReport> = emptyList()
)

