package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalModelEntity
import com.example.engine.ModelUpgradeItem
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueGlow
import com.example.ui.theme.GeminiBlueLight
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.WarningOrange

@Composable
fun ModelManagerScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    localModels: List<LocalModelEntity>,
    modifier: Modifier = Modifier
) {
    val totalInstalledMb = localModels.filter { it.isDownloaded }.sumOf { it.downloadSizeMb }

    var compareModelAId by remember { mutableStateOf<String?>(null) }
    var compareModelBId by remember { mutableStateOf<String?>(null) }
    var isComparing by remember { mutableStateOf(false) }

    val availableCompareModels = localModels
    if (compareModelAId == null && availableCompareModels.isNotEmpty()) {
        compareModelAId = availableCompareModels.firstOrNull()?.id
    }
    if (compareModelBId == null && availableCompareModels.size > 1) {
        compareModelBId = availableCompareModels.getOrNull(1)?.id
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header
            Column {
                Text(
                    text = "Local LLM Model Manager",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                )
                Text(
                    text = "On-device quantizations optimized for Galaxy S26 Ultra NPU Turbo",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted)
                )
            }
        }

        // Storage & NPU Accelerator Overview Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Local Model Storage Footprint",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                            )
                        }
                        Text(
                            text = "${"%.1f".format(totalInstalledMb / 1024f)} GB / 512 GB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiBlueLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (totalInstalledMb / 1024f) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GeminiBlue,
                        trackColor = DarkSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = GeminiEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NPU Turbo: 85 TOPS Active",
                                style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontSize = 11.sp)
                            )
                        }
                        Text(
                            text = "Zero-Token Local Execution",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Hugging Face CLI & Hub Downloader Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hf_downloader_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeminiPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = GeminiPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Hugging Face CLI & Hub Downloader",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary
                                    )
                                )
                                Text(
                                    text = "GGUF Quantization • Fast Shard Downloader",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GeminiCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "HF CLI READY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Command / Repo Input
                    OutlinedTextField(
                        value = uiState.hfDownloadInput,
                        onValueChange = { viewModel.setHfDownloadInput(it) },
                        label = { Text("Model Repo / CLI Command", fontSize = 12.sp) },
                        placeholder = { Text("hf download JonathanColetti/Qwen3.8-27B-Uncensored-GGUF") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = GeminiPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = DarkTextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeminiPurple,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hf_download_input")
                    )

                    // Quick Model Selection Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "JonathanColetti/Qwen3.8-27B-Uncensored-GGUF" to "Qwen 3.8 27B",
                            "deepseek-ai/DeepSeek-R1-Distill-Qwen-7B" to "DeepSeek R1 7B",
                            "meta-llama/Llama-3.3-70B-Instruct" to "Llama 3.3 70B"
                        )
                        presets.forEach { (repo, label) ->
                            FilterChip(
                                selected = uiState.hfDownloadInput.contains(repo),
                                onClick = {
                                    viewModel.setHfDownloadInput("hf download $repo")
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeminiPurple.copy(alpha = 0.25f),
                                    selectedLabelColor = GeminiPurple,
                                    containerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }

                    // Quantization Selection Row
                    Text(
                        text = "SELECT GGUF QUANTIZATION PROFILE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextSubtle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quants = listOf(
                            "Q4_K_M" to "Q4_K_M (16.4 GB - Balanced)",
                            "Q5_K_M" to "Q5_K_M (19.8 GB - High)",
                            "Q8_0" to "Q8_0 (28.9 GB - Max Precision)",
                            "Q3_K_M" to "Q3_K_M (13.2 GB - Fast)"
                        )
                        quants.forEach { (q, label) ->
                            FilterChip(
                                selected = uiState.hfDownloadSelectedQuant == q,
                                onClick = { viewModel.setHfDownloadQuant(q) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeminiBlue.copy(alpha = 0.25f),
                                    selectedLabelColor = GeminiBlueLight,
                                    containerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }

                    // Download Action Button
                    val isDownloadingThis = uiState.downloadingModelId != null
                    Button(
                        onClick = { viewModel.downloadHfModel() },
                        enabled = !isDownloadingThis && uiState.hfDownloadInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("start_hf_download_button")
                    ) {
                        if (isDownloadingThis) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloading from Hugging Face CDN...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download & Compile for NPU",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Live Download Progress Telemetry
                    if (isDownloadingThis) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, GeminiPurple.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = uiState.downloadCurrentShard.ifBlank { "Shard Download in progress..." },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        color = GeminiCyan
                                    )
                                )
                                Text(
                                    text = "${(uiState.downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GeminiPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            LinearProgressIndicator(
                                progress = { uiState.downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GeminiPurple,
                                trackColor = DarkSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Speed: ${"%.1f".format(uiState.downloadSpeedMbps)} MB/s",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontSize = 10.5.sp)
                                )
                                Text(
                                    text = if (uiState.downloadEtaSeconds > 0) "ETA: ~${uiState.downloadEtaSeconds}s" else "Finalizing",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 10.5.sp)
                                )
                            }

                            if (uiState.downloadStatusMessage.isNotBlank()) {
                                Text(
                                    text = uiState.downloadStatusMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(color = GeminiBlueLight, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Upgrades Banner (if any updates available)
        val upgradeCount = uiState.modelUpgradeItems.count { it.hasUpdate }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("model_updates_banner"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (upgradeCount > 0) GeminiBlue else DarkOutlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (upgradeCount > 0) "$upgradeCount Model Upgrades Available" else "All Models Up to Date",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (upgradeCount > 0) GeminiBlueLight else DarkTextPrimary
                                )
                            )
                            Text(
                                text = "Dynamic model complexity router & upstream channels",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.checkForModelUpdates() },
                        enabled = !uiState.isCheckingForModelUpdates,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("manager_check_updates_btn")
                    ) {
                        Text(
                            text = if (uiState.isCheckingForModelUpdates) "Checking..." else "Check Updates",
                            color = GeminiBlueLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Collapsible Model Comparison Matrix Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("model_comparison_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = if (isComparing) 0.6f else 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚖️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NPU Model Comparison & Profiler",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Button(
                            onClick = { isComparing = !isComparing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isComparing) GeminiBlue else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("toggle_comparison_btn")
                        ) {
                            Text(
                                text = if (isComparing) "Hide Matrix" else "Compare Versions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isComparing) Color.White else GeminiBlueLight
                            )
                        }
                    }

                    if (isComparing) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Selector A
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MODEL / VERSION A",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable {
                                            // Cycle model A
                                            val currentIdx = availableCompareModels.indexOfFirst { it.id == compareModelAId }
                                            val nextIdx = (currentIdx + 1) % availableCompareModels.size
                                            compareModelAId = availableCompareModels.getOrNull(nextIdx)?.id
                                        }
                                        .padding(8.dp)
                                        .testTag("compare_select_a")
                                ) {
                                    Text(
                                        text = availableCompareModels.find { it.id == compareModelAId }?.name ?: "Select Model",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Selector B
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MODEL / VERSION B",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable {
                                            // Cycle model B
                                            val currentIdx = availableCompareModels.indexOfFirst { it.id == compareModelBId }
                                            val nextIdx = (currentIdx + 1) % availableCompareModels.size
                                            compareModelBId = availableCompareModels.getOrNull(nextIdx)?.id
                                        }
                                        .padding(8.dp)
                                        .testTag("compare_select_b")
                                ) {
                                    Text(
                                        text = availableCompareModels.find { it.id == compareModelBId }?.name ?: "Select Model",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val modelA = availableCompareModels.find { it.id == compareModelAId }
                        val modelB = availableCompareModels.find { it.id == compareModelBId }

                        if (modelA != null && modelB != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Specs Compare Rows
                                CompareSpecRow(label = "Version tag", valA = modelA.version, valB = modelB.version)
                                CompareSpecRow(label = "Release Date", valA = modelA.releaseDate, valB = modelB.releaseDate)
                                CompareSpecRow(label = "Parameters", valA = modelA.parameterSize, valB = modelB.parameterSize)
                                CompareSpecRow(label = "Context limit", valA = modelA.contextWindow, valB = modelB.contextWindow)
                                CompareSpecRow(label = "Quantization", valA = modelA.quantization, valB = modelB.quantization)
                                CompareSpecRow(label = "Compute", valA = modelA.computeBackend, valB = modelB.computeBackend)
                                
                                // Simulated speed benchmarks
                                val speedA = when {
                                    modelA.id.contains("think") -> "48.5 tok/s"
                                    modelA.id.contains("pro") -> "14.2 tok/s"
                                    modelA.id.contains("distill") -> "32.0 tok/s"
                                    modelA.id.contains("gemma") -> "22.5 tok/s"
                                    modelA.id.contains("med") -> "28.0 tok/s"
                                    else -> "8.5 tok/s"
                                }
                                val speedB = when {
                                    modelB.id.contains("think") -> "48.5 tok/s"
                                    modelB.id.contains("pro") -> "14.2 tok/s"
                                    modelB.id.contains("distill") -> "32.0 tok/s"
                                    modelB.id.contains("gemma") -> "22.5 tok/s"
                                    modelB.id.contains("med") -> "28.0 tok/s"
                                    else -> "8.5 tok/s"
                                }
                                CompareSpecRow(label = "Simulated Speed", valA = speedA, valB = speedB, highlight = true)

                                // Simulated RAM footprint
                                val ramA = when {
                                    modelA.id.contains("uncensored") -> "18.2 GB"
                                    modelA.id.contains("pro") -> "11.4 GB"
                                    modelA.id.contains("gemma") -> "7.8 GB"
                                    else -> "4.9 GB"
                                }
                                val ramB = when {
                                    modelB.id.contains("uncensored") -> "18.2 GB"
                                    modelB.id.contains("pro") -> "11.4 GB"
                                    modelB.id.contains("gemma") -> "7.8 GB"
                                    else -> "4.9 GB"
                                }
                                CompareSpecRow(label = "NPU RAM Alloc", valA = ramA, valB = ramB)

                                Spacer(modifier = Modifier.height(4.dp))
                                // Quick action buttons to set active
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.setActiveModel(modelA) },
                                        enabled = modelA.isDownloaded,
                                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Set A Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.setActiveModel(modelB) },
                                        enabled = modelB.isDownloaded,
                                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Set B Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "AVAILABLE DEEP REASONING MODELS (${localModels.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DarkTextSubtle,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
        }

        // Models List
        items(localModels, key = { it.id }) { model ->
            val upgradeItem = uiState.modelUpgradeItems.find { it.modelId == model.id }
            LocalModelCard(
                model = model,
                isActive = model.id == uiState.activeModelId,
                isDownloading = model.id == uiState.downloadingModelId,
                downloadProgress = uiState.downloadProgress,
                upgradeItem = upgradeItem,
                onSelectActive = { viewModel.setActiveModel(model) },
                onDownload = { viewModel.downloadModel(model) },
                onDelete = { viewModel.deleteDownloadedModel(model) },
                onUpgrade = { upgradeItem?.let { viewModel.upgradeModel(it) } }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LocalModelCard(
    model: LocalModelEntity,
    isActive: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    upgradeItem: ModelUpgradeItem? = null,
    onSelectActive: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onUpgrade: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_card_${model.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkSurfaceElevated else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isActive) GeminiBlue else DarkOutlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Name + Active Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) GeminiBlueLight else DarkTextPrimary
                        )
                    )
                    Text(
                        text = "${model.family} • ${model.parameterSize} params",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.5.sp)
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GeminiBlue.copy(alpha = 0.2f))
                            .border(1.dp, GeminiBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiBlueLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Version info & Release Date info card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GeminiBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.version,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Released: ${model.releaseDate}",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "TRAINING DATA CORPUS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GeminiCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.trainingData,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextSubtle,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Spec Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quantization
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = model.quantization,
                        style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontSize = 10.sp)
                    )
                }

                // Compute backend
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = model.computeBackend,
                        style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontSize = 10.sp)
                    )
                }

                // Context Window
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = model.contextWindow,
                        style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontSize = 10.sp)
                    )
                }
            }

            // Upgrade Banner if available
            if (upgradeItem != null && upgradeItem.hasUpdate) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GeminiBlue.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🚀 Newer Version Available: v${upgradeItem.latestVersion}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GeminiBlueLight,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = upgradeItem.changelog,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextSubtle,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                        Button(
                            onClick = onUpgrade,
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Upgrade", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Downloading Progress
            if (isDownloading) {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Optimizing weights for S26 Ultra NPU...",
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontSize = 11.sp)
                        )
                        Text(
                            text = "${(downloadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GeminiBlue,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${model.downloadSizeMb} MB",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = DarkTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (model.isDownloaded) {
                        if (!isActive) {
                            Button(
                                onClick = onSelectActive,
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("activate_model_${model.id}")
                            ) {
                                Text("Set Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("delete_model_${model.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete model weights",
                                tint = DarkTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("download_model_${model.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = GeminiBlueLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download & Install", color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareSpecRow(
    label: String,
    valA: String,
    valB: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlight) GeminiBlue.copy(alpha = 0.08f) else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp),
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = valA,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (highlight) GeminiBlueLight else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = valB,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (highlight) GeminiPurple else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}
