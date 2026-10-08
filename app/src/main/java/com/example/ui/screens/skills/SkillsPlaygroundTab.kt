package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.engine.skills.SkillExecutionResult
import com.example.ui.theme.*

@Composable
fun SkillsPlaygroundTab(
    activeSkills: List<AgentSkill>,
    selectedSkillId: String?,
    onSelectSkillId: (String) -> Unit,
    prompt: String,
    onPromptChange: (String) -> Unit,
    result: SkillExecutionResult?,
    onExecuteSkill: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.8.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = DeepThinkAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agenten-Ausführungs-Playground",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Wähle einen aktiven Skill und teste, wie seine Markdown-Instruktionen und Werkzeuge auf Prompts reagieren.",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ziel-Skill:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    if (activeSkills.isEmpty()) {
                        Text(
                            text = "Keine Skills aktiv. Aktiviere zuerst einen Skill im Reiter 'Installiert'.",
                            fontSize = 11.sp,
                            color = DeepThinkAmber
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeSkills.forEach { skill ->
                                val isSelected = skill.id == selectedSkillId || (selectedSkillId == null && skill == activeSkills.first())
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectSkillId(skill.id) },
                                    label = { Text("${skill.icon} ${skill.name}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepThinkEmerald.copy(alpha = 0.3f),
                                        selectedLabelColor = DeepThinkEmerald
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Prompt für den Agenten:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = onPromptChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("skills_playground_prompt_input"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepThinkCyan,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val effectiveSkillId = selectedSkillId ?: activeSkills.firstOrNull()?.id
                    Button(
                        onClick = {
                            if (effectiveSkillId != null) {
                                onExecuteSkill(effectiveSkillId, prompt)
                            }
                        },
                        enabled = effectiveSkillId != null,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skills_playground_run_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Skill auf Prompt anwenden",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Result Card
        result?.let { res ->
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.8.dp, DeepThinkEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ausführungsergebnis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepThinkEmerald
                            )
                            Text(
                                text = "${res.executionTimeMs} ms • NPU: ${if (res.npuOffloaded) "JA" else "NEIN"}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DarkTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.output,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
