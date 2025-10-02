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
}

-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}