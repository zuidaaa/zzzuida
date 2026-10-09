package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * UI Fragment: Multimodal Coding Prompt, Audio Whisper Stream, and Execution Action Card.
 */
@Composable
fun EdgeMultimodalInputCard(
    promptInput: String,
    onPromptChange: (String) -> Unit,
    isSimulatingVoice: Boolean,
    onToggleVoice: (Boolean) -> Unit,
    voiceTranscriptInput: String,
    onVoiceTranscriptChange: (String) -> Unit,
    isRunningPipeline: Boolean,
    onRunPipeline: () -> Unit,
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
                text = "Unified Multimodal Input (Text + UI Mockup + Speech)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = onPromptChange,
                label = { Text("Task Description / Code Prompt", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edge_prompt_input"),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = VibrantTeal,
                    unfocusedBorderColor = DarkOutlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Voice transcript simulation row (Phase 3: Speech <-> Code)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Stream",
                    tint = VibrantPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Whisper STT Audio Stream",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = VibrantPurple
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isSimulatingVoice,
                    onCheckedChange = onToggleVoice,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = VibrantPurple
                    )
                )
            }

            if (isSimulatingVoice) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = voiceTranscriptInput,
                    onValueChange = onVoiceTranscriptChange,
                    label = { Text("Live Audio Transcript (Whisper)", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whisper_transcript_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = VibrantPurple,
                        unfocusedBorderColor = DarkOutlineVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRunPipeline,
                enabled = !isRunningPipeline,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_edge_pipeline_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal)
            ) {
                if (isRunningPipeline) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Executing Edge Pipeline (Phase 0-3)...",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Loop",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Run Edge Multimodal Coding Loop",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
