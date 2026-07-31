package com.remtrik.m3khelper.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.ktx.animateColorScheme
import com.remtrik.m3khelper.M3KApp
import com.remtrik.m3khelper.util.variables.AppSettings

private val DarkColorScheme = darkColorScheme(
    primary = m3k_theme_dark_primary,
    onPrimary = m3k_theme_dark_onPrimary,
    primaryContainer = m3k_theme_dark_primaryContainer,
    secondary = m3k_theme_dark_secondary,
    onSecondary = m3k_theme_dark_onSecondary,
    secondaryContainer = m3k_theme_dark_secondaryContainer,
    onSecondaryContainer = m3k_theme_dark_onSecondaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = m3k_theme_light_primary,
    onPrimary = m3k_theme_light_onPrimary,
    primaryContainer = m3k_theme_light_primaryContainer,
    secondary = m3k_theme_light_secondary,
    onSecondary = m3k_theme_light_onSecondary,
    secondaryContainer = m3k_theme_light_secondaryContainer,
    onSecondaryContainer = m3k_theme_light_onSecondaryContainer
)

@Composable
fun M3KHelperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    if (LocalInspectionMode.current) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
        return
    }

    val engineEnable by AppSettings.themeEngineEnable.flow.collectAsStateWithLifecycle()
    val materialUEnable by AppSettings.themeEngineEnableMaterialU.flow.collectAsStateWithLifecycle()
    val styleName by AppSettings.themeEnginePaletteStyle.flow.collectAsStateWithLifecycle()
    val r by AppSettings.themeEngineColorR.flow.collectAsStateWithLifecycle()
    val g by AppSettings.themeEngineColorG.flow.collectAsStateWithLifecycle()
    val b by AppSettings.themeEngineColorB.flow.collectAsStateWithLifecycle()

    val style = remember(styleName) {
        runCatching {
            styleName?.let { PaletteStyle.valueOf(it) }
        }.getOrDefault(PaletteStyle.TonalSpot)
    }

    val colorScheme = remember(darkTheme, engineEnable, materialUEnable, style, r, g, b) {
        when {
            materialUEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(M3KApp) else dynamicLightColorScheme(M3KApp)
            }

            engineEnable -> {
                style?.let {
                    dynamicColorScheme(
                        seedColor = Color(r, g, b),
                        isDark = darkTheme,
                        style = it,
                    )
                }
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    colorScheme?.let {
        MaterialTheme(
            colorScheme = animateColorScheme(it),
            typography = Typography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
