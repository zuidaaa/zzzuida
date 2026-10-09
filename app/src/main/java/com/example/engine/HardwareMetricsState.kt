package com.example.engine

data class HardwareMetricsState(
    val temperatureCelsius: Float = 36.5f,
    val thermalState: String = "NOMINAL",
    val batteryPercent: Int = 88,
    val batteryDischargeRateMa: Int = 320,
    val cpuUsagePercent: Int = 42,
    val cpuFrequencyGhz: Float = 2.4f,
    val gpuUsagePercent: Int = 35,
    val npuTopsUsage: Float = 42.5f,
    val availableRamMb: Long = 3450L,
    val activeLlmName: String = "Gemini 3.5 Flash",
    val tokenThroughputSec: Float = 78.5f,
    val timestamp: Long = System.currentTimeMillis()
)
