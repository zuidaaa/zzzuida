package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.buttons.*
import com.example.ui.MainViewModel
import com.example.ui.components.GlassPanel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurableButtonMappingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val buttonManager = remember { viewModel.buttonActionManager }
    val mappings by buttonManager.mappings.collectAsStateWithLifecycle()
    val executionLogs by buttonManager.executionLogs.collectAsStateWithLifecycle()
    val lastEvent by buttonManager.lastTriggeredEvent.collectAsStateWithLifecycle()

    var selectedKeyForEdit by remember { mutableStateOf<DigitalButtonKey?>(null) }
    var simulatedPressType by remember { mutableStateOf(TriggerType.SINGLE_CLICK) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Button Action-Mapping & Event-Handling Engine",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Konfigurierbare Tasten: Fachliche Action-Zuordnung & technische Ereignisverarbeitung",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            OutlinedButton(
                onClick = { buttonManager.resetToDefaults() },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("reset_button_mappings")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = GeminiBlue)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Defaults", fontSize = 11.sp, color = GeminiBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Event-Handling Interactive Sandbox & Status
            item {
                GlassPanel {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Technische Ereignisverarbeitung (Live Event Sandbox)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = LuminousBlue)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Mode: ", fontSize = 11.sp, color = TextSecondary)
                                FilterChip(
                                    selected = simulatedPressType == TriggerType.SINGLE_CLICK,
                                    onClick = { simulatedPressType = TriggerType.SINGLE_CLICK },
                                    label = { Text("Tap", fontSize = 10.sp) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = simulatedPressType == TriggerType.LONG_PRESS,
                                    onClick = { simulatedPressType = TriggerType.LONG_PRESS },
                                    label = { Text("Long Press", fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Soft Keypad
                        Text("Triggern Sie digitale Tasten zur technischen Auslösung:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DigitalButtonKey.values().take(5).forEach { key ->
                                Button(
                                    onClick = {
                                        val pressDuration = if (simulatedPressType == TriggerType.LONG_PRESS) 650L else 120L
                                        val clickCount = if (simulatedPressType == TriggerType.DOUBLE_CLICK) 2 else 1
                                        buttonManager.processButtonEvent(key, pressDuration, clickCount)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("digital_key_${key.id}")
                                ) {
                                    Text(key.defaultLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Last Triggered Event Badge
                        lastEvent?.let { ev ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ev.isSuccess) VibrantGreen.copy(alpha = 0.15f) else VibrantRed.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (ev.isSuccess) VibrantGreen else VibrantRed)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Event intercepted: ${ev.key.displayName} (${ev.triggerType.displayName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Mapped Action: ${ev.mappedAction.displayName} • ${ev.statusMessage}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = "${ev.executionLatencyMs}ms",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = LuminousBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Action-Mapping Configuration (Fachliche Zuordnung)
            item {
                Text(
                    text = "Action-Mapping: Fachliche Zuordnung frei konfigurierbarer Tasten",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            items(mappings) { mapping ->
                ButtonMappingCard(
                    mapping = mapping,
                    onToggleEnabled = { enabled -> buttonManager.toggleKeyEnabled(mapping.key, enabled) },
                    onEditClick = { selectedKeyForEdit = mapping.key }
                )
            }

            // Section 3: Technical Execution Logs Stream
            item {
                GlassPanel {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Technische Ereignisprotokolle (Execution Stream)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = VibrantTeal)
                            )
                            TextButton(onClick = { buttonManager.clearLogs() }) {
                                Text("Clear Logs", fontSize = 11.sp, color = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (executionLogs.isEmpty()) {
                            Text("No key execution events recorded yet. Press buttons above to test.", fontSize = 11.sp, color = TextSecondary)
                        } else {
                            executionLogs.take(8).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (log.isSuccess) VibrantGreen else VibrantRed)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${log.key.defaultLabel} [${log.triggerType.displayName}] -> ${log.mappedAction.displayName}",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "${log.executionLatencyMs}ms",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )
                                }
                                HorizontalDivider(color = DarkOutlineVariant, thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Configure Action Assignment for selected key
    selectedKeyForEdit?.let { keyToEdit ->
        val currentMapping = mappings.find { it.key == keyToEdit }
        if (currentMapping != null) {
            ButtonMappingEditDialog(
                mapping = currentMapping,
                onDismiss = { selectedKeyForEdit = null },
                onSave = { triggerType, mappedAction, payload ->
                    buttonManager.updateMapping(keyToEdit, triggerType, mappedAction, payload)
                    selectedKeyForEdit = null
                }
            )
        }
    }
}

@Composable
private fun ButtonMappingCard(
    mapping: ButtonActionMapping,
    onToggleEnabled: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (mapping.isEnabled) DarkOutline else BorderSubtle.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mapping.key.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (mapping.isEnabled) Color.White else TextDisabled
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GeminiBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = mapping.triggerType.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeminiBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = VibrantPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mapping.mappedAction.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VibrantPurple
                    )
                }

                Text(
                    text = mapping.mappedAction.description,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Action", tint = LuminousBlue, modifier = Modifier.size(20.dp))
                }
                Switch(
                    checked = mapping.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = VibrantGreen)
                )
            }
        }
    }
}

@Composable
private fun ButtonMappingEditDialog(
    mapping: ButtonActionMapping,
    onDismiss: () -> Unit,
    onSave: (TriggerType, MappedAction, String) -> Unit
) {
    var selectedTrigger by remember { mutableStateOf(mapping.triggerType) }
    var selectedAction by remember { mutableStateOf(mapping.mappedAction) }
    var customPayload by remember { mutableStateOf(mapping.customPayload) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Action Mapping: ${mapping.key.displayName}", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Trigger Type (Ereignis-Typ):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TriggerType.values().forEach { type ->
                        FilterChip(
                            selected = selectedTrigger == type,
                            onClick = { selectedTrigger = type },
                            label = { Text(type.displayName, fontSize = 10.sp) }
                        )
                    }
                }

                Text("Select Mapped Domain Action (Fachliche Zuordnung):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 240.dp)
                ) {
                    MappedAction.values().forEach { action ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAction = action },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedAction == action) VibrantPurple.copy(alpha = 0.25f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedAction == action) VibrantPurple else DarkOutline)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(action.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(action.description, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                if (selectedAction == MappedAction.CUSTOM_MACRO_SCRIPT) {
                    OutlinedTextField(
                        value = customPayload,
                        onValueChange = { customPayload = it },
                        label = { Text("Custom Macro Payload", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedTrigger, selectedAction, customPayload) },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple)
            ) {
                Text("Save Mapping")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = AbsoluteBlack
    )
}
