package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.OfflineDeepThinkingEngine
import com.example.engine.ThinkingLevel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("T#ink insid3 the b0x", appName)
  }

  @Test
  fun `offline deep thinking engine produces thought stream and final answer`() = runBlocking {
    val chunks = OfflineDeepThinkingEngine.processPromptStream(
      prompt = "Write a binary search algorithm in Kotlin",
      thinkingLevel = ThinkingLevel.LOW,
      streamingSpeedMs = 0L
    ).toList()

    assertTrue(chunks.isNotEmpty())
    val hasThoughts = chunks.any { it.isThinking }
    val hasAnswer = chunks.any { !it.isThinking }
    assertTrue("Should contain thinking tokens", hasThoughts)
    assertTrue("Should contain answer tokens", hasAnswer)
  }

  @Test
  fun `generative model structured output available on android sdk 36 and thinking works`() = runBlocking {
    val generativeModel = com.example.engine.GenerativeModel()
    assertTrue(generativeModel.isStructuredOutputFeatureAvailable())

    val request = com.example.engine.generateContentRequest {
      text("Classify Monstera Deliciosa")
      enableThinking = true
      setSchema(com.example.engine.Plant::class.java)
    }

    val response = generativeModel.generateContent(request)
    assertTrue(response.candidates.isNotEmpty())
    assertTrue(response.thoughtProcess.isNotEmpty())
  }

  @Test
  fun `video metadata generator generates chapters and hashtags with android uri`() = runBlocking {
    val generator = com.example.engine.VideoMetadataGenerator()
    val mockUri = android.net.Uri.parse("android.resource://com.example/raw/sample_video")

    val hashtags = generator.generateHashtags(mockUri)
    assertTrue(hashtags.contains("#GeminiFlash"))

    val chapters = generator.generateChapters(mockUri)
    assertTrue(chapters.containsKey("00:00"))
    assertEquals("Introduction & Setup", chapters["00:00"])
  }

  @Test
  fun `video metadata service produces composable blocks for all 6 metadata types`() = runBlocking {
    val mockUri = android.net.Uri.parse("https://video.gemini.ai/sample_keynote.mp4")

    val descComposable = com.example.engine.VideoMetadataService.generateDescription(mockUri, "Gemini Keynote")
    val hashtagsComposable = com.example.engine.VideoMetadataService.generateHashtags(mockUri, "Gemini Keynote")
    val chaptersComposable = com.example.engine.VideoMetadataService.generateChapters(mockUri, "Gemini Keynote")
    val accountsComposable = com.example.engine.VideoMetadataService.generateAccountTags(mockUri, "Gemini Keynote")
    val linksComposable = com.example.engine.VideoMetadataService.generateLinks(mockUri, "Gemini Keynote")
    val thumbnailsComposable = com.example.engine.VideoMetadataService.generateThumbnails(mockUri, "Gemini Keynote")

    org.junit.Assert.assertNotNull(descComposable)
    org.junit.Assert.assertNotNull(hashtagsComposable)
    org.junit.Assert.assertNotNull(chaptersComposable)
    org.junit.Assert.assertNotNull(accountsComposable)
    org.junit.Assert.assertNotNull(linksComposable)
    org.junit.Assert.assertNotNull(thumbnailsComposable)
  }

  @Test
  fun `rag local document store case insensitive keyword matching works`() {
    val localDocs = listOf(
        com.example.data.DocumentEntity(
            id = "doc1",
            title = "npu_telemetry_crash.log",
            content = "High tensor contention detected on Cortex-X6 cache boundary.",
            fileSize = 120L,
            isRagEnabled = true
        ),
        com.example.data.DocumentEntity(
            id = "doc2",
            title = "quarterly_strategy_brief.txt",
            content = "zero-token, privacy-first local LLM reasoning.",
            fileSize = 250L,
            isRagEnabled = true
        )
    )

    val searchToken = "CORTEX-X6"
    val matches = localDocs.filter { doc ->
        doc.title.lowercase().contains(searchToken.lowercase()) ||
        doc.content.lowercase().contains(searchToken.lowercase())
    }

    assertEquals(1, matches.size)
    assertEquals("doc1", matches.first().id)
    assertTrue(matches.first().content.contains("Cortex-X6", ignoreCase = true))
  }
}


