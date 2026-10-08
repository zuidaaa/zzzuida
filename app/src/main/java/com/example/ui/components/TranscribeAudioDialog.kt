package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTranscriptionManager
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.VerificationGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscribeAudioSheet(
    transcriptionManager: AudioTranscriptionManager,
    onSendTranscriptToEngine: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by transcriptionManager.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val clipboard = LocalClipboardManager.current

    var showLanguageMenu by remember { mutableStateOf(false) }
    var copiedState by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            transcriptionManager.stopLiveRecording()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null,
        modifier = modifier.testTag("transcribe_audio_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeminiEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = GeminiEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Audio & Speech Transcriber",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DarkTextPrimary
                        )
                        Text(
                            text = "On-Device Neural Acoustic Recognition",
                            fontSize = 11.sp,
                            color = DarkTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = {
                        transcriptionManager.stopLiveRecording()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_close_transcribe_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language Selector & Preset Audios Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Dropdown Selector
                Box {
                    Surface(
                        onClick = { showLanguageMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant,
                        border = BorderStroke(0.8.dp, DarkOutlineVariant),
                        modifier = Modifier.testTag("btn_select_transcribe_language")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = GeminiBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.selectedLanguage,
                                color = DarkTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        transcriptionManager.supportedLanguages.forEach { (name, code) ->
                            DropdownMenuItem(
                                text = { Text(name, color = DarkTextPrimary, fontSize = 12.sp) },
                                onClick = {
                                    transcriptionManager.setLanguage(name, code)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }

                // Sample Audio Buttons for Quick Testing
                listOf("Algorithms", "Mathematics", "Distributed Systems").forEach { sampleName ->
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            transcriptionManager.loadSampleAudioTranscript(sampleName)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = GeminiPurple.copy(alpha = 0.12f),
                        border = BorderStroke(0.8.dp, GeminiPurple.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("btn_sample_transcript_$sampleName")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sample: $sampleName",
                                color = GeminiPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Waveform Audio Equalizer Visualizer (32-Band dynamic amplitude canvas)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (state.isRecording) GeminiEmerald.copy(alpha = 0.5f) else DarkOutlineVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isRecording) {
                        LiveWaveformCanvas(
                            amplitudes = state.liveAmplitudes,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = DarkTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.segments.isNotEmpty()) "${state.segments.size} Audio Segments Captured (${state.wordCount} words)" else "Ready to Record or Load Audio",
                                color = DarkTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transcript Body Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.segments.isNotEmpty()) {
                        state.segments.forEach { segment ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = segment.speaker,
                                        color = GeminiCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Conf: ${(segment.confidence * 100).toInt()}%",
                                        color = VerificationGreen,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = segment.text,
                                    color = DarkTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    } else if (state.partialTranscriptText.isNotBlank()) {
                        Text(
                            text = state.partialTranscriptText,
                            color = DarkTextPrimary,
                            fontSize = 12.sp
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Live speech transcription will appear here in real-time.\nSupports multilingual dictation & multi-speaker separation.",
                                color = DarkTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Error notice if any
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = GeminiPink,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Toolbar: Copy, Clear, Record, Send to Engine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button
                OutlinedButton(
                    onClick = {
                        val textToCopy = if (state.fullTranscriptText.isNotBlank()) state.fullTranscriptText else state.partialTranscriptText
                        if (textToCopy.isNotBlank()) {
                            clipboard.setText(AnnotatedString(textToCopy))
                            copiedState = true
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, DarkOutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextPrimary),
                    modifier = Modifier.testTag("btn_copy_transcript")
                ) {
                    Icon(
                        imageVector = if (copiedState) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (copiedState) "Copied" else "Copy", fontSize = 11.sp)
                }

                // Clear Button
                OutlinedButton(
                    onClick = {
                        transcriptionManager.clearTranscript()
                        copiedState = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, DarkOutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextMuted),
                    modifier = Modifier.testTag("btn_clear_transcript")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Record / Stop Live Dictation Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (state.isRecording) {
                            transcriptionManager.stopLiveRecording()
                        } else {
                            transcriptionManager.startLiveRecording()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) GeminiPink else GeminiEmerald,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).testTag("btn_record_transcribe_audio")
                ) {
                    Icon(
                        imageVector = if (state.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isRecording) "Stop (${state.recordingDurationSec}s)" else "Record Audio",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Transfer to Cognitive Engine Button
            val hasContent = state.fullTranscriptText.isNotBlank() || state.partialTranscriptText.isNotBlank()
            if (hasContent) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val text = if (state.fullTranscriptText.isNotBlank()) state.fullTranscriptText else state.partialTranscriptText
                        transcriptionManager.stopLiveRecording()
                        onSendTranscriptToEngine(text)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_send_transcript_to_engine"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeminiBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deconstruct in Reasoning Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveWaveformCanvas(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barCount = amplitudes.size
        val totalWidth = size.width
        val barSpacing = 3.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = (totalWidth - totalSpacing) / barCount
        val maxHeight = size.height

        amplitudes.forEachIndexed { index, amp ->
            val barHeight = (amp * maxHeight).coerceIn(4.dp.toPx(), maxHeight)
            val left = index * (barWidth + barSpacing)
            val top = (maxHeight - barHeight) / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(GeminiCyan, GeminiEmerald)
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
