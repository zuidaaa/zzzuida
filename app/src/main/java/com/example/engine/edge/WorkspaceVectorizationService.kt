package com.example.engine.edge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.math.sqrt

/**
 * Workspace Vectorization Service (State Management Design - Point 2).
 *
 * Implements:
 * "Vektorisierung des Arbeitsbereichs: Anstatt den gesamten Quellcode zu übermitteln,
 * wird die Semantik der Dateien durch kompakte, lokale Embedding-Modelle erfasst und als Kontext integriert."
 *
 * Provides:
 * - Local lightweight semantic vectorization (128-dimensional dense float vector)
 * - Cosine similarity calculation between code embeddings and user queries
 * - Relevant context assembly within strict token constraints for Edge LLM context window
 */
class WorkspaceVectorizationService {

    companion object {
        const val EMBEDDING_DIMENSION = 128

        // Key code semantic anchors for mobile development domains
        private val CODE_SEMANTIC_ANCHORS = listOf(
            "compose", "ui", "view", "card", "button", "screen", "theme", "color",
            "state", "flow", "viewmodel", "coroutine", "suspend", "mutable", "collect",
            "database", "dao", "room", "entity", "query", "insert", "sql", "table",
            "network", "api", "retrofit", "ktor", "json", "endpoint", "fetch", "http",
            "engine", "edge", "llm", "npu", "cpu", "telemetry", "hardware", "quantization",
            "ast", "security", "sandbox", "validate", "syntax", "tool", "action", "execution",
            "whisper", "speech", "audio", "voice", "transcript", "realtime", "multimodal",
            "vision", "encoder", "vit", "mockup", "vector", "embedding", "projection",
            "snapshot", "kvcache", "memory", "cache", "restore", "persist", "serialization",
            "test", "unit", "assert", "mock", "verification", "refine", "loop", "benchmark",
            "python", "rust", "kotlin", "java", "c++", "jni", "tokio", "shell",
            "parser", "token", "prompt", "context", "history", "pipeline", "agent", "orchestration",
            "battery", "thermal", "governor", "latency", "throughput", "fps", "headroom", "frequency",
            "cloud", "gateway", "remote", "fallback", "sync", "export", "import", "zip",
            "clean", "solid", "mvvm", "repository", "singleton", "factory", "builder", "adapter",
            "diff", "patch", "git", "commit", "branch", "file", "tree", "workspace"
        )
    }

    /**
     * Extracts a compact 128-dimensional semantic embedding vector for a given source code content.
     * Uses a multi-tiered token frequency + structural n-gram hashing projection.
     */
    suspend fun generateEmbedding(filePath: String, codeContent: String): FileVectorEmbedding = withContext(Dispatchers.Default) {
        val vector = FloatArray(EMBEDDING_DIMENSION)
        val lowerCode = codeContent.lowercase()
        val tokens = lowerCode.split(Regex("[^a-zA-Z0-9_]+")).filter { it.length > 2 }

        // Tier 1: Semantic anchors projection (first 64 dimensions)
        for (i in 0 until 64) {
            val anchor = CODE_SEMANTIC_ANCHORS.getOrElse(i) { "token_$i" }
            val count = tokens.count { it.contains(anchor) || anchor.contains(it) }
            vector[i] = count.toFloat()
        }

        // Tier 2: Structural n-gram hashing projection (dimensions 64 to 127)
        for (token in tokens) {
            val hash = (token.hashCode() and 0x7FFFFFFF) % 64
            vector[64 + hash] += 1.0f
        }

        // Apply path heuristics boost
        val lowerPath = filePath.lowercase()
        if (lowerPath.contains("ui") || lowerPath.contains("screen")) vector[0] += 5.0f
        if (lowerPath.contains("dao") || lowerPath.contains("data") || lowerPath.contains("db")) vector[16] += 5.0f
        if (lowerPath.contains("engine") || lowerPath.contains("agent")) vector[32] += 5.0f

        // L2 Normalization of vector to unit length
        var sumSquares = 0.0f
        for (v in vector) sumSquares += (v * v)
        val norm = sqrt(sumSquares.coerceAtLeast(1e-7f))
        for (i in vector.indices) {
            vector[i] /= norm
        }

        // SHA-256 Checksum for tracking version updates
        val md = MessageDigest.getInstance("SHA-256")
        val sha256 = md.digest(codeContent.toByteArray()).joinToString("") { "%02x".format(it) }

        // Summary snippet (first non-empty lines)
        val snippet = codeContent.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("//") && !it.startsWith("#") }
            .take(3)
            .joinToString("; ")
            .take(140)

        FileVectorEmbedding(
            filePath = filePath,
            embeddingVector = vector,
            dimension = EMBEDDING_DIMENSION,
            tokenCount = tokens.size,
            summarySnippet = snippet,
            sha256Checksum = sha256,
            generatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Vectorizes a text query (e.g. user prompt or voice instruction) into the same 128-dim latent space.
     */
    suspend fun generateQueryEmbedding(query: String): FloatArray = withContext(Dispatchers.Default) {
        val vector = FloatArray(EMBEDDING_DIMENSION)
        val lowerQuery = query.lowercase()
        val tokens = lowerQuery.split(Regex("[^a-zA-Z0-9_]+")).filter { it.length > 2 }

        for (i in 0 until 64) {
            val anchor = CODE_SEMANTIC_ANCHORS.getOrElse(i) { "query_$i" }
            val count = tokens.count { it.contains(anchor) || anchor.contains(it) }
            vector[i] = count.toFloat() * 2.0f
        }

        for (token in tokens) {
            val hash = (token.hashCode() and 0x7FFFFFFF) % 64
            vector[64 + hash] += 1.0f
        }

        var sumSquares = 0.0f
        for (v in vector) sumSquares += (v * v)
        val norm = sqrt(sumSquares.coerceAtLeast(1e-7f))
        for (i in vector.indices) {
            vector[i] /= norm
        }
        vector
    }

    /**
     * Computes the Cosine Similarity between two normalized embedding vectors.
     * Returns score between -1.0 and 1.0.
     */
    fun computeCosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        if (vecA.size != vecB.size) return 0.0f
        var dot = 0.0f
        for (i in vecA.indices) {
            dot += (vecA[i] * vecB[i])
        }
        return dot.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Searches workspace file embeddings for top-K files most semantically relevant to a query.
     */
    suspend fun searchRelevantFiles(
        query: String,
        fileEmbeddings: List<FileVectorEmbedding>,
        topK: Int = 3
    ): List<WorkspaceSemanticSearchResult> = withContext(Dispatchers.Default) {
        if (fileEmbeddings.isEmpty() || query.isBlank()) return@withContext emptyList()

        val queryVec = generateQueryEmbedding(query)

        fileEmbeddings.map { embedding ->
            val score = computeCosineSimilarity(queryVec, embedding.embeddingVector)
            WorkspaceSemanticSearchResult(
                filePath = embedding.filePath,
                similarityScore = score,
                matchingSnippet = embedding.summarySnippet,
                vectorDimension = embedding.dimension
            )
        }
        .sortedByDescending { it.similarityScore }
        .take(topK)
    }

    /**
     * Builds a compact vectorized semantic context string for the Edge LLM prompt token stream.
     * Replaces bulky full-source-code dump with precise semantic descriptors.
     */
    fun buildVectorizedContextString(searchResults: List<WorkspaceSemanticSearchResult>): String {
        if (searchResults.isEmpty()) return "[Semantic Context: Empty Workspace]"
        return buildString {
            appendLine("[Vectorized Semantic Context (Zero-Fullcode Transfer)]")
            searchResults.forEachIndexed { index, res ->
                val percentage = (res.similarityScore * 100f).coerceIn(0f, 100f)
                appendLine(" - #${index + 1} File: ${res.filePath} (Match: ${"%.1f".format(percentage)}%) | Summary: ${res.matchingSnippet}")
            }
        }.trimEnd()
    }
}
