package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetCatalog
import com.example.data.ReasoningPreset
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.ui.components.*

@Composable
fun ReasoningSandboxesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeminiBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Deep Thinking Reasoning Sandboxes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pre-calibrated cognitive domains configured with multi-branch hypothesis exploration and rigorous verification steps.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(PresetCatalog.presets, key = { it.id }) { preset ->
                SandboxCard(
                    preset = preset,
                    onLaunch = { viewModel.applyPreset(preset) }
                )
            }
        }
    }
}

@Composable
fun SandboxCard(
    preset: ReasoningPreset,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
            .testTag("sandbox_card_${preset.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = preset.icon,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = preset.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Domain Tag Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeminiBlue.copy(alpha = 0.15f))
                        .border(0.8.dp, GeminiBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = preset.domainTag.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiBlueLight,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample Reasoning Prompt Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "BENCHMARK PROMPT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiBlueLight,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${preset.promptTemplate}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E2E6),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer with recommended budget & launch button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rec. Budget: ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextMuted,
                            fontSize = 11.5.sp
                        )
                    )
                    Text(
                        text = "${preset.recommendedLevel.icon} ${preset.recommendedLevel.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiBlueLight,
                            fontSize = 11.5.sp
                        )
                    )
                }

                Button(
                    onClick = onLaunch,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("launch_preset_button_${preset.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Launch Sandbox",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

