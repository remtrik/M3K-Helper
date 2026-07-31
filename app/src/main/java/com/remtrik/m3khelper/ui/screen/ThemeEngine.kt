package com.remtrik.m3khelper.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.materialkolor.PaletteStyle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.ui.component.ColorPicker
import com.remtrik.m3khelper.ui.component.CommonTopAppBar
import com.remtrik.m3khelper.ui.component.SwitchItem
import com.remtrik.m3khelper.util.collapseTransition
import com.remtrik.m3khelper.util.expandTransition
import com.remtrik.m3khelper.util.variables.AppSettings
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.sdp

@Destination<RootGraph>
@Composable
fun ThemeEngineScreen(navigator: DestinationsNavigator) {
    val themeEngineEnable by AppSettings.themeEngineEnable.flow.collectAsStateWithLifecycle()
    val themeEngineEnableMaterialU by AppSettings.themeEngineEnableMaterialU.flow.collectAsStateWithLifecycle()
    val themeEnginePaletteStyle by AppSettings.themeEnginePaletteStyle.flow.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                navigator = navigator,
                text = R.string.theme_engine,
                isPopBack = true
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = PaddingValue)
        ) {
            ThemeSwitchItems(
                enableThemeEngine = themeEngineEnable,
                enableMaterialU = themeEngineEnableMaterialU,
                onThemeEngineChanged = {
                    AppSettings.themeEngineEnable.update(it)
                    if (it) {
                        AppSettings.themeEngineEnableMaterialU.update(false)
                    }
                },
                onMaterialUChanged = {
                    AppSettings.themeEngineEnableMaterialU.update(it)
                    if (it) {
                        AppSettings.themeEngineEnable.update(false)
                    }
                }
            )
            PaletteSelector(
                enableThemeEngine = themeEngineEnable,
                paletteStyle = themeEnginePaletteStyle,
                onPaletteStyleChanged = {
                    AppSettings.themeEnginePaletteStyle.update(it)
                }
            )
        }
    }
}

@Composable
private fun ThemeSwitchItems(
    enableThemeEngine: Boolean,
    enableMaterialU: Boolean,
    onThemeEngineChanged: (Boolean) -> Unit,
    onMaterialUChanged: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaddingValue / 2)
    ) {
        Column {
            SwitchItem(
                icon = Icons.Filled.Brush,
                title = stringResource(R.string.enable_materialu),
                summary = stringResource(R.string.enable_materialu_summary),
                checked = enableMaterialU,
                onCheckedChange = onMaterialUChanged
            )
            SwitchItem(
                icon = Icons.Filled.FormatPaint,
                title = stringResource(R.string.theme_engine_enable),
                summary = stringResource(R.string.theme_engine_enable_summary),
                checked = enableThemeEngine,
                onCheckedChange = onThemeEngineChanged
            )
        }
    }
}

@Composable
private fun PaletteSelector(
    enableThemeEngine: Boolean,
    paletteStyle: String?,
    onPaletteStyleChanged: (String) -> Unit
) {
    AnimatedVisibility(
        visible = enableThemeEngine,
        enter = expandTransition,
        exit = collapseTransition,
        modifier = Modifier
            .padding(PaddingValue)
            .fillMaxWidth()
    ) {
        PaletteCard(paletteStyle, onPaletteStyleChanged)
    }
}

@Composable
private fun PaletteCard(
    paletteStyle: String?,
    onPaletteStyleChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.padding(PaddingValue)
    ) {
        PaletteDropdown(
            expanded = expanded,
            paletteStyle = paletteStyle,
            onExpandedChange = { expanded = it },
            onPaletteStyleSelected = onPaletteStyleChanged
        )
        ColorPicker()
    }
}

@Composable
private fun PaletteDropdown(
    expanded: Boolean,
    paletteStyle: String?,
    onExpandedChange: (Boolean) -> Unit,
    onPaletteStyleSelected: (String) -> Unit
) {
    val otherStyles = remember(paletteStyle) {
        PaletteStyle.entries.filter { it.name != paletteStyle }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            onClick = { onExpandedChange(!expanded) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(PaddingValue)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.sdp())
            ) {
                Icon(
                    Icons.Filled.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(25.sdp()),
                    tint = MaterialTheme.colorScheme.primary
                )
                paletteStyle?.let {
                    Text(
                        text = stringResource(R.string.theme_engine_current_palette, it),
                        modifier = Modifier.weight(1f),
                        fontSize = FontSize,
                        lineHeight = LineHeight
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.padding(PaddingValue)
        ) {
            otherStyles.forEach { palette ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = palette.name,
                            fontSize = FontSize,
                            lineHeight = LineHeight
                        )
                    },
                    onClick = {
                        onPaletteStyleSelected(palette.name)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}