package com.omu.japan.ui.glass

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.PowerManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.omu.japan.model.GlassQuality
import com.omu.japan.ui.theme.OmuTheme

@Composable
fun glassEnabled(): Boolean {
    val context = LocalContext.current
    val q = OmuTheme.settings.glassQuality
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val saver = pm?.isPowerSaveMode == true
    return when (q) {
        GlassQuality.BATTERY_SAVER -> false
        GlassQuality.HIGH -> Build.VERSION.SDK_INT >= 31
        GlassQuality.BALANCED -> Build.VERSION.SDK_INT >= 31 && !saver
        GlassQuality.AUTO -> Build.VERSION.SDK_INT >= 31 && !saver
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    strong: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val c = OmuTheme.c
    val enabled = glassEnabled()
    val pressed = remember { MutableInteractionSource() }
    val isPressed by pressed.collectIsPressedAsState()
    val reduce = OmuTheme.settings.reduceMotion
    val scale by animateFloatAsState(if (!reduce && isPressed && onClick != null) 0.985f else 1f, spring(stiffness = 500f), label = "glassScale")
    val bg = Brush.linearGradient(
        if (strong) listOf(c.glassStrong, c.glass, c.accent.copy(alpha = if (enabled) 0.12f else 0.08f))
        else listOf(c.glass, Color.Transparent, c.accent.copy(alpha = if (enabled) 0.08f else 0.05f))
    )
    val border = Brush.linearGradient(listOf(c.stroke, Color.White.copy(alpha = 0.10f), c.accent.copy(alpha = 0.28f)))
    val m = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clip(shape)
        .background(bg)
        .border(1.dp, border, shape)
        .drawBehind {
            drawCircle(color = c.accent.copy(alpha = if (enabled) 0.10f else 0.06f), radius = size.minDimension * 0.55f, center = Offset(size.width * 0.15f, size.height * 0.1f))
            drawCircle(color = Color.White.copy(alpha = if (c.isDark) 0.07f else 0.20f), radius = size.minDimension * 0.35f, center = Offset(size.width * 0.85f, 0f))
        }
        .then(if (onClick != null) Modifier.clickable(interactionSource = pressed, indication = null, onClick = onClick) else Modifier)
    Box(modifier = m, content = content)
}

fun Modifier.omBackgroundBlur(radius: Dp, enabled: Boolean = true): Modifier = if (enabled && Build.VERSION.SDK_INT >= 31) {
    this.graphicsLayer {
        renderEffect = RenderEffect.createBlurEffect(radius.value.coerceAtLeast(0.1f), radius.value.coerceAtLeast(0.1f), Shader.TileMode.CLAMP).asComposeRenderEffect()
    }
} else this
