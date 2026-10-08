package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceConversationManager
import com.example.audio.VoiceConversationState
import com.example.ui.theme.*
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConversationSheet(
    voiceManager: VoiceConversationManager,
    onDismiss: () -> Unit,
    onOpenCotDebugger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voiceState by voiceManager.voiceState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = {
            voiceManager.stopListening()
            voiceManager.stopSpeaking()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null,
        modifier = modifier.testTag("voice_conversation_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (voiceState.conversationState) {
                                    VoiceConversationState.LISTENING -> GeminiCyan
                                    VoiceConversationState.THINKING -> GeminiPurple
                                    VoiceConversationState.SPEAKING -> GeminiEmerald
                                    VoiceConversationState.IDLE -> DarkTextMuted
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% On-Device Voice Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DarkTextPrimary
                    )
                }

                IconButton(
                    onClick = {
                        voiceManager.stopListening()
                        voiceManager.stopSpeaking()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_close_voice_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = when (voiceState.conversationState) {
                    VoiceConversationState.LISTENING -> GeminiCyan.copy(alpha = 0.15f)
                    VoiceConversationState.THINKING -> GeminiPurple.copy(alpha = 0.15f)
                    VoiceConversationState.SPEAKING -> GeminiEmerald.copy(alpha = 0.15f)
                    VoiceConversationState.IDLE -> DarkSurfaceVariant
                },
                border = BorderStroke(
                    1.dp,
                    when (voiceState.conversationState) {
                        VoiceConversationState.LISTENING -> GeminiCyan
                        VoiceConversationState.THINKING -> GeminiPurple
                        VoiceConversationState.SPEAKING -> GeminiEmerald
                        VoiceConversationState.IDLE -> DarkOutlineVariant
                    }
                )
            ) {
                Text(
                    text = when (voiceState.conversationState) {
                        VoiceConversationState.LISTENING -> "● Listening to Voice Query..."
                        VoiceConversationState.THINKING -> "✦ Deconstructing Reasoning Tree..."
                        VoiceConversationState.SPEAKING -> "▶ Synthesizing Cognitive Voice Response..."
                        VoiceConversationState.IDLE -> "Tap Microphone or Speak to Start"
                    },
                    color = when (voiceState.conversationState) {
                        VoiceConversationState.LISTENING -> GeminiCyan
                        VoiceConversationState.THINKING -> GeminiPurple
                        VoiceConversationState.SPEAKING -> GeminiEmerald
                        VoiceConversationState.IDLE -> DarkTextMuted
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated Breathing Visualizer Orb
            VoiceOrbVisualizer(
                state = voiceState.conversationState,
                rms = voiceState.rmsDecibels,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (voiceState.conversationState == VoiceConversationState.LISTENING) {
                        voiceManager.stopListening()
                    } else if (voiceState.conversationState == VoiceConversationState.SPEAKING) {
                        voiceManager.stopSpeaking()
                    } else {
                        voiceManager.startListening()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Caption & Transcript Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (voiceState.userSpokenText.isNotBlank() || voiceState.partialSpokenText.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "You: ",
                                color = GeminiCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (voiceState.partialSpokenText.isNotBlank()) voiceState.partialSpokenText else voiceState.userSpokenText,
                                color = DarkTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    if (voiceState.currentReasoningStage.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = GeminiPurple,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = voiceState.currentReasoningStage,
                                color = GeminiPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (voiceState.modelSpokenText.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "Engine: ",
                                color = GeminiEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = voiceState.modelSpokenText,
                                color = DarkTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    if (voiceState.userSpokenText.isBlank() && voiceState.modelSpokenText.isBlank() && voiceState.partialSpokenText.isBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ask any complex algorithmic, mathematical, or logic puzzle query hands-free.",
                                color = DarkTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech Rate Chip
                Surface(
                    onClick = {
                        val nextRate = when (voiceState.speechRate) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 0.8f
                            else -> 1.0f
                        }
                        voiceManager.setSpeechRate(nextRate)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(0.8.dp, DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = DarkTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${voiceState.speechRate}x Speed",
                            color = DarkTextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Continuous Auto-Listen Toggle
                Surface(
                    onClick = {
                        voiceManager.toggleAutoListen(!voiceState.isAutoListenLoopEnabled)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (voiceState.isAutoListenLoopEnabled) GeminiBlue.copy(alpha = 0.15f) else DarkSurfaceVariant,
                    border = BorderStroke(0.8.dp, if (voiceState.isAutoListenLoopEnabled) GeminiBlue else DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (voiceState.isAutoListenLoopEnabled) GeminiBlue else DarkTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (voiceState.isAutoListenLoopEnabled) "Auto Loop: ON" else "Auto Loop: OFF",
                            color = if (voiceState.isAutoListenLoopEnabled) GeminiBlue else DarkTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Node Debugger Shortcut
                Surface(
                    onClick = {
                        voiceManager.stopSpeaking()
                        voiceManager.stopListening()
                        onDismiss()
                        onOpenCotDebugger()
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = GeminiCyan.copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, GeminiCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("btn_voice_to_cot_debugger")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CoT Tree",
                            color = GeminiCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Bottom Action Buttons: Mic Toggle, Stop Speaking, Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (voiceState.conversationState == VoiceConversationState.LISTENING) {
                            voiceManager.stopListening()
                        } else {
                            voiceManager.startListening()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("btn_voice_mic_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (voiceState.conversationState == VoiceConversationState.LISTENING) WarningOrange else GeminiBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (voiceState.conversationState == VoiceConversationState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (voiceState.conversationState == VoiceConversationState.LISTENING) "Stop Listening" else "Start Speaking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (voiceState.conversationState == VoiceConversationState.SPEAKING) {
                    OutlinedButton(
                        onClick = {
                            voiceManager.stopSpeaking()
                        },
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("btn_interrupt_voice_speech"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextPrimary),
                        border = BorderStroke(1.dp, DarkOutline)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Interrupt")
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceOrbVisualizer(
    state: VoiceConversationState,
    rms: Float,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    val coreColor by animateColorAsState(
        targetValue = when (state) {
            VoiceConversationState.LISTENING -> GeminiCyan
            VoiceConversationState.THINKING -> GeminiPurple
            VoiceConversationState.SPEAKING -> GeminiEmerald
            VoiceConversationState.IDLE -> GeminiBlue
        },
        animationSpec = tween(400),
        label = "orb_color"
    )

    val glowColor by animateColorAsState(
        targetValue = when (state) {
            VoiceConversationState.LISTENING -> GeminiBlue
            VoiceConversationState.THINKING -> GeminiPink
            VoiceConversationState.SPEAKING -> VerificationGreen
            VoiceConversationState.IDLE -> DarkSurfaceElevated
        },
        animationSpec = tween(400),
        label = "glow_color"
    )

    val effectiveScale = (pulseScale + (rms * 0.03f)).coerceIn(0.9f, 1.35f)

    Box(
        modifier = Modifier
            .size(130.dp)
            .clickable { onClick() }
            .testTag("voice_orb_visualizer_box"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.minDimension / 2) * 0.65f * effectiveScale

            // Outer Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreColor.copy(alpha = 0.35f), glowColor.copy(alpha = 0.1f), Color.Transparent),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                radius = baseRadius * 1.5f,
                center = center
            )

            // Middle Glowing Shell
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.6f), coreColor.copy(alpha = 0.25f)),
                    center = center,
                    radius = baseRadius * 1.15f
                ),
                radius = baseRadius * 1.15f,
                center = center
            )

            // Inner Vibrant Neural Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), coreColor, glowColor),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )
        }

        Icon(
            imageVector = when (state) {
                VoiceConversationState.LISTENING -> Icons.Default.Mic
                VoiceConversationState.THINKING -> Icons.Default.Psychology
                VoiceConversationState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                VoiceConversationState.IDLE -> Icons.Default.GraphicEq
            },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(34.dp)
        )
    }
}
