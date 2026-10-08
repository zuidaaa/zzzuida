package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SkillSyncLogEntity
import com.example.engine.skills.SkillsAuditSummary
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SkillAuditDialog(
    auditSummary: SkillsAuditSummary,
    syncLogs: List<SkillSyncLogEntity>,
    isSyncing: Boolean,
    onDismiss: () -> Unit,
    onTriggerSync: () -> Unit
) {
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
            Button(
                onClick = onTriggerSync,
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Jetzt Sync ausführen", fontSize = 11.sp, color = Color.White)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️ skills.sh Audit & Sync Historie", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // KPI Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.6.dp, DeepThinkEmerald.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sicherheit", fontSize = 9.sp, color = DarkTextMuted)
                            Text("${auditSummary.averageSecurityScore}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepThinkEmerald)
                            Text("Knox Zero-Trust", fontSize = 8.sp, color = DeepThinkEmeraldLight)
                        }
                    }

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.6.dp, DeepThinkCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NPU Offload", fontSize = 9.sp, color = DarkTextMuted)
                            Text("${auditSummary.npuAcceleratedCount}/${auditSummary.totalSkills}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepThinkCyan)
                            Text("92 TOPS NPU", fontSize = 8.sp, color = DeepThinkCyan)
                        }
                    }

                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.6.dp, DeepThinkViolet.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Room SQLite", fontSize = 9.sp, color = DarkTextMuted)
                            Text("${auditSummary.installedCount} Inst.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepThinkVioletLight)
                            Text("${auditSummary.activeCount} Aktiv", fontSize = 8.sp, color = DeepThinkEmerald)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Room SQLite Synchronisationsprotokoll:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.6.dp, DarkOutlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (syncLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Noch keine Synchronisations-Logs vorhanden.", fontSize = 11.sp, color = DarkTextMuted)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(syncLogs) { log ->
                                Surface(
                                    color = DarkSurfaceHigh,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${log.triggerType} (${log.skillsCheckedCount} geprüft)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (log.status == "SUCCESS") DeepThinkEmerald else DeepThinkAmber
                                            )
                                            val sdf = SimpleDateFormat("HH:mm:ss dd.MM.", Locale.getDefault())
                                            Text(
                                                text = sdf.format(Date(log.timestamp)),
                                                fontSize = 9.sp,
                                                color = DarkTextMuted
                                            )
                                        }
                                        Text(
                                            text = log.summaryMessage,
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}
