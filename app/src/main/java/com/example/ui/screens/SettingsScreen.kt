package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Wifi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.engine.EngineMode
import com.example.engine.ThinkingLevel
import com.example.ui.MainViewModel
import com.example.ui.UiState
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkRose
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueLight
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import androidx.compose.material.icons.filled.TouchApp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customInstructionText by remember { mutableStateOf(uiState.systemInstruction) }
    var activeGuideTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GeminiBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Engine Settings & Cognitive Controls",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        // ONE UI 9 SYSTEM AUDITOR & CLEANER CARD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("settings_one_ui_9_audit_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "One UI 9 System Intelligence",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Android 17 • Exynos 2600 EU • 5-Pfeiler-Audit & Cleaner",
                                fontSize = 10.sp,
                                color = DeepThinkCyan
                            )
                        }
                    }
                    Button(
                        onClick = { viewModel.openOneUi9Center() },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("settings_open_one_ui_9_btn")
                    ) {
                        Text("Öffnen", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        var downloadingLang by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
        var downloadStatusMsg by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
        var selectedNativeLang by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("en") }
        var showManualDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        var showButtonMappingDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        var showDebuggerDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

        if (showDebuggerDialog) {
            AlertDialog(
                onDismissRequest = { showDebuggerDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDebuggerDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxSize(0.95f)) {
                        ThinkingAiDebuggerScreen(viewModel = viewModel)
                    }
                },
                containerColor = DarkBackground,
                modifier = Modifier.fillMaxSize(0.95f)
            )
        }

        if (showButtonMappingDialog) {
            AlertDialog(
                onDismissRequest = { showButtonMappingDialog = false },
                confirmButton = {
                    TextButton(onClick = { showButtonMappingDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxSize(0.95f)) {
                        ConfigurableButtonMappingScreen(viewModel = viewModel)
                    }
                },
                containerColor = DarkBackground,
                modifier = Modifier.fillMaxSize(0.95f)
            )
        }

        if (showManualDialog) {
            AlertDialog(
                onDismissRequest = { showManualDialog = false },
                confirmButton = {
                    TextButton(onClick = { showManualDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxSize(0.95f)) {
                        UserManualAndRoadmapScreen(viewModel = viewModel, uiState = uiState)
                    }
                },
                containerColor = DarkBackground,
                modifier = Modifier.fillMaxSize(0.95f)
            )
        }

        // Native Language Download & User Manual Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("native_language_download_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Native Language Download",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Choose German, English, or Mandarin for offline translation",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }
                    }
                    Button(
                        onClick = { showManualDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_user_manual_btn")
                    ) {
                        Text("Manual & Roadmap", fontSize = 11.sp, color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                    }
                }

                androidx.compose.material3.HorizontalDivider(color = DarkOutlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        Triple("en", "English 🇬🇧", "en"),
                        Triple("de", "German 🇩🇪", "de"),
                        Triple("zh", "Mandarin 🇨🇳", "zh")
                    )
                    languages.forEach { (code, label, langTag) ->
                        val isSelected = selectedNativeLang == code
                        val isDownloading = downloadingLang == code
                        Button(
                            onClick = {
                                selectedNativeLang = code
                                downloadingLang = code
                                downloadStatusMsg = "Downloading $label..."
                                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                    val success = viewModel.translationManager.downloadLanguageModel(langTag)
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        downloadingLang = null
                                        downloadStatusMsg = if (success) "$label downloaded successfully!" else "Download failed for $label"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) GeminiBlue else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("lang_download_${code}_btn")
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                if (downloadStatusMsg != null) {
                    Text(
                        text = downloadStatusMsg!!,
                        fontSize = 11.sp,
                        color = VerificationGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Button Action Mapping & Event Handling Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("button_action_mapping_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantPurple.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = VibrantPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Frei Konfigurierbare Tasten & Action-Mapping",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Action-Zuordnung & technische Ereignisverarbeitung digitaler Schaltflächen",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { showButtonMappingDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_button_mapping_btn")
                    ) {
                        Text("Konfigurieren", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Thinking AI-Core Debugger & Crash Prevention Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("thinking_ai_debugger_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Psychology,
                            contentDescription = null,
                            tint = GeminiBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Thinking AI-Core Debugger",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Segment-by-segment crash prevention & sustained app state",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { showDebuggerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_thinking_debugger_btn")
                    ) {
                        Text("Diagnose", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SSO & Credential Manager Authentication Section
        Card(
            modifier = Modifier.fillMaxWidth().testTag("auth_section_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Tune,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plattformübergreifende Anmeldung (SSO)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    if (uiState.isLoggedIn) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VerificationGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Angemeldet",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = VerificationGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Nicht angemeldet",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Wenn für Inhalte eine Anmeldung erforderlich ist, erfolgt diese plattformübergreifend (Android/iOS/Web) automatisch mit nur einem Klick nach der Erstanmeldung per Google One-Tap & Passkeys.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp
                    )
                )

                if (uiState.authStatusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.isAuthenticating) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = GeminiBlueLight
                                )
                            } else {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VerificationGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.authStatusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeminiBlueLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.isLoggedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = null,
                            tint = VerificationGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = uiState.loggedInUserName ?: "Cognitive User",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = uiState.loggedInUserEmail ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.performSignOut() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Text("Sign Out", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    val context = LocalContext.current
                    val activity = context as? Activity
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { activity?.let { viewModel.performSignIn(it) } },
                            enabled = !uiState.isAuthenticating,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_sso_button")
                        ) {
                            Text("Sign in with Google", style = MaterialTheme.typography.labelMedium)
                        }
                        
                        uiState.authStatusMessage?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepThinkRose,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // SECTION: RAG LOKALE DOKUMENTE (Option D)
        var docTitle by remember { mutableStateOf("") }
        var docContent by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("rag_section_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Info,
                        contentDescription = null,
                        tint = GeminiEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lokales RAG Dokumenten-Archiv",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Lade Dokumente oder Notizen in das lokale Archiv. Wenn aktiviert, fließen diese Dokumente als Grounding-Kontext direkt in deine Reasoning-Anfragen ein.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    label = { Text("Titel (z.B. Geheimrezept)", color = DarkTextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("doc_title_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiEmerald,
                        unfocusedBorderColor = DarkOutline,
                        focusedLabelColor = GeminiEmerald,
                        unfocusedLabelColor = DarkTextMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = docContent,
                    onValueChange = { docContent = it },
                    label = { Text("Inhalt / Textdaten", color = DarkTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("doc_content_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiEmerald,
                        unfocusedBorderColor = DarkOutline,
                        focusedLabelColor = GeminiEmerald,
                        unfocusedLabelColor = DarkTextMuted
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (docTitle.isNotBlank() && docContent.isNotBlank()) {
                            viewModel.addLocalDocument(docTitle, docContent)
                            docTitle = ""
                            docContent = ""
                        }
                    },
                    enabled = docTitle.isNotBlank() && docContent.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_doc_button")
                ) {
                    Text("Dokument hinzufügen & indizieren", color = DarkBackground, fontWeight = FontWeight.Bold)
                }

                if (uiState.localDocuments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Indizierte Dokumente (${uiState.localDocuments.size})",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.localDocuments.forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBackground)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                    Text(
                                        text = "${doc.fileSize} Bytes • RAG Aktiv",
                                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 10.sp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = doc.isRagEnabled,
                                        onCheckedChange = { viewModel.toggleLocalDocumentRag(doc.id, it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = GeminiEmerald,
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = DarkOutline
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "LÖSCHEN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.Red.copy(alpha = 0.8f),
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .clickable { viewModel.deleteLocalDocument(doc.id) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION: DATA BACKUP & RESTORE (Option A)
        var backupInputText by remember { mutableStateOf("") }
        var showBackupData by remember { mutableStateOf(false) }
        var generatedBackupString by remember { mutableStateOf("") }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("backup_section_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Tune,
                        contentDescription = null,
                        tint = GeminiBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Datensicherung & Export",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Exportiere oder importiere deine Konfigurationen, SSO-Modulzustände und lokalen Archive als verschlüsselte JSON-Strings.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            generatedBackupString = viewModel.exportBackup()
                            showBackupData = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("export_backup_button")
                    ) {
                        Text("Sicherung exportieren", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            if (backupInputText.isNotBlank()) {
                                viewModel.importBackup(backupInputText)
                                backupInputText = ""
                            }
                        },
                        enabled = backupInputText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("import_backup_button")
                    ) {
                        Text("Backup importieren", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (showBackupData) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = generatedBackupString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kopiere diesen Sicherungscode", color = GeminiBlueLight) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlueLight,
                            unfocusedBorderColor = DarkOutline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = backupInputText,
                    onValueChange = { backupInputText = it },
                    label = { Text("Füge einen Sicherungscode ein", color = DarkTextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("backup_import_input"),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutline,
                        focusedLabelColor = GeminiBlue,
                        unfocusedLabelColor = DarkTextMuted
                    ),
                    singleLine = true
                )

                if (uiState.backupRestoreLog != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = uiState.backupRestoreLog ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = GeminiBlueLight, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // SECTION: HAPTIC TEST (Option B)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("haptics_section_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                        contentDescription = null,
                        tint = GeminiBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Physikalische UX & Vibrations-Engine",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Erlebe feinste tactile Vibrations-Impulse bei kritischen Denkphasen, Modellwechseln oder Datenübertragungen.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.triggerHapticFeedback() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_haptics_button")
                ) {
                    Text("Taktiles Feedback testen", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // --- SECTION: DYNAMIC MODEL SWITCHING BY TASK COMPLEXITY ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dynamic_model_switching_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFB703).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFB703),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Dynamic Model Switching",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Task Complexity Engine",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFFB703),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = uiState.autoModelRoutingEnabled,
                        onCheckedChange = { viewModel.toggleAutoModelRouting() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFFB703),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = DarkOutline
                        ),
                        modifier = Modifier.testTag("auto_model_routing_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Automatically analyzes prompt complexity and dynamically switches LLMs: routes mathematical proofs, algorithmic verification, and architecture design to Pro Reasoners (or DeepSeek CoT), standard queries to Flash, and lightweight lookups to Flash-Lite.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Status Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.autoModelRoutingEnabled) Color(0xFFFFB703).copy(alpha = 0.35f) else DarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (uiState.autoModelRoutingEnabled) Color(0xFFFFB703) else DarkTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.autoModelRoutingEnabled) "ROUTING ACTIVE" else "MANUAL MODEL SELECTION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.autoModelRoutingEnabled) Color(0xFFFFB703) else DarkTextMuted,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.6.sp
                                    )
                                )
                            }

                            Text(
                                text = uiState.lastRoutingBadge,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiBlueLight,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        if (uiState.currentRoutingPlan != null && uiState.autoModelRoutingEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(uiState.currentRoutingPlan.complexity.colorHex).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = uiState.currentRoutingPlan.complexity.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(uiState.currentRoutingPlan.complexity.colorHex),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "Confidence: ${(uiState.currentRoutingPlan.confidenceScore * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DarkTextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.currentRoutingPlan.decisionReason,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextSubtle,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Threshold Selector
                Text(
                    text = "ROUTING AGGRESSIVENESS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Conservative", "Balanced", "Aggressive").forEach { level ->
                        val isSelected = uiState.routingThresholdLevel.startsWith(level.take(5))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) DarkSurfaceVariant else DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFFB703) else DarkOutline),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setRoutingThresholdLevel(level) }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else DarkTextMuted,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION: LLM MODEL UPDATES & UPGRADES ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("model_updates_upgrades_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiBlue.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Model Updates & Upgrades",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Newer LLM Releases & Weights",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiBlueLight,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }

                    // Check for updates button
                    Button(
                        onClick = { viewModel.checkForModelUpdates() },
                        enabled = !uiState.isCheckingForModelUpdates,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = GeminiBlueLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("check_model_updates_button")
                    ) {
                        if (uiState.isCheckingForModelUpdates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = GeminiBlueLight,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checking...", fontSize = 11.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Updates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Upgrade existing models to newer checkpoint revisions, apply Qualcomm NPU / SVE2 vector quantization optimizations, or register custom newer LLM model identifiers (e.g. Gemini 3.8 Flash Preview).",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        lineHeight = 16.sp,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Register Newer Model Button
                Button(
                    onClick = { viewModel.openRegisterCustomModelDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("register_newer_model_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+ Register Newer LLM Model / Endpoint",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Models List with Updates
                Text(
                    text = "INSTALLED & CLOUD LLM MODELS (${uiState.modelUpgradeItems.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        fontSize = 9.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.modelUpgradeItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (item.hasUpdate) GeminiBlue.copy(alpha = 0.6f) else DarkOutline
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("upgrade_item_${item.modelId}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.displayName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (item.isCloud) GeminiBlue.copy(alpha = 0.2f) else GeminiEmerald.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (item.isCloud) "Cloud" else "On-Device",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (item.isCloud) GeminiBlueLight else GeminiEmerald,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "v${item.currentVersion}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = DarkTextMuted,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                            if (item.hasUpdate) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowForward,
                                                    contentDescription = null,
                                                    tint = GeminiEmerald,
                                                    modifier = Modifier.size(10.dp).padding(horizontal = 2.dp)
                                                )
                                                Text(
                                                    text = "New: ${item.latestVersion}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = GeminiEmerald,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                            Text(
                                                text = " • ${item.contextWindow}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = DarkTextMuted,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }

                                    // Action: Upgrade or Up-to-date
                                    if (item.isUpgrading) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GeminiBlue.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${(item.upgradeProgress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = GeminiBlueLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    } else if (item.hasUpdate) {
                                        Button(
                                            onClick = { viewModel.upgradeModel(item) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp).testTag("upgrade_btn_${item.modelId}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upgrade", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = VerificationGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Up to date",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = VerificationGreen,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }

                                if (item.isUpgrading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = item.upgradeProgress,
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                        color = GeminiBlue,
                                        trackColor = DarkOutline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.upgradeStatusMessage,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeminiBlueLight,
                                            fontSize = 10.sp
                                        )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.changelog,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = DarkTextSubtle,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Update Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Check Updates on Launch",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Periodically checks AI Studio and Hugging Face registries for newer weights",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = uiState.autoCheckUpdatesOnLaunch,
                        onCheckedChange = { viewModel.toggleAutoCheckUpdatesOnLaunch() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GeminiBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Download Weights on Wi-Fi",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Downloads GGUF/AWQ quantized model updates in background on unmetered Wi-Fi",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = uiState.autoUpdateOnWifi,
                        onCheckedChange = { viewModel.toggleAutoUpdateOnWifi() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GeminiBlue
                        )
                    )
                }
            }
        }

        // Section 1: Engine Architecture & Offline Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.engineMode.isOffline) Icons.Default.CloudOff else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (uiState.engineMode.isOffline) GeminiEmerald else GeminiBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inference Engine Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Switch(
                        checked = uiState.engineMode == EngineMode.ONLINE_GEMINI_API,
                        onCheckedChange = { viewModel.toggleEngineMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GeminiBlue,
                            uncheckedThumbColor = GeminiEmerald,
                            uncheckedTrackColor = Color(0xFF10281D)
                        ),
                        modifier = Modifier.testTag("engine_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (uiState.engineMode.isOffline) {
                        "🟢 100% OFFLINE GEMINI 3.7 CORE ACTIVE\nZero network calls required. Full on-device multi-step heuristic reasoning, algorithmic proof generation, and verification trees."
                    } else {
                        "🌐 ONLINE CLOUD GEMINI API ACTIVE\nDirect REST connection with automatic offline fallback when disconnected."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = DarkTextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        // Section 2: Thinking Token Budget
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = GeminiBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Default Deep Thinking Budget",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ThinkingLevel.values().forEach { level ->
                    val isSelected = level == uiState.selectedThinkingLevel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (isSelected) GeminiBlue else DarkOutlineVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        color = if (isSelected) DarkSurfaceVariant else DarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { viewModel.setThinkingLevel(level) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${level.icon} ${level.label}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else DarkTextMuted
                                    )
                                )
                                Text(
                                    text = "${level.description} (~${level.tokenBudget} tokens)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkTextMuted,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = GeminiBlueLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Token Streaming Velocity
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stream Token Speed",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Text(
                        text = "${uiState.streamingSpeedMs} ms/token",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GeminiBlueLight
                        )
                    )
                }

                Slider(
                    value = uiState.streamingSpeedMs.toFloat(),
                    onValueChange = { viewModel.setStreamingSpeed(it.toLong()) },
                    valueRange = 0f..40f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = GeminiBlueLight,
                        activeTrackColor = GeminiBlue
                    ),
                    modifier = Modifier.testTag("streaming_speed_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Instant (0ms)", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted))
                    Text("Realistic (16ms)", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted))
                    Text("Deliberate (40ms)", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted))
                }
            }
        }

        // Section 4: Custom System Prompt / Persona
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = GeminiPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cognitive Persona & System Prompt",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customInstructionText,
                    onValueChange = { customInstructionText = it },
                    placeholder = {
                        Text(
                            "e.g. You are a Senior Algorithm Engineer with strict focus on formal complexity proofs...",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSubtle)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("custom_system_prompt_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutlineVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.setSystemInstruction(customInstructionText)
                        Toast.makeText(context, "System instructions saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Apply Persona", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }
            }
        }

        // Section 5: European Samsung S26 Ultra CPU Acceleration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.4f))
        ) {
            val profile = uiState.cpuHardwareProfile
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = GeminiEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "One UI 9.0 & Android 17 Suite",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeminiBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("A17 / v17", style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeminiEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ONE UI 9.0", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold, fontSize = 8.5.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• SoC: ${profile.socName}\n• Architecture: ${profile.architecture}\n• NPU Acceleration: ${profile.npuTops} TOPS Dual-Neural Engine\n• SIMD Extensions: SVE2 & ARM NEON Vector Acceleration Active\n• GPU Engine: ${profile.gpuModel}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = DarkTextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        // Section 6: Comprehensive User Guide & Real-World Handbook
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeminiBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Interactive Application Guide",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Tabs Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("Workings", "Examples", "Real-World")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = activeGuideTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GeminiBlue else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GeminiBlueLight else DarkOutlineVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { activeGuideTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else DarkTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content Display
                when (activeGuideTab) {
                    0 -> {
                        // Workings
                        Text(
                            text = "How the Cognitive Engine Works",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This application hosts a sophisticated multi-stage reasoning engine that generates chain-of-thought traces (thought paths) prior to formulating replies. It supports two primary execution modes:\n\n" +
                                    "• ONLINE CLOUD MODE: Connects directly to Gemini 2.5 Flash / Pro API endpoints for ultra-low latency, dynamic translation matrices, and high-fidelity video analytics pipelines.\n\n" +
                                    "• 100% OFFLINE MODE: Operates fully on-device without any internet connectivity. Powered by localized lightweight LLM model shards optimized for ARM SVE2/NEON architectures, executing proof checking, reasoning, and offline text processing fully client-side.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                lineHeight = 16.sp
                            )
                        )
                    }
                    1 -> {
                        // Worked Examples
                        Text(
                            text = "Tested & Working Examples",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Example 1
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "1. ALGORITHMIC GRAPH COMPLEXITY",
                                    style = MaterialTheme.typography.labelMedium.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Prompt: \"Prove why Dijkstra's algorithm with a Binary Heap has a run time complexity of O((V + E) log V).\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Result: Generates step-by-step mathematical tracing of priority queue states, edge evaluation costs, and cumulative proof steps.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Example 2
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "2. MCP REGISTRY INTEGRATION",
                                    style = MaterialTheme.typography.labelMedium.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Prompt: \"mcp: search models for classification and use the registry to audit its performance metrics.\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.White, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Result: Triggers Model Control Protocol, fetches JSON payloads from Hugging Face APIs, and processes live schema metadata.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                    2 -> {
                        // Real-world Uses
                        Text(
                            text = "Real-World Practical Deployments",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• COGNITIVE REASONING ARCHIVES: Acts as an interactive notebook for engineers, scientists, and analysts requiring robust mathematical verification traces before accepting replies.\n\n" +
                                    "• SECURE OFFLINE FIELD RESEARCH: Allows medical, geographic, and field researchers to run rich conversational AI diagnostics and text categorization in remote areas lacking network links.\n\n" +
                                    "• CULTURAL GROUNDING & ARCHIVAL DIGITIZATION: Supports curators in analyzing artifacts, writing historical context summaries, and performing localized translation buffers.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.isRegisterCustomModelDialogOpen) {
        RegisterNewerModelDialog(
            onDismiss = { viewModel.closeRegisterCustomModelDialog() },
            onRegister = { id, name, family, paramSize, quant, contextWindow, desc, isCloud, ver, tData, relDate ->
                viewModel.registerCustomModel(
                    id = id,
                    name = name,
                    family = family,
                    parameterSize = paramSize,
                    quantization = quant,
                    contextWindow = contextWindow,
                    description = desc,
                    isCloud = isCloud,
                    version = ver,
                    trainingData = tData,
                    releaseDate = relDate
                )
            }
        )
    }
}

@Composable
fun RegisterNewerModelDialog(
    onDismiss: () -> Unit,
    onRegister: (
        id: String,
        name: String,
        family: String,
        paramSize: String,
        quant: String,
        contextWindow: String,
        desc: String,
        isCloud: Boolean,
        version: String,
        trainingData: String,
        releaseDate: String
    ) -> Unit
) {
    var modelId by remember { mutableStateOf("gemini-3.8-flash") }
    var modelName by remember { mutableStateOf("Gemini 3.8 Flash Preview") }
    var family by remember { mutableStateOf("Google DeepMind") }
    var paramSize by remember { mutableStateOf("Next-Gen Flash") }
    var quant by remember { mutableStateOf("Cloud Native FP16") }
    var contextWindow by remember { mutableStateOf("1.0M Tokens") }
    var description by remember { mutableStateOf("Next-gen high-efficiency reasoning model with enhanced multi-turn synthesis.") }
    var isCloud by remember { mutableStateOf(true) }
    var version by remember { mutableStateOf("v3.8.0-Alpha") }
    var trainingData by remember { mutableStateOf("Trained on next-gen multimodal reasoning dataset with human feedback.") }
    var releaseDate by remember { mutableStateOf("2026-03-15") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Register Newer LLM Model",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Quick presets row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "Gemini 3.8 Flash" to Triple("gemini-3.8-flash", "Gemini 3.8 Flash Preview", true),
                        "DeepSeek R2" to Triple("deepseek-r2-distill-q4", "DeepSeek R2 Distill 8B", false),
                        "Gemma 3 12B" to Triple("gemma-3-12b-it", "Gemma 3 12B IT", false)
                    )

                    presets.forEach { (label, data) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    modelId = data.first
                                    modelName = data.second
                                    isCloud = data.third
                                    if (data.third) {
                                        family = "Google DeepMind"
                                        paramSize = "Next-Gen Flash"
                                        quant = "Cloud Native FP16"
                                        contextWindow = "1.0M Tokens"
                                        description = "Next-gen multi-turn reasoning preview endpoint"
                                        version = "v3.8.0-Alpha"
                                        trainingData = "Trained on next-gen multimodal reasoning dataset with human preference learning."
                                        releaseDate = "2026-03-15"
                                    } else {
                                        family = if (data.first.contains("deepseek")) "DeepSeek AI" else "Google Gemma"
                                        paramSize = if (data.first.contains("deepseek")) "8B Parameters" else "12B Parameters"
                                        quant = "GGUF Q4_K_M (SVE2 / NPU)"
                                        contextWindow = "128k Tokens"
                                        description = "On-device quantized offline neural weight file"
                                        version = if (data.first.contains("deepseek")) "v2.0.0-Beta" else "v3.0.0-Preview"
                                        trainingData = if (data.first.contains("deepseek")) "Trained on 2T mathematical and Chinese/English reasoning steps." else "Trained on Google Gemma-3 math alignment and instruction corpus."
                                        releaseDate = "2026-02-10"
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GeminiBlueLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = modelId,
                    onValueChange = { modelId = it },
                    label = { Text("Model ID / Endpoint", color = DarkTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_model_id"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Display Name", color = DarkTextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_model_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = family,
                        onValueChange = { family = it },
                        label = { Text("Family", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = paramSize,
                        onValueChange = { paramSize = it },
                        label = { Text("Parameters", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quant,
                        onValueChange = { quant = it },
                        label = { Text("Quantization", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = contextWindow,
                        onValueChange = { contextWindow = it },
                        label = { Text("Context", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = { Text("Version", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = releaseDate,
                        onValueChange = { releaseDate = it },
                        label = { Text("Release Date", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                OutlinedTextField(
                    value = trainingData,
                    onValueChange = { trainingData = it },
                    label = { Text("Training Data Corpus", color = DarkTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes", color = DarkTextMuted) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = DarkOutline,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCloud) "Cloud Hosted API" else "Local On-Device Weight File",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isCloud) GeminiBlueLight else GeminiEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Switch(
                        checked = isCloud,
                        onCheckedChange = { isCloud = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GeminiBlue,
                            uncheckedThumbColor = GeminiEmerald,
                            uncheckedTrackColor = Color(0xFF10281D)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (modelId.isNotBlank() && modelName.isNotBlank()) {
                        onRegister(
                            modelId,
                            modelName,
                            family,
                            paramSize,
                            quant,
                            contextWindow,
                            description,
                            isCloud,
                            version,
                            trainingData,
                            releaseDate
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_register_model_btn")
            ) {
                Text("Register & Deploy", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DarkTextMuted)
            }
        }
    )
}

