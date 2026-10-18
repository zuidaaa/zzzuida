package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OfflineLlmResponseCacheEntity
import com.example.data.TtlPreset
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheTtlSettingsScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val policyState by viewModel.cachePolicyState.collectAsStateWithLifecycle()
    val totalCount by viewModel.offlineCacheTotalCount.collectAsStateWithLifecycle()
    val expiredCount by viewModel.offlineCacheExpiredCount.collectAsStateWithLifecycle()
    val pinnedCount by viewModel.offlineCachePinnedCount.collectAsStateWithLifecycle()
    val trainingCount by viewModel.offlineCacheTrainingCount.collectAsStateWithLifecycle()
    val cachedResponses by viewModel.offlineCachedResponses.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showApplyToExistingDialog by remember { mutableStateOf(false) }
    var pendingPreset by remember { mutableStateOf<TtlPreset?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .padding(16.dp)
            .testTag("cache_ttl_settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("ttl_settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = CircleShape,
                color = LuminousBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = LuminousBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Cache-Ablaufrichtlinie (TTL)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Room SQLite Persistenz • Automatische Bereinigung & Schutz",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Telemetrie & Speicher-Übersicht
            item {
                CacheTelemetryOverviewCard(
                    totalCount = totalCount,
                    expiredCount = expiredCount,
                    pinnedCount = pinnedCount,
                    trainingCount = trainingCount
                )
            }

            // 2. TTL Presets
            item {
                Text(
                    text = "Aufbewahrungsdauer (Time-To-Live)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtlPreset.entries.forEach { preset ->
                        val isSelected = policyState.selectedPreset == preset
                        PresetOptionCard(
                            preset = preset,
                            isSelected = isSelected,
                            onSelect = {
                                pendingPreset = preset
                                showApplyToExistingDialog = true
                            }
                        )
                    }
                }
            }

            // 3. Automatisierungs- & Sicherheitsregeln
            item {
                Text(
                    text = "Automatisierungs- & Schutzregeln",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            item {
                AutomationAndSafetyCard(
                    policyState = policyState,
                    onToggleAutoCleanup = { viewModel.setAutoCleanupOnLaunch(it) },
                    onToggleProtectTraining = { viewModel.setProtectTrainingDatasets(it) },
                    onToggleProtectPinned = { viewModel.setProtectPinnedItems(it) }
                )
            }

            // 4. Manuelle Aktionen
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.manualPurgeExpiredCache { count ->
                                Toast.makeText(
                                    context,
                                    if (count > 0) "$count abgelaufene Einträge erfolgreich bereinigt." else "Keine abgelaufenen Einträge gefunden.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_purge_expired_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abgelaufene löschen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            // Seed a sample offline LLM cache entry to easily verify behavior
                            coroutineScope.launch {
                                viewModel.offlineLlmResponseRepository.saveResponse(
                                    prompt = "Was ist der Vorteil von Unified Memory für LLM KV-Caches?",
                                    responseContent = "Unified Memory ermöglicht Zero-Copy Datentransfer zwischen CPU, NPU und GPU ohne DDR-Roundtrips, was die Time-to-First-Token Latenz drastisch minimiert.",
                                    modelId = "snapdragon-8-elite-npu",
                                    multimodalType = "TEXT",
                                    thoughtTrace = "1. Analysiere Hardware-Bus\n2. Prüfe Shared Memory Architektur\n3. Berechne Bandbreite",
                                    tokensUsed = 256,
                                    latencyMs = 180L,
                                    isTrainingCandidate = true
                                )
                                Toast.makeText(context, "Beispiel-Cache-Eintrag angelegt!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("seed_sample_cache_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test-Cache anlegen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Gecachte Einträge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gecachte Antworten (${cachedResponses.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }

            if (cachedResponses.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Keine Antworten im Offline-Cache gespeichert.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tippen Sie auf 'Test-Cache anlegen' oder führen Sie eine LLM-Abfrage durch.",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                            )
                        }
                    }
                }
            } else {
                items(cachedResponses, key = { it.cacheKey }) { entry ->
                    CachedResponseItemCard(
                        entry = entry,
                        onTogglePin = { viewModel.toggleOfflineCachePin(entry.cacheKey, entry.isPinned) },
                        onToggleTraining = { viewModel.toggleOfflineCacheTrainingCandidate(entry.cacheKey, entry.isTrainingCandidate) },
                        onDelete = { viewModel.deleteOfflineCachedResponse(entry.cacheKey) }
                    )
                }
            }
        }
    }

    // Dialog for applying TTL preset to existing rows
    if (showApplyToExistingDialog && pendingPreset != null) {
        val preset = pendingPreset!!
        AlertDialog(
            onDismissRequest = { showApplyToExistingDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(text = "Ablaufrichtlinie anwenden", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Möchten Sie '${preset.displayName}' (${preset.description}) nur für neue Antworten festlegen oder auch auf alle bestehenden nicht-gepinnten Cache-Einträge anwenden?",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setCacheTtlPreset(preset, applyToExisting = true)
                        showApplyToExistingDialog = false
                        Toast.makeText(context, "${preset.displayName} auf alle Einträge angewendet.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue)
                ) {
                    Text("Auf alle anwenden", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setCacheTtlPreset(preset, applyToExisting = false)
                        showApplyToExistingDialog = false
                        Toast.makeText(context, "${preset.displayName} für neue Einträge aktiv.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Nur für neue", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CacheTelemetryOverviewCard(
    totalCount: Int,
    expiredCount: Int,
    pinnedCount: Int,
    trainingCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cache_telemetry_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cache-Telemetrie & Status",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem(
                    label = "GESAMT",
                    value = "$totalCount",
                    color = LuminousBlue,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "ABGELAUFEN",
                    value = "$expiredCount",
                    color = if (expiredCount > 0) VibrantRed else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "GEPINNT",
                    value = "$pinnedCount",
                    color = VibrantPurple,
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricItem(
                    label = "TRAINING",
                    value = "$trainingCount",
                    color = VerificationGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color))
    }
}

@Composable
private fun PresetOptionCard(
    preset: TtlPreset,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .testTag("ttl_preset_${preset.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) LuminousBlue.copy(alpha = 0.15f) else DarkSurface,
        border = BorderStroke(1.dp, if (isSelected) LuminousBlue else BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(selectedColor = LuminousBlue, unselectedColor = TextSecondary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = preset.displayName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) LuminousBlue else Color.White
                        )
                    )
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LuminousBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "AKTIV",
                        color = LuminousBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AutomationAndSafetyCard(
    policyState: com.example.data.CachePolicyState,
    onToggleAutoCleanup: (Boolean) -> Unit,
    onToggleProtectTraining: (Boolean) -> Unit,
    onToggleProtectPinned: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automatische Bereinigung bei App-Start",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Löscht abgelaufene Einträge, die nicht geschützt sind",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                    )
                }
                Switch(
                    checked = policyState.autoCleanupOnLaunch,
                    onCheckedChange = onToggleAutoCleanup,
                    colors = SwitchDefaults.colors(checkedThumbColor = LuminousBlue, checkedTrackColor = LuminousBlue.copy(alpha = 0.3f))
                )
            }

            HorizontalDivider(color = DarkSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trainings- & Tuning-Daten schützen",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Schützt als SFT/DPO-Kandidat markierte Interaktionen vor Verfall",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                    )
                }
                Switch(
                    checked = policyState.protectTrainingDatasets,
                    onCheckedChange = onToggleProtectTraining,
                    colors = SwitchDefaults.colors(checkedThumbColor = VerificationGreen, checkedTrackColor = VerificationGreen.copy(alpha = 0.3f))
                )
            }

            HorizontalDivider(color = DarkSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gepinnte Einträge niemals löschen",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Verhindert automatische TTL- und LRU-Löschung für Favoriten",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                    )
                }
                Switch(
                    checked = policyState.protectPinnedItems,
                    onCheckedChange = onToggleProtectPinned,
                    colors = SwitchDefaults.colors(checkedThumbColor = VibrantPurple, checkedTrackColor = VibrantPurple.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun CachedResponseItemCard(
    entry: OfflineLlmResponseCacheEntity,
    onTogglePin: () -> Unit,
    onToggleTraining: () -> Unit,
    onDelete: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isExpired = entry.ttlMs > 0 && (entry.createdAt + entry.ttlMs < now) && !entry.isPinned
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cached_item_${entry.cacheKey.take(8)}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (isExpired) VibrantRed.copy(alpha = 0.4f) else BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = LuminousBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = entry.modelId,
                            color = LuminousBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isExpired) VibrantRed.copy(alpha = 0.2f) else VerificationGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isExpired) "Abgelaufen" else if (entry.isPinned) "Gepinnt (Dauerhaft)" else "Gültig",
                            color = if (isExpired) VibrantRed else VerificationGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isPinned) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Pin toggle",
                            tint = if (entry.isPinned) VibrantPurple else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleTraining,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Training candidate toggle",
                            tint = if (entry.isTrainingCandidate) VerificationGreen else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = VibrantRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prompt query
            Text(
                text = "Q: ${entry.promptQuery}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Response content excerpt
            Text(
                text = entry.responseContent,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (!entry.thoughtTrace.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🧠 Thought Trace: ${entry.thoughtTrace}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = VibrantPurple,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Erstellt: ${dateFormat.format(Date(entry.createdAt))}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                )
                Text(
                    text = "Hits: ${entry.accessCount} • ${entry.tokensUsed} Tokens",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                )
            }
        }
    }
}
