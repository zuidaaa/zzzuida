package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GlassCardPanel)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun FunctionalTag(
    text: String,
    variant: TagVariant = TagVariant.INFO_BLUE
) {
    val (bg, textColor) = when (variant) {
        TagVariant.INFO_BLUE -> LuminousBlue.copy(alpha = 0.15f) to LuminousBlue
        TagVariant.REASONING_PURPLE -> VibrantPurple.copy(alpha = 0.15f) to VibrantPurple
        TagVariant.TECHNICAL_TEAL -> VibrantTeal.copy(alpha = 0.15f) to VibrantTeal
        TagVariant.STATUS_GREEN -> VibrantGreen.copy(alpha = 0.15f) to VibrantGreen
        TagVariant.MODE_YELLOW -> LuminousYellow.copy(alpha = 0.15f) to LuminousYellow
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class TagVariant {
    INFO_BLUE, REASONING_PURPLE, TECHNICAL_TEAL, STATUS_GREEN, MODE_YELLOW
}

/**
 * Quick Select Circular Menu with 8 radial action segments matching the design specification
 */
@Composable
fun QuickSelectCircularMenu(
    modifier: Modifier = Modifier,
    onActionSelected: (Int) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 45f else 0f, label = "rotation")

    val segmentColors = listOf(
        Color(0xFF3B82F6), // Luminous Blue
        Color(0xFF14B8A6), // Vibrant Teal
        Color(0xFF8B5CF6), // Vibrant Purple
        Color(0xFF14B8A6), // Vibrant Teal
        Color(0xFFF59E0B), // Luminous Yellow
        Color(0xFFEF4444), // Vibrant Red
        Color(0xFF3B82F6), // Luminous Blue
        Color(0xFF14B8A6)  // Vibrant Teal
    )

    val segmentIcons = listOf(
        Icons.Default.Chat,
        Icons.Default.Api,
        Icons.Default.Hub,
        Icons.Default.Memory,
        Icons.Default.AutoAwesome,
        Icons.Default.Shield,
        Icons.Default.Tune,
        Icons.Default.Speed
    )

    Box(
        modifier = modifier
            .testTag("quick_select_circular_menu"),
        contentAlignment = Alignment.Center
    ) {
        // Expanded 8 Segments
        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xCC161B22),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                segmentColors.forEachIndexed { index, color ->
                    val angleDeg = index * 45.0 - 90.0
                    val angleRad = Math.toRadians(angleDeg)
                    val radius = 80.0
                    val x = (radius * cos(angleRad)).dp
                    val y = (radius * sin(angleRad)).dp

                    Surface(
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                onActionSelected(index)
                                isExpanded = false
                            },
                        shape = CircleShape,
                        color = color.copy(alpha = 0.85f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = segmentIcons[index],
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Center Action Trigger (64px)
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { isExpanded = !isExpanded },
            shape = CircleShape,
            color = LuminousBlue,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, BorderActive)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Widgets,
                    contentDescription = "Quick Select Menu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
