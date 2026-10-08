package com.example.data

import java.util.UUID

data class EnvironmentConfig(
    val environmentId: String = "env-default-384",
    val sandboxName: String = "Default Core Sandbox",
    val customSystemInstructions: String = "You are a secure sandboxed assistant.",
    val allowedOutboundDomains: String = "api.github.com, maven.org, huggingface.co",
    val persistWorkspaceState: Boolean = true,
    val simulatedPackageRegistry: String = "Default Maven & npm Node-Safe Feed",
    val memoryLimitMb: Int = 1024,
    val networkRulesRuleCount: Int = 3
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED
}

data class AgentBackgroundTask(
    val interactionId: String = UUID.randomUUID().toString(),
    val prompt: String,
    val environmentId: String,
    val background: Boolean = true,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Float = 0.0f,
    val logs: List<String> = emptyList(),
    val resultOutput: String = "",
    val timestampMs: Long = System.currentTimeMillis()
)
