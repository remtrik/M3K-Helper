package com.remtrik.m3khelper.util

import android.os.Parcelable
import com.remtrik.m3khelper.BuildConfig
import com.remtrik.m3khelper.R.drawable
import com.remtrik.m3khelper.R.string.unknown_device
import com.remtrik.m3khelper.util.funcs.string
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceCard(
    val deviceCodename: List<String>,
    val deviceName: String,
    val deviceImage: Int,
    val deviceGuide: String,
    val groupLink: String,
    val driversLink: String,
    val uefiLink: String,
    val noModem: Boolean, val noFlash: Boolean,
    val noBoot: Boolean, val noMount: Boolean,
    val sensors: Boolean, val noGuide: Boolean,
    val noGroup: Boolean, val noDrivers: Boolean,
    val noUEFI: Boolean, val unifiedDriversUEFI: Boolean,
    val noLinks: Boolean = false
) : Parcelable

val vayuCard: DeviceCard = DeviceCard(
    listOf("vayu", "bhima"),
    "POCO X3 Pro",
    drawable.vayu,
    "https://github.com/WaLoVayu/POCOX3Pro-Windows-Guides",
    "https://t.me/WaLoVayu",
    "https://github.com/remtrik-stuff/POCOX3Pro-Windows-Releases/releases/latest",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)

val nabuCard: DeviceCard = DeviceCard(
    listOf("nabu"),
    "Xiaomi Pad 5",
    drawable.nabu,
    "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5",
    "https://t.me/nabuwoa",
    "https://github.com/remtrik-stuff/MiPad5-Windows-Releases/releases/latest",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)

val raphaelCard: DeviceCard = DeviceCard(
    listOf("raphael"),
    "Xiaomi Mi 9T Pro",
    drawable.raphael,
    "https://github.com/graphiks/woa-raphael",
    "https://t.me/woaraphael",
    "https://github.com/woa-raphael/raphael-drivers/releases/latest",
    "https://github.com/woa-raphael/woa-raphael/releases/tag/raphael-uefi",
    noModem = false, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = false
)

val raphaelinCard: DeviceCard =
    raphaelCard.copy(deviceCodename = listOf("raphaelin"), deviceName = "Redmi K20 Pro")
val raphaelsCard: DeviceCard =
    raphaelCard.copy(deviceCodename = listOf("raphaels"), deviceName = "Redmi K20 Pro Premium")

val cepheusCard: DeviceCard = DeviceCard(
    listOf("cepheus"),
    "Xiaomi Mi 9",
    drawable.cepheus,
    "https://github.com/ivnvrvnn/Port-Windows-XiaoMI-9",
    "https://t.me/woacepheus",
    "https://github.com/qaz6750/XiaoMi9-Drivers/releases/latest",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)

val berylliumCard: DeviceCard = DeviceCard(
    listOf("beryllium"),
    "POCO F1",
    drawable.beryllium,
    "https://github.com/n00b69/woa-beryllium",
    "https://t.me/WinOnF1",
    "https://github.com/n00b69/woa-beryllium/releases/tag/Drivers",
    "https://github.com/n00b69/woa-beryllium/releases/tag/UEFI",
    noModem = false, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = false
)

val miatollCard: DeviceCard = DeviceCard(
    listOf("miatoll", "durandal", "curtana_india", "joyeuse", "miatoll_mainline"),
    "Redmi Note 9 Pro",
    drawable.miatoll,
    "https://github.com/woa-miatoll/Port-Windows-11-Redmi-Note-9-Pro",
    "http://t.me/woamiatoll",
    "https://github.com/woa-miatoll/Miatoll-Releases/releases/latest",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)
val curtanaCard: DeviceCard =
    miatollCard.copy(deviceCodename = listOf("curtana"), deviceName = "Redmi Note 9S")
val excaliburCard: DeviceCard =
    miatollCard.copy(deviceCodename = listOf("excalibur"), deviceName = "Redmi Note 9 Pro Max")
val gramCard: DeviceCard =
    miatollCard.copy(deviceCodename = listOf("gram"), deviceName = "POCO M2 Pro")

val alphaCard: DeviceCard = DeviceCard(
    listOf("alpha", "alphalm"),
    "LG G8",
    drawable.alpha,
    "https://github.com/n00b69/woa-alphaplus",
    "https://t.me/lgedevices",
    "https://github.com/n00b69/woa-alphaplus/releases/tag/Drivers",
    "https://github.com/n00b69/woa-alphaplus/releases/tag/UEFI",
    noModem = false, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = false
)

val mh2lm5gCard: DeviceCard = DeviceCard(
    listOf("mh2lm5g"),
    "LG V50S",
    drawable.mh2,
    "https://github.com/n00b69/woa-mh2lm5g",
    "https://t.me/lgedevices",
    "https://github.com/n00b69/woa-mh2lm5g/releases/tag/Drivers",
    "https://github.com/n00b69/woa-mh2lm5g/releases/tag/UEFI",
    noModem = false, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = false
)

val mh2Card: DeviceCard = mh2lm5gCard.copy(
    deviceCodename = listOf("mh2", "mh2lm"),
    deviceName = "LG G8X",
    deviceGuide = "https://github.com/n00b69/woa-mh2lm",
    uefiLink = "https://github.com/n00b69/woa-mh2lm/releases/tag/UEFI",
    driversLink = "https://github.com/n00b69/woa-mh2lm5g/releases/tag/Drivers"
)

val betaCard: DeviceCard = DeviceCard(
    listOf("beta", "betalm"),
    "LG G8S",
    drawable.beta,
    "https://github.com/n00b69/woa-betalm",
    "https://t.me/lgedevices",
    "https://github.com/n00b69/woa-betalm/releases/tag/Drivers",
    "https://github.com/n00b69/woa-betalm/releases/tag/UEFI",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = false
)

val flashCard: DeviceCard = mh2lm5gCard.copy(
    deviceCodename = listOf("flash", "flashlm"),
    deviceName = "LG V50",
    deviceGuide = "https://github.com/n00b69/woa-flashlmdd",
    deviceImage = drawable.flashlm
)

val guacamoleCard: DeviceCard = DeviceCard(
    listOf("guacamole", "OnePlus7Pro"),
    "OnePlus 7 Pro",
    drawable.guacamole,
    "",
    "https://t.me/onepluswoachat",
    "",
    "",
    noModem = true, noFlash = true,
    noBoot = true, noMount = false,
    sensors = false, noGuide = true,
    noGroup = false, noDrivers = true,
    noUEFI = true, unifiedDriversUEFI = false
)

val hotdogCard: DeviceCard = guacamoleCard.copy(
    deviceCodename = listOf("hotdog", "OnePlus7TPro"),
    deviceName = "OnePlus 7T Pro",
    deviceImage = drawable.hotdog
)

val suryaCard: DeviceCard = DeviceCard(
    listOf("surya", "karna"),
    "POCO X3",
    drawable.vayu,
    "https://github.com/woa-surya/POCOX3NFC-Guides",
    "https://t.me/windows_on_pocox3_nfc",
    "",
    "",
    noModem = true, noFlash = true,
    noBoot = true, noMount = false,
    sensors = false, noGuide = true,
    noGroup = true, noDrivers = true,
    noUEFI = true, unifiedDriversUEFI = true,
    noLinks = true
)

val a52sxqCard: DeviceCard = DeviceCard(
    listOf("a52sxq"),
    "Samsung Galaxy A52s",
    drawable.a52sxq,
    "https://github.com/woa-a52s/Samsung-A52s-5G-Guides",
    "https://t.me/a52sxq_uefi",
    "https://github.com/woa-a52s/Samsung-A52s-5G-Releases/releases/latest",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)

val beyond1Card: DeviceCard = DeviceCard(
    listOf("beyond1"),
    "Samsung Galaxy S10",
    drawable.beyond1,
    "",
    "https://t.me/woahelperchat",
    "",
    "",
    noModem = true, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = true,
    noGroup = false, noDrivers = true,
    noUEFI = true, unifiedDriversUEFI = false
)

val debugCard: DeviceCard = DeviceCard(
    listOf("emu64xa"),
    "emu64xa",
    drawable.vayu,
    "https://google.com",
    "https://google.com",
    "https://google.com",
    "https://google.com",
    noModem = false, noFlash = false,
    noBoot = false, noMount = false,
    sensors = false, noGuide = false,
    noGroup = false, noDrivers = false,
    noUEFI = false, unifiedDriversUEFI = true
)

val unknownCard: DeviceCard = DeviceCard(
    listOf("unknown"),
    unknown_device.string(),
    drawable.ic_device_unknown,
    "",
    "",
    "",
    "",
    noModem = true, noFlash = true,
    noBoot = true, noMount = true,
    sensors = false, noGuide = true,
    noGroup = true, noDrivers = true,
    noUEFI = true, unifiedDriversUEFI = false,
    noLinks = true
)

val deviceCardsArray: Array<DeviceCard> =
    arrayOf(
        unknownCard,
        vayuCard,
        suryaCard,
        nabuCard,
        raphaelCard, raphaelinCard, raphaelsCard,
        cepheusCard,
        berylliumCard,
        curtanaCard, excaliburCard, gramCard, miatollCard,
        alphaCard,
        mh2lm5gCard,
        mh2Card,
        betaCard,
        flashCard,
        guacamoleCard,
        hotdogCard,
        a52sxqCard,
        if (BuildConfig.DEBUG) debugCard else beyond1Card
    )

val specialDeviceCardsArray: Array<DeviceCard> =
    arrayOf(
        if (BuildConfig.DEBUG) debugCard else nabuCard
    )