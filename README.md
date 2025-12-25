# 🎹 NextGenKeyboard

A modern, production-ready Android keyboard application built with **Jetpack Compose** and **Google Gemini AI**. NextGenKeyboard combines the speed of native Android development with the intelligence of Large Language Models to deliver a superior typing experience.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-30%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=30)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![AI](https://img.shields.io/badge/AI-Gemini-purple.svg)](https://deepmind.google/technologies/gemini/)

---

## 🚧 Project Status

**Current Version:** v1.0.0

> ⚠️ **Note:** This project is open source. AI features require a Gemini API key.

### ✅ Completed Features
- [x] **Core Architecture**: Jetpack Compose UI, Hilt DI, Clean Architecture
- [x] **Swipe Typing**: O(1) Spatial Grid algorithm (Stabilized)
- [x] **Smart Corrections**: Hybrid Autocorrect + Gemini AI Prediction Mediator
- [x] **Symbol Layer**: Full symbol support with toggle
- [x] **Clipboard Manager**: Secure, local history with sensitive data filtering
- [x] **GIF Keyboard**: Giphy integration
- [x] **Accessibility**: TalkBack support optimized

### 🚀 Roadmap (v1.1+)
- [ ] **Sync**: Cloud dictionary sync (Firebase)
- [ ] **Themes**: Community theme store

---

## 🚀 What's New in v1.0.0 (AI & Robustness)

We have reached a major milestone in development with the completion of Phase 3. Key highlights include:

*   **🧠 Gemini AI Integration**: Context-aware next-word predictions powered by Google's **Gemini 1.5 Flash** model. The keyboard understands the nuance of your sentence to suggest the most relevant words.
*   **⚡ True Multi-Touch Support**: A completely rewritten gesture detector that supports rapid two-thumb typing, key rollover, and shift-key combinations without dropped inputs.
*   **🛡️ Robust Swipe Typing**: Optimized **O(1) Spatial Key Grid** algorithm that instantly maps swipe paths to keys, ensuring smooth performance even on low-end devices.
*   **🔧 Advanced Autocorrect**: A hybrid engine combining instant common-typo lookup with Levenshtein distance calculations for smart, non-intrusive corrections.

---

## ✨ Features

### Core Experience
- **👆 Swipe Typing**: Efficient glide typing with dynamic path tracing and smart error correction.
- **🎤 Voice Typing**: Seamless, real-time voice-to-text conversion.
- **🌍 Multi-Language Support**: Support for **35+ languages** including Latin, Cyrillic, Arabic (RTL), Hebrew, Greek, and Indic scripts.
- **😀 Emoji Keyboard**: Full emoji support with categorized browsing and "Recent" history.
- **🖼️ GIF Keyboard**: Integrated Giphy support for searching and inserting GIFs directly from the keyboard.

### Smart & Secure
- **📋 Clipboard Manager**: Advanced history with pinned clips, search, and **security filtering** (automatically hides passwords and OTPs).
- **🔐 Privacy First**: All typing data is processed locally. AI predictions communicate securely with Google's API only when enabled, and no personal data is stored on external servers.
- **🎨 Dynamic Themes**: Switch instantly between Light, Dark, Neon, Glass, Material You, and Gaming themes.

---

## 📱 Requirements

- **Minimum SDK**: Android 11 (API 30)
- **Target SDK**: Android 14 (API 36)
- **Compile SDK**: 36
- **Architecture**: ARM64 / x86_64
- **Network**: Internet connection required for AI Predictions and GIF Search.

---

## 🛠️ Installation

### For Users

1. Download the latest APK from the [**Releases Page**](https://github.com/aktarjabed/NextGenKeyboard/releases/latest).
2. Enable installation from unknown sources in Android settings.
3. Install the APK.
4. Go to **Settings → System → Languages & Input → On-screen keyboard**.
5. Enable **NextGenKeyboard**.
6. Select NextGenKeyboard as your default keyboard.

### For Developers

#### Prerequisites
- Android Studio Koala or later
- JDK 17
- Android SDK API 36

#### Clone and Build

```bash
# Clone the repository
git clone https://github.com/aktarjabed/NextGenKeyboard.git
cd NextGenKeyboard

# Create local.properties with your SDK path if not exists
# echo "sdk.dir=/path/to/android/sdk" > local.properties

# Build and Install
./gradlew installDebug
```

---

## 🔑 Configuration

To unlock the full potential of NextGenKeyboard (AI Predictions & GIFs), you need to configure API keys.

Create a `gradle.properties` file in the root directory:

```properties
# Giphy API Key (Required for GIF keyboard)
# Register at: https://developers.giphy.com/
GIPHY_API_KEY=your_giphy_api_key_here

# Gemini API Key (Required for Smart Predictions)
# Get key at: https://makersuite.google.com/app/apikey
GEMINI_API_KEY=your_gemini_api_key_here
```

> **Note**: The app will function as a standard keyboard without these keys, but smart features will be disabled.

---

## 🏗️ Architecture

NextGenKeyboard uses a modern, scalable architecture designed for stability and performance.

```
app/
├── feature/            # Modularized features
│   ├── ai/            # Gemini AI client & prediction logic
│   ├── swipe/         # O(1) Spatial Grid & Path Processor
│   ├── autocorrect/   # Hybrid Levenshtein + Lookup engine
│   └── keyboard/      # Core keyboard logic
├── service/           # IMS Implementation
│   └── NextGenKeyboardService.kt # Bridged lifecycle owner
├── ui/                # Jetpack Compose UI
│   └── MainKeyboardView.kt # Entry point for Compose
└── data/              # Data Layer (Room, DataStore)
```

### Technical Highlights
*   **Hybrid Lifecycle Management**: A custom implementation bridges the Android Service lifecycle with Jetpack Compose's `ViewModel` and `LifecycleOwner` requirements, ensuring modern state management within an IME service.
*   **Dependency Injection**: Extensive use of **Hilt** for injecting dependencies like `SwipePathProcessor` and `AiContextManager`.
*   **Coroutines & Flow**: Heavy use of Kotlin Coroutines for off-main-thread dictionary loading and AI network calls.

---

## 🧪 Testing

We maintain a high standard of code quality with comprehensive unit and instrumentation tests.

```bash
# Run unit tests (including Autocorrect & AI logic)
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

---

## 🔒 Privacy & Security

*   **Local Processing**: Keystrokes are processed locally for autocorrect and standard predictions.
*   **Sensitive Data Protection**: The clipboard manager uses regex-based filtering to detect and mask sensitive information (passwords, credit cards) automatically.
*   **Crash Reporting**: Optional Firebase Crashlytics integration (disabled by default, user opt-in required).

For full details, see our [Privacy Policy](PRIVACY_POLICY.md).

---

## 💬 Support & Contact

*   **Issues**: [GitHub Issues](https://github.com/aktarjabed/NextGenKeyboard/issues)
*   **Discussions**: [GitHub Discussions](https://github.com/aktarjabed/NextGenKeyboard/discussions)

---

<div align="center">

**Built with ❤️ by [aktarjabed](https://github.com/aktarjabed)**

</div>
