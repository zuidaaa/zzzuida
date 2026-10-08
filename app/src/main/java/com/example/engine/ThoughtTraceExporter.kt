package com.example.engine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ThoughtTraceExportData(
    val prompt: String,
    val thoughtProcess: String,
    val reasoningSteps: List<ReasoningStep> = emptyList(),
    val finalResponse: String,
    val modelName: String = "Gemini 3.7 Deep Thinking",
    val thinkingDurationMs: Long = 0L,
    val thinkingTokens: Int = 0,
    val searchCitations: List<SearchCitation> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

object ThoughtTraceExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Generates a richly formatted Markdown document containing the user query,
     * the complete cognitive thought trace (with step breakdown), citations, and the final response.
     */
    fun generateMarkdown(data: ThoughtTraceExportData): String {
        val dateStr = dateFormat.format(Date(data.timestamp))
        val durationSec = String.format(Locale.US, "%.2f", data.thinkingDurationMs / 1000f)

        val sb = StringBuilder()
        sb.appendLine("# 🧠 Deep Thinking Reasoning & Solution Report")
        sb.appendLine("> **Model**: ${data.modelName}")
        sb.appendLine("> **Generated**: $dateStr")
        sb.appendLine("> **Thinking Duration**: ${data.thinkingDurationMs} ms (${durationSec}s) | **Token Budget**: ${data.thinkingTokens} tokens")
        if (data.searchCitations.isNotEmpty()) {
            sb.appendLine("> **Search Grounding**: ${data.searchCitations.size} verified citations")
        }
        sb.appendLine("\n---\n")

        // Prompt
        sb.appendLine("## ❓ User Prompt / Task")
        sb.appendLine("```")
        sb.appendLine(data.prompt.ifBlank { "Universal Proof & Logic Synthesis Task" })
        sb.appendLine("```\n")

        // Thought Trace
        sb.appendLine("## 🔬 Cognitive Thought Trace")
        if (data.thoughtProcess.isNotBlank()) {
            sb.appendLine("### Step-by-Step Internal Reasoning")
            sb.appendLine(data.thoughtProcess)
            sb.appendLine()
        }

        // Structured Reasoning Steps
        if (data.reasoningSteps.isNotEmpty()) {
            sb.appendLine("### 📋 Verified Chain-of-Thought Steps")
            data.reasoningSteps.forEachIndexed { index, step ->
                val statusEmoji = if (step.verified) "✅" else "🔹"
                val confPercent = (step.confidence * 100).toInt()
                sb.appendLine("${index + 1}. **${step.headline}** $statusEmoji `[${step.phase.badge} | Confidence: $confPercent% | ${step.latencyMs}ms]`")
                if (step.details.isNotBlank()) {
                    sb.appendLine("   - *Details*: ${step.details}")
                }
                if (step.branchLabel.isNotBlank()) {
                    sb.appendLine("   - *Branch*: `${step.branchLabel}`")
                }
                if (step.thoughtPayload.isNotBlank()) {
                    sb.appendLine("   - *Deduction*: ${step.thoughtPayload}")
                }
            }
            sb.appendLine()
        }

        // Citations
        if (data.searchCitations.isNotEmpty()) {
            sb.appendLine("## 🌐 Verified Search Citations")
            data.searchCitations.forEachIndexed { i, c ->
                val linkUrl = if (c.url.isNotBlank()) c.url else "https://${c.sourceDomain}"
                sb.appendLine("${i + 1}. [${c.title}]($linkUrl) — *${c.snippet}* (${c.freshness})")
            }
            sb.appendLine()
        }

        // Final Response
        sb.appendLine("## 💡 Final AI Response & Validated Output")
        sb.appendLine(data.finalResponse.ifBlank { "*(Response empty or streaming)*" })
        sb.appendLine("\n---\n")
        sb.appendLine("*Exported via Gemini 3.7 Deep Thinking Mobile Engine on Samsung S26 Ultra*")

        return sb.toString()
    }

    /**
     * Saves Markdown to a cache file and returns the shareable File.
     */
    fun saveMarkdownFile(context: Context, data: ThoughtTraceExportData): File {
        val fileName = "thought_trace_${fileDateFormat.format(Date(data.timestamp))}.md"
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(cacheDir, fileName)
        val content = generateMarkdown(data)
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    /**
     * Generates a multi-page, formatted PDF document containing the prompt,
     * thought trace card, and final response with clean typography and layout.
     */
    fun generatePdfFile(context: Context, data: ThoughtTraceExportData): File {
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        val margin = 36f
        val contentWidth = (pageWidth - (margin * 2)).toInt()

        val pdfDocument = PdfDocument()
        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Paint definitions
        val primaryPaint = Paint().apply {
            color = Color.rgb(37, 99, 235) // DeepThink Indigo / Blue
            isAntiAlias = true
        }

        val textPaint = TextPaint().apply {
            color = Color.rgb(30, 41, 59) // Dark Slate
            textSize = 10.5f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val boldTextPaint = TextPaint().apply {
            color = Color.rgb(15, 23, 42) // Dark Navy
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val headerTitlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 15f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val subHeaderPaint = TextPaint().apply {
            color = Color.rgb(224, 231, 255)
            textSize = 9.5f
            isAntiAlias = true
        }

        val sectionHeaderPaint = TextPaint().apply {
            color = Color.rgb(30, 58, 138)
            textSize = 12f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val thoughtTextPaint = TextPaint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val cardBgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            isAntiAlias = true
        }

        val cardBorderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val thoughtCardBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            isAntiAlias = true
        }

        val footerPaint = TextPaint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8.5f
            isAntiAlias = true
        }

        var cursorY = margin

        // Function to draw header banner
        fun drawPageHeader(c: Canvas) {
            val bannerHeight = 54f
            val bannerRect = RectF(margin, margin, pageWidth - margin, margin + bannerHeight)
            val cornerRadius = 8f

            c.drawRoundRect(bannerRect, cornerRadius, cornerRadius, primaryPaint)

            c.drawText("🧠 Gemini 3.7 Deep Thinking Report", margin + 14f, margin + 22f, headerTitlePaint)
            val meta = "Model: ${data.modelName}  •  Thinking: ${data.thinkingDurationMs}ms (${data.thinkingTokens} tokens)  •  ${dateFormat.format(Date(data.timestamp))}"
            c.drawText(meta, margin + 14f, margin + 40f, subHeaderPaint)

            cursorY = margin + bannerHeight + 16f
        }

        // Function to draw footer
        fun drawPageFooter(c: Canvas, pageNum: Int) {
            val footerY = pageHeight - 20f
            c.drawLine(margin, footerY - 10f, pageWidth - margin, footerY - 10f, cardBorderPaint)
            c.drawText("Generated via Deep Thinking Mobile Engine • Page $pageNum", margin, footerY, footerPaint)
            val dateStr = dateFormat.format(Date(data.timestamp))
            val dateWidth = footerPaint.measureText(dateStr)
            c.drawText(dateStr, pageWidth - margin - dateWidth, footerY, footerPaint)
        }

        // Check page overflow helper
        fun checkAndCreateNewPage(requiredHeight: Float) {
            if (cursorY + requiredHeight > pageHeight - 40f) {
                drawPageFooter(canvas, currentPageNumber)
                pdfDocument.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                drawPageHeader(canvas)
            }
        }

        // Start Page 1 Header
        drawPageHeader(canvas)

        // 1. User Prompt Section
        val promptText = data.prompt.ifBlank { "Universal Proof & Multi-Node Logic Synthesis Task" }
        val promptLayout = StaticLayout.Builder.obtain(promptText, 0, promptText.length, textPaint, contentWidth - 20)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .build()

        val promptCardHeight = promptLayout.height + 34f
        checkAndCreateNewPage(promptCardHeight + 20f)

        canvas.drawText("1. User Task / Problem Statement", margin, cursorY + 12f, sectionHeaderPaint)
        cursorY += 20f

        val promptCardRect = RectF(margin, cursorY, pageWidth - margin, cursorY + promptCardHeight)
        canvas.drawRoundRect(promptCardRect, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(promptCardRect, 6f, 6f, cardBorderPaint)

        canvas.save()
        canvas.translate(margin + 10f, cursorY + 10f)
        promptLayout.draw(canvas)
        canvas.restore()

        cursorY += promptCardHeight + 16f

        // 2. Thought Trace Section
        val thoughtStr = buildString {
            if (data.thoughtProcess.isNotBlank()) {
                append(data.thoughtProcess)
                append("\n\n")
            }
            if (data.reasoningSteps.isNotEmpty()) {
                append("--- Reasoning Step Proof Nodes ---\n")
                data.reasoningSteps.forEachIndexed { i, s ->
                    append("${i + 1}. [${s.headline}] (${(s.confidence * 100).toInt()}% conf, ${s.latencyMs}ms)\n")
                    if (s.details.isNotBlank()) {
                        append("   -> ${s.details}\n")
                    }
                }
            }
        }.ifBlank { "Autonomous Deep Thinking internal CoT execution verified." }

        canvas.drawText("2. Cognitive Thought Trace & Proof Breakdown", margin, cursorY + 12f, sectionHeaderPaint)
        cursorY += 22f

        val thoughtLayout = StaticLayout.Builder.obtain(thoughtStr, 0, thoughtStr.length, thoughtTextPaint, contentWidth - 20)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .build()

        val thoughtCardHeight = thoughtLayout.height + 24f
        checkAndCreateNewPage(Math.min(thoughtCardHeight, 200f))

        val thoughtCardRect = RectF(margin, cursorY, pageWidth - margin, cursorY + thoughtCardHeight)
        canvas.drawRoundRect(thoughtCardRect, 6f, 6f, thoughtCardBgPaint)
        canvas.drawRoundRect(thoughtCardRect, 6f, 6f, cardBorderPaint)

        canvas.save()
        canvas.translate(margin + 10f, cursorY + 10f)
        thoughtLayout.draw(canvas)
        canvas.restore()

        cursorY += thoughtCardHeight + 16f

        // 3. Final AI Response Section
        val respStr = data.finalResponse.ifBlank { "Solution synthesis complete." }
        val respLayout = StaticLayout.Builder.obtain(respStr, 0, respStr.length, textPaint, contentWidth - 20)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2.5f, 1.05f)
            .build()

        val respCardHeight = respLayout.height + 30f
        checkAndCreateNewPage(Math.min(respCardHeight, 200f))

        canvas.drawText("3. Final AI Solution & Verified Output", margin, cursorY + 12f, sectionHeaderPaint)
        cursorY += 22f

        val respCardRect = RectF(margin, cursorY, pageWidth - margin, cursorY + respCardHeight)
        canvas.drawRoundRect(respCardRect, 6f, 6f, cardBgPaint)
        canvas.drawRoundRect(respCardRect, 6f, 6f, cardBorderPaint)

        canvas.save()
        canvas.translate(margin + 10f, cursorY + 10f)
        respLayout.draw(canvas)
        canvas.restore()

        cursorY += respCardHeight + 20f

        // Final page footer
        drawPageFooter(canvas, currentPageNumber)
        pdfDocument.finishPage(page)

        // Write to file
        val fileName = "thought_trace_${fileDateFormat.format(Date(data.timestamp))}.pdf"
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        pdfDocument.close()

        return file
    }

    /**
     * Obtains a secure content URI using FileProvider.
     */
    fun getFileUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Triggers the Android system share sheet for Markdown / PDF documents.
     */
    fun shareDocument(context: Context, file: File, mimeType: String, title: String = "Export Thought Trace") {
        val uri = getFileUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Copies Markdown text to the clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Thought Trace Markdown") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
