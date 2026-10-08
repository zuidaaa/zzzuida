package com.example.data

/**
 * Data structures for CPU-bound background aggregation jobs.
 */
data class DataPoint(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: Category = Category.GENERAL,
    val value: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Category {
    GENERAL,
    REASONING,
    VISION,
    AUDIO,
    SYSTEM,
    BENCHMARK
}
