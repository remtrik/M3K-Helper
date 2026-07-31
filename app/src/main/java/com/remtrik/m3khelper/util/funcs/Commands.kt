package com.remtrik.m3khelper.util.funcs

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.remtrik.m3khelper.M3KApp
import com.remtrik.m3khelper.R
import com.remtrik.m3khelper.util.variables.SDCARD_PATH
import com.remtrik.m3khelper.util.variables.bootBackupStatus
import com.remtrik.m3khelper.util.variables.commandError
import com.remtrik.m3khelper.util.variables.device
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

internal val BlockWindowsPath by lazy {
    ShellUtils.fastCmd("readlink -fn /dev/block/bootdevice/by-name/win")
}

private const val TAG = "M3K: Commands"
private const val WINDOWS_PARTITION = "/dev/block/by-name/win"
private const val SENSORS_PATH =
    "Windows/Windows/System32/Drivers/DriverData/QUALCOMM/fastRPC/vendor/etc/sensors"
private const val MODEM_INF_PATTERN = "qcremotefs8150.inf_arm64_*"

object RootCommandExecutor {
    suspend fun exec(command: String): Shell.Result = withContext(Dispatchers.IO) {
        try {
            Shell.cmd(command).exec()
        } catch (t: Throwable) {
            Log.e(TAG, "RootCommandExecutor failed: ", t)
            throw t
        }
    }
}

abstract class Commands {
    private val mutex = Mutex()

    private fun Shell.Result?.toCommandResult(command: String = ""): CommandResult {
        return this?.let {
            CommandResult(
                isSuccess = it.isSuccess,
                output = it.out.toList(),
                error = it.err.toList(),
                command = command
            )
        } ?: CommandResult(
            isSuccess = false,
            output = listOf(R.string.mount_error_default.string()),
            error = listOf(R.string.mount_error_default.string()),
            command = command
        )
    }

    private fun errorResult(message: String, command: String = ""): CommandResult = CommandResult(
        isSuccess = false,
        output = listOf(message),
        error = listOf(message),
        command = command
    )

    suspend fun dumpBoot(type: ErrorType, where: BootBackupState): CommandResult =
        withContext(Dispatchers.IO) {
            when (where) {
                BootBackupState.WINDOWS -> {
                    val ok = withMountedWindows(type) {
                        val target = File("$SDCARD_PATH/Windows/boot.img").canonicalFile
                        val cmd =
                            "dd if=/dev/block/bootdevice/by-name/boot${device.slot} of=${target.path} bs=32M"
                        RootCommandExecutor.exec(cmd)
                        bootBackupStatus()
                    }
                    if (!ok) return@withContext errorResult(
                        R.string.mount_error_default.string(),
                        "mount"
                    )
                    null
                }

                BootBackupState.ANDROID -> {
                    val target = File("$SDCARD_PATH/boot.img").canonicalFile
                    val cmd =
                        "dd if=/dev/block/bootdevice/by-name/boot${device.slot} of=${target.path}"
                    RootCommandExecutor.exec(cmd).also { bootBackupStatus(forceMount = false) }
                }

                else -> return@withContext errorResult("Invalid 'where' arg: $where")
            }.toCommandResult()
        }

    suspend fun mountWindows(): CommandResult = withContext(Dispatchers.IO) {
        RootCommandExecutor.exec("mkdir -p $SDCARD_PATH/Windows")
        RootCommandExecutor.exec(
            "su -mm -c mount.ntfs $WINDOWS_PARTITION $SDCARD_PATH/Windows"
        ).toCommandResult("mount.ntfs")
    }

    suspend fun umountWindows(): CommandResult = withContext(Dispatchers.IO) {
        RootCommandExecutor.exec("su -mm -c umount $SDCARD_PATH/Windows")
            .toCommandResult("umount")
    }

    suspend fun isMounted(): MountStatus = withContext(Dispatchers.IO) {
        val result = Shell.cmd("mount | grep $BlockWindowsPath").exec()
        if (result.isSuccess && result.out.isNotEmpty() && result.out[0].contains("Windows")) {
            MountStatus.MOUNTED
        } else {
            MountStatus.NOT_MOUNTED
        }
    }

    private suspend fun checkSensors(): Boolean = withContext(Dispatchers.IO) {
        if (!device.currentDeviceCard.value.sensors) return@withContext true
        var found = false
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            val out = ShellUtils.fastCmd("ls $SDCARD_PATH/$SENSORS_PATH/")
            found = out.isNotEmpty()
        }
        found
    }

    suspend fun dumpSensors(): CommandResult = withContext(Dispatchers.IO) {
        var res: Shell.Result? = null
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            res = RootCommandExecutor.exec(
                "cp -r /vendor/etc/sensors/* $SDCARD_PATH/$SENSORS_PATH"
            )
        }
        res.toCommandResult("cp sensors")
    }

    suspend fun dumpModem(): CommandResult = withContext(Dispatchers.IO) {
        var res: Shell.Result? = null
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            val path = ShellUtils.fastCmd(
                "find $SDCARD_PATH/Windows/Windows/System32/DriverStore/FileRepository -name $MODEM_INF_PATTERN"
            )
            if (path.isEmpty()) {
                res = null
                return@withMountedWindows
            }
            val cmd =
                "dd if=/dev/block/bootdevice/by-name/modemst1 of=$path/bootmodem_fs1 bs=8388608 && " +
                        "dd if=/dev/block/bootdevice/by-name/modemst2 of=$path/bootmodem_fs2 bs=8388608"
            res = RootCommandExecutor.exec(cmd)
        }
        res.toCommandResult("dump modem")
    }

    suspend fun flashUEFI(uefiPath: String): CommandResult = withContext(Dispatchers.IO) {
        val file = File(uefiPath).canonicalFile
        val cmd = "dd if=${file.path} of=/dev/block/bootdevice/by-name/boot${device.slot} bs=32M"
        RootCommandExecutor.exec(cmd).toCommandResult("flash UEFI")
    }

    suspend fun quickBoot(uefiPath: String): Unit = withContext(Dispatchers.IO) {
        var manualReboot = false
        val card = device.currentDeviceCard.value

        if (!card.noMount) {
            if (!File("$SDCARD_PATH/Windows/boot.img").exists()) {
                val result = dumpBoot(ErrorType.QUICKBOOT_ERROR, BootBackupState.WINDOWS)
                if (!result.isSuccess) {
                    reportError(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }

            if (!card.noModem) {
                val result = dumpModem()
                if (!result.isSuccess) {
                    reportError(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }

            if (card.sensors && !checkSensors()) {
                val result = dumpSensors()
                if (!result.isSuccess) {
                    reportError(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }
        }

        if (!File("$SDCARD_PATH/boot.img").exists()) {
            val result = dumpBoot(ErrorType.QUICKBOOT_ERROR, BootBackupState.ANDROID)
            if (!result.isSuccess) {
                reportError(ErrorType.QUICKBOOT_ERROR, result)
                return@withContext
            }
        }

        val result = flashUEFI(uefiPath)
        if (!result.isSuccess) {
            reportError(ErrorType.QUICKBOOT_ERROR, result)
            return@withContext
        }

        if (!manualReboot) {
            RootCommandExecutor.exec("svc power reboot")
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    M3KApp,
                    R.string.manual_reboot_toast.string(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    suspend fun withMountedWindows(
        type: ErrorType,
        block: suspend () -> Unit
    ): Boolean = mutex.withLock {
        val wasMounted = isMounted()
        val noMount = device.currentDeviceCard.value.noMount
        if (wasMounted == MountStatus.NOT_MOUNTED && !noMount) {
            val res = mountWindows()
            if (!res.isSuccess) {
                reportError(type, res)
                if (type != ErrorType.MOUNT_ERROR) return@withLock false
            }
        }
        try {
            block()
        } finally {
            if (wasMounted == MountStatus.NOT_MOUNTED && !noMount) {
                val res = umountWindows()
                if (!res.isSuccess) reportError(type, res)
            }
        }
        return@withLock true
    }

    private fun reportError(type: ErrorType, result: CommandResult) {
        val errorDetail = buildString {
            if (result.command.isNotEmpty()) {
                append("[${result.command}] ")
            }
            val stderr = result.error.firstOrNull()
            val stdout = result.output.firstOrNull()
            when {
                stderr != null && stdout != null -> append("$stderr\n$stdout")
                stderr != null -> append(stderr)
                stdout != null -> append(stdout)
                else -> append(M3KApp.getString(R.string.unknown_error))
            }
        }
        Log.e(TAG, "Command failed: type=$type, command=${result.command}, error=$errorDetail")
        val title = when (type) {
            ErrorType.MOUNT_ERROR -> M3KApp.getString(R.string.mnt_error_title)
            ErrorType.BOOTBACKUP_ERROR -> M3KApp.getString(R.string.backupboot_error)
            ErrorType.QUICKBOOT_ERROR -> M3KApp.getString(R.string.quickboot_error_title)
        }
        commandError.value = com.remtrik.m3khelper.util.variables.CommandError(
            type = type,
            title = title,
            message = errorDetail
        )
    }
}

fun Context.restart() {
    runCatching {
        packageManager.getLaunchIntentForPackage(packageName)?.let {
            startActivity(Intent.makeRestartActivityTask(it.component))
        }
    }.onFailure { e -> Log.e("M3K Helper", "restart failed", e) }
}

data class CommandResult(
    val isSuccess: Boolean,
    val output: List<String>,
    val error: List<String>,
    val command: String = ""
)

enum class ErrorType { MOUNT_ERROR, BOOTBACKUP_ERROR, QUICKBOOT_ERROR }

enum class MountStatus { NOT_MOUNTED, MOUNTED }

enum class BootBackupState { NONE, ANDROID, WINDOWS, BOTH }
