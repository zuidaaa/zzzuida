package com.example.ui.screens.edge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkspaceSnapshotEntity
import com.example.ui.theme.*

/**
 * UI Fragment: KV-Cache State Snapshots & Context Memory (Subtab 2).
 */
@Composable
fun EdgeKvSnapshotsList(
    snapshots: List<WorkspaceSnapshotEntity>,
    lastRestoredSnapshotId: String?,
    onCreateSnapshot: () -> Unit,
    onTogglePin: (String) -> Unit,
    onRestoreSnapshot: (String, String) -> Unit,
    onDeleteSnapshot: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KV-Cache State Snapshots & Context Memory",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                )
                Text(
                    text = "Eliminates state loss across app lifecycle switches",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
            Button(
                onClick = onCreateSnapshot,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple)
            ) {
                Text("New Snapshot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (snapshots.isEmpty()) {
            Text(
                text = "No snapshots persisted in Room yet. Run a coding loop or click 'New Snapshot'.",
                color = DarkTextMuted,
                fontSize = 12.sp
            )
        } else {
            snapshots.forEach { snap ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (snap.isPinned) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = GeminiAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = snap.snapshotId,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = VibrantPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${snap.serializedKvCacheBytes / 1024} KB KV-Cache",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GeminiEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onTogglePin(snap.snapshotId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (snap.isPinned) Icons.Default.PushPin else Icons.Default.BookmarkBorder,
                                        contentDescription = "Pin",
                                        tint = if (snap.isPinned) GeminiAmber else DarkTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onRestoreSnapshot(snap.snapshotId, snap.summary) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "Restore Snapshot",
                                        tint = if (lastRestoredSnapshotId == snap.snapshotId) GeminiEmerald else GeminiBlueLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteSnapshot(snap.snapshotId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = VibrantRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = snap.summary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Model: ${snap.modelTag} • Checksum: 0x${snap.vectorEmbeddingChecksum} • ${snap.filesCount} files",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                fontSize = 10.sp
                            )
                        )
                        if (lastRestoredSnapshotId == snap.snapshotId) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "✓ Active Workspace Restored to this state",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GeminiEmerald,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
