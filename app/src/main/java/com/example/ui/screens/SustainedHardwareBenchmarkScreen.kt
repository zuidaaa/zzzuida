package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BenchmarkPhaseMetrics
import com.example.engine.SustainedBenchmarkResult
import com.example.engine.SustainedHardwareBenchmarkEngine
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SustainedHardwareBenchmarkScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRunning by remember { mutableStateOf(false) }
    var currentPhaseIndex by remember { mutableIntStateOf(0) }
    var currentPhaseProgress by remember { mutableFloatStateOf(0f) }
    var liveMetrics by remember { mutableStateOf<BenchmarkPhaseMetrics?>(null) }
    var benchmarkResult by remember { mutableStateOf<SustainedBenchmarkResult?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = GeminiCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "90s Sustained Hardware Benchmark",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "5-Phase Sustained Stress Test (450s Total) • Thermal, NPU & LLM Routing",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                    )
                }
            }

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        benchmarkResult = null
                        coroutineScope.launch {
                            val result = SustainedHardwareBenchmarkEngine.runSustainedBenchmark(context) { pIdx, prog, metrics ->
                                currentPhaseIndex = pIdx
                                currentPhaseProgress = prog
                                liveMetrics = metrics
                            }
                            benchmarkResult = result
                            isRunning = false
                        }
                    }
                },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("start_sustained_benchmark_btn")
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start 5x90s Test", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Progress & Telemetry Card
        val metrics = liveMetrics
        if (isRunning && metrics != null) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("live_benchmark_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Phase ${metrics.phaseNumber}/5: ${metrics.phaseName}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${metrics.durationSeconds}s Sustained",
                            color = GeminiCyan,
                            fontSize = 12.sp
                        )
                    }

                    LinearProgressIndicator(
                        progress = { currentPhaseProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = GeminiCyan,
                        trackColor = DarkSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryBadge("CPU: ${metrics.cpuFrequencyGhz} GHz")
                        TelemetryBadge("NPU: ${metrics.npuTops.toInt()} TOPS")
                        TelemetryBadge("Temp: ${metrics.temperatureCelsius}°C")
                        TelemetryBadge("State: ${metrics.thermalState}", tint = if (metrics.thermalState == "CRITICAL") WarningOrange else VerificationGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryBadge("RAM Free: ${metrics.availableRamMb} MB")
                        TelemetryBadge("Storage: ${metrics.storageWriteSpeedMbS.toInt()} MB/s")
                        TelemetryBadge("Battery: ${metrics.batteryDischargeRateMa} mA")
                    }
                }
            }
        }

        // Results Section
        val result = benchmarkResult
        if (result != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("benchmark_results_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VerificationGreen)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Benchmark Completed Successfully",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = VerificationGreen
                                )
                                Surface(
                                    color = VerificationGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Score: ${result.totalScore} / 10000",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = VerificationGreen,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Text(
                                text = "Device: ${result.deviceName}",
                                fontSize = 13.sp,
                                color = DarkTextMuted
                            )
                            Text(
                                text = "Max Temp: ${result.maxTemperatureCelsius}°C • Throttling: ${if (result.thermalThrottlingDetected) "Detected (Managed)" else "None"}",
                                fontSize = 12.sp,
                                color = if (result.thermalThrottlingDetected) WarningOrange else GeminiCyan
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Supported LLMs on Device:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                result.supportedLlms.forEach { llm ->
                                    Surface(
                                        color = GeminiBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = llm,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            color = GeminiBlueLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(result.phases) { phase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkOutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Phase ${phase.phaseNumber}: ${phase.phaseName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${phase.temperatureCelsius}°C",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (phase.temperatureCelsius > 41f) WarningOrange else GeminiCyan
                                )
                            }
                            Text(
                                text = "CPU: ${phase.cpuFrequencyGhz} GHz • NPU: ${phase.npuTops.toInt()} TOPS • GPU: ${phase.gpuLoadPercent}% • State: ${phase.thermalState}",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                            Text(
                                text = "RAM Free: ${phase.availableRamMb} MB • Storage: ${phase.storageWriteSpeedMbS.toInt()} MB/s • Battery Drain: ${phase.batteryDischargeRateMa} mA",
                                fontSize = 11.sp,
                                color = DarkTextSubtle
                            )
                        }
                    }
                }
            }
        } else if (!isRunning) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = DarkTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Press 'Start 5x90s Test' to begin hardware probing & LLM compatibility audit.",
                        color = DarkTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryBadge(text: String, tint: Color = GeminiCyan) {
    Surface(
        color = tint.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}
