# NxtGenKeyboard

NxtGenKeyboard is a privacy-oriented, multilingual Android keyboard (IME) with built-in autocorrect/suggestion, grammar checking, and Hindi/Bengali transliteration. It’s written in Kotlin using modern Android libraries (Room, Coroutines, etc.) and targets Android API 36 (Android 13+). NxtGenKeyboard is opt-in by design: features like clipboard history and online grammar checks are disabled by default with explicit user consent and warnings.

## Features

*   **Multilingual Support:** English, Hindi, and Bengali. Quickly switch languages with the globe/key label.
*   **Phonetic Transliteration:** Type Hindi/Bengali words using Latin (roman) letters. Common words are in a special dictionary to ensure correct output.
*   **Smart Suggestions & Autocorrect:** A built-in dictionary (with user-learned words) provides suggestions for mistyped words using edit-distance. Autocorrect on space is optional.
*   **Grammar Checking:** Offline English grammar rules catch repeated words, spacing, and capitalization issues. Optional online grammar checks (via LanguageTool API) highlight more issues.
*   **Clipboard History:** Stores up to 200 recent unpinned clips plus any pinned clips. Easily copy or pin entries. All data is local and excluded from backups for privacy.
*   **User Dictionary:** Learns new words as you type and adds them to suggestions in future.
*   **Customizable Input Feedback:** Toggle keypress sound and vibration. Haptic and audio feedback use standard Android effects.
*   **Privacy-Focused Defaults:** Sensitive fields (passwords, PINs, OTPs) disable learning, suggestions, grammar, transliteration, and clipboard capture. No user text is logged or sent externally (unless online grammar is explicitly enabled in Settings).
*   **Modern Dark Theme:** Optimized for eye comfort; matches system dark mode (uses AppCompat DayNight).
*   **Performance:** Room clipboard queries are optimized. NxtGenKeyboard utilizes coroutines and correctly debounces suggestion generation without introducing N+1 queries.

## Installation & Setup

1.  **Download or Build the APK:**
    *   Download a release APK from the project’s GitHub releases (if available).
    *   Or build from source (see Build Instructions below) and install via `adb install`.
2.  **Enable the Keyboard:**
    *   Go to Android Settings > System > Languages & Input > Virtual Keyboard > Manage Keyboards (path may vary by device).
    *   Turn on NxtGenKeyboard and confirm the warning that appears (the app requests Input Method permission to function as a keyboard).
3.  **Select NxtGenKeyboard:**
    *   In any text field, tap the keyboard icon or long-press the spacebar (depending on device) to switch input methods, then choose NxtGenKeyboard.
    *   Alternatively, you can press the globe key (⟲) on the keyboard to cycle languages/modes.
4.  **Configure Settings:**
    *   Open NxtGenKeyboard Settings from your app drawer or via the notification that appears when enabling the keyboard.
    *   Customize preferences: enable Online Grammar, Clipboard History, Autocorrect, Sound, Vibration, etc. Note that Online Grammar and Clipboard History are disabled by default and will show a warning dialog if you turn them on.

## Usage Tips

*   **Language Switching:** Tap the EN/हि/বা key to cycle between English, Hindi, and Bengali. The current language is shown in the Toast message.
*   **Transliteration:** When in Hindi or Bengali mode, type phonetically in Latin letters (e.g., “namaste” → “नमस्ते”). A short press on a non-letter (space or punctuation) commits the transliterated text.
*   **Suggestions:** When typing in English (or after transliteration completes a word), suggestion candidates appear above the keyboard. Tap a suggestion to replace the current word.
*   **Grammar Check:** Press the checkmark key (✓) to run grammar analysis on the entire text. Suggestions appear with options to apply fixes. If you modify text after checking, you will be prompted to re-run the check.
*   **Clipboard History:** Tap the clipboard key (📋) to view recent clippings. Tap an item to copy it back to the clipboard, long-press to pin/unpin or delete. In Clear Clipboard menus, you can remove unpinned or all entries.

## Build Instructions

For developers or advanced users who want to build from source:

*   **Prerequisites:** JDK 17, Android SDK (with API 36), and the Android command-line tools.

*   **Clone the repo:**
    ```bash
    git clone https://github.com/YourUsername/NxtGenKeyboard.git
    cd NxtGenKeyboard
    ```

*   **Build:**
    Run `./gradlew assembleDebug` to build a debug APK.
    (Optionally) run `./gradlew assembleRelease` for a release APK (requires signing setup).

*   **Install:**
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

*   **Run tests:**
    Unit tests (JUnit/Robolectric) can be run with `./gradlew test`.
    Instrumented tests (AndroidX) with `./gradlew connectedAndroidTest`.

## Technical Stack

*   **Language & Framework:** Kotlin, Android SDK (API 36)
*   **Input Method:** Uses InputMethodService and KeyboardView for the UI.
*   **Data Storage:** Room (SQLite) for clipboard and user dictionaries, with a migration and exported schema for testing.
*   **Concurrency:** Kotlin Coroutines for async operations (database, I/O).
*   **Other Libraries:** AndroidX Core/AppCompat, Lifecycle (ViewModel/LiveData), Room KTX, Kotlin Coroutines.
*   **Testing:** JUnit and Robolectric (unit tests), AndroidX Test and Espresso (instrumented tests).

## Privacy & Security

NxtGenKeyboard is designed with user privacy in mind:

*   **No Data Leakage:** No typed text is ever logged or sent out of the device, except if you opt in to Online Grammar, which will send only the checked text segment to LanguageTool (over HTTPS).
*   **Opt-In Features with Warnings:**
    *   **Clipboard History:** Completely local and disabled by default. Enabling it shows a warning that it may store sensitive data. History data is excluded from backups (`android:dataExtractionRules` excludes database and shared prefs).
    *   **Online Grammar:** Disabled by default with a warning that text is sent to the grammar server.
*   **Sensitive Fields:** Automatic disabling of suggestions, transliteration, clipboard capture, and grammar in password/PIN/OTP fields (TextView input types flagged as no suggestions or password).
*   **Encryption & Permissions:** The keyboard only requests permissions needed for its features (e.g., Internet for optional grammar checks, VIBRATE for haptic feedback). There is no external storage or extra permissions used.

## Contributing

Contributions are welcome! Please see CONTRIBUTING.md for guidelines on bug reports and pull requests. If you add translations, UI improvements, or expand the word lists, please be sure to follow the existing code style and ensure thorough testing (suggestion engine, grammar fixes, etc.).

## License

(Insert license information here, e.g., MIT License, Apache 2.0, or as appropriate)

---
*Screenshots, CI badges, or additional media can be added here in the future.*
