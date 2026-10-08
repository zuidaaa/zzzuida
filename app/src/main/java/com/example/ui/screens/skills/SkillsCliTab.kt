package com.example.ui.screens.skills

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SkillsCliTab(
    cliInput: String,
    onCliInputChange: (String) -> Unit,
    onExecuteCliCommand: (String) -> Unit,
    customMarkdownInput: String,
    onCustomMarkdownChange: (String) -> Unit,
    onImportCustomMarkdown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // CLI Execution Card
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
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = DeepThinkCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "skills.sh CLI Runner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Füge ein offizielles Repository per Standard-CLI Befehl hinzu (z.B. 'npx skills add <owner>/<repo>').",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cliInput,
                        onValueChange = onCliInputChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skills_cli_input_field"),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        placeholder = {
                            Text("npx skills add vercel/nextjs-app-router", fontSize = 12.sp, color = DarkTextMuted)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepThinkCyan,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick presets
                    Text(
                        text = "Vorschläge:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "npx skills add supabase/database-architect",
                            "npx skills add huggingface/deep-research"
                        ).forEach { cmd ->
                            Surface(
                                color = DarkSurfaceHigh,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onCliInputChange(cmd) }
                            ) {
                                Text(
                                    text = cmd.substringAfter("add "),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = DeepThinkCyan,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onExecuteCliCommand(cliInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skills_cli_execute_button")
                    ) {
                        Text(
                            text = "Befehl ausführen & Skill registrieren",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Custom SKILL.md Markdown Import Card
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
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DeepThinkEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Eigenen SKILL.md Standard importieren",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Füge deine eigene YAML Frontmatter + Markdown Skill-Definition ein.",
                        fontSize = 11.sp,
                        color = DarkTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customMarkdownInput,
                        onValueChange = onCustomMarkdownChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("skills_custom_markdown_field"),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepThinkEmerald,
                            unfocusedBorderColor = DarkOutlineVariant,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onImportCustomMarkdown(customMarkdownInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("skills_custom_markdown_import_button")
                    ) {
                        Text(
                            text = "SKILL.md parsen & zur Bibliothek hinzufügen",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
