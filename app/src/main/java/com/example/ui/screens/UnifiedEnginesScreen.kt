package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalModelEntity
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*

@Composable
fun UnifiedEnginesScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    localModels: List<LocalModelEntity>,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sleek Sub-navigation Scrollable Segmented Bar
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EngineSubTabItem(
                    title = "⚡ MLC (40 tok/s)",
                    icon = Icons.Default.Bolt,
                    isSelected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    testTag = "sub_tab_mlc_engine"
                )
                EngineSubTabItem(
                    title = "📚 AnythingLLM RAG",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    isSelected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    testTag = "sub_tab_anything_llm"
                )
                EngineSubTabItem(
                    title = "💻 Termux CLI",
                    icon = Icons.Default.Terminal,
                    isSelected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    testTag = "sub_tab_termux_cli"
                )
                EngineSubTabItem(
                    title = "Offline LLM",
                    icon = Icons.Default.Psychology,
                    isSelected = selectedSubTab == 3,
                    onClick = { selectedSubTab = 3 },
                    testTag = "sub_tab_offline_llm"
                )
                EngineSubTabItem(
                    title = "Models Hub",
                    icon = Icons.Default.Storage,
                    isSelected = selectedSubTab == 4,
                    onClick = { selectedSubTab = 4 },
                    testTag = "sub_tab_models_hub"
                )
                EngineSubTabItem(
                    title = "Pipelines",
                    icon = Icons.Default.AdminPanelSettings,
                    isSelected = selectedSubTab == 5,
                    onClick = { selectedSubTab = 5 },
                    testTag = "sub_tab_pipelines"
                )
                EngineSubTabItem(
                    title = "✨ One UI 9 Audit",
                    icon = Icons.Default.Shield,
                    isSelected = selectedSubTab == 6,
                    onClick = { selectedSubTab = 6 },
                    testTag = "sub_tab_one_ui_9_validation"
                )
                EngineSubTabItem(
                    title = "📦 skills.sh Hub",
                    icon = Icons.Default.Extension,
                    isSelected = selectedSubTab == 7,
                    onClick = { selectedSubTab = 7 },
                    testTag = "sub_tab_skills_hub"
                )
                EngineSubTabItem(
                    title = "🤖 Edge Vibe-Coder",
                    icon = Icons.Default.Code,
                    isSelected = selectedSubTab == 8,
                    onClick = { selectedSubTab = 8 },
                    testTag = "sub_tab_edge_coding_agent"
                )
            }
        }

        // Sub-screen display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedSubTab) {
                0 -> MlcHardwareEngineScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                1 -> AnythingLlmRagScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                2 -> TermuxConsoleScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                3 -> OfflineLlmScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                4 -> ModelManagerScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    localModels = localModels
                )
                5 -> PipelinesAndAdminScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                6 -> OneUi9ValidationCenterScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onClose = { selectedSubTab = 0 }
                )
                7 -> SkillsHubScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
                8 -> EdgeCodingAgentScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun EngineSubTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val bg = if (isSelected) DeepThinkIndigo.copy(alpha = 0.35f) else Color.Transparent
    val border = if (isSelected) DeepThinkIndigoLight else Color.Transparent
    val textColor = if (isSelected) Color.White else DarkTextMuted
    val iconTint = if (isSelected) DeepThinkIndigoLight else DarkTextMuted

    Row(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                fontSize = 11.5.sp
            ),
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}
