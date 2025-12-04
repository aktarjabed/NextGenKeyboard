# QUICK REFERENCE

## 🔑 Key Classes

| Class | Responsibility | Key Method |
|-------|----------------|------------|
| `NextGenKeyboardService` | Main IME Service | `onCreateInputView()` |
| `PreferencesRepository` | DataStore Settings | `isDarkMode`, `setKeyboardLanguage()` |
| `ClipboardRepository` | Clipboard History | `saveClip()`, `getClipboardContent()` |
| `AdvancedAutocorrectEngine` | Predictive Text | `getAdvancedSuggestions()` |

## 🛠️ Architecture

- **Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture.
- **DI:** Hilt (`@AndroidEntryPoint` in Service, `@HiltViewModel` in ViewModel).
- **UI:** Jetpack Compose (hosted in Service via `ComposeView` with `ViewModelStoreOwner`).
- **Data:** Room (SQLite) for Clipboard, DataStore for Preferences.

## 🚀 Commands

| Action | Command |
|--------|---------|
| Build | `./gradlew build` |
| Clean | `./gradlew clean` |
| Install | `./gradlew installDebug` |
| Test | `./gradlew test` |

## ⚠️ Critical Notes

1. **Service Injection:** `NextGenKeyboardService` uses **Field Injection** for dependencies (`@Inject lateinit var ...`) and manual ViewModel creation.
2. **Context:** Use `@ApplicationContext` for Repositories.
3. **Database:** `ClipboardDatabase` uses destructive migration in DEBUG builds only.

## 🔒 Security

- **Sensitive Data:** Blocked by `ClipboardRepository.isSensitiveContent()` (OTP, Credit Cards, Passwords).
- **Private Mode:** `NextGenKeyboardService` detects password fields and disables learning/saving.
