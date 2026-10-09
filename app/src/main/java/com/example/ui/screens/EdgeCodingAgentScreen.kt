package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.*
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.screens.edge.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Screen: Edge-AI Multimodal Coding Agent.
 *
 * Coordinates on-device multimodal code synthesis across:
 * - Tab 0: Live Pipeline (Hardware Telemetry, Quantization, Multimodal Input, Pipeline Timeline)
 * - Tab 1: Isolated Sandbox (Virtual workspace files, AST Safety Pre-Execution Inspector, Unit Tests)
 * - Tab 2: KV Snapshots (State serialization, Room SQLite persistence, 128-D workspace vectorization)
 */
@Composable
fun EdgeCodingAgentScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val edgeEngine = remember { viewModel.edgeCodingEngine }

    val telemetry by edgeEngine.telemetryState.collectAsState()
    val pipelineEvents by edgeEngine.pipelineEvents.collectAsState()
    var selectedQuantization by remember { mutableStateOf(ModelQuantization.INT4) }
    var promptInput by remember { mutableStateOf("Synthesize dynamic Jetpack Compose Dashboard with hardware telemetry cards and responsive layout") }
    var voiceTranscriptInput by remember { mutableStateOf("Add a 120Hz refresh sync indicator with green highlight") }
    var isSimulatingVoice by remember { mutableStateOf(false) }
    var isRunningPipeline by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Live Pipeline, 1: Isolated Sandbox, 2: KV Snapshots
    var generatedCodeResult by remember { mutableStateOf<String?>(null) }
    var selectedSandboxFile by remember { mutableStateOf("main.py") }

    val workspaceFiles = remember(isRunningPipeline) { edgeEngine.getWorkspaceFiles() }
    val persistedSnapshots by viewModel.workspaceSnapshotRepository.allSnapshots.collectAsState(initial = emptyList())
    val codeHistoryList by viewModel.workspaceSnapshotRepository.allCodeHistory.collectAsState(initial = emptyList())

    // Vectorization and AST inspection state
    var semanticSearchQuery by remember { mutableStateOf("") }
    var semanticSearchResults by remember { mutableStateOf<List<WorkspaceSemanticSearchResult>>(emptyList()) }
    var isComputingEmbeddings by remember { mutableStateOf(false) }
    var lastRestoredSnapshotId by remember { mutableStateOf<String?>(null) }
    var lastAstValidationReport by remember { mutableStateOf<AstValidationReport?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VibrantTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Terminal Icon",
                                tint = VibrantTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edge-AI Multimodal Agent",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        )
                    }
                    Text(
                        text = "llama.cpp • Gemma-4-2B (PEFT INT4/INT3) • LiteViT5 Vision • Whisper STT",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.5.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GeminiEmerald.copy(alpha = 0.15f))
                        .border(1.dp, GeminiEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "EDGE NATIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GeminiEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
        }

        // Subtabs Navigation: 0: Live Pipeline, 1: Isolated Sandbox, 2: KV Snapshots
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    0 to "Live Pipeline",
                    1 to "Isolated Sandbox",
                    2 to "KV Snapshots (${persistedSnapshots.size})"
                ).forEach { (idx, label) ->
                    val isSelected = activeSubTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) VibrantTeal else Color.Transparent)
                            .clickable { activeSubTab = idx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else DarkTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                // Telemetry Dashboard (Phase 2)
                item {
                    EdgeTelemetryCard(telemetry = telemetry)
                }

                // Quantization Selector (Phase 0)
                item {
                    EdgeQuantizationCard(
                        selectedQuantization = selectedQuantization,
                        onSelectQuantization = { selectedQuantization = it }
                    )
                }

                // Unified Multimodal Input & Audio Stream (Phase 1 & 3)
                item {
                    EdgeMultimodalInputCard(
                        promptInput = promptInput,
                        onPromptChange = { promptInput = it },
                        isSimulatingVoice = isSimulatingVoice,
                        onToggleVoice = { isSimulatingVoice = it },
                        voiceTranscriptInput = voiceTranscriptInput,
                        onVoiceTranscriptChange = { voiceTranscriptInput = it },
                        isRunningPipeline = isRunningPipeline,
                        onRunPipeline = {
                            if (!isRunningPipeline) {
                                isRunningPipeline = true
                                coroutineScope.launch {
                                    val code = edgeEngine.runFullMultimodalCodingPipeline(
                                        userPrompt = promptInput,
                                        quantization = selectedQuantization,
                                        mockupBitmap = null,
                                        voiceTranscript = if (isSimulatingVoice) voiceTranscriptInput else null,
                                        onEvent = { }
                                    )
                                    generatedCodeResult = code
                                    isRunningPipeline = false
                                }
                            }
                        }
                    )
                }

                // Pipeline Execution Trace Events Stream
                if (pipelineEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "PIPELINE EXECUTION TRACE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DarkTextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }

                    items(pipelineEvents) { event ->
                        EdgePipelineEventCard(event = event)
                    }
                }

                // Generated Code Output Card
                generatedCodeResult?.let { code ->
                    item {
                        EdgeGeneratedCodeCard(code = code)
                    }
                }
            }

            1 -> {
                // Tab 1: Isolated Sandbox & AST Pre-Execution Validator
                item {
                    EdgeSandboxTab(
                        workspaceFiles = workspaceFiles,
                        selectedFile = selectedSandboxFile,
                        onSelectFile = { selectedSandboxFile = it },
                        onRunAstValidation = { content, language ->
                            val report = EdgeAstSafetyValidator.validate(content, language)
                            lastAstValidationReport = report
                            coroutineScope.launch {
                                edgeEngine.executeToolAction(
                                    """{"action": "run_tests", "path": "$selectedSandboxFile"}"""
                                )
                            }
                        },
                        astReport = lastAstValidationReport
                    )
                }
            }

            2 -> {
                // Tab 2: KV Snapshots & Workspace Vectorization
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            EdgeKvSnapshotsList(
                                snapshots = persistedSnapshots,
                                lastRestoredSnapshotId = lastRestoredSnapshotId,
                                onCreateSnapshot = {
                                    edgeEngine.createWorkspaceSnapshot("Manual checkpoint snapshot")
                                },
                                onTogglePin = { snapshotId ->
                                    coroutineScope.launch {
                                        viewModel.workspaceSnapshotRepository.togglePinSnapshot(snapshotId)
                                    }
                                },
                                onRestoreSnapshot = { snapshotId, summary ->
                                    coroutineScope.launch {
                                        val files = viewModel.workspaceSnapshotRepository.restoreSnapshotFiles(snapshotId)
                                        if (files.isNotEmpty()) {
                                            edgeEngine.restoreWorkspaceSnapshot(files, summary)
                                            lastRestoredSnapshotId = snapshotId
                                        }
                                    }
                                },
                                onDeleteSnapshot = { snapshotId ->
                                    coroutineScope.launch {
                                        viewModel.workspaceSnapshotRepository.deleteSnapshot(snapshotId)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            EdgeVectorizationCard(
                                isComputingEmbeddings = isComputingEmbeddings,
                                onIndexEmbeddings = {
                                    coroutineScope.launch {
                                        isComputingEmbeddings = true
                                        edgeEngine.refreshWorkspaceEmbeddings()
                                        if (semanticSearchQuery.isNotBlank()) {
                                            semanticSearchResults = edgeEngine.searchWorkspaceSemantics(semanticSearchQuery, topK = 3)
                                        }
                                        isComputingEmbeddings = false
                                    }
                                },
                                searchQuery = semanticSearchQuery,
                                onSearchQueryChange = { semanticSearchQuery = it },
                                onExecuteSearch = {
                                    coroutineScope.launch {
                                        semanticSearchResults = edgeEngine.searchWorkspaceSemantics(
                                            semanticSearchQuery.ifBlank { "main function" },
                                            topK = 3
                                        )
                                    }
                                },
                                searchResults = semanticSearchResults
                            )

                            if (codeHistoryList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                EdgeCodeHistoryList(codeHistoryList = codeHistoryList)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
