package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeneratedMediaEntity
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiBlueGlow
import com.example.ui.theme.GeminiBlueLight
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.WarningOrange

@Composable
fun CreativeStudioScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    mediaList: List<GeneratedMediaEntity>,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableStateOf(0) } // 0: Image Creator & Editor, 1: Veo 3 Video Animator

    val styles = listOf("Cinematic 8K", "Photorealistic Product", "Cyberpunk Neon", "Anime Concept Art", "Minimalist Vector", "3D Blender Render")
    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3")
    val motionPresets = listOf("Cinematic Drone Orbit", "Dynamic Zoom & Dolly", "Subject Character Pan", "Ethereal Particle Flow", "Fast Hyperlapse")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Header
            Column {
                Text(
                    text = "Vision & Veo 3 Creative Studio",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                )
                Text(
                    text = "High-speed text-to-image synthesis & Veo 3 dynamic video motion rendering",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted)
                )
            }
        }

        // Subtabs
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = DarkSurface,
                contentColor = GeminiBlueLight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[subTab]),
                        color = GeminiBlueLight
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create & Edit Images", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Veo 3 Animator", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                )
                Tab(
                    selected = subTab == 2,
                    onClick = { subTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Video Metadata AI", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                )
            }
        }

        if (subTab == 2) {
            // TAB 2: AI Video Metadata & Insights (Gemini Flash)
            item {
                VideoMetadataStudioSection(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        } else if (subTab == 0) {
            // TAB 0: Image Creator & Editor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Text-to-Image Prompt",
                            style = MaterialTheme.typography.labelMedium.copy(color = GeminiCyan, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.imagePromptInput,
                            onValueChange = { viewModel.setImagePromptInput(it) },
                            placeholder = {
                                Text(
                                    "e.g. Ultra-detailed futuristic holographic cybernetic watch on a dark glass pedestal with volumetric sapphire glow...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .testTag("image_prompt_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeminiBlue,
                                unfocusedBorderColor = DarkOutlineVariant,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant,
                                focusedTextColor = DarkTextPrimary,
                                unfocusedTextColor = DarkTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Style selector chips
                        Text(
                            text = "Artistic Style",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            styles.forEach { style ->
                                val isSelected = style == uiState.selectedImageStyle
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) GeminiBlue.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) GeminiBlue else DarkOutlineVariant, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.setImageStyle(style) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = style,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else DarkTextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Aspect ratio chips
                        Text(
                            text = "Aspect Ratio",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            aspectRatios.forEach { ratio ->
                                val isSelected = ratio == uiState.selectedAspectRatio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) GeminiCyan.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) GeminiCyan else DarkOutlineVariant, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setAspectRatio(ratio) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ratio,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) GeminiCyan else DarkTextMuted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Generate Button
                        Button(
                            onClick = { viewModel.generateImage() },
                            enabled = uiState.imagePromptInput.isNotBlank() && !uiState.isGeneratingImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("generate_image_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isGeneratingImage) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing Visual Asset...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Image", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Latest Generated Preview if available
            uiState.latestGeneratedMedia?.let { media ->
                item {
                    Text(
                        text = "LATEST GENERATION PREVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    MediaVisualCard(
                        media = media,
                        isPlaying = false,
                        onAnimateWithVeo = {
                            viewModel.setVeoActiveMedia(media)
                            subTab = 1
                        },
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }
            }
        } else {
            // TAB 1: Veo 3 Video Animator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Veo 3 Motion Physics & Camera Director",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Transform still product photos or concept art into dynamic, physics-guided video ads with cinematic camera motion paths.",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, lineHeight = 16.sp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Motion Presets
                        Text(
                            text = "Camera Motion Path Preset",
                            style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            motionPresets.forEach { preset ->
                                val isSelected = preset == uiState.veoMotionPreset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) GeminiPurple.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) GeminiPurple else DarkOutlineVariant, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.setVeoMotionPreset(preset) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else DarkTextMuted,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Active Target Image for animation
                        if (uiState.veoActiveMedia != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = GeminiCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Target: ${uiState.veoActiveMedia.prompt.take(32)}...",
                                        style = MaterialTheme.typography.labelMedium.copy(color = DarkTextPrimary, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Style: ${uiState.veoActiveMedia.style} • Preset: ${uiState.veoMotionPreset}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 10.5.sp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Animate Button
                        Button(
                            onClick = { viewModel.animateWithVeo3() },
                            enabled = (uiState.veoActiveMedia != null || mediaList.isNotEmpty()) && !uiState.isGeneratingVeo,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("animate_veo_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isGeneratingVeo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rendering Veo 3 Video Motion...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Animate Image with Veo 3", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Interactive Veo Video Canvas Player
            uiState.veoActiveMedia?.let { active ->
                item {
                    Text(
                        text = "VEO 3 DYNAMIC MOTION CANVAS",
                        style = MaterialTheme.typography.labelSmall.copy(color = DarkTextSubtle, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    InteractiveVeoPlayerCard(
                        media = active,
                        isPlaying = uiState.veoPlaybackPlaying,
                        onTogglePlay = { viewModel.toggleVeoPlayback() }
                    )
                }
            }
        }

        if (subTab != 2) {
            // Media Gallery
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STUDIO MEDIA GALLERY (${mediaList.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = DarkTextSubtle,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            if (mediaList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No media generated yet", style = MaterialTheme.typography.bodyMedium.copy(color = DarkTextMuted))
                            Text("Enter a prompt above to generate 8K visuals or Veo 3 animations.", style = MaterialTheme.typography.bodySmall.copy(color = DarkTextSubtle))
                        }
                    }
                }
            } else {
                items(mediaList, key = { it.id }) { media ->
                    MediaVisualCard(
                        media = media,
                        isPlaying = false,
                        onAnimateWithVeo = {
                            viewModel.setVeoActiveMedia(media)
                            subTab = 1
                        },
                        onDelete = { viewModel.deleteMedia(media.id) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MediaVisualCard(
    media: GeneratedMediaEntity,
    isPlaying: Boolean,
    onAnimateWithVeo: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("media_card_${media.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Visual Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F172A),
                                if (media.mediaType == "VIDEO") Color(0xFF2E1065) else Color(0xFF0C4A6E),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .border(1.dp, DarkOutline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Procedural Artistic Pattern
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (media.mediaType == "VIDEO") GeminiPurple else GeminiCyan).copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.45f
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (media.mediaType == "VIDEO") Icons.Default.Videocam else Icons.Default.Palette,
                        contentDescription = null,
                        tint = if (media.mediaType == "VIDEO") GeminiPurple else GeminiCyan,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (media.mediaType == "VIDEO") "Veo 3 Video: ${media.motionPreset}" else "8K Asset: ${media.style}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Seed: ${media.mediaSeed} • Ratio: ${media.aspectRatio}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DarkTextMuted,
                            fontSize = 10.sp
                        )
                    )
                }

                // Type Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkBackground.copy(alpha = 0.8f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = media.mediaType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (media.mediaType == "VIDEO") GeminiPurple else GeminiCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prompt description
            Text(
                text = media.prompt,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (media.mediaType == "IMAGE") {
                    Button(
                        onClick = onAnimateWithVeo,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Animate with Veo 3", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                    }
                } else {
                    Text(
                        text = "Motion Path: ${media.motionPreset}",
                        style = MaterialTheme.typography.labelSmall.copy(color = GeminiPurple, fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkTextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun InteractiveVeoPlayerCard(
    media: GeneratedMediaEntity,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("veo_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GeminiPurple)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Live Motion Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF030712))
                    .border(1.dp, DarkOutline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Procedural Camera Orbit Motion Simulation
                Canvas(modifier = Modifier.fillMaxSize().rotate(rotation)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(GeminiPurple.copy(alpha = 0.6f), GeminiCyan.copy(alpha = 0.4f), Color.Transparent)
                        ),
                        radius = size.width * 0.35f,
                        center = center
                    )
                }

                // Play / Pause Overlay Icon
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GeminiPurple.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Info Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Veo 3 Real-Time Physics Playback",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    )
                    Text(
                        text = "Preset: ${media.motionPreset.ifBlank { "Cinematic Orbit" }} • 60 FPS Smooth",
                        style = MaterialTheme.typography.bodySmall.copy(color = GeminiPurple, fontSize = 11.sp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPlaying) GeminiEmerald.copy(alpha = 0.2f) else DarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPlaying) "PLAYING" else "PAUSED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isPlaying) GeminiEmerald else DarkTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
