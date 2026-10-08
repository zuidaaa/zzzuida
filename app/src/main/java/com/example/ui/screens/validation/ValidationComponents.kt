package com.example.ui.screens.validation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.validation.DirectInputValidationResult
import com.example.engine.validation.EnvironmentCleanupReport
import com.example.engine.validation.ValidationCategory
import com.example.engine.validation.ValidationItem
import com.example.engine.validation.ValidationStatus
import com.example.ui.theme.*

@Composable
fun SpecPill(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color.White)
        }
    }
}

@Composable
fun DirectInputCard(
    inputText: String,
    onInputChanged: (String) -> Unit,
    isValidating: Boolean,
    onValidate: () -> Unit,
    onSelectPreset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.8.dp, DeepThinkCyan.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = DeepThinkCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Direkt-Eingabe-Validierer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepThinkCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AST & Syntax",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepThinkCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Prüft Code-Snippets, Agenten-Anweisungen oder Prompt-Ketten auf Sicherheit, Stabilität, Kompatibilität und Performance.",
                fontSize = 11.sp,
                color = DarkTextMuted,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("one_ui_9_direct_input_field"),
                placeholder = {
                    Text(
                        "Füge Kotlin-Code, SKILL.md Markdown oder System-Prompts ein...",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepThinkCyan,
                    unfocusedBorderColor = DarkOutlineVariant,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Preset Suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Vorlagen:", fontSize = 10.sp, color = DarkTextMuted, modifier = Modifier.align(Alignment.CenterVertically))

                listOf(
                    "Coroutine IO Flow" to "fun streamData(): Flow<String> = flow { emit(\"test\") }.flowOn(Dispatchers.IO)",
                    "Secure Auth Gate" to "if (Firebase.auth.currentUser == null) throw SecurityException(\"Unauthorized\")",
                    "One UI 9 Insets" to "Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding -> Box(Modifier.padding(padding)) }"
                ).forEach { (title, snippet) ->
                    Surface(
                        color = DarkSurfaceHigh,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.6.dp, DarkOutlineVariant),
                        modifier = Modifier.clickable { onSelectPreset(snippet) }
                    ) {
                        Text(
                            text = title,
                            fontSize = 9.sp,
                            color = DeepThinkCyanLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onValidate,
                enabled = !isValidating,
                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("one_ui_9_run_validation_button")
            ) {
                if (isValidating) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Validierung läuft...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eingabe & App-Umfeld jetzt prüfen", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CleanupReportCard(
    report: EnvironmentCleanupReport,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.8.dp, DeepThinkEmerald.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DeepThinkEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bereinigung & Optimierung abgeschlossen", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepThinkEmerald)
                }
                Text("RAM: ${String.format("%.1f", report.ramFreedMb)} MB", fontSize = 10.sp, color = DeepThinkEmerald)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RAM Vorher/Nachher", fontSize = 8.sp, color = DarkTextMuted)
                        Text("${String.format("%.0f", report.memoryBeforeMb)}→${String.format("%.0f", report.memoryAfterMb)} MB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepThinkEmerald)
                    }
                }

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Room SQLite", fontSize = 8.sp, color = DarkTextMuted)
                        Text(if (report.databaseVacuumed) "VACUUM OK" else "Optimiert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepThinkCyan)
                    }
                }

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pruned Logs", fontSize = 8.sp, color = DarkTextMuted)
                        Text("${report.cacheEntriesPruned} Einträge", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepThinkVioletLight)
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationScoreBanner(
    result: DirectInputValidationResult,
    modifier: Modifier = Modifier
) {
    val scoreColor = when {
        result.overallScore >= 90 -> DeepThinkEmerald
        result.overallScore >= 75 -> DeepThinkCyan
        result.overallScore >= 60 -> DeepThinkAmber
        else -> Color.Red
    }

    val isProdReady = result.overallScore >= 80

    val categoryScores = listOf(
        ValidationCategory.SECURITY to result.securityScore,
        ValidationCategory.STABILITY to result.stabilityScore,
        ValidationCategory.COMPATIBILITY to result.compatibilityScore,
        ValidationCategory.CODE_QUALITY to result.codeScore,
        ValidationCategory.PERFORMANCE to result.performanceScore
    )

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, scoreColor.copy(alpha = 0.7f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(scoreColor.copy(alpha = 0.2f))
                            .border(1.5.dp, scoreColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${result.overallScore}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = scoreColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Gesamt-Audit-Score",
                            fontSize = 11.sp,
                            color = DarkTextMuted
                        )
                        Text(
                            text = result.executionSafetyVerdict,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isProdReady) DeepThinkEmerald.copy(alpha = 0.2f) else DeepThinkAmber.copy(alpha = 0.2f))
                        .border(0.6.dp, if (isProdReady) DeepThinkEmerald else DeepThinkAmber, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isProdReady) "PROD-READY ✓" else "OPTIMIERUNG NÖTIG ⚠️",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProdReady) DeepThinkEmerald else DeepThinkAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scores per Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categoryScores.forEach { (cat, score) ->
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(cat.name.take(3), fontSize = 8.sp, color = DarkTextMuted, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$score%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    score >= 90 -> DeepThinkEmerald
                                    score >= 75 -> DeepThinkCyan
                                    else -> DeepThinkAmber
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationItemCard(
    item: ValidationItem,
    onApplyRecommendation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val statusColor = when (item.status) {
        ValidationStatus.PASSED -> DeepThinkEmerald
        ValidationStatus.WARNING -> DeepThinkAmber
        ValidationStatus.FAILED -> Color.Red
        ValidationStatus.INFO -> DeepThinkCyan
    }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.status) {
                                ValidationStatus.PASSED -> Icons.Default.Check
                                ValidationStatus.WARNING -> Icons.Default.Warning
                                ValidationStatus.FAILED -> Icons.Default.Error
                                ValidationStatus.INFO -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = item.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${item.category.displayName} • Score: ${item.score}/100 • ${item.metric}",
                            fontSize = 10.sp,
                            color = DarkTextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(0.6.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.details,
                fontSize = 11.sp,
                color = Color.LightGray,
                lineHeight = 15.sp
            )

            // Recommendation
            if (item.recommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 ${item.recommendation}",
                            fontSize = 10.sp,
                            color = DeepThinkCyanLight,
                            modifier = Modifier.weight(1f),
                            lineHeight = 14.sp
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(item.recommendation))
                                onApplyRecommendation(item.recommendation)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopieren", tint = DeepThinkCyan, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}
