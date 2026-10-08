package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ReviewGeneratorManager
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextSubtle
import com.example.ui.theme.DeepThinkAmber
import com.example.ui.theme.DeepThinkCyan
import com.example.ui.theme.DeepThinkEmerald
import com.example.ui.theme.DeepThinkEmeraldLight
import com.example.ui.theme.DeepThinkIndigo
import com.example.ui.theme.DeepThinkIndigoLight
import com.example.ui.theme.DeepThinkRose
import com.example.ui.theme.DeepThinkViolet

@Composable
fun ReviewGeneratorDialog(
    viewModel: MainViewModel,
    uiState: UiState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            color = DarkBackground
        ) {
            ReviewGeneratorContent(
                viewModel = viewModel,
                uiState = uiState,
                onClose = onDismiss
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewGeneratorContent(
    viewModel: MainViewModel,
    uiState: UiState,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val predefinedTopics = remember { ReviewGeneratorManager.predefinedTopics }
    val tones = remember { ReviewGeneratorManager.availableTones }
    val highlights = remember { ReviewGeneratorManager.availableHighlights }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top App Bar
        Surface(
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(DeepThinkEmerald, DeepThinkCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = "Review Generator",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Review Generator",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DeepThinkEmerald.copy(alpha = 0.35f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Gemini 3.5 Flash-Lite Hybrid",
                                    color = DeepThinkEmeraldLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Topic-Selected On-Device Review Synthesis",
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .testTag("close_review_generator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Topic Selection & Custom Topic Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "1. Select Topic or Exhibition",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Predefined Topic Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            predefinedTopics.forEach { topic ->
                                val isSelected = uiState.reviewRequestTopic == topic
                                Surface(
                                    onClick = { viewModel.updateReviewTopic(topic) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) DeepThinkIndigo.copy(alpha = 0.35f) else DarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.dp else 0.5.dp,
                                        if (isSelected) DeepThinkIndigoLight else DarkOutlineVariant
                                    ),
                                    modifier = Modifier.testTag("topic_chip_${topic.take(10)}")
                                ) {
                                    Text(
                                        text = topic,
                                        color = if (isSelected) Color.White else DarkTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = uiState.reviewRequestTopic,
                            onValueChange = { viewModel.updateReviewTopic(it) },
                            placeholder = { Text("Or enter custom topic / venue...", color = DarkTextSubtle, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DeepThinkIndigoLight,
                                unfocusedBorderColor = DarkOutlineVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_topic_input_field"),
                            singleLine = true
                        )
                    }
                }
            }

            // 2. Star Rating & Tone Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "2. Rating & Review Tone",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Star Rating Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Score:",
                                color = DarkTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            (1..5).forEach { star ->
                                val isFilled = star <= uiState.reviewRatingStars
                                IconButton(
                                    onClick = { viewModel.updateReviewRating(star) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("star_rating_$star")
                                ) {
                                    Icon(
                                        imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "$star Stars",
                                        tint = if (isFilled) DeepThinkAmber else DarkTextSubtle,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.reviewRatingStars} / 5 Stars",
                                color = DeepThinkAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tone of Voice:",
                            color = DarkTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tones.forEach { tone ->
                                val isSelected = uiState.reviewSelectedTone == tone
                                Surface(
                                    onClick = { viewModel.updateReviewTone(tone) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) DeepThinkEmerald.copy(alpha = 0.3f) else DarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.dp else 0.5.dp,
                                        if (isSelected) DeepThinkEmeraldLight else DarkOutlineVariant
                                    ),
                                    modifier = Modifier.testTag("tone_chip_$tone")
                                ) {
                                    Text(
                                        text = tone,
                                        color = if (isSelected) DeepThinkEmeraldLight else DarkTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Highlight Tags Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "3. Select Specific Highlights & Attributes",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            highlights.forEach { tag ->
                                val isSelected = uiState.reviewSelectedHighlights.contains(tag)
                                Surface(
                                    onClick = { viewModel.toggleReviewHighlight(tag) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) DeepThinkCyan.copy(alpha = 0.25f) else DarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 1.dp else 0.5.dp,
                                        if (isSelected) DeepThinkCyan else DarkOutlineVariant
                                    ),
                                    modifier = Modifier.testTag("highlight_tag_$tag")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = DeepThinkCyan,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = tag,
                                            color = if (isSelected) Color.White else DarkTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Generate Button
            item {
                Button(
                    onClick = { viewModel.generateReview() },
                    enabled = !uiState.isGeneratingReview && uiState.reviewRequestTopic.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_review_button")
                ) {
                    if (uiState.isGeneratingReview) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Synthesizing Review on Gemini 3.5 Flash-Lite...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Review (On-Device + Cloud Fallback)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Result Display
            uiState.latestGeneratedReview?.let { review ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DeepThinkEmerald.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DeepThinkEmerald.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = review.modelUsed,
                                        color = DeepThinkEmeraldLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${review.latencyMs}ms • ${review.tokensGenerated} tokens",
                                        color = DarkTextSubtle,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Review", "${review.headline}\n\n${review.narrativeReview}\n\nTips: ${review.visitorTips}")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Review copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = DarkTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.headline,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Stars display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= review.ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (star <= review.ratingStars) DeepThinkAmber else DarkTextSubtle,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = review.overallVerdict,
                                    color = DeepThinkAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = review.narrativeReview,
                                color = Color(0xFFF1F5F9),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            // Highlights & Pros
                            if (review.pros.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                tint = DeepThinkEmeraldLight,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Highlights & Positives:",
                                                color = DeepThinkEmeraldLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        review.pros.forEach { pro ->
                                            Text(
                                                text = "• $pro",
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Cons
                            if (review.cons.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbDown,
                                                contentDescription = null,
                                                tint = DeepThinkRose,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Room for Improvement:",
                                                color = DeepThinkRose,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        review.cons.forEach { con ->
                                            Text(
                                                text = "• $con",
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Tips
                            if (review.visitorTips.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TipsAndUpdates,
                                            contentDescription = null,
                                            tint = DeepThinkAmber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Visitor Advice & Tips:",
                                                color = DeepThinkAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = review.visitorTips,
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Translation Section
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Translate Review (ML Kit):",
                                    color = DarkTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val langs = listOf(
                                    "es" to "🇪🇸 Spanish",
                                    "fr" to "🇫🇷 French",
                                    "de" to "🇩🇪 German",
                                    "it" to "🇮🇹 Italian",
                                    "ja" to "🇯🇵 Japanese"
                                )

                                langs.forEach { (code, label) ->
                                    Surface(
                                        onClick = { viewModel.translateGeneratedReview(code) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = DarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(0.6.dp, DarkOutlineVariant),
                                        modifier = Modifier.testTag("translate_review_$code")
                                    ) {
                                        Text(
                                            text = label,
                                            color = DeepThinkIndigoLight,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            uiState.reviewTranslatedVersion?.let { translated ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E1B4B),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, DeepThinkIndigoLight.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "🌐 ML Kit Translated Review (${uiState.reviewSelectedTranslateLang.uppercase()}):",
                                            color = DeepThinkIndigoLight,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = translated,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
