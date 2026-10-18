package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.engine.ReasoningPhase
import com.example.engine.ReasoningStep
import com.example.ui.theme.*

/**
 * Refined 3D tilted stacking card layout for Chain-of-Thought reasoning steps.
 * Provides transparency and error analysis by breaking down intermediate steps.
 */
@Composable
fun ThoughtStackVisualizer(
    steps: List<ReasoningStep>,
    modifier: Modifier = Modifier,
    onStepClick: (ReasoningStep) -> Unit = {}
) {
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("thought_stack_visualizer")
    ) {
        Text(
            text = "REASONING PIPELINE: VERIFIED NODES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = VibrantPurple,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        ) {
            steps.forEachIndexed { index, step ->
                val isExpanded = expandedIndex == index
                val offsetTransition by animateFloatAsState(
                    targetValue = if (isExpanded) 0f else (index * 12f),
                    animationSpec = tween(500),
                    label = "offset"
                )
                val rotationTransition by animateFloatAsState(
                    targetValue = if (isExpanded) 0f else (-5f + (index * 2f)),
                    animationSpec = tween(500),
                    label = "rotation"
                )
                val scaleTransition by animateFloatAsState(
                    targetValue = if (isExpanded) 1f else (1f - (index * 0.02f)),
                    animationSpec = tween(500),
                    label = "scale"
                )

                ThoughtStackCard(
                    step = step,
                    isExpanded = isExpanded,
                    modifier = Modifier
                        .zIndex(if (isExpanded) 100f else (steps.size - index).toFloat())
                        .graphicsLayer {
                            translationY = offsetTransition.dp.toPx()
                            rotationZ = rotationTransition
                            scaleX = scaleTransition
                            scaleY = scaleTransition
                            cameraDistance = 12f * density
                        }
                        .clickable {
                            expandedIndex = if (expandedIndex == index) -1 else index
                            onStepClick(step)
                        },
                    onClose = { expandedIndex = -1 }
                )
            }
        }
    }
}

@Composable
fun ThoughtStackCard(
    step: ReasoningStep,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val phaseColor = when (step.phase) {
        ReasoningPhase.DECONSTRUCT -> LuminousBlue
        ReasoningPhase.EXPLORE_BRANCHES -> VibrantPurple
        ReasoningPhase.VERIFY_TRACE -> VibrantGreen
        ReasoningPhase.SELF_CORRECTION -> VibrantRed
        ReasoningPhase.MCP_TOOL -> VibrantTeal
        ReasoningPhase.SYNTHESIS -> LuminousYellow
    }

    val tagVariant = when (step.phase) {
        ReasoningPhase.DECONSTRUCT -> TagVariant.INFO_BLUE
        ReasoningPhase.EXPLORE_BRANCHES -> TagVariant.REASONING_PURPLE
        ReasoningPhase.VERIFY_TRACE -> TagVariant.STATUS_GREEN
        ReasoningPhase.SELF_CORRECTION -> TagVariant.INFO_BLUE // Red variant not in enum, use Blue
        ReasoningPhase.MCP_TOOL -> TagVariant.TECHNICAL_TEAL
        ReasoningPhase.SYNTHESIS -> TagVariant.MODE_YELLOW
    }

    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = if (isExpanded) 400.dp else 100.dp),
        blurAlpha = if (isExpanded) 0.95f else 0.85f,
        cornerRadius = 20f
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FunctionalTag(
                    text = step.phase.badge,
                    variant = tagVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STEP ${step.stepIndex}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
            }

            if (isExpanded) {
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            } else {
                if (step.verified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = VibrantGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = step.headline,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            maxLines = if (isExpanded) 3 else 1,
            overflow = TextOverflow.Ellipsis
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AbsoluteBlack.copy(alpha = 0.4f))
                    .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyColumn {
                    item {
                        Text(
                            text = step.details,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                    if (step.thoughtPayload.isNotBlank()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "LOGGING_STREAM:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = VibrantTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = step.thoughtPayload,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = VibrantTeal.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${step.latencyMs}ms",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Token, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${step.tokensUsed} tokens",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled)
                    )
                }
            }
        }
    }
}
