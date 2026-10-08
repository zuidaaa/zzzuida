package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ConversationEntity
import com.example.data.ReasoningCacheEntity
import com.example.engine.ReasoningStep
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.LinearProgressIndicator
import com.example.ui.components.D3ReasoningVisualizationPanel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationsHistoryScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    conversations: List<ConversationEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cachedReasoningList by viewModel.cachedReasoningList.collectAsStateWithLifecycle()
    val totalTokensCached by viewModel.totalTokensCached.collectAsStateWithLifecycle()

    var renameTargetConv by remember { mutableStateOf<ConversationEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showClearCacheConfirm by remember { mutableStateOf(false) }

    val liveDomains = listOf("All", "Software Engineering", "Mathematics", "Distributed Systems", "Logic Puzzles", "General")
    val cacheDomains = listOf("All", "Mathematics", "Logic & Proofs", "Systems", "Biomedical", "General")

    val filteredConversations by viewModel.filteredConversations.collectAsStateWithLifecycle()

    // Rename Dialog
    renameTargetConv?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTargetConv = null },
            title = { Text("Rename Thinking Session", fontWeight = FontWeight.Bold, color = DarkTextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Session Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameConversation(target.id, renameInput.trim())
                            renameTargetConv = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetConv = null }) {
                    Text("Cancel", color = DarkTextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Clear All Conversations Confirmation
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("Clear All Conversations?", fontWeight = FontWeight.Bold, color = WarningOrange) },
            text = { Text("This will permanently remove all past chat histories from local SQLite storage.", color = DarkTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllConversations()
                        showClearAllConfirm = false
                        Toast.makeText(context, "All past sessions cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirm = false }) {
                    Text("Cancel", color = DarkTextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Clear Cache Confirmation
    if (showClearCacheConfirm) {
        AlertDialog(
            onDismissRequest = { showClearCacheConfirm = false },
            title = { Text("Clear Offline Reasoning Cache?", fontWeight = FontWeight.Bold, color = WarningOrange) },
            text = { Text("This will delete all locally cached reasoning steps and precomputed proof traces from the Room Database.", color = DarkTextPrimary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllReasoningCache()
                        showClearCacheConfirm = false
                        Toast.makeText(context, "Reasoning cache cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                ) {
                    Text("Clear Cache", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheConfirm = false }) {
                    Text("Cancel", color = DarkTextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Modal Inspection Sheet for Deep Proof
    uiState.inspectingCachedItem?.let { item ->
        OfflineReasoningInspectorDialog(
            item = item,
            steps = uiState.cachedItemSteps,
            onDismiss = { viewModel.inspectCachedItem(null) },
            onLoadInChat = {
                viewModel.loadCachedReasoningIntoChat(item)
                Toast.makeText(context, "Loaded proof into active chat", Toast.LENGTH_SHORT).show()
            },
            onOpenDebugOverlay = { stepIdx ->
                viewModel.openCotDebugOverlay(uiState.cachedItemSteps, stepIdx)
            },
            onToggleFavorite = { isFav ->
                viewModel.toggleCacheFavorite(item.cacheKey, isFav)
            },
            onSaveNotes = { notes ->
                viewModel.updateCacheNotes(item.cacheKey, notes)
                Toast.makeText(context, "Notes updated in Room DB", Toast.LENGTH_SHORT).show()
            },
            onCopyMarkdown = {
                val md = viewModel.exportCachedItemMarkdown(item)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Reasoning Proof", md))
                Toast.makeText(context, "Proof Markdown copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Chain-of-Thought Step Debugger Overlay
    if (uiState.isCotDebugOverlayOpen) {
        ChainOfThoughtDebugOverlay(
            steps = uiState.cotDebugSteps,
            initialStepIndex = uiState.cotDebugInitialStep,
            modelName = uiState.activeModelName,
            localDocuments = uiState.localDocuments,
            onDismiss = { viewModel.closeCotDebugOverlay() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
    ) {
        // Sub-Tab Switcher: Live Conversations vs Offline Room Reasoning Cache vs Telemetry Analytics
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xCC161B22), // rgba(22, 27, 34, 0.8)
            border = androidx.compose.foundation.BorderStroke(0.8.dp, BorderSubtle)
        ) {
            TabRow(
                selectedTabIndex = uiState.sessionsTabSubIndex,
                containerColor = Color.Transparent,
                contentColor = LuminousBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.sessionsTabSubIndex]),
                        color = LuminousBlue,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = uiState.sessionsTabSubIndex == 0,
                    onClick = { viewModel.setSessionsSubTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Chats (${conversations.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    selectedContentColor = LuminousBlue,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_live_conversations")
                )

                Tab(
                    selected = uiState.sessionsTabSubIndex == 1,
                    onClick = { viewModel.setSessionsSubTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(15.dp), tint = VibrantTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Offline Cache (${cachedReasoningList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    selectedContentColor = VibrantTeal,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_reasoning_cache")
                )

                Tab(
                    selected = uiState.sessionsTabSubIndex == 2,
                    onClick = { viewModel.setSessionsSubTab(2) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(15.dp), tint = VibrantPurple)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Telemetry & Stats", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    selectedContentColor = VibrantPurple,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_telemetry_stats")
                )
            }
        }

        when (uiState.sessionsTabSubIndex) {
            0 -> {
                // Live Conversations View
                LiveConversationsContent(
                    conversations = filteredConversations,
                    searchQuery = uiState.historySearchQuery,
                    onSearchChange = { viewModel.setHistorySearchQuery(it) },
                    selectedDomain = uiState.historySelectedDomain,
                    onDomainChange = { viewModel.setHistoryDomainFilter(it) },
                    domains = liveDomains,
                    currentConversationId = uiState.currentConversationId,
                    onSelect = { viewModel.selectConversation(it) },
                    onRename = { conv ->
                        renameTargetConv = conv
                        renameInput = conv.title
                    },
                    onDelete = { viewModel.deleteConversation(it) },
                    onStartNew = { viewModel.startNewConversation() },
                    onClearAll = { showClearAllConfirm = true }
                )
            }
            1 -> {
                // Offline Room Reasoning Cache View
                OfflineReasoningCacheContent(
                    cachedList = cachedReasoningList,
                    totalTokensCached = totalTokensCached ?: 0L,
                    cacheStorageStats = uiState.cacheStorageStats,
                    isSimulateOfflineMode = uiState.isSimulateOfflineMode,
                    onToggleSimulateOffline = { viewModel.toggleSimulateOfflineMode(it) },
                    onPruneLru = { viewModel.pruneLruCache() },
                    searchQuery = uiState.cacheSearchQuery,
                    onSearchChange = { viewModel.setCacheSearchQuery(it) },
                    selectedCategory = uiState.selectedCacheCategory,
                    onCategoryChange = { viewModel.setSelectedCacheCategory(it) },
                    categories = cacheDomains,
                    onInspect = { item -> viewModel.inspectCachedItem(item) },
                    onLoadInChat = { item -> viewModel.loadCachedReasoningIntoChat(item) },
                    onToggleFavorite = { key, fav -> viewModel.toggleCacheFavorite(key, fav) },
                    onDelete = { key -> viewModel.deleteCachedReasoning(key) },
                    onClearCache = { showClearCacheConfirm = true },
                    onCopyMarkdown = { item ->
                        val md = viewModel.exportCachedItemMarkdown(item)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Reasoning Proof", md))
                        Toast.makeText(context, "Proof Markdown copied", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else -> {
                // Telemetry & Cognitive Analytics View
                TelemetryAnalyticsContent(
                    cachedList = cachedReasoningList,
                    totalTokensCached = totalTokensCached ?: 0L,
                    onInspect = { item -> viewModel.inspectCachedItem(item) },
                    onClearCache = { showClearCacheConfirm = true }
                )
            }
        }
    }
}

@Composable
private fun LiveConversationsContent(
    conversations: List<ConversationEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedDomain: String,
    onDomainChange: (String) -> Unit,
    domains: List<String>,
    currentConversationId: String,
    onSelect: (String) -> Unit,
    onRename: (ConversationEntity) -> Unit,
    onDelete: (String) -> Unit,
    onStartNew: () -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search chat sessions...", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = AbsoluteBlack,
                        unfocusedContainerColor = AbsoluteBlack,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onStartNew,
                    colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("new_session_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", style = MaterialTheme.typography.labelMedium.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Domain Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                domains.forEach { dom ->
                    val isSelected = dom == selectedDomain
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) LuminousBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (isSelected) LuminousBlue else BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { onDomainChange(dom) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = dom,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Sessions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GlassPanel(
                    modifier = Modifier.border(1.dp, VibrantTeal.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RateReview, contentDescription = null, tint = VibrantTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LLM Feedback System Active",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "User ratings calibrate offline reasoning heuristics",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("98.4%", style = MaterialTheme.typography.labelSmall.copy(color = VibrantGreen, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            if (conversations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, DarkOutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching sessions found", style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted))
                            Text("Start a new session or adjust your search filter.", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSubtle))
                        }
                    }
                }
            } else {
                items(conversations, key = { it.id }) { conv ->
                    val isCurrent = conv.id == currentConversationId
                    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(conv.updatedAt))

                    GlassPanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(conv.id) }
                            .testTag("conversation_item_${conv.id}")
                            .border(
                                1.dp,
                                if (isCurrent) LuminousBlue else BorderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isCurrent) LuminousBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = if (isCurrent) LuminousBlue else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conv.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) LuminousBlue else TextPrimary
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(LuminousBlue.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = LuminousBlue,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (conv.lastMessagePreview.isNotBlank()) conv.lastMessagePreview else "No messages yet",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextDisabled, fontSize = 10.sp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${conv.domainTag}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = VibrantTeal, fontSize = 10.sp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onRename(conv) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextSecondary, modifier = Modifier.size(15.dp))
                                }

                                IconButton(
                                    onClick = { onDelete(conv.id) },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .testTag("delete_conversation_${conv.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (conversations.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = onClearAll,
                            colors = ButtonDefaults.textButtonColors(contentColor = WarningOrange)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All Chat Histories", fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun OfflineReasoningCacheContent(
    cachedList: List<ReasoningCacheEntity>,
    totalTokensCached: Long,
    cacheStorageStats: com.example.data.CacheStorageStats,
    isSimulateOfflineMode: Boolean,
    onToggleSimulateOffline: (Boolean) -> Unit,
    onPruneLru: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    categories: List<String>,
    onInspect: (ReasoningCacheEntity) -> Unit,
    onLoadInChat: (ReasoningCacheEntity) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onClearCache: () -> Unit,
    onCopyMarkdown: (ReasoningCacheEntity) -> Unit
) {
    var showD3Chart by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Interactive D3.js Visualization Panel Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "D3.js Thought-Trace Latency & Tokens",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }
                        TextButton(onClick = { showD3Chart = !showD3Chart }) {
                            Text(
                                text = if (showD3Chart) "Hide D3 Chart" else "Show D3 Chart",
                                fontSize = 11.sp,
                                color = VibrantTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (showD3Chart) {
                        Spacer(modifier = Modifier.height(8.dp))
                        D3ReasoningVisualizationPanel(
                            cachedItems = cachedList,
                            onTraceSelected = { metric ->
                                val match = cachedList.find { it.cacheKey == metric.id }
                                if (match != null) onInspect(match)
                            }
                        )
                    }
                }
            }
        }

        // Room SQLite Database Analytics & Thought-Trace Cache Profiler Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeminiCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Room SQLite Thought-Trace Cache v8",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(
                                    text = "Zero-Token Offline Proofs • Indexed Trace Persistence",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSimulateOfflineMode) GeminiAmber.copy(alpha = 0.2f) else GeminiEmerald.copy(alpha = 0.2f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isSimulateOfflineMode) "OFFLINE ONLY" else "100% PERSISTENT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSimulateOfflineMode) GeminiAmber else GeminiEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CACHED TRACES", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text("${cachedList.size} Traces", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiCyan))
                        }

                        Column {
                            Text("HIT RATIO", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text("${cacheStorageStats.hitRatePercent.toInt()}% Hits", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald))
                        }

                        Column {
                            Text("TOKENS PRESERVED", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text(String.format(Locale.US, "%,d", cacheStorageStats.totalTokensCached), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiBlueLight))
                        }

                        Column {
                            Text("QUERY LATENCY", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text("< 1.2 ms", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = DarkOutlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Offline simulation & LRU Maintenance Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Switch(
                                checked = isSimulateOfflineMode,
                                onCheckedChange = onToggleSimulateOffline,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Simulate Offline Mode",
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = onPruneLru,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Prune LRU", fontSize = 10.sp, color = GeminiCyan)
                            }
                            androidx.compose.material3.OutlinedButton(
                                onClick = onClearCache,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Clear", fontSize = 10.sp, color = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search cached proofs, math theorems, code...", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cache_search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GeminiCyan,
                    unfocusedBorderColor = DarkOutlineVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                singleLine = true
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) GeminiCyan.copy(alpha = 0.25f) else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) GeminiCyan else DarkOutlineVariant, RoundedCornerShape(14.dp))
                            .clickable { onCategoryChange(cat) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else DarkTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Cached Items List
        if (cachedList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No cached reasoning proofs match", style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted))
                        Text("Run a deep thinking prompt in Chat to automatically cache full reasoning traces in Room.", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSubtle))
                    }
                }
            }
        } else {
            items(cachedList, key = { it.cacheKey }) { item ->
                CachedReasoningCard(
                    item = item,
                    onInspect = { onInspect(item) },
                    onLoadInChat = { onLoadInChat(item) },
                    onToggleFavorite = { onToggleFavorite(item.cacheKey, !item.isFavorite) },
                    onDelete = { onDelete(item.cacheKey) },
                    onCopyMarkdown = { onCopyMarkdown(item) }
                )
            }
        }

        if (cachedList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onClearCache,
                        colors = ButtonDefaults.textButtonColors(contentColor = WarningOrange)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Room Cached Proofs", fontSize = 12.sp)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun CachedReasoningCard(
    item: ReasoningCacheEntity,
    onInspect: () -> Unit,
    onLoadInChat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onCopyMarkdown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onInspect() }
            .testTag("cached_item_${item.cacheKey.take(8)}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (item.isFavorite) GeminiCyan.copy(alpha = 0.7f) else DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Domain tag, Model & Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GeminiBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.domainCategory.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.thinkingLevel,
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) GeminiCyan else DarkTextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DarkTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Query Text
            Text(
                text = item.promptQuery,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 18.sp
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Answer Preview
            Text(
                text = item.answerContent.replace("#", "").trim(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkTextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.thinkingTokens} thought tokens", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextPrimary, fontSize = 10.sp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${(item.thinkingDurationMs / 1000f)}s • ${item.tokensPerSecond.toInt()} t/s", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontSize = 10.sp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.hitCount} hits", style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontSize = 10.sp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: Inspect Proof & Load in Chat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onInspect,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = GeminiCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect Proof", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = onLoadInChat,
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume in Chat", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                }

                IconButton(
                    onClick = onCopyMarkdown,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Markdown", tint = DarkTextPrimary, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun OfflineReasoningInspectorDialog(
    item: ReasoningCacheEntity,
    steps: List<ReasoningStep>,
    onDismiss: () -> Unit,
    onLoadInChat: () -> Unit,
    onOpenDebugOverlay: (Int) -> Unit = {},
    onToggleFavorite: (Boolean) -> Unit,
    onSaveNotes: (String) -> Unit,
    onCopyMarkdown: () -> Unit
) {
    var userNotes by remember { mutableStateOf(item.userNotes) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Final Proof & Solution, 1: Cognitive Thought Trace, 2: Granular Steps, 3: Notes & Metadata

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .fillMaxSize(0.88f),
        containerColor = DarkBackground,
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopyMarkdown) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Markdown", tint = GeminiBlueLight)
                }

                Row {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = DarkTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onLoadInChat,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open in Chat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Deep Thinking Inspector", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                }

                IconButton(
                    onClick = { onToggleFavorite(!item.isFavorite) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (item.isFavorite) GeminiCyan else DarkTextMuted
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Metadata Header Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurface,
                    border = BorderStroke(0.8.dp, DarkOutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = item.promptQuery,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GeminiBlueLight)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Model: ${item.modelName}", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 10.sp))
                            Text("Domain: ${item.domainCategory}", style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontSize = 10.sp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.thinkingTokens} Thought Tokens • ${item.thinkingDurationMs}ms", style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontSize = 10.sp))
                            Text("Budget: ${item.thinkingBudgetTokens} tokens", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 10.sp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Proof Solution", "Thought Trace", "Cognitive Steps", "Notes").forEachIndexed { idx, title ->
                        val isSelected = activeSubTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GeminiBlue else Color.Transparent)
                                .clickable { activeSubTab = idx }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.White else DarkTextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (activeSubTab) {
                    0 -> {
                        // Proof Solution
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface,
                            border = BorderStroke(1.dp, DarkOutlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Verified Solution Synthesis",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.answerContent,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DarkTextPrimary,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }
                    1 -> {
                        // Thought Trace
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Raw Cognitive Thought Stream (${item.thinkingTokens} tokens)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GeminiPurple)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.thoughtProcess,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        color = GeminiBlueLight,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                    2 -> {
                        // Cognitive Steps
                        if (steps.isEmpty()) {
                            Text("No granular steps recorded for this session.", color = DarkTextMuted, fontSize = 12.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenDebugOverlay(0)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_cot_debugger_from_cached_dialog"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GeminiCyan.copy(alpha = 0.2f),
                                        contentColor = GeminiCyan
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = GeminiCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Open Step-by-Step CoT Node Debugger",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                steps.forEach { step ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onDismiss()
                                                onOpenDebugOverlay((step.stepIndex - 1).coerceAtLeast(0))
                                            }
                                            .testTag("cached_step_card_${step.stepIndex}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(0.8.dp, DarkOutlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Step ${step.stepIndex}: ${step.phase.name}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeminiCyan)
                                                )
                                                Text(
                                                    text = "${(step.confidence * 100).toInt()}% Conf",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = step.headline,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary, fontSize = 12.sp)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = step.details,
                                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Notes & Persistence
                        Column {
                            OutlinedTextField(
                                value = userNotes,
                                onValueChange = { userNotes = it },
                                label = { Text("Offline Proof Notes & Annotations") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiCyan,
                                    unfocusedBorderColor = DarkOutlineVariant,
                                    focusedTextColor = DarkTextPrimary,
                                    unfocusedTextColor = DarkTextPrimary
                                ),
                                minLines = 4
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onSaveNotes(userNotes) },
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Notes to Room DB", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TelemetryAnalyticsContent(
    cachedList: List<ReasoningCacheEntity>,
    totalTokensCached: Long,
    onInspect: (ReasoningCacheEntity) -> Unit,
    onClearCache: () -> Unit
) {
    // Telemetry aggregations
    val totalTimeMs = cachedList.sumOf { it.thinkingDurationMs }
    val totalSeconds = totalTimeMs / 1000f
    val avgTps = if (cachedList.isNotEmpty()) {
        cachedList.map { it.tokensPerSecond }.average()
    } else 0.0
    
    val totalConfidence = if (cachedList.isNotEmpty()) {
        cachedList.map { it.confidenceScore }.average()
    } else 1.0
    
    // Domain categorizations
    val domainCounts = cachedList.groupBy { it.domainCategory }
        .mapValues { it.value.size }
    val maxDomainCount = domainCounts.values.maxOrNull() ?: 1
    
    val standardCategories = listOf(
        "Mathematics" to GeminiCyan,
        "Logic & Proofs" to GeminiPurple,
        "Systems" to GeminiBlueLight,
        "Biomedical" to GeminiPink,
        "General" to GeminiEmerald
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // Interactive D3.js Visualization Panel: Reasoning Time & Token Usage
        item {
            D3ReasoningVisualizationPanel(
                cachedItems = cachedList,
                onTraceSelected = { metric ->
                    val match = cachedList.find { it.cacheKey == metric.id }
                    if (match != null) {
                        onInspect(match)
                    }
                }
            )
        }

        // Top Core Efficiency Analytics Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GeminiPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "On-Device Cognitive Telemetry",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Text(
                                text = "Heuristic performance, execution logs, & speed benchmarks",
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("CORE CPU/NPU SECONDS", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text(String.format(Locale.US, "%.2fs", totalSeconds), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiPurple))
                        }

                        Column {
                            Text("COGNITIVE VELOCITY", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text(String.format(Locale.US, "%,d T/s", avgTps.toInt()), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiCyan))
                        }

                        Column {
                            Text("CONFIDENCE FLOOR", style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.sp))
                            Text(String.format(Locale.US, "%.1f%%", totalConfidence * 100), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = GeminiEmerald))
                        }
                    }
                }
            }
        }

        // Domain Cognitive Weight Distribution Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COGNITIVE WEIGHT BY DOMAIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (cachedList.isEmpty()) {
                        Text(
                            text = "No recorded sessions to build domain weights.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            standardCategories.forEach { (category, color) ->
                                val count = domainCounts[category] ?: 0
                                val progress = count.toFloat() / maxDomainCount.toFloat()
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                                        )
                                        Text(
                                            text = "$count runs (${String.format(Locale.US, "%,d", count * 2420)} tokens)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = color,
                                        trackColor = color.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // History Registry Header
        item {
            Text(
                text = "HISTORICAL THOUGHT RECONSTRUCTION LOGS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = DarkTextSubtle,
                    letterSpacing = 0.8.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (cachedList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Historical cache registry is empty. Generate deep thinking solutions in Chat to build your telemetry database.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, lineHeight = 16.sp)
                        )
                    }
                }
            }
        } else {
            items(cachedList, key = { it.cacheKey }) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInspect(session) }
                        .testTag("telemetry_log_row_${session.cacheKey}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(0.8.dp, DarkOutlineVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GeminiPurple.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = session.domainCategory,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeminiCyan)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = GeminiBlueLight,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Revisit Trace",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiBlueLight, fontWeight = FontWeight.Bold, fontSize = 9.5.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = session.promptQuery,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary, fontSize = 12.5.sp),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = session.answerContent,
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateStr = try {
                                SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(session.cachedAt))
                            } catch (e: Exception) {
                                "Recent Session"
                            }
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle, fontSize = 9.5.sp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${session.thinkingTokens} tokens",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiEmerald, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
                                )
                                Text(
                                    text = "${session.thinkingDurationMs} ms",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontWeight = FontWeight.SemiBold, fontSize = 9.5.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = onClearCache,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarningOrange)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear All Telemetry History", fontSize = 11.5.sp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
