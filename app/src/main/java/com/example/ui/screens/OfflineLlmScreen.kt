package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ConversationEntity
import com.example.data.DocumentEntity
import com.example.data.LocalModelEntity
import com.example.engine.ThinkingLevel
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.MarkdownContentView
import com.example.ui.components.ThoughtAccordion
import com.example.ui.components.ChainOfThoughtDebugOverlay
import com.example.ui.theme.*

/**
 * Predefined sample files representing realistic documents to immediately test the RAG engine
 */
data class SampleFileEntry(
    val name: String,
    val description: String,
    val content: String
)

object SampleFileCatalog {
    val sampleFiles = listOf(
        SampleFileEntry(
            name = "npu_telemetry_crash.log",
            description = "Hardware memory log showing NPU memory exhaustion bounds",
            content = """
            [2026-09-01 04:12:01.402] [INFO] [Exynos-NPU-Core0] Initializing quantization weights (Q4_K_M)...
            [2026-09-01 04:12:01.488] [INFO] [Exynos-NPU-Core0] Memory allocated: 1,842 MB / 12,288 MB VRAM.
            [2026-09-01 04:12:02.110] [WARN] [Exynos-Memory] High tensor contention detected on Cortex-X6 cache boundary.
            [2026-09-01 04:12:02.340] [DEBUG] [SVE2-Vector] 128-bit vector arithmetic fallback applied for Layer 28 attention head.
            [2026-09-01 04:12:02.780] [SUCCESS] [ReasoningEngine] 480 tokens verified with zero memory leakage.
            """.trimIndent()
        ),
        SampleFileEntry(
            name = "quarterly_strategy_brief.txt",
            description = "Q3 Roadmap for European privacy-first local AI services",
            content = """
            EXECUTIVE STRATEGY BRIEF - Q3/Q4 ON-DEVICE COGNITIVE SYSTEMS
            Target: Deploy zero-token, privacy-first local LLM reasoning across 50M European mobile devices.
            Key Findings:
            1. Zero Latency: On-device NPU eliminates roundtrip cloud delays, enabling real-time voice translation.
            2. Privacy & Compliance: 100% GDPR/Knox sandbox compliance with local SQLite Room vector caching.
            3. Cost Optimization: Reduces cloud inference operating expenditures by 78% on high-frequency summarization.
            Action Items:
            - Finalize Gemma 3 4B-IT and Gemini 3.7 Flash on-device kernels.
            - Benchmark FP8 / INT4 precision trade-offs.
            """.trimIndent()
        ),
        SampleFileEntry(
            name = "sensor_metrics.csv",
            description = "Real-time CPU & NPU thermal utilization diagnostics table",
            content = """
            timestamp,cpu_temp_c,npu_utilization_pct,power_draw_mw,fps
            04:00:01,34.2,12.5,450,120.0
            04:00:02,36.8,84.1,1820,119.8
            04:00:03,38.1,98.6,2240,120.0
            04:00:04,39.0,95.2,2180,120.0
            04:00:05,37.4,45.0,980,120.0
            """.trimIndent()
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineLlmScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localModels by viewModel.localModels.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val cachedReasoningList by viewModel.cachedReasoningList.collectAsStateWithLifecycle()

    var showAddDocDialog by remember { mutableStateOf(false) }
    var newDocTitle by remember { mutableStateOf("") }
    var newDocContent by remember { mutableStateOf("") }
    var activeChallengeForWorkbench by remember { mutableStateOf<com.example.engine.problemsolving.ProblemChallenge?>(null) }

    // Interactive Problem Solving Workbench Dialog
    activeChallengeForWorkbench?.let { challenge ->
        val challengeProgress = uiState.problemProgressList.find { it.problemId == challenge.id }
        ProblemSolvingWorkbenchDialog(
            challenge = challenge,
            initialProgress = challengeProgress,
            onDismiss = { activeChallengeForWorkbench = null },
            onSaveProgress = { updatedProgress ->
                viewModel.saveProblemProgress(updatedProgress)
            }
        )
    }

    // RAG Add Document Dialog
    if (showAddDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = null,
                        tint = GeminiBlueLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Index New RAG Document",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
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
                        text = "The document will be saved to your secure SQLite local store and embedded for context-matching.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                    )

                    OutlinedTextField(
                        value = newDocTitle,
                        onValueChange = { newDocTitle = it },
                        label = { Text("Document Title (e.g. tech_specs.txt)", color = DarkTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = newDocContent,
                        onValueChange = { newDocContent = it },
                        label = { Text("Text Content / Body", color = DarkTextMuted) },
                        minLines = 4,
                        maxLines = 8,
                        placeholder = { Text("Paste specifications, logs, codes, or instructions here...", color = DarkTextSubtle) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiBlue,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary
                        )
                    )

                    Text(
                        text = "OR LOAD SAMPLE FILE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SampleFileCatalog.sampleFiles) { sample ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(8.dp))
                                    .clickable {
                                        newDocTitle = sample.name
                                        newDocContent = sample.content
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Column {
                                    Text(sample.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                                    Text(sample.description, fontSize = 9.sp, color = DarkTextMuted, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDocTitle.isNotBlank() && newDocContent.isNotBlank()) {
                            viewModel.addLocalDocument(newDocTitle, newDocContent)
                            newDocTitle = ""
                            newDocContent = ""
                            showAddDocDialog = false
                        }
                    },
                    enabled = newDocTitle.isNotBlank() && newDocContent.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Index Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) {
                    Text("Cancel", color = DarkTextMuted)
                }
            }
        )
    }

    // Main layout
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Diagnostic Title / Telemetry Header
        item {
            OfflineLlmHeaderCard(uiState = uiState, viewModel = viewModel)
        }

        // 2. Select Loaded Model (Horizontal Scroll)
        item {
            OfflineLocalModelSelectorCard(
                localModels = localModels,
                activeModelId = uiState.activeModelId,
                onSelectModel = { viewModel.setActiveModel(it) }
            )
        }

        // 3. Adjust Generation Parameters
        item {
            OfflineGenerationParametersCard(
                uiState = uiState,
                onTemperatureChanged = { viewModel.setOfflineTemperature(it) },
                onMaxTokensChanged = { viewModel.setOfflineMaxTokens(it) },
                onThinkingLevelChanged = { viewModel.setThinkingLevel(it) }
            )
        }

        // 3.5 Interactive Problem-Solving Modules
        item {
            OfflineProblemSolvingCard(
                challenges = com.example.engine.problemsolving.ProblemCatalog.challenges,
                progressList = uiState.problemProgressList,
                onOpenChallenge = { challenge ->
                    activeChallengeForWorkbench = challenge
                }
            )
        }

        // 3.6 D3.js Thought-Trace Reasoning Time & Token Usage Analytics
        item {
            com.example.ui.components.D3ReasoningVisualizationPanel(
                cachedItems = cachedReasoningList
            )
        }

        // 4. RAG Document Store Panel
        item {
            OfflineRagDocumentStoreCard(
                documents = uiState.localDocuments,
                onToggleRag = { id, enabled -> viewModel.toggleLocalDocumentRag(id, enabled) },
                onDeleteDoc = { viewModel.deleteLocalDocument(it) },
                onAddDocClick = { showAddDocDialog = true }
            )
        }

        // 5. Chat History & Sessions Manager
        item {
            OfflineChatSessionHistoryCard(
                conversations = conversations,
                currentConvId = uiState.currentConversationId,
                onSelectSession = { viewModel.selectConversation(it.id) },
                onNewSession = { viewModel.startNewConversation() },
                onClearHistory = { viewModel.clearAllConversations() }
            )
        }

        // 6. Common Fast Actions templates Bar
        item {
            OfflineCommonActionsBar(
                viewModel = viewModel,
                uiState = uiState,
                onAnalyzeFileClick = { viewModel.openFileAnalysisDialog() },
                onGenerateSummaryClick = { viewModel.openSummaryDialog() }
            )
        }

        // 7. Prompt Input Card
        item {
            OfflinePromptInputCard(
                promptInput = uiState.offlinePromptInput,
                isGenerating = uiState.isOfflineGenerating,
                onPromptChanged = { viewModel.setOfflinePromptInput(it) },
                onRunInference = { viewModel.runOfflineLlmInference() },
                onStopGenerating = { viewModel.stopOfflineGenerating() },
                onClearInput = { viewModel.setOfflinePromptInput("") }
            )
        }

        // 8. Multi-turn Response display area
        item {
            OfflineResponseDisplayCard(
                viewModel = viewModel,
                uiState = uiState,
                messages = messages,
                onClearResponse = { viewModel.clearOfflineDisplay() },
                onCopyResponse = { text ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Offline LLM Response", text))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                onShareResponse = { text ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Offline LLM Inference")
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share response"))
                }
            )
        }
    }

    if (uiState.isCotDebugOverlayOpen) {
        ChainOfThoughtDebugOverlay(
            steps = uiState.cotDebugSteps,
            initialStepIndex = uiState.cotDebugInitialStep,
            modelName = uiState.activeModelName,
            localDocuments = uiState.localDocuments,
            onDismiss = { viewModel.closeCotDebugOverlay() }
        )
    }
}

/**
 * Top Header Card displaying On-Device NPU status, Model details, and Temperature Tuning
 */
@Composable
fun OfflineLlmHeaderCard(
    uiState: UiState,
    viewModel: MainViewModel
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Offline LLM Studio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (uiState.isOfflineGenerating) GeminiCyan else GeminiEmerald, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isOfflineGenerating) "NPU Execution Active" else "Zero-Token On-Device Ready",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (uiState.isOfflineGenerating) GeminiCyan else GeminiEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Zero-Token Hardware Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GeminiBlue.copy(alpha = 0.15f))
                        .border(0.8.dp, GeminiBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = GeminiBlueLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Exynos NPU Turbo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. Model Selector Card - Horizontal Scroll
 */
@Composable
fun OfflineLocalModelSelectorCard(
    localModels: List<LocalModelEntity>,
    activeModelId: String,
    onSelectModel: (LocalModelEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT LOADED MODEL",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkTextSubtle,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(localModels) { model ->
                val isSelected = activeModelId == model.id
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectModel(model) }
                        .border(
                            width = if (isSelected) 1.5.dp else 0.8.dp,
                            color = if (isSelected) GeminiBlue else DarkOutlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GeminiBlue.copy(alpha = 0.12f) else DarkSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = model.parameterSize,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GeminiBlueLight else DarkTextMuted,
                                    fontSize = 10.sp
                                )
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(GeminiBlueLight, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = model.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (isSelected) Color.White else DarkTextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${model.quantization} • ${model.computeBackend}",
                            fontSize = 11.sp,
                            color = DarkTextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Generation Parameters Card - Temperature and Max Tokens Tuning
 */
@Composable
fun OfflineGenerationParametersCard(
    uiState: UiState,
    onTemperatureChanged: (Float) -> Unit,
    onMaxTokensChanged: (Int) -> Unit,
    onThinkingLevelChanged: (ThinkingLevel) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "LLM GENERATION PARAMETERS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GeminiBlueLight,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Temperature Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeviceThermostat, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Temperature", fontSize = 13.sp, color = DarkTextPrimary, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", uiState.offlineTemperature),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = GeminiBlueLight,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = uiState.offlineTemperature,
                onValueChange = onTemperatureChanged,
                valueRange = 0.1f..1.5f,
                steps = 14,
                colors = SliderDefaults.colors(
                    thumbColor = GeminiBlue,
                    activeTrackColor = GeminiBlueLight,
                    inactiveTrackColor = DarkOutlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Max Tokens Chips
            Text("Max Output Tokens", fontSize = 13.sp, color = DarkTextPrimary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val tokenOptions = listOf(256, 512, 1024, 2048, 4096)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tokenOptions) { tokens ->
                    val isSelected = uiState.offlineMaxTokens == tokens
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMaxTokensChanged(tokens) },
                        label = { Text("$tokens", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeminiBlue.copy(alpha = 0.25f),
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceVariant,
                            labelColor = DarkTextMuted
                        ),
                        modifier = Modifier.testTag("max_tokens_${tokens}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thinking Level / Depth Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inference Depth", fontSize = 13.sp, color = DarkTextPrimary, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(ThinkingLevel.LOW, ThinkingLevel.HIGH).forEach { level ->
                        val isSelected = uiState.selectedThinkingLevel == level
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GeminiPurple else DarkSurfaceVariant)
                                .clickable { onThinkingLevelChanged(level) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("inference_depth_${level.name}")
                        ) {
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else DarkTextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * 3. Retrieval-Augmented Generation (RAG) local document store panel
 */
@Composable
fun OfflineRagDocumentStoreCard(
    documents: List<DocumentEntity>,
    onToggleRag: (String, Boolean) -> Unit,
    onDeleteDoc: (String) -> Unit,
    onAddDocClick: () -> Unit
) {
    val enabledDocsCount = documents.count { it.isRagEnabled }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RAG LOCAL DOCUMENT STORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiCyan,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "$enabledDocsCount of ${documents.size} files active for grounding",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                    )
                }

                IconButton(
                    onClick = onAddDocClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeminiCyan.copy(alpha = 0.15f))
                        .testTag("rag_add_doc_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Document", tint = GeminiCyan, modifier = Modifier.size(18.dp))
                }
            }

            var docSearchQuery by remember { mutableStateOf("") }
            val filteredDocs = remember(documents, docSearchQuery) {
                if (docSearchQuery.isBlank()) {
                    documents
                } else {
                    documents.filter { it.title.contains(docSearchQuery, ignoreCase = true) || it.content.contains(docSearchQuery, ignoreCase = true) }
                }
            }

            if (documents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = docSearchQuery,
                    onValueChange = { docSearchQuery = it },
                    placeholder = { Text("Filter loaded documents...", color = DarkTextMuted, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(16.dp)) },
                    trailingIcon = if (docSearchQuery.isNotBlank()) {
                        {
                            IconButton(onClick = { docSearchQuery = "" }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = DarkTextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiCyan.copy(alpha = 0.6f),
                        unfocusedBorderColor = DarkOutlineVariant,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No documents loaded. Press '+' above to upload files.",
                        fontSize = 11.5.sp,
                        color = DarkTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No documents match \"$docSearchQuery\"",
                        fontSize = 11.5.sp,
                        color = DarkTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredDocs.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(0.8.dp, DarkOutlineVariant, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (doc.isRagEnabled) GeminiCyan else DarkTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = doc.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (doc.isRagEnabled) Color.White else DarkTextMuted,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${doc.fileSize} Bytes • ${doc.mimeType}",
                                        fontSize = 10.5.sp,
                                        color = DarkTextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = doc.isRagEnabled,
                                    onCheckedChange = { onToggleRag(doc.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GeminiCyan,
                                        checkedTrackColor = GeminiCyan.copy(alpha = 0.4f),
                                        uncheckedThumbColor = DarkTextMuted,
                                        uncheckedTrackColor = DarkOutlineVariant
                                    ),
                                    modifier = Modifier.scale(0.8f).testTag("rag_toggle_${doc.title}")
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { onDeleteDoc(doc.id) },
                                    modifier = Modifier.size(28.dp).testTag("rag_delete_${doc.title}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Chat History / Conversations Manager Card
 */
@Composable
fun OfflineChatSessionHistoryCard(
    conversations: List<ConversationEntity>,
    currentConvId: String,
    onSelectSession: (ConversationEntity) -> Unit,
    onNewSession: () -> Unit,
    onClearHistory: () -> Unit
) {
    val activeConv = conversations.find { it.id == currentConvId }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MANAGE CHAT HISTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GeminiEmerald,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = activeConv?.title?.let { "Active: $it" } ?: "Select or Start Session",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp),
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNewSession,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiEmerald.copy(alpha = 0.15f))
                            .testTag("session_new_button")
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "New Session", tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Red.copy(alpha = 0.1f))
                            .testTag("session_clear_all_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions found.", fontSize = 11.sp, color = DarkTextMuted)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(conversations) { conv ->
                        val isSelected = conv.id == currentConvId
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GeminiEmerald.copy(alpha = 0.15f) else DarkSurfaceVariant)
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) GeminiEmerald else DarkOutlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectSession(conv) }
                                .padding(8.dp)
                                .testTag("session_item_${conv.id.take(6)}")
                        ) {
                            Column {
                                Text(
                                    text = conv.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) Color.White else DarkTextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (conv.lastMessagePreview.isNotBlank()) conv.lastMessagePreview else "No messages",
                                    fontSize = 9.5.sp,
                                    color = DarkTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Common Actions Bar containing quick action templates
 */
@Composable
fun OfflineCommonActionsBar(
    viewModel: MainViewModel,
    uiState: UiState,
    onAnalyzeFileClick: () -> Unit,
    onGenerateSummaryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "QUICK TEMPLATES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = DarkTextSubtle,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButtonItem(
                title = "Analyze File",
                icon = Icons.Default.FolderOpen,
                accentColor = GeminiCyan,
                testTag = "offline_action_analyze_file",
                onClick = onAnalyzeFileClick
            )

            ActionButtonItem(
                title = "Summary Brief",
                icon = Icons.Default.Summarize,
                accentColor = GeminiPurple,
                testTag = "offline_action_generate_summary",
                onClick = onGenerateSummaryClick
            )

            ActionButtonItem(
                title = "Code Review",
                icon = Icons.Default.BugReport,
                accentColor = WarningOrange,
                testTag = "offline_action_code_fix",
                onClick = {
                    val codePrompt = "Review the following Kotlin code for asymptotic bottlenecks, memory leaks, and thread-safety:\n\n```kotlin\nfun findDuplicates(arr: IntArray): List<Int> {\n    val result = mutableListOf<Int>()\n    for (i in arr.indices) {\n        for (j in i + 1 until arr.size) {\n            if (arr[i] == arr[j] && !result.contains(arr[i])) {\n                result.add(arr[i])\n            }\n        }\n    }\n    return result\n}\n```\n\nExplain and provide O(N) optimization."
                    viewModel.setOfflinePromptInput(codePrompt)
                    viewModel.runOfflineLlmInference(codePrompt)
                }
            )

            ActionButtonItem(
                title = "Polish Text",
                icon = Icons.Default.Spellcheck,
                accentColor = GeminiBlueLight,
                testTag = "offline_action_proofread",
                onClick = {
                    val proofreadPrompt = "Polish and proofread the following text for business executive clarity:\n\n\"The on-device ai model are running very fast on samsung phone and it have zero token cost and no privacy issues at all.\""
                    viewModel.setOfflinePromptInput(proofreadPrompt)
                    viewModel.runOfflineLlmInference(proofreadPrompt)
                }
            )
        }
    }
}

@Composable
fun ActionButtonItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary,
                    fontSize = 12.5.sp
                )
            )
        }
    }
}

/**
 * Text Input Area Card for Prompts
 */
@Composable
fun OfflinePromptInputCard(
    promptInput: String,
    isGenerating: Boolean,
    onPromptChanged: (String) -> Unit,
    onRunInference: () -> Unit,
    onStopGenerating: () -> Unit,
    onClearInput: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROMPT INPUT AREA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlueLight,
                        letterSpacing = 1.sp
                    )
                )

                if (promptInput.isNotBlank()) {
                    IconButton(
                        onClick = onClearInput,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("offline_clear_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear prompt input",
                            tint = DarkTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = promptInput,
                onValueChange = onPromptChanged,
                placeholder = {
                    Text(
                        "Enter prompt, instructions, code snippet, or query for the offline LLM...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DarkTextSubtle,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    )
                },
                minLines = 3,
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_prompt_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GeminiBlue,
                    unfocusedBorderColor = DarkOutlineVariant,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Press 'Run' to execute on NPU",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextMuted,
                        fontSize = 11.5.sp
                    )
                )

                if (isGenerating) {
                    Button(
                        onClick = onStopGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("offline_btn_stop_inference")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Stop", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onRunInference,
                        enabled = promptInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeminiBlue,
                            disabledContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("offline_btn_run_inference")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (promptInput.isNotBlank()) Color.White else DarkTextSubtle
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Run Inference",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (promptInput.isNotBlank()) Color.White else DarkTextSubtle
                        )
                    }
                }
            }
        }
    }
}

/**
 * Display Area for Offline LLM Responses (Displays multi-turn timeline of active conversation!)
 */
@Composable
fun OfflineResponseDisplayCard(
    viewModel: MainViewModel,
    uiState: UiState,
    messages: List<com.example.data.ChatMessageEntity>,
    onClearResponse: () -> Unit,
    onCopyResponse: (String) -> Unit,
    onShareResponse: (String) -> Unit
) {
    val isTimelineEmpty = messages.isEmpty()
    val hasStreamingResponse = uiState.offlineResponseText.isNotBlank() || uiState.offlineThoughtText.isNotBlank() || uiState.isOfflineGenerating

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (uiState.isOfflineGenerating) GeminiBlue.copy(alpha = 0.6f) else DarkOutlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offline_response_display_area")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (uiState.isOfflineGenerating) GeminiCyan.copy(alpha = 0.2f) else GeminiEmerald.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isOfflineGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = GeminiCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GeminiEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (uiState.isOfflineGenerating) "Offline LLM Inference Active..." else "Response Display Area",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        )
                        if (uiState.offlineExecutionTimeMs > 0) {
                            Text(
                                text = "${uiState.offlineExecutionTimeMs} ms • ${String.format(java.util.Locale.US, "%.1f", uiState.offlineTokensPerSec)} tok/s",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeminiBlueLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }

                // Action controls (Clear, Copy, Share of latest response)
                if (hasStreamingResponse || !isTimelineEmpty) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val textToCopy = if (uiState.offlineResponseText.isNotBlank()) {
                                    uiState.offlineResponseText
                                } else {
                                    messages.lastOrNull { it.role == "assistant" }?.content ?: ""
                                }
                                onCopyResponse(textToCopy)
                            },
                            modifier = Modifier.size(32.dp).testTag("offline_btn_copy")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                val textToShare = if (uiState.offlineResponseText.isNotBlank()) {
                                    uiState.offlineResponseText
                                } else {
                                    messages.lastOrNull { it.role == "assistant" }?.content ?: ""
                                }
                                onShareResponse(textToShare)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onClearResponse,
                            modifier = Modifier.size(32.dp).testTag("offline_btn_clear")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display active messaging timeline if any messages exist in conversation history
            if (!isTimelineEmpty) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    messages.forEach { msg ->
                        val isUser = msg.role == "user"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            // Message Sender Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = if (isUser) GeminiBlueLight else GeminiEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isUser) "User Query" else msg.modelMode,
                                    fontSize = 11.sp,
                                    color = DarkTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Message Thought Process Accordion (for assistant)
                            if (!isUser && msg.thoughtProcess.isNotBlank()) {
                                val parsedSteps: List<com.example.engine.ReasoningStep> = remember(msg.reasoningStepsJson) {
                                    viewModel.thinkingManager.parseStepsJson(msg.reasoningStepsJson)
                                }
                                ThoughtAccordion(
                                    thoughtText = msg.thoughtProcess,
                                    isThinkingLive = false,
                                    durationMs = msg.thinkingDurationMs,
                                    thinkingTokens = msg.thinkingTokens,
                                    steps = parsedSteps,
                                    currentStep = null,
                                    onDebugNodes = { idx ->
                                        viewModel.openCotDebugOverlay(parsedSteps, idx)
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            // Message Bubble Content
                            Surface(
                                color = if (isUser) DarkSurfaceVariant else DarkSurfaceElevated,
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 0.8.dp,
                                    color = if (isUser) GeminiBlue.copy(alpha = 0.3f) else DarkOutlineVariant
                                ),
                                modifier = Modifier.fillMaxWidth(0.95f)
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    MarkdownContentView(
                                        content = msg.content,
                                        textColor = DarkTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Live stream generation (emits currently active generation if any is running)
            if (uiState.isOfflineGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = GeminiCyan, strokeWidth = 1.5.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.activeModelName} (Streaming...)",
                            fontSize = 11.sp,
                            color = GeminiCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Live thoughts
                    if (uiState.offlineThoughtText.isNotBlank()) {
                        ThoughtAccordion(
                            thoughtText = uiState.offlineThoughtText,
                            isThinkingLive = true,
                            durationMs = uiState.offlineExecutionTimeMs,
                            thinkingTokens = (uiState.offlineThoughtText.length / 4),
                            currentStep = uiState.offlineCurrentStep
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Live Answer
                    if (uiState.offlineResponseText.isNotBlank()) {
                        Surface(
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, GeminiBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                MarkdownContentView(
                                    content = uiState.offlineResponseText,
                                    textColor = DarkTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // If empty display placeholder
            if (isTimelineEmpty && !uiState.isOfflineGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                        .border(0.8.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = DarkTextSubtle,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inference Timeline Empty",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write a prompt or select a quick template above, then run to start an active reasoning session.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextSubtle,
                                fontSize = 11.5.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
