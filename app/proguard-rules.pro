# NextGen Keyboard ProGuard Rules

# ----------------------------------------------------------------------------
# Application Rules
# ----------------------------------------------------------------------------

# Keep all classes in main package
-keep class com.aktarjabed.nextgenkeyboard.** { *; }

# Keep data models
-keep class com.aktarjabed.nextgenkeyboard.data.model.** { *; }

# Keep keyboard service (Required for Manifest reference)
-keep class com.aktarjabed.nextgenkeyboard.service.NextGenKeyboardService { *; }

# Keep Room entities and DAOs (Required for Reflection)
-keep class com.aktarjabed.nextgenkeyboard.data.model.Clip { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.ClipboardDao { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.NextGenDatabase { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.LearnedWordEntity { *; }
-keep class com.aktarjabed.nextgenkeyboard.data.local.LearnedWordDao { *; }

# ----------------------------------------------------------------------------
# Library Rules
# ----------------------------------------------------------------------------

# Hilt
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class * extends androidx.hilt.work.HiltWorkerFactory
-keep class * extends androidx.hilt.work.HiltWorker

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
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# DataStore
-keep class androidx.datastore.*.** { *; }

# InputMethodService
-keep class * extends android.inputmethodservice.InputMethodService { *; }
-keep class android.view.inputmethod.** { *; }

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Giphy SDK
-keep class com.giphy.sdk.** { *; }
-dontwarn com.giphy.sdk.**

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
    public static *** i(...);
}
