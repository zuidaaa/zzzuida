package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LlmDatasetEntity
import com.example.data.ProofOfThoughtEntity
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.GlassPanel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchTuningScreen(
    viewModel: MainViewModel,
    uiState: UiState
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Datasets (SFT/DPO)", "Proof-of-Thought Cache", "File & Network Manager", "Optimization Vectors")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .padding(16.dp)
    ) {
        // Screen Header
        Text(
            text = "Research & Tuning Lab",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Room SQLite Proof Cache • Android File & Network Manager • LLM Training Datasets",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = LuminousBlue,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LuminousBlue
                    )
                }
            },
            divider = { HorizontalDivider(color = BorderSubtle) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) LuminousBlue else TextSecondary,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> DatasetsManagementSection(viewModel = viewModel)
            1 -> ProofOfThoughtSection(viewModel = viewModel)
            2 -> FileNetworkManagerSection(viewModel = viewModel)
            3 -> OptimizationVectorsSection(viewModel = viewModel, uiState = uiState)
        }
    }
}

// ==========================================
// TAB 0: Training & Fine-Tuning Datasets
// ==========================================

@Composable
private fun DatasetsManagementSection(viewModel: MainViewModel) {
    val datasets by viewModel.datasetsList.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var viewingDataset by remember { mutableStateOf<LlmDatasetEntity?>(null) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).testTag("dataset_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Dataset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Box {
                OutlinedButton(
                    onClick = { showPresetMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("dataset_load_preset_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Load Presets", color = LuminousBlue, fontSize = 12.sp)
                }

                DropdownMenu(
                    expanded = showPresetMenu,
                    onDismissRequest = { showPresetMenu = false },
                    modifier = Modifier.background(DarkSurfaceVariant).border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("GSM8K Math Proofs (SFT)", color = Color.White) },
                        onClick = {
                            viewModel.loadPresetFineTuningDataset("gsm8k")
                            showPresetMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Functions, contentDescription = null, tint = LuminousBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Hoare Logic Invariants (SFT)", color = Color.White) },
                        onClick = {
                            viewModel.loadPresetFineTuningDataset("hoare")
                            showPresetMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = VibrantGreen) }
                    )
                    DropdownMenuItem(
                        text = { Text("RLHF DPO Alignment (DPO)", color = Color.White) },
                        onClick = {
                            viewModel.loadPresetFineTuningDataset("dpo")
                            showPresetMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = VibrantPurple) }
                    )
                    DropdownMenuItem(
                        text = { Text("Exynos NPU Quantization (INT4)", color = Color.White) },
                        onClick = {
                            viewModel.loadPresetFineTuningDataset("quant")
                            showPresetMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Memory, contentDescription = null, tint = VibrantTeal) }
                    )
                }
            }

            if (datasets.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllDatasets() },
                    modifier = Modifier.testTag("dataset_clear_all_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Datasets", tint = TextSecondary)
                }
            }
        }

        // Summary Bar
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Active Datasets", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${datasets.size} Registered", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Samples", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    val totalEntries = datasets.sumOf { it.entryCount }
                    Text("$totalEntries rows", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantTeal)
                }
            }
        }

        if (exportStatusMessage != null) {
            Surface(
                color = VibrantGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGreen.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(exportStatusMessage ?: "", color = VibrantGreen, fontSize = 12.sp)
                    IconButton(onClick = { exportStatusMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantGreen, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Datasets List
        if (datasets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No fine-tuning datasets stored in Room DB", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Click 'Load Presets' or 'Add Dataset' to import training files.", color = TextDisabled, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(datasets, key = { it.id }) { dataset ->
                    DatasetCard(
                        dataset = dataset,
                        onViewContent = { viewingDataset = dataset },
                        onExport = {
                            val file = viewModel.exportDatasetToFile(dataset)
                            exportStatusMessage = "Exported to: ${file.name} (${file.length()} bytes)"
                        },
                        onDelete = { viewModel.deleteTrainingDataset(dataset.id) }
                    )
                }
            }
        }
    }

    // Add Dataset Dialog
    if (showAddDialog) {
        AddDatasetDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, desc, format, content, purpose, model ->
                viewModel.uploadDataset(name, desc, format, content, purpose, model)
                showAddDialog = false
            }
        )
    }

    // View Dataset Content Dialog
    if (viewingDataset != null) {
        AlertDialog(
            onDismissRequest = { viewingDataset = null },
            confirmButton = {
                TextButton(onClick = { viewingDataset = null }) {
                    Text("Close", color = LuminousBlue)
                }
            },
            title = {
                Text(viewingDataset?.name ?: "Dataset Content", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Format: ${viewingDataset?.fileFormat} | Entries: ${viewingDataset?.entryCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        color = AbsoluteBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)
                    ) {
                        Text(
                            text = viewingDataset?.rawContent ?: "",
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            containerColor = DarkSurfaceVariant
        )
    }
}

@Composable
private fun DatasetCard(
    dataset: LlmDatasetEntity,
    onViewContent: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dataset.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = LuminousBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = dataset.fileFormat,
                        color = LuminousBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = VibrantPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = dataset.purpose,
                        color = VibrantPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Model: ${dataset.modelTarget}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "• ${dataset.entryCount} entries",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantTeal
                )
            }

            if (dataset.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = dataset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewContent,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", color = TextSecondary, fontSize = 12.sp)
                }

                TextButton(
                    onClick = onExport,
                    modifier = Modifier.height(36.dp).testTag("dataset_export_${dataset.id}")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = LuminousBlue, fontSize = 12.sp)
                }

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.height(36.dp).testTag("dataset_delete_${dataset.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = VibrantRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove", color = VibrantRed, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AddDatasetDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, desc: String, format: String, content: String, purpose: String, model: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("JSONL") }
    var purpose by remember { mutableStateOf("Supervised Fine-Tuning (SFT)") }
    var modelTarget by remember { mutableStateOf("deepseek-r1-7b") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && content.isNotBlank()) {
                        onAdd(name, desc, format, content, purpose, modelTarget)
                    }
                },
                enabled = name.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple)
            ) {
                Text("Save to Room DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        title = { Text("Add Dataset for LLM Fine-Tuning", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dataset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = format,
                        onValueChange = { format = it },
                        label = { Text("Format (JSONL/CSV)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = modelTarget,
                        onValueChange = { modelTarget = it },
                        label = { Text("Target Model") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Training Purpose (SFT / DPO)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Raw Training Data Content") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    placeholder = { Text("Paste JSONL or CSV formatted dataset...") }
                )
            }
        },
        containerColor = DarkSurfaceVariant
    )
}

// ==========================================
// TAB 1: Proof-of-Thought (PoT) Offline Cache
// ==========================================

@Composable
private fun ProofOfThoughtSection(viewModel: MainViewModel) {
    val proofs by viewModel.proofOfThoughts.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalProofCount.collectAsStateWithLifecycle()
    val totalTokens by viewModel.totalProofTokens.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf("All") }
    var showAddProofDialog by remember { mutableStateOf(false) }
    var exportStatusMessage by remember { mutableStateOf<String?>(null) }

    val filteredProofs = remember(proofs, searchQuery, selectedDomain) {
        proofs.filter { proof ->
            val matchDomain = selectedDomain == "All" || proof.domain.equals(selectedDomain, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                proof.title.contains(searchQuery, ignoreCase = true) ||
                proof.premiseOrHypothesis.contains(searchQuery, ignoreCase = true) ||
                proof.formalProofBody.contains(searchQuery, ignoreCase = true)
            matchDomain && matchSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Offline Persistence Banner
        Surface(
            color = VibrantGreen.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGreen.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("100% OFFLINE PERSISTENCE LAYER ACTIVE", fontWeight = FontWeight.Bold, color = VibrantGreen, fontSize = 12.sp)
                    Text("Proof-of-Thought mathematical proofs cached in Room SQLite for instant, zero-network retrieval.", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // Search & Controls Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search theorems, invariants, formulas...", fontSize = 12.sp, color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuminousBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AbsoluteBlack,
                    unfocusedContainerColor = AbsoluteBlack
                )
            )

            Button(
                onClick = { showAddProofDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(50.dp).testTag("proof_cache_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cache Proof", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Domain filter pills
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Mathematics", "Computer Science", "Distributed Systems").forEach { domain ->
                FilterChip(
                    selected = selectedDomain == domain,
                    onClick = { selectedDomain = domain },
                    label = { Text(domain, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LuminousBlue.copy(alpha = 0.2f),
                        selectedLabelColor = LuminousBlue
                    )
                )
            }
        }

        if (exportStatusMessage != null) {
            Surface(
                color = LuminousBlue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(exportStatusMessage ?: "", color = LuminousBlue, fontSize = 12.sp)
                    IconButton(onClick = { exportStatusMessage = null }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LuminousBlue, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Proofs List
        if (filteredProofs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No proofs matching current search.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProofs, key = { it.id }) { proof ->
                    ProofOfThoughtCard(
                        proof = proof,
                        onExport = {
                            val file = viewModel.exportProofToFile(proof)
                            exportStatusMessage = "Proof exported to: ${file.name}"
                        },
                        onDelete = { viewModel.deleteProofOfThought(proof.id) }
                    )
                }
            }
        }
    }

    if (showAddProofDialog) {
        AddProofDialog(
            onDismiss = { showAddProofDialog = false },
            onAdd = { title, premise, domain, technique, body, qed ->
                viewModel.cacheProofOfThought(
                    title = title,
                    premise = premise,
                    domain = domain,
                    technique = technique,
                    proofBody = body,
                    qedConclusion = qed
                )
                showAddProofDialog = false
            }
        )
    }
}

@Composable
private fun ProofOfThoughtCard(
    proof: ProofOfThoughtEntity,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = proof.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = VibrantGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = proof.verificationStatus,
                        color = VibrantGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LuminousBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = proof.domain,
                        color = LuminousBlue,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = VibrantPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = proof.proofTechnique,
                        color = VibrantPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "• ${(proof.confidenceScore * 100).toInt()}% Confidence",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantTeal
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Premise: ${proof.premiseOrHypothesis}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = TextPrimary
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        color = AbsoluteBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = proof.formalProofBody,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (proof.qedConclusion.isNotBlank()) {
                        Text(
                            text = proof.qedConclusion,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = VibrantGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Proof Details" else "View Full Proof", color = LuminousBlue, fontSize = 12.sp)
                }

                Row {
                    TextButton(onClick = onExport, modifier = Modifier.testTag("proof_export_${proof.id}")) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", color = VibrantTeal, fontSize = 12.sp)
                    }
                    TextButton(onClick = onDelete, modifier = Modifier.testTag("proof_delete_${proof.id}")) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = VibrantRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = VibrantRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProofDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, premise: String, domain: String, technique: String, body: String, qed: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var premise by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("Mathematics") }
    var technique by remember { mutableStateOf("Contradiction") }
    var body by remember { mutableStateOf("") }
    var qed by remember { mutableStateOf("Q.E.D.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onAdd(title, premise, domain, technique, body, qed)
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue)
            ) {
                Text("Cache to Room DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        title = { Text("Cache Proof-of-Thought Response", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Theorem / Problem Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = premise,
                    onValueChange = { premise = it },
                    label = { Text("Premise / Hypothesis") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text("Domain") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = technique,
                        onValueChange = { technique = it },
                        label = { Text("Technique") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Formal Proof Steps (Markdown/LaTeX)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = qed,
                    onValueChange = { qed = it },
                    label = { Text("Q.E.D. Conclusion") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = DarkSurfaceVariant
    )
}

// ==========================================
// TAB 2: Android File & Network Manager
// ==========================================

@Composable
private fun FileNetworkManagerSection(viewModel: MainViewModel) {
    val netStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Network Status Card
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (netStatus.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (netStatus.isConnected) VibrantGreen else VibrantRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Network Management", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("State: ${netStatus.effectiveState}", color = if (netStatus.isConnected) VibrantGreen else VibrantRed, fontSize = 12.sp)
                        }
                    }

                    Switch(
                        checked = netStatus.isForceOffline,
                        onCheckedChange = { viewModel.toggleForceOfflineMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VibrantRed,
                            checkedTrackColor = VibrantRed.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("toggle_force_offline")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Connection Type:", color = TextSecondary, fontSize = 12.sp)
                    Text(netStatus.connectionType, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Link Downstream:", color = TextSecondary, fontSize = 12.sp)
                    Text("${netStatus.linkSpeedMbps} Mbps", color = VibrantTeal, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Metered Connection:", color = TextSecondary, fontSize = 12.sp)
                    Text(if (netStatus.isMetered) "YES (Data Saver)" else "NO (Unmetered)", color = TextPrimary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.refreshNetworkAndStorage() },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AbsoluteBlack),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuminousBlue.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-evaluate Network & Storage", color = LuminousBlue, fontSize = 12.sp)
                }
            }
        }

        // File Management Card
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = LuminousYellow, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Android File Management", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("App-Scoped Persistence & Export Paths", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Proof Cache Disk Size:", color = TextSecondary, fontSize = 12.sp)
                    Text("${storageStats.proofCacheSizeBytes / 1024} KB", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Datasets Disk Size:", color = TextSecondary, fontSize = 12.sp)
                    Text("${storageStats.datasetsSizeBytes / 1024} KB", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Room SQLite DB Size:", color = TextSecondary, fontSize = 12.sp)
                    Text("${storageStats.databaseSizeBytes / 1024} KB", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Available Free Space:", color = TextSecondary, fontSize = 12.sp)
                    Text("${storageStats.availableSpaceMb} MB", color = VibrantGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Internal Path: ${storageStats.filesDirPath}", color = TextDisabled, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Android Manifest Permissions Status Card
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Granted Android Permissions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                PermissionRow("android.permission.INTERNET", true)
                PermissionRow("android.permission.ACCESS_NETWORK_STATE", true)
                PermissionRow("android.permission.ACCESS_WIFI_STATE", true)
                PermissionRow("android.permission.CHANGE_NETWORK_STATE", true)
                PermissionRow("androidx.core.content.FileProvider", true)
            }
        }
    }
}

@Composable
private fun PermissionRow(permission: String, granted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(permission, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
        Surface(
            color = if (granted) VibrantGreen.copy(alpha = 0.15f) else VibrantRed.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = if (granted) "GRANTED" else "DENIED",
                color = if (granted) VibrantGreen else VibrantRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

// ==========================================
// TAB 3: Optimization Vectors & Benchmarks
// ==========================================

@Composable
private fun OptimizationVectorsSection(
    viewModel: MainViewModel,
    uiState: UiState
) {
    var showAdvancedDropdown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
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
            onAction = { }
        )

        TuningCard(
            title = "Dataset Augmentation",
            description = "Synthetically expand training data using generative reasoning paths.",
            icon = Icons.Default.AutoAwesome,
            accentColor = VibrantPurple,
            onAction = { }
        )

        TuningCard(
            title = "NPU Kernel Optimization",
            description = "Tune low-level matrix multiplication kernels for Exynos 2600 architecture.",
            icon = Icons.Default.Memory,
            accentColor = VibrantTeal,
            onAction = { }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Advanced Benchmarks", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = LuminousBlue), modifier = Modifier.padding(vertical = 4.dp))
        LlmOptimizationResearchScreen(viewModel, uiState, Modifier.fillMaxWidth())
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
            .padding(vertical = 4.dp)
            .clickable { onAction() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
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
