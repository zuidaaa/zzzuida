package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.engine.SearchCitation
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextSubtle
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

/**
 * Reusable Thought Trace Component that visualizes AI internal reasoning chains,
 * supporting diverse presentation states: collapsed, expanded, live active reasoning,
 * extensive multi-step proof chains, truncated text mode, and verified search citations.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThoughtTraceComponent(
    thoughtText: String,
    modifier: Modifier = Modifier,
    isThinkingLive: Boolean = false,
    durationMs: Long = 0L,
    thinkingTokens: Int = 0,
    steps: List<ReasoningStep> = emptyList(),
    currentStep: ReasoningStep? = null,
    citations: List<SearchCitation> = emptyList(),
    initialExpanded: Boolean = isThinkingLive,
    isTruncatedPreview: Boolean = false,
    maxPreviewLines: Int = 4,
    modelName: String = "Gemini 3.7 Deep Thinking",
    onInspectTree: () -> Unit = {},
    onDebugNodes: (stepIndex: Int) -> Unit = {},
    onExport: () -> Unit = {}
) {
    var isExpanded by remember(initialExpanded) { mutableStateOf(initialExpanded) }
    val formattedDuration = String.format(java.util.Locale.US, "%.1fs", (durationMs / 1000f).coerceAtLeast(0.1f))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                1.2.dp,
                if (isThinkingLive) DeepThinkIndigo.copy(alpha = 0.75f) else DarkOutlineVariant,
                RoundedCornerShape(16.dp)
            )
            .testTag("thought_trace_component_card"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row (48dp min touch target)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = if (isExpanded) "Collapse Thought Trace" else "Expand Thought Trace"
                    ) { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("thought_trace_header"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isThinkingLive) {
                                        listOf(DeepThinkIndigo, DeepThinkViolet)
                                    } else {
                                        listOf(DeepThinkIndigo.copy(alpha = 0.7f), DeepThinkViolet.copy(alpha = 0.5f))
                                    }
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Thought Process",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isThinkingLive) "Deep Thinking in progress..." else "Reasoning Thought Trace",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isThinkingLive) DeepThinkIndigoLight else Color.White,
                                    fontSize = 13.sp
                                )
                            )

                            if (steps.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(DeepThinkIndigo.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${steps.size} steps",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DeepThinkIndigoLight,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Meta line: duration & tokens
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Thought for $formattedDuration",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 10.5.sp
                                )
                            )

                            if (thinkingTokens > 0) {
                                Text(
                                    text = " • $thinkingTokens tokens",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepThinkVioletLight.copy(alpha = 0.8f),
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Expand/Collapse Icon
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = DarkTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Expanded Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp)
                ) {
                    // Active live step indicator if currently generating
                    if (isThinkingLive && currentStep != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DeepThinkIndigo.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigo.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Active Step",
                                    tint = DeepThinkIndigoLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Executing: ${currentStep.headline}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                    if (currentStep.details.isNotBlank()) {
                                        Text(
                                            text = currentStep.details,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = DarkTextMuted,
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Raw Thought Stream Monospace Container
                    if (thoughtText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("thought_trace_stream_box")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = thoughtText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.5.sp,
                                        color = DarkTextMuted
                                    ),
                                    maxLines = if (isTruncatedPreview) maxPreviewLines else Int.MAX_VALUE,
                                    overflow = if (isTruncatedPreview) TextOverflow.Ellipsis else TextOverflow.Clip
                                )

                                if (isTruncatedPreview) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "... [Truncated for preview]",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DeepThinkAmber,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Multi-Step Breakdown
                    if (steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Proof & Reasoning Pipeline (${steps.size} verified nodes)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepThinkVioletLight,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            steps.forEach { step ->
                                ThoughtTraceStepCard(
                                    step = step,
                                    onDebugClick = { onDebugNodes(step.stepIndex) }
                                )
                            }
                        }
                    }

                    // Verified Citations
                    if (citations.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Search Grounding Sources (${citations.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepThinkCyan,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            citations.forEach { citation ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkCyan.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Link,
                                            contentDescription = "Source",
                                            tint = DeepThinkCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = citation.sourceDomain.ifBlank { citation.title },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Controls: Inspect Tree, Debug CoT, Export Trace
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onInspectTree,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DeepThinkIndigoLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigo.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("inspect_trace_tree_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = "Inspect Tree",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Inspect Tree", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onExport,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DeepThinkVioletLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkViolet.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("export_trace_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Document",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Export MD/PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThoughtTraceStepCard(
    step: ReasoningStep,
    onDebugClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (step.phase) {
        ReasoningPhase.DECONSTRUCT -> DeepThinkIndigoLight
        ReasoningPhase.EXPLORE_BRANCHES -> DeepThinkVioletLight
        ReasoningPhase.VERIFY_TRACE -> DeepThinkEmeraldLight
        ReasoningPhase.SELF_CORRECTION -> DeepThinkRose
        ReasoningPhase.MCP_TOOL -> DeepThinkCyan
        ReasoningPhase.SYNTHESIS -> DeepThinkAmber
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
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
                            .background(phaseColor.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
                            .border(0.8.dp, phaseColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
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

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Step ${step.stepIndex}: ${step.headline}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (step.verified) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = DeepThinkEmeraldLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug",
                        tint = DarkTextSubtle,
                        modifier = Modifier
                            .size(13.dp)
                            .clickable(onClick = onDebugClick)
                    )
                }
            }

            if (step.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = DarkTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}
