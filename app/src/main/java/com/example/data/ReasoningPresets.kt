package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.engine.ThinkingLevel

data class ReasoningPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val promptTemplate: String,
    val recommendedLevel: ThinkingLevel,
    val icon: ImageVector,
    val domainTag: String,
    val systemPrompt: String = ""
)

object PresetCatalog {
    val presets = listOf(
        ReasoningPreset(
            id = "code_execution",
            title = "Code Execution Engine",
            subtitle = "Run Bash, Python, and Node.js commands. Install packages, run tests, build apps.",
            promptTemplate = "Execute a Python script that validates container environment details, lists available packages, and runs a diagnostic check.",
            recommendedLevel = ThinkingLevel.HIGH,
            icon = Icons.Default.Code,
            domainTag = "Execution",
            systemPrompt = "You are an expert systems engineer. You can run Bash, Python, and Node.js commands, install packages, and build applications."
        ),
        ReasoningPreset(
            id = "file_explorer",
            title = "File Manager & Explorer",
            subtitle = "Read, write, edit, search, copy, paste and list files. Files persist across interactions.",
            promptTemplate = "Conduct a complete workspace audit: recursively list directory structure, inspect build configurations, and search for active source patterns.",
            recommendedLevel = ThinkingLevel.MEDIUM,
            icon = Icons.Default.FolderOpen,
            domainTag = "File Manager",
            systemPrompt = "You are a senior DevOps engineer and security auditor with full workspace access."
        ),
        ReasoningPreset(
            id = "web_access",
            title = "Web Access & Search",
            subtitle = "Google Search and URL fetching for live/grounded data.",
            promptTemplate = "Search Google for the latest Android development milestones, fetch documentation URLs, and compile a feature changelog.",
            recommendedLevel = ThinkingLevel.EXTENDED,
            icon = Icons.Default.Language,
            domainTag = "Web Grounding",
            systemPrompt = "You are an elite research assistant. Ground all statements in authoritative web citations and direct links."
        ),
        ReasoningPreset(
            id = "context_compaction",
            title = "Semantic Context Compaction",
            subtitle = "Automatic context compaction to support long-running, multi-turn sessions without losing context or hitting token limits.",
            promptTemplate = "Analyze our current chat history and demonstrate how context compaction keeps key variables and session parameters alive while discarding redundant noise.",
            recommendedLevel = ThinkingLevel.HIGH,
            icon = Icons.Default.Psychology,
            domainTag = "Cognition",
            systemPrompt = "You are a cognitive computing architect. Explain and simulate context compaction and dynamic token-pruning protocols."
        ),
        ReasoningPreset(
            id = "code_architect",
            title = "Code Synthesis & Bug Hunt",
            subtitle = "Optimal algorithms, time/space trade-offs & edge cases",
            promptTemplate = "Write a high-performance, memory-efficient algorithm to find the Longest Substring Without Repeating Characters. Explain the two-pointer invariant and space-time complexity.",
            recommendedLevel = ThinkingLevel.HIGH,
            icon = Icons.Default.Code,
            domainTag = "Coding",
            systemPrompt = "You are a Principal Software Architect. Deconstruct problems with strict algorithmic rigor, asymptotic complexity analysis, and clean production code."
        ),
        ReasoningPreset(
            id = "math_proof",
            title = "Math Proof & Derivation",
            subtitle = "Symbolic proofs, first-principles logic & verification",
            promptTemplate = "Prove mathematically that the square root of 2 is irrational using proof by contradiction. Show all intermediate deductions.",
            recommendedLevel = ThinkingLevel.HIGH,
            icon = Icons.Default.Calculate,
            domainTag = "Math",
            systemPrompt = "You are a Theoretical Mathematician. Provide formal step-by-step proofs with boundary checks and counter-example testing."
        ),
        ReasoningPreset(
            id = "system_design",
            title = "Distributed Architecture",
            subtitle = "High-scale topologies, CAP theorem & trade-off matrices",
            promptTemplate = "Design a globally distributed, low-latency URL shortener (like TinyURL) that handles 100M daily writes and 1B reads. Detail caching, database sharding, and collision handling.",
            recommendedLevel = ThinkingLevel.EXTENDED,
            icon = Icons.Default.AccountTree,
            domainTag = "Systems",
            systemPrompt = "You are a Chief Systems Architect. Detail distributed topologies, latency bottlenecks, consensus protocols, and failover resilience."
        ),
        ReasoningPreset(
            id = "logic_puzzle",
            title = "Logic Puzzle & Deduction",
            subtitle = "Knights & knaves, probability paradoxes & game theory",
            promptTemplate = "Explain the Monty Hall problem with 3 doors. Why is switching to the remaining door mathematically superior? Provide a probability decision tree.",
            recommendedLevel = ThinkingLevel.MEDIUM,
            icon = Icons.Default.Psychology,
            domainTag = "Logic",
            systemPrompt = "You are a Master of Formal Logic. Break down logic riddles into truth tables, conditional probability formulas, and state diagrams."
        ),
        ReasoningPreset(
            id = "dialectic_debate",
            title = "Dialectic & Steelman Debate",
            subtitle = "Thesis, antithesis & synthesis across opposing views",
            promptTemplate = "Conduct a rigorous dialectic debate on whether Artificial General Intelligence (AGI) should be open-source or strictly closed/licensed. Steelman both arguments before synthesizing.",
            recommendedLevel = ThinkingLevel.EXTENDED,
            icon = Icons.Default.Forum,
            domainTag = "Philosophy",
            systemPrompt = "You are a Dialectic Philosopher. Steelman contrasting viewpoints, eliminate biases, and synthesize harmonious solutions."
        ),
        ReasoningPreset(
            id = "science_sandbox",
            title = "Scientific Problem Solver",
            subtitle = "Physics principles, chemical reactions & thermodynamic laws",
            promptTemplate = "Why does ice float on water? Explain the anomalous density of water through hydrogen bonding and molecular crystal lattice geometry.",
            recommendedLevel = ThinkingLevel.MEDIUM,
            icon = Icons.Default.Science,
            domainTag = "Science",
            systemPrompt = "You are a Senior Physicist. Explain natural phenomena using fundamental physical laws and molecular dynamics."
        ),
        ReasoningPreset(
            id = "hybrid_voting_refine",
            title = "Hybrid Voting & Cross-Critique (Ollama)",
            subtitle = "3-Phase Multi-Model Consensus: DeepSeek-R1 + Qwen2.5-Coder + Chief Judge",
            promptTemplate = "Erstelle eine Python-Funktion, die verschachtelte JSON-Objekte flachklopft (flatten), dabei aber Listen-Indizes als 'key[0]' beibehält.",
            recommendedLevel = ThinkingLevel.HIGH,
            icon = Icons.Default.Code,
            domainTag = "Consensus",
            systemPrompt = "You are an autonomous Multi-Agent Consensus Orchestrator executing Phase 1 (Parallel Drafts), Phase 2 (Mutual Cross-Critique), and Phase 3 (Chief Architect Synthesis)."
        )
    )
}
