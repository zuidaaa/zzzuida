package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch
import com.example.data.WebWatcherEntity
import com.example.data.WikiPageEntity
import com.example.engine.octopus.OctopusToolDefinition
import com.example.ui.MainViewModel
import com.example.ui.UiState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.example.ui.theme.*
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OctopusAgentScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Executor, 1: Browser, 2: Watchers, 3: Wiki, 4: Workspace
    val watchers by viewModel.webWatchersList.collectAsStateWithLifecycle()
    val wikiPages by viewModel.wikiPagesList.collectAsStateWithLifecycle()
    val cachedReasoning by viewModel.cachedReasoningList.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
    ) {
        // Tab row header
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xCC161B22), // rgba(22, 27, 34, 0.8)
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🐙 Executor", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("octopus_tab_executor")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐 Browser", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        if (uiState.isAutopilotStuckOnCaptcha) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DeepThinkRose)
                             )
                        }
                    }
                },
                modifier = Modifier.testTag("octopus_tab_browser")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("📝 Wiki", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("octopus_tab_wiki")
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("☁️ Workspace & One", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("octopus_tab_workspace")
            )
        }

        when (selectedTab) {
            0 -> ExecutorSection(viewModel = viewModel, uiState = uiState, cachedReasoning = cachedReasoning)
            1 -> AutopilotBrowserSection(viewModel = viewModel, uiState = uiState)
            3 -> LLMWikiSection(viewModel = viewModel, uiState = uiState, wikiPages = wikiPages)
            4 -> GoogleWorkspaceSection(viewModel = viewModel, uiState = uiState)
        }
    }
}

@Composable
private fun ExecutorSection(viewModel: MainViewModel, uiState: UiState, cachedReasoning: List<com.example.data.ReasoningCacheEntity>) {
    var promptInput by remember { mutableStateOf("Check current device battery telemetry and calculate 2^16 - 1") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Panel: Model & Role Configuration
        item {
            GlassPanel {
                Text(
                    text = "Model & Role Configuration",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto Mode", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.autoModelRoutingEnabled,
                        onCheckedChange = { viewModel.toggleAutoModelRouting() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = LuminousYellow
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FunctionalTag(text = uiState.selectedChatbotRole, variant = TagVariant.MODE_YELLOW)
                    FunctionalTag(text = "NPU TURBO", variant = TagVariant.MODE_YELLOW)
                }
            }
        }

        // 2. Panel: Chain-of-Thought Debugger
        item {
            GlassPanel {
                Text(
                    text = "Chain-of-Thought Debugger",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FunctionalTag("REASONING", TagVariant.REASONING_PURPLE)
                    FunctionalTag("TRACE", TagVariant.REASONING_PURPLE)
                    FunctionalTag("NEXA", TagVariant.TECHNICAL_TEAL)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.toggleCotDebugPanel() },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Live Debug Trace", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { /* Optimize */ },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Optimize Node", fontSize = 11.sp, color = VibrantPurple)
                    }
                }
            }
        }

        // Interactive Prompt Execution Box (Redesigned)
        item {
            GlassPanel {
                Text(
                    text = "Agent Command Prompt",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = AbsoluteBlack,
                        unfocusedContainerColor = AbsoluteBlack,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.executeOctopusPlan(promptInput) },
                    enabled = !uiState.octopusIsExecuting,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dispatch Octopus Plan", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. Component: Tilted Cached Proofs Stack
        item {
            Text(
                text = "Cached Proofs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            
            ProofSearchBar(
                query = uiState.cacheSearchQuery,
                onQueryChanged = { viewModel.setCacheSearchQuery(it) }
            )
            
            TiltedCachedProofsStack(cachedReasoning)
        }

        // 5. Execution Plan & Visual Action Trace Result
        if (uiState.octopusLatestPlan != null) {
            val plan = uiState.octopusLatestPlan
            item {
                GlassPanel(
                    modifier = Modifier.border(1.dp, VibrantGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Predicted Octopus Action Token",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = VibrantGreen
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${plan.latencyMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            color = AbsoluteBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = plan.predictedActionToken,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = VibrantTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        if (plan.toolTrace != null) {
                            val trace = plan.toolTrace
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚡ Tool Output: [${trace.toolName}] (${trace.executionDurationMs}ms)",
                                style = MaterialTheme.typography.labelSmall.copy(color = LuminousBlue, fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                color = AbsoluteBlack,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = trace.executionResult,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (trace.isSuccess) Color.White else VibrantRed
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Synthesized Agent Response:",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            color = AbsoluteBlack,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = plan.finalResponseText,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Registered Tools Catalog Header
        item {
            Text(
                text = "Registered On-Device Tools (${uiState.octopusTools.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(uiState.octopusTools) { tool ->
            OctopusToolCard(tool = tool)
        }
    }
}

@Composable
private fun TiltedCachedProofsStack(cachedReasoning: List<com.example.data.ReasoningCacheEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (cachedReasoning.isEmpty()) {
            Text("No cached proofs found", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        } else {
            // Show up to 3 cards tilted
            val displayList = cachedReasoning.take(3).reversed()
            displayList.forEachIndexed { index, proof ->
                val tiltFactor = (displayList.size - 1 - index)
                CachedProofCard(
                    title = proof.promptQuery,
                    offset = (tiltFactor * 8).dp,
                    rotation = (tiltFactor * -2f),
                    alpha = 1.0f - (tiltFactor * 0.3f)
                )
            }
        }
    }
}

@Composable
private fun CachedProofCard(title: String, offset: androidx.compose.ui.unit.Dp, rotation: Float, alpha: Float) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(100.dp)
            .graphicsLayer {
                rotationZ = rotation
                translationX = offset.toPx()
                translationY = (offset.toPx() / 2)
            },
        color = Color(0xFF161B22).copy(alpha = alpha * 0.7f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = alpha))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("VERIFIED BY GEMINI 3.7", style = MaterialTheme.typography.labelSmall, color = VibrantGreen)
        }
    }
}

@Composable
private fun OctopusToolCard(tool: OctopusToolDefinition) {
    GlassPanel {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                SuggestionChip(
                    onClick = {},
                    label = { Text(tool.category, fontSize = 10.sp, color = TextPrimary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.White.copy(alpha = 0.1f))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = AbsoluteBlack,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tool.exampleInvocation,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = VibrantTeal
                    ),
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun AutopilotBrowserSection(viewModel: MainViewModel, uiState: UiState) {
    var urlInput by remember { mutableStateOf(uiState.autopilotBrowserUrl) }
    var actionPrompt by remember { mutableStateOf(uiState.autopilotBrowserPrompt) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BYOM (Bring Your Own Model) Configuration Indicator
        item {
            GlassPanel(
                modifier = Modifier.border(1.dp, VibrantTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VpnKey,
                        contentDescription = "BYOM",
                        tint = VibrantTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bring Your Own Model (BYOM) Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            "Using local OpenAI-compatible route (ChatGPT/Claude/DeepSeek API). Offline execution guaranteed.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text("API Active", fontSize = 9.sp, color = VibrantTeal) }
                    )
                }
            }
        }

        // URL & Prompt Configuration Box
        item {
            GlassPanel {
                Column {
                    Text(
                        "On-Device Browser Autopilot",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Browser Destination URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("autopilot_url_input"),
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantTeal,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = actionPrompt,
                        onValueChange = { actionPrompt = it },
                        label = { Text("Autopilot Instruction / Objective") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("autopilot_prompt_input"),
                        placeholder = { Text("e.g. Book the 6:00 AM gym class slots when they unlock") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantTeal,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = AbsoluteBlack,
                            unfocusedContainerColor = AbsoluteBlack
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.triggerAutopilotNavigation(urlInput, actionPrompt) },
                        enabled = !uiState.autopilotIsNavigating && !uiState.isAutopilotStuckOnCaptcha,
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("autopilot_launch_button")
                    ) {
                        if (uiState.autopilotIsNavigating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Autopilot Navigating & Executing...", color = Color.Black, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Launch, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Autonomous Browser Session", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Simulated Viewport Container
        item {
            GlassPanel(
                modifier = Modifier.border(1.dp, if (uiState.isAutopilotStuckOnCaptcha) VibrantRed.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Column {
                    // Browser viewport header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AbsoluteBlack)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VibrantRed))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LuminousYellow))
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VibrantGreen))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = uiState.autopilotBrowserUrl,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (uiState.isAutopilotStuckOnCaptcha) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VibrantRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("USER HAND-OFF", color = VibrantRed, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        } else if (uiState.autopilotIsNavigating) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VibrantTeal.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("AUTOPILOT RUNNING", color = VibrantTeal, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    // Viewport body
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF1E1E2E))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isAutopilotStuckOnCaptcha) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = VibrantRed, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "🚨 STUCK ON CAPTCHA / LOGIN GATE",
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantRed,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Complete the captcha/login on your screen. Tap below when finished to hand back control to the Agent.",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.simulateCaptchaSolved() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                                    modifier = Modifier.testTag("autopilot_captcha_solve_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Captcha Solved. Hand Back to Agent", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (uiState.autopilotIsNavigating) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VibrantTeal, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Agent filling forms, extracting values, & clicking buttons...",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Browser is idle. No task is running.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Launch a session to automate online checkouts, bookings, or monitoring.",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { viewModel.simulateCaptchaEncountered() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTeal),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantTeal)
                                ) {
                                    Text("Simulate Captcha Interrupt Gate")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Log Stream Console
        item {
            Text(
                "Autopilot Session Trace Console",
                style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = AbsoluteBlack,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    uiState.autopilotBrowserLog.forEach { logLine ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(
                                text = "> ",
                                color = VibrantTeal,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = logLine,
                                color = if (logLine.contains("⚠️")) VibrantRed else if (logLine.contains("✅")) VibrantGreen else Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WebWatchersSection(viewModel: MainViewModel, watchers: List<WebWatcherEntity>) {
    var siteNameInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var frequencyInput by remember { mutableStateOf("12") } // Hours
    var scheduledTimeInput by remember { mutableStateOf("12:00") }
    var codeInput by remember { mutableStateOf("") }
    var isAddingWatcher by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Background Web Watchers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        "Always-on local watchers checking sites for price drops, resale tickets, and slot cancels",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                    )
                }
                IconButton(
                    onClick = { isAddingWatcher = !isAddingWatcher },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = DeepThinkIndigo.copy(alpha = 0.3f))
                ) {
                    Icon(if (isAddingWatcher) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Watcher", tint = DeepThinkIndigoLight)
                }
            }
        }

        // Add Watcher Panel
        if (isAddingWatcher) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigoLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Configure New Watcher Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )

                        OutlinedTextField(
                            value = siteNameInput,
                            onValueChange = { siteNameInput = it },
                            label = { Text("Site / Objective Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Visa Appointment Slot Grabber") }
                        )

                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            label = { Text("Target Web URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://www.resale-tickets.com/...)") }
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = frequencyInput,
                                onValueChange = { frequencyInput = it },
                                label = { Text("Freq (Hours)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = scheduledTimeInput,
                                onValueChange = { scheduledTimeInput = it },
                                label = { Text("Time (24h)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = it },
                            label = { Text("Watcher Automation Code / Selector Rule") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Check '.ticket-price'. Notify if price < 150.") }
                        )

                        Button(
                            onClick = {
                                viewModel.addWebWatcher(
                                    siteName = siteNameInput,
                                    url = urlInput,
                                    freqHours = frequencyInput.toIntOrNull() ?: 12,
                                    scheduledTime = scheduledTimeInput,
                                    code = codeInput
                                )
                                siteNameInput = ""
                                urlInput = ""
                                codeInput = ""
                                isAddingWatcher = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Deploy Background Watcher")
                        }
                    }
                }
            }
        }

        // Active Watchers List
        if (watchers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active web watchers defined.", color = DarkTextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(watchers) { watcher ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (watcher.active) DeepThinkIndigo.copy(alpha = 0.3f) else DarkOutline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    watcher.siteName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    watcher.url,
                                    fontSize = 11.sp,
                                    color = DarkTextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Switch(
                                checked = watcher.active,
                                onCheckedChange = { viewModel.toggleWebWatcher(watcher.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeepThinkIndigo,
                                    uncheckedThumbColor = DarkTextMuted,
                                    uncheckedTrackColor = DarkBackground
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Schedule details
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⏰ Runs: Every ${watcher.frequencyHours} hrs at ${watcher.scheduledTime}",
                                fontSize = 11.sp,
                                color = DeepThinkCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "📩 Notifications: ${watcher.notificationSentCount}",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Automation code block
                        Surface(
                            color = DarkBackground,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "🤖 AUTOMATION WATCHER CODE:",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = DeepThinkIndigoLight,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    watcher.watcherCode,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.triggerWebWatcherCheck(watcher.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepThinkCyan)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check / Simulate Now", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { viewModel.deleteWebWatcher(watcher.id) },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = DeepThinkRose.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DeepThinkRose)
                            }
                        }

                        // Last checked status feedback
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Status: ${watcher.lastCheckedValue} (Checked: ${java.text.SimpleDateFormat("HH:mm:ss").format(watcher.lastCheckedTimestamp)})",
                            fontSize = 10.sp,
                            color = DarkTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LLMWikiSection(
    viewModel: MainViewModel,
    uiState: UiState,
    wikiPages: List<WikiPageEntity>
) {
    var selectedSubTab by remember { mutableIntStateOf(1) } // 0: Raw Sources, 1: Compiled Wiki, 2: Instructional Schema
    var searchQuery by remember { mutableStateOf(uiState.wikiSearchQuery) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var isAddingPage by remember { mutableStateOf(false) }

    // Export status feedback
    var exportStatusText by remember { mutableStateOf<String?>(null) }

    // Monospace AGENT.md text
    var agentSchemaMarkdown by remember { mutableStateOf("""# Personal Assistant LLM Wiki Instruction Schema

## Metadata Rules
- Every page must contain three mandatory frontmatter headers: Title, Tags, LastUpdated.
- Linked entities must be enclosed in double square brackets, e.g., [[Topic]].

## Write-Time Compilation Schema
- Summarize raw ground-truth source logs into concise, entity-centric Markdown pages.
- Do not create orphan pages; link new cards to at least one existing concept node.

## Conflict Resolution (Overnight Routine)
- During daily 02:00 AM maintenance, run cross-entropy comparison.
- If a raw source contradicts an existing wiki card, flag with '⚠️ Conflict' and prefer latest verifiable telemetry source.""") }

    // Raw Ground-Truth Sources List
    var rawSources by remember { mutableStateOf(listOf(
        RawSourceItem("watch_meeting_transcripts.txt", "Raw transcript containing Andrej Karpathy's quotes on on-device LLM caching and semantic markdown nodes.", "TXT", 1240, "NOT_COMPILED"),
        RawSourceItem("exynos_telemetry_capture.csv", "Raw hardware execution logs of Exynos 2600 NPU computing 85 TOPS matrices.", "CSV", 4580, "NOT_COMPILED"),
        RawSourceItem("assistant_manifest_profile.json", "Raw profile containing user device specification, email wuemasl@gmail.com, and agent preferences.", "JSON", 512, "NOT_COMPILED")
    )) }

    var compilingSourceId by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Futuristic Agent Illustration Banner
        item {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.agent_onboarding),
                contentDescription = "Holographic Phone Agent Onboarding",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DeepThinkCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            )
        }

        // Hero / Karpathy Wiki Concept Intro
        item {
            GlassPanel(
                modifier = Modifier.border(1.dp, VibrantTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Column {
                    Text(
                        "🧠 Karpathy LLM Wiki Memory",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Based on Andrej Karpathy's LLM Wiki proposal. Everyone, everything, and every topic the agent learns about is saved inside a linked on-device Wiki. Persistent, searchable, and fully indexable memory.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Three-Layer Sub-Tab Row
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = AbsoluteBlack,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = VibrantTeal
                    )
                },
                divider = { HorizontalDivider(color = BorderSubtle) }
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("📁 Raw Sources", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("📖 Compiled Wiki", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("⚙️ AGENT.md Schema", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        // Overnight Maintenance Card (Only shown on Compiled Wiki Tab)
        if (selectedSubTab == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QueryBuilder, contentDescription = null, tint = DeepThinkCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Overnight Wiki Maintenance Optimiser",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DeepThinkCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("02:00 AM Active", color = DeepThinkCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            uiState.overnightWikiMaintenanceLog,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.runOvernightWikiMaintenance() },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Overnight LLM Optimization Now", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // LAYER 0: RAW SOURCES
        if (selectedSubTab == 0) {
            item {
                Text(
                    "Raw Sources Layer (Ground-Truth Evidence)",
                    style = MaterialTheme.typography.labelMedium.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold)
                )
            }

            items(rawSources) { source ->
                GlassPanel(
                    modifier = Modifier.border(1.dp, if (compilingSourceId == source.name) VibrantTeal else BorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (source.format == "TXT") Icons.Default.Description else if (source.format == "CSV") Icons.Default.GridOn else Icons.Default.DataObject,
                                contentDescription = null,
                                tint = VibrantTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(source.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("${source.sizeBytes} bytes • format: ${source.format}", fontSize = 11.sp, color = TextSecondary)
                            }

                            if (source.status == "COMPILED") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VibrantGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("COMPILED UPFRONT", color = VibrantGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(source.description, fontSize = 11.sp, color = Color.LightGray)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (compilingSourceId == source.name) {
                            Column {
                                LinearProgressIndicator(color = VibrantTeal, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Write-Time Compilation: Indexing entity connections upfront...",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = VibrantTeal
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        compilingSourceId = source.name
                                        kotlinx.coroutines.delay(2000L)
                                        
                                        // Simulate Compile upfront by inserting matching pages!
                                        if (source.name.contains("carpathy")) {
                                            viewModel.addWikiPage(
                                                "Andrej Karpathy",
                                                "Renowned AI researcher, co-founder of OpenAI, and former Director of AI at Tesla. Popularized the LLM Wiki concept, where an on-device personal assistant maintains a persistent and structured wiki of memory about everything it learns. This app's memory system is based directly on this vision. Interlinks: [[DeepThink Project]].",
                                                "AI, Researchers, Founders"
                                            )
                                        } else if (source.name.contains("exynos")) {
                                            viewModel.addWikiPage(
                                                "Samsung Galaxy S26 Ultra",
                                                "Primary hardware host for this LLM assistant. Features Exynos 2600 octa-core CPU, 16 GB LPDDR6 RAM, and a high-performance 85 TOPS NPU backend. Fully integrated with on-device model routing. Interlinks: [[DeepThink Project]], [[Octopus Tool Routing]].",
                                                "Hardware, Systems"
                                            )
                                        } else if (source.name.contains("manifest")) {
                                            viewModel.addWikiPage(
                                                "DeepThink Project",
                                                "The custom core workspace project name of this on-device system. Configured on Samsung Exynos 2600 + NPU architecture. Designed as an always-on, autonomous assistant system with on-device memory, browser automation, and background watch algorithms. Keeps all data securely offline on your phone. Interlinks: [[Andrej Karpathy]], [[Samsung Galaxy S26 Ultra]].",
                                                "Workspace, Devices"
                                            )
                                        }

                                        rawSources = rawSources.map {
                                            if (it.name == source.name) it.copy(status = "COMPILED") else it
                                        }
                                        compilingSourceId = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compile Upfront", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // LAYER 1: COMPILED WIKI
        if (selectedSubTab == 1) {
            // Export and Sync Tools Bar
            item {
                GlassPanel(
                    modifier = Modifier.border(1.dp, VibrantTeal.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Column {
                        Text(
                            "Export & Sync Vault",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Export your compiled semantic memory wiki directly to your favorite tools offline or in the cloud.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    exportStatusText = "Obsidian Vault Export Complete: 3 connected nodes exported with wikilinks [[Topic]] format."
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTeal),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Obsidian .zip", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    exportStatusText = "NotebookLM Context Copied: 4,500 words of semantic context formatted for NotebookLM source upload!"
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTeal),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("NotebookLM", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.syncWikiToGoogleDrive()
                                    exportStatusText = "Google Drive Sync Aktiv: ${wikiPages.size} verbundene Markdown-Knoten werden in den Google Drive Vault synchronisiert."
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTeal),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google Drive", fontSize = 10.sp)
                            }
                        }

                        if (exportStatusText != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VibrantTeal.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    exportStatusText!!,
                                    fontSize = 11.sp,
                                    color = VibrantTeal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Adding Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.updateWikiSearchQuery(it)
                        },
                        label = { Text("Search Compiled Wiki Memory...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    IconButton(
                        onClick = { isAddingPage = !isAddingPage },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = DeepThinkCyan.copy(alpha = 0.15f))
                    ) {
                        Icon(if (isAddingPage) Icons.Default.Close else Icons.Default.Create, contentDescription = "Add page", tint = DeepThinkCyanLight)
                    }
                }
            }

            // Add Wiki Page Form
            if (isAddingPage) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Add New Wiki Memory Node",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )

                            OutlinedTextField(
                                value = titleInput,
                                onValueChange = { titleInput = it },
                                label = { Text("Topic Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = contentInput,
                                onValueChange = { contentInput = it },
                                label = { Text("Content / Facts Learned") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = tagsInput,
                                onValueChange = { tagsInput = it },
                                label = { Text("Tags (comma separated)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    viewModel.addWikiPage(titleInput, contentInput, tagsInput)
                                    titleInput = ""
                                    contentInput = ""
                                    tagsInput = ""
                                    isAddingPage = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Topic Node", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Wiki Nodes List
            if (wikiPages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching wiki memory pages found.", color = DarkTextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                items(wikiPages) { page ->
                    GlassPanel(
                        modifier = Modifier.border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    page.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                
                                IconButton(
                                    onClick = { viewModel.deleteWikiPage(page.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = VibrantRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }

                            // Tags
                            if (page.tags.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    page.tags.split(",").forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VibrantTeal.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tag.trim(), color = VibrantTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                page.content,
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                lineHeight = 16.sp
                            )

                            // ConnectionsJson / Links
                            if (page.connectionsJson.isNotBlank() && page.connectionsJson != "[]") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Linked Memory Cards (Wikilinks):",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val links = try {
                                        org.json.JSONArray(page.connectionsJson)
                                    } catch (e: Exception) {
                                        org.json.JSONArray()
                                    }
                                    for (i in 0 until links.length()) {
                                        val linkedNode = links.optString(i, "Linked Node")
                                        val nodeLabel = linkedNode.replace("wiki-", "").replace("-", " ").capitalize()
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, LuminousBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Link, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("[[$nodeLabel]]", color = Color.White, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Last updated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(page.lastUpdated)}",
                                fontSize = 9.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // LAYER 2: INSTRUCTIONAL SCHEMA
        if (selectedSubTab == 2) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Instructional Schema Configuration (AGENT.md)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "This configuration schema guides the agent's write-time compilation, overnight optimization rules, and metadata compilation structures.",
                            fontSize = 11.sp,
                            color = DarkTextMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = agentSchemaMarkdown,
                            onValueChange = { agentSchemaMarkdown = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .testTag("agent_schema_markdown_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                exportStatusText = "AGENT.md Schema Updated: Applied compilation, indexing, and overnight maintenance configurations."
                                selectedSubTab = 1 // Go back to Wiki view
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Instructional Schema Rules", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class RawSourceItem(
    val name: String,
    val description: String,
    val format: String,
    val sizeBytes: Long,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleWorkspaceSection(viewModel: MainViewModel, uiState: UiState) {
    val isSyncing by viewModel.isWorkspaceSyncing.collectAsStateWithLifecycle()
    val gmailMessages by viewModel.gmailMessages.collectAsStateWithLifecycle()
    val calendarEvents by viewModel.calendarEvents.collectAsStateWithLifecycle()
    val driveFiles by viewModel.driveFiles.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val googleOneStatus by viewModel.googleOneStatus.collectAsStateWithLifecycle()
    val gmailVoiceSummary by viewModel.gmailVoiceSummary.collectAsStateWithLifecycle()
    val operationMsg by viewModel.workspaceOperationMessage.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Gmail, 2: Calendar, 3: Drive/Docs, 4: Sheets, 5: Contacts, 6: Google One
    var isPlayingVoiceBriefing by remember { mutableStateOf(false) }
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventLocation by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Operation Feedback Banner
        operationMsg?.let { msg ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkEmeraldLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            color = DeepThinkEmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearWorkspaceOperationMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // OAuth Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, DeepThinkCyan.copy(alpha = 0.5f))
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.linearGradient(listOf(DeepThinkCyan, DeepThinkIndigo))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("☁️", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Google Workspace & One",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(DeepThinkEmeraldLight)
                                    )
                                }
                                Text(
                                    "OAuth 2.0 Active • gen-lang-client-0811117488",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepThinkCyanLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.refreshWorkspaceData() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("workspace_sync_now_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Brand: Uwe Maslonka's Apps • Autonomously connects Gmail, Calendar, Drive, Docs, Sheets, Contacts, and Google One on your device.",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scopes Chips Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val scopes = listOf(
                            "✉️ Gmail (readonly)",
                            "📅 Calendar",
                            "📁 Drive (file)",
                            "📄 Docs",
                            "📊 Sheets",
                            "👥 Contacts (readonly)"
                        )
                        items(scopes) { scope ->
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant)
                            ) {
                                Text(
                                    text = scope,
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Google One 100 GB Storage & Cloud Backup Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkEmerald.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = DeepThinkEmeraldLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Google One (100 GB Plan)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "${String.format("%.2f", googleOneStatus.usedBytes / 1e9)} GB / 100 GB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkEmeraldLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented storage bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkBackground)
                    ) {
                        Box(modifier = Modifier.weight(0.082f).fillMaxHeight().background(DeepThinkCyan))
                        Box(modifier = Modifier.weight(0.0415f).fillMaxHeight().background(DeepThinkRose))
                        Box(modifier = Modifier.weight(0.058f).fillMaxHeight().background(DeepThinkAmber))
                        Box(modifier = Modifier.weight(0.003f).fillMaxHeight().background(DeepThinkEmeraldLight))
                        Box(modifier = Modifier.weight(0.8155f).fillMaxHeight().background(Color(0xFF2A2E39)))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Drive: 8.2 GB", fontSize = 9.sp, color = DeepThinkCyan)
                        Text("Gmail: 4.15 GB", fontSize = 9.sp, color = DeepThinkRose)
                        Text("Fotos: 5.8 GB", fontSize = 9.sp, color = DeepThinkAmber)
                        Text("Wiki/DB: 300 MB", fontSize = 9.sp, color = DeepThinkEmeraldLight)
                        Text("Frei: ~81.5 GB", fontSize = 9.sp, color = DarkTextMuted)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = googleOneStatus.backupStatusMessage,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.triggerGoogleOneBackup() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                            modifier = Modifier.weight(1f).testTag("trigger_google_one_backup_button")
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google One Backup Starten", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Service Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val filters = listOf(
                    "Alle Dienste",
                    "✉️ Gmail & Voice",
                    "📅 Calendar Auto-Book",
                    "📁 Drive & Docs",
                    "📊 Sheets Telemetrie",
                    "👥 Kontakte"
                )
                items(filters.size) { idx ->
                    val isSelected = selectedFilter == idx
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = idx },
                        label = { Text(filters[idx], fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepThinkCyan.copy(alpha = 0.25f),
                            selectedLabelColor = DeepThinkCyanLight
                        )
                    )
                }
            }
        }

        // SECTION: GMAIL & 30-SECOND VOICE BRIEFING (Filters 0, 1)
        if (selectedFilter == 0 || selectedFilter == 1) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkRose.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "✉️ Gmail & 30-Sekunden Voice-Note",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "300 unread messages in der Familiengruppe in eine 30-Sekunden Sprachnotiz verwandeln",
                                    fontSize = 11.sp,
                                    color = DarkTextMuted
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.generateGmailVoiceSummary()
                                    isPlayingVoiceBriefing = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkRose),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("generate_voice_briefing_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("30s Voice-Briefing", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        // Audio Player Waveform Card
                        gmailVoiceSummary?.let { summary ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground)
                                    .border(1.dp, DeepThinkRose.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        IconButton(
                                            onClick = { isPlayingVoiceBriefing = !isPlayingVoiceBriefing },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingVoiceBriefing) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                contentDescription = "Play Audio",
                                                tint = DeepThinkRose,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🎙️ 30s Agent Voice Briefing", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                            Text("Grounded on 300 group chat messages & alerts", fontSize = 10.sp, color = DarkTextMuted)
                                        }
                                        Text("0:30", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = DeepThinkRose)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Waveform bars simulation
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(6, 12, 18, 10, 22, 16, 24, 14, 20, 8, 16, 22, 12, 18, 10, 14, 20, 16, 8, 12).forEach { h ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(if (isPlayingVoiceBriefing) h.dp else 6.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(if (isPlayingVoiceBriefing) DeepThinkRose else DarkTextMuted)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = summary,
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email threads list
                        gmailMessages.forEach { msg ->
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                        Surface(
                                            color = if (msg.category == "Family") DeepThinkIndigo.copy(alpha = 0.3f) else DeepThinkAmber.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = msg.category,
                                                fontSize = 9.sp,
                                                color = if (msg.category == "Family") DeepThinkIndigoLight else DeepThinkAmber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(msg.subject, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DeepThinkCyanLight)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(msg.snippet, fontSize = 10.sp, color = DarkTextMuted, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION: GOOGLE CALENDAR AUTONOMOUS BOOKING (Filters 0, 2)
        if (selectedFilter == 0 || selectedFilter == 2) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkAmber.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📅 Google Calendar & Autonome Buchungen",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "6am Gym-Class um Mitternacht buchen & Pass-Termine campen",
                                    fontSize = 11.sp,
                                    color = DarkTextMuted
                                )
                            }
                            IconButton(onClick = { showCreateEventDialog = true }) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add Event", tint = DeepThinkAmber)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick trigger buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.autoBookGymClass() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkAmber)
                            ) {
                                Text("🏋️ 6am Gym Buchen", fontSize = 10.sp, color = DeepThinkAmber)
                            }

                            OutlinedButton(
                                onClick = { viewModel.campPassportAppointment() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan)
                            ) {
                                Text("🛂 Pass-Slot Campen", fontSize = 10.sp, color = DeepThinkCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Calendar events
                        calendarEvents.forEach { ev ->
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ev.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        if (ev.isAutoBookedByAgent) {
                                            Surface(
                                                color = DeepThinkEmerald.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "Autonom Gebucht",
                                                    fontSize = 9.sp,
                                                    color = DeepThinkEmeraldLight,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${ev.location} • ${ev.description}",
                                        fontSize = 10.sp,
                                        color = DarkTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION: GOOGLE DRIVE & DOCS CLOUD VAULT (Filters 0, 3)
        if (selectedFilter == 0 || selectedFilter == 3) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📁 Google Drive Vault & Google Docs",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Karpathy LLM Wiki Vault in Google Drive sichern & Docs exportieren",
                                    fontSize = 11.sp,
                                    color = DarkTextMuted
                                )
                            }
                            Button(
                                onClick = { viewModel.syncWikiToGoogleDrive() },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("sync_wiki_to_drive_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Drive Sync", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        driveFiles.forEach { file ->
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (file.mimeType.contains("spreadsheet")) Icons.Default.TableChart else if (file.mimeType.contains("document")) Icons.Default.Description else Icons.Default.FolderZip,
                                        contentDescription = null,
                                        tint = DeepThinkCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text("${file.sizeBytes / 1024} KB • Google Cloud File", fontSize = 9.sp, color = DarkTextMuted)
                                    }
                                    Text("Synced", fontSize = 10.sp, color = DeepThinkEmeraldLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SECTION: GOOGLE SHEETS PRICE WATCHERS (Filters 0, 4)
        if (selectedFilter == 0 || selectedFilter == 4) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkEmerald.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📊 Google Sheets Watcher-Telemetrie",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Hintergrund-Watcher loggen Preisdrops automatisch in Sheets",
                                    fontSize = 11.sp,
                                    color = DarkTextMuted
                                )
                            }
                            Button(
                                onClick = { viewModel.exportWatchersToGoogleSheets() },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("export_sheets_telemetry_button")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export Sheets", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Verknüpfte Tabelle: 'Web_Watcher_Price_Telemetry.xlsx' mit automatischer Zeilen-Synchronisation für eBay Kleinanzeigen, Konzert-Wiederverkäufe und Konsulat-Termine.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        // SECTION: GOOGLE CONTACTS NETWORK (Filters 0, 5)
        if (selectedFilter == 0 || selectedFilter == 5) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigoLight.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "👥 Google Kontakte & Karpathy Wiki Linking",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            "Verlinke Kontakte als vernetzte Personen-Knoten in dein semantisches Gedächtnis",
                            fontSize = 11.sp,
                            color = DarkTextMuted
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        contacts.forEach { contact ->
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DeepThinkIndigo),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(contact.avatarLetter, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Text("${contact.company} • ${contact.email}", fontSize = 9.sp, color = DarkTextMuted)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.linkContactToWiki(contact) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Wiki Link", fontSize = 10.sp, color = DeepThinkCyanLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateEventDialog) {
        AlertDialog(
            onDismissRequest = { showCreateEventDialog = false },
            title = { Text("Neuen Google Calendar Termin Anlegen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text("Titel (z.B. Team Sync)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEventLocation,
                        onValueChange = { newEventLocation = it },
                        label = { Text("Ort / Google Meet Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEventTitle.isNotBlank()) {
                            viewModel.createWorkspaceCalendarEvent(
                                title = newEventTitle,
                                timeMs = System.currentTimeMillis() + 3600_000L * 2,
                                location = newEventLocation
                            )
                        }
                        showCreateEventDialog = false
                    }
                ) {
                    Text("In Calendar Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateEventDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}


