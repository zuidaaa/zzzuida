package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessageEntity
import com.example.data.PresetCatalog
import com.example.engine.ReasoningStep
import com.example.engine.SearchCitation
import com.example.engine.ThinkingLevel
import com.example.engine.ThoughtTraceExportData
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    messages: List<ChatMessageEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showSandboxesDialog by remember { mutableStateOf(false) }

    // Auto scroll on new messages or during generation
    LaunchedEffect(messages.size, uiState.liveAnswerText, uiState.liveThoughtText) {
        if (messages.isNotEmpty() || uiState.isGenerating) {
            val targetIndex = (messages.size + if (uiState.isGenerating) 1 else 0) - 1
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    // Modal dialog for visual reasoning tree inspection
    uiState.inspectingSteps?.let { steps ->
        ThoughtTreeDialog(
            steps = steps,
            onDismiss = { viewModel.closeInspectDialog() },
            onOpenDebugOverlay = { stepIndex ->
                viewModel.openCotDebugOverlay(steps, stepIndex)
            }
        )
    }

    // Modal dialog for Chain-of-Thought step debugger overlay
    if (uiState.isCotDebugOverlayOpen) {
        ChainOfThoughtDebugOverlay(
            steps = uiState.cotDebugSteps,
            initialStepIndex = uiState.cotDebugInitialStep,
            modelName = uiState.activeModelName,
            localDocuments = uiState.localDocuments,
            onDismiss = { viewModel.closeCotDebugOverlay() }
        )
    }

    // Modal dialog for user feedback rating
    uiState.feedbackMessageTarget?.let { target ->
        FeedbackDialog(
            targetMessage = target,
            currentRating = uiState.feedbackRatingInput,
            currentText = uiState.feedbackTextInput,
            onRatingChanged = { viewModel.setFeedbackRatingInput(it) },
            onTextChanged = { viewModel.setFeedbackTextInput(it) },
            onSubmit = { viewModel.submitFeedback() },
            onDismiss = { viewModel.closeFeedbackDialog() }
        )
    }

    // Modal Voice Conversation BottomSheet
    if (uiState.isVoiceConversationOpen) {
        VoiceConversationSheet(
            voiceManager = viewModel.voiceConversationManager,
            onDismiss = { viewModel.closeVoiceConversation() },
            onOpenCotDebugger = {
                val latestAssistant = messages.lastOrNull { it.role == "assistant" && it.reasoningStepsJson.isNotBlank() }
                if (latestAssistant != null) {
                    val steps = viewModel.thinkingManager.parseStepsJson(latestAssistant.reasoningStepsJson)
                    if (steps.isNotEmpty()) {
                        viewModel.openCotDebugOverlay(steps, 0)
                    }
                }
            }
        )
    }

    // Modal Audio & Speech Transcriber BottomSheet
    if (uiState.isAudioTranscriberOpen) {
        TranscribeAudioSheet(
            transcriptionManager = viewModel.audioTranscriptionManager,
            onSendTranscriptToEngine = { transcript ->
                inputText = transcript
                viewModel.sendMessage(transcript)
            },
            onDismiss = { viewModel.closeAudioTranscriber() }
        )
    }

    // Modal Quick ML Kit + Gemini Translator Sheet
    if (uiState.isQuickTranslatorSheetOpen) {
        QuickTranslatorSheet(
            viewModel = viewModel,
            uiState = uiState,
            onDismiss = { viewModel.closeQuickTranslator() }
        )
    }

    // Modal Export Thought Trace & AI Response Dialog (Markdown / PDF)
    if (uiState.isExportDialogOpen && uiState.exportTargetData != null) {
        ExportThoughtTraceDialog(
            exportData = uiState.exportTargetData,
            onDismiss = { viewModel.closeExportDialog() }
        )
    }

    // Modal Reasoning Sandboxes Dialog
    if (showSandboxesDialog) {
        AlertDialog(
            onDismissRequest = { showSandboxesDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { showSandboxesDialog = false },
                    modifier = Modifier.heightIn(min = 44.dp)
                ) {
                    Text("Close", color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxSize()) {
                    ReasoningSandboxesScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(0.95f)
        )
    }

    // Modal Custom Chatbot Role / System Instruction Dialog
    if (uiState.isCustomRoleDialogOpen) {
        var customPromptText by remember(uiState.systemInstruction) {
            mutableStateOf(
                if (uiState.systemInstruction.isNotBlank()) uiState.systemInstruction
                else viewModel.getRoleDefaultPrompt(uiState.selectedChatbotRole)
            )
        }
        AlertDialog(
            onDismissRequest = { viewModel.closeCustomRoleDialog() },
            title = {
                Text(
                    text = "Chatbot Role & System Instruction",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = "Customize the chatbot persona, instructions, and response behavior for Gemini multi-turn conversations.",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = customPromptText,
                        onValueChange = { customPromptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp)
                            .testTag("custom_role_instruction_input"),
                        placeholder = { Text("Enter custom role system instruction...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepThinkIndigoLight,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setSystemInstruction(customPromptText)
                        viewModel.selectChatbotRole("Custom")
                        viewModel.closeCustomRoleDialog()
                    },
                    modifier = Modifier.testTag("save_custom_role_button")
                ) {
                    Text("Apply Role", color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val defaultPrompt = viewModel.getRoleDefaultPrompt("General Assistant")
                        viewModel.setSystemInstruction(defaultPrompt)
                        viewModel.selectChatbotRole("General Assistant")
                        viewModel.closeCustomRoleDialog()
                    }
                ) {
                    Text("Reset", color = DarkTextMuted)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
    ) {
        // Atmospheric Ambient Glow background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LuminousBlue.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 60.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(VibrantPurple.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Controls Bar (Thinking Level & Mode & Search Grounding)
            ThinkingControlsBar(
                selectedLevel = uiState.selectedThinkingLevel,
                engineMode = uiState.engineMode,
                searchGroundingEnabled = uiState.searchGroundingEnabled,
                activeModelName = if (uiState.engineMode == com.example.engine.EngineMode.ONLINE_GEMINI_API) uiState.selectedGeminiModel else uiState.activeModelName,
                selectedGeminiModel = uiState.selectedGeminiModel,
                selectedChatbotRole = uiState.selectedChatbotRole,
                autoModelRoutingEnabled = uiState.autoModelRoutingEnabled,
                lastRoutingBadge = uiState.lastRoutingBadge,
                onModelSelected = { viewModel.selectGeminiModel(it) },
                onEnableAutoRouting = { viewModel.enableAutoRouting() },
                onRoleSelected = { viewModel.selectChatbotRole(it) },
                onOpenCustomRoleDialog = { viewModel.openCustomRoleDialog() },
                onLevelSelected = { viewModel.setThinkingLevel(it) },
                onToggleEngineMode = { viewModel.toggleEngineMode() },
                onToggleSearchGrounding = { viewModel.toggleSearchGrounding() }
            )

            // Chat Messages / Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !uiState.isGenerating) {
                    EmptyChatGreeting(
                        onSelectPresetPrompt = { prompt, level ->
                            viewModel.setThinkingLevel(level)
                            viewModel.sendMessage(prompt)
                        },
                        onOpenSandboxes = { showSandboxesDialog = true }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val parsedSteps = remember(msg.reasoningStepsJson) {
                                viewModel.thinkingManager.parseStepsJson(msg.reasoningStepsJson)
                            }
                            ChatMessageCard(
                                message = msg,
                                citations = viewModel.thinkingManager.parseCitationsJson(msg.searchCitationsJson),
                                parsedSteps = parsedSteps,
                                translatedText = uiState.chatMessageTranslations[msg.id],
                                isTranslating = uiState.isTranslatingMessageId == msg.id,
                                onTranslate = { viewModel.translateChatMessage(msg.id, msg.content, useGeminiNuance = false) },
                                onTranslateNuanced = { viewModel.translateChatMessage(msg.id, msg.content, useGeminiNuance = true) },
                                onInspectTree = {
                                    viewModel.inspectSteps(parsedSteps)
                                },
                                onDebugNodes = { stepIdx ->
                                    viewModel.openCotDebugOverlay(parsedSteps, stepIdx)
                                },
                                onExportTrace = {
                                    val userMsg = messages.takeWhile { it.id != msg.id }.lastOrNull { it.role == "user" }
                                    val prompt = userMsg?.content ?: "Reasoning & Deep Thinking Task"
                                    val exportData = ThoughtTraceExportData(
                                        prompt = prompt,
                                        thoughtProcess = msg.thoughtProcess,
                                        reasoningSteps = parsedSteps,
                                        finalResponse = msg.content,
                                        modelName = msg.modelMode,
                                        thinkingDurationMs = msg.thinkingDurationMs,
                                        thinkingTokens = msg.thinkingTokens,
                                        searchCitations = viewModel.thinkingManager.parseCitationsJson(msg.searchCitationsJson),
                                        timestamp = msg.timestamp
                                    )
                                    viewModel.openExportDialog(exportData)
                                },
                                onOpenFeedback = { viewModel.openFeedbackDialog(msg) },
                                onQuickRate = { rating -> viewModel.quickRateMessage(msg.id, rating) }
                            )
                        }

                        // Live generating message
                        if (uiState.isGenerating) {
                            item(key = "live_generating_item") {
                                LiveStreamingMessageCard(
                                    viewModel = viewModel,
                                    uiState = uiState,
                                    liveCitations = uiState.liveSearchCitations
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                // Scroll to Bottom FAB
                val showScrollFab by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 1
                    }
                }

                // Floating Action Button to Export Current Thought Trace & AI Response as Markdown / PDF
                val hasAssistantResponse = remember(messages, uiState.isGenerating, uiState.liveAnswerText, uiState.liveThoughtText) {
                    messages.any { it.role == "assistant" } || (uiState.isGenerating && (uiState.liveAnswerText.isNotBlank() || uiState.liveThoughtText.isNotBlank()))
                }

                if (hasAssistantResponse) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openExportDialog() },
                        containerColor = DeepThinkIndigo,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Thought Trace and AI Response",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = {
                            Text(
                                text = "Export Trace (MD/PDF)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = if (showScrollFab) 62.dp else 16.dp)
                            .height(44.dp)
                            .testTag("export_thought_trace_fab")
                    )
                }

                if (showScrollFab) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                val target = (messages.size + if (uiState.isGenerating) 1 else 0) - 1
                                if (target >= 0) listState.animateScrollToItem(target)
                            }
                        },
                        containerColor = DarkSurfaceElevated,
                        contentColor = GeminiBlueLight,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll down", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Proof Search Bar
            ProofSearchBar(
                query = uiState.cacheSearchQuery,
                onQueryChanged = { viewModel.updateCacheSearchQuery(it) }
            )

            // Collapsible CoT Debug Panel
            CollapsibleCoTPanel(
                steps = uiState.cotDebugSteps,
                isExpanded = uiState.isCotDebugPanelOpen,
                onToggle = { viewModel.toggleCotDebugPanel() }
            )

            // Agent & Model Mix-and-Match Configurator
            AgentMixMatchBar(
                selectedAgent = uiState.selectedAgentType,
                selectedStep = uiState.selectedInteractionStepType,
                linkEnabled = uiState.linkToPreviousInteraction,
                onAgentSelected = { viewModel.setSelectedAgentType(it) },
                onStepSelected = { viewModel.setSelectedInteractionStepType(it) },
                onToggleLink = { viewModel.toggleLinkToPreviousInteraction() },
                hasHistory = messages.any { it.role == "assistant" }
            )

            // Quick Prompt Chips Bar
            QuickPromptChips(
                onPromptSelected = { prompt, level ->
                    viewModel.setThinkingLevel(level)
                    viewModel.sendMessage(prompt)
                }
            )

            // Samsung Keyboard Symbol Palette & Voice/Transcription Bar & HF MCP Hub & Hybrid Features
            SamsungKeyboardAccessoryBar(
                onInsertSymbol = { symbol ->
                    inputText += symbol
                },
                onApplyTemplate = { template ->
                    inputText = template
                },
                onOpenVoiceConversation = {
                    viewModel.openVoiceConversation()
                },
                onOpenAudioTranscriber = {
                    viewModel.openAudioTranscriber()
                },
                nonStreamingEnabled = uiState.nonStreamingTreeEnabled,
                onToggleNonStreaming = { viewModel.toggleNonStreamingTree(it) },
                onOpenHfMcp = { viewModel.openHfMcpConnector() },
                onOpenReviewGenerator = { viewModel.openReviewGenerator() },
                onOpenQuickTranslator = { viewModel.openQuickTranslator(inputText) }
            )

            // Input Bar
            ChatInputBar(
                inputText = inputText,
                onInputChanged = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                onVoiceClick = {
                    viewModel.openVoiceConversation()
                },
                onAnalyzeFileClick = {
                    viewModel.openFileAnalysisDialog()
                },
                onGenerateSummaryClick = {
                    viewModel.openSummaryDialog()
                },
                isGenerating = uiState.isGenerating,
                onStop = { viewModel.stopGenerating() },
                onNewChat = { viewModel.startNewConversation() }
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit = {},
    onAnalyzeFileClick: () -> Unit = {},
    onGenerateSummaryClick: () -> Unit = {},
    isGenerating: Boolean,
    onStop: () -> Unit,
    onNewChat: () -> Unit
) {
    Surface(
        color = Color(0xCC161B22), // rgba(22, 27, 34, 0.8)
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New Session Button
            IconButton(
                onClick = onNewChat,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .testTag("new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Thinking Session",
                    tint = LuminousBlue,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Analyze File Quick Trigger Button
            IconButton(
                onClick = onAnalyzeFileClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .testTag("chat_input_analyze_file_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Analyze File",
                    tint = VibrantTeal,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Main Text Input
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        "Ask any inquiry, prompt, or file...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDisabled, fontSize = 13.sp)
                    )
                },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuminousBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AbsoluteBlack,
                    unfocusedContainerColor = AbsoluteBlack,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Voice Conversation Mic Trigger Button
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .testTag("voice_conversation_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Start Voice Conversation",
                    tint = VibrantTeal,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Send / Stop Button
            if (isGenerating) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VibrantRed)
                        .testTag("stop_generation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Generation",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) LuminousBlue else Color.White.copy(alpha = 0.05f)
                        )
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank()) Color.White else TextDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageCard(
    message: ChatMessageEntity,
    citations: List<SearchCitation>,
    parsedSteps: List<ReasoningStep> = emptyList(),
    translatedText: String? = null,
    isTranslating: Boolean = false,
    onTranslate: () -> Unit = {},
    onTranslateNuanced: () -> Unit = {},
    onInspectTree: () -> Unit,
    onDebugNodes: (stepIndex: Int) -> Unit = {},
    onExportTrace: () -> Unit = {},
    onOpenFeedback: () -> Unit,
    onQuickRate: (Int) -> Unit
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message Bubble
            Surface(
                shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp),
                color = GeminiBlue,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    if (message.searchGrounded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Search Grounding Enabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Gemini Assistant Bubble (Immersive Deep Slate Surface)
            Surface(
                shape = RoundedCornerShape(6.dp, 20.dp, 20.dp, 20.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Model Tag & Search Badge & Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GeminiBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GeminiBlueLight,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.modelMode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiBlueLight,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.4.sp
                                )
                            )

                            if (message.interactionMetadataJson.contains("\"isCacheHit\":true") || message.modelMode.contains("Cache Hit")) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeminiEmerald.copy(alpha = 0.22f))
                                        .border(0.8.dp, GeminiEmerald.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "⚡ ROOM CACHE HIT (0 ms)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeminiEmerald,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }

                            if (message.agentType != "DEFAULT") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DeepThinkIndigo.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (message.agentType) {
                                            "DEEP_RESEARCH" -> "🕵️ RESEARCH"
                                            "DATA_COLLECTOR" -> "📊 COLLECTOR"
                                            "CODE_ARCHITECT" -> "💻 ARCHITECT"
                                            else -> message.agentType
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DeepThinkIndigoLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (message.interactionStepType != "CONVERSATION") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeminiPurple.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = when (message.interactionStepType) {
                                            "DATA_COLLECTION" -> "🔍 COLLECTION"
                                            "SUMMARIZATION" -> "📝 SUMMARIZE"
                                            "REFORMATTING" -> "📊 REFORMAT"
                                            "DATASET_GENERATION" -> "💾 DATASET GEN"
                                            "CODE_SYNTHESIS" -> "⚙️ SYNTHESIS"
                                            else -> message.interactionStepType ?: ""
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeminiBlueLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            if (message.previousInteractionId != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeminiCyan.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = GeminiCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "LINKED STEP",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GeminiCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            if (message.searchGrounded) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeminiCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = GeminiCyan,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "GROUNDED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GeminiCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("response", message.content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("copy_answer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = DarkTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Collapsible Thought Accordion
                    if (message.thoughtProcess.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ThoughtAccordion(
                            thoughtText = message.thoughtProcess,
                            isThinkingLive = false,
                            durationMs = message.thinkingDurationMs,
                            thinkingTokens = message.thinkingTokens,
                            steps = parsedSteps,
                            onInspectTree = onInspectTree,
                            onDebugNodes = onDebugNodes
                        )
                    }

                    // Citations Card if present
                    if (citations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🌐 Real-Time Google Search Grounding Sources",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        citations.forEach { cit ->
                            SearchCitationCard(
                                citation = cit,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Markdown Final Answer
                    MarkdownContentView(
                        content = message.content,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )

                    // Translation Action & Result
                    if (isTranslating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = GeminiCyan,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Translating with ML Kit...",
                                color = GeminiCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    translatedText?.let { translation ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1B4B),
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, GeminiBlueLight.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌐 ML Kit Translation:",
                                        color = GeminiCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Translation", translation)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied translation!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Translation",
                                            tint = DarkTextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = translation,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Footer Feedback Actions Row
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, DarkOutlineVariant, RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rating badges or prompt
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (message.userFeedbackRating != 0) "Your Rating:" else "Rate response:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 10.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            // Thumbs Up
                            IconButton(
                                onClick = { onQuickRate(if (message.userFeedbackRating > 0) 0 else 1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (message.userFeedbackRating > 0) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Helpful",
                                    tint = if (message.userFeedbackRating > 0) GeminiEmerald else DarkTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            // Thumbs Down
                            IconButton(
                                onClick = { onQuickRate(if (message.userFeedbackRating < 0) 0 else -1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (message.userFeedbackRating < 0) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                    contentDescription = "Needs Work",
                                    tint = if (message.userFeedbackRating < 0) WarningOrange else DarkTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            if (message.userFeedbackRating > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${message.userFeedbackRating}★",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFFFC107),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Right actions: ML Kit Translate + Feedback Note + Export
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Export Action
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onExportTrace)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export Document",
                                    tint = DeepThinkIndigoLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Export",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepThinkIndigoLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Translate Action
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onTranslate)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Translate",
                                    tint = GeminiCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Translate",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GeminiCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Detailed Feedback Dialog Button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable(onClick = onOpenFeedback)
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (message.userFeedbackText.isNotBlank()) Icons.Filled.Feedback else Icons.Outlined.Feedback,
                                    contentDescription = "Add review note",
                                    tint = if (message.userFeedbackText.isNotBlank()) GeminiBlueLight else DarkTextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (message.userFeedbackText.isNotBlank()) "Feedback Saved" else "Add Note",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (message.userFeedbackText.isNotBlank()) GeminiBlueLight else DarkTextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
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
fun LiveStreamingMessageCard(
    viewModel: MainViewModel,
    uiState: UiState,
    liveCitations: List<SearchCitation>
) {
    Surface(
        shape = RoundedCornerShape(6.dp, 20.dp, 20.dp, 20.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GeminiBlue.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(GeminiBlue.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = GeminiBlueGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${uiState.activeModelName} Running",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlueGlow,
                        letterSpacing = 0.5.sp
                    )
                )

                if (uiState.searchGroundingEnabled) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SEARCH ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Live Thought Accordion
            if (uiState.liveThoughtText.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                ThoughtAccordion(
                    thoughtText = uiState.liveThoughtText,
                    isThinkingLive = true,
                    durationMs = uiState.liveDurationMs,
                    thinkingTokens = uiState.liveThoughtTokens,
                    currentStep = uiState.liveCurrentStep,
                    steps = uiState.cotDebugSteps,
                    onInspectTree = { viewModel.inspectSteps(uiState.cotDebugSteps) },
                    onDebugNodes = { stepIdx -> viewModel.openCotDebugOverlay(uiState.cotDebugSteps, stepIdx) }
                )
            }

            // Live Citations
            if (liveCitations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🌐 Retrieved Grounding Sources",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GeminiCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                liveCitations.forEach { cit ->
                    SearchCitationCard(
                        citation = cit,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Live Answer stream
            if (uiState.liveAnswerText.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                MarkdownContentView(
                    content = uiState.liveAnswerText,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun QuickPromptChips(
    onPromptSelected: (String, ThinkingLevel) -> Unit,
    onAnalyzeFileClick: () -> Unit = {},
    onGenerateSummaryClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground.copy(alpha = 0.95f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Action Chip: Analyze File
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GeminiBlue.copy(alpha = 0.15f))
                .border(0.8.dp, GeminiBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onAnalyzeFileClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("quick_chip_analyze_file"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = GeminiCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Analyze File",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GeminiCyan,
                    fontSize = 11.5.sp
                )
            )
        }

        // Quick Action Chip: Generate Summary
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GeminiPurple.copy(alpha = 0.15f))
                .border(0.8.dp, GeminiPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable { onGenerateSummaryClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("quick_chip_generate_summary"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Summarize,
                contentDescription = null,
                tint = GeminiPurple,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Generate Summary",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 11.5.sp
                )
            )
        }

        PresetCatalog.presets.take(4).forEach { preset ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(0.8.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
                    .clickable { onPromptSelected(preset.promptTemplate, preset.recommendedLevel) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("quick_chip_${preset.id}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = preset.domainTag,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyChatGreeting(
    onSelectPresetPrompt: (String, ThinkingLevel) -> Unit,
    onOpenSandboxes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Immersive Multi-Ring Neural Constellation
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    Brush.radialGradient(
                        listOf(GeminiBlue.copy(alpha = 0.25f), GeminiPurple.copy(alpha = 0.15f), Color.Transparent)
                    ),
                    CircleShape
                )
                .border(1.dp, GeminiBlue.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(DarkSurfaceVariant, CircleShape)
                    .border(1.dp, DarkOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Deep Thinking Gemini 3.7",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Offline All-in-One Cognitive Engine with Google Search Grounding, Local Model Hub & Veo 3 Studio",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DarkTextMuted,
                lineHeight = 20.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Cognitive Booster Launcher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onOpenSandboxes() }
                .testTag("launch_sandboxes_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GeminiPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GeminiBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Explore Cognitive Sandboxes",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Deploy multi-branch verification trees & mathematical proofs",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Prompt Tiles
        Text(
            text = "SELECT A REASONING BENCHMARK",
            style = MaterialTheme.typography.labelSmall.copy(
                color = DarkTextSubtle,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetCatalog.presets.take(4).forEach { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPresetPrompt(preset.promptTemplate, preset.recommendedLevel) }
                        .testTag("greeting_preset_${preset.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = preset.domainTag + " • " + preset.recommendedLevel.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentMixMatchBar(
    selectedAgent: String,
    selectedStep: String,
    linkEnabled: Boolean,
    onAgentSelected: (String) -> Unit,
    onStepSelected: (String) -> Unit,
    onToggleLink: () -> Unit,
    hasHistory: Boolean,
    modifier: Modifier = Modifier
) {
    var showAgentDropdown by remember { mutableStateOf(false) }
    var showStepDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground.copy(alpha = 0.95f))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Link to Previous Step Chip
        val linkBgColor = if (linkEnabled && hasHistory) GeminiBlue.copy(alpha = 0.22f) else DarkSurfaceVariant
        val linkBorderColor = if (linkEnabled && hasHistory) GeminiBlue else DarkOutlineVariant
        val linkTextColor = if (linkEnabled && hasHistory) GeminiBlueLight else DarkTextMuted

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(linkBgColor)
                .border(0.8.dp, linkBorderColor, RoundedCornerShape(16.dp))
                .clickable(enabled = hasHistory) { onToggleLink() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("link_previous_step_chip"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (linkEnabled && hasHistory) Icons.Default.CheckCircle else Icons.Default.Stop,
                contentDescription = null,
                tint = linkTextColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasHistory) {
                    if (linkEnabled) "🔗 LINKED TO PREVIOUS" else "🔗 LINK DISABLED"
                } else {
                    "🔗 NO PREVIOUS STEP"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = linkTextColor,
                    fontSize = 11.sp
                )
            )
        }

        // Agent Dropdown Chip
        val agentLabel = when (selectedAgent) {
            "DEEP_RESEARCH" -> "🕵️ Deep Research Agent"
            "DATA_COLLECTOR" -> "📊 Data Collector Agent"
            "CODE_ARCHITECT" -> "💻 Code Architect Agent"
            else -> "🤖 Standard Gemini"
        }

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepThinkIndigo.copy(alpha = 0.22f))
                    .border(0.8.dp, DeepThinkIndigo, RoundedCornerShape(16.dp))
                    .clickable { showAgentDropdown = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("agent_dropdown_chip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agent: $agentLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepThinkIndigoLight,
                        fontSize = 11.sp
                    )
                )
            }

            androidx.compose.material3.DropdownMenu(
                expanded = showAgentDropdown,
                onDismissRequest = { showAgentDropdown = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                listOf(
                    "DEFAULT" to "🤖 Standard Gemini",
                    "DEEP_RESEARCH" to "🕵️ Deep Research Agent",
                    "DATA_COLLECTOR" to "📊 Data Collector Agent",
                    "CODE_ARCHITECT" to "💻 Code Architect Agent"
                ).forEach { (type, label) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(label, color = Color.White) },
                        onClick = {
                            onAgentSelected(type)
                            showAgentDropdown = false
                        }
                    )
                }
            }
        }

        // Step Dropdown Chip
        val stepLabel = when (selectedStep) {
            "DATA_COLLECTION" -> "🔍 Data Collection"
            "SUMMARIZATION" -> "📝 Summarization"
            "REFORMATTING" -> "📊 Reformatting"
            "DATASET_GENERATION" -> "💾 Dataset Gen"
            "CODE_SYNTHESIS" -> "⚙️ Code Synthesis"
            else -> "💬 Chat"
        }

        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(GeminiPurple.copy(alpha = 0.22f))
                    .border(0.8.dp, GeminiPurple, RoundedCornerShape(16.dp))
                    .clickable { showStepDropdown = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("step_dropdown_chip"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step: $stepLabel",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlueLight,
                        fontSize = 11.sp
                    )
                )
            }

            androidx.compose.material3.DropdownMenu(
                expanded = showStepDropdown,
                onDismissRequest = { showStepDropdown = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                listOf(
                    "CONVERSATION" to "💬 Chat",
                    "DATA_COLLECTION" to "🔍 Data Collection",
                    "SUMMARIZATION" to "📝 Summarization",
                    "REFORMATTING" to "📊 Reformatting",
                    "DATASET_GENERATION" to "💾 Dataset Gen",
                    "CODE_SYNTHESIS" to "⚙️ Code Synthesis"
                ).forEach { (type, label) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(label, color = Color.White) },
                        onClick = {
                            onStepSelected(type)
                            showStepDropdown = false
                        }
                    )
                }
            }
        }
    }
}

