package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.LlmPerformancePoint
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Live-updating D3.js Line Chart for LLM Performance Telemetry.
 * Visualizes Latency (ms) and Throughput (tokens/sec) over time.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3PerformanceChart(
    points: List<LlmPerformancePoint>,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(points) {
        generateD3PerformanceHtml(points)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("d3_performance_chart"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, GeminiCyan.copy(alpha = 0.35f))
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(0)
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true
                    }
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        )
    }
}

private fun generateD3PerformanceHtml(points: List<LlmPerformancePoint>): String {
    val jsonArray = JSONArray()
    points.forEachIndexed { index, p ->
        val obj = JSONObject()
        obj.put("x", index)
        obj.put("latency", p.latencyMs)
        obj.put("throughput", p.tokensPerSec)
        obj.put("agent", p.agent)
        jsonArray.put(obj)
    }

    val dataJson = jsonArray.toString()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        body { background: transparent; margin: 0; padding: 0; overflow: hidden; font-family: sans-serif; }
        #chart { width: 100%; height: 100vh; }
        .axis text { fill: #8E918F; font-size: 10px; }
        .axis path, .axis line { stroke: #2E3036; }
        .line-latency { fill: none; stroke: #00E5FF; stroke-width: 2.5; }
        .line-throughput { fill: none; stroke: #8B5CF6; stroke-width: 2.5; }
        .area-latency { fill: #00E5FF; fill-opacity: 0.1; }
        .grid line { stroke: #24262b; stroke-opacity: 0.7; shape-rendering: crispEdges; }
        .grid path { stroke-width: 0; }
        .dot-latency { fill: #00E5FF; stroke: #1a1b1f; stroke-width: 1.5; }
        .dot-throughput { fill: #8B5CF6; stroke: #1a1b1f; stroke-width: 1.5; }
    </style>
    <script src="https://cdn.jsdelivr.net/npm/d3@7"></script>
</head>
<body>
    <div id="chart"></div>
    <script>
        const data = $dataJson;
        const container = document.getElementById('chart');
        const width = container.clientWidth;
        const height = container.clientHeight;
        const margin = {top: 20, right: 40, bottom: 30, left: 40};
        const innerWidth = width - margin.left - margin.right;
        const innerHeight = height - margin.top - margin.bottom;

        const svg = d3.select('#chart').append('svg')
            .attr('width', width)
            .attr('height', height)
            .append('g')
            .attr('transform', `translate(${'$'}{margin.left},${'$'}{margin.top})`);

        if (data.length > 0) {
            const x = d3.scaleLinear()
                .domain([0, Math.max(data.length - 1, 10)])
                .range([0, innerWidth]);

            const yLatency = d3.scaleLinear()
                .domain([0, d3.max(data, d => d.latency) * 1.2 || 1000])
                .range([innerHeight, 0]);

            const yThroughput = d3.scaleLinear()
                .domain([0, d3.max(data, d => d.throughput) * 1.2 || 50])
                .range([innerHeight, 0]);

            // Gridlines
            svg.append('g').attr('class', 'grid')
                .call(d3.axisLeft(yLatency).ticks(5).tickSize(-innerWidth).tickFormat(''));

            // Axes
            svg.append('g').attr('class', 'axis')
                .attr('transform', `translate(0, ${'$'}{innerHeight})`)
                .call(d3.axisBottom(x).ticks(5).tickFormat(d => 'n-' + (data.length - d)));

            svg.append('g').attr('class', 'axis')
                .call(d3.axisLeft(yLatency).ticks(5));

            svg.append('g').attr('class', 'axis')
                .attr('transform', `translate(${'$'}{innerWidth}, 0)`)
                .call(d3.axisRight(yThroughput).ticks(5));

            // Latency Line
            const latencyLine = d3.line()
                .x(d => x(d.x))
                .y(d => yLatency(d.latency))
                .curve(d3.curveMonotoneX);

            svg.append('path')
                .datum(data)
                .attr('class', 'line-latency')
                .attr('d', latencyLine);

            // Throughput Line
            const throughputLine = d3.line()
                .x(d => x(d.x))
                .y(d => yThroughput(d.throughput))
                .curve(d3.curveMonotoneX);

            svg.append('path')
                .datum(data)
                .attr('class', 'line-throughput')
                .attr('d', throughputLine);

            // Labels
            svg.append('text').attr('x', 5).attr('y', -5).attr('fill', '#00E5FF').style('font-size', '9px').text('Latency (ms)');
            svg.append('text').attr('x', innerWidth).attr('y', -5).attr('text-anchor', 'end').attr('fill', '#8B5CF6').style('font-size', '9px').text('Tokens/sec');
        }
    </script>
</body>
</html>
    """.trimIndent()
}
