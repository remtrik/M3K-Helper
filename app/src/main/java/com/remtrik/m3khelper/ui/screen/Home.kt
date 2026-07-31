package com.remtrik.m3khelper.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.ui.component.BackupButton
import com.remtrik.m3khelper.ui.component.CommonTopAppBar
import com.remtrik.m3khelper.ui.component.DeviceImage
import com.remtrik.m3khelper.ui.component.ErrorDialog
import com.remtrik.m3khelper.ui.component.InfoCard
import com.remtrik.m3khelper.ui.component.MountButton
import com.remtrik.m3khelper.ui.component.QuickBootButton
import com.remtrik.m3khelper.ui.viewmodel.DeviceViewModel
import com.remtrik.m3khelper.util.DeviceCard
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.commandError
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.sdp

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val deviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()
    val error by commandError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                navigator = navigator,
                text = R.string.app_name,
                isNavigate = if (isLandscape) null else true,
                destination = SettingsScreenDestination,
                icon = Icons.Filled.Settings,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = PaddingValue),
            verticalArrangement = Arrangement.spacedBy(PaddingValue)
        ) {
            if (isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PaddingValue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PaddingValue),
                        modifier = Modifier.width(300.sdp())
                    ) {
                        DeviceInfo(
                            Modifier
                                .height(210.sdp())
                                .width(300.sdp())
                        )
                    }
                    Buttons(deviceCard)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PaddingValue)
                ) {
                    DeviceInfo(Modifier.height(416.sdp()))
                }
                Buttons(deviceCard)
            }
        }

        error?.let { err ->
            ErrorDialog(
                title = err.title,
                description = err.message,
                showDialog = true,
                onDismiss = { commandError.value = null }
            )
        }
    }
}

@Composable
private fun Buttons(deviceCard: DeviceCard) {
    Column(verticalArrangement = Arrangement.spacedBy(10.sdp())) {
        if (!deviceCard.noBoot) BackupButton()
        if (!deviceCard.noMount) MountButton()
        if (!deviceCard.noFlash) QuickBootButton()
    }
}

@Composable
private fun DeviceInfo(modifier: Modifier) {
    val viewModel: DeviceViewModel = viewModel()
    DeviceImage(modifier)
    InfoCard(modifier, viewModel)
}