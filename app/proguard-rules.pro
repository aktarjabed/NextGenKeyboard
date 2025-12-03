# NextGen Keyboard ProGuard Rules

# Keep all classes in main package
-keep class com.nextgen.keyboard.** { *; }

# Hilt
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# Timber
-dontwarn org.jetbrains.annotations.**
-keep class timber.log.** { *; }

# DataStore
-keep class androidx.datastore.*.** { *; }

# InputMethodService
-keep class * extends android.inputmethodservice.InputMethodService { *; }
-keep class android.view.inputmethod.** { *; }

# Keep data models
-keep class com.nextgen.keyboard.data.model.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
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
    public static *** i(...);
}