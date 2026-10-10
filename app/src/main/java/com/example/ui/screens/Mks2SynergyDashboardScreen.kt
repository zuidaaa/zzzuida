package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.engine.mks2.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun Mks2SynergyDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val mks2State by viewModel.mks2SynergyEngine.state.collectAsStateWithLifecycle()
    var showAddBlockDialog by remember { mutableStateOf(false) }
    var showFaultSimDialog by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Overview & Tiers, 1: Pre-Flight Gate, 2: Resilient Circuit-Breaker, 3: CRC-64 Blocks

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AbsoluteBlack)
            .padding(16.dp)
            .testTag("mks2_synergy_screen")
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = LuminousBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = LuminousBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = mks2State.projectName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${mks2State.targetHardware} • v${mks2State.version}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Quick Status Indicator
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (!mks2State.preFlightGate.isMutationLocked) VerificationGreen.copy(alpha = 0.2f) else VibrantRed.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (!mks2State.preFlightGate.isMutationLocked) VerificationGreen.copy(alpha = 0.5f) else VibrantRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (!mks2State.preFlightGate.isMutationLocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (!mks2State.preFlightGate.isMutationLocked) VerificationGreen else VibrantRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!mks2State.preFlightGate.isMutationLocked) "Gate Open" else "Mutation Locked",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (!mks2State.preFlightGate.isMutationLocked) VerificationGreen else VibrantRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.mks2SynergyEngine.runPreFlightGateCheck() },
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).testTag("mks2_verify_gate_btn")
            ) {
                Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pre-Flight Gate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { showFaultSimDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mks2State.circuitBreaker.mode == CircuitMode.CLOSED) VibrantPurple else VibrantRed
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).testTag("mks2_fault_sim_btn")
            ) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (mks2State.circuitBreaker.mode == CircuitMode.CLOSED) "Circuit Breaker" else "Fault Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.mks2SynergyEngine.executeClosedLoopCycle() },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantTeal),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).testTag("mks2_run_cycle_btn")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Closed Loop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color.Transparent,
            contentColor = LuminousBlue,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedSection < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = LuminousBlue
                    )
                }
            },
            divider = { HorizontalDivider(color = BorderSubtle) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            listOf("Speicherhierarchie", "Pre-Flight Gate", "Resilient Loop", "KV-Blöcke & CRC-64").forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedSection == index) LuminousBlue else TextSecondary,
                            fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Main Content Area
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedSection) {
                0 -> {
                    // TAB 0: Speicherhierarchie & Overview
                    item { KeyMetricsSummaryCard(mks2State) }
                    item { MemoryTiersSection(mks2State) }
                    item { CoreEnginesOverview() }
                }
                1 -> {
                    // TAB 1: Phase 1 Pre-Flight Gate
                    item { PreFlightGateDetailedCard(mks2State, viewModel) }
                }
                2 -> {
                    // TAB 2: Phase 2 Resilient Loop & Circuit-Breaker
                    item { ResilientLoopDetailedCard(mks2State, viewModel) }
                }
                3 -> {
                    // TAB 3: KV-Cache Blocks & CRC-64 Checksums
                    item {
                        KvBlocksAndChecksumSection(
                            mks2State = mks2State,
                            onAddBlock = { showAddBlockDialog = true },
                            onVerifyCrc = { viewModel.mks2SynergyEngine.verifyKvBlockCrc64(it) },
                            onToggleLock = { viewModel.mks2SynergyEngine.toggleReconstructionLock() },
                            onSimTrim = { viewModel.mks2SynergyEngine.simulateOnTrimMemory("TRIM_MEMORY_RUNNING_CRITICAL") }
                        )
                    }
                }
            }

            // Live Closed-Loop Console Log (Always visible at bottom)
            item {
                LiveConsoleLogCard(mks2State)
            }
        }
    }

    // Add Block Dialog
    if (showAddBlockDialog) {
        AddKvBlockDialog(
            onDismiss = { showAddBlockDialog = false },
            onAdd = { tokens, importance ->
                viewModel.mks2SynergyEngine.placeNewTokenBlock(tokens, importance)
                showAddBlockDialog = false
            }
        )
    }

    // Fault Simulation Dialog
    if (showFaultSimDialog) {
        FaultSimulationDialog(
            circuitMode = mks2State.circuitBreaker.mode,
            onDismiss = { showFaultSimDialog = false },
            onFault = { comp ->
                viewModel.mks2SynergyEngine.triggerCircuitBreakerFault(comp)
                showFaultSimDialog = false
            },
            onReset = {
                viewModel.mks2SynergyEngine.resetCircuitBreaker()
                showFaultSimDialog = false
            }
        )
    }
}

// =========================================================================
// SECTION COMPONENTS
// =========================================================================

@Composable
private fun KeyMetricsSummaryCard(mks2State: Mks2SynergyState) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("mks2_metrics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "TTFT SPEEDUP", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Text(text = mks2State.totalTtftReduction, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = VerificationGreen))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "KONTEXT-LÄNGE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Text(text = mks2State.contextExtensionFactor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminousBlue))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "ENERGIE-ERSPARNIS", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Text(text = mks2State.energySavings, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = LuminousYellow))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SICHERHEITS-STUFE", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Text(text = mks2State.securityStatus, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = VibrantPurple))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DarkSurfaceVariant, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(VerificationGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = mks2State.activeEngine, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp))
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = VerificationGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Score: ${mks2State.systemHealthScore}%",
                        style = MaterialTheme.typography.labelSmall.copy(color = VerificationGreen, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryTiersSection(mks2State: Mks2SynergyState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Ebene 1: 4-Stufige Speicherhierarchie",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )

        mks2State.tierTelemetries.forEach { telemetry ->
            val tierColor = when (telemetry.tier) {
                MemoryTier.T0_NPU -> LuminousBlue
                MemoryTier.T1_DRAM -> VibrantPurple
                MemoryTier.T2_UFS4 -> VibrantTeal
                MemoryTier.T3_CLOUD -> LuminousYellow
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, tierColor.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = tierColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = telemetry.tier.tierId,
                                    color = tierColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = telemetry.tier.tierName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(
                                    text = "Governor: ${telemetry.tier.governor} • Latency: ${telemetry.tier.latencyDescription}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                )
                            }
                        }

                        Text(
                            text = "${(telemetry.fillRatio * 100).toInt()}%",
                            color = tierColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { telemetry.fillRatio },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = tierColor,
                        trackColor = DarkSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Kapazität: ${telemetry.tier.capacityDescription}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                        )
                        Text(
                            text = "${telemetry.allocatedTokens} Tokens",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreFlightGateDetailedCard(mks2State: Mks2SynergyState, viewModel: MainViewModel) {
    val gate = mks2State.preFlightGate

    Card(
        modifier = Modifier.fillMaxWidth().testTag("pre_flight_gate_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (!gate.isMutationLocked) VerificationGreen.copy(alpha = 0.5f) else VibrantRed.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phase 1: Pre-Flight Gate",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Harte Trennung: Lese-/Prüfphase vor Mutation",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!gate.isMutationLocked) VerificationGreen.copy(alpha = 0.25f) else VibrantRed.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = if (!gate.isMutationLocked) "SCHREIBRECHTE FREI" else "GESPERRT (READ-ONLY)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (!gate.isMutationLocked) VerificationGreen else VibrantRed,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Schreib- und Ausführungsrechte bleiben strikt gesperrt, bis Systemzustand, Hardware-Abhängigkeiten und Wissensspeicher fehlerfrei verifiziert sind.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = DarkSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Systemzustand
            Text(text = "1. Systemzustand (Thermal, Battery, RAM)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = LuminousBlue))
            Spacer(modifier = Modifier.height(6.dp))
            gate.systemStateChecks.forEach { check -> GateCheckRow(check) }

            Spacer(modifier = Modifier.height(12.dp))
            // 2. Hardware-Abhängigkeiten
            Text(text = "2. Hardware-Abhängigkeiten (Snapdragon 8 Elite, UFS 4.0)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = VibrantPurple))
            Spacer(modifier = Modifier.height(6.dp))
            gate.dependencyChecks.forEach { check -> GateCheckRow(check) }

            Spacer(modifier = Modifier.height(12.dp))
            // 3. Wissensspeicher & CRC-64
            Text(text = "3. Wissensspeicher & Integrität (SQLite, Ring Buffers, CRC-64)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = VibrantTeal))
            Spacer(modifier = Modifier.height(6.dp))
            gate.knowledgeStoreChecks.forEach { check -> GateCheckRow(check) }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.mks2SynergyEngine.runPreFlightGateCheck() },
                modifier = Modifier.fillMaxWidth().testTag("reverify_gate_action_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pre-Flight Gate Jetzt Neu Validieren", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GateCheckRow(check: GateCheckResult) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (check.passed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (check.passed) VerificationGreen else VibrantRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = check.name, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Medium, fontSize = 11.sp))
                    Text(text = check.details, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp))
                }
            }
            Text(text = "${check.latencyMs}ms", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
        }
    }
}

@Composable
private fun ResilientLoopDetailedCard(mks2State: Mks2SynergyState, viewModel: MainViewModel) {
    val cb = mks2State.circuitBreaker

    Card(
        modifier = Modifier.fillMaxWidth().testTag("circuit_breaker_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            1.dp,
            when (cb.mode) {
                CircuitMode.CLOSED -> VerificationGreen.copy(alpha = 0.5f)
                CircuitMode.HALF_OPEN -> LuminousYellow.copy(alpha = 0.5f)
                CircuitMode.OPEN -> VibrantRed.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Phase 2: Resilient Loop & Fallback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "Circuit-Breaker-Muster mit Fehler-Isolation",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (cb.mode) {
                        CircuitMode.CLOSED -> VerificationGreen.copy(alpha = 0.25f)
                        CircuitMode.HALF_OPEN -> LuminousYellow.copy(alpha = 0.25f)
                        CircuitMode.OPEN -> VibrantRed.copy(alpha = 0.25f)
                    }
                ) {
                    Text(
                        text = "MODE: ${cb.mode.name}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = when (cb.mode) {
                                CircuitMode.CLOSED -> VerificationGreen
                                CircuitMode.HALF_OPEN -> LuminousYellow
                                CircuitMode.OPEN -> VibrantRed
                            },
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Aktiver Ausweichpfad (Fallback Route):", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = cb.activeFallbackPath, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Persistenter Störungs- & Erkenntnisspeicher:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))

            if (cb.isolatedErrors.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Keine aktiven Störungen isoliert. Alle Primär-Engines operieren nominal.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                cb.isolatedErrors.forEach { inc ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, if (!inc.resolved) VibrantRed.copy(alpha = 0.4f) else VerificationGreen.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "${inc.id} • ${inc.component}", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                Text(text = inc.timestamp, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = inc.errorMessage, style = MaterialTheme.typography.bodySmall.copy(color = VibrantRed, fontSize = 10.sp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "➜ ${inc.fallbackEngaged}", style = MaterialTheme.typography.labelSmall.copy(color = LuminousBlue, fontSize = 10.sp))
                        }
                    }
                }
            }

            if (cb.mode != CircuitMode.CLOSED) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.mks2SynergyEngine.resetCircuitBreaker() },
                    colors = ButtonDefaults.buttonColors(containerColor = VerificationGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Canary-Probe & Circuit-Breaker Zurücksetzen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun KvBlocksAndChecksumSection(
    mks2State: Mks2SynergyState,
    onAddBlock: () -> Unit,
    onVerifyCrc: (String) -> Unit,
    onToggleLock: () -> Unit,
    onSimTrim: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // NPUGen Lock & onTrimMemory Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, if (mks2State.npuGenLock.isReconstructionLockAcquired) VibrantTeal else DarkSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "NPUGen Rekonstruktions-Lock", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        Text(text = "Schützt Rekonstruktion vor onTrimMemory-Korruption", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    }

                    Switch(
                        checked = mks2State.npuGenLock.isReconstructionLockAcquired,
                        onCheckedChange = { onToggleLock() },
                        colors = SwitchDefaults.colors(checkedThumbColor = VibrantTeal, checkedTrackColor = VibrantTeal.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Abgefangene Trim-Events: ${mks2State.npuGenLock.trimMemoryInterceptions}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                    Text(text = "INT4 Effizienz: ${mks2State.npuGenLock.int4AnchorEfficiency}%", style = MaterialTheme.typography.labelSmall.copy(color = VibrantTeal, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSimTrim() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simuliere Android onTrimMemory (OS-Speicherdruck)", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // KV Blocks List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktive KV-Cache Blöcke & CRC-64 Checksummen",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
            )

            Button(
                onClick = onAddBlock,
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Neu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // KV Blocks
        mks2State.kvBlocks.forEach { block ->
            val tierColor = when (block.assignedTier) {
                MemoryTier.T0_NPU -> LuminousBlue
                MemoryTier.T1_DRAM -> VibrantPurple
                MemoryTier.T2_UFS4 -> VibrantTeal
                MemoryTier.T3_CLOUD -> LuminousYellow
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = block.id, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = tierColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${block.assignedTier.tierId} (${if (block.isCompressed) "Low-Rank" else "Exact"})",
                                    color = tierColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onVerifyCrc(block.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Verify CRC-64", tint = VerificationGreen, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tokens: ${block.tokenCount} • Importance: ${String.format("%.2f", block.importance)}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
                        Text(text = "CRC-64: 0x${block.crc64Checksum}", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 10.sp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreEnginesOverview() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Ebene 2: Kern-Synergie-Engines",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
        )

        EngineInfoCard(
            title = "Engine A: mzCache",
            role = "Elastische Speicherverwaltung unter Multitasking-Druck via onTrimMemory",
            impact = "2,1–5,5× Reduktion der Time-to-First-Token durch Backward-Out Eviction",
            color = LuminousBlue
        )
        EngineInfoCard(
            title = "Engine B: KVSwap",
            role = "Disk-backed KV-Cache mit predictive Prefetching auf UFS 4.0",
            impact = "Ermöglicht Long-Context-Inferenz (>32K Token) auf Geräten mit <8 GB RAM",
            color = VibrantPurple
        )
        EngineInfoCard(
            title = "Engine C: NPUGen",
            role = "NPU-Rekonstruktion fehlender KV-Caches aus INT4-Ankern",
            impact = "Vermeidet UFS-I/O-Asymmetrie und spart 34% Energie ohne DRAM-Roundtrip",
            color = VerificationGreen
        )
        EngineInfoCard(
            title = "Agent.xpu & FlexServe TEE",
            role = "Heterogene iGPU/NPU-Koordination & ARM TrustZone Sicherheitsisolation",
            impact = "1,2–4,9× proaktiver Durchsatz & sichere AES-GCM Cloud-Brücke",
            color = LuminousYellow
        )
    }
}

@Composable
private fun EngineInfoCard(title: String, role: String, impact: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = role, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "➜ $impact", style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Medium, fontSize = 10.sp))
        }
    }
}

@Composable
private fun LiveConsoleLogCard(mks2State: Mks2SynergyState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Closed-Loop Telemetrie-Konsole",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    text = "SNAPDRAGON 8 ELITE",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DarkSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            if (mks2State.logTrace.isEmpty()) {
                Text(text = "Initialisiere MKS² Telemetrie...", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            } else {
                mks2State.logTrace.take(8).forEach { log ->
                    val textColor = when {
                        log.contains("CIRCUIT-BREAKER") || log.contains("FAILED") -> VibrantRed
                        log.contains("PRE-FLIGHT") || log.contains("PASSED") -> VerificationGreen
                        log.contains("NPUGen") -> VibrantTeal
                        log.contains("mzCache") -> LuminousBlue
                        log.contains("KVSwap") -> VibrantPurple
                        else -> Color.White
                    }

                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// DIALOGS
// =========================================================================

@Composable
private fun AddKvBlockDialog(
    onDismiss: () -> Unit,
    onAdd: (tokens: Int, importance: Float) -> Unit
) {
    var tokensText by remember { mutableStateOf("8192") }
    var importanceVal by remember { mutableFloatStateOf(0.85f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(text = "Neuen KV-Cache Block platzieren", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = tokensText,
                    onValueChange = { tokensText = it },
                    label = { Text("Token Anzahl") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = LuminousBlue,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                Text(
                    text = "Importance Score: ${String.format("%.2f", importanceVal)}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Slider(
                    value = importanceVal,
                    onValueChange = { importanceVal = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = LuminousBlue, activeTrackColor = LuminousBlue)
                )

                Text(
                    text = when {
                        importanceVal >= 0.7f -> "Ziel: T0 NPU Shared Memory (Exact Tier)"
                        importanceVal >= 0.3f -> "Ziel: T1 System DRAM (Low-Rank Komprimiert)"
                        importanceVal >= 0.1f -> "Ziel: T2 UFS 4.0 Flash (Disk-Aware KVSwap)"
                        else -> "Ziel: T3 Cloud / Remote TEE (AES-GCM Verschlüsselt)"
                    },
                    color = LuminousBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tokens = tokensText.toIntOrNull() ?: 4096
                    onAdd(tokens, importanceVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue)
            ) {
                Text("Platzieren", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun FaultSimulationDialog(
    circuitMode: CircuitMode,
    onDismiss: () -> Unit,
    onFault: (component: String) -> Unit,
    onReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(text = "Circuit-Breaker Störungs-Simulation", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Wählen Sie eine Systemkomponente, um eine Störung zu simulieren und die automatische Isolation sowie den resilienten Ausweichpfad zu testen:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { onFault("NPUGen") },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NPUGen Latenz-Spike (>150ms) simulieren", color = VibrantRed, fontSize = 11.sp)
                }

                Button(
                    onClick = { onFault("UFS 4.0") },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("UFS 4.0 I/O Queue Stau simulieren", color = VibrantRed, fontSize = 11.sp)
                }

                Button(
                    onClick = { onFault("FlexServe TEE") },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantRed.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("FlexServe TEE Cloud Handshake Timeout", color = VibrantRed, fontSize = 11.sp)
                }

                if (circuitMode != CircuitMode.CLOSED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onReset,
                        colors = ButtonDefaults.buttonColors(containerColor = VerificationGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Circuit-Breaker Zurücksetzen (Canary Pass)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Schließen", color = TextSecondary)
            }
        }
    )
}
