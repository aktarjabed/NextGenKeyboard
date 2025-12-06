package com.aktarjabed.nextgenkeyboard.feature.swipe

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class SwipePathProcessorTest {
    private lateinit var processor: SwipePathProcessor

    // Mock Context and Resources manually since we can't use Mockito easily without dependency
    // and we are in a limited environment.
    // However, if we can't run tests, we should at least make the code compilable.
    // To make it runable in a unit test environment without Robolectric or Mockito,
    // we need to be creative or assume Mockito is available (it is not in the list explicitly, but maybe transitive?).
    // 'junit:junit:4.13.2' is there. 'androidx.test.ext:junit' is for instrumentation.
    // I don't see mockito-core.

    // I will create a dummy Context wrapper if possible, or use reflection/subclassing if I can't mock.
    // But Context is an abstract class.

    // Plan: Create a MinimalMockContext class inside the test file to satisfy the constructor.

    class MinimalMockContext : Context() {
        override fun getResources(): Resources {
            return MinimalMockResources()
        }

        // Implement other abstract methods with TODO or throws,
        // we only need getResources().
        override fun getAssets(): android.content.res.AssetManager = throw NotImplementedError()
        override fun getTheme(): android.content.res.Resources.Theme = throw NotImplementedError()
        override fun getClassLoader(): ClassLoader = throw NotImplementedError()
        override fun getPackageName(): String = "com.test"
        override fun getApplicationInfo(): android.content.pm.ApplicationInfo = throw NotImplementedError()
        override fun getPackageResourcePath(): String = throw NotImplementedError()
        override fun getPackageCodePath(): String = throw NotImplementedError()
        override fun getSharedPreferences(name: String?, mode: Int): android.content.SharedPreferences = throw NotImplementedError()
        override fun moveSharedPreferencesFrom(context: Context?, name: String?): Boolean = throw NotImplementedError()
        override fun deleteSharedPreferences(name: String?): Boolean = throw NotImplementedError()
        override fun openFileInput(name: String?): java.io.FileInputStream = throw NotImplementedError()
        override fun openFileOutput(name: String?, mode: Int): java.io.FileOutputStream = throw NotImplementedError()
        override fun deleteFile(name: String?): Boolean = throw NotImplementedError()
        override fun getFileStreamPath(name: String?): java.io.File = throw NotImplementedError()
        override fun getDataDir(): java.io.File = throw NotImplementedError()
        override fun getFilesDir(): java.io.File = throw NotImplementedError()
        override fun getNoBackupFilesDir(): java.io.File = throw NotImplementedError()
        override fun getExternalFilesDir(type: String?): java.io.File? = throw NotImplementedError()
        override fun getExternalFilesDirs(type: String?): Array<java.io.File> = throw NotImplementedError()
        override fun getObbDir(): java.io.File? = throw NotImplementedError()
        override fun getObbDirs(): Array<java.io.File> = throw NotImplementedError()
        override fun getCacheDir(): java.io.File = throw NotImplementedError()
        override fun getCodeCacheDir(): java.io.File = throw NotImplementedError()
        override fun getExternalCacheDir(): java.io.File? = throw NotImplementedError()
        override fun getExternalCacheDirs(): Array<java.io.File> = throw NotImplementedError()
        override fun getExternalMediaDirs(): Array<java.io.File> = throw NotImplementedError()
        override fun fileList(): Array<String> = throw NotImplementedError()
        override fun getDir(name: String?, mode: Int): java.io.File = throw NotImplementedError()
        override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?): android.database.sqlite.SQLiteDatabase = throw NotImplementedError()
        override fun openOrCreateDatabase(name: String?, mode: Int, factory: android.database.sqlite.SQLiteDatabase.CursorFactory?, errorHandler: android.database.DatabaseErrorHandler?): android.database.sqlite.SQLiteDatabase = throw NotImplementedError()
        override fun moveDatabaseFrom(context: Context?, name: String?): Boolean = throw NotImplementedError()
        override fun deleteDatabase(name: String?): Boolean = throw NotImplementedError()
        override fun getDatabasePath(name: String?): java.io.File = throw NotImplementedError()
        override fun databaseList(): Array<String> = throw NotImplementedError()
        override fun getWallpaper(): android.graphics.drawable.Drawable = throw NotImplementedError()
        override fun peekWallpaper(): android.graphics.drawable.Drawable = throw NotImplementedError()
        override fun getWallpaperDesiredMinimumWidth(): Int = 0
        override fun getWallpaperDesiredMinimumHeight(): Int = 0
        override fun setWallpaper(bitmap: android.graphics.Bitmap?) = throw NotImplementedError()
        override fun setWallpaper(data: java.io.InputStream?) = throw NotImplementedError()
        override fun clearWallpaper() = throw NotImplementedError()
        override fun startActivity(intent: android.content.Intent?) = throw NotImplementedError()
        override fun startActivity(intent: android.content.Intent?, options: android.os.Bundle?) = throw NotImplementedError()
        override fun startActivities(intents: Array<android.content.Intent>?) = throw NotImplementedError()
        override fun startActivities(intents: Array<android.content.Intent>?, options: android.os.Bundle?) = throw NotImplementedError()
        override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int) = throw NotImplementedError()
        override fun startIntentSender(intent: android.content.IntentSender?, fillInIntent: android.content.Intent?, flagsMask: Int, flagsValues: Int, extraFlags: Int, options: android.os.Bundle?) = throw NotImplementedError()
        override fun sendBroadcast(intent: android.content.Intent?) = throw NotImplementedError()
        override fun sendBroadcast(intent: android.content.Intent?, receiverPermission: String?) = throw NotImplementedError()
        override fun sendOrderedBroadcast(intent: android.content.Intent?, receiverPermission: String?) = throw NotImplementedError()
        override fun sendOrderedBroadcast(intent: android.content.Intent?, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) = throw NotImplementedError()
        override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) = throw NotImplementedError()
        override fun sendBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?) = throw NotImplementedError()
        override fun sendOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, receiverPermission: String?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) = throw NotImplementedError()
        override fun sendStickyBroadcast(intent: android.content.Intent?) = throw NotImplementedError()
        override fun sendStickyOrderedBroadcast(intent: android.content.Intent?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) = throw NotImplementedError()
        override fun removeStickyBroadcast(intent: android.content.Intent?) = throw NotImplementedError()
        override fun sendStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) = throw NotImplementedError()
        override fun sendStickyOrderedBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?, resultReceiver: android.content.BroadcastReceiver?, scheduler: android.os.Handler?, initialCode: Int, initialData: String?, initialExtras: android.os.Bundle?) = throw NotImplementedError()
        override fun removeStickyBroadcastAsUser(intent: android.content.Intent?, user: android.os.UserHandle?) = throw NotImplementedError()
        override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?): android.content.Intent? = throw NotImplementedError()
        override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, flags: Int): android.content.Intent? = throw NotImplementedError()
        override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?): android.content.Intent? = throw NotImplementedError()
        override fun registerReceiver(receiver: android.content.BroadcastReceiver?, filter: android.content.IntentFilter?, broadcastPermission: String?, scheduler: android.os.Handler?, flags: Int): android.content.Intent? = throw NotImplementedError()
        override fun unregisterReceiver(receiver: android.content.BroadcastReceiver?) = throw NotImplementedError()
        override fun startService(service: android.content.Intent?): android.content.ComponentName? = throw NotImplementedError()
        override fun stopService(service: android.content.Intent?): Boolean = throw NotImplementedError()
        override fun bindService(service: android.content.Intent?, conn: android.content.ServiceConnection, flags: Int): Boolean = throw NotImplementedError()
        override fun unbindService(conn: android.content.ServiceConnection) = throw NotImplementedError()
        override fun startInstrumentation(className: android.content.ComponentName?, profileFile: String?, arguments: android.os.Bundle?) = throw NotImplementedError()
        override fun getSystemService(name: String): Any? = throw NotImplementedError()
        override fun getSystemServiceName(serviceClass: Class<*>): String? = throw NotImplementedError()
        override fun checkPermission(permission: String, pid: Int, uid: Int): Int = throw NotImplementedError()
        override fun checkCallingPermission(permission: String): Int = throw NotImplementedError()
        override fun checkCallingOrSelfPermission(permission: String): Int = throw NotImplementedError()
        override fun checkSelfPermission(permission: String): Int = throw NotImplementedError()
        override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) = throw NotImplementedError()
        override fun enforceCallingPermission(permission: String, message: String?) = throw NotImplementedError()
        override fun enforceCallingOrSelfPermission(permission: String, message: String?) = throw NotImplementedError()
        override fun grantUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) = throw NotImplementedError()
        override fun revokeUriPermission(uri: android.net.Uri?, modeFlags: Int) = throw NotImplementedError()
        override fun revokeUriPermission(toPackage: String?, uri: android.net.Uri?, modeFlags: Int) = throw NotImplementedError()
        override fun checkUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int): Int = throw NotImplementedError()
        override fun checkUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int): Int = throw NotImplementedError()
        override fun checkCallingUriPermission(uri: android.net.Uri?, modeFlags: Int): Int = throw NotImplementedError()
        override fun checkCallingUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, modeFlags: Int): Int = throw NotImplementedError()
        override fun enforceUriPermission(uri: android.net.Uri?, pid: Int, uid: Int, modeFlags: Int, message: String?) = throw NotImplementedError()
        override fun enforceUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, pid: Int, uid: Int, modeFlags: Int, message: String?) = throw NotImplementedError()
        override fun enforceCallingUriPermission(uri: android.net.Uri?, modeFlags: Int, message: String?) = throw NotImplementedError()
        override fun enforceCallingUriPermission(uri: android.net.Uri?, readPermission: String?, writePermission: String?, modeFlags: Int, message: String?) = throw NotImplementedError()
        override fun createPackageContext(packageName: String?, flags: Int): Context = throw NotImplementedError()
        override fun createConfigurationContext(overrideConfiguration: android.content.res.Configuration): Context = throw NotImplementedError()
        override fun createDisplayContext(display: android.view.Display): Context = throw NotImplementedError()
        override fun createDeviceProtectedStorageContext(): Context = throw NotImplementedError()
        override fun isDeviceProtectedStorage(): Boolean = false

    }

    class MinimalMockResources : Resources(null, null, null) {
        override fun getDisplayMetrics(): DisplayMetrics {
            val metrics = DisplayMetrics()
            metrics.widthPixels = 2560
            metrics.heightPixels = 1600
            return metrics
        }
    }

    @Before
    fun setup() {
        val mockContext = MinimalMockContext()
        processor = SwipePathProcessor(mockContext)
    }

    @Test
    fun `processPathToKeySequence rejects short invalid path`() {
        val invalidPath = listOf(
            Offset(Float.NaN, Float.NaN),  // Invalid NaN
            Offset(Float.POSITIVE_INFINITY, 100f)  // Invalid infinity
        )
        // This will now throw or return empty string?
        // validateAndFilterPath returns empty list, then code checks if size < 3.
        // It returns empty string.
        assertEquals("", processor.processPathToKeySequence(invalidPath))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `processPathToKeySequence rejects too long path`() {
        // Construct a path > 500
        val longPath = List(501) { Offset(it.toFloat(), it.toFloat()) }
        processor.processPathToKeySequence(longPath)
    }

    @Test
    fun `processPathToKeySequence handles concurrent registration safely`() {
        processor.registerKeyPosition("a", Rect(0f, 0f, 100f, 100f))
        processor.registerKeyPosition("b", Rect(100f, 0f, 200f, 100f))

        val path = listOf(
            Offset(50f, 50f), Offset(150f, 50f), Offset(150f, 150f)
        )
        val result = processor.processPathToKeySequence(path)
        assertTrue("Expected result to contain 'ab' but was '$result'", result.contains("ab"))
    }

    @Test
    fun `processPathToKeySequence filters low velocity correctly`() {
        processor.registerKeyPosition("a", Rect(0f, 0f, 100f, 100f))

        val slowPath = listOf(
            Offset(0f, 0f), Offset(1f, 1f), Offset(2f, 2f)
        )
        assertEquals("", processor.processPathToKeySequence(slowPath))
    }
}
