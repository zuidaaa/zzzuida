package com.example.ui.screens

import androidx.compose.animation.core.*
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
import com.example.engine.SustainedHardwareBenchmarkEngine
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*
import com.example.ui.components.D3PerformanceChart
import kotlinx.coroutines.launch

@Composable
fun BenchmarkTelemetryDashboardScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRunning by remember { mutableStateOf(false) }
    var currentStage by remember { mutableIntStateOf(1) }
    var stageProgress by remember { mutableFloatStateOf(0f) }
    val telemetryState by com.example.engine.TelemetryEngine.state.collectAsState()
    var metrics by remember { mutableStateOf<com.example.engine.BenchmarkPhaseMetrics?>(null) }
    val telemetryHistory = remember { mutableStateListOf<com.example.engine.BenchmarkPhaseMetrics>() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = GeminiPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Real-Time Sensor & Telemetry Dashboard",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Sustained 5-Stage Benchmark • Thermal Probing & Core Load Monitoring",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            telemetryHistory.clear()
                            coroutineScope.launch {
                                SustainedHardwareBenchmarkEngine.runSustainedBenchmark(context) { pIdx, prog, m ->
                                    currentStage = pIdx + 1
                                    stageProgress = prog
                                    metrics = m
                                    telemetryHistory.add(m)
                                }
                                isRunning = false
                            }
                        }
                    },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_telemetry_dashboard_btn")
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Live Audit", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            repeat(5) {
                                val latency = (500..2500).random().toLong()
                                val tokens = (100..800).random()
                                com.example.engine.TelemetryEngine.recordMetrics(
                                    latencyMs = latency,
                                    tokens = tokens,
                                    isCacheHit = (0..10).random() > 8,
                                    agent = listOf("DEEP_RESEARCH", "CODE_ARCHITECT", "GENERAL").random()
                                )
                                kotlinx.coroutines.delay(500)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = GeminiPurple)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sim Load", fontSize = 10.sp, color = GeminiPurple)
                }
            }
        }

        // Active Load Stage (Ladestufe) Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("active_stage_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aktuelle Ladestufe: $currentStage / 5",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Surface(
                            color = if (isRunning) GeminiCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isRunning) "LAUFEND (90s Stage)" else "BEREIT",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) GeminiCyan else DarkTextMuted
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { stageProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = GeminiCyan,
                        trackColor = DarkSurfaceVariant
                    )

                    if (isRunning) {
                        Text(
                            text = "Benchmark in progress...",
                            fontSize = 13.sp,
                            color = DarkTextMuted
                        )
                    } else {
                        Text(
                            text = "Wählen Sie 'Start Live Audit' um die 5-stufige Sensor-Überwachung zu starten.",
                            fontSize = 13.sp,
                            color = DarkTextMuted
                        )
                    }
                }
            }
        }

        // LLM Performance Telemetry (Live Chart)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LLM Performance Telemetry (D3.js Live)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                
                D3PerformanceChart(
                    points = telemetryState.performanceHistory,
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Avg Latency: ${if (telemetryState.totalRequests > 0) telemetryState.totalLatencyMs / telemetryState.totalRequests else 0}ms", fontSize = 11.sp, color = GeminiCyan)
                    Text("Cache Hits: ${telemetryState.cacheHits}", fontSize = 11.sp, color = GeminiEmerald)
                    Text("Active Agent: ${telemetryState.activeAgent}", fontSize = 11.sp, color = GeminiPurple)
                }
            }
        }

        // Real-Time Sensor Metrics Grid
        item {
            val activeMetrics = metrics
            if (activeMetrics != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Hardware Sensor Metriken",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SensorMetricCard(
                            title = "Temperatur",
                            value = "${activeMetrics.temperatureCelsius}°C",
                            subtitle = "State: ${activeMetrics.thermalState}",
                            color = if (activeMetrics.temperatureCelsius > 41f) WarningOrange else GeminiPink,
                            modifier = Modifier.weight(1f)
                        )
                        SensorMetricCard(
                            title = "CPU Auslastung",
                            value = "${activeMetrics.cpuFrequencyGhz} GHz",
                            subtitle = "ARM Cortex SVE2",
                            color = GeminiCyan,
                            modifier = Modifier.weight(1f)
                        )
                        SensorMetricCard(
                            title = "GPU Last",
                            value = "${activeMetrics.gpuLoadPercent}%",
                            subtitle = "Mali / Xclipse",
                            color = GeminiBlueLight,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SensorMetricCard(
                            title = "NPU Durchsatz",
                            value = "${activeMetrics.npuTops.toInt()} TOPS",
                            subtitle = "Samsung NPU Turbo",
                            color = VerificationGreen,
                            modifier = Modifier.weight(1f)
                        )
                        SensorMetricCard(
                            title = "Verfügbarer RAM",
                            value = "${activeMetrics.availableRamMb} MB",
                            subtitle = "LPDDR5X Heap",
                            color = GeminiPurple,
                            modifier = Modifier.weight(1f)
                        )
                        SensorMetricCard(
                            title = "Speicher I/O",
                            value = "${activeMetrics.storageWriteSpeedMbS.toInt()} MB/s",
                            subtitle = "UFS 4.0 SQLite",
                            color = WarningOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = DarkTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Keine Echtzeit-Hardware-Metriken aktiv.",
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Telemetry History Log
        if (telemetryHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Protokollierte Stufen-Historie",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
            items(telemetryHistory) { hist ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stufe ${hist.phaseNumber}: ${hist.phaseName}", fontSize = 11.sp, color = Color.White)
                        Text("${hist.temperatureCelsius}°C • ${hist.cpuFrequencyGhz}GHz • ${hist.thermalState}", fontSize = 11.sp, color = GeminiCyan)
                    }
                }
            }
        }
    }
}

@Composable
fun SensorMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontSize = 11.sp, color = DarkTextMuted)
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 10.sp, color = DarkTextSubtle)
        }
    }
}
