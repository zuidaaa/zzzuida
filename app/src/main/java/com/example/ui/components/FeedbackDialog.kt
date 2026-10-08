package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatMessageEntity
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
import com.example.ui.theme.WarningOrange

@Composable
fun FeedbackDialog(
    targetMessage: ChatMessageEntity,
    currentRating: Int,
    currentText: String,
    onRatingChanged: (Int) -> Unit,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("feedback_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rate LLM Reasoning Response",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        )
                        Text(
                            text = "Help optimize the offline cognitive engine",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = DarkTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thumbs Up / Thumbs Down & Star Rating
                Text(
                    text = "Overall Response Quality",
                    style = MaterialTheme.typography.labelMedium.copy(color = GeminiCyan, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbs Up
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (currentRating > 0) GeminiEmerald.copy(alpha = 0.25f) else DarkSurfaceVariant)
                            .border(1.dp, if (currentRating > 0) GeminiEmerald else DarkOutlineVariant, RoundedCornerShape(12.dp))
                            .clickable { onRatingChanged(if (currentRating > 0) 0 else 1) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("feedback_thumbs_up"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentRating > 0) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Helpful",
                                tint = if (currentRating > 0) GeminiEmerald else DarkTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Accurate & Deep",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (currentRating > 0) GeminiEmerald else DarkTextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Thumbs Down
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (currentRating < 0) WarningOrange.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .border(1.dp, if (currentRating < 0) WarningOrange else DarkOutlineVariant, RoundedCornerShape(12.dp))
                            .clickable { onRatingChanged(if (currentRating < 0) 0 else -1) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("feedback_thumbs_down"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentRating < 0) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                contentDescription = "Needs improvement",
                                tint = if (currentRating < 0) WarningOrange else DarkTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Needs Work",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (currentRating < 0) WarningOrange else DarkTextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Star Rating Granularity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val isSelected = (currentRating >= star && currentRating > 0) || (currentRating == 1 && star == 1)
                        IconButton(
                            onClick = { onRatingChanged(star) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (isSelected) Color(0xFFFFC107) else DarkTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Rigorous Proof", "Great Code", "Factually Grounded", "Concise CoT").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .border(0.5.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
                                .clickable {
                                    val newText = if (currentText.isBlank()) tag else "$currentText, $tag"
                                    onTextChanged(newText)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ $tag",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiBlue,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Feedback Input
                Text(
                    text = "Detailed Observations (Optional)",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted)
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = currentText,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .testTag("feedback_text_input"),
                    placeholder = {
                        Text(
                            "e.g., The derivation of boundary conditions in step 3 was especially thorough...",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted.copy(alpha = 0.6f))
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutlineVariant,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = DarkTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("feedback_submit_button")
                    ) {
                        Text("Submit Feedback", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
