package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

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
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class TagVariant {
    INFO_BLUE, REASONING_PURPLE, TECHNICAL_TEAL, STATUS_GREEN, MODE_YELLOW
}
