package com.remtrik.m3khelper.util.variables

import android.annotation.SuppressLint
import android.content.SharedPreferences
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
import java.io.File

private const val TAG = "M3K: Variables"

@Parcelize
data class UEFICard(var uefiPath: String, val uefiType: Int) : Parcelable

class DeviceCommands(var mountPath: String = "")

class DeviceData(
    val currentDeviceCard: MutableStateFlow<DeviceCard> = MutableStateFlow(unknownCard),
    val deviceCodenames: List<String> =
        listOfNotNull(
            Build.DEVICE,
            ShellUtils.fastCmd("getprop ro.product.device"),
            ShellUtils.fastCmd("getprop ro.lineage.device")
        ).distinct(),
    val savedDeviceCard: MutableStateFlow<DeviceCard> = MutableStateFlow(
        deviceCardsArray.getOrElse(prefs.getInt("saved_device_card", 0)) { unknownCard }
    ),
    val ram: String = getMemory(),
    val slot: String = ShellUtils.fastCmd("getprop ro.boot.slot_suffix"),
    val panelType: MutableStateFlow<String> = MutableStateFlow(
        prefs.getString("saved_device_panel", string.unknown_panel.string()).orEmpty()
    ),
    val uefiCards: MutableStateFlow<List<UEFICard>> = MutableStateFlow(emptyList()),
    val isSpecial: MutableStateFlow<Boolean> = MutableStateFlow(false),
)

val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

val device: DeviceData by lazy { DeviceData() }

val SDCARD_PATH: String by lazy { Environment.getExternalStorageDirectory().path }

class PrefSetting<T>(
    private val key: String,
    private val default: T,
    private val read: SharedPreferences.(String, T) -> T,
    private val write: SharedPreferences.Editor.(String, T) -> Unit
) {
    val flow: MutableStateFlow<T> = MutableStateFlow(
        with(prefs) { read(key, default) }
    )

    fun update(value: T) {
        flow.value = value
        appScope.launch(Dispatchers.IO) {
            prefs.edit { write(key, value) }
        }
    }

    operator fun component1(): T = flow.value
}

object AppSettings {
    val checkUpdate: PrefSetting<Boolean> = PrefSetting(
        "check_update",
        true,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )
    val forceRotation: PrefSetting<Boolean> = PrefSetting(
        "force_rotation",
        false,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )
    val overrideDevice: PrefSetting<Boolean> = PrefSetting(
        "override_device",
        false,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )
    val overriddenDeviceName: PrefSetting<String?> = PrefSetting(
        "overridden_device_name",
        "Poco X3 Pro",
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )
    val overriddenDeviceCodename: PrefSetting<String?> = PrefSetting(
        "overridden_device_codename",
        "vayu",
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )

    // Theme Settings
    val themeEngineEnable: PrefSetting<Boolean> = PrefSetting(
        "theme_engine_enable",
        false,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )
    val themeEngineEnableMaterialU: PrefSetting<Boolean> = PrefSetting(
        "theme_engine_enable_materialu",
        true,
        SharedPreferences::getBoolean,
        SharedPreferences.Editor::putBoolean
    )
    val themeEnginePaletteStyle: PrefSetting<String?> = PrefSetting(
        "theme_engine_palette_style",
        "TonalSpot",
        SharedPreferences::getString,
        SharedPreferences.Editor::putString
    )
    val themeEngineColorR: PrefSetting<Float> = PrefSetting(
        "theme_engine_color_R",
        0f,
        SharedPreferences::getFloat,
        SharedPreferences.Editor::putFloat
    )
    val themeEngineColorG: PrefSetting<Float> = PrefSetting(
        "theme_engine_color_G",
        0f,
        SharedPreferences::getFloat,
        SharedPreferences.Editor::putFloat
    )
    val themeEngineColorB: PrefSetting<Float> = PrefSetting(
        "theme_engine_color_B",
        0f,
        SharedPreferences::getFloat,
        SharedPreferences.Editor::putFloat
    )
}

val currentDeviceCommands: DeviceCommands by lazy { DeviceCommands() }

// UI State
val bootIsPresent: MutableStateFlow<BootBackupState> = MutableStateFlow(BootBackupState.NONE)
val windowsIsPresent: MutableStateFlow<Int> = MutableStateFlow(string.no)
val showWarningCard: MutableStateFlow<Boolean> = MutableStateFlow(true)

data class CommandError(
    val type: ErrorType,
    val title: String,
    val message: String
)

val commandError: MutableStateFlow<CommandError?> = MutableStateFlow(null)

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

        currentDeviceCommands.mountPath = SDCARD_PATH

        dynamicVars()

        if (BuildConfig.DEBUG) {
            debugLog()
        }
    }
}

fun fetchDeviceCard() {
    val codenames = device.deviceCodenames
    val match = deviceCardsArray.find { card ->
        card.deviceCodename.any { it in codenames }
    }
    if (match != null) {
        updateDeviceCard(deviceCardsArray.indexOf(match), match)
    }
}

private fun updateDeviceCard(cardNum: Int, card: DeviceCard) {
    device.currentDeviceCard.value = card
    device.savedDeviceCard.value = card
    device.isSpecial.value = card in specialDeviceCardsArray
    showWarningCard.value = false
    prefs.edit {
        putInt("saved_device_card", cardNum)
        putBoolean("firstboot", false)
        putBoolean("unknown", false)
    }
}

fun fastLoadSavedDevice(override: Boolean = AppSettings.overrideDevice.flow.value) {
    device.currentDeviceCard.value = if (override) {
        deviceCardsArray.find {
            it.deviceCodename.contains(AppSettings.overriddenDeviceCodename.flow.value)
        } ?: device.savedDeviceCard.value
    } else {
        device.savedDeviceCard.value
    }
    if (device.panelType.value == string.unknown_panel.string()) getPanel()
    device.isSpecial.value = device.currentDeviceCard.value in specialDeviceCardsArray
    showWarningCard.value = false
}

private fun getPanel() {
    device.panelType.value =
        getPanelNative().takeUnless { it == "Invalid" } ?: string.unknown_panel.string()
    prefs.edit { putString("saved_device_panel", device.panelType.value) }
}

private val samsungPanelMarkers = listOf("samsung", "ea8076", "s6e3fc3", "ams646yd01")
private val huaxingPanelMarkers = listOf("j20s_42", "k82_42", "huaxing")
private val tianmaPanelMarkers = listOf("j20s_36", "tianma", "k82_36")

fun getPanelNative(): String {
    val cmdline = ShellUtils.fastCmd("cat /proc/cmdline")
    if (cmdline.isEmpty()) return "Unknown"

    val panelInfo = cmdline.substringAfter("msm_drm", "")
        .substringBefore("android", "")
        .ifEmpty { return "Software" }
        .lowercase()

    return when {
        samsungPanelMarkers.any { panelInfo.contains(it) } -> "Samsung"
        huaxingPanelMarkers.any { panelInfo.contains(it) } -> "Huaxing"
        tianmaPanelMarkers.any { panelInfo.contains(it) } -> "Tianma"
        panelInfo.contains("ebbg") -> "EBBG"
        else -> "Invalid"
    }
}

fun bootBackupStatus(forceMount: Boolean = true) {
    appScope.launch {
        if (forceMount) {
            commandHandler.withMountedWindows(ErrorType.MOUNT_ERROR) {
                bootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
            }
        } else {
            bootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
        }
    }
}

fun dynamicVars() {
    appScope.launch {
        commandHandler.withMountedWindows(ErrorType.MOUNT_ERROR) {
            windowsIsPresent.value = if (
                File("$SDCARD_PATH/Windows/Windows/explorer.exe").exists()
            ) string.yes else string.no
            bootIsPresent.value = checkBootImages(device.currentDeviceCard.value.noMount)
        }
        if (device.uefiCards.value.isEmpty()) {
            val find = Shell.cmd("find /mnt/sdcard/UEFI/ -type f -name '*.img'").exec()
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
    val androidExists = File("$SDCARD_PATH/boot.img").exists()
    val windowsExists = File("$SDCARD_PATH/Windows/boot.img").exists()

    return when {
        !noMount && windowsExists -> if (androidExists) BootBackupState.BOTH else BootBackupState.WINDOWS
        androidExists -> BootBackupState.ANDROID
        else -> BootBackupState.NONE
    }
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
    Log.i(TAG, "Boot is present: ${bootIsPresent.value}")
    Log.i(TAG, "Windows is present: ${windowsIsPresent.value.string()}")
    Log.i(TAG, "Panel Type: ${device.panelType.value}")
    device.deviceCodenames
        .filter { it.isNotEmpty() }
        .forEach { Log.i(TAG, "Device codename: $it") }
    Log.i(TAG, "Current device: ${device.currentDeviceCard.value.deviceName}")
    Log.i(TAG, "Saved device: ${device.savedDeviceCard.value.deviceName}")
    Log.i(TAG, "Override device enabled: ${AppSettings.overrideDevice.flow.value}")
    if (AppSettings.overrideDevice.flow.value) {
        Log.i(TAG, "Override device codename: ${AppSettings.overriddenDeviceCodename.flow.value}")
    }
    Log.i(TAG, "Current mount path: ${currentDeviceCommands.mountPath}")
}
