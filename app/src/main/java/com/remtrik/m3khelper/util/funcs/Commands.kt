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
import com.remtrik.m3khelper.util.variables.showBootBackupErrorDialog
import com.remtrik.m3khelper.util.variables.showMountErrorDialog
import com.remtrik.m3khelper.util.variables.showQuickBootErrorDialog
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

internal val BlockWindowsPath by lazy {
    ShellUtils.fastCmd("readlink -fn /dev/block/bootdevice/by-name/win")
}

private const val TAG = "M3K: Commands"

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

    private fun toCommandResult(res: Shell.Result?): CommandResult {
        return res?.let {
            CommandResult(it.isSuccess, it.out.toMutableList(), it.err.toMutableList())
        } ?: CommandResult(
            false,
            mutableListOf(R.string.mount_error_default.string()),
            mutableListOf(R.string.mount_error_default.string())
        )
    }

    suspend fun dumpBoot(type: ErrorType, where: BootBackupState): CommandResult = withContext(Dispatchers.IO) {
            val result: Shell.Result? = when (where) {
                BootBackupState.WINDOWS -> {
                    var innerRes: Shell.Result? = null
                    val ok = withMountedWindows(type) {
                        val target = File("$SDCARD_PATH/Windows/boot.img").canonicalFile
                        innerRes = RootCommandExecutor.exec(
                            "dd if=/dev/block/bootdevice/by-name/boot${device.slot} of=${target.path} bs=32M"
                        )
                        bootBackupStatus()
                    }
                    if (!ok) return@withContext CommandResult(
                        false,
                        mutableListOf(R.string.mount_error_default.string()),
                        mutableListOf(R.string.mount_error_default.string())
                    )
                    innerRes
                }

                BootBackupState.ANDROID -> {
                    val target = File("$SDCARD_PATH/boot.img").canonicalFile
                    val res = RootCommandExecutor.exec(
                        "dd if=/dev/block/bootdevice/by-name/boot${device.slot} of=${target.path}"
                    )
                    bootBackupStatus(forceMount = false)
                    res
                }

                else -> return@withContext CommandResult(
                    false,
                    mutableListOf("Invalid 'where' arg"),
                    mutableListOf("Invalid 'where' arg")
                )
            }
            toCommandResult(result)
        }

    suspend fun mountWindows(): CommandResult = withContext(Dispatchers.IO) {
        RootCommandExecutor.exec("mkdir -p ${SDCARD_PATH}/Windows")
        val res = RootCommandExecutor.exec(
            "su -mm -c mount.ntfs /dev/block/by-name/win ${SDCARD_PATH}/Windows"
        )
        toCommandResult(res)
    }

    suspend fun umountWindows(): CommandResult = withContext(Dispatchers.IO) {
        val res = RootCommandExecutor.exec("su -mm -c umount ${SDCARD_PATH}/Windows")
        toCommandResult(res)
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
        var check = false
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            val out = ShellUtils.fastCmd(
                "find ${SDCARD_PATH}/Windows/Windows/System32/Drivers/DriverData/QUALCOMM/fastRPC/vendor/etc/sensors/*"
            )
            check = out.isNotEmpty()
        }
        check
    }

    suspend fun dumpSensors(): CommandResult = withContext(Dispatchers.IO) {
        var res: Shell.Result? = null
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            res = RootCommandExecutor.exec(
                "cp -r /vendor/etc/sensors/* ${SDCARD_PATH}/Windows/Windows/System32/Drivers/DriverData/QUALCOMM/fastRPC/vendor/etc/sensors"
            )
        }
        toCommandResult(res)
    }

    suspend fun dumpModem(): CommandResult = withContext(Dispatchers.IO) {
        var res: Shell.Result? = null
        withMountedWindows(ErrorType.QUICKBOOT_ERROR) {
            val path = ShellUtils.fastCmd(
                "find ${SDCARD_PATH}/Windows/Windows/System32/DriverStore/FileRepository -name qcremotefs8150.inf_arm64_*"
            )
            res = if (path.isEmpty()) {
                Shell.cmd("echo 'modem path not found'").exec()
            } else {
                RootCommandExecutor.exec(
                    "dd if=/dev/block/bootdevice/by-name/modemst1 of=$path/bootmodem_fs1 bs=8388608 && " +
                            "dd if=/dev/block/bootdevice/by-name/modemst2 of=$path/bootmodem_fs2 bs=8388608"
                )
            }
        }
        toCommandResult(res)
    }

    private suspend fun uefitell(uefiFile: File): Shell.Result {
        return RootCommandExecutor.exec(
            "dd if=${uefiFile.path} of=/dev/block/bootdevice/by-name/boot${device.slot} bs=32M"
        )
    }

    suspend fun flashUEFI(uefiPath: String): CommandResult = withContext(Dispatchers.IO) {
        val file = File(uefiPath).canonicalFile
        val res = uefitell(file)
        toCommandResult(res)
    }

    suspend fun quickBoot(uefiPath: String): Unit = withContext(Dispatchers.IO) {
        var manualReboot = false
        if (!device.currentDeviceCard.value.noMount) {
            if (ShellUtils.fastCmd("find ${SDCARD_PATH}/Windows/boot.img").isEmpty()) {
                val result = dumpBoot(ErrorType.QUICKBOOT_ERROR, BootBackupState.WINDOWS)
                if (!result.isSuccess) {
                    handleErrorType(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }

            if (!device.currentDeviceCard.value.noModem) {
                val result = dumpModem()
                if (!result.isSuccess) {
                    handleErrorType(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }

            if (device.currentDeviceCard.value.sensors && !checkSensors()) {
                val result = dumpSensors()
                if (!result.isSuccess) {
                    handleErrorType(ErrorType.QUICKBOOT_ERROR, result)
                    manualReboot = true
                }
            }
        }
        if (ShellUtils.fastCmd("find ${SDCARD_PATH}/boot.img").isEmpty()) {
            val result = dumpBoot(ErrorType.QUICKBOOT_ERROR, BootBackupState.ANDROID)
            if (!result.isSuccess) {
                handleErrorType(ErrorType.QUICKBOOT_ERROR, result)
                return@withContext
            }
        }

        val result = flashUEFI(uefiPath)
        if (!result.isSuccess) {
            handleErrorType(ErrorType.QUICKBOOT_ERROR, result)
            return@withContext
        }

        if (!manualReboot) {
            RootCommandExecutor.exec("svc power reboot")
        } else {
            MainScope().launch {
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
        if (wasMounted == MountStatus.NOT_MOUNTED && !device.currentDeviceCard.value.noMount) {
            val res = mountWindows()
            if (!res.isSuccess) {
                handleErrorType(type, res)
                if (type != ErrorType.MOUNT_ERROR) {
                    return@withLock false
                }
            }
        }
        try {
            block()
        } finally {
            if (wasMounted == MountStatus.NOT_MOUNTED && !device.currentDeviceCard.value.noMount) {
                val res = umountWindows()
                if (!res.isSuccess) {
                    handleErrorType(type, res)
                }
            }
        }
        return@withLock true
    }

    private fun handleErrorType(type: ErrorType, result: CommandResult) {
        commandError.value = result.output.firstOrNull() ?: ""
        when (type) {
            ErrorType.MOUNT_ERROR -> showMountErrorDialog.value = true
            ErrorType.BOOTBACKUP_ERROR -> showBootBackupErrorDialog.value = true
            ErrorType.QUICKBOOT_ERROR -> showQuickBootErrorDialog.value = true
        }
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
    val output: MutableList<String>,
    val error: MutableList<String>
)

enum class ErrorType { MOUNT_ERROR, BOOTBACKUP_ERROR, QUICKBOOT_ERROR }

enum class MountStatus { NOT_MOUNTED, MOUNTED }

enum class BootBackupState { NONE, ANDROID, WINDOWS, BOTH }
