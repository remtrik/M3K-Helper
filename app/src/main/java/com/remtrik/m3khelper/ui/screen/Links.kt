package com.remtrik.m3khelper.ui.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.ui.component.CommonTopAppBar
import com.remtrik.m3khelper.ui.component.LinkButton
import com.remtrik.m3khelper.util.DeviceCard
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.sdp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination<RootGraph>()
@Composable
fun LinksScreen(navigator: DestinationsNavigator) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            CommonTopAppBar(
                navigator = navigator,
                text = R.string.links,
            )
        }
    ) { innerPadding ->
        LinksContent(
            isLandscape = isLandscape,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun LinksContent(
    isLandscape: Boolean,
    innerPadding: PaddingValues,
    scrollState: ScrollState = rememberScrollState()
) {
    val spacing = 10.sdp()
    val deviceCard by device.currentDeviceCard.collectAsState()
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                top = innerPadding.calculateTopPadding(),
                start = PaddingValue,
                end = PaddingValue
            )
    ) {
        if (isLandscape) {
            LandscapeLinksLayout(spacing, deviceCard, uriHandler)
        } else {
            PortraitLinksLayout(deviceCard, uriHandler)
        }
    }
}

@Composable
private fun LandscapeLinksLayout(
    spacing: Dp,
    deviceCard: DeviceCard,
    uriHandler: UriHandler
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        LinksColumn(modifier = Modifier.weight(1f)) {
            FilesLinks(deviceCard, uriHandler)
        }
        LinksColumn(modifier = Modifier.weight(1f)) {
            SocialLinks(deviceCard, uriHandler)
        }
    }
}

@Composable
private fun PortraitLinksLayout(
    deviceCard: DeviceCard,
    uriHandler: UriHandler
) {
    FilesLinks(deviceCard, uriHandler)
    SocialLinks(deviceCard, uriHandler)
}

@Composable
private fun LinksColumn(
    modifier: Modifier = Modifier,
    verticalSpacing: Dp = 10.sdp(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        modifier = modifier,
        content = content
    )
}

@Composable
private fun FilesLinks(
    deviceCard: DeviceCard,
    uriHandler: UriHandler
) {
    with(deviceCard) {
        if (unifiedDriversUEFI) {
            LinkButton(
                title = stringResource(R.string.driversuefi, deviceName),
                subtitle = null,
                link = driversLink,
                icon = R.drawable.ic_drivers,
                uriHandler = uriHandler
            )
        } else {

            if (!noDrivers) {
                LinkButton(
                    title = stringResource(R.string.drivers, deviceName),
                    subtitle = null,
                    link = driversLink,
                    icon = R.drawable.ic_drivers,
                    uriHandler = uriHandler
                )
            }

            if (!noUEFI) {
                LinkButton(
                    title = stringResource(R.string.uefi, deviceName),
                    subtitle = null,
                    link = uefiLink,
                    icon = R.drawable.ic_uefi,
                    uriHandler = uriHandler
                )
            }
        }
    }
}

@Composable
private fun SocialLinks(
    deviceCard: DeviceCard,
    uriHandler: UriHandler
) {
    with(deviceCard) {
        if (!noGroup) {
            LinkButton(
                title = stringResource(R.string.group, deviceName),
                subtitle = null,
                link = groupLink,
                icon = Icons.AutoMirrored.Filled.Message,
                uriHandler = uriHandler
            )
        }

        if (!noGuide) {
            LinkButton(
                title = stringResource(R.string.guide, deviceName),
                subtitle = null,
                link = deviceGuide,
                icon = Icons.Filled.Book,
                uriHandler = uriHandler
            )
        }
    }
}