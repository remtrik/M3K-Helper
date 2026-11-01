package com.remtrik.m3khelper.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SecurityUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.remtrik.m3khelper.BuildConfig
import com.remtrik.m3khelper.M3KApp
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.util.funcs.LatestVersionInfo
import com.remtrik.m3khelper.util.funcs.restart
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.sdp
import com.remtrik.m3khelper.util.variables.showWarningCard
import com.remtrik.m3khelper.util.variables.ssp

@Composable
private fun BaseAlertDialog(
    icon: @Composable () -> Unit,
    title: String? = null,
    description: String? = null,
    onDismissRequest: () -> Unit = {},
    dismissButton: (@Composable () -> Unit)? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        icon = icon,
        title = title?.let {
            {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    fontSize = FontSize,
                    lineHeight = LineHeight
                )
            }
        },
        text = {
            if (content != null) {
                content()
            } else if (description != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    textAlign = TextAlign.Center,
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
            }
        },
        onDismissRequest = onDismissRequest,
        dismissButton = dismissButton ?: {},
        confirmButton = confirmButton ?: {}
    )
}

@Composable
fun ErrorDialog(
    title: String?,
    description: String?,
    showDialog: Boolean,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        BaseAlertDialog(
            icon = {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.sdp())
                )
            },
            title = title,
            description = description,
            onDismissRequest = onDismiss,
            dismissButton = {
                AssistChip(
                    onClick = onDismiss,
                    label = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.sdp()),
                            text = stringResource(R.string.yes),
                            fontSize = FontSize
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun UpdateDialog(version: LatestVersionInfo) {
    val localUriHandler = LocalUriHandler.current
    BaseAlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.SecurityUpdate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.sdp())
            )
        },
        title = stringResource(R.string.new_update),
        description = stringResource(
            R.string.new_update_summary,
            BuildConfig.VERSION_NAME,
            version.versionName
        ),
        confirmButton = {
            AssistChip(
                onClick = { localUriHandler.openUri(version.downloadUrl) },
                label = {
                    Text(
                        modifier = Modifier.padding(vertical = 2.sdp()),
                        text = stringResource(R.string.download),
                    )
                }
            )
        }
    )
}

@Composable
fun ConfirmDialog(
    icon: Painter,
    title: String?,
    description: String?,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (showDialog) {
        BaseAlertDialog(
            icon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.sdp())
                )
            },
            title = title,
            description = description,
            onDismissRequest = onDismiss,
            dismissButton = {
                AssistChip(
                    onClick = onDismiss,
                    label = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.sdp()),
                            text = stringResource(R.string.no),
                            fontSize = FontSize
                        )
                    }
                )
            },
            confirmButton = {
                AssistChip(
                    onClick = onConfirm,
                    label = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.sdp()),
                            text = stringResource(R.string.yes),
                            fontSize = FontSize
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun NoRoot() {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.sdp())
            )
        },
        title = null,
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.no_root),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                lineHeight = 35.ssp(),
                fontSize = 25.ssp()
            )
        },
        onDismissRequest = {},
        dismissButton = {},
        confirmButton = {
            AssistChip(
                onClick = {
                    M3KApp.restart()
                },
                label = {
                    Text(
                        modifier = Modifier.padding(vertical = 2.sdp()),
                        text = stringResource(R.string.reload),
                    )
                }
            )
        }
    )
}

@Composable
fun StatusDialog(
    icon: Painter,
    title: Int,
    showDialog: Boolean,
) {
    if (showDialog) {
        AlertDialog(
            icon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.sdp())
                )
            },
            title = {
                Text(
                    text = stringResource(title),
                    textAlign = TextAlign.Center,
                    fontSize = FontSize,
                    lineHeight = LineHeight
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(32.sdp()),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            },
            onDismissRequest = {},
            dismissButton = {},
            confirmButton = {}
        )
    }
}

@Composable
fun UnknownDevice() {
    AlertDialog(
        icon = {
            Icon(
                modifier = Modifier
                    .size(40.sdp()),
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = null,
        text = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.device_unknown),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                lineHeight = 35.ssp(),
                fontSize = 25.ssp()
            )
        },
        onDismissRequest = { showWarningCard.value = false },
        dismissButton = {},
        confirmButton = {
            AssistChip(
                onClick = {
                    showWarningCard.value = false
                },
                label = {
                    Text(
                        modifier = Modifier.padding(vertical = 2.sdp()),
                        text = stringResource(R.string.device_unknown_confirm),
                    )
                }
            )
        }
    )
}