package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AutonomousPipeline
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueGlow
import com.example.ui.theme.GeminiBlueLight
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.WarningOrange

@Composable
fun PipelinesAndAdminScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var activeScreenTab by remember { mutableStateOf(0) }
    
    // Sandbox Form states
    var newSandboxName by remember { mutableStateOf("Simulated DevOps Sandbox") }
    var newSystemInstructions by remember { mutableStateOf("You are an expert sandbox compilation agent.") }
    var newDomains by remember { mutableStateOf("pypi.org, docker.com, alpine.org") }
    var newPersistState by remember { mutableStateOf(true) }
    var newRegistry by remember { mutableStateOf("Python pypi secure local proxy") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header
            Column {
                Text(
                    text = "Autonomous Pipelines & One UI 9 Tuning",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                )
                Text(
                    text = "Zero-token autonomous task outsourcing & European Samsung S26 Ultra Exynos / Cortex-X CPU administration",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeScreenTab == 0) GeminiBlue else Color.Transparent)
                        .clickable { activeScreenTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pipelines & Tuning",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (activeScreenTab == 0) Color.White else DarkTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
                // Tab 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeScreenTab == 1) GeminiBlue else Color.Transparent)
                        .clickable { activeScreenTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Async Tasks",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (activeScreenTab == 1) Color.White else DarkTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
                // Tab 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (activeScreenTab == 2) GeminiBlue else Color.Transparent)
                        .clickable { activeScreenTab = 2 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sources & Benchmarks",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (activeScreenTab == 2) Color.White else DarkTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        if (activeScreenTab == 0) {
            // European Samsung Galaxy S26 Ultra (Exynos 2600 / ARMv9.2-A) Admin Suite Card
            item {
            val profile = uiState.cpuHardwareProfile
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = GeminiEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "One UI 9.0 & Android 17 Suite",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                )
                                Text(
                                    text = "Samsung S26 Ultra • Exynos ${profile.oneUiVersion} Kernel Optimizer",
                                    style = MaterialTheme.typography.bodySmall.copy(color = GeminiEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text("A17 / v17", style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text("ONE UI 9.0", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CPU Cluster Allocation Map
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CPU CLUSTER MAPPING (DECA-CORE)",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Prime Core
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiBlue.copy(alpha = 0.3f))
                                    .border(1.dp, GeminiBlueLight, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("1x Prime", style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 9.5.sp))
                                    Text("Cortex-X", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 8.5.sp))
                                }
                            }
                            // Perf Cores
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiCyan.copy(alpha = 0.25f))
                                    .border(1.dp, GeminiCyan, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("3x Perf", style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontWeight = FontWeight.Bold, fontSize = 9.5.sp))
                                    Text("Cortex-A", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 8.5.sp))
                                }
                            }
                            // Mid Cores
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiPurple.copy(alpha = 0.25f))
                                    .border(1.dp, GeminiPurple, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("2x Mid", style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontWeight = FontWeight.Bold, fontSize = 9.5.sp))
                                    Text("Validation", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 8.5.sp))
                                }
                            }
                            // Efficiency Cores
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiEmerald.copy(alpha = 0.2f))
                                    .border(1.dp, GeminiEmerald, RoundedCornerShape(6.dp))
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("4x E-Core", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold, fontSize = 9.5.sp))
                                    Text("SQLite / IO", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 8.5.sp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Setting 1: SVE2 Vector Math Engine
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SVE2 256-Bit Vector SIMD Acceleration",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Uses ARMv9.2 Scalable Vector Extensions for fast on-device heuristic matrix tensor evaluation.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = uiState.sve2VectorMathEnabled,
                            onCheckedChange = { viewModel.toggleSve2VectorMath() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Setting 2: Exynos NPU Turbo (92 TOPS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Samsung Exynos Dual-NPU Delegate (92 TOPS)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Delegates quantized reasoning token evaluation to Samsung Neural Engine hardware blocks.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = uiState.exynosNpuDelegateEnabled && uiState.s26UltraNpuTurbo,
                            onCheckedChange = {
                                viewModel.toggleExynosNpuDelegate()
                                viewModel.toggleS26UltraNpuTurbo()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiEmerald
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Setting 3: One UI 9 120Hz Animation Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "One UI 9 Dynamic 120Hz Fluidity",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Locks thought stream rendering to European Galaxy 120Hz LTPO refresh rate with smooth haptics.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = uiState.oneUi120HzSync,
                            onCheckedChange = { viewModel.toggleOneUi120HzSync() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Setting 4: Knox Private Sandbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Knox Hardware-Isolated Sandbox",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Guarantees zero outbound data leakage for enterprise reasoning and proprietary code.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = uiState.knoxPrivateSandbox,
                            onCheckedChange = { viewModel.toggleKnoxSandbox() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Setting 5: Google UI9 Organic Theme Engine
                    var googleUi9ThemeActive by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Google UI9 Organic Accent Styling",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Applies soft pastel accent highlights and rounded fluid shapes tailored for Google UI 9.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = googleUi9ThemeActive,
                            onCheckedChange = { googleUi9ThemeActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiBlueLight
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Setting 6: Android 17 Dynamic Material You Colors
                    var android17DynamicColorsActive by remember { mutableStateOf(true) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Android 17 Dynamic Theme Engine",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Text(
                                text = "Extracts dynamic wallpaper accents natively using Android 17 SDK 37 core APIs.",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = android17DynamicColorsActive,
                            onCheckedChange = { android17DynamicColorsActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GeminiCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CPU Governor Mode Switcher
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "CPU GOVERNOR / THERMAL PROFILE",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Performance AI Turbo", "Balanced Dynamic", "Efficiency Saver").forEach { mode ->
                                val isSelected = mode == uiState.cpuGovernorMode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GeminiEmerald.copy(alpha = 0.25f) else DarkSurface)
                                        .border(1.dp, if (isSelected) GeminiEmerald else DarkOutlineVariant, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setCpuGovernorMode(mode) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else DarkTextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 9.5.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-Time CPU Benchmark Trigger & Results
                    Button(
                        onClick = { viewModel.runCpuBenchmark() },
                        enabled = !uiState.isBenchmarkingCpu,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isBenchmarkingCpu) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Benchmarking S26 Ultra CPU...", style = MaterialTheme.typography.labelMedium.copy(color = Color.White))
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run S26 Ultra Exynos / ARMv9 CPU Benchmark", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                        }
                    }

                    uiState.cpuBenchmarkResult?.let { bench ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, GeminiEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SVE2 SIMD Score: ${bench.sve2VectorScore} pts", style = MaterialTheme.typography.labelMedium.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold))
                                Text("${bench.multiCoreThroughputTps} t/s throughput", style = MaterialTheme.typography.labelMedium.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Matrix Latency: ${bench.matrixMulLatencyMs}ms • NPU Latency: ${bench.npuInferenceLatencyMs}ms",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        // Hugging Face MCP Server Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD21E).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFD21E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🤗", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hugging Face MCP Server",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                )
                                Text(
                                    text = "https://endpoints.huggingface.co/mcp",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFD21E), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (uiState.hfMcpStatus.isConnected) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (uiState.hfMcpStatus.isConnected) "ONLINE" else "CONNECTING",
                                color = if (uiState.hfMcpStatus.isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Connects on-device reasoning assistants to the entire Hugging Face Hub (Models, Datasets, Spaces, Papers, and Serverless Inference).",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openHfMcpConnector() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD21E),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Open MCP Hub", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.pingHfMcpServer() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurfaceVariant,
                                contentColor = DarkTextPrimary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ping", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Outsource & Autonomous Pipelines Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTONOMOUS WORKFLOW PIPELINES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextSubtle,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeminiCyan.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ZERO-TOKEN OUTSOURCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GeminiCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Pipelines List
        items(viewModel.predefinedPipelines, key = { it.id }) { pipeline ->
            val isRunning = pipeline.id == uiState.activeRunningPipelineId
            PipelineCard(
                pipeline = pipeline,
                isRunning = isRunning,
                stepProgress = if (isRunning) uiState.pipelineStepProgress else 0,
                onTrigger = { viewModel.runAutonomousPipeline(pipeline) }
            )
        }

        // Live Pipeline Execution Log if active
        if (uiState.pipelineLog.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Autonomous Pipeline Telemetry Log",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                            )
                            if (uiState.activeRunningPipelineId != null) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = GeminiBlueLight, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        uiState.pipelineLog.takeLast(6).forEach { log ->
                            Text(
                                text = "› $log",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    } else if (activeScreenTab == 1) {
        // ==================== TAB 1: ASYNC TASKS & ENVIRONMENT SANDBOXES ====================
            
            // 1. Task Builder Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Asynchronous Agent Dispatcher",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Submit a multi-step execution query. With background=True, the system dispatches the job and hands back an Interaction ID instantly for status polling.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 12.sp)
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = "Agent prompt / Multi-step task",
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = uiState.newTaskPrompt,
                            onValueChange = { viewModel.onNewTaskPromptChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = GeminiBlue,
                                unfocusedBorderColor = DarkOutlineVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Select Sandbox Environment to reuse",
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Select Environment Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.sandboxes.forEach { sandbox ->
                                val isSelected = uiState.selectedSandboxId == sandbox.environmentId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GeminiEmerald.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                        .border(1.2.dp, if (isSelected) GeminiEmerald else DarkOutlineVariant, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.onSelectedSandboxIdChange(sandbox.environmentId) }
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = sandbox.sandboxName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = if (isSelected) GeminiEmerald else Color.White,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = sandbox.environmentId,
                                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // API Compliance Status indicators
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "API Header: background=true",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Reuses sandbox workspace, preserving local files, sqlite db state, and network proxy rules.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.5.sp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Button(
                            onClick = { viewModel.dispatchAgentBackgroundTask() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dispatch Asynchronous Task", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Polling and Task Queue Card
            if (uiState.backgroundTasks.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Layers, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Asynchronous Task Queue",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                    )
                                }
                                
                                Text(
                                    text = "Clear All",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFF5252), fontWeight = FontWeight.Bold),
                                    modifier = Modifier.clickable { viewModel.clearCompletedTasks() }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            uiState.backgroundTasks.forEach { task ->
                                val isSelected = uiState.selectedTaskId == task.interactionId
                                val sandbox = uiState.sandboxes.firstOrNull { it.environmentId == task.environmentId } ?: com.example.data.EnvironmentConfig()
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) DarkSurfaceElevated else Color.Transparent)
                                        .border(1.dp, if (isSelected) GeminiEmerald.copy(alpha = 0.5f) else DarkOutlineVariant, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.onSelectedTaskIdChange(task.interactionId) }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "ID: ${task.interactionId}",
                                                    style = MaterialTheme.typography.labelMedium.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "Sandbox: ${sandbox.sandboxName}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                                                )
                                            }
                                            
                                            // Status Badge
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        when (task.status) {
                                                            com.example.data.TaskStatus.PENDING -> Color.Gray.copy(alpha = 0.2f)
                                                            com.example.data.TaskStatus.RUNNING -> GeminiBlue.copy(alpha = 0.2f)
                                                            com.example.data.TaskStatus.COMPLETED -> GeminiEmerald.copy(alpha = 0.2f)
                                                            com.example.data.TaskStatus.FAILED -> Color(0xFFFF5252).copy(alpha = 0.2f)
                                                        }
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = task.status.name,
                                                    color = when (task.status) {
                                                        com.example.data.TaskStatus.PENDING -> Color.LightGray
                                                        com.example.data.TaskStatus.RUNNING -> GeminiBlueLight
                                                        com.example.data.TaskStatus.COMPLETED -> GeminiEmerald
                                                        com.example.data.TaskStatus.FAILED -> Color(0xFFFF5252)
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Query: \"${task.prompt}\"",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                            maxLines = 1
                                        )
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Progress Bar
                                        LinearProgressIndicator(
                                            progress = { task.progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = if (task.status == com.example.data.TaskStatus.COMPLETED) GeminiEmerald else GeminiBlue,
                                            trackColor = DarkSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            // Selected Task Detailed Terminal & Report
                            uiState.selectedTaskId?.let { selId ->
                                val selectedTask = uiState.backgroundTasks.firstOrNull { it.interactionId == selId }
                                if (selectedTask != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = DarkOutlineVariant, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        text = "Interactive Status Logs (Polled ID: $selId)",
                                        style = MaterialTheme.typography.labelMedium.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Terminal
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                            .border(1.dp, GeminiEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                            items(selectedTask.logs) { log ->
                                                Text(
                                                    text = "› $log",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color(0xFF00FF66),
                                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                            }
                                        }
                                    }
                                    
                                    if (selectedTask.status == com.example.data.TaskStatus.COMPLETED) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Final Output Report",
                                            style = MaterialTheme.typography.labelMedium.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkSurfaceElevated)
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = selectedTask.resultOutput,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color.White,
                                                    lineHeight = 16.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Sandbox Config Manager Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Reuse Existing Environment Config",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Preserve workspace state, modified SQLite records, and whitelisted outbound domain restrictions to support multi-agent sessions without cold-start times.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 12.sp)
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DarkOutlineVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Configure New Virtual Environment",
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontWeight = FontWeight.Bold)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Sandbox Config Fields
                        Text("Sandbox Name", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle))
                        OutlinedTextField(
                            value = newSandboxName,
                            onValueChange = { newSandboxName = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = GeminiPurple,
                                unfocusedBorderColor = DarkOutlineVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Allowed Outbound Domains (IP Whitelist)", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle))
                        OutlinedTextField(
                            value = newDomains,
                            onValueChange = { newDomains = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = GeminiPurple,
                                unfocusedBorderColor = DarkOutlineVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Simulated Package Registry Proxy", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle))
                        OutlinedTextField(
                            value = newRegistry,
                            onValueChange = { newRegistry = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = GeminiPurple,
                                unfocusedBorderColor = DarkOutlineVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = newPersistState,
                                onCheckedChange = { newPersistState = it },
                                colors = CheckboxDefaults.colors(checkedColor = GeminiPurple)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Persist files and database across interactions",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                if (newSandboxName.isNotBlank()) {
                                    viewModel.addNewCustomSandbox(
                                        newSandboxName,
                                        newSystemInstructions,
                                        newDomains,
                                        newPersistState,
                                        newRegistry
                                    )
                                    // reset sandbox input
                                    newSandboxName = "Dev Sandbox ${java.util.UUID.randomUUID().toString().substring(0, 4)}"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Provision Virtual Sandbox Environment", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // ==================== TAB 2: SOURCES & BENCHMARKS ====================
            sourcesAndBenchmarksSection(viewModel, uiState)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PipelineCard(
    pipeline: AutonomousPipeline,
    isRunning: Boolean,
    stepProgress: Int,
    onTrigger: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pipeline_card_${pipeline.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRunning) GeminiBlue else DarkOutlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pipeline.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                    )
                    Text(
                        text = "${pipeline.category} • Target: ${pipeline.targetPlatform}",
                        style = MaterialTheme.typography.bodySmall.copy(color = GeminiCyan, fontSize = 11.sp)
                    )
                }

                if (pipeline.isZeroToken) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "0 TOKENS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = pipeline.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stages list
            pipeline.stages.forEach { stage ->
                val isStageCompleted = stepProgress > stage.stageNumber
                val isStageActive = isRunning && stepProgress == stage.stageNumber

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isStageCompleted -> GeminiEmerald
                                    isStageActive -> GeminiBlue
                                    else -> DarkSurfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isStageCompleted) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "${stage.stageNumber}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isStageActive) Color.White else DarkTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${stage.title} (${stage.assignedAgent})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isStageActive) GeminiBlueLight else DarkTextMuted,
                            fontWeight = if (isStageActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isRunning) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { stepProgress.toFloat() / pipeline.stages.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GeminiBlue,
                    trackColor = DarkSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trigger Button
            Button(
                onClick = onTrigger,
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_pipeline_${pipeline.id}")
            ) {
                if (isRunning) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Executing Autonomous Pipeline...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Autonomous Pipeline", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
