package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SkillSyncLogEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SkillSyncBanner(
    isSyncing: Boolean,
    syncLogs: List<SkillSyncLogEntity>,
    onTriggerSync: () -> Unit,
    onOpenAuditDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.8.dp, if (isSyncing) DeepThinkCyan else DarkOutlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = DeepThinkCyan
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DeepThinkEmerald)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isSyncing) "skills.sh Sync läuft..." else "skills.sh WorkManager (6h Auto-Sync)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSyncing) DeepThinkCyan else Color.White
                    )
                    val lastSyncText = syncLogs.firstOrNull()?.let {
                        val sdf = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault())
                        "Letzter Sync: ${sdf.format(Date(it.timestamp))} • ${it.skillsCheckedCount} geprüft"
                    } ?: "Hintergrund-Dienst aktiv"
                    Text(
                        text = lastSyncText,
                        fontSize = 10.sp,
                        color = DarkTextMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Zero-Trust Audit Dialog Trigger
                FilledTonalButton(
                    onClick = onOpenAuditDialog,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DeepThinkIndigo.copy(alpha = 0.4f),
                        contentColor = DeepThinkCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Audit",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Audit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Immediate manual sync trigger
                Button(
                    onClick = onTriggerSync,
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepThinkEmerald,
                        disabledContainerColor = DarkSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("skills_manual_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Color.White,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(12.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSyncing) "Sync..." else "Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
