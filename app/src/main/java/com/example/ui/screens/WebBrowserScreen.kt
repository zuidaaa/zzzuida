package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.browser.*
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.components.MarkdownContentView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebBrowserScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val browserManager = viewModel.webBrowserManager
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val currentUrl by browserManager.currentUrl.collectAsStateWithLifecycle()
    val currentTitle by browserManager.currentTitle.collectAsStateWithLifecycle()
    val isLoading by browserManager.isLoading.collectAsStateWithLifecycle()
    val pageProgress by browserManager.pageProgress.collectAsStateWithLifecycle()
    val canGoBack by browserManager.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by browserManager.canGoForward.collectAsStateWithLifecycle()
    val isDesktopMode by browserManager.isDesktopMode.collectAsStateWithLifecycle()
    val extractedContent by browserManager.extractedContent.collectAsStateWithLifecycle()
    val bookmarks by browserManager.bookmarks.collectAsStateWithLifecycle()
    val downloads by browserManager.downloads.collectAsStateWithLifecycle()
    val researchAnalysis by browserManager.activeResearchAnalysis.collectAsStateWithLifecycle()
    val isAnalyzingWithLlm by browserManager.isAnalyzingWithLlm.collectAsStateWithLifecycle()
    val llmError by browserManager.llmAnalysisError.collectAsStateWithLifecycle()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var urlInputText by remember(currentUrl) { mutableStateOf(currentUrl) }
    var showAiResearchModal by remember { mutableStateOf(false) }
    var showDownloadsModal by remember { mutableStateOf(false) }
    var showBookmarksModal by remember { mutableStateOf(false) }
    var selectedResearchTab by remember { mutableIntStateOf(0) }
    var customQueryInput by remember { mutableStateOf("") }

    // Intercept back button to navigate webview history before exiting tab
    BackHandler(enabled = true) {
        if (showAiResearchModal) {
            showAiResearchModal = false
        } else if (showDownloadsModal) {
            showDownloadsModal = false
        } else if (showBookmarksModal) {
            showBookmarksModal = false
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            viewModel.setActiveTab(0)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AbsoluteBlack)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP ADDRESS & NAVIGATION OMNIBAR
            Surface(
                color = Color(0xFF161B22),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // SSL Lock & Protocol indicator
                        val isHttps = currentUrl.startsWith("https://")
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    if (isHttps) "Connection is Encrypted (HTTPS)" else "Unencrypted Connection (HTTP)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Security Status",
                                tint = if (isHttps) VibrantGreen else LuminousYellow,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // URL / Search Input Omnibar
                        OutlinedTextField(
                            value = urlInputText,
                            onValueChange = { urlInputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("browser_url_input"),
                            placeholder = {
                                Text(
                                    "Search or enter URL (e.g. arxiv.org, huggingface.co)",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            singleLine = true,
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (urlInputText.isNotBlank()) {
                                        IconButton(
                                            onClick = { urlInputText = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            browserManager.navigateTo(urlInputText)
                                            webViewInstance?.loadUrl(browserManager.currentUrl.value)
                                        },
                                        modifier = Modifier.size(32.dp).testTag("browser_go_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Go",
                                            tint = LuminousBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                browserManager.navigateTo(urlInputText)
                                webViewInstance?.loadUrl(browserManager.currentUrl.value)
                            }),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LuminousBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = Color(0xFF0D1117),
                                unfocusedContainerColor = Color(0xFF0D1117)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Reload / Stop
                        IconButton(
                            onClick = {
                                if (isLoading) {
                                    webViewInstance?.stopLoading()
                                } else {
                                    webViewInstance?.reload()
                                }
                            },
                            modifier = Modifier.size(36.dp).testTag("browser_refresh_button")
                        ) {
                            Icon(
                                imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                                contentDescription = if (isLoading) "Stop Loading" else "Reload",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Bookmark Toggle
                        val isBookmarked = browserManager.isBookmarked(currentUrl)
                        IconButton(
                            onClick = {
                                browserManager.toggleBookmark(currentTitle, currentUrl)
                                Toast.makeText(
                                    context,
                                    if (!isBookmarked) "Bookmarked" else "Bookmark removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(36.dp).testTag("browser_bookmarks_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) LuminousYellow else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Desktop Mode Toggle
                        IconButton(
                            onClick = {
                                browserManager.toggleDesktopMode()
                                webViewInstance?.let { wv ->
                                    val desktopUa = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
                                    val mobileUa = WebSettings.getDefaultUserAgent(context)
                                    wv.settings.userAgentString = if (!isDesktopMode) desktopUa else mobileUa
                                    wv.reload()
                                }
                            },
                            modifier = Modifier.size(36.dp).testTag("browser_desktop_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDesktopMode) Icons.Default.DesktopMac else Icons.Default.PhoneAndroid,
                                contentDescription = "Desktop Mode",
                                tint = if (isDesktopMode) LuminousBlue else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick Links / Research Presets Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { showBookmarksModal = true },
                            color = Color(0x333B82F6),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0x553B82F6)),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Bookmarks, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bookmarks (${bookmarks.size})", fontSize = 11.sp, color = LuminousBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        BrowserPresets.QUICK_LINKS.forEach { link ->
                            Surface(
                                onClick = {
                                    browserManager.navigateTo(link.url)
                                    webViewInstance?.loadUrl(link.url)
                                },
                                color = Color(0xFF21262D),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = link.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (link.badge) {
                                            "Papers", "Research" -> VibrantPurple
                                            "Models", "Local LLMs" -> VibrantGreen
                                            "Code" -> LuminousBlue
                                            else -> LuminousYellow
                                        },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text(link.title, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // LOADING PROGRESS BAR
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { pageProgress / 100f },
                    modifier = Modifier.fillMaxWidth().height(2.5.dp),
                    color = LuminousBlue,
                    trackColor = Color(0xFF161B22)
                )
            }

            // WEBVIEW CONTAINER
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AndroidView(
                    factory = { ctx ->
                        createConfiguredWebView(
                            context = ctx,
                            browserManager = browserManager,
                            onWebViewCreated = { wv -> webViewInstance = wv }
                        ).also { wv ->
                            wv.loadUrl(currentUrl)
                        }
                    },
                    update = { wv ->
                        // Only load if URL changed from external navigation
                        if (wv.url != currentUrl && !isLoading) {
                            wv.loadUrl(currentUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize().testTag("browser_webview")
                )
            }

            // BOTTOM ACTION & RESEARCH BAR
            Surface(
                color = Color(0xFF161B22),
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back / Forward / Home Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { webViewInstance?.goBack() },
                            enabled = canGoBack,
                            modifier = Modifier.size(36.dp).testTag("browser_back_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) Color.White else TextDisabled,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { webViewInstance?.goForward() },
                            enabled = canGoForward,
                            modifier = Modifier.size(36.dp).testTag("browser_forward_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) Color.White else TextDisabled,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                browserManager.navigateTo("https://duckduckgo.com")
                                webViewInstance?.loadUrl("https://duckduckgo.com")
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Home",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Extracted Word Count Badge
                        if (extractedContent != null && extractedContent!!.wordCount > 0) {
                            Surface(
                                color = Color(0x2210B981),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0x4410B981)),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(VibrantGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${extractedContent!!.wordCount}w",
                                        fontSize = 10.sp,
                                        color = VibrantGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons: Downloads & AI Research
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Downloads Badge
                        val activeOrCompletedCount = downloads.size
                        BadgedBox(
                            badge = {
                                if (activeOrCompletedCount > 0) {
                                    Badge(
                                        containerColor = LuminousBlue,
                                        contentColor = Color.White
                                    ) {
                                        Text("$activeOrCompletedCount")
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { showDownloadsModal = true },
                                modifier = Modifier.size(38.dp).testTag("browser_downloads_button")
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = "Downloads",
                                    tint = if (downloads.any { it.status == DownloadStatus.DOWNLOADING }) LuminousBlue else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Glowing AI Research & Synthesis Trigger
                        Button(
                            onClick = {
                                showAiResearchModal = true
                                if (researchAnalysis == null && !isAnalyzingWithLlm) {
                                    browserManager.performAiResearch(analysisType = "SUMMARY")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("browser_ai_research_button")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "AI Research",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // AI RESEARCH DIALOG / MODAL
        if (showAiResearchModal) {
            AlertDialog(
                onDismissRequest = { showAiResearchModal = false },
                containerColor = Color(0xFF161B22),
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.90f),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = VibrantPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "LLM Online Research Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { showAiResearchModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Source Webpage Card
                        Surface(
                            color = Color(0xFF0D1117),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = currentTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentUrl,
                                    fontSize = 11.sp,
                                    color = LuminousBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Tab Row for Research Modes
                        val researchTabs = listOf("Executive Summary", "Deep Dossier", "Extract Code", "Critique", "Ask Question")
                        ScrollableTabRow(
                            selectedTabIndex = selectedResearchTab,
                            containerColor = Color.Transparent,
                            contentColor = VibrantPurple,
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            researchTabs.forEachIndexed { index, label ->
                                Tab(
                                    selected = selectedResearchTab == index,
                                    onClick = {
                                        selectedResearchTab = index
                                        val type = when (index) {
                                            0 -> "SUMMARY"
                                            1 -> "DEEP_RESEARCH"
                                            2 -> "CODE_EXTRACTION"
                                            3 -> "CRITIQUE"
                                            else -> "ASK_QUESTION"
                                        }
                                        if (index != 4) {
                                            browserManager.performAiResearch(analysisType = type)
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedResearchTab == index) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedResearchTab == index) VibrantPurple else TextSecondary
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Query input when on "Ask Question" tab
                        if (selectedResearchTab == 4) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                OutlinedTextField(
                                    value = customQueryInput,
                                    onValueChange = { customQueryInput = it },
                                    placeholder = { Text("Ask anything about this webpage...", fontSize = 12.sp, color = TextSecondary) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = Color.White),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VibrantPurple,
                                        unfocusedBorderColor = BorderSubtle,
                                        focusedContainerColor = Color(0xFF0D1117),
                                        unfocusedContainerColor = Color(0xFF0D1117)
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (customQueryInput.isNotBlank()) {
                                            browserManager.performAiResearch("ASK_QUESTION", customQueryInput)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Content Display Area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            when {
                                isAnalyzingWithLlm -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = VibrantPurple,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Thinking & Synthesizing Web Research...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            "Parsing text structure, cross-referencing concepts & verifying logic",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                llmError != null -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = VibrantRed, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(llmError ?: "An error occurred", color = VibrantRed, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { browserManager.performAiResearch("SUMMARY") },
                                            colors = ButtonDefaults.buttonColors(containerColor = VibrantPurple)
                                        ) {
                                            Text("Retry Synthesis")
                                        }
                                    }
                                }

                                researchAnalysis != null -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        MarkdownContentView(
                                            content = researchAnalysis!!.summary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                else -> {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Tap any tab above to generate instant AI research analysis for this page.",
                                            color = TextSecondary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        // Copy Button
                        TextButton(
                            onClick = {
                                researchAnalysis?.let {
                                    clipboardManager.setText(AnnotatedString(it.summary))
                                    Toast.makeText(context, "Research summary copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = researchAnalysis != null
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }

                        // Send to Cognitive Chat Button
                        Button(
                            onClick = {
                                researchAnalysis?.let {
                                    viewModel.insertResearchIntoChat(currentTitle, currentUrl, it.summary)
                                    showAiResearchModal = false
                                }
                            },
                            enabled = researchAnalysis != null,
                            colors = ButtonDefaults.buttonColors(containerColor = LuminousBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send to Chat", fontSize = 12.sp)
                        }

                        // Save to Workspace Dossier
                        Button(
                            onClick = {
                                researchAnalysis?.let {
                                    viewModel.saveResearchToWorkspace(currentTitle, currentUrl, it.summary)
                                    Toast.makeText(context, "Saved to 3-Folder Workspace (Research)", Toast.LENGTH_SHORT).show()
                                    showAiResearchModal = false
                                }
                            },
                            enabled = researchAnalysis != null,
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Dossier", fontSize = 12.sp)
                        }
                    }
                }
            )
        }

        // DOWNLOADS DIALOG / DRAWER
        if (showDownloadsModal) {
            AlertDialog(
                onDismissRequest = { showDownloadsModal = false },
                containerColor = Color(0xFF161B22),
                modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.75f),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = LuminousBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        if (downloads.isNotEmpty()) {
                            TextButton(onClick = { browserManager.clearDownloads() }) {
                                Text("Clear All", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                },
                text = {
                    if (downloads.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No downloads yet", color = TextSecondary, fontSize = 14.sp)
                                Text(
                                    "Files, PDFs, datasets and models downloaded from the web will appear here.",
                                    color = TextDisabled,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(downloads) { item ->
                                Surface(
                                    color = Color(0xFF0D1117),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = when {
                                                        item.fileName.endsWith(".pdf") -> Icons.Default.PictureAsPdf
                                                        item.fileName.endsWith(".gguf") || item.fileName.endsWith(".bin") -> Icons.Default.Memory
                                                        item.fileName.endsWith(".py") || item.fileName.endsWith(".kt") || item.fileName.endsWith(".json") -> Icons.Default.Code
                                                        else -> Icons.AutoMirrored.Filled.InsertDriveFile
                                                    },
                                                    contentDescription = null,
                                                    tint = when (item.status) {
                                                        DownloadStatus.COMPLETED -> VibrantGreen
                                                        DownloadStatus.DOWNLOADING -> LuminousBlue
                                                        DownloadStatus.FAILED -> VibrantRed
                                                        else -> LuminousYellow
                                                    },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        item.fileName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = when (item.status) {
                                                            DownloadStatus.COMPLETED -> "Completed • ${formatBytes(item.fileSizeBytes)}"
                                                            DownloadStatus.DOWNLOADING -> "Downloading (${item.progress}%)"
                                                            DownloadStatus.FAILED -> "Failed: ${item.errorMessage ?: "Unknown error"}"
                                                            DownloadStatus.PENDING -> "Queued..."
                                                        },
                                                        fontSize = 11.sp,
                                                        color = when (item.status) {
                                                            DownloadStatus.COMPLETED -> VibrantGreen
                                                            DownloadStatus.DOWNLOADING -> LuminousBlue
                                                            DownloadStatus.FAILED -> VibrantRed
                                                            else -> TextSecondary
                                                        }
                                                    )
                                                }
                                            }

                                            // Open or action button
                                            if (item.status == DownloadStatus.COMPLETED && item.localPath.isNotBlank()) {
                                                IconButton(
                                                    onClick = {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                                setDataAndType(Uri.parse(item.localPath), item.mimeType)
                                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            }
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Saved to: ${item.localPath}", Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open", tint = LuminousBlue, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        if (item.status == DownloadStatus.DOWNLOADING) {
                                            LinearProgressIndicator(
                                                progress = { item.progress / 100f },
                                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(3.dp),
                                                color = LuminousBlue,
                                                trackColor = Color(0xFF21262D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDownloadsModal = false }) {
                        Text("Close", color = LuminousBlue)
                    }
                }
            )
        }

        // BOOKMARKS DIALOG
        if (showBookmarksModal) {
            AlertDialog(
                onDismissRequest = { showBookmarksModal = false },
                containerColor = Color(0xFF161B22),
                modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.70f),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bookmarks & Saved Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { showBookmarksModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                },
                text = {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(bookmarks) { b ->
                            Surface(
                                onClick = {
                                    browserManager.navigateTo(b.url)
                                    webViewInstance?.loadUrl(b.url)
                                    showBookmarksModal = false
                                },
                                color = Color(0xFF0D1117),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Text(b.url, fontSize = 11.sp, color = LuminousBlue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(
                                        onClick = { browserManager.toggleBookmark(b.title, b.url) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = VibrantRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookmarksModal = false }) {
                        Text("Close", color = LuminousBlue)
                    }
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createConfiguredWebView(
    context: Context,
    browserManager: WebBrowserManager,
    onWebViewCreated: (WebView) -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setSupportMultipleWindows(false)
        }

        // Configure DownloadListener for full download management
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            browserManager.startDownload(
                url = url,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimetype,
                contentLength = contentLength
            )
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { browserManager.onPageStarted(it) }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val targetUrl = url ?: ""
                val targetTitle = view?.title ?: targetUrl
                browserManager.onPageFinished(
                    url = targetUrl,
                    title = targetTitle,
                    canBack = view?.canGoBack() ?: false,
                    canForward = view?.canGoForward() ?: false
                )

                // Inject JavaScript to extract readable body text for AI Research
                view?.evaluateJavascript(
                    "(function() { return (document.body ? document.body.innerText : '') || ''; })();"
                ) { result ->
                    val cleanText = unescapeJs(result)
                    browserManager.setExtractedContent(targetTitle, targetUrl, cleanText)
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val reqUrl = request?.url?.toString() ?: return false
                return when {
                    reqUrl.startsWith("http://") || reqUrl.startsWith("https://") -> false
                    else -> {
                        // Open external apps (mailto:, tel:, market:, intent:)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reqUrl))
                            view?.context?.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignored if app not installed
                        }
                        true
                    }
                }
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                // Allow research servers or self-signed intranet test endpoints
                handler?.proceed()
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                browserManager.onProgressChanged(newProgress)
            }
        }

        onWebViewCreated(this)
    }
}

private fun unescapeJs(raw: String?): String {
    if (raw == null || raw == "null" || raw == "\"\"") return ""
    var str = raw.trim()
    if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
        str = str.substring(1, str.length - 1)
    }
    return str
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, 3)
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
