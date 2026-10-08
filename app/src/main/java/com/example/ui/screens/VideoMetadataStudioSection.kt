package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleVideo
import com.example.engine.AccountTagsUi
import com.example.engine.ChaptersUi
import com.example.engine.DescriptionUi
import com.example.engine.ErrorUi
import com.example.engine.HashtagsUi
import com.example.engine.LinksUi
import com.example.engine.ThumbnailsUi
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueLight
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.WarningOrange

private val SAMPLE_VIDEOS = listOf(
    SampleVideo(
        title = "Gemini 3.7 Vision & Multimodal Keynote",
        category = "Keynote",
        durationFormatted = "07:45",
        resolution = "4K HDR (60 FPS)",
        simulatedUriString = "content://media/external/video/media/1001",
        descriptionContext = "Complete breakdown of Gemini 3.7 Deep Thinking, sub-second multimodal tokenization, and live video analysis workflows."
    ),
    SampleVideo(
        title = "Samsung S26 Ultra NPU Turbo Deep Dive",
        category = "Hardware",
        durationFormatted = "05:20",
        resolution = "1080p (60 FPS)",
        simulatedUriString = "content://media/external/video/media/1002",
        descriptionContext = "Hardware performance benchmarks, Exynos 2600 10-core CPU governor, and zero-token mobile NPU execution."
    ),
    SampleVideo(
        title = "Fullstack Android Jetpack Compose Tutorial",
        category = "Tutorial",
        durationFormatted = "12:10",
        resolution = "1080p (30 FPS)",
        simulatedUriString = "content://media/external/video/media/1003",
        descriptionContext = "Step-by-step masterclass building production-grade Android apps with Material 3, Room local database, and Gemini Flash."
    ),
    SampleVideo(
        title = "Autonomous LLM Agents & MCP Hub",
        category = "Architecture",
        durationFormatted = "08:30",
        resolution = "4K (60 FPS)",
        simulatedUriString = "content://media/external/video/media/1004",
        descriptionContext = "Connecting live Hugging Face MCP tools, JSON-RPC endpoints, and decentralized reasoning pipelines."
    )
)

/**
 * Composable providing full UI and controls for AI Video Metadata generation using Gemini Flash.
 */
@Composable
fun VideoMetadataStudioSection(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Zero-permission Photo & Video Picker launcher compliant with Google Play Policy
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment ?: "Device Video"
            viewModel.selectVideo(
                uri = uri,
                title = "Selected Video ($fileName)",
                duration = "06:30",
                resolution = "1080p",
                category = "User Media",
                defaultContext = "User imported video stream for comprehensive Gemini Flash analysis."
            )
            Toast.makeText(context, "Video loaded! Ready for Gemini Flash analysis.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("video_metadata_hero_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gemini Flash Video Intelligence",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                            )
                            Text(
                                text = "Sub-second multimodal metadata extraction",
                                style = MaterialTheme.typography.bodySmall.copy(color = GeminiBlueLight, fontSize = 11.sp)
                            )
                        }
                    }

                    // Select from device button
                    Button(
                        onClick = {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlueLight.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("pick_video_from_device_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Video", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sample Video Selection Carousel
                Text(
                    text = "Select Video for AI Analysis:",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SAMPLE_VIDEOS.forEach { sample ->
                        val isSelected = uiState.selectedVideoTitle == sample.title
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) GeminiBlue.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) GeminiBlueLight else DarkOutlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.selectVideo(
                                        uri = Uri.parse(sample.simulatedUriString),
                                        title = sample.title,
                                        duration = sample.durationFormatted,
                                        resolution = sample.resolution,
                                        category = sample.category,
                                        defaultContext = sample.descriptionContext
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else DarkTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${sample.category} • ${sample.durationFormatted} • ${sample.resolution}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) GeminiBlueLight else DarkTextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Video Header & Live Preview Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("active_video_preview_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Video Screen Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF022C22))
                            )
                        )
                        .border(1.dp, DarkOutline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GeminiBlueLight.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            radius = size.width * 0.45f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GeminiBlue.copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Preview", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.selectedVideoTitle,
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }

                    // Top Left Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = uiState.selectedVideoCategory,
                            style = MaterialTheme.typography.labelSmall.copy(color = GeminiCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        )
                    }

                    // Bottom Right Duration
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${uiState.selectedVideoDuration} • ${uiState.selectedVideoResolution}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Custom Context Field
                OutlinedTextField(
                    value = uiState.videoCustomContext,
                    onValueChange = { viewModel.updateVideoContext(it) },
                    label = { Text("Video Prompt Context (Optional)", fontSize = 11.sp) },
                    placeholder = { Text("Add extra topic or focus keywords for Gemini Flash...") },
                    modifier = Modifier.fillMaxWidth().testTag("video_context_input_field"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlueLight,
                        unfocusedBorderColor = DarkOutline,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary
                    ),
                    maxLines = 2
                )
            }
        }

        // Action Trigger Grid
        Card(
            modifier = Modifier.fillMaxWidth().testTag("video_metadata_action_grid_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Generate with Gemini Flash:",
                    style = MaterialTheme.typography.labelMedium.copy(color = DarkTextMuted, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Primary Generate All Button
                Button(
                    onClick = { viewModel.generateAllVideoMetadata() },
                    enabled = !uiState.isAnalyzingVideoMetadata,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeminiBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("generate_all_video_metadata_button")
                ) {
                    if (uiState.isAnalyzingVideoMetadata && uiState.activeMetadataTask.contains("full video", ignoreCase = true)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing Video Stream...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate All Video Metadata (1-Click)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2x3 Grid of Individual Extractors
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Description
                        Button(
                            onClick = { viewModel.generateVideoDescription() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_description_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GeminiBlueLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Description", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Hashtags
                        Button(
                            onClick = { viewModel.generateVideoHashtags() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_hashtags_btn")
                        ) {
                            Icon(Icons.Default.Tag, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hashtags", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3. Chapters
                        Button(
                            onClick = { viewModel.generateVideoChapters() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_chapters_btn")
                        ) {
                            Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chapters", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 4. Account Tags
                        Button(
                            onClick = { viewModel.generateVideoAccountTags() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_account_tags_btn")
                        ) {
                            Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = GeminiEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Account Tags", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 5. Links
                        Button(
                            onClick = { viewModel.generateVideoLinks() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPink.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_links_btn")
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = GeminiPink, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Links & Docs", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // 6. Thumbnails
                        Button(
                            onClick = { viewModel.generateVideoThumbnails() },
                            enabled = !uiState.isAnalyzingVideoMetadata,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("generate_thumbnails_btn")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thumbnails", color = DarkTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Status banner if analyzing
                AnimatedVisibility(visible = uiState.isAnalyzingVideoMetadata) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = GeminiBlueLight,
                            trackColor = DarkSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.activeMetadataTask.ifBlank { "Gemini Flash is processing video tokens..." },
                            style = MaterialTheme.typography.bodySmall.copy(color = GeminiBlueLight, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Export Suite Bar
        val hasAnyMetadata = uiState.videoMetadataDescriptionHtml != null ||
                uiState.videoMetadataHashtags.isNotEmpty() ||
                uiState.videoMetadataChapters.isNotEmpty()

        if (hasAnyMetadata) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("video_metadata_export_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1-Click Metadata Export Suite",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        )

                        IconButton(
                            onClick = { viewModel.clearVideoMetadata() },
                            modifier = Modifier.size(28.dp).testTag("clear_video_metadata_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // YouTube Description Export
                        Button(
                            onClick = {
                                val sb = StringBuilder()
                                if (!uiState.videoMetadataDescriptionHtml.isNullOrBlank()) {
                                    sb.appendLine(uiState.videoMetadataDescriptionHtml.replace(Regex("<[^>]*>"), ""))
                                    sb.appendLine()
                                }
                                if (uiState.videoMetadataChapters.isNotEmpty()) {
                                    sb.appendLine("TIMESTAMPS / CHAPTERS:")
                                    uiState.videoMetadataChapters.forEach { sb.appendLine("${it.timestamp} - ${it.title}") }
                                    sb.appendLine()
                                }
                                if (uiState.videoMetadataLinks.isNotEmpty()) {
                                    sb.appendLine("LINKS & RESOURCES:")
                                    uiState.videoMetadataLinks.forEach { sb.appendLine("${it.label}: ${it.url}") }
                                    sb.appendLine()
                                }
                                if (uiState.videoMetadataHashtags.isNotEmpty()) {
                                    sb.appendLine(uiState.videoMetadataHashtags.joinToString(" "))
                                }
                                clipboardManager.setText(AnnotatedString(sb.toString()))
                                Toast.makeText(context, "Full YouTube description copied!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("export_youtube_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("YouTube Pack", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Social Reel / TikTok Caption Export
                        Button(
                            onClick = {
                                val cleanDesc = (uiState.videoMetadataDescriptionHtml ?: "")
                                    .replace(Regex("<[^>]*>"), "")
                                    .take(120)
                                val tags = uiState.videoMetadataHashtags.take(8).joinToString(" ")
                                val handles = uiState.videoMetadataAccountTags.take(3).joinToString(" ") { it.handle }
                                val text = "$cleanDesc\n\n$handles\n$tags"
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Social caption copied for Reels & TikTok!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(38.dp).testTag("export_social_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reels / TikTok", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // View Filter Tabs
        if (hasAnyMetadata) {
            val filterOptions = listOf("All", "Description", "Hashtags", "Chapters", "Accounts", "Links", "Thumbnails")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = uiState.videoMetadataActiveViewFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GeminiBlue.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) GeminiBlueLight else DarkOutlineVariant, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setVideoMetadataFilter(filter) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) GeminiBlueLight else DarkTextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        // Display Generated Results
        val filter = uiState.videoMetadataActiveViewFilter

        if (uiState.videoMetadataError != null) {
            ErrorUi(uiState.videoMetadataError)
        }

        // 1. Description
        if ((filter == "All" || filter == "Description") && uiState.videoMetadataDescriptionHtml != null) {
            DescriptionUi(uiState.videoMetadataDescriptionHtml)
        }

        // 2. Hashtags
        if ((filter == "All" || filter == "Hashtags") && uiState.videoMetadataHashtags.isNotEmpty()) {
            HashtagsUi(uiState.videoMetadataHashtags)
        }

        // 3. Chapters
        if ((filter == "All" || filter == "Chapters") && uiState.videoMetadataChapters.isNotEmpty()) {
            ChaptersUi(
                chapters = uiState.videoMetadataChapters,
                onChapterClick = { chapter ->
                    Toast.makeText(context, "Seeking to ${chapter.timestamp}: ${chapter.title}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. Account Tags
        if ((filter == "All" || filter == "Accounts") && uiState.videoMetadataAccountTags.isNotEmpty()) {
            AccountTagsUi(uiState.videoMetadataAccountTags)
        }

        // 5. Links
        if ((filter == "All" || filter == "Links") && uiState.videoMetadataLinks.isNotEmpty()) {
            LinksUi(uiState.videoMetadataLinks)
        }

        // 6. Thumbnails
        if ((filter == "All" || filter == "Thumbnails") && uiState.videoMetadataThumbnails.isNotEmpty()) {
            ThumbnailsUi(uiState.videoMetadataThumbnails)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
