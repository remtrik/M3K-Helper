package com.remtrik.m3khelper.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.remtrik.m3khelper.BuildConfig
import com.remtrik.m3khelper.R.drawable.ic_windows
import com.remtrik.m3khelper.R.string.app_name
import com.remtrik.m3khelper.R.string.source
import com.remtrik.m3khelper.R.string.version_format
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.sdp

/**
 * About card found in settings
 */
@Destination<RootGraph>(style = DestinationStyle.Dialog::class)
@Composable
fun AboutCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.sdp()),
    ) {
        Column(
            modifier = Modifier.padding(24.sdp()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Icon(
                    modifier = Modifier.size(40.sdp()),
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = ImageVector.vectorResource(ic_windows),
                    contentDescription = null
                )

                Spacer(Modifier.width(10.sdp()))

                Column {
                    Text(
                        stringResource(id = app_name),
                        fontSize = FontSize,
                        lineHeight = LineHeight
                    )
                    Text(
                        stringResource(version_format, BuildConfig.VERSION_NAME),
                        fontSize = FontSize,
                        lineHeight = LineHeight
                    )

                    Spacer(Modifier.height(10.sdp()))

                    Text(
                        text = AnnotatedString.fromHtml(
                            htmlString = stringResource(id = source) + " " + "<b><a href=\"https://github.com/WaLoVayu/M3K-Helper\">GitHub</a></b>",
                            linkStyles = TextLinkStyles(
                                style = SpanStyle(textDecoration = TextDecoration.Underline)
                            )
                        ),
                        fontSize = FontSize,
                        lineHeight = LineHeight
                    )
                }
            }
        }
    }
}
