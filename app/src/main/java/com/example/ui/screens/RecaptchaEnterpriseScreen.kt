package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.recaptcha.RecaptchaTelemetryEntry
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecaptchaEnterpriseScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onBack: () -> Unit = {}
) {
    val settings = uiState.recaptchaSettings
    val logs = uiState.recaptchaTelemetryLogs
    val isEvaluating = uiState.isEvaluatingRecaptcha
    val lastScore = uiState.lastRecaptchaScore

    var projectIdInput by remember(settings.projectId) { mutableStateOf(settings.projectId) }
    var siteKeyInput by remember(settings.siteKey) { mutableStateOf(settings.siteKey) }
    var apiKeyInput by remember(settings.apiKeyOverride) { mutableStateOf(settings.apiKeyOverride) }
    var isExpandedConfig by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("recaptcha_enterprise_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "reCAPTCHA Enterprise",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Risikobewertung für Sign-In & Cloud Vault",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Status Badge
            Surface(
                color = if (lastScore != null && lastScore < settings.minScoreThreshold) Color(0x33EF4444) else Color(0x3322C55E),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (lastScore != null && lastScore < settings.minScoreThreshold) VibrantRed else VibrantGreen)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (lastScore != null && lastScore < settings.minScoreThreshold) VibrantRed else VibrantGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lastScore != null) "Score: ${String.format(Locale.US, "%.2f", lastScore)}" else "Bereit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (lastScore != null && lastScore < settings.minScoreThreshold) VibrantRed else VibrantGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Policy Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0x1A14B8A6),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Passiver Audit-Modus aktiv: Verdächtige Risikowerte (< 0,5) werden detailliert protokolliert, ohne reguläre Benutzer zu blockieren.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCCFBF1)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Konfiguration & Anmeldedaten",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            TextButton(onClick = { isExpandedConfig = !isExpandedConfig }) {
                                Text(if (isExpandedConfig) "Weniger" else "Bearbeiten", color = LuminousBlue, fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Projekt: ${settings.projectId} • Site-Key: ${settings.siteKey.take(12)}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        AnimatedVisibility(visible = isExpandedConfig) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                OutlinedTextField(
                                    value = projectIdInput,
                                    onValueChange = {
                                        projectIdInput = it
                                        viewModel.updateRecaptchaSettings(projectId = it)
                                    },
                                    label = { Text("Google Cloud Projekt-ID") },
                                    modifier = Modifier.fillMaxWidth().testTag("recaptcha_project_id_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuminousBlue,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = siteKeyInput,
                                    onValueChange = {
                                        siteKeyInput = it
                                        viewModel.updateRecaptchaSettings(siteKey = it)
                                    },
                                    label = { Text("reCAPTCHA Enterprise Site-Key") },
                                    modifier = Modifier.fillMaxWidth().testTag("recaptcha_site_key_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuminousBlue,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = {
                                        apiKeyInput = it
                                        viewModel.updateRecaptchaSettings(apiKeyOverride = it)
                                    },
                                    label = { Text("API-Key Override (Optional / BuildConfig)") },
                                    modifier = Modifier.fillMaxWidth().testTag("recaptcha_api_key_override_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuminousBlue,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Entwickler-Simulationsmodus",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Erzeugt Offline-Heuristiken ohne API-Kontingent",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = settings.isSimulationMode,
                                        onCheckedChange = { viewModel.updateRecaptchaSettings(isSimulationMode = it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live Testing Actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assessment-Simulation & Tests",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        if (isEvaluating) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                color = LuminousBlue,
                                trackColor = DarkOutlineVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerTestRecaptchaAssessment("SIGN_IN") },
                                enabled = !isEvaluating,
                                modifier = Modifier.weight(1f).testTag("test_recaptcha_signin_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Test SIGN_IN", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.triggerTestRecaptchaAssessment("CLOUD_SYNC") },
                                enabled = !isEvaluating,
                                modifier = Modifier.weight(1f).testTag("test_recaptcha_cloud_sync_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Test CLOUD_SYNC", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = { viewModel.triggerTestRecaptchaAssessment("SUSPICIOUS_TEST") },
                                enabled = !isEvaluating,
                                modifier = Modifier.weight(1f).testTag("test_recaptcha_suspicious_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("Test Verdacht (0.2)", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Audit Telemetry Logs Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Risiko-Audit-Protokoll (${logs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    if (logs.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearRecaptchaLogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leeren", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            if (logs.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        color = Color(0x16FFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Noch keine Assessment-Ereignisse erfasst", color = TextSecondary, fontSize = 12.sp)
                            Text("Führe Google Sign-In, Cloud Sync oder einen obigen Test aus.", color = TextDisabled, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                items(logs, key = { it.id }) { logEntry ->
                    RecaptchaTelemetryLogCard(logEntry = logEntry)
                }
            }
        }
    }
}

@Composable
fun RecaptchaTelemetryLogCard(logEntry: RecaptchaTelemetryEntry) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(logEntry.timestamp) { timeFormat.format(Date(logEntry.timestamp)) }

    val scoreColor = when {
        logEntry.score >= 0.7f -> VibrantGreen
        logEntry.score >= 0.4f -> LuminousYellow
        else -> VibrantRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC161B22)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (logEntry.isSuspicious) VibrantRed.copy(alpha = 0.5f) else BorderSubtle)
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scoreColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = logEntry.action,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $formattedTime",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    color = scoreColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Score: ${String.format(Locale.US, "%.2f", logEntry.score)}",
                        color = scoreColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = logEntry.statusMessage,
                fontSize = 11.sp,
                color = if (logEntry.isSuspicious) VibrantRed else Color(0xFFE2E8F0)
            )

            if (logEntry.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gründe: ${logEntry.reasons.joinToString(", ")}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = LuminousYellow
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latenz: ${logEntry.latencyMs} ms",
                    fontSize = 10.sp,
                    color = TextDisabled
                )
                Text(
                    text = if (logEntry.isSuspicious) "⚠️ Verdächtig protokolliert (Nicht blockiert)" else "✓ Verifiziert",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (logEntry.isSuspicious) LuminousYellow else VibrantGreen
                )
            }
        }
    }
}
