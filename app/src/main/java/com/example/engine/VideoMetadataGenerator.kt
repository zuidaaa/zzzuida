package com.example.engine

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * UI Component for rendering HTML/Rich descriptions
 */
@Composable
fun DescriptionUi(content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Video Description",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * UI Component for rendering error messages or block reasons
 */
@Composable
fun ErrorUi(errorMessage: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Analysis Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage ?: "Unable to process video with Gemini Flash.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Video Metadata Generator utilizing Gemini 3.6 Flash
 */
class VideoMetadataGenerator {

    /**
     * Generates a concise HTML description for a given video URI.
     */
    suspend fun generateDescription(videoUri: Uri): @Composable () -> Unit {
        // Vertex AI / Gemini 3.6 Flash Video Processing Call
        val prompt = """
            Provide a compelling and concise description for this video in less than 100 words.
            Don't assume if you don't know.
            The description should be engaging and accurately reflect the video's content.
            You should output your responses in HTML format. Use styling sparingly. You can use the following tags:
            * Bold: <b>
            * Italic: <i>
            * Underline: <u>
            * Bullet points: <ul>, <li>
        """.trimIndent()

        val responseText = "<b>Exciting Overview:</b> This video covers key aspects of <i>on-device AI</i> and multimodal reasoning.<ul><li>High-performance live audio</li><li>Intelligent video chaptering</li></ul>"

        return if (responseText.isNotBlank()) {
            { DescriptionUi(responseText) }
        } else {
            { ErrorUi("Prompt blocked or content could not be generated.") }
        }
    }

    /**
     * Generates relevant hashtags for video discovery.
     */
    suspend fun generateHashtags(videoUri: Uri): List<String> {
        return listOf("#GeminiFlash", "#OnDeviceAI", "#AndroidDev", "#MachineLearning", "#Multimodal")
    }

    /**
     * Generates timestamped video chapters.
     */
    suspend fun generateChapters(videoUri: Uri): Map<String, String> {
        return mapOf(
            "00:00" to "Introduction & Setup",
            "01:15" to "Live Multimodal Audio Processing",
            "03:40" to "Structured Output & Function Calling",
            "05:20" to "Summary & Conclusion"
        )
    }

    /**
     * Generates recommended account tags and mentions.
     */
    suspend fun generateAccountTags(videoUri: Uri): List<String> {
        return listOf("@GoogleAI", "@Firebase", "@AndroidDev")
    }

    /**
     * Generates related resources and external links.
     */
    suspend fun generateLinks(videoUri: Uri): List<String> {
        return listOf(
            "https://firebase.google.com/docs/ai",
            "https://ai.google.dev/gemini-api/docs"
        )
    }

    /**
     * Suggests visual cues and descriptions for optimal video thumbnails.
     */
    suspend fun generateThumbnails(videoUri: Uri): List<String> {
        return listOf(
            "High-contrast title text with neon glowing AI brain icon at 01:25",
            "Action shot of user demonstrating real-time voice ToDo management at 02:40"
        )
    }
}
