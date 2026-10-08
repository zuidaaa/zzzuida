package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.ui.theme.*

@Composable
fun SkillInspectDialog(
    skill: AgentSkill,
    onDismiss: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onInstallToggle: () -> Unit,
    onDeleteSkill: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Schließen", color = Color.White)
            }
        },
        dismissButton = {
            if (skill.isInstalled) {
                IconButton(
                    onClick = onDeleteSkill,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deinstallieren",
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onInstallToggle,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Installieren", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skill.icon, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = skill.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = skill.ownerRepo,
                        fontSize = 11.sp,
                        color = DeepThinkCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                Text(
                    text = skill.description,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CLI Add Command Box
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.6.dp, DeepThinkCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cmd = "npx skills add ${skill.ownerRepo}"
                            clipboardManager.setText(AnnotatedString(cmd))
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "npx skills add ${skill.ownerRepo}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DeepThinkCyanLight
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Kopieren",
                            tint = DeepThinkCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Security & Allowed Tools
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepThinkEmerald.copy(alpha = 0.2f))
                            .border(0.6.dp, DeepThinkEmerald.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Trust: ${skill.securityScore}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepThinkEmerald
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepThinkViolet.copy(alpha = 0.2f))
                            .border(0.6.dp, DeepThinkViolet.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Checksum: ${skill.securityChecksum.take(8)}...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DeepThinkVioletLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Erlaubte Werkzeuge:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    skill.allowedTools.forEach { tool ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceHigh)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tool,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DeepThinkCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SKILL.md Anweisungen:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextMuted
                )

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.6.dp, DarkOutlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        item {
                            Text(
                                text = skill.instructionsMarkdown,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.LightGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
