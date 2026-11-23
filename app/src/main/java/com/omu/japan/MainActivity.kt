package com.omu.japan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.omu.japan.ui.OmuAppRoot
import com.omu.japan.ui.theme.OmuJapanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as OmuApplication).container
        setContent {
            val settings by container.repository.settingsFlow.collectAsState(initial = com.omu.japan.model.AppSettings())
            val progress by container.repository.progressFlow.collectAsState(initial = com.omu.japan.model.UserProgress())
            OmuJapanTheme(settings = settings) {
                OmuAppRoot(container = container, settings = settings, progress = progress)
            }
        }
    }
}
