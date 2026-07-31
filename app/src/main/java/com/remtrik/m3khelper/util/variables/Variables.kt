package com.remtrik.m3khelper.util.variables

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.remtrik.m3khelper.BuildConfig
import com.remtrik.m3khelper.R.string
import com.remtrik.m3khelper.prefs
import com.remtrik.m3khelper.util.DeviceCard
import com.remtrik.m3khelper.util.deviceCardsArray
import com.remtrik.m3khelper.util.funcs.BootBackupState
import com.remtrik.m3khelper.util.funcs.Commands
import com.remtrik.m3khelper.util.funcs.ErrorType
import com.remtrik.m3khelper.util.funcs.string
import com.remtrik.m3khelper.util.specialDeviceCardsArray
import com.remtrik.m3khelper.util.unknownCard
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

private const val TAG = "M3K: Variables"

@Parcelize
data class UEFICard(var uefiPath: String, val uefiType: Int) : Parcelable

@Parcelize
data class DeviceCommands(var mountPath: String = "") : Parcelable

data class DeviceData(
    val currentDeviceCard: MutableStateFlow<DeviceCard> = MutableStateFlow(unknownCard),
    val deviceCodenames: List<String> =
        listOf(
            Build.DEVICE,
            ShellUtils.fastCmd("getprop ro.product.device"),
            ShellUtils.fastCmd("getprop ro.lineage.device")
        ).filter { it.isNotEmpty() }.distinct(),
    val savedDeviceCard: MutableStateFlow<DeviceCard> = MutableStateFlow(
        deviceCardsArray.getOrNull(prefs.getInt("saved_device_card", 0))
            ?: unknownCard
    ),
    val overrideDeviceCard: MutableStateFlow<Boolean> =
        MutableStateFlow(prefs.getBoolean("override_device", false)),
    val ram: String = getMemory(),
    val slot: String = ShellUtils.fastCmd("getprop ro.boot.slot_suffix"),
    val panelType: MutableStateFlow<String> = MutableStateFlow(
        prefs.getString("saved_device_panel", string.unknown_panel.string()).toString()
    ),
    val uefiCards: MutableStateFlow<List<UEFICard>> = MutableStateFlow(emptyList()),
    val isSpecial: MutableStateFlow<Boolean> = MutableStateFlow(false),
)

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

val device: DeviceData by lazy { DeviceData() }

val SDCARD_PATH: String by lazy { Environment.getExternalStorageDirectory().path }
//const val SDCARD_PATH: String = "/sdcard"

object AppSettings {
    val checkUpdate = MutableStateFlow(prefs.getBoolean("check_update", true))
    val forceRotation = MutableStateFlow(prefs.getBoolean("force_rotation", false))
    val overrideDevice = MutableStateFlow(prefs.getBoolean("override_device", false))
    val overridenDeviceName = MutableStateFlow(prefs.getString("overriden_device_name", "Poco X3 Pro") ?: "Poco X3 Pro")
    val overridenDeviceCodename = MutableStateFlow(prefs.getString("overriden_device_codename", "vayu") ?: "vayu")

    // Theme Settings
    val themeEngineEnable = MutableStateFlow(prefs.getBoolean("theme_engine_enable", false))
    val themeEngineEnableMaterialU = MutableStateFlow(prefs.getBoolean("theme_engine_enable_materialu", true))
    val themeEnginePaletteStyle = MutableStateFlow(prefs.getString("theme_engine_palette_style", "TonalSpot") ?: "TonalSpot")
    val themeEngineColorR = MutableStateFlow(prefs.getFloat("theme_engine_color_R", 0f))
    val themeEngineColorG = MutableStateFlow(prefs.getFloat("theme_engine_color_G", 0f))
    val themeEngineColorB = MutableStateFlow(prefs.getFloat("theme_engine_color_B", 0f))

    fun <T> update(key: String, value: T, flow: MutableStateFlow<T>) {
        flow.value = value
        appScope.launch(Dispatchers.IO) {
            prefs.edit {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Float -> putFloat(key, value)
                    is Int -> putInt(key, value)
                }
            }
        }
    }

    fun <T> liveUpdate(value: T, flow: MutableStateFlow<T>) {
        flow.value = value
    }
}

val currentDeviceCommands: DeviceCommands by lazy { DeviceCommands() }

// UI State
val BootIsPresent = MutableStateFlow(BootBackupState.NONE)
val WindowsIsPresent = MutableStateFlow(string.no)
val showWarningCard = MutableStateFlow(true)
val commandError = MutableStateFlow("")
val showBootBackupErrorDialog = MutableStateFlow(false)
val showMountErrorDialog = MutableStateFlow(false)
val showQuickBootErrorDialog = MutableStateFlow(false)

val commandHandler: Commands = object : Commands() {}

// ui defaults
var FontSize: TextUnit = 0.sp
var PaddingValue: Dp = 0.dp
var LineHeight: TextUnit = 0.sp

// App State
val firstBoot: Boolean get() = prefs.getBoolean("firstboot", true)

@SuppressLint("RestrictedApi")
fun vars() {
    appScope.launch {
        if (prefs.getString("version", "3.4") != BuildConfig.VERSION_NAME) {
            prefs.edit {
                putBoolean("firstboot", true)
                putString("version", BuildConfig.VERSION_NAME)
            }
            getPanel()
            fetchDeviceCard()
        } else {
            fastLoadSavedDevice()
        }

        // TODO: Examine the OS behavior with different paths
        /*CurrentDeviceCommands.mountPath = when {
        ShellUtils.fastCmd("find /mnt/pass_through -maxdepth 0")
            .isNotEmpty() -> "/mnt/pass_through/0/emulated/0" // passthrough+getExternalStorageDirectory maybe?
        else -> Environment.getExternalStorageDirectory().path
    }*/

        currentDeviceCommands.mountPath = SDCARD_PATH

        dynamicVars()

        if (BuildConfig.DEBUG) {
            debugLog()
        }
    }
}

fun fetchDeviceCard() {
    var tmp = 0
    deviceCardsArray
        .find { card ->
            card.deviceCodename.any { deviceCodename ->
                device.deviceCodenames.any { normalizedCodename -> normalizedCodename == deviceCodename }
            }
        }?.let { card -> updateDeviceCard(deviceCardsArray.indexOf(card), card); tmp = 1 }
    if (tmp != 1) { // fallback to at least somewhat close device if cant find the exact match
        deviceCardsArray
            .find { card -> card.deviceCodename.any { device.deviceCodenames.contains(it) } }
            ?.let { card ->
                updateDeviceCard(deviceCardsArray.indexOf(card), card)
            }
    }
}

private fun updateDeviceCard(cardNum: Int, card: DeviceCard) {
    device.currentDeviceCard.value = card
    prefs.edit {
        putInt("saved_device_card", cardNum)
        putBoolean("firstboot", false)
        putBoolean("unknown", false)
    }
    device.savedDeviceCard.value = card
    isSpecial(card)
    showWarningCard.value = false
}

fun fastLoadSavedDevice(override: Boolean = AppSettings.overrideDevice.value) {
    device.currentDeviceCard.value = if (override) {
        deviceCardsArray.find {
            it.deviceCodename.contains(
                AppSettings.overridenDeviceCodename.value
            )
        } ?: device.savedDeviceCard.value
    } else {
        device.savedDeviceCard.value
    }
    if (device.panelType.value == string.unknown_panel.string()) getPanel()
    isSpecial(device.currentDeviceCard.value)
    showWarningCard.value = false
}

private fun getPanel() {
    device.panelType.value =
        getPanelNative().takeUnless { it == "Invalid" } ?: string.unknown_panel.string()
    prefs.edit { putString("saved_device_panel", device.panelType.value) }
}

fun getPanelNative(): String {
    val cmdline = ShellUtils.fastCmd("cat /proc/cmdline")
    if (cmdline.isEmpty()) return "Unknown"

    val panelInfo = cmdline.substringAfter("msm_drm", "")
        .substringBefore("android", "")
        .ifEmpty { return "Software" }
        .lowercase()

    return when {
        listOf("samsung", "ea8076", "s6e3fc3", "ams646yd01").any { panelInfo.contains(it) } -> "Samsung"
        listOf("j20s_42", "k82_42", "huaxing").any { panelInfo.contains(it) } -> "Huaxing"
        listOf("j20s_36", "tianma", "k82_36").any { panelInfo.contains(it) } -> "Tianma"
        panelInfo.contains("ebbg") -> "EBBG"
        else -> "Invalid"
    }
}

fun bootBackupStatus(forceMount: Boolean = true) {
    appScope.launch {
        if (forceMount) {
            commandHandler.withMountedWindows(ErrorType.MOUNT_ERROR) {
                BootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
            }
        } else {
            BootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
        }
    }
}

fun dynamicVars() {
    appScope.launch {
        commandHandler.withMountedWindows(ErrorType.MOUNT_ERROR) {
            WindowsIsPresent.value = when {
                ShellUtils.fastCmd("find ${SDCARD_PATH}/Windows/Windows/explorer.exe")
                    .isNotEmpty() -> string.yes

                else -> string.no
            }
            BootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
        }
        if (device.uefiCards.value.isEmpty()) {
            val find = Shell.cmd("find /mnt/sdcard/UEFI/ -type f | grep .img").exec()
            if (find.isSuccess && device.uefiCards.value.isEmpty()) {
                device.uefiCards.value = find.out
                    .filter { it.contains("hz") }
                    .mapNotNull { path ->
                        when {
                            path.contains("120hz") -> UEFICard(path, 120)
                            path.contains("90hz") -> UEFICard(path, 90)
                            path.contains("60hz") -> UEFICard(path, 60)
                            else -> null
                        }
                    }
                    .ifEmpty { listOf(UEFICard(find.out.first(), 1)) }
            }
        }
    }
}

fun checkBootImages(noMount: Boolean): BootBackupState {

    val check = ShellUtils.fastCmd(
        "if [ -f $SDCARD_PATH/boot.img ]; then echo -n 'A'; fi; " +
                "if [ -f $SDCARD_PATH/Windows/boot.img ]; then echo -n 'W'; fi"
    )

    val androidExists = check.contains('A')
    val windowsExists = check.contains('W')

    return when {
        !noMount && windowsExists -> if (androidExists) BootBackupState.BOTH else BootBackupState.WINDOWS
        androidExists -> BootBackupState.ANDROID
        else -> BootBackupState.NONE
    }
}

private fun isSpecial(card: DeviceCard) {
    device.isSpecial.value = specialDeviceCardsArray.contains(card)
}

data class DeviceStrings(
    val woa: String,
    val model: String,
    val ram: String,
    val panel: String,
    val bootState: String?,
    val slot: String?,
    val windowsStatus: String?
)

@SuppressLint("LogConditional")
private fun debugLog() {
    Log.i(TAG, "First Boot: $firstBoot")
    Log.i(TAG, "Boot is present: ${BootIsPresent.value}")
    Log.i(TAG, "Windows is present: ${WindowsIsPresent.value.string()}")
    Log.i(TAG, "Panel Type: ${device.panelType.value}")
    device.deviceCodenames
        .filter { it.isNotEmpty() }
        .forEach { Log.i(TAG, "Device codename: $it") }
    Log.i(TAG, "Current device: ${device.currentDeviceCard.value.deviceName}")
    Log.i(TAG, "Saved device: ${device.savedDeviceCard.value.deviceName}")
    Log.i(TAG, "Override device enabled: ${AppSettings.overrideDevice.value}")
    if (AppSettings.overrideDevice.value) {
        Log.i(TAG, "Override device codename: ${AppSettings.overridenDeviceCodename.value}")
    }
    Log.i(TAG, "Current mount path: ${currentDeviceCommands.mountPath}")
}
