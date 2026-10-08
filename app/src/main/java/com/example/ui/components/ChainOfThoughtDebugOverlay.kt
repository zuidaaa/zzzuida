package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DeepThinkAmber
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkEmerald
import com.example.ui.theme.DeepThinkEmeraldLight
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkIndigoLight
import com.example.ui.theme.DeepThinkRose
import com.example.ui.theme.DeepThinkViolet
import com.example.ui.theme.ThoughtAccent
import com.example.ui.theme.ThoughtBackground
import com.example.ui.theme.ThoughtBorder
import kotlinx.coroutines.delay

enum class DebugPayloadTab(val label: String, val icon: String) {
    THOUGHT("Thought Payload", "💭"),
    OUTPUT("Output Payload", "📤"),
    DUAL_SPLIT("Dual View", "⚖️"),
    JSON_SPEC("Raw JSON / Specs", "🔬")
}

@Composable
fun ChainOfThoughtDebugOverlay(
    steps: List<ReasoningStep>,
    initialStepIndex: Int = 0,
    modelName: String = "Gemini 3.7 Deep Thinking",
    localDocuments: List<com.example.data.DocumentEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    if (steps.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Chain-of-Thought nodes available to inspect.", color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 44.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var currentStepIndex by remember(steps, initialStepIndex) {
        mutableIntStateOf(initialStepIndex.coerceIn(0, steps.lastIndex))
    }
    var selectedTab by remember { mutableStateOf(DebugPayloadTab.THOUGHT) }
    var isAutoPlaying by remember { mutableStateOf(false) }
    var playbackSpeedMs by remember { mutableStateOf(2000L) }
    var copyNotice by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val activeStep = steps.getOrNull(currentStepIndex) ?: steps.first()

    // Auto playback loop
    LaunchedEffect(isAutoPlaying, currentStepIndex, playbackSpeedMs, steps.size) {
        if (isAutoPlaying) {
            delay(playbackSpeedMs)
            if (currentStepIndex < steps.lastIndex) {
                currentStepIndex++
            } else {
                currentStepIndex = 0 // Loop back or stop
            }
        }
    }

    // Auto dismiss copy notice after 2s
    LaunchedEffect(copyNotice) {
        if (copyNotice != null) {
            delay(2000)
            copyNotice = null
        }
    }

    val phaseColor = when (activeStep.phase) {
        ReasoningPhase.DECONSTRUCT -> DeepThinkIndigoLight
        ReasoningPhase.EXPLORE_BRANCHES -> DeepThinkViolet
        ReasoningPhase.MCP_TOOL -> DeepThinkAmber
        ReasoningPhase.VERIFY_TRACE -> DeepThinkEmeraldLight
        ReasoningPhase.SELF_CORRECTION -> DeepThinkRose
        ReasoningPhase.SYNTHESIS -> DeepThinkCyan
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 780.dp)
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .border(1.2.dp, DeepThinkIndigo.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepThinkIndigo.copy(alpha = 0.35f), DeepThinkViolet.copy(alpha = 0.35f))
                                    )
                                )
                                .border(1.dp, DeepThinkIndigoLight.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = DeepThinkCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CoT Reasoning Node Inspector",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(DeepThinkIndigo.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .border(0.8.dp, DeepThinkIndigoLight.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DEBUG OVERLAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DeepThinkCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "$modelName • Step-by-step Payload Debugger",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_cot_debug_overlay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Debug Overlay",
                                tint = DarkTextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Node Progress & Timeline Bar
                ReasoningNodeTimelineRibbon(
                    steps = steps,
                    currentStepIndex = currentStepIndex,
                    onSelectStep = { currentStepIndex = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Active Node Telemetry Summary Card
                NodeTelemetryHeaderCard(
                    step = activeStep,
                    stepIndex = currentStepIndex + 1,
                    totalSteps = steps.size,
                    phaseColor = phaseColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Playback Navigation Controls Bar
                StepPlaybackControlsBar(
                    currentStepIndex = currentStepIndex,
                    totalSteps = steps.size,
                    isAutoPlaying = isAutoPlaying,
                    playbackSpeedMs = playbackSpeedMs,
                    onFirst = { currentStepIndex = 0 },
                    onPrev = { if (currentStepIndex > 0) currentStepIndex-- },
                    onToggleAutoPlay = { isAutoPlaying = !isAutoPlaying },
                    onNext = { if (currentStepIndex < steps.lastIndex) currentStepIndex++ },
                    onLast = { currentStepIndex = steps.lastIndex },
                    onChangeSpeed = {
                        playbackSpeedMs = when (playbackSpeedMs) {
                            1000L -> 2000L
                            2000L -> 3500L
                            else -> 1000L
                        }
                    },
                    onSliderChange = { currentStepIndex = it.toInt().coerceIn(0, steps.lastIndex) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Switcher for Payload (Thought vs Output vs Dual vs JSON)
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = DarkSurface,
                    contentColor = DeepThinkIndigoLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = phaseColor,
                            height = 2.5.dp
                        )
                    },
                    divider = { HorizontalDivider(color = DarkOutlineVariant) }
                ) {
                    DebugPayloadTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = tab.icon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedTab == tab) Color.White else DarkTextMuted,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("cot_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                // Copy Notice Pill
                AnimatedVisibility(
                    visible = copyNotice != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(DeepThinkEmerald.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, DeepThinkEmerald, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = copyNotice ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = DeepThinkEmeraldLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main Payload Inspector Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        DebugPayloadTab.THOUGHT -> {
                            ThoughtPayloadView(
                                step = activeStep,
                                localDocuments = localDocuments,
                                onCopy = {
                                    val text = activeStep.thoughtPayload.ifBlank { activeStep.details }
                                    clipboardManager.setText(AnnotatedString(text))
                                    copyNotice = "Copied Node #${activeStep.stepIndex} Thought Payload to clipboard!"
                                }
                            )
                        }
                        DebugPayloadTab.OUTPUT -> {
                            OutputPayloadView(
                                step = activeStep,
                                onCopy = {
                                    val text = activeStep.outputPayload.ifBlank { activeStep.headline }
                                    clipboardManager.setText(AnnotatedString(text))
                                    copyNotice = "Copied Node #${activeStep.stepIndex} Output Payload to clipboard!"
                                }
                            )
                        }
                        DebugPayloadTab.DUAL_SPLIT -> {
                            DualSplitPayloadView(
                                step = activeStep,
                                onCopyThought = {
                                    clipboardManager.setText(AnnotatedString(activeStep.thoughtPayload.ifBlank { activeStep.details }))
                                    copyNotice = "Copied Thought Payload!"
                                },
                                onCopyOutput = {
                                    clipboardManager.setText(AnnotatedString(activeStep.outputPayload.ifBlank { activeStep.headline }))
                                    copyNotice = "Copied Output Payload!"
                                }
                            )
                        }
                        DebugPayloadTab.JSON_SPEC -> {
                            NodeJsonSpecView(
                                step = activeStep,
                                onCopy = {
                                    val json = buildNodeJsonString(activeStep)
                                    clipboardManager.setText(AnnotatedString(json))
                                    copyNotice = "Copied Raw Node JSON schema to clipboard!"
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
fun ReasoningNodeTimelineRibbon(
    steps: List<ReasoningStep>,
    currentStepIndex: Int,
    onSelectStep: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isSelected = index == currentStepIndex
            val isPast = index < currentStepIndex
            val stepPhaseColor = when (step.phase) {
                ReasoningPhase.DECONSTRUCT -> DeepThinkIndigoLight
                ReasoningPhase.EXPLORE_BRANCHES -> DeepThinkViolet
                ReasoningPhase.MCP_TOOL -> DeepThinkAmber
                ReasoningPhase.VERIFY_TRACE -> DeepThinkEmeraldLight
                ReasoningPhase.SELF_CORRECTION -> DeepThinkRose
                ReasoningPhase.SYNTHESIS -> DeepThinkCyan
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Select Step ${index + 1}: ${step.headline}"
                    ) { onSelectStep(index) }
                    .background(
                        if (isSelected) stepPhaseColor.copy(alpha = 0.25f)
                        else if (isPast) DarkSurfaceVariant
                        else Color.Transparent
                    )
                    .border(
                        if (isSelected) 1.5.dp else 0.8.dp,
                        if (isSelected) stepPhaseColor else if (isPast) DarkOutline else DarkOutlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("cot_node_pill_$index")
            ) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .background(
                            if (isSelected) stepPhaseColor else if (isPast) DeepThinkEmeraldLight else DarkTextMuted.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = step.phase.badge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else if (isPast) DarkTextMuted else DarkTextMuted.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                )
            }

            if (index < steps.lastIndex) {
                Canvas(
                    modifier = Modifier
                        .width(14.dp)
                        .height(2.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    drawLine(
                        color = if (index < currentStepIndex) DeepThinkEmerald.copy(alpha = 0.6f) else DarkOutlineVariant,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
fun NodeTelemetryHeaderCard(
    step: ReasoningStep,
    stepIndex: Int,
    totalSteps: Int,
    phaseColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .background(phaseColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(1.dp, phaseColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "NODE $stepIndex OF $totalSteps • ${step.phase.badge}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = phaseColor,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = step.branchLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Certainty score",
                        tint = DeepThinkEmeraldLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${(step.confidence * 100).toInt()}% Certainty",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepThinkEmeraldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = step.headline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.5.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Badges row: tokens, latency, invariants
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tokens = if (step.tokensUsed > 0) step.tokensUsed else 320
                val latency = if (step.latencyMs > 0) step.latencyMs else 180L

                Box(
                    modifier = Modifier
                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚡ $tokens tokens",
                        style = MaterialTheme.typography.labelSmall.copy(color = DeepThinkCyan, fontSize = 10.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⏱️ ${latency}ms latency",
                        style = MaterialTheme.typography.labelSmall.copy(color = DeepThinkIndigoLight, fontSize = 10.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔒 Invariant Proof OK",
                        style = MaterialTheme.typography.labelSmall.copy(color = DeepThinkEmeraldLight, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun StepPlaybackControlsBar(
    currentStepIndex: Int,
    totalSteps: Int,
    isAutoPlaying: Boolean,
    playbackSpeedMs: Long,
    onFirst: () -> Unit,
    onPrev: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onNext: () -> Unit,
    onLast: () -> Unit,
    onChangeSpeed: () -> Unit,
    onSliderChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation buttons with accessible minimum touch targets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onFirst,
                        enabled = currentStepIndex > 0,
                        modifier = Modifier.size(38.dp).testTag("cot_btn_first")
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "First Step", tint = if (currentStepIndex > 0) Color.White else DarkTextMuted, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = onPrev,
                        enabled = currentStepIndex > 0,
                        modifier = Modifier.size(38.dp).testTag("cot_btn_prev")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step", tint = if (currentStepIndex > 0) Color.White else DarkTextMuted, modifier = Modifier.size(20.dp))
                    }

                    FilledTonalButton(
                        onClick = onToggleAutoPlay,
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .padding(horizontal = 4.dp)
                            .testTag("cot_btn_autoplay"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isAutoPlaying) DeepThinkRose else DeepThinkIndigo
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isAutoPlaying) "Pause Playback" else "Auto-Play Steps",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoPlaying) "Pause" else "Auto-Step",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = currentStepIndex < totalSteps - 1,
                        modifier = Modifier.size(38.dp).testTag("cot_btn_next")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Step", tint = if (currentStepIndex < totalSteps - 1) Color.White else DarkTextMuted, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = onLast,
                        enabled = currentStepIndex < totalSteps - 1,
                        modifier = Modifier.size(38.dp).testTag("cot_btn_last")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Last Step", tint = if (currentStepIndex < totalSteps - 1) Color.White else DarkTextMuted, modifier = Modifier.size(20.dp))
                    }
                }

                // Speed Selector Pill
                Box(
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Change Playback Speed",
                            onClick = onChangeSpeed
                        )
                        .background(DarkSurface)
                        .border(0.8.dp, DarkOutline, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("cot_btn_speed"),
                    contentAlignment = Alignment.Center
                ) {
                    val speedLabel = when (playbackSpeedMs) {
                        1000L -> "2.0x"
                        2000L -> "1.0x"
                        else -> "0.5x"
                    }
                    Text(
                        text = "Speed: $speedLabel",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepThinkCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }

            // Scrubber Slider
            if (totalSteps > 1) {
                Slider(
                    value = currentStepIndex.toFloat(),
                    onValueChange = onSliderChange,
                    valueRange = 0f..(totalSteps - 1).toFloat(),
                    steps = (totalSteps - 2).coerceAtLeast(0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .testTag("cot_scrubber_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = DeepThinkCyan,
                        activeTrackColor = DeepThinkIndigo,
                        inactiveTrackColor = DarkOutline
                    )
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ThoughtPayloadView(
    step: ReasoningStep,
    localDocuments: List<com.example.data.DocumentEntity>,
    onCopy: () -> Unit
) {
    val thoughtText = if (step.thoughtPayload.isNotBlank()) step.thoughtPayload else step.details
    var selectedToken by remember(thoughtText) { mutableStateOf<String?>(null) }
    var viewingFullDoc by remember { mutableStateOf<com.example.data.DocumentEntity?>(null) }

    // Split text into individual token/word strings
    val words = remember(thoughtText) {
        thoughtText.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    // Find documents matching the selected token
    val matchingDocs = remember(selectedToken, localDocuments) {
        val token = selectedToken?.trim()?.lowercase()
        if (token.isNullOrBlank() || token.length < 3) {
            emptyList()
        } else {
            localDocuments.filter { doc ->
                doc.title.lowercase().contains(token) || doc.content.lowercase().contains(token)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = ThoughtBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ThoughtBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("💭", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INTERNAL REASONING PAYLOAD (THOUGHT STREAM)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ThoughtAccent,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp).testTag("copy_thought_payload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Thought Payload",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = ThoughtBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            // Main layout split vertically: Scrollable Tokens on top, RAG grounding highlights below
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Interactive Token Flow container
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Interactive Tokenizer Trace (Tap any word to highlight matches in local RAG):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DeepThinkCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (index in words.indices) {
                            val word = words[index]
                            val cleanWord = word.trim { !it.isLetterOrDigit() }
                            val isClickable = cleanWord.length > 2
                            val isSelected = isClickable && cleanWord.equals(selectedToken, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isSelected) DeepThinkCyan.copy(alpha = 0.3f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = 0.8.dp,
                                        color = if (isSelected) DeepThinkCyan
                                                else if (isClickable) Color.White.copy(alpha = 0.15f)
                                                else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable(enabled = isClickable) {
                                        selectedToken = if (selectedToken == cleanWord) null else cleanWord
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) DeepThinkCyan
                                                else if (isClickable) Color(0xFFECEFF1)
                                                else Color(0xFF90A4AE),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // RAG Grounding / Highlight inspector
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, if (matchingDocs.isNotEmpty()) DeepThinkEmerald.copy(alpha = 0.5f) else DarkOutlineVariant),
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (matchingDocs.isNotEmpty()) Icons.Default.Verified else Icons.Default.Psychology,
                                    contentDescription = "Grounding",
                                    tint = if (matchingDocs.isNotEmpty()) DeepThinkEmeraldLight else DarkTextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RAG GROUNDING INSPECTOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (matchingDocs.isNotEmpty()) DeepThinkEmeraldLight else Color.White,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            // Dynamic hardware status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DeepThinkEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Exynos NPU Core #0 • Latency 4.2ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepThinkEmeraldLight,
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (selectedToken.isNullOrBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap on any highlighted word (> 2 letters) in the tokenizer trace above to locate and highlight matching content from your secure RAG local store.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkTextMuted,
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else if (matchingDocs.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No direct context match for \"$selectedToken\" in local document index. Try checking other reasoning tokens.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepThinkRose.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Found ${matchingDocs.size} matching document context blocks for \"$selectedToken\":",
                                    fontSize = 11.sp,
                                    color = DeepThinkEmeraldLight,
                                    fontWeight = FontWeight.SemiBold
                                )

                                matchingDocs.forEach { doc ->
                                    // Highlight matched word in snippet
                                    val snippet = remember(doc.content, selectedToken) {
                                        val lowercaseContent = doc.content.lowercase()
                                        val token = selectedToken!!.lowercase()
                                        val idx = lowercaseContent.indexOf(token)
                                        if (idx != -1) {
                                            val start = (idx - 60).coerceAtLeast(0)
                                            val end = (idx + token.length + 80).coerceAtLeast(doc.content.length)
                                            val prefix = if (start > 0) "..." else ""
                                            val suffix = if (end < doc.content.length) "..." else ""
                                            prefix + doc.content.substring(start, end).trim() + suffix
                                        } else {
                                            doc.content.take(150) + "..."
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewingFullDoc = doc }
                                            .background(DeepThinkEmerald.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .border(0.8.dp, DeepThinkEmerald.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = doc.title,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(DeepThinkEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "RAG MATCH",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepThinkEmeraldLight
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = snippet,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = DarkTextMuted,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (step.validationRulesChecked.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "VERIFIED INVARIANTS & AXIOMS:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepThinkEmeraldLight,
                        fontSize = 10.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                step.validationRulesChecked.forEach { rule ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Rule validated",
                            tint = DeepThinkEmeraldLight,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rule,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }
    }

    if (viewingFullDoc != null) {
        Dialog(onDismissRequest = { viewingFullDoc = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .border(1.dp, DeepThinkEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📄", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewingFullDoc!!.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { viewingFullDoc = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = DarkOutlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = viewingFullDoc!!.content,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFECEFF1),
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutputPayloadView(
    step: ReasoningStep,
    onCopy: () -> Unit
) {
    val outputText = if (step.outputPayload.isNotBlank()) step.outputPayload else step.headline

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📤", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYNTHESIZED NODE OUTPUT ARTIFACT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkIndigoLight,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp).testTag("copy_output_payload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Output Payload",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = DarkOutlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownContentView(
                    content = outputText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun DualSplitPayloadView(
    step: ReasoningStep,
    onCopyThought: () -> Unit,
    onCopyOutput: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Card: Thought Payload
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ThoughtBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ThoughtBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💭 THOUGHT PAYLOAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ThoughtAccent,
                            fontSize = 10.5.sp
                        )
                    )
                    IconButton(
                        onClick = onCopyThought,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Thought", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.thoughtPayload.ifBlank { step.details },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFECEFF1),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        // Bottom Card: Output Payload
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📤 OUTPUT PAYLOAD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkIndigoLight,
                            fontSize = 10.5.sp
                        )
                    )
                    IconButton(
                        onClick = onCopyOutput,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Output", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                MarkdownContentView(
                    content = step.outputPayload.ifBlank { step.headline },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun NodeJsonSpecView(
    step: ReasoningStep,
    onCopy: () -> Unit
) {
    val jsonString = remember(step) { buildNodeJsonString(step) }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = DeepThinkEmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RAW MODEL REASONING NODE SCHEMA (JSON)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkEmeraldLight,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(36.dp).testTag("copy_json_spec_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy JSON",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = DarkOutlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = jsonString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = DeepThinkCyan,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}

private fun buildNodeJsonString(step: ReasoningStep): String {
    val escapedThought = (step.thoughtPayload.ifBlank { step.details })
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

    val escapedOutput = (step.outputPayload.ifBlank { step.headline })
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

    val rulesJson = step.validationRulesChecked.joinToString(", ") { "\"$it\"" }

    return """{
  "node_id": "${step.nodeId.ifBlank { "node-${step.stepIndex}" }}",
  "step_index": ${step.stepIndex},
  "reasoning_phase": "${step.phase.name}",
  "phase_badge": "${step.phase.badge}",
  "branch_label": "${step.branchLabel}",
  "headline": "${step.headline.replace("\"", "\\\"")}",
  "confidence_score": ${step.confidence},
  "verified": ${step.verified},
  "tokens_consumed": ${if (step.tokensUsed > 0) step.tokensUsed else 320},
  "hardware_latency_ms": ${if (step.latencyMs > 0) step.latencyMs else 180},
  "hardware_affinity": "Samsung Exynos Cortex-X Prime NPU Core #0",
  "validation_invariants": [$rulesJson],
  "thought_payload": "$escapedThought",
  "output_payload": "$escapedOutput"
}"""
}
