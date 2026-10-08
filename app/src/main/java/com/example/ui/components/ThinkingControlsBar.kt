package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.EngineMode
import com.example.engine.GeminiApiClient
import com.example.engine.ThinkingLevel
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkEmerald
import com.example.ui.theme.DeepThinkEmeraldLight
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkIndigoLight
import com.example.ui.theme.DeepThinkViolet

@Composable
fun ThinkingControlsBar(
    selectedLevel: ThinkingLevel,
    engineMode: EngineMode,
    searchGroundingEnabled: Boolean,
    activeModelName: String,
    selectedGeminiModel: String = GeminiApiClient.MODEL_FLASH,
    selectedChatbotRole: String = "General Assistant",
    autoModelRoutingEnabled: Boolean = true,
    lastRoutingBadge: String = "",
    onModelSelected: (String) -> Unit = {},
    onEnableAutoRouting: () -> Unit = {},
    onRoleSelected: (String) -> Unit = {},
    onOpenCustomRoleDialog: () -> Unit = {},
    onLevelSelected: (ThinkingLevel) -> Unit,
    onToggleEngineMode: () -> Unit,
    onToggleSearchGrounding: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // 1. Gemini Models Selector: Auto Dynamic, Lite, Flash, Pro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MODEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepThinkIndigoLight,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Auto Dynamic Task Complexity Switcher
                    ModelPill(
                        label = "⚡ Auto (Dynamic)",
                        subLabel = if (autoModelRoutingEnabled) "Task Complexity" else "Off",
                        isSelected = autoModelRoutingEnabled,
                        selectedColor = Color(0xFFFFB703),
                        onClick = onEnableAutoRouting,
                        testTag = "model_select_auto_dynamic"
                    )

                    // Fast: gemini-3.1-flash-lite
                    val isLite = !autoModelRoutingEnabled && selectedGeminiModel.contains("lite", ignoreCase = true)
                    ModelPill(
                        label = "⚡ Fast (Lite)",
                        subLabel = "Fast Tasks",
                        isSelected = isLite,
                        selectedColor = DeepThinkCyan,
                        onClick = { onModelSelected(GeminiApiClient.MODEL_FLASH_LITE) },
                        testTag = "model_select_flash_lite"
                    )

                    // General: gemini-3.5-flash
                    val isFlash = !autoModelRoutingEnabled && !isLite && !selectedGeminiModel.contains("pro", ignoreCase = true)
                    ModelPill(
                        label = "✨ General (Flash)",
                        subLabel = "General Tasks",
                        isSelected = isFlash,
                        selectedColor = DeepThinkIndigoLight,
                        onClick = { onModelSelected(GeminiApiClient.MODEL_FLASH) },
                        testTag = "model_select_flash"
                    )

                    // Complex: gemini-3.1-pro-preview
                    val isPro = !autoModelRoutingEnabled && selectedGeminiModel.contains("pro", ignoreCase = true)
                    ModelPill(
                        label = "🧠 Complex (Pro)",
                        subLabel = "Deep Reasoning",
                        isSelected = isPro,
                        selectedColor = DeepThinkViolet,
                        onClick = { onModelSelected(GeminiApiClient.MODEL_PRO) },
                        testTag = "model_select_pro"
                    )
                }
            }

            // 2. Chatbot Role / Persona Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepThinkIndigoLight,
                        letterSpacing = 0.8.sp,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(end = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val roles = listOf(
                        Triple("General Assistant", "Assistant", Icons.Default.AutoAwesome),
                        Triple("Coding Architect", "Architect", Icons.Default.Code),
                        Triple("Research Scientist", "Scientist", Icons.Default.Science),
                        Triple("Fast Summarizer", "Summarizer", Icons.Default.ShortText),
                        Triple("Creative Writer", "Creative", Icons.Default.Psychology)
                    )

                    roles.forEach { (roleFullName, shortLabel, icon) ->
                        val isSelected = selectedChatbotRole == roleFullName
                        val roleBg by animateColorAsState(
                            targetValue = if (isSelected) DeepThinkIndigo.copy(alpha = 0.35f) else DarkSurfaceVariant,
                            animationSpec = tween(200),
                            label = "role_bg"
                        )
                        val roleBorder by animateColorAsState(
                            targetValue = if (isSelected) DeepThinkIndigoLight else DarkOutlineVariant,
                            animationSpec = tween(200),
                            label = "role_border"
                        )

                        Row(
                            modifier = Modifier
                                .heightIn(min = 34.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(roleBg)
                                .border(1.dp, roleBorder, RoundedCornerShape(16.dp))
                                .clickable { onRoleSelected(roleFullName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("role_pill_${shortLabel.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = shortLabel,
                                tint = if (isSelected) Color.White else DarkTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = shortLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Custom Role Button
                    Row(
                        modifier = Modifier
                            .heightIn(min = 34.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(16.dp))
                            .clickable { onOpenCustomRoleDialog() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("role_pill_custom"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Custom Role",
                            tint = DeepThinkCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Custom...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = DeepThinkCyan,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // 3. Main Level & Feature Pills Row (Engine Mode, Search Grounding, Thinking Level)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Mode Toggle (Offline vs Online)
                val engineBgColor by animateColorAsState(
                    targetValue = if (engineMode.isOffline) Color(0xFF0F261C) else DeepThinkIndigo.copy(alpha = 0.16f),
                    animationSpec = tween(250),
                    label = "engine_bg"
                )
                val engineBorderColor by animateColorAsState(
                    targetValue = if (engineMode.isOffline) DeepThinkEmerald.copy(alpha = 0.6f) else DeepThinkIndigo.copy(alpha = 0.6f),
                    animationSpec = tween(250),
                    label = "engine_border"
                )

                Row(
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(engineBgColor)
                        .border(1.dp, engineBorderColor, RoundedCornerShape(18.dp))
                        .clickable(
                            role = Role.Switch,
                            onClickLabel = "Toggle Offline/Online Mode",
                            onClick = onToggleEngineMode
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("engine_mode_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (engineMode.isOffline) Icons.Default.CloudOff else Icons.Default.Cloud,
                        contentDescription = "Engine Mode Status",
                        tint = if (engineMode.isOffline) DeepThinkEmeraldLight else DeepThinkIndigoLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (engineMode.isOffline) "100% OFFLINE" else "ONLINE GEMINI",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (engineMode.isOffline) DeepThinkEmeraldLight else DeepThinkIndigoLight,
                            fontSize = 10.5.sp
                        )
                    )
                }

                // Google Search Grounding Pill
                val searchBgColor by animateColorAsState(
                    targetValue = if (searchGroundingEnabled) DeepThinkCyan.copy(alpha = 0.22f) else DarkSurfaceVariant,
                    animationSpec = tween(250),
                    label = "search_bg"
                )
                val searchBorderColor by animateColorAsState(
                    targetValue = if (searchGroundingEnabled) DeepThinkCyan else DarkOutlineVariant,
                    animationSpec = tween(250),
                    label = "search_border"
                )

                Row(
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(searchBgColor)
                        .border(1.dp, searchBorderColor, RoundedCornerShape(18.dp))
                        .clickable(
                            role = Role.Switch,
                            onClickLabel = "Toggle Search Grounding",
                            onClick = onToggleSearchGrounding
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("search_grounding_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Search Grounding Status",
                        tint = if (searchGroundingEnabled) DeepThinkCyan else DarkTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (searchGroundingEnabled) "GROUNDED" else "SEARCH OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (searchGroundingEnabled) DeepThinkCyan else DarkTextMuted,
                            fontSize = 10.5.sp
                        )
                    )
                }

                // Thinking Levels
                ThinkingLevel.values().forEach { level ->
                    val isSelected = level == selectedLevel
                    val levelBg by animateColorAsState(
                        targetValue = if (isSelected) DeepThinkIndigo.copy(alpha = 0.28f) else DarkSurfaceVariant,
                        animationSpec = tween(200),
                        label = "level_bg"
                    )
                    val levelBorder by animateColorAsState(
                        targetValue = if (isSelected) DeepThinkIndigo else DarkOutlineVariant,
                        animationSpec = tween(200),
                        label = "level_border"
                    )

                    Box(
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(levelBg)
                            .border(1.dp, levelBorder, RoundedCornerShape(18.dp))
                            .clickable(
                                role = Role.RadioButton,
                                onClickLabel = "Select ${level.label}",
                                onClick = { onLevelSelected(level) }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("thinking_level_${level.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = level.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = level.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DarkTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelPill(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) selectedColor.copy(alpha = 0.25f) else DarkSurfaceVariant,
        animationSpec = tween(200),
        label = "model_bg"
    )
    val border by animateColorAsState(
        targetValue = if (isSelected) selectedColor else DarkOutlineVariant,
        animationSpec = tween(200),
        label = "model_border"
    )

    Row(
        modifier = Modifier
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DarkTextMuted,
                fontSize = 11.5.sp
            )
        )
    }
}
