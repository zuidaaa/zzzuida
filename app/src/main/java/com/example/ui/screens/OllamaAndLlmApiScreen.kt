package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.engine.llmapi.UniversalLlmApiClient
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.ollama.OllamaModel
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.UiState
import com.example.data.LlmDatasetEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OllamaAndLlmApiScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ollama, 1: LLM API Gateway, 2: Training Datasets

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
    ) {
        // Tab Header
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xCC161B22), // rgba(22, 27, 34, 0.8)
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🦙 Ollama Host", fontWeight = FontWeight.Bold)
                        if (uiState.ollamaStatus.isConnected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(VibrantGreen)
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("tab_ollama")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡ Universal LLM API", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_llm_api")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊 Training Datasets", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("tab_training_datasets")
            )
        }

        when (selectedTab) {
            0 -> OllamaSection(viewModel = viewModel, uiState = uiState)
            1 -> UniversalLlmApiSection(viewModel = viewModel, uiState = uiState)
            2 -> LlmTrainingDatasetsSection(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun OllamaSection(
    viewModel: MainViewModel,
    uiState: UiState
) {
    var hostInput by remember { mutableStateOf(uiState.ollamaHostUrl) }
    var pullModelInput by remember { mutableStateOf("deepseek-r1:7b") }
    var testPrompt by remember { mutableStateOf("Explain how backpropagation computes gradients in 2 concise sentences.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Connection & Status Card
        item {
            GlassPanel(
                modifier = Modifier.border(1.dp, if (uiState.ollamaStatus.isConnected) VibrantGreen.copy(alpha = 0.3f) else VibrantRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (uiState.ollamaStatus.isConnected) VibrantGreen else VibrantRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.ollamaStatus.isConnected) "OLLAMA CONNECTED (${uiState.ollamaStatus.version})" else "DISCONNECTED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (uiState.ollamaStatus.isConnected) VibrantGreen else VibrantRed,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (uiState.ollamaStatus.isConnected) {
                            Text(
                                text = "${uiState.ollamaStatus.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Ollama Daemon URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ollama_host_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuminousBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setOllamaHostUrl(hostInput)
                                viewModel.checkOllamaHealth()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ollama_connect_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect / Refresh")
                        }

                        OutlinedButton(
                            onClick = {
                                hostInput = "http://10.0.2.2:11434"
                                viewModel.setOllamaHostUrl("http://10.0.2.2:11434")
                                viewModel.checkOllamaHealth()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LuminousBlue),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue),
                            modifier = Modifier.testTag("ollama_preset_emulator_button")
                        ) {
                            Text("Emulator Host")
                        }
                    }

                    if (uiState.ollamaStatus.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.ollamaStatus.errorMessage,
                            color = VibrantRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Pull Model Section
        item {
            GlassPanel {
                Column {
                    Text(
                        text = "Pull Model from Ollama Registry",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pullModelInput,
                            onValueChange = { pullModelInput = it },
                            placeholder = { Text("e.g. deepseek-r1:7b, llama3.2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantTeal,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = AbsoluteBlack,
                                unfocusedContainerColor = AbsoluteBlack
                            )
                        )
                        Button(
                            onClick = { viewModel.pullOllamaModel(pullModelInput) },
                            enabled = !uiState.ollamaIsPulling,
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pull", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.ollamaIsPulling) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.ollamaPullProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = VibrantTeal,
                            trackColor = BorderSubtle
                        )
                        Text(
                            text = uiState.ollamaPullStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = VibrantTeal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Installed Models Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Installed Ollama Models (${uiState.ollamaModels.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Active: ${uiState.selectedOllamaModel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepThinkIndigoLight
                )
            }
        }

        // Models List
        if (uiState.ollamaModels.isEmpty()) {
            item {
                Text(
                    text = "No models discovered. Ensure Ollama daemon is running (`ollama serve`) and tap Refresh.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkTextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(uiState.ollamaModels) { model ->
                OllamaModelCard(
                    model = model,
                    isSelected = model.name == uiState.selectedOllamaModel,
                    onSelect = { viewModel.selectOllamaModel(model.name) },
                    onDelete = { viewModel.deleteOllamaModel(model.name) }
                )
            }
        }

        // Live Chat / Prompt Generation Sandbox
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigo.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ollama Inference Sandbox",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = testPrompt,
                        onValueChange = { testPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.executeOllamaInference(testPrompt) },
                        enabled = !uiState.ollamaIsGenerating,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo)
                    ) {
                        if (uiState.ollamaIsGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating token stream...")
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute Inference with ${uiState.selectedOllamaModel}")
                        }
                    }

                    if (uiState.ollamaLiveThought.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🧠 Model Reasoning Trace (<think>):",
                            style = MaterialTheme.typography.labelSmall.copy(color = DeepThinkCyan, fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = DarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = uiState.ollamaLiveThought,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = DarkTextMuted),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (uiState.ollamaLiveResponse.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Output Response:",
                            style = MaterialTheme.typography.labelSmall.copy(color = DeepThinkEmeraldLight, fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = DarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = uiState.ollamaLiveResponse,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OllamaModelCard(
    model: OllamaModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.border(
            1.dp,
            if (isSelected) LuminousBlue else BorderSubtle,
            RoundedCornerShape(12.dp)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(model.sizeFormatted, fontSize = 11.sp, color = TextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                    )
                    if (model.parameterSize.isNotBlank()) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(model.parameterSize, fontSize = 11.sp, color = TextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) VibrantGreen else LuminousBlue
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isSelected) "Active" else "Select", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Model", tint = VibrantRed)
            }
        }
    }
}

@Composable
private fun UniversalLlmApiSection(
    viewModel: MainViewModel,
    uiState: UiState
) {
    var baseUrl by remember { mutableStateOf(uiState.llmApiConfig.baseUrl) }
    var apiKey by remember { mutableStateOf(uiState.llmApiConfig.apiKey) }
    var modelName by remember { mutableStateOf(uiState.llmApiConfig.model) }
    var queryInput by remember { mutableStateOf("Write a production Kotlin binary search function with asymptotic invariant proofs.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preset LLM API Gateways",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UniversalLlmApiClient.PRESET_PROVIDERS.take(3).forEach { provider ->
                    FilterChip(
                        selected = uiState.llmApiConfig.providerId == provider.id,
                        onClick = {
                            baseUrl = provider.defaultBaseUrl
                            modelName = provider.defaultModel
                            viewModel.updateLlmApiConfig(
                                uiState.llmApiConfig.copy(
                                    providerId = provider.id,
                                    baseUrl = provider.defaultBaseUrl,
                                    model = provider.defaultModel
                                )
                            )
                        },
                        label = { Text(provider.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Endpoint Configuration",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            viewModel.updateLlmApiConfig(uiState.llmApiConfig.copy(baseUrl = it))
                        },
                        label = { Text("Base URL (OpenAI Compatible /v1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = modelName,
                        onValueChange = {
                            modelName = it
                            viewModel.updateLlmApiConfig(uiState.llmApiConfig.copy(model = it))
                        },
                        label = { Text("Model Name (e.g. deepseek-reasoner, gpt-4o, claude-3-7-sonnet)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            viewModel.updateLlmApiConfig(uiState.llmApiConfig.copy(apiKey = it))
                        },
                        label = { Text("API Key (Bearer Auth)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testLlmApiEndpoint() },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Endpoint", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.llmApiTestResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.llmApiTestResult,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.llmApiTestSuccess) DeepThinkEmeraldLight else DeepThinkRose
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigo.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Execute API Completion",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = { queryInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.executeLlmApiCompletion(queryInput) },
                        enabled = !uiState.llmApiIsCalling,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo)
                    ) {
                        if (uiState.llmApiIsCalling) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Querying $modelName...")
                        } else {
                            Text("Send to $modelName")
                        }
                    }

                    if (uiState.llmApiLatestResponse != null) {
                        val resp = uiState.llmApiLatestResponse
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(onClick = {}, label = { Text("Latency: ${resp.latencyMs}ms") })
                            SuggestionChip(onClick = {}, label = { Text("Tokens: ${resp.totalTokens}") })
                        }

                        if (resp.thoughtProcess.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Deep Reasoning Trace:", color = DeepThinkCyan, style = MaterialTheme.typography.labelSmall)
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = resp.thoughtProcess,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = DarkTextMuted),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        if (resp.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Completion Answer:", color = DeepThinkEmeraldLight, style = MaterialTheme.typography.labelSmall)
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(
                                    text = resp.content,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LlmTrainingDatasetsSection(
    viewModel: MainViewModel,
    uiState: UiState
) {
    val datasets by viewModel.datasetsList.collectAsStateWithLifecycle()

    var nameInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var formatInput by remember { mutableStateOf("JSON") } // JSON, JSONL, CSV, TXT
    var rawContentInput by remember { mutableStateOf("") }
    var purposeInput by remember { mutableStateOf("Supervised Fine-Tuning (SFT)") } // SFT, DPO, Pre-training
    var modelTargetInput by remember { mutableStateOf("deepseek-r1-7b") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dataset Upload Form Card
        item {
            GlassPanel(
                modifier = Modifier.border(1.dp, LuminousBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Column {
                    Text(
                        text = "📤 Upload Fine-Tuning Dataset",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Dataset Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuminousBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Dataset Description / Purpose") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuminousBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = formatInput,
                            onValueChange = { formatInput = it },
                            label = { Text("Format (JSON/CSV/JSONL)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminousBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = AbsoluteBlack,
                                unfocusedContainerColor = AbsoluteBlack
                            )
                        )
                        OutlinedTextField(
                            value = modelTargetInput,
                            onValueChange = { modelTargetInput = it },
                            label = { Text("Target LLM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminousBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = AbsoluteBlack,
                                unfocusedContainerColor = AbsoluteBlack
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawContentInput,
                        onValueChange = { rawContentInput = it },
                        label = { Text("Raw Dataset Content (Paste JSON / CSV / JSONL list)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        minLines = 4,
                        placeholder = { 
                            Text(
                                "[\n  {\"prompt\": \"Query 1\", \"completion\": \"Answer 1\"},\n  {\"prompt\": \"Query 2\", \"completion\": \"Answer 2\"}\n]"
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuminousBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && rawContentInput.isNotBlank()) {
                                viewModel.uploadDataset(
                                    name = nameInput,
                                    description = descInput,
                                    fileFormat = formatInput,
                                    rawContent = rawContentInput,
                                    purpose = purposeInput,
                                    modelTarget = modelTargetInput
                                )
                                nameInput = ""
                                descInput = ""
                                rawContentInput = ""
                            }
                        },
                        enabled = nameInput.isNotBlank() && rawContentInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Upload Dataset to Room SQLite", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }

        // Uploaded Datasets Headers
        item {
            Text(
                text = "📚 Uploaded Training Datasets (${datasets.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
        }

        if (datasets.isEmpty()) {
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No datasets uploaded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Fill the form above to persist SFT/DPO datasets locally in SQLite.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextMuted
                        )
                    }
                }
            }
        } else {
            items(datasets) { dataset ->
                DatasetItemCard(
                    dataset = dataset,
                    onDelete = { viewModel.deleteTrainingDataset(dataset.id) }
                )
            }
        }
    }
}

@Composable
private fun DatasetItemCard(
    dataset: com.example.data.LlmDatasetEntity,
    onDelete: () -> Unit
) {
    GlassPanel(
        modifier = Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dataset.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    if (dataset.description.isNotBlank()) {
                        Text(
                            text = dataset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = VibrantRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("Entries: ${dataset.entryCount}", color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                )
                SuggestionChip(
                    onClick = {},
                    label = { Text("Format: ${dataset.fileFormat}", color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "File Contents Preview:",
                style = MaterialTheme.typography.labelSmall,
                color = LuminousBlue
            )
            Surface(
                color = AbsoluteBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                val previewText = if (dataset.rawContent.length > 200) {
                    dataset.rawContent.take(197) + "..."
                } else {
                    dataset.rawContent
                }
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = TextSecondary),
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}
