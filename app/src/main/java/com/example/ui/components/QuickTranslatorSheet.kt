package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.DeepThinkAmber
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkEmerald
import com.example.ui.theme.DeepThinkEmeraldLight
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkIndigoLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTranslatorSheet(
    viewModel: MainViewModel,
    uiState: UiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val languages = remember { viewModel.translationManager.supportedLanguages }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DeepThinkIndigo.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            tint = DeepThinkIndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ML Kit + Gemini Translator",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time on-device translation assistance",
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Language Chips
            Text(
                text = "Target Language:",
                color = DarkTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.forEach { lang ->
                    val isSelected = uiState.quickTranslateTargetLang == lang.code
                    Surface(
                        onClick = { viewModel.setQuickTranslateTargetLang(lang.code) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) DeepThinkIndigo.copy(alpha = 0.35f) else DarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.dp else 0.5.dp,
                            if (isSelected) DeepThinkIndigoLight else DarkOutlineVariant
                        ),
                        modifier = Modifier.testTag("quick_lang_${lang.code}")
                    ) {
                        Text(
                            text = "${lang.flagEmoji} ${lang.displayName}",
                            color = if (isSelected) Color.White else DarkTextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input TextField
            OutlinedTextField(
                value = uiState.quickTranslateInput,
                onValueChange = { viewModel.onQuickTranslateInputChange(it) },
                placeholder = { Text("Enter text to translate...", color = DarkTextSubtle, fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepThinkIndigoLight,
                    unfocusedBorderColor = DarkOutlineVariant,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp)
                    .testTag("quick_translate_input_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Translate Action Buttons (ML Kit On-Device & Gemini Semantic Nuance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.executeQuickTranslate(useGeminiNuance = false) },
                    enabled = uiState.quickTranslateInput.isNotBlank() && !uiState.isQuickTranslating,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mlkit_translate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ML Kit (Instant)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.executeQuickTranslate(useGeminiNuance = true) },
                    enabled = uiState.quickTranslateInput.isNotBlank() && !uiState.isQuickTranslating,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_nuance_translate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gemini Nuance", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Translation Result Card
            if (uiState.isQuickTranslating) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = DeepThinkIndigoLight,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Translating...", color = DarkTextMuted, fontSize = 11.sp)
                }
            }

            uiState.quickTranslateResult?.let { result ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, DeepThinkIndigoLight.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DeepThinkIndigo.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${result.engineUsed} • ${result.sourceLanguageCode.uppercase()} ➔ ${result.targetLanguageCode.uppercase()}",
                                    color = DeepThinkIndigoLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Translation", result.translatedText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied translation!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = DarkTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.translatedText,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (result.culturalNuanceNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 ${result.culturalNuanceNotes}",
                                color = DeepThinkAmber,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
