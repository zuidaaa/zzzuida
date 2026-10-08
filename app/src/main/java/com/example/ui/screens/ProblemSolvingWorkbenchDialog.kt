package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ProblemProgressEntity
import com.example.engine.ReasoningStep
import com.example.engine.problemsolving.OfflineProblemSolvingEngine
import com.example.engine.problemsolving.ProblemCatalog
import com.example.engine.problemsolving.ProblemChallenge
import com.example.engine.problemsolving.ProblemDifficulty
import com.example.engine.problemsolving.ProblemDomain
import com.example.ui.MainViewModel
import com.example.ui.components.MarkdownContentView
import com.example.ui.components.ThoughtAccordion
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemSolvingWorkbenchDialog(
    challenge: ProblemChallenge,
    initialProgress: ProblemProgressEntity?,
    onDismiss: () -> Unit,
    onSaveProgress: (ProblemProgressEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeTabIndex by remember { mutableIntStateOf(1) } // 0: Statement, 1: Workbench, 2: Solutions
    var userDraft by remember { mutableStateOf(initialProgress?.userSolutionDraft ?: challenge.starterPremiseOrCode) }
    var unlockedHintsCount by remember { mutableIntStateOf(initialProgress?.unlockedHintsCount ?: 0) }
    var isBookmarked by remember { mutableStateOf(initialProgress?.isBookmarked ?: false) }
    var currentStatus by remember { mutableStateOf(initialProgress?.status ?: "IN_PROGRESS") }

    // LLM Guidance Execution State
    var isThinking by remember { mutableStateOf(false) }
    var liveGuidanceTitle by remember { mutableStateOf("") }
    var liveGuidanceText by remember { mutableStateOf("") }
    var liveThoughtText by remember { mutableStateOf("") }
    var liveThoughtTokens by remember { mutableIntStateOf(0) }
    var liveDurationMs by remember { mutableLongStateOf(0L) }
    var liveCurrentStep by remember { mutableStateOf<ReasoningStep?>(null) }
    var executionJob by remember { mutableStateOf<Job?>(null) }

    fun cancelExecution() {
        executionJob?.cancel()
        executionJob = null
        isThinking = false
    }

    fun saveCurrentProgress() {
        val updated = ProblemProgressEntity(
            problemId = challenge.id,
            domain = challenge.domain.name,
            status = currentStatus,
            unlockedHintsCount = unlockedHintsCount,
            userSolutionDraft = userDraft,
            userNotes = liveGuidanceText.take(500),
            lastAttemptTimestamp = System.currentTimeMillis(),
            isBookmarked = isBookmarked
        )
        onSaveProgress(updated)
    }

    Dialog(
        onDismissRequest = {
            saveCurrentProgress()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 1. Top Bar
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        saveCurrentProgress()
                                        onDismiss()
                                    },
                                    modifier = Modifier.testTag("close_workbench_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = DarkTextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${challenge.domain.icon} ${challenge.domain.title}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GeminiCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (challenge.difficulty) {
                                                        ProblemDifficulty.INTERMEDIATE -> GeminiEmerald.copy(alpha = 0.2f)
                                                        ProblemDifficulty.HARD -> GeminiAmber.copy(alpha = 0.2f)
                                                        ProblemDifficulty.OLYMPIAD -> GeminiPurple.copy(alpha = 0.25f)
                                                    }
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = challenge.difficulty.badgeLabel,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = when (challenge.difficulty) {
                                                        ProblemDifficulty.INTERMEDIATE -> GeminiEmerald
                                                        ProblemDifficulty.HARD -> GeminiAmber
                                                        ProblemDifficulty.OLYMPIAD -> GeminiPurple
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.5.sp
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = challenge.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DarkTextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        isBookmarked = !isBookmarked
                                        saveCurrentProgress()
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) GeminiCyan else DarkTextMuted
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        currentStatus = if (currentStatus == "VERIFIED_SOLVED") "IN_PROGRESS" else "VERIFIED_SOLVED"
                                        saveCurrentProgress()
                                        Toast.makeText(
                                            context,
                                            if (currentStatus == "VERIFIED_SOLVED") "Marked as Solved! 🏆" else "Status set to In Progress",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (currentStatus == "VERIFIED_SOLVED") GeminiEmerald else DarkTextMuted
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (currentStatus == "VERIFIED_SOLVED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentStatus == "VERIFIED_SOLVED") "SOLVED" else "MARK SOLVED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Navigation Tabs
                        TabRow(
                            selectedTabIndex = activeTabIndex,
                            containerColor = DarkSurface,
                            contentColor = GeminiCyan,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabIndex]),
                                    color = GeminiCyan,
                                    height = 2.5.dp
                                )
                            },
                            divider = { HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp) }
                        ) {
                            Tab(
                                selected = activeTabIndex == 0,
                                onClick = { activeTabIndex = 0 },
                                text = { Text("Problem & Constraints", fontSize = 11.5.sp, fontWeight = if (activeTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                            )
                            Tab(
                                selected = activeTabIndex == 1,
                                onClick = { activeTabIndex = 1 },
                                text = { Text("Interactive Workbench", fontSize = 11.5.sp, fontWeight = if (activeTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                            )
                            Tab(
                                selected = activeTabIndex == 2,
                                onClick = { activeTabIndex = 2 },
                                text = { Text("Optimal Proof & Code", fontSize = 11.5.sp, fontWeight = if (activeTabIndex == 2) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // 2. Tab Content View
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTabIndex) {
                        0 -> {
                            // TAB 0: Problem Statement & Constraints
                            ProblemStatementContent(
                                challenge = challenge,
                                onCopyStarter = {
                                    userDraft = challenge.starterPremiseOrCode
                                    activeTabIndex = 1
                                    Toast.makeText(context, "Loaded template into Workbench", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        1 -> {
                            // TAB 1: Interactive Workbench
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Draft Editor Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = BorderStroke(1.dp, DarkOutlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Your Proposed Solution / Draft",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                                )
                                            }
                                            Text(
                                                text = "${userDraft.length} chars",
                                                style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 10.sp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = userDraft,
                                            onValueChange = { userDraft = it },
                                            placeholder = {
                                                Text(
                                                    "Type your logic, mathematical equations, algorithm thoughts, or code snippet...",
                                                    color = DarkTextSubtle,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            minLines = 6,
                                            maxLines = 14,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("user_solution_input"),
                                            textStyle = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                color = DarkTextPrimary
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GeminiCyan,
                                                unfocusedBorderColor = DarkOutlineVariant,
                                                focusedContainerColor = DarkSurfaceVariant,
                                                unfocusedContainerColor = DarkSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                // 2. Interactive Guidance Action Buttons Grid
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "OFFLINE LLM COGNITIVE MENTORSHIP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeminiCyan,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            fontSize = 10.sp
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 1: Reveal Socratic Hint
                                        OutlinedButton(
                                            onClick = {
                                                cancelExecution()
                                                val targetHintIdx = unlockedHintsCount.coerceAtMost(challenge.hints.size - 1)
                                                unlockedHintsCount = (targetHintIdx + 1).coerceAtMost(challenge.hints.size)
                                                saveCurrentProgress()

                                                isThinking = true
                                                liveGuidanceTitle = "💡 Socratic Hint ${targetHintIdx + 1} / ${challenge.hints.size}"
                                                liveGuidanceText = ""
                                                liveThoughtText = ""
                                                val startTime = System.currentTimeMillis()

                                                executionJob = coroutineScope.launch {
                                                    OfflineProblemSolvingEngine.streamSocraticHint(
                                                        challenge = challenge,
                                                        hintIndex = targetHintIdx,
                                                        userDraft = userDraft
                                                    ).collect { chunk ->
                                                        val elapsed = System.currentTimeMillis() - startTime
                                                        liveDurationMs = elapsed
                                                        if (chunk.isThinking) {
                                                            liveThoughtText += chunk.token
                                                            liveThoughtTokens = chunk.thoughtTokenCount
                                                            liveCurrentStep = chunk.currentStep ?: liveCurrentStep
                                                        } else {
                                                            liveGuidanceText += chunk.token
                                                        }
                                                    }
                                                    isThinking = false
                                                    saveCurrentProgress()
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("reveal_hint_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                                            border = BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.6f))
                                        ) {
                                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (unlockedHintsCount >= challenge.hints.size) "Re-examine Hints" else "Hint (${unlockedHintsCount + 1}/${challenge.hints.size})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GeminiAmber
                                            )
                                        }

                                        // Button 2: Compare Approaches
                                        OutlinedButton(
                                            onClick = {
                                                cancelExecution()
                                                isThinking = true
                                                liveGuidanceTitle = "⚡ Algorithmic Approaches & Trade-off Matrix"
                                                liveGuidanceText = ""
                                                liveThoughtText = ""
                                                val startTime = System.currentTimeMillis()

                                                executionJob = coroutineScope.launch {
                                                    OfflineProblemSolvingEngine.streamApproachAnalysis(challenge).collect { chunk ->
                                                        val elapsed = System.currentTimeMillis() - startTime
                                                        liveDurationMs = elapsed
                                                        if (chunk.isThinking) {
                                                            liveThoughtText += chunk.token
                                                            liveThoughtTokens = chunk.thoughtTokenCount
                                                            liveCurrentStep = chunk.currentStep ?: liveCurrentStep
                                                        } else {
                                                            liveGuidanceText += chunk.token
                                                        }
                                                    }
                                                    isThinking = false
                                                    saveCurrentProgress()
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("explore_approaches_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                                            border = BorderStroke(1.dp, GeminiBlueLight.copy(alpha = 0.6f))
                                        ) {
                                            Icon(Icons.Default.AltRoute, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Approaches",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GeminiBlueLight
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Button 3: Critique My Attempt
                                        Button(
                                            onClick = {
                                                cancelExecution()
                                                isThinking = true
                                                liveGuidanceTitle = "🧪 Verification & Invariant Critique of Your Attempt"
                                                liveGuidanceText = ""
                                                liveThoughtText = ""
                                                val startTime = System.currentTimeMillis()

                                                executionJob = coroutineScope.launch {
                                                    OfflineProblemSolvingEngine.streamUserDraftEvaluation(
                                                        challenge = challenge,
                                                        userDraft = userDraft
                                                    ).collect { chunk ->
                                                        val elapsed = System.currentTimeMillis() - startTime
                                                        liveDurationMs = elapsed
                                                        if (chunk.isThinking) {
                                                            liveThoughtText += chunk.token
                                                            liveThoughtTokens = chunk.thoughtTokenCount
                                                            liveCurrentStep = chunk.currentStep ?: liveCurrentStep
                                                        } else {
                                                            liveGuidanceText += chunk.token
                                                        }
                                                    }
                                                    isThinking = false
                                                    saveCurrentProgress()
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("critique_attempt_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo)
                                        ) {
                                            Icon(Icons.Default.BugReport, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Critique Attempt",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        // Button 4: Deep Thinking Full Solution
                                        Button(
                                            onClick = {
                                                cancelExecution()
                                                isThinking = true
                                                liveGuidanceTitle = "🧠 Offline Deep Thinking Full Solution & Invariant Proof"
                                                liveGuidanceText = ""
                                                liveThoughtText = ""
                                                val startTime = System.currentTimeMillis()

                                                executionJob = coroutineScope.launch {
                                                    OfflineProblemSolvingEngine.streamFullDeepThinkingSolution(challenge).collect { chunk ->
                                                        val elapsed = System.currentTimeMillis() - startTime
                                                        liveDurationMs = elapsed
                                                        if (chunk.isThinking) {
                                                            liveThoughtText += chunk.token
                                                            liveThoughtTokens = chunk.thoughtTokenCount
                                                            liveCurrentStep = chunk.currentStep ?: liveCurrentStep
                                                        } else {
                                                            liveGuidanceText += chunk.token
                                                        }
                                                    }
                                                    isThinking = false
                                                    saveCurrentProgress()
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("full_proof_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan)
                                        ) {
                                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Full Deep Proof",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }

                                // 3. Active Thinking Progress Banner
                                if (isThinking) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                        border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = GeminiCyan,
                                                    strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Offline LLM Deep Thinking Active...",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                                    )
                                                    Text(
                                                        text = "$liveThoughtTokens reasoning tokens • ${liveDurationMs}ms",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = GeminiCyan, fontSize = 10.sp)
                                                    )
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { cancelExecution() },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Stop", fontSize = 10.sp, color = Color(0xFFFF5252))
                                            }
                                        }
                                    }
                                }

                                // 4. Live Thought Accordion
                                if (liveThoughtText.isNotBlank()) {
                                    ThoughtAccordion(
                                        thoughtText = liveThoughtText,
                                        isThinkingLive = isThinking,
                                        durationMs = liveDurationMs,
                                        thinkingTokens = liveThoughtTokens,
                                        currentStep = liveCurrentStep,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 5. LLM Guidance Output Card
                                if (liveGuidanceText.isNotBlank()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                        border = BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = liveGuidanceTitle,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = GeminiBlueLight
                                                    )
                                                )

                                                IconButton(
                                                    onClick = {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("LLM Guidance", liveGuidanceText))
                                                        Toast.makeText(context, "Copied explanation to clipboard", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp)
                                            Spacer(modifier = Modifier.height(10.dp))

                                            MarkdownContentView(
                                                content = liveGuidanceText,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // TAB 2: Optimal Solution & Proof
                            OptimalSolutionContent(
                                challenge = challenge,
                                onCopySolution = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Optimal Solution", challenge.optimalSolutionSnippet))
                                    Toast.makeText(context, "Copied solution to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProblemStatementContent(
    challenge: ProblemChallenge,
    onCopyStarter: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = challenge.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = GeminiCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MarkdownContentView(
                        content = challenge.description,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Constraints
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Rule, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Formal Constraints & Bounds",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    challenge.constraints.forEach { c ->
                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text("• ", color = GeminiAmber, fontWeight = FontWeight.Bold)
                            Text(c, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextPrimary))
                        }
                    }
                }
            }
        }

        // Starter Premise or Code
        if (challenge.starterPremiseOrCode.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Starter Premise / Interface",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                            Button(
                                onClick = onCopyStarter,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp)
                            ) {
                                Text("Load in Workbench", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = challenge.starterPremiseOrCode,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    color = DarkTextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Verification Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Verification Invariant Checklist",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    challenge.verificationChecklist.forEach { item ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item, style = MaterialTheme.typography.bodySmall.copy(color = DarkTextPrimary))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun OptimalSolutionContent(
    challenge: ProblemChallenge,
    onCopySolution: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Optimal Code or Mathematical Proof Snippet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Canonical Verified Implementation",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                        }

                        IconButton(onClick = onCopySolution, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GeminiEmerald, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = challenge.optimalSolutionSnippet,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                color = DarkTextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Deep Thinking Formal Proof
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Exhaustive Invariant Proof & Derivations",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = DarkOutlineVariant, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    MarkdownContentView(
                        content = challenge.formalProofOrExplanation,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
