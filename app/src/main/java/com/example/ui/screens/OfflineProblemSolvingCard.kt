package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProblemProgressEntity
import com.example.engine.problemsolving.ProblemCatalog
import com.example.engine.problemsolving.ProblemChallenge
import com.example.engine.problemsolving.ProblemDifficulty
import com.example.engine.problemsolving.ProblemDomain
import com.example.ui.theme.*

@Composable
fun OfflineProblemSolvingCard(
    challenges: List<ProblemChallenge>,
    progressList: List<ProblemProgressEntity>,
    onOpenChallenge: (ProblemChallenge) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDomainFilter by remember { mutableStateOf<ProblemDomain?>(null) } // null = All

    val filteredChallenges = remember(selectedDomainFilter, challenges) {
        if (selectedDomainFilter == null) challenges
        else challenges.filter { it.domain == selectedDomainFilter }
    }

    val solvedCount = remember(progressList) {
        progressList.count { it.status == "VERIFIED_SOLVED" }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_problem_solving_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeminiCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Interactive Problem-Solving Modules",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Coding • Logic • Quantum Science • Mathematical Proofs",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DarkTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (solvedCount > 0) GeminiEmerald.copy(alpha = 0.2f) else GeminiBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (solvedCount > 0) "$solvedCount / ${challenges.size} SOLVED 🏆" else "${challenges.size} CHALLENGES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (solvedCount > 0) GeminiEmerald else GeminiBlueLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Domain Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedDomainFilter == null,
                    onClick = { selectedDomainFilter = null },
                    label = { Text("All (${challenges.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeminiCyan.copy(alpha = 0.2f),
                        selectedLabelColor = GeminiCyan,
                        containerColor = DarkSurfaceVariant,
                        labelColor = DarkTextMuted
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedDomainFilter == null) GeminiCyan else DarkOutlineVariant,
                        enabled = true,
                        selected = selectedDomainFilter == null
                    )
                )

                ProblemDomain.entries.forEach { domain ->
                    val domainCount = challenges.count { it.domain == domain }
                    val isSelected = selectedDomainFilter == domain
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDomainFilter = domain },
                        label = { Text("${domain.icon} ${domain.title} ($domainCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeminiCyan.copy(alpha = 0.2f),
                            selectedLabelColor = GeminiCyan,
                            containerColor = DarkSurfaceVariant,
                            labelColor = DarkTextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) GeminiCyan else DarkOutlineVariant,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Carousel of Challenges
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredChallenges) { challenge ->
                    val progress = progressList.find { it.problemId == challenge.id }
                    val isSolved = progress?.status == "VERIFIED_SOLVED"

                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenChallenge(challenge) }
                            .testTag("challenge_item_${challenge.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                        border = BorderStroke(
                            1.dp,
                            if (isSolved) GeminiEmerald.copy(alpha = 0.6f) else DarkOutlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Top Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${challenge.domain.icon} ${challenge.domain.title.take(12)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GeminiCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
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
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
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
                                                fontSize = 8.sp
                                            )
                                        )
                                    }
                                }

                                if (isSolved) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Solved",
                                        tint = GeminiEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (progress?.isBookmarked == true) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Bookmarked",
                                        tint = GeminiCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = challenge.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                ),
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = challenge.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 2,
                                minLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action button
                            Button(
                                onClick = { onOpenChallenge(challenge) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSolved) DarkSurfaceElevated else GeminiBlue
                                ),
                                border = if (isSolved) BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.5f)) else null
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSolved) Icons.Default.Visibility else Icons.Default.Psychology,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isSolved) GeminiEmerald else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSolved) "Review Solution" else "Solve with Offline LLM",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSolved) GeminiEmerald else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
