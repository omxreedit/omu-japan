package com.omu.japan.data

import android.content.Context
import com.omu.japan.audio.TtsManager

class AppContainer(context: Context) {
    val repository = AppRepository(context)
    val content = ContentRepository(context)
    val tts = TtsManager(context)
}
