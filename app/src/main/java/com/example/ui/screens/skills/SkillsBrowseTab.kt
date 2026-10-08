package com.example.ui.screens.skills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.skills.AgentSkill
import com.example.ui.theme.DarkTextMuted

@Composable
fun SkillsBrowseTab(
    filteredSkills: List<AgentSkill>,
    onToggleActive: (AgentSkill, Boolean) -> Unit,
    onInstallToggle: (AgentSkill) -> Unit,
    onInspect: (AgentSkill) -> Unit,
    onRunInPlayground: (AgentSkill) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filteredSkills.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Keine Skills für diese Kriterien gefunden.",
                fontSize = 13.sp,
                color = DarkTextMuted
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(filteredSkills, key = { it.id }) { skill ->
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
