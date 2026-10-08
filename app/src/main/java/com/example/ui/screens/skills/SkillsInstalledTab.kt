package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.ui.theme.*

@Composable
fun SkillsInstalledTab(
    installedSkills: List<AgentSkill>,
    activeSkills: List<AgentSkill>,
    onToggleActive: (AgentSkill, Boolean) -> Unit,
    onInstallToggle: (AgentSkill) -> Unit,
    onInspect: (AgentSkill) -> Unit,
    onRunInPlayground: (AgentSkill) -> Unit,
    modifier: Modifier = Modifier
) {
    if (installedSkills.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Noch keine Skills installiert.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Durchsuche den 'skills.sh Katalog' um Fähigkeiten hinzuzufügen.",
                    fontSize = 11.sp,
                    color = DarkTextMuted
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.6.dp, DeepThinkEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Aktive Agenten-Pipeline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${activeSkills.size} von ${installedSkills.size} Fähigkeiten im Kontextfenster aktiv",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }
                    }
                }
            }

            items(installedSkills, key = { it.id }) { skill ->
                SkillCard(
                    skill = skill,
                    onToggleActive = { active -> onToggleActive(skill, active) },
                    onInstallToggle = { onInstallToggle(skill) },
                    onInspect = { onInspect(skill) },
                    onRunInPlayground = { onRunInPlayground(skill) }
                )
            }
        }
    }
}
