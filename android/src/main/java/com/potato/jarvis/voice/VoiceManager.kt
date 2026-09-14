package com.potato.jarvis.voice

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Lifecycle-owned voice coordinator.
 * SpeechRecognizer callbacks and commands are kept on the main thread as required by Android.
 * The recognizer is single-shot; POTATO never creates a continuous recognition loop.
 */
class VoiceManager(
    context: Context,
    private val onText: (String) -> Unit,
    private val onPartialText: (String) -> Unit,
    private val onState: (State) -> Unit,
    private val onError: (String) -> Unit,
) : RecognitionListener, TextToSpeech.OnInitListener {
    enum class State { IDLE, LISTENING, SPEAKING, ERROR }

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val audioFocusRequest: AudioFocusRequest? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            .setOnAudioFocusChangeListener { if (it == AudioManager.AUDIOFOCUS_LOSS) stopSpeaking() }
            .build()
    } else null

    private val tts = TextToSpeech(appContext, this)
    private var ttsReady = false
    private var closed = false
    private var listening = false
    private var speaking = false
    private var locale = Locale.getDefault()
    private var speechRate = 1.0f

    private val recognizer: SpeechRecognizer? = createRecognizer()

    private fun createRecognizer(): SpeechRecognizer? {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) return null
        return runCatching {
            val instance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
            } else {
                SpeechRecognizer.createSpeechRecognizer(appContext)
            }
            instance.setRecognitionListener(this)
            instance
        }.getOrNull()
    }

    override fun onInit(status: Int) {
        if (closed) return
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            configureTts(locale, speechRate)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    speaking = true
                    onState(State.SPEAKING)
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        speaking = false
                        abandonAudioFocus()
                        if (!closed) onState(State.IDLE)
                    }
                }

                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        speaking = false
                        abandonAudioFocus()
                        if (!closed) {
                            onError("Text-to-speech failed")
                            onState(State.ERROR)
                        }
                    }
                }
            })
            onState(State.IDLE)
        } else {
            ttsReady = false
            onError("Text-to-speech is unavailable on this device")
            onState(State.ERROR)
        }
    }

    fun configure(locale: Locale = this.locale, speechRate: Float = this.speechRate) {
        this.locale = locale
        this.speechRate = speechRate.coerceIn(0.5f, 2.0f)
        if (ttsReady) configureTts(this.locale, this.speechRate)
    }

    private fun configureTts(locale: Locale, rate: Float) {
        val result = tts.setLanguage(locale)
        tts.setSpeechRate(rate)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            onError("The selected speech language is unavailable")
        }
    }

    fun listen() {
        checkMainThread()
        if (closed) return
        if (recognizer == null) {
            onError("Speech recognition is unavailable on this device")
            onState(State.ERROR)
            return
        }
        stopSpeaking()
        val activeRecognizer = recognizer ?: run {
            onError("Speech recognition is unavailable on this device")
            onState(State.ERROR)
            return
        }
        activeRecognizer.cancel()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }
        listening = true
        onState(State.LISTENING)
        runCatching { activeRecognizer.startListening(intent) }.onFailure {
            listening = false
            onError("Could not start speech recognition")
            onState(State.ERROR)
        }
    }

    fun stopListening() {
        checkMainThread()
        if (!listening) return
        recognizer?.cancel()
        listening = false
        onState(State.IDLE)
    }

    fun speak(text: String) {
        checkMainThread()
        if (closed || text.isBlank()) return
        if (!ttsReady) {
            onError("Text-to-speech is not ready")
            onState(State.ERROR)
            return
        }
        stopListening()
        requestAudioFocus()
        val utteranceId = "potato-${System.nanoTime()}"
        val result = tts.speak(text.trim(), TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            speaking = false
            abandonAudioFocus()
            onError("Could not start text-to-speech")
            onState(State.ERROR)
        } else {
            speaking = true
            onState(State.SPEAKING)
        }
    }

    fun stopSpeaking() {
        checkMainThread()
        if (!speaking && !tts.isSpeaking) return
        tts.stop()
        speaking = false
        abandonAudioFocus()
        if (!closed) onState(State.IDLE)
    }

    fun stopAll() {
        stopListening()
        stopSpeaking()
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    private fun checkMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) { "VoiceManager must be used from the main thread" }
    }

    fun close() {
        if (closed) return
        checkMainThread()
        closed = true
        recognizer?.cancel()
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
        abandonAudioFocus()
        listening = false
        speaking = false
        ttsReady = false
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() {
        listening = false
    }

    override fun onResults(results: Bundle?) {
        listening = false
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty().trim()
        if (text.isNotBlank()) onText(text)
        onState(State.IDLE)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty().trim()
        if (text.isNotBlank()) onPartialText(text)
    }

    override fun onError(error: Int) {
        listening = false
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Microphone audio could not be captured"
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was cancelled"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required"
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network error"
            SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy; try again"
            SpeechRecognizer.ERROR_SERVER -> "Speech recognition service error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything"
            else -> "Speech recognition failed (code $error)"
        }
        if (error != SpeechRecognizer.ERROR_CLIENT) {
            onError(message)
            onState(State.ERROR)
        } else {
            onState(State.IDLE)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
