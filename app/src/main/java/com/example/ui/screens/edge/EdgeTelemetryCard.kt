package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.EdgeTelemetryState
import com.example.ui.theme.*

/**
 * UI Fragment: Hardware-Aware Telemetry Dashboard Card (Phase 2).
 * Displays real-time Exynos Cortex-X load, thermal status, NPU headroom,
 * and adaptive routing target recommendations.
 */
@Composable
fun EdgeTelemetryCard(
    telemetry: EdgeTelemetryState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Telemetry Icon",
                        tint = VibrantTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Phase 2: Adaptive Hardware Telemetry",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                    )
                }
                Text(
                    text = telemetry.thermalStatus,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (telemetry.thermalStatus == "NOMINAL") GeminiEmerald else WarningOrange,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("CPU / NPU LOAD", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                        Text("${telemetry.cpuUtilizationPercent.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = VibrantTeal))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("BATTERY TEMP", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                        Text("${telemetry.batteryTemperatureCelsius}°C", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("NPU HEADROOM", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                        Text("${telemetry.npuThermalHeadroom.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Routing: ${telemetry.recommendedTarget.displayName}",
                    style = MaterialTheme.typography.bodySmall.copy(color = GeminiCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Complexity: ${(telemetry.complexityScore * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(color = VibrantTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
