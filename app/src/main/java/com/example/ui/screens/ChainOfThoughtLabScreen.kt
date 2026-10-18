package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Refined Chain-of-Thought (CoT) Lab Screen.
 * Breaks down intermediate reasoning steps using a 3D tilted stacking card layout.
 * Designed for transparency, error analysis, and deep cognitive inspection.
 */
@Composable
fun ChainOfThoughtLabScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val steps = uiState.inspectingSteps ?: emptyList()
    var selectedStep by remember { mutableStateOf<com.example.engine.ReasoningStep?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Lab Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutlineVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = VibrantPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CHAIN-OF-THOUGHT LAB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Cognitive Trace Inspection & Error Analysis",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { /* INITIALIZE_AUTONOMOUS_ENVIRONMENT */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantTeal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantTeal.copy(alpha = 0.5f)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INIT_AUTONOMOUS_ENV", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FunctionalTag(
                        text = "ACTIVE_INSPECTOR",
                        variant = TagVariant.REASONING_PURPLE
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: 3D Stack Visualizer
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                if (steps.isNotEmpty()) {
                    ThoughtStackVisualizer(
                        steps = steps,
                        onStepClick = { selectedStep = it }
                    )
                } else {
                    EmptyLabState()
                }
            }

            // Right Side: Analysis Panel
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = DarkSurface.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DIAGNOSTIC_ANALYSIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VibrantTeal,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(
                        visible = selectedStep != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        selectedStep?.let { step ->
                            StepDiagnosticPanel(step = step)
                        }
                    }

                    if (selectedStep == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Select a node from the stack to begin error analysis.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled),
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepDiagnosticPanel(step: com.example.engine.ReasoningStep) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "CONFIDENCE_SCORE",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
            Text(
                text = "${(step.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = if (step.confidence > 0.8f) VibrantGreen else LuminousYellow
                )
            )
            LinearProgressIndicator(
                progress = { step.confidence },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (step.confidence > 0.8f) VibrantGreen else LuminousYellow,
                trackColor = DarkOutlineVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "VERIFICATION_LOGS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))

        step.validationRulesChecked.forEach { rule ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VibrantGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = rule,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                )
            }
        }
        
        if (step.validationRulesChecked.isEmpty()) {
            Text(
                text = "No validation rules recorded for this node.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "CROSS_CHECK_STATUS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary)
        )
        FunctionalTag(
            text = if (step.verified) "VALIDATED_CONSISTENT" else "INCONSISTENCY_DETECTED",
            variant = if (step.verified) TagVariant.STATUS_GREEN else TagVariant.MODE_YELLOW
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* Re-verify action */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Trigger Node Re-Verification")
        }
    }
}

@Composable
fun EmptyLabState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = TextDisabled,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NO ACTIVE COGNITIVE TRACE",
                style = MaterialTheme.typography.titleSmall.copy(color = TextDisabled)
            )
            Text(
                text = "Generate a deep thinking response to inspect reasoning.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextDisabled)
            )
        }
    }
}
