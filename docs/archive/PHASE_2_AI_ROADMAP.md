# 🧠 Phase 2 Roadmap: The AI Evolution

**Theme:** "From Functionality to Intelligence"
**Goal:** Transform NextGenKeyboard from a static input method into a context-aware, learning assistant using local AI/ML.

---

## 📅 Timeline: 4 Weeks

### 🧱 Milestone 1: The Memory (Persistence)
**Focus:** Storing user habits to personalize the AI.
* **Architecture:** Room Database + KSP + Hilt.
* **Tasks:**
    1.  **Define Entities:**
        *   `LearnedWord`: Custom words added by the user.
        *   `UsageHistory`: Frequency map of word pairs (Bigrams) for basic statistical prediction.
    2.  **Create DAOs:** Efficient queries for "most likely next word".
    3.  **Repository Integration:** Update `AdvancedAutocorrectEngine` to read from Room instead of just the static `en_dict.txt`.

### 🤖 Milestone 2: The Brain (On-Device AI)
**Focus:** Implementing real "NextGen" prediction.
* **Technology:** TensorFlow Lite (TFLite) or MediaPipe LLM Inference.
* **Tasks:**
    1.  **Integrate TFLite:** Add dependencies for on-device inference.
    2.  **Model Selection:**
        *   *Starter:* Smart Reply model (lightweight, suggests short phrases).
        *   *Advanced:* MobileBERT or GPT-2 Small (quantized) for true text generation.
    3.  **Prediction Pipeline:**
        *   Input: Current typed sentence.
        *   Processing: AI Model inference (background coroutine).
        *   Output: List of 3 candidate words.

### 🎨 Milestone 3: The Interface (Smart UI)
**Focus:** Visualizing intelligence without lag.
* **Components:** Jetpack Compose `SuggestionStrip`.
* **Tasks:**
    1.  **Connect ViewModel:** Expose `StateFlow<List<Suggestion>>` from `AdvancedAutocorrectEngine`.
    2.  **Suggestion UI:**
        *   Center: Best prediction (AI confidence > 70%).
        *   Left/Right: Alternatives or corrections.
    3.  **Actions:** Tap to accept, long-press to remove/blacklist.

### 🧪 Milestone 4: Quality Assurance
**Focus:** Reliability and Performance.
* **Tasks:**
    1.  **Unit Tests:** Verify `AdvancedAutocorrectEngine` logic (Mock the AI model).
    2.  **Performance Profiling:** Ensure AI inference takes < 16ms (60fps) or runs asynchronously to avoid blocking typing.
    3.  **Memory Leak Checks:** Verify `Closeable` resources in TFLite.

---

## 🛠️ Recommended Tech Stack for Phase 2

| Component | Choice | Reason |
|-----------|--------|--------|
| **Database** | **Room** | Robust, SQL-based, integrates with Kotlin Flow. |
| **ML Engine** | **TensorFlow Lite** | Standard for Android, massive model support. |
| **Concurrency** | **Coroutines/Flow** | Non-blocking database and AI access. |
| **Structure** | **Clean Architecture** | Keep AI logic separate from UI. |

---

## 🚀 Getting Started

1.  **Checkout Branch:** `git checkout -b feature/phase2-ai-persistence`
2.  **Add Room Dependencies:** Update `build.gradle.kts` with KSP support.
3.  **Design Database Schema:** Create `app/src/main/java/com/aktarjabed/nextgenkeyboard/data/local/entity`.

*Let's build the smartest keyboard on Android.*
