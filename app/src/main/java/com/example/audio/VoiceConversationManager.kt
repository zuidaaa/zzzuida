package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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

enum class VoiceConversationState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class VoiceState(
    val conversationState: VoiceConversationState = VoiceConversationState.IDLE,
    val userSpokenText: String = "",
    val partialSpokenText: String = "",
    val modelSpokenText: String = "",
    val currentReasoningStage: String = "",
    val currentThinkingNode: String = "",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val rmsDecibels: Float = 0f,
    val isAutoListenLoopEnabled: Boolean = true,
    val isMuted: Boolean = false,
    val isTtsReady: Boolean = false,
    val errorMessage: String? = null
)

class VoiceConversationManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val TAG = "VoiceConvManager"

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isListeningActive = false
    private var amplitudeSimulationJob: Job? = null

    var onUserQuerySpoken: ((String) -> Unit)? = null

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.let { tts ->
                    val result = tts.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setLanguage(Locale.getDefault())
                    }
                    tts.setSpeechRate(_voiceState.value.speechRate)
                    tts.setPitch(_voiceState.value.speechPitch)

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _voiceState.update { it.copy(conversationState = VoiceConversationState.SPEAKING) }
                            startAmplitudeSimulation()
                        }

                        override fun onDone(utteranceId: String?) {
                            stopAmplitudeSimulation()
                            _voiceState.update { it.copy(conversationState = VoiceConversationState.IDLE, rmsDecibels = 0f) }
                            if (_voiceState.value.isAutoListenLoopEnabled && !isListeningActive) {
                                coroutineScope.launch(Dispatchers.Main) {
                                    delay(400)
                                    startListening()
                                }
                            }
                        }

                        override fun onError(utteranceId: String?) {
                            stopAmplitudeSimulation()
                            _voiceState.update { it.copy(conversationState = VoiceConversationState.IDLE, rmsDecibels = 0f) }
                        }
                    })
                    _voiceState.update { it.copy(isTtsReady = true) }
                }
            } else {
                Log.e(TAG, "TTS Initialization failed")
            }
        }
    }

    fun startListening() {
        if (_voiceState.value.isMuted) return
        stopSpeaking()

        coroutineScope.launch(Dispatchers.Main) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 900L)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListeningActive = true
                        _voiceState.update {
                            it.copy(
                                conversationState = VoiceConversationState.LISTENING,
                                errorMessage = null,
                                partialSpokenText = ""
                            )
                        }
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(rmsdB: Float) {
                        _voiceState.update { it.copy(rmsDecibels = (rmsdB + 2f).coerceAtLeast(0f)) }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListeningActive = false
                    }

                    override fun onError(error: Int) {
                        isListeningActive = false
                        val errorText = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue for cloud speech (fallback enabled)"
                            else -> "Speech recognition code: $error"
                        }
                        _voiceState.update {
                            it.copy(
                                conversationState = VoiceConversationState.IDLE,
                                rmsDecibels = 0f,
                                errorMessage = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) null else errorText
                            )
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isListeningActive = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotBlank()) {
                            _voiceState.update {
                                it.copy(
                                    userSpokenText = text,
                                    partialSpokenText = "",
                                    conversationState = VoiceConversationState.THINKING,
                                    rmsDecibels = 0f
                                )
                            }
                            onUserQuerySpoken?.invoke(text)
                        } else {
                            _voiceState.update { it.copy(conversationState = VoiceConversationState.IDLE) }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull() ?: ""
                        _voiceState.update { it.copy(partialSpokenText = partial) }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting speech recognizer", e)
                _voiceState.update {
                    it.copy(
                        conversationState = VoiceConversationState.IDLE,
                        errorMessage = "Speech service error: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun stopListening() {
        try {
            isListeningActive = false
            speechRecognizer?.stopListening()
            _voiceState.update {
                if (it.conversationState == VoiceConversationState.LISTENING) {
                    it.copy(conversationState = VoiceConversationState.IDLE, rmsDecibels = 0f)
                } else it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener", e)
        }
    }

    fun speakText(text: String, stageInfo: String = "") {
        stopListening()
        val cleaned = cleanTextForSpeech(text)
        _voiceState.update {
            it.copy(
                modelSpokenText = cleaned,
                currentReasoningStage = stageInfo,
                conversationState = VoiceConversationState.SPEAKING
            )
        }
        textToSpeech?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "VOICE_UTTERANCE_${System.currentTimeMillis()}")
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            stopAmplitudeSimulation()
            _voiceState.update {
                if (it.conversationState == VoiceConversationState.SPEAKING) {
                    it.copy(conversationState = VoiceConversationState.IDLE, rmsDecibels = 0f)
                } else it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    fun setReasoningStage(stage: String, nodeInfo: String = "") {
        _voiceState.update {
            it.copy(
                currentReasoningStage = stage,
                currentThinkingNode = nodeInfo,
                conversationState = VoiceConversationState.THINKING
            )
        }
    }

    fun toggleAutoListen(enabled: Boolean) {
        _voiceState.update { it.copy(isAutoListenLoopEnabled = enabled) }
    }

    fun toggleMute(muted: Boolean) {
        if (muted) {
            stopListening()
        }
        _voiceState.update { it.copy(isMuted = muted) }
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.6f, 2.0f)
        textToSpeech?.setSpeechRate(clamped)
        _voiceState.update { it.copy(speechRate = clamped) }
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.6f, 1.6f)
        textToSpeech?.setPitch(clamped)
        _voiceState.update { it.copy(speechPitch = clamped) }
    }

    private fun startAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        amplitudeSimulationJob = coroutineScope.launch {
            while (true) {
                val simulatedRms = (4f + (Math.sin(System.currentTimeMillis() / 120.0) * 3.5).toFloat() + (Math.random() * 2f).toFloat()).coerceAtLeast(1f)
                _voiceState.update { it.copy(rmsDecibels = simulatedRms) }
                delay(60)
            }
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        amplitudeSimulationJob = null
    }

    private fun cleanTextForSpeech(raw: String): String {
        return raw
            .replace(Regex("```[a-zA-Z]*\n[\\s\\S]*?```"), "Code implementation omitted for speech.")
            .replace(Regex("[#*`_~>\\[\\]()$\\\\]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(600)
    }

    fun release() {
        try {
            stopAmplitudeSimulation()
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources", e)
        }
    }
}
