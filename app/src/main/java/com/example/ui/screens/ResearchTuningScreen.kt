package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.GlassPanel
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.Memory

@Composable
fun ResearchTuningScreen(
    viewModel: MainViewModel,
    uiState: UiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Research & Tuning Lab",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Optimization of Large Language Models through deep data analysis",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SectionHeader("Optimization Vectors")
        
        var showAdvancedDropdown by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { showAdvancedDropdown = true },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Optimization Vector...", color = Color.White)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                }
            }
            
            DropdownMenu(
                expanded = showAdvancedDropdown,
                onDismissRequest = { showAdvancedDropdown = false },
                modifier = Modifier.background(DarkSurfaceVariant).border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("RLHF Fine-Tuning", color = Color.White) },
                    onClick = { showAdvancedDropdown = false },
                    leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = LuminousBlue) }
                )
                DropdownMenuItem(
                    text = { Text("Dataset Augmentation", color = Color.White) },
                    onClick = { showAdvancedDropdown = false },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VibrantPurple) }
                )
                DropdownMenuItem(
                    text = { Text("NPU Kernel Optimization", color = Color.White) },
                    onClick = { showAdvancedDropdown = false },
                    leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null, tint = VibrantTeal) }
                )
                DropdownMenuItem(
                    text = { Text("Latent Space Mapping", color = Color.White) },
                    onClick = { showAdvancedDropdown = false },
                    leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = VibrantGreen) }
                )
            }
        }

        TuningCard(
            title = "RLHF Fine-Tuning",
            description = "Adjust model weights based on reinforcement learning from human feedback.",
            icon = Icons.Default.Tune,
            accentColor = LuminousBlue,
            onAction = { /* Tune */ }
        )

        TuningCard(
            title = "Dataset Augmentation",
            description = "Synthetically expand training data using generative reasoning paths.",
            icon = Icons.Default.AutoAwesome,
            accentColor = VibrantPurple,
            onAction = { /* Augment */ }
        )

        TuningCard(
            title = "NPU Kernel Optimization",
            description = "Tune low-level matrix multiplication kernels for Exynos 2600 architecture.",
            icon = Icons.Default.Memory,
            accentColor = VibrantTeal,
            onAction = { /* Optimize */ }
        )

        TuningCard(
            title = "Latent Space Mapping",
            description = "Visualize and map the high-dimensional latent space of reasoning nodes.",
            icon = Icons.Default.Psychology,
            accentColor = VibrantGreen,
            onAction = { /* Map */ }
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Active Research Keys")
        
        GlassPanel {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Core Integration Keys",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        "TOTAL: 8",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                ResearchKeyItem("PROMPT_ENGINEERING_V4", "ACTIVE", VibrantGreen)
                ResearchKeyItem("REASONING_TRACE_LOGS", "CAPTURING", LuminousBlue)
                ResearchKeyItem("SYNTHETIC_EVAL_SET", "PENDING", LuminousYellow)
                ResearchKeyItem("OCTOPUS_TOOL_INDEX", "INDEXED", VibrantTeal)
                ResearchKeyItem("D3_LATENCY_METRICS", "STREAMING", VibrantPurple)
                ResearchKeyItem("NEURAL_CACHE_V2", "READY", VibrantGreen)
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Rotate Keys */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AbsoluteBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Rotate Research Keys", color = LuminousBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionHeader("Advanced Benchmarks")
        
        LlmOptimizationResearchScreen(viewModel, uiState, Modifier.fillMaxWidth())
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = LuminousBlue),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ResearchKeyItem(key: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace), color = TextPrimary)
        Surface(
            color = statusColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
        ) {
            Text(
                text = status,
                color = statusColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun TuningCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onAction: () -> Unit
) {
    GlassPanel(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clickable { onAction() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
