package com.remtrik.m3khelper.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
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
import com.remtrik.m3khelper.util.beyond1Card
import com.remtrik.m3khelper.util.collapseTransition
import com.remtrik.m3khelper.util.debugCard
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination<RootGraph>
@Composable
fun SettingsScreen(navigator: DestinationsNavigator) {
    val scrollState = rememberScrollState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isSpecial by device.isSpecial.collectAsState()

    val checkUpdate by AppSettings.checkUpdate.collectAsState()
    val forceRotation by AppSettings.forceRotation.collectAsState()
    val overrideDevice by AppSettings.overrideDevice.collectAsState()
    val overridenDeviceName by AppSettings.overridenDeviceName.collectAsState()

    val currentDeviceCard by device.currentDeviceCard.collectAsState()
    val savedDeviceCard by device.savedDeviceCard.collectAsState()

    var expanded by rememberSaveable { mutableStateOf(false) }

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
                .verticalScroll(scrollState)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(horizontal = PaddingValue)
                .fillMaxSize(),
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
                    SwitchItem(
                        icon = Icons.Filled.Update,
                        title = stringResource(R.string.autoupdate),
                        summary = stringResource(R.string.autoupdate_summary),
                        checked = checkUpdate
                    ) {
                        AppSettings.update("check_update", it, AppSettings.checkUpdate)
                    }
                    SwitchItem(
                        icon = Icons.Filled.DevicesOther,
                        title = stringResource(R.string.override_device),
                        summary = stringResource(R.string.override_device_summary),
                        checked = overrideDevice
                    ) {
                        AppSettings.update("override_device", it, AppSettings.overrideDevice)
                        fastLoadSavedDevice(it)
                    }

                    AnimatedVisibility(
                        visible = overrideDevice,
                        enter = expandTransition,
                        exit = collapseTransition,
                    ) {
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
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.device, overridenDeviceName),
                                        modifier = Modifier.weight(1f),
                                        fontSize = FontSize,
                                        lineHeight = LineHeight
                                    )
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
                                deviceCardsArray
                                    .filterNot {
                                        it in listOf(
                                            beyond1Card,
                                            debugCard,
                                            unknownCard,
                                            savedDeviceCard,
                                            currentDeviceCard
                                        )
                                    }
                                    .forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = it.deviceName,
                                                    fontSize = FontSize,
                                                    lineHeight = LineHeight
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                AppSettings.update(
                                                    "overriden_device_codename",
                                                    it.deviceCodename[0],
                                                    AppSettings.overridenDeviceCodename
                                                )
                                                AppSettings.update(
                                                    "overriden_device_name",
                                                    it.deviceName,
                                                    AppSettings.overridenDeviceName
                                                )
                                                fastLoadSavedDevice(true)
                                            }
                                        )
                                    }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = !isSpecial,
                        enter = expandTransition,
                        exit = collapseTransition,
                    ) {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            SwitchItem(
                                icon = R.drawable.ic_rotation,
                                title = stringResource(R.string.force_rotation),
                                summary = stringResource(R.string.force_rotation_summary),
                                checked = forceRotation
                            ) {
                                AppSettings.update("force_rotation", it, AppSettings.forceRotation)
                                if (it) M3KApp.resources.configuration.orientation =
                                    Configuration.ORIENTATION_UNDEFINED
                                else Configuration.ORIENTATION_PORTRAIT
                            }
                        }
                    }

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
