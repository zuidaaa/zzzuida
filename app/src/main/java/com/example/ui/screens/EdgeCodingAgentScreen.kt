package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.*
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

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
    var activeSubTab by remember { mutableStateOf(0) } // 0: Orchestrator & Live Loop, 1: Isolated Sandbox Files, 2: State Snapshots (KV-Cache)
    var generatedCodeResult by remember { mutableStateOf<String?>(null) }
    var selectedSandboxFile by remember { mutableStateOf("main.py") }

    val workspaceFiles = remember(isRunningPipeline) { edgeEngine.getWorkspaceFiles() }
    val persistedSnapshots by viewModel.workspaceSnapshotRepository.allSnapshots.collectAsState(initial = emptyList())
    val codeHistoryList by viewModel.workspaceSnapshotRepository.allCodeHistory.collectAsState(initial = emptyList())

    // State Management: Workspace Vectorization State
    var semanticSearchQuery by remember { mutableStateOf("") }
    var semanticSearchResults by remember { mutableStateOf<List<WorkspaceSemanticSearchResult>>(emptyList()) }
    var isComputingEmbeddings by remember { mutableStateOf(false) }
    var lastRestoredSnapshotId by remember { mutableStateOf<String?>(null) }
    var lastAstValidationReport by remember { mutableStateOf<com.example.engine.edge.AstValidationReport?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header
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
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edge-AI Multimodal Agent",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
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
                        style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                    )
                }
            }
        }

        // Subtabs: 0: Orchestrator, 1: Isolated Sandbox, 2: KV-Cache State Snapshots
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

        if (activeSubTab == 0) {
            // Hardware-Aware Telemetry Bar (Phase 2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Phase 2: Adaptive Hardware Telemetry",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                )
                            }
                            Text(
                                text = telemetry.thermalStatus,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (telemetry.thermalStatus == "NOMINAL") GeminiEmerald else WarningOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("CPU / NPU LOAD", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                                    Text("${telemetry.cpuUtilizationPercent.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = VibrantTeal))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("BATTERY TEMP", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                                    Text("${telemetry.batteryTemperatureCelsius}°C", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("NPU HEADROOM", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                                    Text("${telemetry.npuThermalHeadroom.toInt()}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Routing: ${telemetry.recommendedTarget.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(color = GeminiCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Complexity: ${(telemetry.complexityScore * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall.copy(color = VibrantTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Phase 0: Quantization & Agent Core Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Phase 0: Quantization & Mobile Runtime (llama.cpp)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(ModelQuantization.INT4, ModelQuantization.INT3, ModelQuantization.INT2).forEach { quant ->
                                val isSelected = selectedQuantization == quant
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) VibrantTeal.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) VibrantTeal else DarkOutlineVariant, RoundedCornerShape(8.dp))
                                        .clickable { selectedQuantization = quant }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = quant.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) VibrantTeal else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${quant.throughputTpsExynos2600} t/s",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Prompt & Multimodal Controls (Vision + Speech-to-Code)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Unified Multimodal Input (Text + UI Mockup + Speech)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            label = { Text("Task Description / Code Prompt", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
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
                            Icon(Icons.Default.Mic, contentDescription = null, tint = VibrantPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Whisper STT Audio Stream",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = VibrantPurple)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(
                                checked = isSimulatingVoice,
                                onCheckedChange = { isSimulatingVoice = it },
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
                                onValueChange = { voiceTranscriptInput = it },
                                label = { Text("Live Audio Transcript (Whisper)", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
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
                            onClick = {
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
                            },
                            enabled = !isRunningPipeline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("run_edge_pipeline_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal)
                        ) {
                            if (isRunningPipeline) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Executing Edge Pipeline (Phase 0-3)...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Edge Multimodal Coding Loop", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Live Pipeline Events Stream (Phases 0, 1, 2, 3)
            if (pipelineEvents.isNotEmpty()) {
                item {
                    Text(
                        text = "PIPELINE EXECUTION TRACE",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    )
                }

                items(pipelineEvents) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                event.isFailed -> Color(0xFFFF5252).copy(alpha = 0.5f)
                                event.isCompleted -> GeminiEmerald.copy(alpha = 0.4f)
                                else -> VibrantTeal.copy(alpha = 0.4f)
                            }
                        )
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
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (event.phase) {
                                                    0 -> GeminiBlue
                                                    1 -> VibrantPurple
                                                    2 -> WarningOrange
                                                    else -> VibrantTeal
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("P${event.phase}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = event.stepTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }

                                if (event.isCompleted) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                                } else if (event.isRunning) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = VibrantTeal, strokeWidth = 2.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = event.detail, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.5.sp))

                            if (event.payloadPreview.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = event.payloadPreview,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            color = GeminiCyan,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 4
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Generated Code Preview
            generatedCodeResult?.let { code ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Edge-Synthesized Code (AST Verified)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald)
                                )
                                Text("EdgeGeneratedDashboard.kt", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = code,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF00FF66),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

        } else if (activeSubTab == 1) {
            // Subtab 1: Isolated Sandbox Files & AST Inspector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Isolated JNI / Rust Virtual Sandbox Workspace",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Files Tab Selector
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(workspaceFiles.keys.toList()) { fileName ->
                                val isSelected = fileName == selectedSandboxFile
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) GeminiBlue.copy(alpha = 0.25f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isSelected) GeminiBlue else DarkOutlineVariant, RoundedCornerShape(8.dp))
                                        .clickable { selectedSandboxFile = fileName }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) GeminiBlueLight else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val currentContent = workspaceFiles[selectedSandboxFile] ?: "// File is empty"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, DarkOutlineVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = currentContent,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trigger AST Validation Button (Phase 2 AST Pre-Execution Validator)
                        Button(
                            onClick = {
                                val report = edgeEngine.orchestrator.validateCodeSafety(
                                    currentContent,
                                    language = if (selectedSandboxFile.endsWith(".kt")) "kotlin" else "python"
                                )
                                lastAstValidationReport = report
                                coroutineScope.launch {
                                    edgeEngine.executeToolAction(
                                        """{"action": "run_tests", "path": "$selectedSandboxFile"}"""
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run AST Pre-Execution Validator & Unit Tests", color = Color.White, fontSize = 12.sp)
                        }

                        // Display AST Report if generated
                        lastAstValidationReport?.let { report ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = if (report.isValid) DarkSurfaceElevated else Color(0xFF331414)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (report.isValid) GeminiEmerald.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (report.isValid) "AST Safety Check: PASSED" else "AST Safety Violations Detected",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (report.isValid) GeminiEmerald else Color(0xFFFF5252),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${report.astNodeCount} Nodes • ${report.checkedLanguage.uppercase()}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.sp)
                                        )
                                    }
                                    if (report.violations.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        report.violations.forEach { violation ->
                                            Text(text = "• $violation", color = Color(0xFFFF8A80), fontSize = 11.sp)
                                        }
                                    }
                                    if (report.warnings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        report.warnings.forEach { warning ->
                                            Text(text = "⚠ $warning", color = WarningOrange, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // Subtab 2: State Management & KV-Cache Snapshots
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KV-Cache State Snapshots & Context Memory",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                )
                                Text(
                                    text = "Eliminates state loss across app lifecycle switches",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                                )
                            }
                            Button(
                                onClick = {
                                    edgeEngine.createWorkspaceSnapshot("Manual checkpoint snapshot")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple)
                            ) {
                                Text("New Snapshot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (persistedSnapshots.isEmpty()) {
                            Text("No snapshots persisted in Room yet. Run a coding loop or click 'New Snapshot'.", color = DarkTextMuted, fontSize = 12.sp)
                        } else {
                            persistedSnapshots.forEach { snap ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceElevated)
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (snap.isPinned) {
                                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = GeminiAmber, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(snap.snapshotId, style = MaterialTheme.typography.labelSmall.copy(color = VibrantPurple, fontWeight = FontWeight.Bold))
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${snap.serializedKvCacheBytes / 1024} KB KV-Cache", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            viewModel.workspaceSnapshotRepository.togglePinSnapshot(snap.snapshotId)
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        if (snap.isPinned) Icons.Default.PushPin else Icons.Default.BookmarkBorder,
                                                        contentDescription = "Pin",
                                                        tint = if (snap.isPinned) GeminiAmber else DarkTextMuted,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            val files = viewModel.workspaceSnapshotRepository.restoreSnapshotFiles(snap.snapshotId)
                                                            if (files.isNotEmpty()) {
                                                                edgeEngine.restoreWorkspaceSnapshot(files, snap.summary)
                                                                lastRestoredSnapshotId = snap.snapshotId
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Restore,
                                                        contentDescription = "Restore Snapshot",
                                                        tint = if (lastRestoredSnapshotId == snap.snapshotId) GeminiEmerald else GeminiBlueLight,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            viewModel.workspaceSnapshotRepository.deleteSnapshot(snap.snapshotId)
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VibrantRed, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(snap.summary, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.5.sp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Model: ${snap.modelTag} • Checksum: 0x${snap.vectorEmbeddingChecksum} • ${snap.filesCount} files", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.sp))
                                        if (lastRestoredSnapshotId == snap.snapshotId) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("✓ Active Workspace Restored to this state", style = MaterialTheme.typography.bodySmall.copy(color = GeminiEmerald, fontSize = 9.5.sp, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }

                        // State Management: Workspace Vectorization Card (Critical Design Aspect 2)
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Hub, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Workspace Vectorization (Zero-Fullcode Transfer)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = VibrantTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isComputingEmbeddings = true
                                                edgeEngine.refreshWorkspaceEmbeddings()
                                                if (semanticSearchQuery.isNotBlank()) {
                                                    semanticSearchResults = edgeEngine.searchWorkspaceSemantics(semanticSearchQuery, topK = 3)
                                                }
                                                isComputingEmbeddings = false
                                            }
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (isComputingEmbeddings) "Computing..." else "Index Embeddings (128-D)", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Captures file semantics via compact local embedding models to feed precise context into the prompt stream instead of transmitting entire bulky source trees.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.5.sp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = semanticSearchQuery,
                                        onValueChange = { semanticSearchQuery = it },
                                        placeholder = { Text("Query semantics (e.g. 'fibonacci', 'unit test', 'dashboard')", color = DarkTextMuted, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = VibrantTeal,
                                            unfocusedBorderColor = DarkOutlineVariant,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                semanticSearchResults = edgeEngine.searchWorkspaceSemantics(
                                                    semanticSearchQuery.ifBlank { "main function" },
                                                    topK = 3
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(VibrantTeal.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = "Semantic Search", tint = VibrantTeal, modifier = Modifier.size(18.dp))
                                    }
                                }

                                if (semanticSearchResults.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Top Semantic Matches (Cosine Similarity):", style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    semanticSearchResults.forEach { res ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                                .background(DarkSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                .padding(6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(res.filePath, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.5.sp))
                                                Text(res.matchingSnippet, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 9.5.sp), maxLines = 1)
                                            }
                                            Text(
                                                text = "${(res.similarityScore * 100f).coerceIn(0f, 100f).toInt()}% match",
                                                style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (codeHistoryList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Code Generation & Refinement History (${codeHistoryList.size} Runs)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            codeHistoryList.take(5).forEach { history ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.4f))
                                        .border(1.dp, if (history.astValidationPassed) GeminiEmerald.copy(alpha = 0.3f) else VibrantRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${history.language.uppercase()} • ${history.modelQuantization} • ${history.executionTarget}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            )
                                            Text(
                                                text = if (history.astValidationPassed) "✓ AST Verified" else "✗ AST Error",
                                                style = MaterialTheme.typography.labelSmall.copy(color = if (history.astValidationPassed) GeminiEmerald else VibrantRed, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = history.prompt,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.sp),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text("Sandbox Exit: ${history.sandboxExitCode} • ${history.sandboxExecutionMs}ms • Iteration ${history.refinementIteration}", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 9.5.sp))
                                    }
                                }
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
