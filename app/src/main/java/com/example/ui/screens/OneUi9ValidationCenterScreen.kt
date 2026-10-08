package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.validation.ValidationCategory
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.screens.validation.*
import com.example.ui.theme.*

@Composable
fun OneUi9ValidationCenterScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val validationResult by viewModel.directValidationResult.collectAsState()
    val isValidating by viewModel.isValidatingDirectInput.collectAsState()
    val cleanupReport by viewModel.cleanupReport.collectAsState()
    val isCleaning by viewModel.isCleaningEnvironment.collectAsState()
    val currentInputText by viewModel.directInputText.collectAsState()

    var activeCategoryFilter by rememberSaveable { mutableStateOf<ValidationCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // One UI 9 Header Area
        Surface(
            color = DarkSurface,
            border = BorderStroke(0.8.dp, DarkOutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .testTag("one_ui_9_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = DeepThinkIndigoLight
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "One UI 9 Intelligence",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    DeepThinkCyan.copy(alpha = 0.3f),
                                                    DeepThinkViolet.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                        .border(
                                            0.8.dp,
                                            DeepThinkCyan.copy(alpha = 0.6f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Galaxy AI ✨",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DeepThinkCyan
                                    )
                                }
                            }
                            Text(
                                text = "Direkt-Eingabe-Validierung & App-Umfeld-Optimierung",
                                fontSize = 11.sp,
                                color = DarkTextMuted
                            )
                        }
                    }

                    // Quick Action: Clean & Optimize
                    Button(
                        onClick = { viewModel.runEnvironmentCleanup() },
                        enabled = !isCleaning,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepThinkEmerald),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("one_ui_9_top_clean_button")
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bereinigen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hardware & OS Specs Badges Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SpecPill(icon = Icons.Default.PhoneAndroid, text = "Android 17 (API 37)", color = DeepThinkEmerald)
                    SpecPill(icon = Icons.Default.Widgets, text = "Samsung One UI 9.0", color = DeepThinkCyan)
                    SpecPill(icon = Icons.Default.Bolt, text = "Exynos 2600 EU (92 TOPS)", color = DeepThinkAmber)
                    SpecPill(icon = Icons.Default.Memory, text = "ARMv9.2 SVE2 SIMD", color = DeepThinkViolet)
                    SpecPill(icon = Icons.Default.CloudSync, text = "Google Workspace & One", color = DeepThinkIndigoLight)
                }
            }
        }

        // Main Scrollable Interaction Body
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // Direct Input Section (Card)
            item {
                DirectInputCard(
                    inputText = currentInputText,
                    onInputChanged = { viewModel.setDirectInputText(it) },
                    isValidating = isValidating,
                    onValidate = { viewModel.validateDirectInput(currentInputText) },
                    onSelectPreset = { preset -> viewModel.setDirectInputText(preset) }
                )
            }

            // Cleanup Report Feedback Card (if available)
            cleanupReport?.let { report ->
                item {
                    CleanupReportCard(report = report)
                }
            }

            // Overall Score & Safety Verdict Banner
            validationResult?.let { result ->
                item {
                    ValidationScoreBanner(result = result)
                }

                // Category Filter Tabs
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = activeCategoryFilter == null,
                            onClick = { activeCategoryFilter = null },
                            label = { Text("Alle Prüfungen (${result.items.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepThinkIndigo.copy(alpha = 0.3f),
                                selectedLabelColor = DeepThinkCyan
                            )
                        )

                        ValidationCategory.entries.forEach { cat ->
                            val count = result.items.count { it.category == cat }
                            FilterChip(
                                selected = activeCategoryFilter == cat,
                                onClick = { activeCategoryFilter = cat },
                                label = { Text("${cat.displayName} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepThinkIndigo.copy(alpha = 0.3f),
                                    selectedLabelColor = DeepThinkCyan
                                )
                            )
                        }
                    }
                }

                // Filtered Items Checklist
                val displayedItems = result.items.filter { item ->
                    activeCategoryFilter == null || item.category == activeCategoryFilter
                }

                items(displayedItems, key = { it.id }) { item ->
                    ValidationItemCard(
                        item = item,
                        onApplyRecommendation = { rec ->
                            viewModel.setDirectInputText(rec)
                        }
                    )
                }
            }
        }
    }
}
