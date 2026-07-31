package com.remtrik.m3khelper.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.remtrik.m3khelper.util.variables.AppSettings
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.sdp

@Composable
fun ColorPicker() {
    val red by AppSettings.themeEngineColorR.flow.collectAsStateWithLifecycle()
    val green by AppSettings.themeEngineColorG.flow.collectAsStateWithLifecycle()
    val blue by AppSettings.themeEngineColorB.flow.collectAsStateWithLifecycle()

    val targetColor = remember(red, green, blue) {
        Color(red, green, blue, 1f)
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "ColorPreviewAnimation"
    )

    Column {
        ColorPreview(animatedColor)
        ColorSliders(red, green, blue)
    }
}

@Composable
private fun ColorPreview(color: Color) {
    Box(
        modifier = Modifier
            .padding(PaddingValue)
            .fillMaxWidth()
            .height(100.sdp())
            .background(color, shape = MaterialTheme.shapes.extraLarge)
            .border(
                width = 2.sdp(),
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge
            ),
        contentAlignment = Alignment.Center
    ) {}
}

@Composable
private fun ColorSliders(
    red: Float,
    green: Float,
    blue: Float
) {
    Column(
        modifier = Modifier.padding(PaddingValue),
        verticalArrangement = Arrangement.spacedBy(10.sdp())
    ) {
        ColorSlider(
            label = "R",
            value = red,
            onValueChange = {
                AppSettings.themeEngineColorR.flow.value = it
            },
            onValueChangeFinished = {
                AppSettings.themeEngineColorR.update(red)
            },
            color = Color.Red
        )
        ColorSlider(
            label = "G",
            value = green,
            onValueChange = {
                AppSettings.themeEngineColorG.flow.value = it
            },
            onValueChangeFinished = {
                AppSettings.themeEngineColorG.update(green)
            },
            color = Color.Green
        )
        ColorSlider(
            label = "B",
            value = blue,
            onValueChange = {
                AppSettings.themeEngineColorB.flow.value = it
            },
            onValueChangeFinished = {
                AppSettings.themeEngineColorB.update(blue)
            },
            color = Color.Blue
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.sdp())
    ) {
        Text(
            text = label,
            fontSize = FontSize,
            lineHeight = LineHeight
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.14f)
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toColorInt().toString(),
            modifier = Modifier.width(35.sdp()),
            fontSize = FontSize,
            lineHeight = LineHeight,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun Float.toColorInt(): Int = (this * 255 + 0.5f).toInt()
