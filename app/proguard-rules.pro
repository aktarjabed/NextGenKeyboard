# Add project specific ProGuard rules here.

# Keep keyboard service (Required for Manifest reference)
-keep class com.aktarjabed.nextgenkeyboard.service.NextGenKeyboardService { *; }

# Keep all Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends androidx.hilt.work.HiltWorkerFactory
-keep class * extends androidx.hilt.work.HiltWorker

# Keep Room entities (Required for Reflection)
-keep class com.aktarjabed.nextgenkeyboard.data.model.ClipboardEntity { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.ClipboardDatabase { *; }

# Keep security classes (Prevent obfuscation of encryption logic if using reflection, otherwise obfuscate)
# Removed -keep class com.aktarjabed.nextgenkeyboard.security.** { *; } to allow obfuscation

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Giphy SDK
-keep class com.giphy.sdk.** { *; }
-dontwarn com.giphy.sdk.**

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# General Android
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
}