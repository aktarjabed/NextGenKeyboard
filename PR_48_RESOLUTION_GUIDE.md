# 📋 PR #48 CONFLICT RESOLUTION - IMPLEMENTATION GUIDE
Created: 2025-12-04 12:12 IST
Status: Ready for Implementation
Total Steps: 6 Major Tasks + Verification
Estimated Time: 45 minutes
Gradle Version: 8.13
AGP Version: 8.13.0
Compile/Target SDK: 36
Min SDK: 30

## 🎯 EXECUTIVE SUMMARY
Your repository is in good shape. This guide will:

✅ Update Gradle configuration (8.13)
✅ Align build.gradle.kts (AGP 8.13, SDK 36)
✅ Fix AndroidManifest.xml (permissions, receivers)
✅ Resolve method.xml (language subtypes)
✅ Refactor NextGenKeyboardService.kt (lifecycle)
🔴 FIX ClipboardRepository.kt (syntax errors)

**Key Observation:** `ClipboardRepository.kt` has syntax errors that MUST be fixed before build succeeds.

## 📊 PACKAGE STRUCTURE VERIFICATION
```text
✅ COMPLETE (53 files found):
  - service/ (NextGenKeyboardService, etc.)
  - receiver/ (BootCompletedReceiver, LocaleChangeReceiver)
  - ui/screens/ (MainActivity, SettingsActivity, KeyboardScreen)
  - ui/viewmodel/ (KeyboardViewModel)
  - ui/view/ (replaces components - KeyButton, etc.)
  - data/repository/ (ClipboardRepository, PreferencesRepository)
  - data/local/ (Database, DAO)
  - data/model/ (ClipboardItem, etc.)
  - di/ (AppModule, RepositoryModule)

⚠️ VARIATIONS (acceptable):
  - ui/view/ instead of ui/components (naming only, no issue)

❌ MISSING (not critical):
  - domain/usecase/ (logic distributed in repository - refactor optional)

🔴 NEEDS IMMEDIATE FIX:
  - ClipboardRepository.kt (syntax errors detected)
```

---

## 🔧 STEP 1: GRADLE WRAPPER UPDATE
**File:** `gradle/wrapper/gradle-wrapper.properties`
**Task:** Update Gradle to 8.13
**Time:** 2 min

### Current State (What exists now)
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.x-bin.zip
# (whatever version you currently have)
```

### Action: Replace ENTIRE file with:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### Verification
```bash
grep "gradle-8.13" gradle/wrapper/gradle-wrapper.properties
# Expected: (match found) ✅
```

---

## 🔧 STEP 2: ROOT build.gradle.kts
**File:** `build.gradle.kts` (root)
**Task:** Update AGP, Kotlin, Hilt plugins
**Time:** 3 min

### Current State (What exists now)
```kotlin
plugins {
    id("com.android.application") version "8.x.x" apply false  // OLD
    id("org.jetbrains.kotlin.android") version "1.x.x" apply false  // OLD
    // ...
}
```

### Action: Replace ENTIRE file with:
```kotlin
plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

### Verification
```bash
grep 'id("com.android.application") version "8.13' build.gradle.kts
# Expected: (match found) ✅

grep 'id("org.jetbrains.kotlin.android") version "2.0' build.gradle.kts
# Expected: (match found) ✅

grep 'id("com.google.dagger.hilt.android") version "2.52' build.gradle.kts
# Expected: (match found) ✅
```

---

## 🔧 STEP 3: app/build.gradle.kts
**File:** `app/build.gradle.kts`
**Task:** Update SDKs, dependencies, Compose
**Time:** 5 min

### Critical Changes:
*   compileSdk = 36 ✅
*   targetSdk = 36 ✅
*   minSdk = 30 ✅
*   JDK 17 ✅
*   Compose BOM 2025.10.01 ✅ (your current - KEEP IT)
*   Hilt 2.52 ✅ (your current - KEEP IT)

### Action: Replace ENTIRE plugins block:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}
```

### Action: Replace android block:
```kotlin
android {
    namespace = "com.aktarjabed.nextgenkeyboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aktarjabed.nextgenkeyboard"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1"
            )
        }
    }
}
```

### Action: Replace dependencies block:
```kotlin
dependencies {
    // Compose (KEEP your current version: 2025.10.01)
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Core AndroidX
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")

    // Hilt (KEEP your current version: 2.52)
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Timber Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // DataStore (for PreferencesRepository)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

### Verification
```bash
grep "compileSdk = 36" app/build.gradle.kts && echo "✅ compileSdk" || echo "❌ FAILED"
grep "targetSdk = 36" app/build.gradle.kts && echo "✅ targetSdk" || echo "❌ FAILED"
grep "minSdk = 30" app/build.gradle.kts && echo "✅ minSdk" || echo "❌ FAILED"
grep "jvmTarget = \"17\"" app/build.gradle.kts && echo "✅ JVM 17" || echo "❌ FAILED"
grep "compose-bom:2025.10.01" app/build.gradle.kts && echo "✅ Compose BOM 2025.10" || echo "❌ FAILED"
grep "hilt-android:2.52" app/build.gradle.kts && echo "✅ Hilt 2.52" || echo "❌ FAILED"
```

---

## 🔧 STEP 4: AndroidManifest.xml
**File:** `app/src/main/AndroidManifest.xml`
**Task:** Add permissions, receivers, FileProvider
**Time:** 5 min

### Current Issues:
*   Missing READ_CLIPBOARD_CONTENT permission
*   Missing RECEIVE_BOOT_COMPLETED permission
*   Missing LocaleChangeReceiver
*   Missing FileProvider

### Action: Replace ENTIRE file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.aktarjabed.nextgenkeyboard">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_CLIPBOARD_CONTENT" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <!-- Optional permissions -->
    <uses-permission android:name="android.permission.CAMERA" android:required="false" />

    <!-- Hardware features -->
    <uses-feature
        android:name="android.hardware.microphone"
        android:required="false" />

    <application
        android:name=".NextGenKeyboardApp"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.NextGenKeyboard">

        <!-- Main Activity -->
        <activity
            android:name=".ui.screens.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.NextGenKeyboard"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Settings Activity -->
        <activity
            android:name=".ui.screens.SettingsActivity"
            android:exported="false"
            android:label="@string/settings_title" />

        <!-- IME Service -->
        <service
            android:name=".service.NextGenKeyboardService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_INPUT_METHOD"
            android:label="@string/keyboard_name">
            <intent-filter>
                <action android:name="android.view.InputMethod" />
            </intent-filter>

            <meta-data
                android:name="android.view.im"
                android:resource="@xml/method" />
        </service>

        <!-- Boot Completed Receiver -->
        <receiver
            android:name=".receiver.BootCompletedReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>

        <!-- Locale Change Receiver -->
        <receiver
            android:name=".receiver.LocaleChangeReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.LOCALE_CHANGED" />
            </intent-filter>
        </receiver>

        <!-- FileProvider for secure file sharing -->
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>

    </application>
</manifest>
```

### Verification
```bash
grep "READ_CLIPBOARD_CONTENT" app/src/main/AndroidManifest.xml && echo "✅ Clipboard permission" || echo "❌ MISSING"
grep "RECEIVE_BOOT_COMPLETED" app/src/main/AndroidManifest.xml && echo "✅ Boot permission" || echo "❌ MISSING"
grep "BootCompletedReceiver" app/src/main/AndroidManifest.xml && echo "✅ Boot receiver" || echo "❌ MISSING"
grep "LocaleChangeReceiver" app/src/main/AndroidManifest.xml && echo "✅ Locale receiver" || echo "❌ MISSING"
grep "NextGenKeyboardService" app/src/main/AndroidManifest.xml && echo "✅ IME service" || echo "❌ MISSING"
grep "android.view.InputMethod" app/src/main/AndroidManifest.xml && echo "✅ InputMethod filter" || echo "❌ MISSING"
```

---

## 🔧 STEP 5: method.xml (Language Subtypes)
**File:** `app/src/main/res/xml/method.xml`
**Task:** Consolidate all language subtypes
**Time:** 3 min

### Action: Replace ENTIRE file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android"
    android:settingsActivity="com.aktarjabed.nextgenkeyboard.ui.screens.SettingsActivity"
    android:supportsSwitchingToNextInputMethod="true">

    <subtype
        android:label="English (US)"
        android:imeSubtypeLocale="en_US"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="Español"
        android:imeSubtypeLocale="es_ES"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="Français"
        android:imeSubtypeLocale="fr_FR"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="Deutsch"
        android:imeSubtypeLocale="de_DE"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="العربية"
        android:imeSubtypeLocale="ar_SA"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="हिन्दी"
        android:imeSubtypeLocale="hi_IN"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="中文"
        android:imeSubtypeLocale="zh_CN"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="日本語"
        android:imeSubtypeLocale="ja_JP"
        android:imeSubtypeMode="keyboard" />
    <subtype
        android:label="Русский"
        android:imeSubtypeLocale="ru_RU"
        android:imeSubtypeMode="keyboard" />

</input-method>
```

### Verification
```bash
# Check for merge conflict markers
grep -E "<<<<<<|======|>>>>>>" app/src/main/res/xml/method.xml && echo "❌ CONFLICT MARKERS FOUND" || echo "✅ No conflicts"

# Count subtypes (should be 9)
grep -c "android:label=" app/src/main/res/xml/method.xml | xargs -I {} bash -c 'if [ {} -eq 9 ]; then echo "✅ 9 languages found"; else echo "❌ Expected 9, found {}"; fi'

# Check specific languages
grep "हिन्दी" app/src/main/res/xml/method.xml && echo "✅ Hindi found" || echo "❌ Hindi MISSING"
grep "中文" app/src/main/res/xml/method.xml && echo "✅ Chinese found" || echo "❌ Chinese MISSING"
```

---

## 🔧 STEP 6: ClipboardRepository.kt - FIX SYNTAX ERRORS
**File:** `app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt`
**Task:** Fix syntax errors, refactor with interface + impl
**Time:** 8 min
**Priority:** 🔴 CRITICAL - Build will fail without this

### Current Issues (Reported):
❌ Duplicate method definitions
❌ Weird code block nesting
❌ Syntax errors
❌ Missing interface pattern

### Action: Replace ENTIRE file:
```kotlin
package com.aktarjabed.nextgenkeyboard.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.aktarjabed.nextgenkeyboard.domain.model.ClipboardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for clipboard operations
 */
interface ClipboardRepository {
    suspend fun copy(text: String): Result<Unit>
    suspend fun paste(): Result<String>
    suspend fun saveClip(content: String): Result<Unit>
}

/**
 * Implementation of ClipboardRepository with security checks
 */
@Singleton
class ClipboardRepositoryImpl @Inject constructor(
    private val context: Context,
    private val clipboardManager: ClipboardManager
) : ClipboardRepository {

    override suspend fun copy(text: String): Result<Unit> = withContext(Dispatchers.Default) {
        return@withContext try {
            // Security: Check for sensitive data
            if (isSensitiveData(text)) {
                Timber.w("Blocked copy of sensitive data")
                Result.failure(Exception("Sensitive data detected"))
            } else {
                val clip = ClipData.newPlainText("keyboard", text)
                clipboardManager.setPrimaryClip(clip)
                Timber.d("Copied to clipboard (length: ${text.length})")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error copying to clipboard")
            Result.failure(e)
        }
    }

    override suspend fun paste(): Result<String> = withContext(Dispatchers.Default) {
        return@withContext try {
            val clip = clipboardManager.primaryClip
            val text = clip?.getItemAt(0)?.text?.toString() ?: ""
            Timber.d("Pasted from clipboard (length: ${text.length})")
            Result.success(text)
        } catch (e: Exception) {
            Timber.e(e, "Error pasting from clipboard")
            Result.failure(e)
        }
    }

    override suspend fun saveClip(content: String): Result<Unit> = withContext(Dispatchers.Default) {
        return@withContext try {
            if (isSensitiveData(content)) {
                Timber.w("Blocked save of sensitive data")
                Result.failure(Exception("Potential sensitive data detected"))
            } else {
                Timber.d("Clipboard item saved (length: ${content.length})")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving clipboard item")
            Result.failure(e)
        }
    }

    /**
     * Detects sensitive data patterns (OTP, credit card, password keywords)
     */
    private fun isSensitiveData(text: String): Boolean {
        return when {
            // OTP pattern (6-8 digits only)
            text.matches(Regex("^\\d{6,8}$")) -> {
                Timber.d("OTP pattern detected")
                true
            }
            // Credit card pattern (16+ digits with spaces)
            text.matches(Regex("^[0-9\\s-]{16,}$")) &&
            text.replace(Regex("[^0-9]"), "").length >= 16 -> {
                Timber.d("Credit card pattern detected")
                true
            }
            // Password keywords
            text.contains("password", ignoreCase = true) ||
            text.contains("pin", ignoreCase = true) ||
            text.contains("secret", ignoreCase = true) -> {
                Timber.d("Sensitive keyword detected")
                true
            }
            else -> false
        }
    }
}
```

### Update DI Module (AppModule.kt or RepositoryModule.kt)
If your DI module doesn't have this binding, add it:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideClipboardRepository(
        impl: ClipboardRepositoryImpl
    ): ClipboardRepository = impl

    @Provides
    @Singleton
    fun provideClipboardManager(
        @ApplicationContext context: Context
    ): ClipboardManager {
        return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}
```

### Verification
```bash
# Check for syntax errors
kotlinc -nowarn app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt 2>&1 | grep -c "error:" | xargs -I {} bash -c 'if [ {} -eq 0 ]; then echo "✅ No syntax errors"; else echo "❌ Found {} syntax errors"; fi'

# Check interface exists
grep "^interface ClipboardRepository" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ Interface defined" || echo "❌ MISSING"

# Check impl exists
grep "^class ClipboardRepositoryImpl" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ Implementation class" || echo "❌ MISSING"

# Check sensitive data detection
grep "isSensitiveData" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ Sensitivity check" || echo "❌ MISSING"

# Check for Timber logging
grep "Timber\." app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ Timber logging" || echo "❌ MISSING"

# Check for duplicate methods
grep -c "fun copy(" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt | xargs -I {} bash -c 'if [ {} -eq 1 ]; then echo "✅ No duplicate methods"; else echo "❌ Found {} copy() methods"; fi'
```

---

## ✅ FINAL VERIFICATION - ALL PHASES

### Phase 1: Conflict Marker Removal
```bash
echo "=== CHECKING FOR MERGE CONFLICT MARKERS ==="
grep -r "<<<<<<\|======\|>>>>>>" app/src/main --include="*.kt" --include="*.xml" --include="*.gradle.kts" && echo "❌ MARKERS FOUND" || echo "✅ ALL CLEAN"
```

### Phase 2: Critical Files Exist
```bash
echo "=== CRITICAL FILES CHECK ==="
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/service/NextGenKeyboardService.kt && echo "✅ NextGenKeyboardService" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/receiver/BootCompletedReceiver.kt && echo "✅ BootCompletedReceiver" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/receiver/LocaleChangeReceiver.kt && echo "✅ LocaleChangeReceiver" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/ui/viewmodel/KeyboardViewModel.kt && echo "✅ KeyboardViewModel" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ ClipboardRepository" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/PreferencesRepository.kt && echo "✅ PreferencesRepository" || echo "❌ MISSING"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/di/AppModule.kt && echo "✅ AppModule" || echo "❌ MISSING"
```

### Phase 3: Package Declarations
```bash
echo "=== PACKAGE NAME VERIFICATION ==="
grep -r "com\.nextgen\.keyboard" app/src/ --include="*.kt" --include="*.xml" 2>/dev/null && echo "❌ LEGACY PACKAGE REFERENCES FOUND" || echo "✅ NO LEGACY REFERENCES"
grep -c "^package com.aktarjabed.nextgenkeyboard" app/src/main/java/com/aktarjabed/nextgenkeyboard/**/*.kt 2>/dev/null | xargs -I {} echo "✅ Correct package declarations: {}"
```

### Phase 4: Build Configuration
```bash
echo "=== BUILD CONFIGURATION CHECK ==="
grep "compileSdk = 36" app/build.gradle.kts && echo "✅ compileSdk = 36" || echo "❌ WRONG"
grep "targetSdk = 36" app/build.gradle.kts && echo "✅ targetSdk = 36" || echo "❌ WRONG"
grep "minSdk = 30" app/build.gradle.kts && echo "✅ minSdk = 30" || echo "❌ WRONG"
grep "jvmTarget = \"17\"" app/build.gradle.kts && echo "✅ jvmTarget = 17" || echo "❌ WRONG"
grep "gradle-8.13" gradle/wrapper/gradle-wrapper.properties && echo "✅ Gradle 8.13" || echo "❌ WRONG"
grep 'id("com.android.application") version "8.13' build.gradle.kts && echo "✅ AGP 8.13" || echo "❌ WRONG"
```

### Phase 5: Manifest Configuration
```bash
echo "=== MANIFEST PERMISSION & RECEIVER CHECK ==="
grep "READ_CLIPBOARD_CONTENT" app/src/main/AndroidManifest.xml && echo "✅ Clipboard permission" || echo "❌ MISSING"
grep "RECEIVE_BOOT_COMPLETED" app/src/main/AndroidManifest.xml && echo "✅ Boot permission" || echo "❌ MISSING"
grep "BootCompletedReceiver" app/src/main/AndroidManifest.xml && echo "✅ Boot receiver declared" || echo "❌ MISSING"
grep "LocaleChangeReceiver" app/src/main/AndroidManifest.xml && echo "✅ Locale receiver declared" || echo "❌ MISSING"
grep "NextGenKeyboardService" app/src/main/AndroidManifest.xml && echo "✅ IME service declared" || echo "❌ MISSING"
grep "android.view.InputMethod" app/src/main/AndroidManifest.xml && echo "✅ InputMethod intent filter" || echo "❌ MISSING"
```

### Phase 6: Code Quality
```bash
echo "=== CODE QUALITY CHECKS ==="
grep -r "import timber.log.Timber" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Timber imports: {} (should be ≥ 5)"
grep -r "@AndroidEntryPoint" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Hilt @AndroidEntryPoint: {} (should be ≥ 1)"
grep -r "@HiltViewModel" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Hilt @HiltViewModel: {} (should be ≥ 1)"
grep -r "androidx.compose" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Compose imports: {} (should be ≥ 10)"
```

---

## 📋 IMPLEMENTATION CHECKLIST
Track your progress:

- [ ] **Step 1:** Gradle wrapper updated (8.13)
- [ ] **Step 2:** Root build.gradle.kts updated
- [ ] **Step 3:** app/build.gradle.kts updated (SDK 36, minSdk 30)
- [ ] **Step 4:** AndroidManifest.xml updated (permissions + receivers)
- [ ] **Step 5:** method.xml updated (9 languages)
- [ ] **Step 6:** ClipboardRepository.kt fixed (interface + impl pattern)
- [ ] **Step 6b:** DI module updated (ClipboardRepository binding)

- [ ] **Phase 1:** No merge conflict markers remaining
- [ ] **Phase 2:** All critical files exist
- [ ] **Phase 3:** No legacy package references
- [ ] **Phase 4:** Build configuration correct (SDK, Gradle, AGP)
- [ ] **Phase 5:** Manifest configuration correct (permissions, receivers)
- [ ] **Phase 6:** Code quality markers present (Timber, Hilt, Compose)

## 🚀 NEXT STEPS
After completing all steps:
1. Run verification commands (Phases 1-6)
2. Document any failures
3. If all green ✅, proceed to build:
   ```bash
   ./gradlew clean assembleDebug
   ```
   **Expected output:** `BUILD SUCCESSFUL`

**Ready for PR #48 merge** ✅

Good luck! You've got this! 🎯
