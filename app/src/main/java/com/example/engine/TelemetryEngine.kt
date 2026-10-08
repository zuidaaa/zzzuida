package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TelemetryState(
    val totalRequests: Int = 0,
    val totalLatencyMs: Long = 0L,
    val totalTokens: Int = 0,
    val cacheHits: Int = 0,
    val activeAgent: String = "DEFAULT",
    val cpuGovernor: String = "Schedutil",
    val npuThermals: String = "34.5°C",
    val memoryBudgetAvailableMb: Long = 512L
)

/**
 * Data structure representing a single row in a telemetry sensor metrics dataset
 * (e.g., timestamp, cpu_temp_c, npu_utilization_pct, power_draw_mw, fps).
 */
data class SensorMetricRow(
    val timestamp: String,
    val cpuTempC: Double,
    val npuUtilizationPct: Double,
    val powerDrawMw: Double,
    val fps: Double
)

data class TelemetryValidationReport(
    val totalRows: Int,
    val validRows: Int,
    val warnings: List<String>,
    val errors: List<String>
)

object TelemetryEngine {
    private val _state = MutableStateFlow(TelemetryState())
    val state: StateFlow<TelemetryState> = _state.asStateFlow()

    /**
     * Validates a single telemetry sensor metrics row against strict hardware invariants
     * and thermal/frame-rate thresholds:
     * - Invariant: 0 <= npu_utilization_pct <= 100
     * - Invariant: power_draw_mw > 0
     * - Warning: fps < 119.5 (FPS drop)
     * - Warning: cpu_temp_c > 39 && npu_utilization_pct > 90 (Near thermal limit under sustained load)
     */
    fun validateRow(row: SensorMetricRow): List<String> {
        val warnings = mutableListOf<String>()

        require(row.npuUtilizationPct in 0.0..100.0) {
            "Validation error at ${row.timestamp}: npu_utilization_pct must be between 0 and 100 (got ${row.npuUtilizationPct})"
        }
        require(row.powerDrawMw > 0.0) {
            "Validation error at ${row.timestamp}: power_draw_mw must be strictly positive (got ${row.powerDrawMw})"
        }

        if (row.fps < 119.5) {
            warnings.add("FPS drop at ${row.timestamp} (${row.fps} FPS)")
        }
        if (row.cpuTempC > 39.0 && row.npuUtilizationPct > 90.0) {
            warnings.add("Near thermal limit under sustained load at ${row.timestamp} (${row.cpuTempC}°C, ${row.npuUtilizationPct}% NPU)")
        }

        return warnings
    }

    /**
     * Parses and validates raw CSV content conforming to:
     * timestamp,cpu_temp_c,npu_utilization_pct,power_draw_mw,fps
     */
    fun parseAndValidateCsv(csvContent: String): TelemetryValidationReport {
        val lines = csvContent.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        if (lines.isEmpty()) {
            return TelemetryValidationReport(0, 0, emptyList(), listOf("Empty CSV content"))
        }

        val allWarnings = mutableListOf<String>()
        val allErrors = mutableListOf<String>()
        var validCount = 0

        // Find header index
        val headerIdx = lines.indexOfFirst { it.contains("cpu_temp_c") && it.contains("npu_utilization_pct") }
        val dataRows = if (headerIdx >= 0) lines.drop(headerIdx + 1) else lines

        for (line in dataRows) {
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 5) continue
            try {
                val row = SensorMetricRow(
                    timestamp = parts[0],
                    cpuTempC = parts[1].toDouble(),
                    npuUtilizationPct = parts[2].toDouble(),
                    powerDrawMw = parts[3].toDouble(),
                    fps = parts[4].toDouble()
                )
                val rowWarnings = validateRow(row)
                allWarnings.addAll(rowWarnings)
                validCount++
            } catch (e: IllegalArgumentException) {
                allErrors.add(e.message ?: "Validation failure on line: $line")
            } catch (e: Exception) {
                allErrors.add("Parse error on line '$line': ${e.message}")
            }
        }

        return TelemetryValidationReport(
            totalRows = dataRows.size,
            validRows = validCount,
            warnings = allWarnings,
            errors = allErrors
        )
    }

    fun recordMetrics(latencyMs: Long, tokens: Int, isCacheHit: Boolean, agent: String) {
        _state.update { current ->
            current.copy(
                totalRequests = current.totalRequests + 1,
                totalLatencyMs = current.totalLatencyMs + latencyMs,
                totalTokens = current.totalTokens + tokens,
                cacheHits = current.cacheHits + if (isCacheHit) 1 else 0,
                activeAgent = agent
            )
        }
    }

    fun updateSystemTelemetry(cpuGovernor: String, thermals: String) {
        _state.update { current ->
            current.copy(
                cpuGovernor = cpuGovernor,
                npuThermals = thermals
            )
        }
    }

    fun reset() {
        _state.value = TelemetryState()
    }
}
