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
