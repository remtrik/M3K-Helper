package com.remtrik.m3khelper.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.sdp

/**
 * Common top app bar(I'm too lazy to turn off warning about missing documentation)
 */
@Composable
fun CommonTopAppBar(
    navigator: DestinationsNavigator,
    text: Int,
    isNavigate: Boolean? = null,
    isPopBack: Boolean? = null,
    destination: DirectionDestinationSpec? = null,
    icon: ImageVector? = null
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.sdp())
            .padding(PaddingValue),
        verticalAlignment = if (isLandscape) Alignment.CenterVertically else Alignment.Bottom,
    ) {
        // Back Button
        isPopBack?.let {
            IconButton(
                onClick = dropUnlessResumed { navigator.popBackStack() },
                modifier = Modifier.size(40.sdp())
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    modifier = Modifier.size(20.sdp())
                )
            }
        }
        // Title
        Box(
            modifier = Modifier.height(40.sdp()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(text),
                fontSize = FontSize,
                lineHeight = LineHeight,
                fontWeight = FontWeight.Bold,
            )
        }
        // Navigation button
        isNavigate?.let {
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { navigator.navigate(destination ?: return@IconButton) },
                modifier = Modifier.size(40.sdp())
            ) {
                Icon(
                    imageVector = icon ?: return@IconButton,
                    contentDescription = null,
                    modifier = Modifier.size(25.sdp())
                )
            }
        }
    }
}