package com.omu.japan.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omu.japan.ui.glass.GlassCard
import com.omu.japan.ui.theme.OmuTheme

@Composable
fun ScreenScaffold(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp)) {
        if (onBack != null) {
            GlassCard(onClick = onBack, modifier = Modifier.widthIn(min = 100.dp)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = OmuTheme.c.text)
                    Spacer(Modifier.width(6.dp))
                    Text("Back", color = OmuTheme.c.text)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium, color = OmuTheme.c.text)
        if (subtitle != null) { Spacer(Modifier.height(4.dp)); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OmuTheme.c.muted) }
        Spacer(Modifier.height(16.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SectionCard(title: String? = null, subtitle: String? = null, strong: Boolean = false, modifier: Modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    GlassCard(strong = strong, onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleLarge, color = OmuTheme.c.text)
            if (subtitle != null) { Spacer(Modifier.height(4.dp)); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = OmuTheme.c.muted) }
            if (title != null || subtitle != null) Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun PrimaryBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = OmuTheme.c.accent, contentColor = OmuTheme.c.text)) { Text(text) }
}

@Composable
fun GhostBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier, colors = ButtonDefaults.outlinedButtonColors(contentColor = OmuTheme.c.text)) { Text(text) }
}
