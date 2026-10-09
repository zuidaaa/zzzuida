package com.example.ui.screens.edge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.edge.AstValidationReport
import com.example.ui.theme.*

/**
 * UI Fragment: Isolated JNI / Rust Virtual Sandbox Workspace & AST Inspector Tab.
 */
@Composable
fun EdgeSandboxTab(
    workspaceFiles: Map<String, String>,
    selectedFile: String,
    onSelectFile: (String) -> Unit,
    onRunAstValidation: (fileContent: String, language: String) -> Unit,
    astReport: AstValidationReport?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Isolated JNI / Virtual Sandbox Workspace",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Files Tab Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(workspaceFiles.keys.toList()) { fileName ->
                    val isSelected = fileName == selectedFile
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GeminiBlue.copy(alpha = 0.25f) else DarkSurfaceElevated)
                            .border(1.dp, if (isSelected) GeminiBlue else DarkOutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { onSelectFile(fileName) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) GeminiBlueLight else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val currentContent = workspaceFiles[selectedFile] ?: "// File is empty"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = currentContent,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trigger AST Validation Button
            Button(
                onClick = {
                    val language = if (selectedFile.endsWith(".kt")) "kotlin" else "python"
                    onRunAstValidation(currentContent, language)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Validate AST",
                    tint = GeminiEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Run AST Pre-Execution Validator & Unit Tests",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            // Display AST Report
            astReport?.let { report ->
                Spacer(modifier = Modifier.height(10.dp))
                EdgeAstReportCard(report = report)
            }
        }
    }
}

/**
 * Dedicated Card for displaying the AST Inspection Report.
 */
@Composable
fun EdgeAstReportCard(
    report: AstValidationReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isValid) DarkSurfaceElevated else Color(0xFF331414)
        ),
        border = BorderStroke(
            1.dp,
            if (report.isValid) GeminiEmerald.copy(alpha = 0.5f) else Color(0xFFFF5252).copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (report.isValid) "AST Safety Check: PASSED" else "AST Safety Violations Detected",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (report.isValid) GeminiEmerald else Color(0xFFFF5252),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${report.astNodeCount} Nodes • ${report.checkedLanguage.uppercase()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkTextMuted,
                        fontSize = 10.sp
                    )
                )
            }
            if (report.violations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                report.violations.forEach { violation ->
                    Text(text = "• $violation", color = Color(0xFFFF8A80), fontSize = 11.sp)
                }
            }
            if (report.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                report.warnings.forEach { warning ->
                    Text(text = "⚠ $warning", color = WarningOrange, fontSize = 11.sp)
                }
            }
        }
    }
}
