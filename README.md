# 🎹 NextGenKeyboard

A modern, feature-rich Android keyboard application built with the latest Android technologies, offering intelligent typing, voice input, GIF support, and advanced privacy features.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-30%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=30)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)

---

## ✨ Features

### 🚀 Production Ready Features

- **🎤 Voice Typing**: Seamless voice-to-text conversion with real-time speech recognition
- **🖼️ GIF Keyboard**: Integrated Giphy support for searching and inserting GIFs
- **📋 Clipboard Manager**: Advanced clipboard history with pinned clips, search, and encryption support
- **🔤 Advanced Autocorrect**: Intelligent autocorrect engine with dictionary-based suggestions
- **🌍 Multi-Language Support**: Support for 35+ languages including Latin, Cyrillic, Arabic, Hebrew, Greek, and Indic scripts.
- **🎨 Dynamic Themes**: Switch instantly between Light, Dark, Neon, Glass, Material You, and Gaming themes.
- **😀 Emoji Keyboard**: Full emoji support with categorized browsing and "Recent" history.
- **👆 Swipe Typing**: Efficient swipe-to-type functionality with O(1) performance optimization.
- **🔐 Privacy & Security**: Local data processing with optional encryption for sensitive data
- **🧠 AI-Powered Predictions**: Gemini AI integration for context-aware suggestions.

### 🔮 Future Roadmap

- **🤖 Enhanced AI**: Advanced ML-based next-word predictions using on-device models.
- **☁️ Cloud Sync**: Optional secure backup and sync across devices.
- **🎨 Custom Theme Builder**: Create your own themes with custom colors and backgrounds.

---

## 📱 Requirements

- **Minimum SDK**: Android 11 (API 30)
- **Target SDK**: Android 14 (API 36)
- **Compile SDK**: 36
- **Kotlin**: 2.0+
- **Gradle**: 8.0+
- **JDK**: 17

---

## 🛠️ Installation

### For Users

1. Download the APK from the [Releases](https://github.com/aktarjabed/NextGenKeyboard/releases) page
2. Enable installation from unknown sources in Android settings
3. Install the APK
4. Go to **Settings → System → Languages & Input → On-screen keyboard**
5. Enable **NextGenKeyboard**
6. Select NextGenKeyboard as your default keyboard

### For Developers

#### Prerequisites

- Android Studio Koala or later
- Git installed on your system
- Android SDK installed

#### Clone and Build

```bash
# Clone the repository
git clone https://github.com/aktarjabed/NextGenKeyboard.git
cd NextGenKeyboard

# Open the project in Android Studio
# Or build from command line:
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

---

## 🔑 Configuration

The app requires API keys for full functionality. Create a `gradle.properties` file in the root directory:

```properties
# Giphy API Key (for GIF keyboard)
GIPHY_API_KEY=your_giphy_api_key_here

# Gemini API Key (for AI predictions)
GEMINI_API_KEY=your_gemini_api_key_here
```

### Obtaining API Keys

- **Giphy**: Register at [Giphy Developers](https://developers.giphy.com/)
- **Gemini**: Get your API key from [Google AI Studio](https://makersuite.google.com/app/apikey)

> **Note**: The keyboard will work without API keys, but GIF search and AI predictions will be disabled.

---

## 🏗️ Architecture

NextGenKeyboard follows **Clean Architecture** principles with **MVVM** pattern:

```
app/
├── data/
│   ├── local/          # Room database, DAOs, and encryption
│   ├── model/          # Data models and entities
│   └── repository/     # Repository implementations
├── di/                 # Dependency injection (Hilt modules)
├── feature/            # Feature-specific modules
│   ├── ai/            # AI prediction engine
│   ├── autocorrect/   # Autocorrect logic
│   ├── backup/        # Settings backup
│   ├── gif/           # Giphy integration
│   ├── keyboard/      # Keyboard utilities
│   ├── swipe/         # Swipe typing (O(1) Spatial Grid)
│   └── voice/         # Voice input manager
├── service/           # Keyboard IME service
├── ui/                # UI components and screens
│   ├── screens/       # Activities and composables
│   ├── theme/         # Theme definitions
│   └── view/          # Custom views and keyboard layouts
└── util/              # Utility functions and extensions
```

### Key Technologies

| Technology | Purpose |
|------------|---------|
| **Jetpack Compose** | Modern declarative UI framework |
| **Kotlin Coroutines** | Asynchronous programming |
| **Flow** | Reactive data streams |
| **Hilt** | Dependency injection |
| **Room** | Local database with type-safe queries |
| **DataStore** | Key-value storage for preferences |
| **WorkManager** | Background task scheduling |
| **Firebase** | Crashlytics and analytics |
| **Gemini AI** | Context-aware text predictions |
| **Giphy SDK** | GIF search and integration |
| **Security Crypto** | Data encryption |

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Generate test coverage report
./gradlew jacocoTestReport
```

---

## 🔒 Privacy & Security

- **Local Processing**: All typing data is processed locally on your device
- **No Data Collection**: We do not collect or transmit your keystrokes
- **Optional Analytics**: Firebase Crashlytics can be disabled in settings
- **Encryption**: Sensitive clipboard data is encrypted using Android Security Crypto
- **Permissions**: Only necessary permissions are requested (microphone for voice typing, internet for GIFs)

For full details, see our [Privacy Policy](PRIVACY_POLICY.md).

---

## 💬 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/aktarjabed/NextGenKeyboard/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aktarjabed/NextGenKeyboard/discussions)
- **Email**: support@nextgenkeyboard.com

---

<div align="center">

**Built with ❤️ by [aktarjabed](https://github.com/aktarjabed)**

</div>
