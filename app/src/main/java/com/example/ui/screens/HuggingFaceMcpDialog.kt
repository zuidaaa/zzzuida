package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mcp.HfHubItem
import com.example.mcp.HfItemType
import com.example.mcp.McpServerConfig
import com.example.mcp.McpServerStatus
import com.example.mcp.McpToolCallResult
import com.example.ui.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuggingFaceMcpDialog(
    uiState: UiState,
    onDismiss: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onUpdateConfig: (String, String, Long) -> Unit,
    onVerifyToken: (String, (Boolean, String) -> Unit) -> Unit,
    onPingServer: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSelectTool: (String) -> Unit,
    onToolArgsChange: (String) -> Unit,
    onExecuteTool: () -> Unit,
    onInsertResource: (HfHubItem) -> Unit,
    onToggleGrounding: (Boolean) -> Unit,
    onDownloadModel: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("hf_mcp_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                HfMcpHeader(
                    status = uiState.hfMcpStatus,
                    onClose = onDismiss,
                    onPing = onPingServer
                )

                // Tabs
                val tabTitles = listOf("Server Config", "Hub Explorer", "Tool Console", "Daily Papers")
                PrimaryTabRow(
                    selectedTabIndex = uiState.hfMcpSelectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("hf_mcp_tabs")
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.hfMcpSelectedTab == index,
                            onClick = { onTabSelected(index) },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (uiState.hfMcpSelectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag("hf_mcp_tab_$index")
                        )
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (uiState.hfMcpSelectedTab) {
                        0 -> HfServerConfigTab(
                            config = uiState.hfMcpConfig,
                            status = uiState.hfMcpStatus,
                            onUpdateConfig = onUpdateConfig,
                            onVerifyToken = onVerifyToken,
                            context = context
                        )
                        1 -> HfHubExplorerTab(
                            query = uiState.hfMcpSearchQuery,
                            activeCategory = uiState.hfMcpActiveCategory,
                            isSearching = uiState.hfMcpIsSearching,
                            results = uiState.hfMcpSearchResults,
                            onQueryChange = onSearchQueryChange,
                            onCategorySelect = onCategorySelected,
                            onSearch = onSearch,
                            onInsert = onInsertResource,
                            onDownloadModel = onDownloadModel,
                            context = context
                        )
                        2 -> HfToolInvokerTab(
                            selectedTool = uiState.hfMcpSelectedTool,
                            toolArgsJson = uiState.hfMcpToolArgsJson,
                            isCalling = uiState.hfMcpIsCallingTool,
                            result = uiState.hfMcpLastToolResult,
                            onSelectTool = onSelectTool,
                            onArgsChange = onToolArgsChange,
                            onExecute = onExecuteTool,
                            context = context
                        )
                        3 -> HfDailyPapersTab(
                            papers = uiState.hfMcpDailyPapers,
                            onInsert = onInsertResource,
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HfMcpHeader(
    status: McpServerStatus,
    onClose: () -> Unit,
    onPing: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFD21E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤗",
                        fontSize = 22.sp
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Hugging Face MCP Connector",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Status indicator badge
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (status.isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252))
                        )
                    }

                    Text(
                        text = if (status.isConnected) "Connected (Latency: ${status.latencyMs}ms)" else "hf-endpoints MCP Server",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ping button
                IconButton(
                    onClick = onPing,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("hf_ping_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Ping MCP Server",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("hf_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: Server Config & Endpoints
// -------------------------------------------------------------
@Composable
private fun HfServerConfigTab(
    config: McpServerConfig,
    status: McpServerStatus,
    onUpdateConfig: (String, String, Long) -> Unit,
    onVerifyToken: (String, (Boolean, String) -> Unit) -> Unit,
    context: Context
) {
    var serverUrl by remember { mutableStateOf(config.httpUrl) }
    var apiKey by remember { mutableStateOf(config.hfApiToken) }
    var timeoutSec by remember { mutableStateOf("15") }
    var verificationStatusMsg by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hugging Face Secure OAuth Login Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (config.isOauthenticated) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD21E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤗", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Hugging Face Hub Authentication",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (config.isOauthenticated) "Securely connected via user access token" else "Authenticate with Hugging Face to access models & datasets",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (config.isOauthenticated) {
                    // Authenticated State View
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F5E9).copy(alpha = 0.8f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Logged in as @${config.username ?: "hf_user"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32)
                            )
                            if (!config.fullname.isNullOrBlank()) {
                                Text(
                                    text = config.fullname,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            if (config.scopes.isNotEmpty()) {
                                Text(
                                    text = "Scopes: ${config.scopes.joinToString(", ")}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF1B5E20).copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                // Reset token to log out
                                onUpdateConfig(serverUrl, "", timeoutSec.toLongOrNull() ?: 15L)
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Log Out",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Unauthenticated/Login State View
                    Text(
                        text = "Accessing gated or private models on the Hugging Face Hub (including deep reasoning versions) requires a User Access Token. Click the button below to initiate secure OAuth login in your browser.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hugging Face login initiator
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://huggingface.co/mcp?login")
                                    )
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening Hugging Face OAuth login page...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to launch browser", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD21E),
                                contentColor = Color(0xFF212121)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("hf_oauth_login_button")
                        ) {
                            Text("🤗 Login via Hugging Face", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Preset Token Helper
                        OutlinedButton(
                            onClick = {
                                apiKey = "hf_mock_read_token"
                                Toast.makeText(context, "Mock Token Loaded! Press 'Verify Token' to simulate.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text("Use Demo Mode", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("Enter/Paste HF User Access Token (hf_...)") },
                        placeholder = { Text("Paste your READ token from the login page") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hf_api_key_input_oauth"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (verificationStatusMsg.isNotBlank()) {
                            Text(
                                text = verificationStatusMsg,
                                fontSize = 11.sp,
                                color = if (verificationStatusMsg.contains("Successfully")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                if (apiKey.isBlank()) {
                                    Toast.makeText(context, "Please enter a token first", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isVerifying = true
                                verificationStatusMsg = "Checking Hugging Face authorization..."
                                onVerifyToken(apiKey) { success, msg ->
                                    isVerifying = false
                                    verificationStatusMsg = msg
                                    if (success) {
                                        Toast.makeText(context, "Authorization Verified!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Verification Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isVerifying && apiKey.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("hf_verify_token_button")
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify Token")
                            }
                        }
                    }
                }
            }
        }

        // Connection Form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Server Settings",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("MCP Server HTTP URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hf_server_url_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = timeoutSec,
                        onValueChange = { timeoutSec = it },
                        label = { Text("Timeout (sec)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val t = timeoutSec.toLongOrNull() ?: 15L
                            onUpdateConfig(serverUrl, apiKey, t)
                            Toast.makeText(context, "MCP Configuration Saved", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .testTag("hf_save_config_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }
                }
            }
        }

        // MCP Specification Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Model Context Protocol (MCP) Configuration",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Direct connection to the Hugging Face Hub MCP Server allowing assistant models to search models, datasets, spaces, papers, and run inference pipelines.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // JSON MCP Config View
                val jsonSnippet = config.toJsonConfigString()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = jsonSnippet,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = {
                                copyToClipboard(context, "MCP Config", jsonSnippet)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy JSON",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Capabilities and Tools List
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "MCP Server Tools & Capabilities (${status.availableToolsCount} Active)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                val tools = listOf(
                    "hf_hub_search_models" to "Search open-source AI models by task, library, or keywords",
                    "hf_hub_get_model" to "Inspect model cards, license, parameters, and architectures",
                    "hf_hub_search_datasets" to "Discover fine-tuning corpora, math benchmarks, reasoning data",
                    "hf_hub_search_spaces" to "Search interactive Gradio / Streamlit community spaces",
                    "hf_hub_search_papers" to "Fetch arXiv daily papers with community discussions & links",
                    "hf_hub_run_inference" to "Execute serverless inference on supported Hugging Face models"
                )

                tools.forEach { (name, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: Hub Explorer (Models, Datasets, Spaces, Papers)
// -------------------------------------------------------------
@Composable
private fun HfHubExplorerTab(
    query: String,
    activeCategory: String,
    isSearching: Boolean,
    results: List<HfHubItem>,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onSearch: (String) -> Unit,
    onInsert: (HfHubItem) -> Unit,
    onDownloadModel: ((String) -> Unit)? = null,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search Hugging Face Hub (e.g. deepseek, qwen, gsm8k)...", fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("hf_search_bar"),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onSearch(query) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("hf_search_button")
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Search")
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf("All", "Models", "Datasets", "Spaces", "Papers")
            categories.forEach { cat ->
                FilterChip(
                    selected = activeCategory == cat,
                    onClick = { onCategorySelect(cat) },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("hf_cat_$cat")
                )
            }
        }

        // Results List
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text("Querying Hugging Face MCP Hub...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("No Hub items found. Try searching 'deepseek' or 'llama'.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("hf_results_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.id }) { item ->
                    HfItemCard(
                        item = item,
                        onInsert = { onInsert(item) },
                        onDownload = if (item.type == HfItemType.MODEL && onDownloadModel != null) {
                            { onDownloadModel(item.id) }
                        } else null,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun HfItemCard(
    item: HfHubItem,
    onInsert: () -> Unit,
    onDownload: (() -> Unit)? = null,
    context: Context
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val (badgeText, badgeColor) = when (item.type) {
                        HfItemType.MODEL -> "MODEL" to Color(0xFF673AB7)
                        HfItemType.DATASET -> "DATASET" to Color(0xFF00897B)
                        HfItemType.SPACE -> "SPACE" to Color(0xFFE65100)
                        HfItemType.PAPER -> "PAPER" to Color(0xFF1565C0)
                        HfItemType.COMMUNITY_TOOL -> "TOOL" to Color(0xFF43A047)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = item.name.ifBlank { item.id },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.likes > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${item.likes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (item.downloads > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(formatCount(item.downloads), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tags row
            if (item.tags.isNotEmpty() || item.task.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.task.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = item.task, fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    item.tags.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = tag, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = when (item.type) {
                            HfItemType.MODEL -> "hf://models/${item.id}"
                            HfItemType.DATASET -> "hf://datasets/${item.id}"
                            HfItemType.SPACE -> "hf://spaces/${item.id}"
                            HfItemType.PAPER -> "hf://papers/${item.id}"
                            HfItemType.COMMUNITY_TOOL -> "hf://tools/${item.id}"
                        }
                        copyToClipboard(context, "MCP Resource URI", uri)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MCP URI", fontSize = 11.sp)
                }

                if (onDownload != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(
                        onClick = onDownload,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF9C27B0))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download GGUF", fontSize = 11.sp, color = Color(0xFF9C27B0))
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onInsert,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reason with Model", fontSize = 11.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: MCP Tool Invoker & Console
// -------------------------------------------------------------
@Composable
private fun HfToolInvokerTab(
    selectedTool: String,
    toolArgsJson: String,
    isCalling: Boolean,
    result: McpToolCallResult?,
    onSelectTool: (String) -> Unit,
    onArgsChange: (String) -> Unit,
    onExecute: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tool Selection Chips
        Text("Select Hugging Face MCP Tool", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

        val tools = listOf(
            "hf_hub_search_models",
            "hf_hub_get_model",
            "hf_hub_search_datasets",
            "hf_hub_search_spaces",
            "hf_hub_search_papers",
            "hf_hub_run_inference"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tools.forEach { tool ->
                FilterChip(
                    selected = selectedTool == tool,
                    onClick = { onSelectTool(tool) },
                    label = { Text(tool, fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Arguments Editor
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tool Arguments (JSON)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    IconButton(
                        onClick = { onArgsChange("{}") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp))
                    }
                }

                OutlinedTextField(
                    value = toolArgsJson,
                    onValueChange = onArgsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("hf_tool_args_input"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = onExecute,
                    enabled = !isCalling,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hf_execute_tool_button")
                ) {
                    if (isCalling) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calling MCP Tool...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute $selectedTool")
                    }
                }
            }
        }

        // Tool Output View
        result?.let { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (!res.isSuccess) Color(0xFFFF5252) else Color(0xFF4CAF50))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (!res.isSuccess) "ERROR" else "200 OK", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text(res.toolName, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { copyToClipboard(context, "Tool Result", res.responseJson) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = res.responseJson,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: Daily Papers
// -------------------------------------------------------------
@Composable
private fun HfDailyPapersTab(
    papers: List<HfHubItem>,
    onInsert: (HfHubItem) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Hugging Face Daily AI & Reasoning Papers",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(papers, key = { it.id }) { paper ->
                HfItemCard(
                    item = paper,
                    onInsert = { onInsert(paper) },
                    context = context
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> "$count"
    }
}
