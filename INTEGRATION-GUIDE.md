# PHASE 1 INTEGRATION GUIDE

## 🎯 Quick Start

### 1. Requirements
- Android Studio Koala Feature Drop or later
- JDK 17
- Gradle 8.13

### 2. Integration Steps

#### Step 1: Copy Critical Files
Copy the provided files to their respective locations in your `app/src/main/java/com/aktarjabed/nextgenkeyboard/` directory:

| Source File | Destination Package |
|-------------|---------------------|
| `PreferencesRepository.kt` | `data.repository` |
| `ClipboardRepository.kt` | `data.repository` |
| `AdvancedAutocorrectEngine.kt` | `feature.autocorrect` |
| `NextGenKeyboardService.kt` | `service` |
| `DatabaseModule.kt` | `di` |

#### Step 2: Verify `AndroidManifest.xml`
Ensure your `AndroidManifest.xml` points to the correct Application class and Service:

```xml
<application
    android:name=".NextGenKeyboardApp"
    ... >

    <service
        android:name=".service.NextGenKeyboardService"
        android:permission="android.permission.BIND_INPUT_METHOD"
        ... >
        ...
    </service>
</application>
```

#### Step 3: Build
Run the following command to verify the build:
```bash
./gradlew clean build
```

---

## 🔧 Troubleshooting

### Compilation Errors

**Error:** `Duplicate class found`
**Fix:** Ensure you have completely replaced the old files with the new ones provided. The old files had multiple class definitions in the same file.

**Error:** `HiltViewModel` injection issues
**Fix:** `NextGenKeyboardService` uses **manual ViewModel instantiation** with injected dependencies. Do not try to inject `KeyboardViewModel` directly into the Service.

**Error:** `DataStore` multiple instances
**Fix:** `PreferencesRepository.kt` now declares the DataStore as a top-level extension property to ensure a singleton instance.

### Runtime Issues

**Crash:** `java.lang.IllegalStateException: You cannot start a load for a destroyed activity`
**Fix:** This might happen if Glide/Coil is used after the service is destroyed. The new Service implementation properly cleans up the Compose view in `onDestroy`.

**Issue:** Keyboard doesn't appear
**Fix:** Go to **System Settings > System > Languages & input > On-screen keyboard > Manage on-screen keyboards** and enable "NextGen Keyboard".

---

## 📦 Dependency Graph

The following Hilt modules are now configured:

- `AppModule`: Provides Application Context.
- `DatabaseModule`: Provides `ClipboardDatabase` and `ClipboardDao`.

`PreferencesRepository` and `ClipboardRepository` are provided as `@Singleton` classes and are available for injection.

---

## 📝 Next Steps (Phase 2)

- Enable Predictive Text UI (currently stubbed).
- Implement real Clipboard history UI in the Keyboard View.
- Add Unit Tests for the new Repositories.
