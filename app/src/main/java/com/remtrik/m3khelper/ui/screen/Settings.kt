package com.remtrik.m3khelper.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AboutCardDestination
import com.ramcosta.composedestinations.generated.destinations.ThemeEngineScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.remtrik.m3khelper.M3KApp
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.ui.component.ButtonItem
import com.remtrik.m3khelper.ui.component.CommonTopAppBar
import com.remtrik.m3khelper.ui.component.SwitchItem
import com.remtrik.m3khelper.util.DeviceCard
import com.remtrik.m3khelper.util.collapseTransition
import com.remtrik.m3khelper.util.deviceCardsArray
import com.remtrik.m3khelper.util.expandTransition
import com.remtrik.m3khelper.util.unknownCard
import com.remtrik.m3khelper.util.variables.AppSettings
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.fastLoadSavedDevice
import com.remtrik.m3khelper.util.variables.sdp

@Destination<RootGraph>
@Composable
fun SettingsScreen(navigator: DestinationsNavigator) {
    val scrollState = rememberScrollState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            CommonTopAppBar(
                navigator = navigator,
                text = R.string.settings,
                isPopBack = if (isLandscape) null else true,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = PaddingValue),
            verticalArrangement = Arrangement.spacedBy(PaddingValue)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ButtonItem(
                        icon = Icons.Filled.Style,
                        title = stringResource(R.string.theme_engine),
                        onClick = { navigator.navigate(ThemeEngineScreenDestination) }
                    )
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    AutoUpdateSetting()
                    DeviceOverrideSetting()
                }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                ButtonItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.about),
                    onClick = { navigator.navigate(AboutCardDestination) }
                )
            }
        }
    }
}

@Composable
private fun AutoUpdateSetting() {
    val checkUpdate by AppSettings.checkUpdate.flow.collectAsStateWithLifecycle()
    SwitchItem(
        icon = Icons.Filled.Update,
        title = stringResource(R.string.autoupdate),
        summary = stringResource(R.string.autoupdate_summary),
        checked = checkUpdate
    ) {
        AppSettings.checkUpdate.update(it)
    }
}

@Composable
private fun DeviceOverrideSetting() {
    val isSpecial by device.isSpecial.collectAsStateWithLifecycle()
    val overrideDevice by AppSettings.overrideDevice.flow.collectAsStateWithLifecycle()
    val overriddenDeviceName by AppSettings.overriddenDeviceName.flow.collectAsStateWithLifecycle()
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()
    val savedDeviceCard by device.savedDeviceCard.collectAsStateWithLifecycle()

    SwitchItem(
        icon = Icons.Filled.DevicesOther,
        title = stringResource(R.string.override_device),
        summary = stringResource(R.string.override_device_summary),
        checked = overrideDevice
    ) {
        AppSettings.overrideDevice.update(it)
        fastLoadSavedDevice(it)
    }

    AnimatedVisibility(
        visible = overrideDevice,
        enter = expandTransition,
        exit = collapseTransition,
    ) {
        DeviceDropdown(
            overriddenDeviceName = overriddenDeviceName,
            currentDeviceCard = currentDeviceCard,
            savedDeviceCard = savedDeviceCard,
            onDeviceSelected = { card ->
                AppSettings.overriddenDeviceCodename.update(card.deviceCodename[0])
                AppSettings.overriddenDeviceName.update(card.deviceName)
                fastLoadSavedDevice(true)
            }
        )
    }

    AnimatedVisibility(
        visible = !isSpecial,
        enter = expandTransition,
        exit = collapseTransition,
    ) {
        ForceRotationSetting()
    }
}

@Composable
private fun DeviceDropdown(
    overriddenDeviceName: String?,
    currentDeviceCard: DeviceCard,
    savedDeviceCard: DeviceCard,
    onDeviceSelected: (DeviceCard) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val excludeDevices = remember(currentDeviceCard, savedDeviceCard) {
        setOf(unknownCard, currentDeviceCard, savedDeviceCard)
    }
    val availableDevices = remember(excludeDevices) {
        deviceCardsArray.filter { it !in excludeDevices }
    }

    Column(modifier = Modifier.padding(PaddingValue)) {
        OutlinedCard(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(PaddingValue)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.sdp()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                overriddenDeviceName?.let {
                    Text(
                        text = stringResource(R.string.device, it),
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
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(PaddingValue)
                .width(250.sdp())
        ) {
            availableDevices.forEach { card ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = card.deviceName,
                            fontSize = FontSize,
                            lineHeight = LineHeight
                        )
                    },
                    onClick = {
                        expanded = false
                        onDeviceSelected(card)
                    }
                )
            }
        }
    }
}

@Composable
private fun ForceRotationSetting() {
    val forceRotation by AppSettings.forceRotation.flow.collectAsStateWithLifecycle()

    LaunchedEffect(forceRotation) {
        M3KApp.resources.configuration.orientation = if (forceRotation) {
            Configuration.ORIENTATION_UNDEFINED
        } else {
            Configuration.ORIENTATION_PORTRAIT
        }
    }

    SwitchItem(
        icon = R.drawable.ic_rotation,
        title = stringResource(R.string.force_rotation),
        summary = stringResource(R.string.force_rotation_summary),
        checked = forceRotation
    ) {
        AppSettings.forceRotation.update(it)
    }
}
