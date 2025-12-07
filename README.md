# 🎹 NextGenKeyboard

A modern, feature-rich Android keyboard application built with the latest Android technologies, offering intelligent typing, voice input, GIF support, and advanced privacy features.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-30%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=30)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org)

---

## ✨ Features

### 🚀 Current Features

- **🎤 Voice Typing**: Seamless voice-to-text conversion with real-time speech recognition
- **🖼️ GIF Keyboard**: Integrated Giphy support for searching and inserting GIFs
- **📋 Clipboard Manager**: Advanced clipboard history with encryption support
- **🔤 Advanced Autocorrect**: Intelligent autocorrect engine with dictionary-based suggestions
- **🌍 Multi-Language Support**: Support for multiple languages and keyboard layouts
- **🎨 Customizable Themes**: Multiple keyboard themes with dark mode support
- **🔐 Privacy & Security**: Local data processing with optional encryption for sensitive data
- **📊 Analytics**: Integrated Firebase Crashlytics for stability monitoring
- **🧠 AI-Powered Predictions**: Gemini AI integration for context-aware suggestions (Phase 2)

### 🔮 Coming Soon

- **👆 Swipe Typing**: Gesture-based typing for faster input
- **😀 Emoji Picker**: Full emoji keyboard with categories and search
- **🤖 Enhanced AI Predictions**: Advanced ML-based next-word predictions using TensorFlow Lite
- **📝 Learning System**: Personalized word suggestions based on typing habits
- **☁️ Cloud Sync**: Optional backup and sync across devices

---

## 📱 Requirements

- **Minimum SDK**: Android 11 (API 30)
- **Target SDK**: Android 14 (API 36)
- **Compile SDK**: 36
- **Kotlin**: 1.9+
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

- Android Studio Hedgehog (2023.1.1) or later
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
│   ├── swipe/         # Swipe typing (in progress)
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

### Test Structure

- **Unit Tests**: Located in `app/src/test/`
  - Repository tests
  - Autocorrect engine tests
  - Swipe predictor tests
  - Security tests

- **Instrumentation Tests**: Located in `app/src/androidTest/`
  - UI tests
  - Database tests
  - End-to-end tests

---

## 🚀 Building for Production

```bash
# Build release APK
./gradlew assembleRelease

# Build release AAB (for Play Store)
./gradlew bundleRelease
```

The output files will be in:
- APK: `app/build/outputs/apk/release/`
- AAB: `app/build/outputs/bundle/release/`

### ProGuard

The app uses ProGuard for code obfuscation and optimization. Rules are defined in:
- [`proguard-rules.pro`](app/proguard-rules.pro)

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork** the repository
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'feat: add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use **ktlint** for formatting
- Write meaningful commit messages
- Add unit tests for new features
- Update documentation as needed

---

## 📚 Documentation

- **[Privacy Policy](PRIVACY_POLICY.md)**: Detailed privacy and data handling policy
- **[Phase 1 Execution Summary](PHASE1-EXECUTION-SUMMARY.md)**: Initial development phase results
- **[Phase 2 AI Roadmap](PHASE_2_AI_ROADMAP.md)**: Upcoming AI features and improvements
- **[Feature Gap Analysis](FEATURE_GAP_ANALYSIS.md)**: Current feature status and roadmap
- **[Integration Guide](INTEGRATION-GUIDE.md)**: Guide for integrating new features
- **[Quick Reference](QUICK-REFERENCE.md)**: Quick reference for developers

---

## 🗺️ Roadmap

### Phase 1: Foundation ✅ (Completed)
- [x] Core keyboard functionality
- [x] Advanced autocorrect engine
- [x] Voice typing
- [x] GIF keyboard
- [x] Clipboard manager backend
- [x] Multi-language support
- [x] Theme system

### Phase 2: Intelligence 🚧 (In Progress)
- [ ] On-device AI predictions (TensorFlow Lite)
- [ ] Swipe typing with gesture recognition
- [ ] Learning system for personalized suggestions
- [ ] Enhanced clipboard UI
- [ ] Emoji picker with search
- [ ] Smart compose suggestions

### Phase 3: Advanced Features 📋 (Planned)
- [ ] Cloud backup and sync
- [ ] Custom user dictionaries
- [ ] Advanced theming with custom colors
- [ ] Floating keyboard mode
- [ ] One-handed mode
- [ ] Accessibility improvements
- [ ] Multi-device clipboard sync

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 aktarjabed

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
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

## 🐛 Bug Reports & Feature Requests

Found a bug or have a feature request? Please [open an issue](https://github.com/aktarjabed/NextGenKeyboard/issues) with:

- **Bug Reports**: Description, steps to reproduce, expected vs actual behavior, device info
- **Feature Requests**: Clear description, use case, and potential benefits

---

## 💬 Support & Contact

- **Issues**: [GitHub Issues](https://github.com/aktarjabed/NextGenKeyboard/issues)
- **Discussions**: [GitHub Discussions](https://github.com/aktarjabed/NextGenKeyboard/discussions)
- **Email**: support@nextgenkeyboard.com

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Giphy](https://giphy.com/) - GIF platform and API
- [Google AI](https://ai.google/) - Gemini AI API
- [Firebase](https://firebase.google.com/) - Analytics and crashlytics
- [Material Design](https://material.io/) - Design system and components

---

## ⭐ Star History

If you find this project useful, please consider giving it a star! ⭐

---

<div align="center">

**Built with ❤️ by [aktarjabed](https://github.com/aktarjabed)**

[Report Bug](https://github.com/aktarjabed/NextGenKeyboard/issues) · [Request Feature](https://github.com/aktarjabed/NextGenKeyboard/issues) · [Documentation](https://github.com/aktarjabed/NextGenKeyboard/wiki)

</div>
