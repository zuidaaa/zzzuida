package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.zIndex

@Composable
fun EdgePanel(
    isVisible: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isVisible) {
        // Transparent clickable background to close on tap outside
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onToggle() }
                .zIndex(99f)
        )
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = Modifier.zIndex(100f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF151515).copy(alpha = 0.95f),
                            Color(0xFF000000).copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}
