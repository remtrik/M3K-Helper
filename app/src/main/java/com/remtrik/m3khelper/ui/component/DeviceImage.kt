package com.remtrik.m3khelper.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.sdp

@Composable
fun DeviceImage(modifier: Modifier) {
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()
    val isSpecial by device.isSpecial.collectAsStateWithLifecycle()

    Image(
        painter = painterResource(id = currentDeviceCard.deviceImage),
        contentDescription = null,
        modifier = if (isSpecial) {
            modifier
        } else {
            Modifier
                .height(210.sdp())
        },
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit
    )
}
