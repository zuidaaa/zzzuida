package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.ModelQuantization
import com.example.ui.theme.*

/**
 * UI Fragment: Edge Model Quantization Selector Card (Phase 0).
 * Displays INT4, INT3, INT2 options with Exynos throughput t/s.
 */
@Composable
fun EdgeQuantizationCard(
    selectedQuantization: ModelQuantization,
    onSelectQuantization: (ModelQuantization) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Phase 0: Quantization & Mobile Runtime (llama.cpp)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(ModelQuantization.INT4, ModelQuantization.INT3, ModelQuantization.INT2).forEach { quant ->
                    val isSelected = selectedQuantization == quant
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VibrantTeal.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (isSelected) VibrantTeal else DarkOutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { onSelectQuantization(quant) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = quant.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) VibrantTeal else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "${quant.throughputTpsExynos2600} t/s",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
