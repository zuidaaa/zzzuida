package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class AudioTranscriptSegment(
    val speaker: String,
    val text: String,
    val timestampMs: Long,
    val confidence: Float = 0.95f
)

data class TranscriptionState(
    val isRecording: Boolean = false,
    val isProcessingFile: Boolean = false,
    val recordingDurationSec: Int = 0,
    val liveAmplitudes: List<Float> = List(32) { 0.05f },
    val fullTranscriptText: String = "",
    val partialTranscriptText: String = "",
    val segments: List<AudioTranscriptSegment> = emptyList(),
    val selectedLanguage: String = "English (US)",
    val selectedLanguageCode: String = "en-US",
    val wordCount: Int = 0,
    val estimatedAudioQuality: String = "High (48kHz 24-bit)",
    val errorMessage: String? = null
)

class AudioTranscriptionManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "AudioTranscriptionMgr"

    private val _state = MutableStateFlow(TranscriptionState())
    val state: StateFlow<TranscriptionState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingTimerJob: Job? = null
    private var waveformVisualizerJob: Job? = null

    val supportedLanguages = listOf(
        "English (US)" to "en-US",
        "English (UK)" to "en-GB",
        "Korean (한국어)" to "ko-KR",
        "German (Deutsch)" to "de-DE",
        "French (Français)" to "fr-FR",
        "Spanish (Español)" to "es-ES",
        "Japanese (日本語)" to "ja-JP",
        "Chinese (Mandarin)" to "zh-CN"
    )

    fun setLanguage(name: String, code: String) {
        _state.update { it.copy(selectedLanguage = name, selectedLanguageCode = code) }
    }

    fun startLiveRecording() {
        stopLiveRecording()
        coroutineScope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, _state.value.selectedLanguageCode)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _state.update {
                            it.copy(
                                isRecording = true,
                                errorMessage = null,
                                recordingDurationSec = 0
                            )
                        }
                        startTimerAndWaveform()
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        updateWaveform(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {}

                    override fun onError(error: Int) {
                        Log.w(TAG, "Speech recognition error in transcriber: $error")
                        // If error occurred during live recording, don't abruptly wipe existing segments
                        _state.update {
                            it.copy(
                                isRecording = false,
                                errorMessage = if (error == SpeechRecognizer.ERROR_NO_MATCH) "No speech detected in audio window." else "Transcription warning: code $error"
                            )
                        }
                        stopTimerAndWaveform()
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            val newSegments = _state.value.segments.toMutableList().apply {
                                add(
                                    AudioTranscriptSegment(
                                        speaker = "Speaker ${(_state.value.segments.size % 2) + 1}",
                                        text = text,
                                        timestampMs = System.currentTimeMillis(),
                                        confidence = 0.96f
                                    )
                                )
                            }
                            val fullText = buildString {
                                newSegments.forEach { seg ->
                                    appendLine("[${seg.speaker}]: ${seg.text}")
                                }
                            }.trim()

                            _state.update {
                                it.copy(
                                    isRecording = false,
                                    partialTranscriptText = "",
                                    fullTranscriptText = fullText,
                                    segments = newSegments,
                                    wordCount = fullText.split("\\s+".toRegex()).size
                                )
                            }
                        } else {
                            _state.update { it.copy(isRecording = false) }
                        }
                        stopTimerAndWaveform()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        _state.update { it.copy(partialTranscriptText = partial) }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting live transcriber", e)
                _state.update {
                    it.copy(
                        isRecording = false,
                        errorMessage = "Microphone recording unavailable: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun stopLiveRecording() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping transcriber listener", e)
        }
        stopTimerAndWaveform()
        _state.update { it.copy(isRecording = false) }
    }

    fun clearTranscript() {
        _state.update {
            it.copy(
                fullTranscriptText = "",
                partialTranscriptText = "",
                segments = emptyList(),
                wordCount = 0,
                errorMessage = null
            )
        }
    }

    fun loadSampleAudioTranscript(sampleType: String) {
        _state.update { it.copy(isProcessingFile = true) }
        coroutineScope.launch {
            delay(500)
            val segments = when (sampleType) {
                "Algorithms" -> listOf(
                    AudioTranscriptSegment("Lecturer", "Today we deconstruct cache-oblivious B-trees and examine memory locality invariants.", 0L, 0.98f),
                    AudioTranscriptSegment("Student", "How does the search complexity compare with classical red-black trees under L1 cache misses?", 4200L, 0.94f),
                    AudioTranscriptSegment("Lecturer", "Under van Emde Boas layout, cache miss complexity drops to O(log_B N) without needing explicit block size parameters.", 8900L, 0.99f)
                )
                "Mathematics" -> listOf(
                    AudioTranscriptSegment("Researcher", "Let f be a holomorphic function on an open subset of the complex plane.", 0L, 0.99f),
                    AudioTranscriptSegment("Researcher", "By Cauchy's Integral Theorem, the closed contour integral vanishes along any simply connected domain.", 5100L, 0.97f),
                    AudioTranscriptSegment("Researcher", "We can now deduce the maximum modulus principle as an immediate corollary.", 10200L, 0.98f)
                )
                "Distributed Systems" -> listOf(
                    AudioTranscriptSegment("Lead Architect", "In our Raft consensus cluster, leader election timeout is set to 150 to 300 milliseconds.", 0L, 0.97f),
                    AudioTranscriptSegment("SRE Engineer", "What happens during network partition if the minority cluster receives client write requests?", 6000L, 0.95f),
                    AudioTranscriptSegment("Lead Architect", "The minority cluster cannot reach quorum, rejects client writes or buffers idempotently, preserving strict safety invariants.", 12400L, 0.99f)
                )
                else -> listOf(
                    AudioTranscriptSegment("Speaker 1", "We need to verify all invariant proofs across our system architecture.", 0L, 0.96f),
                    AudioTranscriptSegment("Speaker 2", "Let us run the cognitive reasoning engine to deconstruct boundary conditions.", 4500L, 0.97f)
                )
            }

            val fullText = buildString {
                segments.forEach { seg ->
                    appendLine("[${seg.speaker}]: ${seg.text}")
                }
            }.trim()

            _state.update {
                it.copy(
                    isProcessingFile = false,
                    fullTranscriptText = fullText,
                    segments = segments,
                    wordCount = fullText.split("\\s+".toRegex()).size,
                    errorMessage = null
                )
            }
        }
    }

    private fun startTimerAndWaveform() {
        recordingTimerJob?.cancel()
        recordingTimerJob = coroutineScope.launch {
            var secs = 0
            while (true) {
                delay(1000)
                secs += 1
                _state.update { it.copy(recordingDurationSec = secs) }
            }
        }

        waveformVisualizerJob?.cancel()
        waveformVisualizerJob = coroutineScope.launch {
            while (true) {
                val current = _state.value.liveAmplitudes.toMutableList()
                current.removeAt(0)
                val newAmp = (0.05f + (Math.random() * 0.45).toFloat()).coerceIn(0.05f, 1.0f)
                current.add(newAmp)
                _state.update { it.copy(liveAmplitudes = current) }
                delay(80)
            }
        }
    }

    private fun updateWaveform(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 14f).coerceIn(0.08f, 1.0f)
        val current = _state.value.liveAmplitudes.toMutableList()
        if (current.isNotEmpty()) {
            current[current.size - 1] = normalized
            _state.update { it.copy(liveAmplitudes = current) }
        }
    }

    private fun stopTimerAndWaveform() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        waveformVisualizerJob?.cancel()
        waveformVisualizerJob = null
    }

    fun release() {
        stopLiveRecording()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
    }
}
