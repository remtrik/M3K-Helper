# libsu
-keep class com.topjohnwu.superuser.** { *; }
-keep class com.topjohnwu.superuser.io.** { *; }
-keep class com.topjohnwu.superuser.nio.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# MaterialKolor
-keep class com.materialkolor.** { *; }

# Compose Destinations
-keep class com.ramcosta.composedestinations.** { *; }
-keep class com.ramcosta.composedestinations.generated.** { *; }
