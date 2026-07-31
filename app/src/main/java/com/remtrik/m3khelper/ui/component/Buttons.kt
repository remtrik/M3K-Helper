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
fun LinkButton(
    title: String,
    subtitle: String?,
    link: String,
    icon: Any?,
    uriHandler: UriHandler
) {
    ElevatedCard(
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
        },
        modifier = Modifier
            .height(105.sdp())
            .fillMaxWidth(),
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
fun BackupButton() {
    var showDialog by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()

    ElevatedCard(
        onClick = { showDialog = true },
        modifier = Modifier
            .height(105.sdp())
            .fillMaxWidth(),
    ) {
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
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
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
                                    modifier = Modifier.padding(
                                        vertical = 2.sdp()
                                    ),
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
                                        modifier = Modifier.padding(
                                            vertical = 2.sdp()
                                        ),
                                        text = stringResource(string.windows),
                                        fontSize = FontSize
                                    )
                                }
                            )
                        }
                        AssistChip(
                            onClick = { showDialog = false },
                            label = {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 2.sdp()
                                    ),
                                    text = stringResource(string.no),
                                    fontSize = FontSize
                                )
                            }
                        )
                    }
                },
                confirmButton = { }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(PaddingValue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.sdp())
        ) {
            IconItem(icon = ic_backup, modifier = Modifier.size(40.sdp()))
            Column {
                Text(
                    stringResource(string.backup_boot_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = FontSize,
                    lineHeight = LineHeight,
                )
                Text(
                    stringResource(string.backup_boot_subtitle),
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
            }
        }
    }
}

@Composable
fun MountButton() {
    var showDialog by remember { mutableStateOf(false) }
    var isMounted by remember { mutableStateOf(MountStatus.NOT_MOUNTED) }

    LaunchedEffect(Unit) {
        isMounted = commandHandler.isMounted()
    }

    val scope = rememberCoroutineScope()

    ElevatedCard(
        onClick = { showDialog = true },
        modifier = Modifier
            .height(105.sdp())
            .fillMaxWidth(),
    ) {
        if (showDialog) {
            if (isMounted == MountStatus.MOUNTED) {
                Dialog(
                    icon = painterResource(id = ic_folder),
                    title = null,
                    description = stringResource(string.umnt_question),
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        scope.launch {
                            val result = commandHandler.umountWindows()
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
            } else {
                Dialog(
                    icon = painterResource(id = ic_folder_open),
                    title = null,
                    description = stringResource(string.mnt_question),
                    showDialog = showDialog,
                    onDismiss = { showDialog = false },
                    onConfirm = {
                        scope.launch {
                            val result = commandHandler.mountWindows()
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
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(PaddingValue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.sdp())
        ) {
            IconItem(
                icon = if (isMounted == MountStatus.MOUNTED) ic_folder else ic_folder_open,
                modifier = Modifier.size(40.sdp())
            )
            Column {
                val mounted: Int =
                    if (isMounted == MountStatus.MOUNTED) {
                        string.umnt_title
                    } else {
                        string.mnt_title
                    }
                Text(
                    stringResource(mounted),
                    fontWeight = FontWeight.Bold,
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
                Text(
                    stringResource(string.mnt_subtitle),
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
            }
        }
    }
}

@Composable
fun QuickBootButton() {
    var showDialog by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val uefiCards by device.uefiCards.collectAsStateWithLifecycle()
    val hasUefi = uefiCards.isNotEmpty()
    val currentDeviceCard by device.currentDeviceCard.collectAsStateWithLifecycle()

    ElevatedCard(
        onClick = { showDialog = true },
        modifier = Modifier
            .height(105.sdp())
            .fillMaxWidth(),
        enabled = hasUefi
    ) {
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
                    Row(
                        Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(10.sdp())
                    ) {
                        uefiCards.forEach {
                            AssistChip(
                                onClick = {
                                    scope.launch {
                                        showDialog = false
                                        showSpinner = true
                                        commandHandler.quickBoot(
                                            it.uefiPath
                                        )
                                        showSpinner = false
                                    }
                                },
                                label = {
                                    Text(
                                        modifier = Modifier.padding(
                                            vertical = 2.sdp()
                                        ),
                                        text = stringResource(
                                            when (it.uefiType) {
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
                        AssistChip(
                            onClick = { showDialog = false },
                            label = {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 2.sdp()
                                    ),
                                    text = stringResource(string.no),
                                    fontSize = FontSize
                                )
                            }
                        )
                    }
                },
                confirmButton = { }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(PaddingValue),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.sdp())
        ) {
            IconItem(icon = ic_windows, modifier = Modifier.size(40.sdp()))
            Column {
                val title: Int
                val subtitle: Int
                if (hasUefi) {
                    title = string.quickboot_title
                    subtitle = when (currentDeviceCard.noModem) {
                        true -> string.quickboot_subtitle_nomodem
                        else -> string.quickboot_subtitle
                    }
                } else {
                    title = string.uefi_not_found_title
                    subtitle = string.uefi_not_found_subtitle
                }
                Text(
                    stringResource(title),
                    fontWeight = FontWeight.Bold,
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
                Text(
                    stringResource(subtitle),
                    lineHeight = LineHeight,
                    fontSize = FontSize
                )
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
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .toggleable(
                value = checked,
                interactionSource = interactionSource,
                role = Role.Switch,
                enabled = enabled,
                indication = LocalIndication.current,
                onValueChange = onCheckedChange
            ),
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
            Column {
                Switch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = onCheckedChange,
                    interactionSource = interactionSource
                )
            }
        }
    }
}

@Composable
fun ButtonItem(
    icon: Any,
    title: String?,
    summary: String? = null,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
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
        }
    }
}