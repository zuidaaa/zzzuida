package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.engine.skills.SkillCategory
import com.example.engine.skills.SkillExecutionResult
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.screens.skills.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SkillsHubScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val allSkills by viewModel.skillsManager.allSkills.collectAsState()
    val installedSkills by viewModel.skillsManager.installedSkills.collectAsState()
    val activeSkills by viewModel.skillsManager.activeSkills.collectAsState()
    val operationMessage by viewModel.skillsManager.operationMessage.collectAsState()
    val isSyncing by viewModel.skillsManager.isSyncing.collectAsState()
    val syncLogs by viewModel.skillsManager.syncLogs.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Entdecken, 1: Installiert, 2: CLI Import, 3: Playground
    var selectedCategory by rememberSaveable { mutableStateOf(SkillCategory.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var inspectingSkill by remember { mutableStateOf<AgentSkill?>(null) }
    var showAuditHistoryDialog by remember { mutableStateOf(false) }

    // Playground state
    var playgroundSelectedSkillId by remember { mutableStateOf<String?>(null) }
    var playgroundPrompt by remember { mutableStateOf("Analysiere die UI-Struktur und optimiere sie für Android 17 One UI 9.") }
    var playgroundResult by remember { mutableStateOf<SkillExecutionResult?>(null) }

    // CLI import state
    var cliImportInput by remember { mutableStateOf("npx skills add vercel/nextjs-app-router") }
    var customMarkdownInput by remember {
        mutableStateOf(
            """
            ---
            name: custom-optimization-skill
            description: Benutzerdefinierte Agenten-Richtlinie für adaptive Systemoptimierung
            author: Lokaler Entwickler
            allowed-tools: read_file edit_file run_command
            ---
            # Custom Optimization Skill
            1. Prüfe Speicherallokation vor rechenintensiven Aufgaben.
            2. Validiere alle Eingaben mit Zero-Trust Regeln.
            """.trimIndent()
        )
    }

    LaunchedEffect(operationMessage) {
        if (operationMessage != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.skillsManager.clearOperationMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Card
        Surface(
            color = DarkSurface,
            border = BorderStroke(0.8.dp, DarkOutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepThinkIndigo, DeepThinkViolet)
                                    )
                                )
                                .border(1.dp, DeepThinkCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "skills.sh Package Manager",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DeepThinkIndigo.copy(alpha = 0.3f))
                                        .border(0.6.dp, DeepThinkIndigoLight.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Open Standard",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepThinkCyan
                                    )
                                }
                            }
                            Text(
                                text = "Erweitere Agenten-Fähigkeiten durch SKILL.md Pakete",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }
                    }

                    // Active badge
                    Surface(
                        color = DeepThinkEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, DeepThinkEmerald.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DeepThinkEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activeSkills.size} Aktiv",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepThinkEmerald
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Modular One UI 9 Auto-Sync Banner
                SkillSyncBanner(
                    isSyncing = isSyncing,
                    syncLogs = syncLogs,
                    onTriggerSync = { viewModel.skillsManager.syncSkillsFromSkillsSh(isManual = true) },
                    onOpenAuditDialog = { showAuditHistoryDialog = true }
                )
            }
        }

        // Operation Feedback Message Toast
        AnimatedVisibility(
            visible = operationMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            operationMessage?.let { msg ->
                Surface(
                    color = DeepThinkIndigo.copy(alpha = 0.9f),
                    border = BorderStroke(0.6.dp, DeepThinkCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Search Bar & Filter Controls (for Browse tab)
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Suche nach Fähigkeiten (z.B. UI, SQL, Cloud)...", fontSize = 12.sp, color = DarkTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepThinkCyan, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skills_search_bar"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepThinkCyan,
                        unfocusedBorderColor = DarkOutlineVariant,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkillCategory.entries.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepThinkIndigo.copy(alpha = 0.3f),
                                selectedLabelColor = DeepThinkCyan,
                                containerColor = DarkSurface,
                                labelColor = DarkTextMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) DeepThinkCyan else DarkOutlineVariant
                            )
                        )
                    }
                }
            }
        }

        // Navigation Tabs (One UI 9 Pill Style)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = DeepThinkCyan,
            divider = { HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Entdecken (${allSkills.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Installiert (${installedSkills.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("CLI & Import", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Playground", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        // Content by Tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> {
                    val filteredSkills = remember(allSkills, selectedCategory, searchQuery) {
                        allSkills.filter { skill ->
                            val matchCategory = selectedCategory == SkillCategory.ALL || skill.category == selectedCategory
                            val matchSearch = searchQuery.isBlank() ||
                                    skill.name.contains(searchQuery, ignoreCase = true) ||
                                    skill.description.contains(searchQuery, ignoreCase = true) ||
                                    skill.ownerRepo.contains(searchQuery, ignoreCase = true)
                            matchCategory && matchSearch
                        }
                    }

                    SkillsBrowseTab(
                        filteredSkills = filteredSkills,
                        onToggleActive = { skill, active -> viewModel.skillsManager.toggleSkillActive(skill.id, active) },
                        onInstallToggle = { skill -> viewModel.skillsManager.installSkill(skill.id) },
                        onInspect = { skill -> inspectingSkill = skill },
                        onRunInPlayground = { skill ->
                            playgroundSelectedSkillId = skill.id
                            selectedTab = 3
                        }
                    )
                }
                1 -> {
                    SkillsInstalledTab(
                        installedSkills = installedSkills,
                        activeSkills = activeSkills,
                        onToggleActive = { skill, active -> viewModel.skillsManager.toggleSkillActive(skill.id, active) },
                        onInstallToggle = { skill -> viewModel.skillsManager.installSkill(skill.id) },
                        onInspect = { skill -> inspectingSkill = skill },
                        onRunInPlayground = { skill ->
                            playgroundSelectedSkillId = skill.id
                            selectedTab = 3
                        }
                    )
                }
                2 -> {
                    SkillsCliTab(
                        cliInput = cliImportInput,
                        onCliInputChange = { cliImportInput = it },
                        onExecuteCliCommand = { cmd -> viewModel.skillsManager.addSkillViaCli(cmd) },
                        customMarkdownInput = customMarkdownInput,
                        onCustomMarkdownChange = { customMarkdownInput = it },
                        onImportCustomMarkdown = { md -> viewModel.skillsManager.importCustomSkillMarkdown(md) }
                    )
                }
                3 -> {
                    SkillsPlaygroundTab(
                        activeSkills = activeSkills,
                        selectedSkillId = playgroundSelectedSkillId,
                        onSelectSkillId = { playgroundSelectedSkillId = it },
                        prompt = playgroundPrompt,
                        onPromptChange = { playgroundPrompt = it },
                        result = playgroundResult,
                        onExecuteSkill = { skillId, prompt ->
                            coroutineScope.launch {
                                playgroundResult = viewModel.skillsManager.executeSkill(skillId, prompt)
                            }
                        }
                    )
                }
            }
        }
    }

    // Inspect Skill Dialog
    inspectingSkill?.let { skill ->
        SkillInspectDialog(
            skill = skill,
            onDismiss = { inspectingSkill = null },
            onToggleActive = { active -> viewModel.skillsManager.toggleSkillActive(skill.id, active) },
            onInstallToggle = { viewModel.skillsManager.installSkill(skill.id) },
            onDeleteSkill = {
                viewModel.skillsManager.uninstallSkill(skill.id)
                inspectingSkill = null
            }
        )
    }

    // Zero-Trust Audit & Room Sync History Dialog
    if (showAuditHistoryDialog) {
        val auditSummary = remember(allSkills, syncLogs) {
            viewModel.skillsManager.auditAllSkillsSecurity()
        }

        SkillAuditDialog(
            auditSummary = auditSummary,
            syncLogs = syncLogs,
            isSyncing = isSyncing,
            onDismiss = { showAuditHistoryDialog = false },
            onTriggerSync = { viewModel.skillsManager.syncSkillsFromSkillsSh(isManual = true) }
        )
    }
}
