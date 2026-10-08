package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ThoughtTraceExportData
import com.example.engine.ThoughtTraceExporter
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkEmeraldLight
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkIndigoLight
import com.example.ui.theme.DeepThinkViolet
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportThoughtTraceDialog(
    exportData: ThoughtTraceExportData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFormatTab by remember { mutableIntStateOf(0) } // 0: Markdown, 1: PDF
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var copiedMarkdown by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    val markdownContent = remember(exportData) {
        ThoughtTraceExporter.generateMarkdown(exportData)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, DeepThinkIndigoLight.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .testTag("export_dialog_container"),
            color = DarkSurfaceElevated,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepThinkIndigo, DeepThinkViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export Document",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Export Thought Trace",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Formatted Markdown (.md) or PDF Document (.pdf)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkTextMuted,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_export_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Overview Chip
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = DeepThinkCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exportData.modelName.take(28),
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${exportData.thinkingDurationMs}ms • ${exportData.thinkingTokens} tokens",
                                color = DeepThinkEmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (exportData.reasoningSteps.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${exportData.reasoningSteps.size} steps",
                                    color = DeepThinkIndigoLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Markdown vs PDF
                TabRow(
                    selectedTabIndex = selectedFormatTab,
                    containerColor = DarkSurface,
                    contentColor = DeepThinkIndigoLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedFormatTab]),
                            color = DeepThinkIndigoLight,
                            height = 2.5.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedFormatTab == 0,
                        onClick = { selectedFormatTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Markdown (.md)", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            }
                        },
                        modifier = Modifier.testTag("export_markdown_tab")
                    )

                    Tab(
                        selected = selectedFormatTab == 1,
                        onClick = { selectedFormatTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF Document (.pdf)", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                            }
                        },
                        modifier = Modifier.testTag("export_pdf_tab")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (selectedFormatTab == 0) {
                        // Markdown View & Actions
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkBackground)
                                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = markdownContent,
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        ThoughtTraceExporter.copyToClipboard(context, markdownContent)
                                        copiedMarkdown = true
                                        Toast.makeText(context, "Markdown copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("copy_markdown_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkIndigoLight)
                                ) {
                                    Icon(
                                        imageVector = if (copiedMarkdown) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = DeepThinkIndigoLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (copiedMarkdown) "Copied!" else "Copy Markdown",
                                        color = DeepThinkIndigoLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            val file = ThoughtTraceExporter.saveMarkdownFile(context, exportData)
                                            withContext(Dispatchers.Main) {
                                                ThoughtTraceExporter.shareDocument(
                                                    context = context,
                                                    file = file,
                                                    mimeType = "text/markdown",
                                                    title = "Share Thought Trace Markdown"
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("share_markdown_file_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkIndigo)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share .md File", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // PDF View & Actions
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0x33EF4444), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFF87171),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Native Multi-Page PDF Document", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Formatted with styled headers, thought trace cards, and validated answer", color = DarkTextMuted, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = DarkOutlineVariant)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text("Document Layout Features:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    PdfFeatureItem("📄 Standard A4 typography with high-resolution vector rendering")
                                    PdfFeatureItem("🔬 Formatted Thought Trace box with latency & token metrics")
                                    PdfFeatureItem("📋 Step-by-Step reasoning tree breakdown with verification badges")
                                    PdfFeatureItem("💡 Highlighted Final AI Response with crisp styling")
                                    PdfFeatureItem("🔒 Stored in secure app cache with zero external permissions")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isGeneratingPdf) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = DeepThinkIndigoLight,
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Rendering PDF Document...", color = DeepThinkIndigoLight, fontSize = 12.5.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isGeneratingPdf = true
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val file = ThoughtTraceExporter.generatePdfFile(context, exportData)
                                                generatedPdfFile = file
                                                withContext(Dispatchers.Main) {
                                                    isGeneratingPdf = false
                                                    ThoughtTraceExporter.shareDocument(
                                                        context = context,
                                                        file = file,
                                                        mimeType = "application/pdf",
                                                        title = "Export Thought Trace PDF"
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    isGeneratingPdf = false
                                                    Toast.makeText(context, "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("share_pdf_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate & Share PDF Document", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfFeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = DarkTextMuted,
            fontSize = 11.5.sp,
            lineHeight = 15.sp
        )
    }
}
