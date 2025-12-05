## Git Merge Conflict Resolution Guide for PR #4

Based on your conflict files, here's a **command-line workflow** to resolve each conflict:

***

### **Quick Status Check**

```bash
cd /path/to/NextGenKeyboard
git status
# Shows conflicted files with both modified status
```

***

## **Conflict Resolution Strategy by File**

### **1️⃣ `.gitignore` (Likely Simple)**

**Typical conflict:** Different ignore patterns added on both branches.

**Resolution:**

```bash
# Option A: Keep both versions (recommended for .gitignore)
git checkout --theirs .gitignore
git add .gitignore

# Option B: Manually edit and keep important patterns from both
# Then: git add .gitignore
```

**Manual approach if needed:**
```bash
# View conflict markers
cat .gitignore
# Edit to combine necessary patterns
# Remove <<<<<<< HEAD / ======= / >>>>>>> markers
git add .gitignore
```

***

### **2️⃣ `PreferencesRepository.kt` (Likely Significant)**

This is one of your **corrupted files from Phase 1**. Approach depends on the conflict type:

**If you already fixed this file in Phase 1:**

```bash
# Use your cleaned version from Phase 1
git checkout --ours app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt
git add app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt
```

**If the conflict represents legitimate changes on both sides:**

```bash
# View the conflict
cat app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt

# Manually resolve by:
# 1. Keeping your Phase 1 fix (the --ours version)
# 2. Removing all <<<<<<< HEAD / ======= / >>>>>>> markers
# 3. Integrating any new changes from the PR branch if needed
git add app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt
```

***

### **3️⃣ `SettingsScreen.kt` (UI Component)**

**Typical conflict:** Different UI state handling or layout changes on both branches.

```bash
# View the conflict markers
cat app/src/main/java/com/nextgen/keyboard/ui/screens/SettingsScreen.kt

# Strategy:
# - If PR adds new UI features: Keep both versions (merge manually)
# - If PR is based on old corrupted version: Use --theirs (PR version)

# Option 1: Use PR version (if it's the newer fix)
git checkout --theirs app/src/main/java/com/nextgen/keyboard/ui/screens/SettingsScreen.kt
git add app/src/main/java/com/nextgen/keyboard/ui/screens/SettingsScreen.kt

# Option 2: Manual merge (recommended if both have valuable changes)
# Edit the file, remove conflict markers, combine logic
git add app/src/main/java/com/nextgen/keyboard/ui/screens/SettingsScreen.kt
```

***

### **4️⃣ `SettingsViewModel.kt` (State Management)**

Similar approach to SettingsScreen.kt:

```bash
# View conflicts
cat app/src/main/java/com/nextgen/keyboard/ui/viewmodel/SettingsViewModel.kt

# Likely scenario: PR added new state properties
# Resolution strategy:

# A) If PR is cleaner/complete: Use PR version
git checkout --theirs app/src/main/java/com/nextgen/keyboard/ui/viewmodel/SettingsViewModel.kt
git add app/src/main/java/com/nextgen/keyboard/ui/viewmodel/SettingsViewModel.kt

# B) If both have good changes: Manual merge
# - Keep all properties from both versions
# - Keep all methods from both versions
# - Remove conflict markers
git add app/src/main/java/com/nextgen/keyboard/ui/viewmodel/SettingsViewModel.kt
```

***

### **5️⃣ `method.xml` (IME Configuration - CRITICAL)**

This is your IME system registration file. **Do NOT guess here.**

```bash
# View the exact conflict
cat app/src/main/res/xml/method.xml

# Expected: Conflict between different IME subtype configurations
# Resolution:
# - Merge BOTH versions into one complete <input-method> block
# - Ensure no duplicate <subtype> definitions
# - Keep all language subtypes from both branches

# Example merged structure:
```

**Typical merged `method.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<input-method xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Subtypes from branch A -->
    <subtype android:name="@string/subtype_en_us"
             android:imeSubtypeLocale="en_US"
             android:imeSubtypeMode="keyboard"/>

    <!-- Subtypes from branch B (if different) -->
    <!-- Add only if not duplicate -->

</input-method>
```

```bash
# After manual edit:
git add app/src/main/res/xml/method.xml
```

***

### **6️⃣ `gradle-wrapper.properties` (Build Tool)**

Usually simple—keep the newer version:

```bash
# Check which version is newer/more appropriate
cat gradle/wrapper/gradle-wrapper.properties

# Typically: Keep the version from main/develop branch
# (usually --theirs if PR is older)

git checkout --theirs gradle/wrapper/gradle-wrapper.properties
git add gradle/wrapper/gradle-wrapper.properties
```

***

## **Complete Resolution Workflow**

### **Step 1: Fetch and View All Conflicts**

```bash
git status
# Shows all 5 conflicted files
```

### **Step 2: Resolve Each File (in order of criticality)**

```bash
# CRITICAL - IME Configuration
git checkout --theirs app/src/main/res/xml/method.xml
git add app/src/main/res/xml/method.xml

# CRITICAL - Core Repository (your Phase 1 fix)
git checkout --ours app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt
git add app/src/main/java/com/nextgen/keyboard/data/repository/PreferencesRepository.kt

# HIGH - ViewModels (manual review recommended)
cat app/src/main/java/com/nextgen/keyboard/ui/viewmodel/SettingsViewModel.kt
# ^ Edit if needed, then: git add

# HIGH - UI Screens (manual review recommended)
cat app/src/main/java/com/nextgen/keyboard/ui/screens/SettingsScreen.kt
# ^ Edit if needed, then: git add

# MEDIUM - Build Properties
git checkout --theirs gradle/wrapper/gradle-wrapper.properties
git add gradle/wrapper/gradle-wrapper.properties

# LOW - Git Ignore
git checkout --theirs .gitignore
git add .gitignore
```

### **Step 3: Verify All Conflicts Resolved**

```bash
git status
# Should show all files staged (green) or modified (red)
# No "both modified" entries
```

### **Step 4: Complete the Merge**

```bash
git commit -m "Resolve merge conflicts in PR #4

- method.xml: Merged IME subtype configurations
- PreferencesRepository.kt: Kept Phase 1 fix version
- SettingsViewModel.kt: Resolved state property conflicts
- SettingsScreen.kt: Resolved UI layout conflicts
- gradle-wrapper.properties: Updated to latest
- .gitignore: Merged patterns"

# Or simply:
git commit -m "Merge PR #4 - conflicts resolved"
```

### **Step 5: Push Resolution**

```bash
git push origin your-branch-name
# PR should automatically update as "ready to merge"
```
