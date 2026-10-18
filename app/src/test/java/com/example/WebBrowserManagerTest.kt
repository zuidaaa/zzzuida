package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.engine.browser.BrowserPresets
import com.example.engine.browser.WebBrowserManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebBrowserManagerTest {

    private lateinit var application: Application
    private lateinit var testScope: TestScope
    private lateinit var browserManager: WebBrowserManager

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        testScope = TestScope(UnconfinedTestDispatcher())
        browserManager = WebBrowserManager(application, testScope)
    }

    @Test
    fun presets_containEssentialAiResearchLinks() {
        val quickLinks = BrowserPresets.QUICK_LINKS
        assertTrue("Quick links should not be empty", quickLinks.isNotEmpty())
        assertTrue("Should include arXiv", quickLinks.any { it.title.contains("arXiv") })
        assertTrue("Should include Hugging Face", quickLinks.any { it.title.contains("Hugging Face") })
        assertTrue("Should include GitHub", quickLinks.any { it.title.contains("GitHub") })
        assertTrue("Should include Papers with Code", quickLinks.any { it.title.contains("Papers with Code") })
    }

    @Test
    fun navigateTo_formatsHttpHttpsAndQueriesCorrectly() {
        // Direct HTTPS
        browserManager.navigateTo("https://arxiv.org/list/cs.AI/recent")
        assertEquals("https://arxiv.org/list/cs.AI/recent", browserManager.currentUrl.value)

        // Domain without scheme
        browserManager.navigateTo("github.com/trending")
        assertEquals("https://github.com/trending", browserManager.currentUrl.value)

        // Search Query
        browserManager.navigateTo("attention is all you need paper")
        assertTrue(browserManager.currentUrl.value.startsWith("https://duckduckgo.com/?q="))
        assertTrue(browserManager.currentUrl.value.contains("attention"))
    }

    @Test
    fun bookmarks_addAndToggleCorrectly() {
        val testUrl = "https://nature.com/articles/s41586-024-00000"
        val testTitle = "Nature Machine Intelligence"

        assertFalse(browserManager.isBookmarked(testUrl))

        browserManager.toggleBookmark(testTitle, testUrl)
        assertTrue(browserManager.isBookmarked(testUrl))

        // Toggling again should remove it
        browserManager.toggleBookmark(testTitle, testUrl)
        assertFalse(browserManager.isBookmarked(testUrl))
    }

    @Test
    fun pageExtraction_detectsWordCountAndCodePresence() {
        val textWithCode = """
            Here is the implementation of our sparse attention model:
            ```python
            import torch
            import torch.nn as nn
            def forward(x):
                return x * 2
            ```
            The model achieves a 94.2% top-1 accuracy on ImageNet.
        """.trimIndent()

        browserManager.setExtractedContent(
            title = "Sparse Attention Paper",
            url = "https://arxiv.org/abs/1234.5678",
            text = textWithCode
        )

        val result = browserManager.extractedContent.value
        assertNotNull(result)
        assertTrue("Word count should be > 15", result!!.wordCount > 15)
        assertTrue("Should detect code snippets in extracted text", result.hasCode)
    }

    @Test
    fun desktopMode_togglesCorrectly() {
        assertFalse(browserManager.isDesktopMode.value)
        browserManager.toggleDesktopMode()
        assertTrue(browserManager.isDesktopMode.value)
        browserManager.toggleDesktopMode()
        assertFalse(browserManager.isDesktopMode.value)
    }

    @Test
    fun onPageLifecycle_updatesStateAccurately() {
        browserManager.onPageStarted("https://huggingface.co/models")
        assertTrue(browserManager.isLoading.value)
        assertEquals("https://huggingface.co/models", browserManager.currentUrl.value)

        browserManager.onPageFinished(
            url = "https://huggingface.co/models",
            title = "Models - Hugging Face",
            canBack = true,
            canForward = false
        )
        assertFalse(browserManager.isLoading.value)
        assertEquals(100, browserManager.pageProgress.value)
        assertEquals("Models - Hugging Face", browserManager.currentTitle.value)
        assertTrue(browserManager.canGoBack.value)
        assertFalse(browserManager.canGoForward.value)
    }
}
