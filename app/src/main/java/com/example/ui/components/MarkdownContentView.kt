package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCyan

@Composable
fun MarkdownContentView(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = parseContentBlocks(content)

    Column(modifier = modifier.fillMaxWidth()) {
        blocks.forEach { block ->
            when (block) {
                is ContentBlock.Code -> {
                    CodeBlockView(
                        code = block.code,
                        language = block.language
                    )
                }
                is ContentBlock.Header -> {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeminiCyan)
                            2 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GeminiBlue)
                            else -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = textColor)
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                is ContentBlock.Paragraph -> {
                    FormattedText(
                        text = block.text,
                        textColor = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is ContentBlock.ListItem -> {
                    Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp)) {
                        Text(
                            text = if (block.bullet.isNotEmpty()) block.bullet else "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeminiCyan
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        FormattedText(
                            text = block.text,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    textColor: Color
) {
    val annotatedString = buildAnnotatedString {
        var cursor = 0
        // Parse bold **text**, inline code `code`, italic *text*
        val regex = Regex("(\\*\\*|__|`|\\*)(.*?)\\1")
        val matches = regex.findAll(text)

        for (match in matches) {
            if (match.range.first > cursor) {
                withStyle(SpanStyle(color = textColor)) {
                    append(text.substring(cursor, match.range.first))
                }
            }

            val delimiter = match.groupValues[1]
            val innerText = match.groupValues[2]

            when {
                delimiter == "**" || delimiter == "__" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append(innerText)
                    }
                }
                delimiter == "`" -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0xFF1E293B),
                            color = GeminiCyan,
                            fontSize = 13.sp
                        )
                    ) {
                        append(" $innerText ")
                    }
                }
                delimiter == "*" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor.copy(alpha = 0.9f))) {
                        append(innerText)
                    }
                }
                else -> {
                    append(innerText)
                }
            }
            cursor = match.range.last + 1
        }

        if (cursor < text.length) {
            withStyle(SpanStyle(color = textColor)) {
                append(text.substring(cursor))
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            lineHeight = 22.sp,
            letterSpacing = 0.15.sp
        )
    )
}

sealed class ContentBlock {
    data class Header(val text: String, val level: Int) : ContentBlock()
    data class Code(val code: String, val language: String) : ContentBlock()
    data class ListItem(val text: String, val bullet: String) : ContentBlock()
    data class Paragraph(val text: String) : ContentBlock()
}

fun parseContentBlocks(rawText: String): List<ContentBlock> {
    val result = mutableListOf<ContentBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Check code block
        if (line.trim().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.appendLine(lines[i])
                i++
            }
            result.add(ContentBlock.Code(codeBuilder.toString().trimEnd(), lang))
            i++
            continue
        }

        // Check headers
        if (line.startsWith("### ")) {
            result.add(ContentBlock.Header(line.removePrefix("### ").trim(), 3))
            i++
            continue
        } else if (line.startsWith("## ")) {
            result.add(ContentBlock.Header(line.removePrefix("## ").trim(), 2))
            i++
            continue
        } else if (line.startsWith("# ")) {
            result.add(ContentBlock.Header(line.removePrefix("# ").trim(), 1))
            i++
            continue
        }

        // Check bullet lists
        val trimmed = line.trim()
        if (trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            val bullet = trimmed.take(2)
            result.add(ContentBlock.ListItem(trimmed.substring(2).trim(), bullet))
            i++
            continue
        } else if (trimmed.matches(Regex("^\\d+\\.\\s+.*"))) {
            val numPrefix = trimmed.substringBefore(" ")
            result.add(ContentBlock.ListItem(trimmed.substringAfter(" ").trim(), numPrefix))
            i++
            continue
        }

        // Regular paragraph
        if (line.isNotBlank()) {
            result.add(ContentBlock.Paragraph(line))
        }
        i++
    }

    return result
}
