package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.debug.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ThinkingAiDebuggerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val debuggerState by viewModel.thinkingDebuggerState.collectAsStateWithLifecycle()
    var expandedSegmentId by remember { mutableStateOf<String?>(LlmPipelineSegment.CONTEXT_WINDOW.id) }
    var isThinkingFeedExpanded by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .testTag("thinking_ai_debugger_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (debuggerState.isLiveDiagnosticActive) VerificationGreen.copy(alpha = pulseAlpha)
                            else DarkTextMuted
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Thinking AI-Core Debugger",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Segment-by-segment crash prevention & sustained app state",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Auto-Shield Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Auto-Shield",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(end = 6.dp)
                )
                Switch(
                    checked = debuggerState.autoCrashMitigationEnabled,
                    onCheckedChange = { viewModel.thinkingAiDebuggerManager.toggleAutoCrashShield() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GeminiPurple
                    ),
                    modifier = Modifier.testTag("auto_crash_shield_switch")
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // System Health & Crash Mitigation Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("system_health_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "System Health Score",
                                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "${debuggerState.overallSystemHealthScore}%",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (debuggerState.overallSystemHealthScore > 80) VerificationGreen
                                        else if (debuggerState.overallSystemHealthScore > 60) LuminousYellow
                                        else VibrantRed
                                    )
                                )
                            }

                            // Crashes Prevented Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GeminiPurple.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Crashes Prevented",
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "${debuggerState.totalCrashesPrevented}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Crashes Mitigated",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear Health Meter
                        LinearProgressIndicator(
                            progress = { debuggerState.overallSystemHealthScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (debuggerState.overallSystemHealthScore > 80) VerificationGreen else LuminousYellow,
                            trackColor = DarkSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.thinkingAiDebuggerManager.simulateCriticalLlmLoadAndRisk() },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_crash_risk_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simulate Risk", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.thinkingAiDebuggerManager.remediateAllRisks() },
                                colors = ButtonDefaults.buttonColors(containerColor = VerificationGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auto_remediate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Remediate", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { viewModel.thinkingAiDebuggerManager.runDeepPipelineScan() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("deep_pipeline_scan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deep Scan", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Real-Time Thinking AI Reasoning Stream
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("thinking_reasoning_feed_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isThinkingFeedExpanded = !isThinkingFeedExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Thinking AI Core",
                                    tint = GeminiBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live Thinking AI Reasoning Feed",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            Icon(
                                imageVector = if (isThinkingFeedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle Thinking Feed",
                                tint = TextSecondary
                            )
                        }

                        if (isThinkingFeedExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))

                            if (debuggerState.activeThinkingSteps.isEmpty()) {
                                Text(
                                    text = "No diagnostic thinking steps recorded yet.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    debuggerState.activeThinkingSteps.takeLast(5).reversed().forEach { step ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = DarkSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "Step #${step.stepNumber} [${step.segment.displayName}]",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = GeminiBlue,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                    Text(
                                                        text = "Active",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = VerificationGreen,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = step.reasoningText,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Color.White,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                                step.actionTaken?.let { action ->
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "➜ Action: $action",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = GeminiPurple,
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 10.sp
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
                }
            }

            // LLM Pipeline Segment Expansion Section Header
            item {
                Text(
                    text = "LLM Application Pipeline Segments",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Expandable Segment Cards
            items(LlmPipelineSegment.values()) { segment ->
                val telemetry = debuggerState.segmentTelemetryList.find { it.segment == segment }
                val isExpanded = expandedSegmentId == segment.id

                val statusColor = when (telemetry?.healthStatus) {
                    SegmentHealthStatus.OPTIMAL -> VerificationGreen
                    SegmentHealthStatus.WARNING -> LuminousYellow
                    SegmentHealthStatus.CRITICAL_RISK -> VibrantRed
                    SegmentHealthStatus.CRASH_PREVENTED -> GeminiPurple
                    null -> DarkTextMuted
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("segment_card_${segment.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isExpanded) statusColor.copy(alpha = 0.7f) else DarkSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Segment Header Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedSegmentId = if (isExpanded) null else segment.id
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = statusColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (segment) {
                                                LlmPipelineSegment.CONTEXT_WINDOW -> Icons.Default.Memory
                                                LlmPipelineSegment.PROMPT_TEMPLATES -> Icons.Default.Code
                                                LlmPipelineSegment.REASONING_GRAPH -> Icons.Default.Psychology
                                                LlmPipelineSegment.GUARDRAILS_SAFETY -> Icons.Default.Shield
                                                LlmPipelineSegment.SYSTEM_RESOURCES -> Icons.Default.Speed
                                            },
                                            contentDescription = segment.displayName,
                                            tint = statusColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = segment.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = telemetry?.metricValue ?: segment.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = telemetry?.healthStatus?.label ?: "Unknown",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand ${segment.displayName}",
                                    tint = TextSecondary
                                )
                            }
                        }

                        // Expanded Segment Content
                        if (isExpanded && telemetry != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = DarkSurfaceVariant, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Details Map
                            Text(
                                text = "Telemetry & System Parameters:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = GeminiBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            telemetry.details.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Active Crash Risks
                            if (telemetry.detectedRisks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Detected Crash Vectors & Mitigation:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = VibrantRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                telemetry.detectedRisks.forEach { risk ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = VibrantRed.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantRed.copy(alpha = 0.4f)),
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
                                                    text = "⚠️ ${risk.title}",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                                Text(
                                                    text = if (risk.isMitigated) "MITIGATED" else risk.severity,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (risk.isMitigated) VerificationGreen else VibrantRed,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Cause: ${risk.rootCause}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Remediation: ${risk.remediationStrategy}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = GeminiPurple,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Diagnostic Trace Log
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Segment Diagnostic Trace:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = GeminiPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            telemetry.diagnosticTrace.forEach { trace ->
                                Text(
                                    text = trace,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkTextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
