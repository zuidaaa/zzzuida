package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*

@Composable
fun UserManualAndRoadmapScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: User Manual, 1: Roadmap

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GeminiBlueLight,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "User Manual & Roadmap",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Comprehensive feature guide & future release milestones",
                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                    )
                }
            }
        }

        // Tab Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedTab = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) GeminiBlue else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("manual_tab_btn")
            ) {
                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("User Manual", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { selectedTab = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) GeminiBlue else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("roadmap_tab_btn")
            ) {
                Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Roadmap", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (selectedTab == 0) {
            UserManualContent()
        } else {
            RoadmapContent()
        }
    }
}

@Composable
fun UserManualContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("user_manual_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ManualSectionCard(
                title = "1. Cognitive Chat & Multi-Turn Reasoning",
                description = "Engage in real-time streaming AI conversations with adjustable thinking intensity (Low, Medium, High, Extreme). Inspect transparent Chain-of-Thought reasoning steps, expand collapsible thoughts, and export traces instantly."
            )
        }
        item {
            ManualSectionCard(
                title = "2. Octopus Agent & Code Automation",
                description = "Autonomous task decomposition and agentic workflow execution. Generate, validate, and execute Android & Kotlin code snippets directly within the secure on-device sandbox."
            )
        }
        item {
            ManualSectionCard(
                title = "3. Native Language Download (German, English, Mandarin)",
                description = "Configure and download offline neural translation models. Choose between English, German, and Mandarin to run ML Kit translation entirely on-device without internet dependency."
            )
        }
        item {
            ManualSectionCard(
                title = "4. Reasoning Sandboxes & AST Validation",
                description = "Test code syntax, bracket balance, safety constraints, and mathematical expressions in isolated execution sandboxes with real-time AST parsing."
            )
        }
        item {
            ManualSectionCard(
                title = "5. Research, Tuning & Vector RAG",
                description = "Perform semantic vectorization across your workspace documents, execute RAG queries, and fine-tune system prompts for specialized engineering tasks."
            )
        }
        item {
            ManualSectionCard(
                title = "6. 3-Folder Foundation & State Persistence",
                description = "Organize projects cleanly into 3 structured folders with automated workspace snapshots ensuring zero state loss across app restarts."
            )
        }
        item {
            ManualSectionCard(
                title = "7. One UI 9 System Intelligence & Driving Co-Pilot",
                description = "Leverage Galaxy S26 Ultra NPU turbo optimization, system health auditors, and the hands-free Driving Co-Pilot voice assistant."
            )
        }
    }
}

@Composable
fun RoadmapContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("roadmap_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RoadmapItemCard(
                version = "v3.8.0 - Q4 2026 (Next Release)",
                status = "In Development",
                color = VerificationGreen,
                features = listOf(
                    "Advanced NPU Hardware Accelerator 2.0 pipeline integration with Exynos 2600.",
                    "Distributed Vector RAG memory caching across peer devices.",
                    "Expanded offline translation dictionary with regional dialects (Swiss German, Traditional Chinese)."
                )
            )
        }
        item {
            RoadmapItemCard(
                version = "v3.9.0 - Q1 2027",
                status = "Planned",
                color = GeminiCyan,
                features = listOf(
                    "Multi-Agent Collaborative Mesh (concurrent execution of specialized coding & research agents).",
                    "Encrypted Cloud Sync & Firebase Firestore multi-user workspace sharing.",
                    "WebAssembly-powered browser preview sandbox inside the Android app."
                )
            )
        }
        item {
            RoadmapItemCard(
                version = "v4.0.0 - Q2 2027",
                status = "Conceptual",
                color = GeminiPurple,
                features = listOf(
                    "On-Device Gemini Nano integration for zero-latency local intelligence.",
                    "Zero-Shot Cross-Device Code Deployment (compile & deploy directly to IoT and smartwatches).",
                    "Advanced Voice Synthesis with real-time emotional resonance tuning."
                )
            )
        }
    }
}

@Composable
fun ManualSectionCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            Text(text = description, fontSize = 13.sp, color = DarkTextMuted, lineHeight = 18.sp)
        }
    }
}

@Composable
fun RoadmapItemCard(version: String, status: String, color: Color, features: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = version, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feature, fontSize = 13.sp, color = DarkTextMuted, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
