package com.example.engine.mks2

/**
 * MKS² – Mobile KV-Cache Synergy Stack v2.0.0 Models
 * Target: Samsung Galaxy S26 Ultra / Snapdragon 8 Elite Gen 5
 * (12 GB LPDDR5X, UFS 4.0, Hexagon NPU with +50% Shared Memory, Unified Memory Architecture)
 */

enum class MemoryTier(
    val tierId: String,
    val tierName: String,
    val capacityDescription: String,
    val latencyDescription: String,
    val governor: String
) {
    T0_NPU(
        tierId = "T0",
        tierName = "NPU Shared Memory / LPDDR5X",
        capacityDescription = "~6.8 GB usable",
        latencyDescription = "~10 ns",
        governor = "mzCache"
    ),
    T1_DRAM(
        tierId = "T1",
        tierName = "System DRAM",
        capacityDescription = "Shared with OS (~3.5 GB allocatable)",
        latencyDescription = "~100 ns",
        governor = "mzCache"
    ),
    T2_UFS4(
        tierId = "T2",
        tierName = "UFS 4.0 Flash Local",
        capacityDescription = "512 GB – 1 TB (~4 GB/s Read, ~2 GB/s Write)",
        latencyDescription = "~100 μs (Read), ~500 μs (Write)",
        governor = "KVSwap"
    ),
    T3_CLOUD(
        tierId = "T3",
        tierName = "Cloud / Remote Storage (TEE)",
        capacityDescription = "Unlimited (AES-GCM Encrypted)",
        latencyDescription = "~10–100 ms",
        governor = "Hybrid-Bridge / FlexServe TEE"
    )
}

data class MemoryTierTelemetry(
    val tier: MemoryTier,
    val allocatedTokens: Int,
    val maxCapacityTokens: Int,
    val fillRatio: Float,
    val bandwidthThroughputMBs: Float,
    val compressionRatio: Float = 1.0f
)

data class GateCheckResult(
    val id: String,
    val name: String,
    val passed: Boolean,
    val details: String,
    val latencyMs: Long
)

data class PreFlightGateState(
    val isVerified: Boolean = false,
    val isMutationLocked: Boolean = true,
    val gateScore: Int = 0,
    val systemStateChecks: List<GateCheckResult> = emptyList(),
    val dependencyChecks: List<GateCheckResult> = emptyList(),
    val knowledgeStoreChecks: List<GateCheckResult> = emptyList(),
    val lastVerifiedTimestamp: String = "Not yet verified"
)

enum class CircuitMode {
    CLOSED,     // Normal operation: all traffic passes through primary engines
    OPEN,       // Tripped on fault: mutation isolated, routes to resilient fallback paths
    HALF_OPEN   // Probing recovery: testing canary block
}

data class CircuitIncident(
    val id: String,
    val timestamp: String,
    val component: String,
    val errorMessage: String,
    val fallbackEngaged: String,
    val resolved: Boolean
)

data class CircuitBreakerState(
    val mode: CircuitMode = CircuitMode.CLOSED,
    val failureCount: Int = 0,
    val failureThreshold: Int = 3,
    val activeFallbackPath: String = "Primary (NPUGen + mzCache Zero-Wait)",
    val isolatedErrors: List<CircuitIncident> = emptyList()
)

data class NpuGenLockState(
    val isReconstructionLockAcquired: Boolean = false,
    val activeBlockId: String? = null,
    val totalReconstructedTokens: Long = 12480L,
    val int4AnchorEfficiency: Float = 94.8f,
    val trimMemoryInterceptions: Int = 18
)

data class KvCacheBlock(
    val id: String,
    val tokenCount: Int,
    val importance: Float,
    val assignedTier: MemoryTier,
    val isCompressed: Boolean,
    val crc64Checksum: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Mks2SynergyState(
    val projectName: String = "MKS² – Mobile KV-Cache Synergy Stack",
    val version: String = "2.0.0",
    val targetHardware: String = "Samsung Galaxy S26 Ultra / Snapdragon 8 Elite Gen 5",
    val activePhase: String = "Phase 1: Pre-Flight Gate & Resilient Validation",
    val systemHealthScore: Int = 98,
    val totalTtftReduction: String = "7.2× Speedup (<0.5s)",
    val contextExtensionFactor: String = "16× (>32K Tokens)",
    val energySavings: String = "34.5% Reduction",
    val securityStatus: String = "FlexServe TEE + AES-GCM Active",
    val activeTier: String = "T0 (NPU Shared Memory / ~6.8 GB)",
    val activeEngine: String = "mzCache + KVSwap + NPUGen + Agent.xpu",
    
    // Phase 1: Pre-Flight Gate
    val preFlightGate: PreFlightGateState = PreFlightGateState(),

    // Phase 2: Resilient Loop & Circuit-Breaker
    val circuitBreaker: CircuitBreakerState = CircuitBreakerState(),

    // Core Hardware & Memory Tiers
    val tierTelemetries: List<MemoryTierTelemetry> = emptyList(),

    // NPUGen Lock & CRC-64 Blocks
    val npuGenLock: NpuGenLockState = NpuGenLockState(),
    val kvBlocks: List<KvCacheBlock> = emptyList(),

    // Log trace
    val logTrace: List<String> = emptyList()
)
