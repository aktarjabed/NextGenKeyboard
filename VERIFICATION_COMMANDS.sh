#!/bin/bash
echo "=== VERIFICATION ==="
# Check packages
echo "Checking SettingsViewModel package..."
grep "package com.aktarjabed.nextgenkeyboard.ui.viewmodel" app/src/main/java/com/aktarjabed/nextgenkeyboard/ui/viewmodel/SettingsViewModel.kt && echo "✅ OK" || echo "❌ FAIL"

echo "Checking SettingsScreen package..."
grep "package com.aktarjabed.nextgenkeyboard.ui.screens" app/src/main/java/com/aktarjabed/nextgenkeyboard/ui/screens/SettingsScreen.kt && echo "✅ OK" || echo "❌ FAIL"

echo "Checking PreferencesRepository package..."
grep "package com.aktarjabed.nextgenkeyboard.data.repository" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/PreferencesRepository.kt && echo "✅ OK" || echo "❌ FAIL"

# Check for duplicate definitions
echo "Checking for duplicates in SettingsViewModel..."
grep -c "class SettingsViewModel" app/src/main/java/com/aktarjabed/nextgenkeyboard/ui/viewmodel/SettingsViewModel.kt | xargs -I {} bash -c 'if [ {} -eq 1 ]; then echo "✅ OK (1 class definition)"; else echo "❌ FAIL ({} class definitions)"; fi'

# Check for ClipboardRepository methods
echo "Checking ClipboardRepository methods..."
grep "fun clearAllClips" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ clearAllClips found" || echo "❌ clearAllClips MISSING"
grep "fun clearUnpinnedClips" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ clearUnpinnedClips found" || echo "❌ clearUnpinnedClips MISSING"

# Check legacy files
ls -R app/src/main/java/com/nextgen 2>/dev/null && echo "❌ LEGACY FILES FOUND" || echo "✅ Legacy files clean"
# VERIFICATION_COMMANDS.sh
# Run this script to verify the repository state during conflict resolution.

echo "=========================================="
echo "PR #48 RESOLUTION VERIFICATION SCRIPT"
echo "=========================================="

echo ""
echo "--- STEP 1: GRADLE WRAPPER ---"
grep "gradle-8.13" gradle/wrapper/gradle-wrapper.properties && echo "✅ Gradle 8.13 detected" || echo "❌ Gradle 8.13 NOT detected"

echo ""
echo "--- STEP 2: ROOT BUILD.GRADLE.KTS ---"
grep 'id("com.android.application") version "8.13' build.gradle.kts && echo "✅ AGP 8.13 detected" || echo "❌ AGP 8.13 NOT detected"
grep 'id("org.jetbrains.kotlin.android") version "2.0' build.gradle.kts && echo "✅ Kotlin 2.0 detected" || echo "❌ Kotlin 2.0 NOT detected"
grep 'id("com.google.dagger.hilt.android") version "2.52' build.gradle.kts && echo "✅ Hilt 2.52 detected" || echo "❌ Hilt 2.52 NOT detected"

echo ""
echo "--- STEP 3: APP BUILD.GRADLE.KTS ---"
grep "compileSdk = 36" app/build.gradle.kts && echo "✅ compileSdk = 36" || echo "❌ compileSdk FAILED"
grep "targetSdk = 36" app/build.gradle.kts && echo "✅ targetSdk = 36" || echo "❌ targetSdk FAILED"
grep "minSdk = 30" app/build.gradle.kts && echo "✅ minSdk = 30" || echo "❌ minSdk FAILED"
grep "jvmTarget = \"17\"" app/build.gradle.kts && echo "✅ JVM 17" || echo "❌ JVM 17 FAILED"
grep "compose-bom:2025.10.01" app/build.gradle.kts && echo "✅ Compose BOM 2025.10" || echo "❌ Compose BOM FAILED"
grep "hilt-android:2.52" app/build.gradle.kts && echo "✅ Hilt 2.52" || echo "❌ Hilt 2.52 FAILED"

echo ""
echo "--- STEP 4: MANIFEST ---"
grep "READ_CLIPBOARD_CONTENT" app/src/main/AndroidManifest.xml && echo "✅ Clipboard permission" || echo "❌ Clipboard permission MISSING"
grep "RECEIVE_BOOT_COMPLETED" app/src/main/AndroidManifest.xml && echo "✅ Boot permission" || echo "❌ Boot permission MISSING"
grep "BootCompletedReceiver" app/src/main/AndroidManifest.xml && echo "✅ Boot receiver" || echo "❌ Boot receiver MISSING"
grep "LocaleChangeReceiver" app/src/main/AndroidManifest.xml && echo "✅ Locale receiver" || echo "❌ Locale receiver MISSING"
grep "NextGenKeyboardService" app/src/main/AndroidManifest.xml && echo "✅ IME service" || echo "❌ IME service MISSING"
grep "android.view.InputMethod" app/src/main/AndroidManifest.xml && echo "✅ InputMethod filter" || echo "❌ InputMethod filter MISSING"

echo ""
echo "--- STEP 5: METHOD.XML ---"
grep -E "<<<<<<|======|>>>>>>" app/src/main/res/xml/method.xml && echo "❌ CONFLICT MARKERS FOUND in method.xml" || echo "✅ No conflicts in method.xml"
grep -c "android:label=" app/src/main/res/xml/method.xml | xargs -I {} bash -c 'if [ {} -eq 9 ]; then echo "✅ 9 languages found"; else echo "❌ Expected 9 languages, found {}"; fi'
grep "हिन्दी" app/src/main/res/xml/method.xml && echo "✅ Hindi found" || echo "❌ Hindi MISSING"
grep "中文" app/src/main/res/xml/method.xml && echo "✅ Chinese found" || echo "❌ Chinese MISSING"

echo ""
echo "--- STEP 6: CLIPBOARD REPOSITORY ---"
# Check for interface and impl
grep "^interface ClipboardRepository" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt > /dev/null && echo "✅ Interface ClipboardRepository found" || echo "❌ Interface ClipboardRepository MISSING"
grep "^class ClipboardRepositoryImpl" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt > /dev/null && echo "✅ Class ClipboardRepositoryImpl found" || echo "❌ Class ClipboardRepositoryImpl MISSING"
# Check for sensitivity check
grep "isSensitiveData" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt > /dev/null && echo "✅ Sensitivity check found" || echo "❌ Sensitivity check MISSING"
# Check for duplicate methods
grep -c "fun copy(" app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt | xargs -I {} bash -c 'if [ {} -eq 1 ]; then echo "✅ No duplicate methods"; else echo "❌ Found {} copy() methods"; fi'

echo ""
echo "--- FINAL VERIFICATION PHASES ---"

echo "PHASE 1: CONFLICT MARKERS"
grep -r "<<<<<<\|======\|>>>>>>" app/src/main --include="*.kt" --include="*.xml" --include="*.gradle.kts" && echo "❌ MARKERS FOUND" || echo "✅ ALL CLEAN"

echo "PHASE 2: CRITICAL FILES"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/service/NextGenKeyboardService.kt && echo "✅ NextGenKeyboardService" || echo "❌ MISSING NextGenKeyboardService"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/receiver/BootCompletedReceiver.kt && echo "✅ BootCompletedReceiver" || echo "❌ MISSING BootCompletedReceiver"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/receiver/LocaleChangeReceiver.kt && echo "✅ LocaleChangeReceiver" || echo "❌ MISSING LocaleChangeReceiver"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/ui/viewmodel/KeyboardViewModel.kt && echo "✅ KeyboardViewModel" || echo "❌ MISSING KeyboardViewModel"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt && echo "✅ ClipboardRepository" || echo "❌ MISSING ClipboardRepository"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/PreferencesRepository.kt && echo "✅ PreferencesRepository" || echo "❌ MISSING PreferencesRepository"
test -f app/src/main/java/com/aktarjabed/nextgenkeyboard/di/AppModule.kt && echo "✅ AppModule" || echo "❌ MISSING AppModule"

echo "PHASE 3: PACKAGE DECLARATIONS"
grep -r "com\.nextgen\.keyboard" app/src/ --include="*.kt" --include="*.xml" 2>/dev/null && echo "❌ LEGACY PACKAGE REFERENCES FOUND" || echo "✅ NO LEGACY REFERENCES"
grep -c "^package com.aktarjabed.nextgenkeyboard" app/src/main/java/com/aktarjabed/nextgenkeyboard/**/*.kt 2>/dev/null | xargs -I {} echo "✅ Correct package declarations: {}"

echo "PHASE 6: CODE QUALITY"
grep -r "import timber.log.Timber" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Timber imports: {} (should be >= 5)"
grep -r "@AndroidEntryPoint" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Hilt @AndroidEntryPoint: {} (should be >= 1)"
grep -r "@HiltViewModel" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Hilt @HiltViewModel: {} (should be >= 1)"
grep -r "androidx.compose" app/src/main/java/com/aktarjabed --include="*.kt" | wc -l | xargs -I {} echo "Compose imports: {} (should be >= 10)"

echo ""
echo "=========================================="
echo "VERIFICATION COMPLETE"
echo "=========================================="
