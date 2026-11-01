package com.remtrik.m3khelper.ui.component

import android.widget.Toast
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.remtrik.m3khelper.M3KApp
import com.remtrik.m3khelper.R.drawable.ic_backup
import com.remtrik.m3khelper.R.drawable.ic_folder
import com.remtrik.m3khelper.R.drawable.ic_folder_open
import com.remtrik.m3khelper.R.drawable.ic_windows
import com.remtrik.m3khelper.R.string
import com.remtrik.m3khelper.util.funcs.BootBackupState
import com.remtrik.m3khelper.util.funcs.ErrorType
import com.remtrik.m3khelper.util.funcs.MountStatus
import com.remtrik.m3khelper.util.variables.FontSize
import com.remtrik.m3khelper.util.variables.LineHeight
import com.remtrik.m3khelper.util.variables.PaddingValue
import com.remtrik.m3khelper.util.variables.commandError
import com.remtrik.m3khelper.util.variables.commandHandler
import com.remtrik.m3khelper.util.variables.device
import com.remtrik.m3khelper.util.variables.dynamicVars
import com.remtrik.m3khelper.util.variables.sdp
import com.remtrik.m3khelper.util.variables.CommandError
import kotlinx.coroutines.launch

@Composable
fun IconItem(
    icon: Any,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    when (icon) {
        is ImageVector -> {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }

        is Int -> {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}

@Composable
fun ActionCard(
    icon: Any?,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(105.sdp())
            .fillMaxWidth(),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(PaddingValue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.sdp())
        ) {
            icon?.let {
                IconItem(icon = it, modifier = Modifier.size(40.sdp()))
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize,
                    lineHeight = LineHeight,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        lineHeight = LineHeight,
                        fontSize = FontSize
                    )
                }
            }
        }
    }
}

@Composable
fun LinkButton(
    title: String,
    subtitle: String?,
    link: String,
    icon: Any?,
    uriHandler: UriHandler
) {
    ActionCard(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = {
            try {
                uriHandler.openUri(link)
            } catch (_: Exception) {
                Toast.makeText(
                    M3KApp,
                    M3KApp.getString(string.no_browser),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}

@Composable
fun BackupButton() {
    var showDialog by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()

    if (showSpinner) {
        StatusDialog(
            icon = painterResource(id = ic_backup),
            title = string.please_wait,
            showDialog = showSpinner,
        )
    }
    if (showDialog) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(id = ic_backup),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.sdp())
                )
            },
            title = null,
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(string.backup_boot_question),
                    textAlign = TextAlign.Center,
                    fontSize = FontSize,
                    lineHeight = LineHeight
                )
            },
            onDismissRequest = { showDialog = false },
            dismissButton = {
                AssistChip(
                    onClick = { showDialog = false },
                    label = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.sdp()),
                            text = stringResource(string.no),
                            fontSize = FontSize
                        )
                    }
                )
            },
            confirmButton = {
                Row(
                    //modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.sdp())
                ) {
                    AssistChip(
                        onClick = {
                            scope.launch {
                                showDialog = false
                                showSpinner = true
                                val result =
                                    commandHandler.dumpBoot(
                                        ErrorType.QUICKBOOT_ERROR,
                                        BootBackupState.ANDROID
                                    )
                                if (!result.isSuccess) {
                                    commandError.value = CommandError(
                                        type = ErrorType.BOOTBACKUP_ERROR,
                                        title = M3KApp.getString(string.backupboot_error),
                                        message = result.error.firstOrNull()
                                            ?: result.output.firstOrNull() ?: M3KApp.getString(
                                                string.unknown_error
                                            )
                                    )
                                } else {
                                    dynamicVars()
                                }
                                showSpinner = false
                            }
                        },
                        label = {
                            Text(
                                modifier = Modifier.padding(vertical = 2.sdp()),
                                text = stringResource(string.android),
                                fontSize = FontSize
                            )
                        }
                    )
                    if (!currentDeviceCard.noMount) {
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    showDialog = false
                                    showSpinner = true
                                    val result =
                                        commandHandler.dumpBoot(
                                            ErrorType.BOOTBACKUP_ERROR,
                                            BootBackupState.WINDOWS
                                        )
                                    if (!result.isSuccess) {
                                        commandError.value = CommandError(
                                            type = ErrorType.BOOTBACKUP_ERROR,
                                            title = M3KApp.getString(string.backupboot_error),
                                            message = result.error.firstOrNull()
                                                ?: result.output.firstOrNull()
                                                ?: M3KApp.getString(string.unknown_error)
                                        )
                                    }
                                    showSpinner = false
                                }
                            },
                            label = {
                                Text(
                                    modifier = Modifier.padding(vertical = 2.sdp()),
                                    text = stringResource(string.windows),
                                    fontSize = FontSize
                                )
                            }
                        )
                    }
                }
            }
        )
    }

    ActionCard(
        icon = ic_backup,
        title = stringResource(string.backup_boot_title),
        subtitle = stringResource(string.backup_boot_subtitle),
        onClick = { showDialog = true }
    )
}

@Composable
fun MountButton() {
    var showDialog by remember { mutableStateOf(false) }
    var isMounted by remember { mutableStateOf(MountStatus.NOT_MOUNTED) }

    LaunchedEffect(Unit) {
        isMounted = commandHandler.isMounted()
    }

    val scope = rememberCoroutineScope()

    if (showDialog) {
        val isMountedState = isMounted == MountStatus.MOUNTED
        ConfirmDialog(
            icon = painterResource(id = if (isMountedState) ic_folder else ic_folder_open),
            title = null,
            description = stringResource(if (isMountedState) string.umnt_question else string.mnt_question),
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                scope.launch {
                    val result =
                        if (isMountedState) commandHandler.umountWindows() else commandHandler.mountWindows()
                    if (!result.isSuccess) {
                        commandError.value = CommandError(
                            type = ErrorType.MOUNT_ERROR,
                            title = M3KApp.getString(string.mnt_error_title),
                            message = result.error.firstOrNull()
                                ?: result.output.firstOrNull()
                                ?: M3KApp.getString(string.unknown_error)
                        )
                    }
                    showDialog = false
                    isMounted = commandHandler.isMounted()
                }
            }
        )
    }

    val isMountedState = isMounted == MountStatus.MOUNTED
    ActionCard(
        icon = if (isMountedState) ic_folder else ic_folder_open,
        title = stringResource(if (isMountedState) string.umnt_title else string.mnt_title),
        subtitle = stringResource(string.mnt_subtitle),
        onClick = { showDialog = true }
    )
}

@Composable
fun QuickBootButton() {
    var showDialog by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uefiCards by device.uefiCards.collectAsStateWithLifecycle()
    val hasUefi = uefiCards.isNotEmpty()
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()

    if (showSpinner) {
        StatusDialog(
            icon = painterResource(id = ic_windows),
            title = string.please_wait,
            showDialog = showSpinner,
        )
    }
    if (showDialog) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(id = ic_windows),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.sdp())
                )
            },
            title = null,
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(string.quickboot_question1),
                    textAlign = TextAlign.Center,
                    fontSize = FontSize
                )
            },
            onDismissRequest = { showDialog = false },
            dismissButton = {
                AssistChip(
                    onClick = { showDialog = false },
                    label = {
                        Text(
                            modifier = Modifier.padding(vertical = 2.sdp()),
                            text = stringResource(string.no),
                            fontSize = FontSize
                        )
                    }
                )
            },
            confirmButton = {
                Row(
                    //modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.sdp())
                ) {
                    uefiCards.forEach { card ->
                        AssistChip(
                            onClick = {
                                scope.launch {
                                    showDialog = false
                                    showSpinner = true
                                    commandHandler.quickBoot(card.uefiPath)
                                    showSpinner = false
                                }
                            },
                            label = {
                                Text(
                                    modifier = Modifier.padding(vertical = 2.sdp()),
                                    text = stringResource(
                                        when (card.uefiType) {
                                            120 -> string.quickboot_question120
                                            90 -> string.quickboot_question90
                                            60 -> string.quickboot_question60
                                            else -> string.yes
                                        }
                                    ),
                                    fontSize = FontSize
                                )
                            }
                        )
                    }
                }
            }
        )
    }

    val (titleRes, subtitleRes) = if (hasUefi) {
        string.quickboot_title to if (currentDeviceCard.noModem) string.quickboot_subtitle_nomodem else string.quickboot_subtitle
    } else {
        string.uefi_not_found_title to string.uefi_not_found_subtitle
    }

    ActionCard(
        icon = ic_windows,
        title = stringResource(titleRes),
        subtitle = stringResource(subtitleRes),
        onClick = { showDialog = true },
        enabled = hasUefi
    )
}

@Composable
private fun BaseSettingItem(
    icon: Any,
    title: String?,
    summary: String? = null,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(PaddingValue)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.sdp())
        ) {
            Column(Modifier.padding(end = 10.sdp())) {
                IconItem(
                    icon = icon,
                    modifier = Modifier
                        .size(25.sdp())
                        .align(Alignment.CenterHorizontally)
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                title?.let {
                    Text(
                        text = title,
                        fontSize = FontSize,
                        lineHeight = LineHeight,
                        fontWeight = FontWeight.Medium
                    )
                }
                summary?.let {
                    Text(
                        text = summary,
                        fontSize = FontSize,
                        lineHeight = LineHeight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailingContent?.let {
                Column { it() }
            }
        }
    }
}

@Composable
fun SwitchItem(
    icon: Any,
    title: String?,
    summary: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    BaseSettingItem(
        icon = icon,
        title = title,
        summary = summary,
        modifier = Modifier.toggleable(
            value = checked,
            interactionSource = interactionSource,
            role = Role.Switch,
            enabled = enabled,
            indication = LocalIndication.current,
            onValueChange = onCheckedChange
        ),
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                interactionSource = interactionSource
            )
        }
    )
}

@Composable
fun ButtonItem(
    icon: Any,
    title: String?,
    summary: String? = null,
    onClick: () -> Unit
) {
    BaseSettingItem(
        icon = icon,
        title = title,
        summary = summary,
        modifier = Modifier.clickable { onClick() }
    )
}