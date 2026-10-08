package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.DeepThinkIndigoGlow
import com.example.ui.theme.DeepThinkIndigoLight
import com.example.ui.theme.DeepThinkRose
import com.example.ui.theme.DeepThinkViolet
import com.example.ui.theme.DeepThinkVioletLight
import com.example.ui.theme.ThoughtAccent
import com.example.ui.theme.ThoughtBackground
import com.example.ui.theme.ThoughtBorder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtAccordion(
    thoughtText: String,
    isThinkingLive: Boolean = false,
    durationMs: Long = 0L,
    thinkingTokens: Int = 0,
    steps: List<ReasoningStep> = emptyList(),
    currentStep: ReasoningStep? = null,
    onInspectTree: () -> Unit = {},
    onDebugNodes: (initialIndex: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(isThinkingLive) }

    // Pulsing neural transition for active deep reasoning
    val infiniteTransition = rememberInfiniteTransition(label = "deep_think_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val formattedDuration = String.format(java.util.Locale.US, "%.1fs", (durationMs / 1000f).coerceAtLeast(0.1f))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                1.2.dp,
                if (isThinkingLive) DeepThinkIndigo.copy(alpha = 0.7f) else DarkOutlineVariant,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row (Clickable with minimum 48dp touch target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (isExpanded) "Collapse Deep Thinking Trace" else "Expand Deep Thinking Trace"
                    ) { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("thought_accordion_header"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isThinkingLive) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                                .background(
                                    Brush.radialGradient(
                                        listOf(DeepThinkIndigo, DeepThinkViolet, Color.Transparent)
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Active Deep Thinking",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(DeepThinkIndigo.copy(alpha = 0.18f))
                                .border(0.8.dp, DeepThinkIndigo.copy(alpha = 0.4f), RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Deep Reasoning Trace",
                                tint = DeepThinkIndigoLight,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isThinkingLive) {
                                currentStep?.headline ?: "Reasoning with Gemini 3.7 Deep Think Engine..."
                            } else {
                                "Reasoned for $formattedDuration"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isThinkingLive) DeepThinkIndigoGlow else Color.White
                            ),
                            maxLines = 1
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (thinkingTokens > 0) "$thinkingTokens thinking tokens" else "Multi-branch verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                            if (!isThinkingLive && steps.isNotEmpty()) {
                                Text(
                                    text = "• ${steps.size} cognitive steps",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepThinkIndigoLight,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse Reasoning Trace" else "Expand Reasoning Trace",
                        tint = if (isExpanded) DeepThinkCyan else DarkTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable Content with smooth transition
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground.copy(alpha = 0.9f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (steps.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STRUCTURED COGNITIVE TRACE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepThinkIndigoLight,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 9.5.sp
                                )
                            )
                            Text(
                                text = "Tap node to inspect payload",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 9.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        steps.forEachIndexed { idx, step ->
                            ReasoningStepItem(
                                step = step,
                                onClick = { onDebugNodes(idx) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    } else if (thoughtText.isNotBlank()) {
                        Text(
                            text = thoughtText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFC4C7D0),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    if (steps.isNotEmpty() && !isThinkingLive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDebugNodes(0) },
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .weight(1.2f, fill = false)
                                    .testTag("open_cot_debug_overlay_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DeepThinkCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, DeepThinkCyan.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Debug Nodes",
                                    tint = DeepThinkCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Debug CoT Nodes",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            OutlinedButton(
                                onClick = onInspectTree,
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .weight(1f, fill = false)
                                    .testTag("inspect_reasoning_tree_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DeepThinkIndigoLight
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigo.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountTree,
                                    contentDescription = "Visual Reasoning Tree",
                                    tint = DeepThinkIndigoLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Visual Tree",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
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
fun ReasoningStepItem(
    step: ReasoningStep,
    onClick: () -> Unit = {}
) {
    val phaseColor = when (step.phase) {
        ReasoningPhase.DECONSTRUCT -> DeepThinkIndigoLight
        ReasoningPhase.EXPLORE_BRANCHES -> DeepThinkViolet
        ReasoningPhase.MCP_TOOL -> DeepThinkAmber
        ReasoningPhase.VERIFY_TRACE -> DeepThinkEmeraldLight
        ReasoningPhase.SELF_CORRECTION -> DeepThinkRose
        ReasoningPhase.SYNTHESIS -> DeepThinkCyan
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = "Inspect Step ${step.stepIndex}: ${step.headline}",
                onClick = onClick
            )
            .border(0.8.dp, DarkOutlineVariant, RoundedCornerShape(12.dp)),
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
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
                            .background(phaseColor.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                            .border(0.8.dp, phaseColor.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = step.phase.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = phaseColor,
                                fontSize = 9.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step ${step.stepIndex}: ${step.headline}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (step.verified) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Step Verified",
                            tint = DeepThinkEmeraldLight,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug Node",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = step.details,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = DarkTextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )
        }
    }
}

