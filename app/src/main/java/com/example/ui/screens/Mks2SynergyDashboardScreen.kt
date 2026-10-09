package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.mks2.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun Mks2SynergyDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val mks2State by viewModel.mks2SynergyEngine.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("mks2_synergy_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = GeminiBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = GeminiBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = mks2State.projectName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${mks2State.targetHardware} • v${mks2State.version}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Button(
                onClick = { viewModel.mks2SynergyEngine.executeCycle() },
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("mks2_execute_cycle_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run Cycle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Summary Metrics Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mks2_summary_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "TTFT Reduction", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                Text(text = mks2State.totalTtftReduction, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = VerificationGreen))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Context Extension", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                Text(text = mks2State.contextExtensionFactor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeminiBlue))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Energy Savings", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                Text(text = mks2State.energySavings, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminousYellow))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Security Tier", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                Text(text = mks2State.securityStatus, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = VibrantPurple))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Active Tier: ${mks2State.activeTier}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VerificationGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "System Score: ${mks2State.systemHealthScore}%",
                                    style = MaterialTheme.typography.labelSmall.copy(color = VerificationGreen, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Core Synergy Engines Architecture
            item {
                Text(
                    text = "Core Synergy Engines (mzCache + KVSwap + NPUGen)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EngineInfoCard(
                        title = "Engine A: mzCache",
                        role = "Elastische Speicherverwaltung unter Multitasking-Druck",
                        impact = "2,1–5,5× Reduktion der Time-to-First-Token via Backward-Out Eviction",
                        color = GeminiBlue
                    )
                    EngineInfoCard(
                        title = "Engine B: KVSwap",
                        role = "Disk-backed KV-Cache mit predictive Prefetching auf UFS 4.0",
                        impact = "Ermöglicht Long-Context-Inferenz (>32K Token) auf Geräten mit <8 GB RAM",
                        color = VibrantPurple
                    )
                    EngineInfoCard(
                        title = "Engine C: NPUGen",
                        role = "NPU-Rekonstruktion fehlender KV-Caches statt Flash-Offloading",
                        impact = "Vermeidet UFS-I/O-Asymmetrie und senkt den Energieverbrauch massiv",
                        color = VerificationGreen
                    )
                    EngineInfoCard(
                        title = "Agent.xpu & FlexServe TEE",
                        role = "Heterogene iGPU/NPU-Koordination & TrustZone Sicherheitsisolation",
                        impact = "1,2–4,9× proaktiver Durchsatz & 10,05× TTFT-Speedup via FlexServe",
                        color = LuminousYellow
                    )
                }
            }

            // Live Closed-Loop Telemetry & Log Trace
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Live Closed-Loop Telemetry & Log Trace",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        if (mks2State.logTrace.isEmpty()) {
                            Text(text = "Initializing MKS² telemetry logs...", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        } else {
                            mks2State.logTrace.forEach { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineInfoCard(title: String, role: String, impact: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = role, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "➜ $impact", style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Medium, fontSize = 10.sp))
        }
    }
}
