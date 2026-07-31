package com.remtrik.m3khelper.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remtrik.m3khelper.R.string
import com.remtrik.m3khelper.ui.viewmodel.DeviceViewModel
import com.remtrik.m3khelper.util.funcs.string
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.sdp

@Composable
fun InfoCard(
    modifier: Modifier,
    viewModel: DeviceViewModel = viewModel()
) {
    val deviceStrings by viewModel.uiState.collectAsStateWithLifecycle()
    val isSpecial by device.isSpecial.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    ElevatedCard(
        modifier =
            if (isSpecial && !isLandscape) {
                modifier
            } else {
                Modifier
                    .heightIn(min = 210.sdp())
            },
        shape = RoundedCornerShape(8.sdp()),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.sdp())) {
            Text(
                modifier = Modifier
                    .padding(top = PaddingValue)
                    .fillMaxWidth(),
                text = string.woa.string(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = FontSize,
                lineHeight = LineHeight
            )

            Spacer(Modifier.height(5.sdp()))
            deviceStrings?.let { info ->
                listOfNotNull(
                    info.model, info.ram, info.panel,
                    info.bootState, info.slot, info.windowsStatus
                ).forEach {
                    Text(
                        text = it,
                        fontSize = FontSize,
                        lineHeight = LineHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = PaddingValue)
                    )
                }
            }
        }
    }
}