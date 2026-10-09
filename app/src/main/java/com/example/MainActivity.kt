package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WikiMaintenanceWorker
import com.example.engine.skills.SkillsSyncWorker
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.screens.*
import com.example.ui.components.EdgePanel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        lifecycleScope.launch(Dispatchers.IO) {
            WikiMaintenanceWorker.schedule(applicationContext)
            SkillsSyncWorker.schedule(applicationContext)
        }

        val decorView = window.decorView
        viewModel.registerHapticCallback {
            decorView.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            MyApplicationTheme(darkTheme = systemDark) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                if (uiState.isLoggedIn) {
                    MainAppLayout(viewModel = viewModel)
                } else {
                    SignInScreen(
                        onSignInClick = { viewModel.performSignIn(this@MainActivity) },
                        isAuthenticating = uiState.isAuthenticating,
                        errorMessage = uiState.authStatusMessage
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val localModels by viewModel.localModels.collectAsStateWithLifecycle()
    
    var showSettingsModal by remember { mutableStateOf(false) }

    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            confirmButton = {
                TextButton(onClick = { showSettingsModal = false }) {
                    Text("Close", color = LuminousBlue, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(viewModel = viewModel, uiState = uiState)
                }
            },
            containerColor = AbsoluteBlack,
            modifier = Modifier.fillMaxSize(0.95f)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(AbsoluteBlack)) {
        // CONTENT CONTAINER (Margin Top 64, Right 64)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, end = 64.dp)
        ) {
            when (uiState.activeTab) {
                0 -> ChatScreen(viewModel = viewModel, uiState = uiState, messages = messages)
                1 -> OllamaAndLlmApiScreen(viewModel = viewModel, uiState = uiState)
                2 -> OctopusAgentScreen(viewModel = viewModel, uiState = uiState)
                3 -> ReasoningSandboxesScreen(viewModel = viewModel)
                4 -> ConversationsHistoryScreen(viewModel = viewModel, uiState = uiState, conversations = conversations)
                5 -> ResearchTuningScreen(viewModel = viewModel, uiState = uiState)
                6 -> FolderFoundationScreen(viewModel = viewModel, uiState = uiState)
                else -> ChatScreen(viewModel = viewModel, uiState = uiState, messages = messages)
            }
        }

        // HEADER GLOBAL (Height 64)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xCC161B22)) // rgba(22, 27, 34, 0.8)
                .blur(10.dp) // Simulated backdrop blur (note: real blur is expensive, but spec asks for it)
                .border(bottom = 1.dp, color = BorderSubtle)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = VibrantPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "T#ink insid3 the b0x",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "READY",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = VibrantGreen
                        )
                    }
                }

                // Right Group
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderActionIcon(Icons.Default.Add) { /* New Chat */ }
                    HeaderActionIcon(text = "🤗") { viewModel.openHfMcpConnector() }
                    HeaderActionIcon(Icons.Default.DirectionsCar) { viewModel.openDrivingMode() }
                    HeaderActionIcon(Icons.Default.AutoAwesome) { viewModel.openOneUi9Center() }
                    HeaderActionIcon(Icons.Default.FilterList) { /* Filter */ }
                    HeaderActionIcon(Icons.Default.Settings) { showSettingsModal = true }
                }
            }
        }

        // ACTION SIDEBAR RIGHT GLOBAL (Width 64)
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(top = 64.dp, bottom = 72.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0x1AFFFFFF), Color.Transparent)
                    )
                )
                .background(Color(0xCC000000))
                .border(left = 1.dp, color = Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SidebarActionIcon(Icons.Default.Menu) { viewModel.toggleEdgePanel() }
                SidebarActionIcon(Icons.Default.Add) { viewModel.startNewConversation() }
                SidebarActionIcon(Icons.Default.Search) { /* Global Search */ }
                
                Spacer(modifier = Modifier.weight(1f))
                
                SidebarActionIcon(Icons.Default.Settings) { showSettingsModal = true }
            }
        }

        // Edge Panel
        EdgePanel(
            isVisible = uiState.isEdgePanelOpen,
            onToggle = { viewModel.toggleEdgePanel() }
        ) {
            EdgePanelContent(viewModel, uiState, onSettingsClick = { showSettingsModal = true })
        }

        // Global Overlays (Hugging Face, Driving Mode, etc.)
        GlobalOverlays(viewModel, uiState)
    }
}

@Composable
fun HeaderActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector? = null, text: String? = null, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        } else if (text != null) {
            Text(text, fontSize = 16.sp)
        }
    }
}

@Composable
fun RowScope.BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) activeColor else TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SidebarActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun EdgePanelContent(viewModel: MainViewModel, uiState: UiState, onSettingsClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "T#ink insid3 the b0x",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        NavigationItem("Cognitive Chat", Icons.AutoMirrored.Filled.Chat, uiState.activeTab == 0) { 
            viewModel.setActiveTab(0); viewModel.toggleEdgePanel() 
        }
        NavigationItem("Octopus Agent", Icons.Default.SmartButton, uiState.activeTab == 2) { 
            viewModel.setActiveTab(2); viewModel.toggleEdgePanel() 
        }
        NavigationItem("Ollama & API", Icons.Default.Hub, uiState.activeTab == 1) { 
            viewModel.setActiveTab(1); viewModel.toggleEdgePanel() 
        }
        NavigationItem("Reasoning Sandboxes", Icons.Default.Psychology, uiState.activeTab == 3) { 
            viewModel.setActiveTab(3); viewModel.toggleEdgePanel() 
        }
        NavigationItem("Research & Tuning", Icons.Default.Science, uiState.activeTab == 5) { 
            viewModel.setActiveTab(5); viewModel.toggleEdgePanel() 
        }
        NavigationItem("History & Analytics", Icons.Default.History, uiState.activeTab == 4) { 
            viewModel.setActiveTab(4); viewModel.toggleEdgePanel() 
        }
        NavigationItem("3-Folder Foundation", Icons.Default.FolderSpecial, uiState.activeTab == 6) { 
            viewModel.setActiveTab(6); viewModel.toggleEdgePanel() 
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("SYSTEM TOOLS", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 8.dp))
        
        NavigationItem("Vector Search", Icons.Default.Search, false) { /* Search */ }
        NavigationItem("System Settings", Icons.Default.Settings, false) { 
            onSettingsClick(); viewModel.toggleEdgePanel() 
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Text(
            "Ver 3.7.0-ALPHA", 
            style = MaterialTheme.typography.labelSmall, 
            color = TextDisabled,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
fun NavigationItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) LuminousBlue.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (isSelected) LuminousBlue else TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) Color.White else TextSecondary)
        }
    }
}

@Composable
fun GlobalOverlays(viewModel: MainViewModel, uiState: UiState) {
    if (uiState.isHfMcpConnectorOpen) {
        HuggingFaceMcpDialog(
            uiState = uiState,
            onDismiss = { viewModel.closeHfMcpConnector() },
            onTabSelected = { viewModel.setHfMcpSelectedTab(it) },
            onUpdateConfig = { url, token, timeout -> viewModel.updateHfMcpConfig(url, token, timeout) },
            onVerifyToken = { token, cb -> viewModel.verifyHfMcpToken(token, cb) },
            onPingServer = { viewModel.pingHfMcpServer() },
            onSearchQueryChange = { viewModel.onHfMcpSearchQueryChange(it) },
            onCategorySelected = { viewModel.setHfMcpCategory(it) },
            onSearch = { viewModel.searchHfHub(it) },
            onSelectTool = { viewModel.selectHfMcpTool(it) },
            onToolArgsChange = { viewModel.onHfMcpToolArgsChange(it) },
            onExecuteTool = { viewModel.executeHfMcpTool() },
            onInsertResource = { viewModel.insertHfResourceIntoChat(it) },
            onToggleGrounding = { viewModel.setHfMcpGrounding(it) },
            onDownloadModel = { viewModel.downloadHfModel(it) }
        )
    }
    if (uiState.isDrivingModeOpen) {
        DrivingCoPilotScreen(viewModel = viewModel, uiState = uiState, onClose = { viewModel.closeDrivingMode() })
    }
    if (viewModel.isOneUi9CenterOpen.collectAsStateWithLifecycle().value) {
        OneUi9ValidationCenterScreen(viewModel = viewModel, uiState = uiState, onClose = { viewModel.closeOneUi9Center() })
    }
}

// Utility extension for Modifier.border to support single side (not standard Compose, but spec implies clean borders)
fun Modifier.border(
    bottom: androidx.compose.ui.unit.Dp = 0.dp,
    top: androidx.compose.ui.unit.Dp = 0.dp,
    left: androidx.compose.ui.unit.Dp = 0.dp,
    right: androidx.compose.ui.unit.Dp = 0.dp,
    color: Color
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        if (bottom > 0.dp) {
            drawLine(color, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), bottom.toPx())
        }
        if (top > 0.dp) {
            drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), top.toPx())
        }
        if (left > 0.dp) {
            drawLine(color, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), left.toPx())
        }
        if (right > 0.dp) {
            drawLine(color, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), right.toPx())
        }
    }
)
