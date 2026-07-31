package com.remtrik.m3khelper.util.variables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class WidthRatioConfig(
    val widthThreshold: Int,
    val divisor: Double
)

@Composable
fun Int.sdp(): Dp {
    val ratio = rememberWidthRatio(sdpRatioConfigs)
    return (this * ratio).dp
}

@Composable
fun Int.ssp(): TextUnit {
    val ratio = rememberWidthRatio(sspRatioConfigs)
    return (this * ratio).sp
}

@Composable
private fun rememberWidthRatio(configs: List<WidthRatioConfig>): Double {
    val configuration = LocalConfiguration.current
    val smallestWidth = configuration.smallestScreenWidthDp

    return remember(smallestWidth) {
        val approxWidth = (smallestWidth + 15) / 30 * 30
        val matchingConfig = configs.find { approxWidth <= it.widthThreshold }
            ?: configs.last()

        approxWidth / matchingConfig.divisor
    }
}

private val sdpRatioConfigs = listOf(
    WidthRatioConfig(widthThreshold = 400, divisor = 440.0),
    WidthRatioConfig(widthThreshold = 550, divisor = 450.0),
    WidthRatioConfig(widthThreshold = Int.MAX_VALUE, divisor = 650.0)
)

private val sspRatioConfigs = listOf(
    WidthRatioConfig(widthThreshold = 400, divisor = 500.0),
    WidthRatioConfig(widthThreshold = 450, divisor = 450.0),
    WidthRatioConfig(widthThreshold = 550, divisor = 500.0),
    WidthRatioConfig(widthThreshold = Int.MAX_VALUE, divisor = 650.0)
)