package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.ui.theme.*

@Composable
fun SkillCard(
    skill: AgentSkill,
    onToggleActive: (Boolean) -> Unit,
    onInstallToggle: () -> Unit,
    onInspect: () -> Unit,
    onRunInPlayground: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        skill.isActive -> DeepThinkEmerald.copy(alpha = 0.8f)
        skill.isInstalled -> DeepThinkCyan.copy(alpha = 0.4f)
        else -> DarkOutlineVariant
    }

    val backgroundColor = when {
        skill.isActive -> DarkSurfaceVariant
        skill.isInstalled -> DarkSurface
        else -> DarkSurface.copy(alpha = 0.8f)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (skill.isActive) 1.2.dp else 0.8.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("skill_card_${skill.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Icon + Title + Version + Active Toggle Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceHigh)
                            .border(0.8.dp, DarkOutlineVariant, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(skill.icon, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = skill.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "v${skill.version}",
                                fontSize = 10.sp,
                                color = DarkTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = skill.ownerRepo,
                            fontSize = 11.sp,
                            color = DeepThinkCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Switch for installed/active state
                if (skill.isInstalled) {
                    Switch(
                        checked = skill.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DeepThinkEmerald,
                            uncheckedThumbColor = DarkTextMuted,
                            uncheckedTrackColor = DarkSurfaceHigh
                        ),
                        modifier = Modifier.testTag("skill_toggle_${skill.id}")
                    )
                } else {
                    OutlinedButton(
                        onClick = onInstallToggle,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(0.8.dp, DeepThinkCyan),
                        modifier = Modifier.testTag("skill_install_btn_${skill.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Install",
                            modifier = Modifier.size(12.dp),
                            tint = DeepThinkCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Holen",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = skill.description,
                fontSize = 12.sp,
                color = DarkTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Category, Security, NPU, Tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepThinkIndigo.copy(alpha = 0.3f))
                            .border(0.6.dp, DeepThinkIndigoLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skill.category.displayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepThinkIndigoLight
                        )
                    }

                    // Security Trust Level Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (skill.securityScore >= 95) DeepThinkEmerald.copy(alpha = 0.2f)
                                else DeepThinkAmber.copy(alpha = 0.2f)
                            )
                            .border(
                                0.6.dp,
                                if (skill.securityScore >= 95) DeepThinkEmerald.copy(alpha = 0.6f)
                                else DeepThinkAmber.copy(alpha = 0.6f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "🛡️ ${skill.securityScore}% Trust",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (skill.securityScore >= 95) DeepThinkEmerald else DeepThinkAmber
                        )
                    }

                    // NPU Hardware Offload Badge
                    if (skill.npuOffloaded) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DeepThinkCyan.copy(alpha = 0.2f))
                                .border(0.6.dp, DeepThinkCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ NPU",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepThinkCyan
                            )
                        }
                    }
                }

                // Action Buttons: Run in Playground & Inspect
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (skill.isActive && onRunInPlayground != null) {
                        IconButton(
                            onClick = onRunInPlayground,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(DeepThinkEmerald.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                modifier = Modifier.size(14.dp),
                                tint = DeepThinkEmerald
                            )
                        }
                    }

                    IconButton(
                        onClick = onInspect,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceHigh)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Inspect",
                            modifier = Modifier.size(14.dp),
                            tint = DeepThinkCyan
                        )
                    }
                }
            }
        }
    }
}
