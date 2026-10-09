package com.example.ui.screens.edge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CodeHistoryEntity
import com.example.ui.theme.*

/**
 * UI Fragment: Code Generation & Refinement History Runs List.
 */
@Composable
fun EdgeCodeHistoryList(
    codeHistoryList: List<CodeHistoryEntity>,
    modifier: Modifier = Modifier
) {
    if (codeHistoryList.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Code Generation & Refinement History (${codeHistoryList.size} Runs)",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        codeHistoryList.take(5).forEach { history ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        if (history.astValidationPassed) GeminiEmerald.copy(alpha = 0.3f) else VibrantRed.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${history.language.uppercase()} • ${history.modelQuantization} • ${history.executionTarget}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GeminiBlueLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = if (history.astValidationPassed) "✓ AST Verified" else "✗ AST Error",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (history.astValidationPassed) GeminiEmerald else VibrantRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = history.prompt,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Sandbox Exit: ${history.sandboxExitCode} • ${history.sandboxExecutionMs}ms • Iteration ${history.refinementIteration}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextMuted,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
        }
    }
}
