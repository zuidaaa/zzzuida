package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun ThoughtTreeDialog(
    steps: List<ReasoningStep>,
    onDismiss: () -> Unit,
    onOpenDebugOverlay: (stepIndex: Int) -> Unit = {}
) {
    var selectedStepIndex by remember { mutableStateOf(0) }
    val activeStep = steps.getOrNull(selectedStepIndex) ?: steps.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .border(1.2.dp, DeepThinkIndigo.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = DarkBackground
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Header with accessible close button
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
                                        listOf(DeepThinkIndigo.copy(alpha = 0.25f), DeepThinkViolet.copy(alpha = 0.25f))
                                    )
                                )
                                .border(1.dp, DeepThinkIndigoLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = DeepThinkCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cognitive Reasoning Tree",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Gemini 3.7 Deep Think Tree Explorer",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DeepThinkIndigoLight,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_tree_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Reasoning Tree",
                            tint = DarkTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Node Chain (Step Progress Map)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(steps) { index, step ->
                        ReasoningTreeNodeRow(
                            step = step,
                            isSelected = index == selectedStepIndex,
                            isLast = index == steps.lastIndex,
                            onClick = { selectedStepIndex = index }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail Inspector Box for selected step
                if (activeStep != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SELECTED NODE: STEP ${activeStep.stepIndex}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepThinkIndigoLight,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Confidence score",
                                        tint = DeepThinkEmeraldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${(activeStep.confidence * 100).toInt()}% Confidence",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DeepThinkEmeraldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = activeStep.headline,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeStep.details,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = DarkTextMuted,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenDebugOverlay(selectedStepIndex)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .testTag("open_node_debugger_from_tree_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = DeepThinkCyan
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, DeepThinkCyan.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    tint = DeepThinkCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Inspect Node Payloads in Debug Overlay",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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
fun ReasoningTreeNodeRow(
    step: ReasoningStep,
    isSelected: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val phaseColor = when (step.phase) {
        ReasoningPhase.DECONSTRUCT -> DeepThinkIndigoLight
        ReasoningPhase.EXPLORE_BRANCHES -> DeepThinkViolet
        ReasoningPhase.MCP_TOOL -> DeepThinkAmber
        ReasoningPhase.VERIFY_TRACE -> DeepThinkEmeraldLight
        ReasoningPhase.SELF_CORRECTION -> DeepThinkRose
        ReasoningPhase.SYNTHESIS -> DeepThinkCyan
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Select Step ${step.stepIndex}: ${step.headline}",
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Line and Node Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isSelected) 18.dp else 14.dp)
                    .background(if (isSelected) phaseColor else phaseColor.copy(alpha = 0.5f), CircleShape)
                    .border(
                        2.dp,
                        if (isSelected) Color.White else Color.Transparent,
                        CircleShape
                    )
            )
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                ) {
                    drawLine(
                        color = phaseColor.copy(alpha = 0.4f),
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .border(
                    if (isSelected) 1.2.dp else 0.8.dp,
                    if (isSelected) phaseColor else DarkOutlineVariant,
                    RoundedCornerShape(12.dp)
                ),
            color = if (isSelected) DarkSurfaceVariant else DarkSurface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "[${step.phase.badge}]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = phaseColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = step.headline,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = step.branchLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}

