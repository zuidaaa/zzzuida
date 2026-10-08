package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit,
    isAuthenticating: Boolean,
    errorMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(DeepThinkIndigo, DeepThinkViolet)
                        )
                    )
                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Deep Think 3.7",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Cognitive Reasoning Engine",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DeepThinkIndigoLight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Securely sync your reasoning traces, problem-solving progress, and conversational history with Google Cloud Firestore.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DarkTextMuted,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isAuthenticating) {
                CircularProgressIndicator(color = DeepThinkCyan)
            } else {
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepThinkIndigo,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
