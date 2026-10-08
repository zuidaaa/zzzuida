package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FolderFileEntity
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.GlassPanel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderFoundationScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val currentList by viewModel.currentFolderFiles.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var editingFile by remember { mutableStateOf<FolderFileEntity?>(null) }

    val filteredList = remember(currentList, searchQuery) {
        if (searchQuery.isBlank()) currentList
        else currentList.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.content.contains(searchQuery, ignoreCase = true) 
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📁 3-Folder Foundation",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Persistent secondary DB independent of LLM model updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showNewFolderDialog = true },
                        modifier = Modifier
                            .background(GlassCardPanel, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = VibrantTeal)
                    }

                    Button(
                        onClick = { 
                            editingFile = null
                            showCreateDialog = true 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Entry")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Folder Tabs (Scrollable if many)
            ScrollableTabRow(
                selectedTabIndex = allFolders.indexOf(selectedFolder).coerceAtLeast(0),
                containerColor = GlassCardPanel,
                contentColor = Color.White,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    val idx = allFolders.indexOf(selectedFolder).coerceAtLeast(0)
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                        color = LuminousBlue
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                divider = {}
            ) {
                allFolders.forEach { folderName ->
                    Tab(
                        selected = selectedFolder == folderName,
                        onClick = { viewModel.selectFolder(folderName) },
                        text = { 
                            Text(
                                text = folderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (selectedFolder == folderName) Color.White else TextSecondary
                            ) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search $selectedFolder...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LuminousBlue) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LuminousBlue,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = AbsoluteBlack,
                    unfocusedContainerColor = AbsoluteBlack,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // File List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = TextDisabled, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No entries in $selectedFolder", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { file ->
                        FolderFileCard(
                            file = file,
                            allFolders = allFolders,
                            onEdit = {
                                editingFile = file
                                showCreateDialog = true
                            },
                            onDelete = {
                                viewModel.deleteFolderFile(file)
                            },
                            onMoveToFolder = { targetFolder ->
                                viewModel.saveFolderFile(
                                    folderName = targetFolder,
                                    title = file.title,
                                    content = file.content,
                                    dateString = file.dateString,
                                    id = file.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog) {
        FolderFileDialog(
            initialFolder = selectedFolder,
            allFolders = allFolders,
            editingFile = editingFile,
            onDismiss = { showCreateDialog = false },
            onSave = { folderName, title, content, dateStr ->
                viewModel.saveFolderFile(
                    folderName = folderName,
                    title = title,
                    content = content,
                    dateString = dateStr,
                    id = editingFile?.id
                )
                showCreateDialog = false
            }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create New Folder", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            // To "create" a folder in this Room setup, we just insert a dummy file 
                            // or just wait until a real file is added.
                            // But we want it to show up in the tabs.
                            // Let's add a placeholder file.
                            viewModel.saveFolderFile(
                                folderName = newFolderName.trim(),
                                title = ".folder_metadata",
                                content = "Folder created on ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}"
                            )
                            viewModel.selectFolder(newFolderName.trim())
                        }
                        showNewFolderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = GlassCardPanel
        )
    }
}

@Composable
fun FolderFileCard(
    file: FolderFileEntity,
    allFolders: List<String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveToFolder: (String) -> Unit
) {
    var showMoveMenu by remember { mutableStateOf(false) }

    GlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (file.title == ".folder_metadata") "📁 [System] Folder Placeholder" else file.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, 
                        color = if (file.title == ".folder_metadata") TextDisabled else Color.White
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box {
                        IconButton(onClick = { showMoveMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Move To", tint = VibrantTeal, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMoveMenu,
                            onDismissRequest = { showMoveMenu = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            allFolders.filter { it != file.folderName }.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folder, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        onMoveToFolder(folder)
                                        showMoveMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = LuminousBlue, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VibrantRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (file.dateString.isNotBlank() && file.title != ".folder_metadata") {
                Text(
                    text = "📅 ${file.dateString}",
                    style = MaterialTheme.typography.labelSmall,
                    color = VibrantTeal,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (file.title != ".folder_metadata") {
                Text(
                    text = file.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 4,
                    lineHeight = 18.sp
                )
            } else {
                Text(
                    text = "This is a placeholder entry to keep the folder active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FolderFileDialog(
    initialFolder: String,
    allFolders: List<String>,
    editingFile: FolderFileEntity?,
    onDismiss: () -> Unit,
    onSave: (folderName: String, title: String, content: String, dateString: String) -> Unit
) {
    var folderName by remember { mutableStateOf(editingFile?.folderName ?: initialFolder) }
    var title by remember { mutableStateOf(editingFile?.title ?: "") }
    var content by remember { mutableStateOf(editingFile?.content ?: "") }
    var dateString by remember { mutableStateOf(editingFile?.dateString ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingFile == null) "New Foundation Entry" else "Edit Entry", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Target Folder:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                // Use a FlowRow or similar for dynamic folders
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allFolders.forEach { f ->
                        FilterChip(
                            selected = folderName == f,
                            onClick = { folderName = f },
                            label = { Text(f, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuminousBlue.copy(alpha = 0.2f),
                                selectedLabelColor = LuminousBlue,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Topic") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date / Timestamp") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Notes / Snippet / Tickets") },
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuminousBlue,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(folderName, title, content, dateString) },
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = GlassCardPanel
    )
}
