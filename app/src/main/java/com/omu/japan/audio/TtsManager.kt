package com.omu.japan.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var tried = false

    fun speak(text: String, enabled: Boolean) {
        if (text.isBlank()) return
        if (!enabled) { Toast.makeText(context, "Audio is disabled in Settings", Toast.LENGTH_SHORT).show(); return }
        val engine = tts
        if (engine == null) {
            if (tried && !ready) { Toast.makeText(context, "Japanese TTS unavailable on this device", Toast.LENGTH_SHORT).show(); return }
            tried = true
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val res = tts?.setLanguage(Locale.JAPANESE)
                    ready = res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED
                    if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "omu_tts")
                    else Toast.makeText(context, "Japanese voice not available. Install Japanese TTS data.", Toast.LENGTH_SHORT).show()
                } else {
                    ready = false
                    Toast.makeText(context, "TTS init failed on this device.", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        if (!ready) { Toast.makeText(context, "Japanese voice not available on this device.", Toast.LENGTH_SHORT).show(); return }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "omu_tts")
    }

    fun shutdown() { tts?.shutdown(); tts = null; ready = false }
}
