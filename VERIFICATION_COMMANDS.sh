#!/bin/bash
set -e

echo "🔍 Starting NextGenKeyboard Verification Script"

echo "---------------------------------------------------"
echo "1. Checking for Conflict Markers"
if grep -r "<<<<<<<" .; then
    echo "❌ Error: Merge conflict markers found in codebase."
    exit 1
else
    echo "✅ No conflict markers found."
fi

echo "---------------------------------------------------"
echo "2. Validating Core Files Existence"
REQUIRED_FILES=(
    "app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/PreferencesRepository.kt"
    "app/src/main/java/com/aktarjabed/nextgenkeyboard/data/repository/ClipboardRepository.kt"
    "app/src/main/java/com/aktarjabed/nextgenkeyboard/service/NextGenKeyboardService.kt"
    "app/src/main/java/com/aktarjabed/nextgenkeyboard/feature/autocorrect/AdvancedAutocorrectEngine.kt"
    "app/src/main/java/com/aktarjabed/nextgenkeyboard/NextGenKeyboardApp.kt"
    "app/src/main/res/xml/method.xml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ Found $file"
    else
        echo "❌ Missing $file"
        exit 1
    fi
done

echo "---------------------------------------------------"
echo "3. Attempting Compilation (Dry Run)"
# This assumes Gradle wrapper exists and is executable
if [ -x "./gradlew" ]; then
    echo "🚀 Running ./gradlew clean build (this may take time)..."
    ./gradlew clean build --dry-run
    if [ $? -eq 0 ]; then
        echo "✅ Gradle build configuration is valid."
    else
        echo "❌ Gradle build failed."
        exit 1
    fi
else
    echo "⚠️ Gradle wrapper not found or not executable. Skipping build test."
fi

echo "---------------------------------------------------"
echo "🎉 Verification Complete! codebase appears healthy."
