package com.nextgen.keyboard.feature.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object Listening : VoiceInputState()
    data class Result(val text: String) : VoiceInputState()
    data class Error(val message: String) : VoiceInputState()
    data class PartialResult(val text: String) : VoiceInputState()
}

@Singleton
class VoiceInputManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    val voiceState: StateFlow<VoiceInputState> = _voiceState.asStateFlow()

    private val _volume = MutableStateFlow(0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    fun startListening(languageCode: String = "en-US") {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _voiceState.value = VoiceInputState.Error("Speech recognition not available")
                Timber.e("Speech recognition not available on this device")
                return
            }

            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceInputState.Listening
            Timber.d("✅ Voice input started with language: $languageCode")
        } catch (e: Exception) {
            Timber.e(e, "Error starting voice input")
            _voiceState.value = VoiceInputState.Error("Failed to start listening: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _voiceState.value = VoiceInputState.Idle
            Timber.d("Voice input stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping voice input")
        }
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            _voiceState.value = VoiceInputState.Idle
            Timber.d("Voice input cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling voice input")
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Timber.d("Ready for speech")
            _voiceState.value = VoiceInputState.Listening
        }

        override fun onBeginningOfSpeech() {
            Timber.d("Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Update volume level (0-10 range normalized to 0-1)
            _volume.value = (rmsdB / 10f).coerceIn(0f, 1f)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }

        override fun onEndOfSpeech() {
            Timber.d("End of speech")
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            Timber.e("Voice input error: $errorMessage")
            _voiceState.value = VoiceInputState.Error(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Timber.d("✅ Voice result: $text")
                _voiceState.value = VoiceInputState.Result(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Timber.d("Partial result: $text")
                _voiceState.value = VoiceInputState.PartialResult(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Custom events
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _voiceState.value = VoiceInputState.Idle
            Timber.d("Voice input manager destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error destroying voice input manager")
        }
    }
}