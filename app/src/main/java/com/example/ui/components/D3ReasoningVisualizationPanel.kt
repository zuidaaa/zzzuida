package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ReasoningCacheEntity
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

data class ThoughtTraceMetric(
    val id: String,
    val title: String,
    val query: String,
    val reasoningTimeMs: Long,
    val tokenUsage: Int,
    val thinkingLevel: String,
    val stepCount: Int,
    val modelName: String,
    val isCacheHit: Boolean,
    val timestamp: Long
)

/**
 * Sample pre-loaded traces for instant rich visualization if the user hasn't generated traces yet
 */
val defaultTraceMetrics = listOf(
    ThoughtTraceMetric(
        id = "trace-1",
        title = "Basel Problem Euler Sinc Proof",
        query = "Prove sum of 1/n^2 equals pi^2/6 using Euler sinc product",
        reasoningTimeMs = 3840L,
        tokenUsage = 4280,
        thinkingLevel = "HIGH",
        stepCount = 5,
        modelName = "gemini-3.7-deep-think",
        isCacheHit = false,
        timestamp = System.currentTimeMillis() - 86400000L * 3
    ),
    ThoughtTraceMetric(
        id = "trace-2",
        title = "MPMC Lock-Free Ring Buffer",
        query = "Implement bounded MPMC queue with sequence monotonic cells",
        reasoningTimeMs = 4650L,
        tokenUsage = 5620,
        thinkingLevel = "EXTENDED",
        stepCount = 6,
        modelName = "gemini-3.7-deep-think",
        isCacheHit = false,
        timestamp = System.currentTimeMillis() - 86400000L * 2
    ),
    ThoughtTraceMetric(
        id = "trace-3",
        title = "Quantum Decoherence Kinetics",
        query = "Lindbladian pure dephasing off-diagonal decay in superconducting transmon",
        reasoningTimeMs = 2920L,
        tokenUsage = 3450,
        thinkingLevel = "MEDIUM",
        stepCount = 4,
        modelName = "gemini-2.5-flash-think",
        isCacheHit = false,
        timestamp = System.currentTimeMillis() - 86400000L
    ),
    ThoughtTraceMetric(
        id = "trace-4",
        title = "100 Explorers Permutation Cycles",
        query = "Calculate group victory probability for loop drawer strategy in S_100",
        reasoningTimeMs = 3410L,
        tokenUsage = 3980,
        thinkingLevel = "HIGH",
        stepCount = 5,
        modelName = "gemini-3.7-deep-think",
        isCacheHit = false,
        timestamp = System.currentTimeMillis() - 3600000L * 8
    ),
    ThoughtTraceMetric(
        id = "trace-5",
        title = "Maxwell's Demon Landauer Bound",
        query = "Derive minimum thermodynamic erasure dissipation Q = k_B * T * ln(2)",
        reasoningTimeMs = 2650L,
        tokenUsage = 3120,
        thinkingLevel = "MEDIUM",
        stepCount = 4,
        modelName = "gemini-2.5-flash-think",
        isCacheHit = true,
        timestamp = System.currentTimeMillis() - 3600000L * 4
    ),
    ThoughtTraceMetric(
        id = "trace-6",
        title = "Held-Karp Bitmask DP Tour",
        query = "Exact polynomial-exponential Hamiltonian cycle in O(2^N * N^2)",
        reasoningTimeMs = 4120L,
        tokenUsage = 4950,
        thinkingLevel = "HIGH",
        stepCount = 5,
        modelName = "gemini-3.7-deep-think",
        isCacheHit = false,
        timestamp = System.currentTimeMillis() - 3600000L
    ),
    ThoughtTraceMetric(
        id = "trace-7",
        title = "Room Thought-Trace Index Lookup",
        query = "Instant deterministic hash retrieval for cached proof",
        reasoningTimeMs = 2L,
        tokenUsage = 3850,
        thinkingLevel = "HIGH",
        stepCount = 4,
        modelName = "room-sqlite-cache",
        isCacheHit = true,
        timestamp = System.currentTimeMillis()
    )
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3ReasoningVisualizationPanel(
    cachedItems: List<ReasoningCacheEntity>,
    modifier: Modifier = Modifier,
    onTraceSelected: ((ThoughtTraceMetric) -> Unit)? = null
) {
    val context = LocalContext.current

    // Convert cachedItems to ThoughtTraceMetric or fallback to defaultTraceMetrics
    val traceMetrics = remember(cachedItems) {
        if (cachedItems.isNotEmpty()) {
            cachedItems.map { item ->
                ThoughtTraceMetric(
                    id = item.cacheKey,
                    title = item.promptQuery.take(35) + if (item.promptQuery.length > 35) "..." else "",
                    query = item.promptQuery,
                    reasoningTimeMs = item.thinkingDurationMs.coerceAtLeast(1L),
                    tokenUsage = item.thinkingTokens.coerceAtLeast(10),
                    thinkingLevel = item.thinkingLevel,
                    stepCount = (item.thinkingTokens / 800).coerceAtLeast(1).coerceAtMost(8),
                    modelName = item.modelName.ifBlank { item.modelId },
                    isCacheHit = item.hitCount > 1,
                    timestamp = item.cachedAt
                )
            }
        } else {
            defaultTraceMetrics
        }
    }

    var selectedChartMode by remember { mutableStateOf("dual_bar_line") } // "dual_bar_line", "scatter", "timeline"
    var activeTraceDetail by remember { mutableStateOf<ThoughtTraceMetric?>(null) }
    var selectedFilterLevel by remember { mutableStateOf("ALL") }

    val filteredMetrics = remember(traceMetrics, selectedFilterLevel) {
        if (selectedFilterLevel == "ALL") traceMetrics
        else traceMetrics.filter { it.thinkingLevel.equals(selectedFilterLevel, ignoreCase = true) }
    }

    // Calculated Aggregates
    val totalTraces = filteredMetrics.size
    val avgTimeMs = remember(filteredMetrics) {
        if (filteredMetrics.isNotEmpty()) filteredMetrics.map { it.reasoningTimeMs }.average().toLong() else 0L
    }
    val totalTokens = remember(filteredMetrics) {
        filteredMetrics.sumOf { it.tokenUsage }
    }
    val avgTokens = remember(filteredMetrics) {
        if (filteredMetrics.isNotEmpty()) totalTokens / filteredMetrics.size else 0
    }
    val throughputTokensPerSec = remember(filteredMetrics, avgTimeMs, avgTokens) {
        if (avgTimeMs > 0) ((avgTokens.toDouble() / avgTimeMs) * 1000).toInt() else 0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_visualization_panel"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 1. Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(listOf(GeminiCyan.copy(alpha = 0.2f), GeminiPurple.copy(alpha = 0.2f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = GeminiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "D3.js Thought-Trace Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GeminiCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "D3 v7",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiCyan
                                )
                            }
                        }
                        Text(
                            text = "Dual-Axis Visualization: Reasoning Latency vs Token Footprint",
                            style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp)
                        )
                    }
                }

                // Chart mode chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { selectedChartMode = "dual_bar_line" },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (selectedChartMode == "dual_bar_line") GeminiCyan.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar & Line",
                            tint = if (selectedChartMode == "dual_bar_line") GeminiCyan else DarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { selectedChartMode = "scatter" },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (selectedChartMode == "scatter") GeminiPurple.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BubbleChart,
                            contentDescription = "Scatter Correlation",
                            tint = if (selectedChartMode == "scatter") GeminiPurple else DarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. High-Level Summary Stat Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryMetricTile(
                    title = "Avg Latency",
                    value = "${String.format("%.2f", avgTimeMs / 1000.0)}s",
                    subtitle = "$avgTimeMs ms",
                    accentColor = GeminiCyan,
                    modifier = Modifier.weight(1f)
                )

                SummaryMetricTile(
                    title = "Total Tokens",
                    value = if (totalTokens >= 1000) "${totalTokens / 1000}k" else "$totalTokens",
                    subtitle = "Avg $avgTokens / trace",
                    accentColor = GeminiEmerald,
                    modifier = Modifier.weight(1f)
                )

                SummaryMetricTile(
                    title = "Throughput",
                    value = "$throughputTokensPerSec",
                    subtitle = "tokens / sec",
                    accentColor = GeminiPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL:",
                    style = MaterialTheme.typography.labelSmall.copy(color = DarkTextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                )

                listOf("ALL", "MEDIUM", "HIGH", "EXTENDED").forEach { level ->
                    val isSelected = selectedFilterLevel == level
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterLevel = level },
                        label = { Text(level, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GeminiCyan.copy(alpha = 0.2f),
                            selectedLabelColor = GeminiCyan,
                            containerColor = DarkSurfaceVariant,
                            labelColor = DarkTextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) GeminiCyan else DarkOutlineVariant,
                            enabled = true,
                            selected = isSelected
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Interactive D3.js WebView Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkOutlineVariant, RoundedCornerShape(12.dp))
            ) {
                val d3HtmlContent = remember(filteredMetrics, selectedChartMode) {
                    generateD3VisualizationHtml(filteredMetrics, selectedChartMode)
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            setBackgroundColor(0x00000000)

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    return true
                                }
                            }

                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onTraceClicked(traceId: String) {
                                    val match = filteredMetrics.find { it.id == traceId }
                                    if (match != null) {
                                        activeTraceDetail = match
                                        onTraceSelected?.invoke(match)
                                    }
                                }
                            }, "AndroidBridge")

                            loadDataWithBaseURL(null, d3HtmlContent, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, d3HtmlContent, "text/html", "UTF-8", null)
                    }
                )
            }

            // 5. Active Trace Inspection Detail Banner (if a node is selected)
            AnimatedVisibility(visible = activeTraceDetail != null) {
                activeTraceDetail?.let { trace ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trace.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary
                                    ),
                                    maxLines = 1
                                )
                                IconButton(onClick = { activeTraceDetail = null }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = DarkTextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = trace.query,
                                style = MaterialTheme.typography.bodySmall.copy(color = DarkTextMuted, fontSize = 11.sp),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⏱ ${trace.reasoningTimeMs}ms", fontSize = 10.sp, color = GeminiCyan, fontWeight = FontWeight.Bold)
                                Text("📊 ${trace.tokenUsage} tokens", fontSize = 10.sp, color = GeminiEmerald, fontWeight = FontWeight.Bold)
                                Text("🧠 ${trace.thinkingLevel}", fontSize = 10.sp, color = GeminiPurple, fontWeight = FontWeight.Bold)
                                if (trace.isCacheHit) {
                                    Text("⚡ Cache Hit", fontSize = 10.sp, color = GeminiAmber, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Legend Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GeminiEmerald))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Token Usage (Right Axis)", fontSize = 10.sp, color = DarkTextMuted)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GeminiCyan))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reasoning Time (Left Axis)", fontSize = 10.sp, color = DarkTextMuted)
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkOutlineVariant, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                    fontSize = 15.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.5.sp,
                    color = DarkTextMuted
                )
            )
        }
    }
}

/**
 * Generates an interactive, responsive D3.js v7 HTML document containing dual-axis charts,
 * interactive tooltips, and click callbacks to Android.
 */
private fun generateD3VisualizationHtml(
    metrics: List<ThoughtTraceMetric>,
    chartMode: String
): String {
    val jsonArray = JSONArray()
    metrics.forEach { m ->
        val obj = JSONObject()
        obj.put("id", m.id)
        obj.put("title", m.title.replace("\"", "\\\""))
        obj.put("timeMs", m.reasoningTimeMs)
        obj.put("timeSec", m.reasoningTimeMs / 1000.0)
        obj.put("tokens", m.tokenUsage)
        obj.put("level", m.thinkingLevel)
        obj.put("steps", m.stepCount)
        obj.put("isCache", m.isCacheHit)
        jsonArray.put(obj)
    }

    val dataJsonString = jsonArray.toString()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
        body {
            background-color: #1a1b1f;
            color: #E2E2E6;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            overflow: hidden;
            width: 100%;
            height: 100%;
        }
        #chart-container {
            width: 100%;
            height: 100%;
            position: relative;
        }
        svg {
            width: 100%;
            height: 100%;
            display: block;
        }
        .axis text {
            fill: #8E918F;
            font-size: 9px;
            font-family: monospace;
        }
        .axis line, .axis path {
            stroke: #2E3036;
            stroke-width: 0.8px;
        }
        .grid-line {
            stroke: #24262b;
            stroke-dasharray: 2, 2;
        }
        .bar {
            cursor: pointer;
            transition: opacity 0.2s;
        }
        .bar:hover {
            opacity: 0.85;
        }
        .line-path {
            fill: none;
            stroke: #00E5FF;
            stroke-width: 2.5px;
            stroke-linejoin: round;
        }
        .dot {
            fill: #00E5FF;
            stroke: #1a1b1f;
            stroke-width: 2px;
            cursor: pointer;
            transition: r 0.2s;
        }
        .dot:hover {
            r: 6;
        }
        .tooltip {
            position: absolute;
            background: rgba(30, 31, 36, 0.95);
            border: 1px solid #00E5FF;
            border-radius: 6px;
            padding: 6px 10px;
            font-size: 10px;
            color: #fff;
            pointer-events: none;
            opacity: 0;
            transition: opacity 0.15s;
            box-shadow: 0 4px 12px rgba(0,0,0,0.5);
            z-index: 100;
        }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
</head>
<body>
    <div id="chart-container">
        <div id="tooltip" class="tooltip"></div>
    </div>

    <script>
    (function() {
        const rawData = $dataJsonString;
        const container = document.getElementById('chart-container');
        const tooltip = document.getElementById('tooltip');
        const mode = '$chartMode';

        function render() {
            container.querySelectorAll('svg').forEach(el => el.remove());

            const width = container.clientWidth || 360;
            const height = container.clientHeight || 260;
            const margin = { top: 20, right: 45, bottom: 35, left: 45 };
            const innerWidth = width - margin.left - margin.right;
            const innerHeight = height - margin.top - margin.bottom;

            if (window.d3) {
                renderWithD3(width, height, innerWidth, innerHeight, margin);
            } else {
                renderNativeSvg(width, height, innerWidth, innerHeight, margin);
            }
        }

        function renderWithD3(width, height, innerWidth, innerHeight, margin) {
            const svg = d3.select('#chart-container')
                .append('svg')
                .attr('viewBox', '0 0 ' + width + ' ' + height)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

            // Defs for gradients
            const defs = svg.append('defs');
            const tokenGrad = defs.append('linearGradient')
                .attr('id', 'token-grad')
                .attr('x1', '0%').attr('y1', '0%')
                .attr('x2', '0%').attr('y2', '100%');
            tokenGrad.append('stop').attr('offset', '0%').attr('stop-color', '#00E676').attr('stop-opacity', 0.9);
            tokenGrad.append('stop').attr('offset', '100%').attr('stop-color', '#00B0FF').attr('stop-opacity', 0.3);

            if (mode === 'scatter') {
                // SCATTER PLOT: X = Reasoning Time (ms), Y = Token Usage
                const xMax = d3.max(rawData, d => d.timeMs) * 1.15 || 5000;
                const yMax = d3.max(rawData, d => d.tokens) * 1.15 || 6000;

                const xScale = d3.scaleLinear().domain([0, xMax]).range([0, innerWidth]);
                const yScale = d3.scaleLinear().domain([0, yMax]).range([innerHeight, 0]);

                // X & Y Gridlines
                svg.append('g').selectAll('.grid-line').data(yScale.ticks(4)).enter()
                    .append('line').attr('class', 'grid-line')
                    .attr('x1', 0).attr('x2', innerWidth)
                    .attr('y1', d => yScale(d)).attr('y2', d => yScale(d));

                // Axes
                svg.append('g').attr('class', 'axis').attr('transform', 'translate(0,' + innerHeight + ')')
                    .call(d3.axisBottom(xScale).ticks(5).tickFormat(d => (d/1000).toFixed(1) + 's'));

                svg.append('g').attr('class', 'axis')
                    .call(d3.axisLeft(yScale).ticks(5).tickFormat(d => (d/1000).toFixed(0) + 'k'));

                // Axis labels
                svg.append('text').attr('x', innerWidth / 2).attr('y', innerHeight + 30)
                    .attr('fill', '#00E5FF').attr('font-size', '9px').attr('text-anchor', 'middle')
                    .text('Reasoning Latency (seconds)');

                svg.append('text').attr('transform', 'rotate(-90)').attr('y', -32).attr('x', -innerHeight / 2)
                    .attr('fill', '#00E676').attr('font-size', '9px').attr('text-anchor', 'middle')
                    .text('Tokens Used');

                // Scatter Points
                svg.selectAll('.scatter-dot')
                    .data(rawData)
                    .enter()
                    .append('circle')
                    .attr('cx', d => xScale(d.timeMs))
                    .attr('cy', d => yScale(d.tokens))
                    .attr('r', d => Math.max(4, Math.min(10, d.steps * 1.5)))
                    .attr('fill', d => d.isCache ? '#FFD700' : '#00E5FF')
                    .attr('stroke', '#7C4DFF')
                    .attr('stroke-width', 1.5)
                    .style('opacity', 0.85)
                    .on('click', (evt, d) => {
                        if (window.AndroidBridge) window.AndroidBridge.onTraceClicked(d.id);
                    })
                    .on('mouseover', (evt, d) => {
                        tooltip.style.opacity = '1';
                        tooltip.innerHTML = '<b>' + d.title + '</b><br>Time: ' + d.timeMs + ' ms<br>Tokens: ' + d.tokens + '<br>Steps: ' + d.steps;
                        tooltip.style.left = (evt.pageX + 10) + 'px';
                        tooltip.style.top = (evt.pageY - 25) + 'px';
                    })
                    .on('mouseout', () => { tooltip.style.opacity = '0'; });

            } else {
                // DUAL AXIS: X = Categories, Y1 (Left) = Time, Y2 (Right) = Tokens
                const x0 = d3.scaleBand()
                    .domain(rawData.map((d, i) => i))
                    .range([0, innerWidth])
                    .padding(0.35);

                const maxTokens = d3.max(rawData, d => d.tokens) * 1.2 || 6000;
                const maxTime = d3.max(rawData, d => d.timeMs) * 1.2 || 5000;

                const yTokens = d3.scaleLinear().domain([0, maxTokens]).range([innerHeight, 0]);
                const yTime = d3.scaleLinear().domain([0, maxTime]).range([innerHeight, 0]);

                // Gridlines
                svg.append('g').selectAll('.grid-line').data(yTime.ticks(4)).enter()
                    .append('line').attr('class', 'grid-line')
                    .attr('x1', 0).attr('x2', innerWidth)
                    .attr('y1', d => yTime(d)).attr('y2', d => yTime(d));

                // Left Axis: Time (Cyan)
                svg.append('g').attr('class', 'axis')
                    .call(d3.axisLeft(yTime).ticks(4).tickFormat(d => (d/1000).toFixed(1) + 's'));

                // Right Axis: Tokens (Emerald)
                svg.append('g').attr('class', 'axis').attr('transform', 'translate(' + innerWidth + ',0)')
                    .call(d3.axisRight(yTokens).ticks(4).tickFormat(d => (d/1000).toFixed(0) + 'k'));

                // Bottom Axis: Indices
                svg.append('g').attr('class', 'axis').attr('transform', 'translate(0,' + innerHeight + ')')
                    .call(d3.axisBottom(x0).tickFormat((d, i) => '#' + (i + 1)));

                // Bars: Token Usage
                svg.selectAll('.bar')
                    .data(rawData)
                    .enter()
                    .append('rect')
                    .attr('class', 'bar')
                    .attr('x', (d, i) => x0(i))
                    .attr('y', d => yTokens(d.tokens))
                    .attr('width', x0.bandwidth())
                    .attr('height', d => innerHeight - yTokens(d.tokens))
                    .attr('rx', 4)
                    .attr('fill', 'url(#token-grad)')
                    .on('click', (evt, d) => {
                        if (window.AndroidBridge) window.AndroidBridge.onTraceClicked(d.id);
                    })
                    .on('mouseover', (evt, d) => {
                        tooltip.style.opacity = '1';
                        tooltip.innerHTML = '<b>' + d.title + '</b><br>Tokens: ' + d.tokens + '<br>Time: ' + d.timeMs + ' ms';
                        tooltip.style.left = (evt.pageX + 8) + 'px';
                        tooltip.style.top = (evt.pageY - 20) + 'px';
                    })
                    .on('mouseout', () => { tooltip.style.opacity = '0'; });

                // Line Generator for Reasoning Time
                const lineGen = d3.line()
                    .x((d, i) => x0(i) + x0.bandwidth() / 2)
                    .y(d => yTime(d.timeMs))
                    .curve(d3.curveMonotoneX);

                svg.append('path')
                    .datum(rawData)
                    .attr('class', 'line-path')
                    .attr('d', lineGen);

                // Line Dots
                svg.selectAll('.dot')
                    .data(rawData)
                    .enter()
                    .append('circle')
                    .attr('class', 'dot')
                    .attr('cx', (d, i) => x0(i) + x0.bandwidth() / 2)
                    .attr('cy', d => yTime(d.timeMs))
                    .attr('r', 4)
                    .on('click', (evt, d) => {
                        if (window.AndroidBridge) window.AndroidBridge.onTraceClicked(d.id);
                    })
                    .on('mouseover', (evt, d) => {
                        tooltip.style.opacity = '1';
                        tooltip.innerHTML = '<b>' + d.title + '</b><br>Reasoning Time: ' + d.timeMs + ' ms<br>Tokens: ' + d.tokens;
                        tooltip.style.left = (evt.pageX + 8) + 'px';
                        tooltip.style.top = (evt.pageY - 20) + 'px';
                    })
                    .on('mouseout', () => { tooltip.style.opacity = '0'; });
            }
        }

        // Resilient Fallback SVG in case CDN script is blocked or offline
        function renderNativeSvg(width, height, innerWidth, innerHeight, margin) {
            const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            svg.setAttribute('viewBox', '0 0 ' + width + ' ' + height);

            const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            g.setAttribute('transform', 'translate(' + margin.left + ',' + margin.top + ')');
            svg.appendChild(g);

            const maxTokens = Math.max(...rawData.map(d => d.tokens), 5000) * 1.2;
            const maxTime = Math.max(...rawData.map(d => d.timeMs), 4000) * 1.2;
            const barWidth = innerWidth / rawData.length * 0.6;

            let pathPoints = [];

            rawData.forEach((d, i) => {
                const x = (i * (innerWidth / rawData.length)) + (innerWidth / rawData.length * 0.2);
                const barHeight = (d.tokens / maxTokens) * innerHeight;
                const yBar = innerHeight - barHeight;

                const rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
                rect.setAttribute('x', x);
                rect.setAttribute('y', yBar);
                rect.setAttribute('width', barWidth);
                rect.setAttribute('height', barHeight);
                rect.setAttribute('rx', '4');
                rect.setAttribute('fill', '#00E676');
                rect.setAttribute('opacity', '0.7');
                rect.onclick = function() { if (window.AndroidBridge) window.AndroidBridge.onTraceClicked(d.id); };
                g.appendChild(rect);

                const yTime = innerHeight - (d.timeMs / maxTime * innerHeight);
                const cx = x + barWidth / 2;
                pathPoints.push(cx + ',' + yTime);

                const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
                circle.setAttribute('cx', cx);
                circle.setAttribute('cy', yTime);
                circle.setAttribute('r', '4');
                circle.setAttribute('fill', '#00E5FF');
                circle.onclick = function() { if (window.AndroidBridge) window.AndroidBridge.onTraceClicked(d.id); };
                g.appendChild(circle);
            });

            if (pathPoints.length > 1) {
                const polyline = document.createElementNS('http://www.w3.org/2000/svg', 'polyline');
                polyline.setAttribute('points', pathPoints.join(' '));
                polyline.setAttribute('fill', 'none');
                polyline.setAttribute('stroke', '#00E5FF');
                polyline.setAttribute('stroke-width', '2.5');
                g.insertBefore(polyline, g.firstChild);
            }

            container.appendChild(svg);
        }

        render();
        window.addEventListener('resize', render);
    })();
    </script>
</body>
</html>
    """.trimIndent()
}
