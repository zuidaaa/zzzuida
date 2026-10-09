package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.EdgePipelineEvent
import com.example.ui.theme.*

/**
 * UI Fragment: Pipeline Execution Timeline & Event Stream (Phases 0, 1, 2, 3).
 */
@Composable
fun EdgePipelineEventCard(
    event: EdgePipelineEvent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(
            1.dp,
            when {
                event.isFailed -> Color(0xFFFF5252).copy(alpha = 0.5f)
                event.isCompleted -> GeminiEmerald.copy(alpha = 0.4f)
                else -> VibrantTeal.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when (event.phase) {
                                    0 -> GeminiBlue
                                    1 -> VibrantPurple
                                    2 -> WarningOrange
                                    else -> VibrantTeal
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P${event.phase}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.stepTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                if (event.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = GeminiEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (event.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = VibrantTeal,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.detail,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    fontSize = 11.5.sp
                )
            )

            if (event.payloadPreview.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(6.dp)
                ) {
                    Text(
                        text = event.payloadPreview,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = GeminiCyan,
                            fontSize = 10.5.sp
                        ),
                        maxLines = 4
                    )
                }
            }
        }
    }
}
