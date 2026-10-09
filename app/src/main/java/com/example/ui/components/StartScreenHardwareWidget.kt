package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.HardwareMetricsState
import com.example.ui.theme.*

@Composable
fun StartScreenHardwareWidget(metrics: HardwareMetricsState, modifier: Modifier = Modifier) {
    val thermalColor = when (metrics.thermalState) {
        "NOMINAL" -> VibrantGreen
        "LIGHT" -> Color(0xFFFFC107)
        "MODERATE" -> Color(0xFFFF9800)
        else -> Color(0xFFFF5252)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Brush.horizontalGradient(listOf(GeminiBlue.copy(alpha = 0.5f), VibrantPurple.copy(alpha = 0.5f))), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(thermalColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START SCREEN HARDWARE & LLM WIDGET",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = thermalColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "THERMAL: ${metrics.thermalState}",
                        color = thermalColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WidgetMetricItem(
                    icon = Icons.Default.Thermostat,
                    label = "Temp",
                    value = String.format("%.1f°C", metrics.temperatureCelsius),
                    tint = GeminiBlue
                )
                WidgetMetricItem(
                    icon = Icons.Default.BatteryChargingFull,
                    label = "Battery",
                    value = "${metrics.batteryPercent}%",
                    tint = VibrantGreen
                )
                WidgetMetricItem(
                    icon = Icons.Default.Memory,
                    label = "CPU Load",
                    value = "${metrics.cpuUsagePercent}%",
                    tint = VibrantPurple
                )
                WidgetMetricItem(
                    icon = Icons.Default.Storage,
                    label = "RAM Free",
                    value = "${metrics.availableRamMb}MB",
                    tint = GeminiBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            androidx.compose.material3.HorizontalDivider(color = DarkOutlineVariant, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = GeminiBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Active LLM: ${metrics.activeLlmName}", color = TextSecondary, fontSize = 11.sp)
                }
                Text(text = "⚡ ${String.format("%.1f", metrics.tokenThroughputSec)} t/s", color = VibrantGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WidgetMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
            .width(72.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
