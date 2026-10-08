package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.SamsungKeyboardManager
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPurple

@Composable
fun SamsungKeyboardAccessoryBar(
    onInsertSymbol: (String) -> Unit,
    onApplyTemplate: (String) -> Unit,
    onOpenVoiceConversation: () -> Unit,
    onOpenAudioTranscriber: () -> Unit,
    nonStreamingEnabled: Boolean,
    onToggleNonStreaming: (Boolean) -> Unit,
    onOpenHfMcp: () -> Unit = {},

    onOpenReviewGenerator: () -> Unit = {},
    onOpenQuickTranslator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showMathPalette by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(BorderStroke(0.8.dp, DarkOutlineVariant))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("samsung_keyboard_accessory_bar")
    ) {
        // Top Toolbar Row: Shortcuts for Voice Mode, Audio Transcribe, Math Palette, Non-Streaming Mode, Templates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Review Generator Button (ENABLE_REVIEW_GENERATION)
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenReviewGenerator()
                },
                shape = RoundedCornerShape(8.dp),
                color = GeminiEmerald.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("btn_open_review_generator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✍️",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Review Studio",
                        color = GeminiEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Translator Button (ML Kit + Gemini)
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenQuickTranslator()
                },
                shape = RoundedCornerShape(8.dp),
                color = GeminiBlue.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("btn_open_quick_translator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌐",
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Translator",
                        color = GeminiCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Voice Conversation Mode Button
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenVoiceConversation()
                },
                shape = RoundedCornerShape(8.dp),
                color = GeminiPurple.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("btn_open_voice_conversation")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Voice Conversation",
                        tint = GeminiPurple,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Voice Conv",
                        color = GeminiPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Audio Transcribe Button
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenAudioTranscriber()
                },
                shape = RoundedCornerShape(8.dp),
                color = GeminiEmerald.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("btn_open_audio_transcriber")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Transcribe Audio",
                        tint = GeminiEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Transcribe",
                        color = GeminiEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hugging Face MCP Hub Button
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenHfMcp()
                },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFD21E).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFFFD21E).copy(alpha = 0.4f)),
                modifier = Modifier.testTag("btn_open_hf_mcp")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🤗",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HF Hub MCP",
                        color = Color(0xFFFFD21E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Non-Streaming Instant Tree Mode Toggle
            FilterChip(
                selected = nonStreamingEnabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleNonStreaming(!nonStreamingEnabled)
                },
                label = {
                    Text(
                        text = if (nonStreamingEnabled) "Instant Tree (Non-Stream)" else "Streaming Mode",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (nonStreamingEnabled) Icons.Default.Speed else Icons.Default.Tune,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GeminiCyan.copy(alpha = 0.2f),
                    selectedLabelColor = GeminiCyan,
                    selectedLeadingIconColor = GeminiCyan,
                    containerColor = DarkSurfaceVariant,
                    labelColor = DarkTextMuted,
                    iconColor = DarkTextMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (nonStreamingEnabled) GeminiCyan.copy(alpha = 0.6f) else DarkOutlineVariant,
                    selectedBorderColor = GeminiCyan,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                    enabled = true,
                    selected = nonStreamingEnabled
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp).testTag("chip_non_streaming_toggle")
            )

            // Math/Logic Symbols Toggle
            AssistChip(
                onClick = {
                    showMathPalette = !showMathPalette
                    if (showMathPalette) showTemplates = false
                },
                label = { Text("Symbols", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Functions,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (showMathPalette) GeminiBlue else DarkTextMuted
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (showMathPalette) GeminiBlue.copy(alpha = 0.15f) else DarkSurfaceVariant,
                    labelColor = if (showMathPalette) GeminiBlue else DarkTextMuted
                ),
                border = BorderStroke(1.dp, if (showMathPalette) GeminiBlue else DarkOutlineVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp).testTag("chip_math_symbols_toggle")
            )

            // Cognitive Domain Templates Toggle
            AssistChip(
                onClick = {
                    showTemplates = !showTemplates
                    if (showTemplates) showMathPalette = false
                },
                label = { Text("Domains", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (showTemplates) GeminiPurple else DarkTextMuted
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (showTemplates) GeminiPurple.copy(alpha = 0.15f) else DarkSurfaceVariant,
                    labelColor = if (showTemplates) GeminiPurple else DarkTextMuted
                ),
                border = BorderStroke(1.dp, if (showTemplates) GeminiPurple else DarkOutlineVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(28.dp).testTag("chip_cognitive_domains_toggle")
            )
        }

        // Expandable Math/Logic Symbol Accessory Strip (Optimized for Samsung Keyboard & S-Pen)
        AnimatedVisibility(
            visible = showMathPalette,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SamsungKeyboardManager.MATH_AND_LOGIC_SYMBOLS.forEach { symbol ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .border(BorderStroke(0.6.dp, DarkOutline))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onInsertSymbol(symbol)
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("symbol_chip_$symbol"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = symbol,
                                color = DarkTextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Expandable Cognitive Domain Prompts (Algorithms, Math, Distributed Systems, Logic Puzzles, Philosophy)
        AnimatedVisibility(
            visible = showTemplates,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SamsungKeyboardManager.COGNITIVE_DOMAINS_TEMPLATES.forEach { template ->
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onApplyTemplate(template.prompt)
                                showTemplates = false
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceVariant,
                            border = BorderStroke(0.8.dp, DarkOutline),
                            modifier = Modifier.testTag("template_chip_${template.badge}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GeminiPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = template.badge,
                                        color = GeminiPurple,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = template.title,
                                    color = DarkTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
