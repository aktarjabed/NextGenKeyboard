package com.aktarjabed.nxtgenkeyboard.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.aktarjabed.nxtgenkeyboard.R
import com.aktarjabed.nxtgenkeyboard.clipboard.AppDatabase
import com.aktarjabed.nxtgenkeyboard.clipboard.ClipboardRepository
import com.aktarjabed.nxtgenkeyboard.grammar.GrammarIssue
import com.aktarjabed.nxtgenkeyboard.grammar.OfflineGrammarService
import com.aktarjabed.nxtgenkeyboard.grammar.OnlineGrammarService
import com.aktarjabed.nxtgenkeyboard.lang.PhoneticTransliterator
import com.aktarjabed.nxtgenkeyboard.suggestion.SuggestionEngine
import com.aktarjabed.nxtgenkeyboard.ui.ClipboardHistoryActivity
import com.aktarjabed.nxtgenkeyboard.user.UserWordRepository
import com.aktarjabed.nxtgenkeyboard.util.AppPrefs
import com.aktarjabed.nxtgenkeyboard.util.ClipboardInsertBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale

private val EMOJI_SET = listOf(
    "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
    "🙂", "😉", "😍", "🥰", "😘", "😗", "😋", "😜", "🤪", "🤨",
    "🧐", "🤓", "😎", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
    "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤",
    "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰",
    "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "👏", "🙌", "🙏",
    "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "💔", "💯", "🔥",
    "⭐", "🌟", "✨", "⚡", "🎉", "🎊", "🎁", "🍕", "☕", "🚀"
)

data class GrammarSnapshot(
    val text: String,
    val cursorStart: Int,
    val cursorEnd: Int,
    val language: String,
    val windowStart: Int,
    val windowEnd: Int
)

class NxtGenKeyboard : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var candidateBar: LinearLayout
    private lateinit var emojiPanel: View
    private lateinit var emojiStrip: LinearLayout
    private lateinit var prefs: AppPrefs

    private lateinit var suggestionEngine: SuggestionEngine
    private lateinit var offlineGrammar: OfflineGrammarService
    private lateinit var onlineGrammar: OnlineGrammarService
    private lateinit var clipboardRepo: ClipboardRepository
    private lateinit var userWordRepo: UserWordRepository

    private val languages = listOf("en", "hi", "bn")
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(serviceJob + Dispatchers.Main.immediate)
    private val clipboardMutex = Mutex()

    private var langIndex = 0
    private var shiftState = 0
    private var symbolActive = false
    private var emojiVisible = false
    private var currentEditorInfo: EditorInfo? = null
    private var grammarSnapshot: GrammarSnapshot? = null
    private var grammarJob: Job? = null
    private var suggestionJob: Job? = null
    private var lastShiftTap = 0L
    private var sensitiveEditor = false
    private var noPersonalizedLearning = false
    private var numericEditor = false
    private var phoneEditor = false
    private var editorNoSuggestions = false
    private var editorAutoComplete = false
    private var editorAutoCorrect = false
    private val romanBuffer = StringBuilder()
    private var clipboardRegistered = false
    private var suggestionRequestId = 0

    // Typo-safe learning: a word is only learned after being typed LEARN_THRESHOLD times.
    private val typedWordCounts = java.util.HashMap<String, Int>()

    private val clipboardManager by lazy {
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    private var sessionToken: String? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        if (!sensitiveEditor && prefs.clipboardHistoryEnabled) saveClipboard()
    }

    companion object {
        const val CODE_DELETE = -5
        const val CODE_SHIFT = -104
        const val CODE_LANG = -100
        const val CODE_CLIP = -101
        const val CODE_GRAMMAR = -102
        const val CODE_ENTER = -103
        const val CODE_SYMBOL = -105
        const val CODE_EMOJI = -106
        const val DOUBLE_TAP_MS = 400L
        const val LEARN_THRESHOLD = 2
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPrefs(this)
        langIndex = prefs.languageIndex.coerceIn(0, languages.lastIndex)

        suggestionEngine = SuggestionEngine(this)
        offlineGrammar = OfflineGrammarService()
        onlineGrammar = OnlineGrammarService()

        val db = AppDatabase.getInstance(this)
        clipboardRepo = ClipboardRepository(db.clipboardDao())
        userWordRepo = UserWordRepository(db.userWordDao())

        scope.launch(Dispatchers.IO) {
            suggestionEngine.loadAll()
            languages.forEach { language ->
                suggestionEngine.addUserWords(language, userWordRepo.words(language))
            }
        }
    }

    override fun onCreateInputView(): View {
        val view = layoutInflater.inflate(R.layout.input_view, null)
        keyboardView = view.findViewById(R.id.keyboardView)
        candidateBar = view.findViewById(R.id.candidateBar)
        emojiPanel = view.findViewById(R.id.emojiPanel)
        emojiStrip = view.findViewById(R.id.emojiStrip)

        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.isPreviewEnabled = true
        keyboardView.keyboard = Keyboard(this, R.xml.keyboard_qwerty)

        view.findViewById<TextView>(R.id.btnEmojiAbc).setOnClickListener {
            showEmojiPanel(false)
        }
        view.findViewById<TextView>(R.id.btnEmojiDelete).setOnClickListener {
            handleDelete()
        }

        emojiStrip.removeAllViews()
        val pad = dp(8)
        EMOJI_SET.forEach { emoji ->
            emojiStrip.addView(TextView(this).apply {
                text = emoji
                contentDescription = emoji
                textSize = 26f
                minimumHeight = dp(48)
            minimumWidth = dp(48)
                minimumWidth = dp(48)
                setPadding(pad, pad / 2, pad, pad / 2)
                setOnClickListener { safeCommitText(emoji) }
            })
        }
        return view
    }

    // Never go to fullscreen extract mode (landscape) — it hides our keyboard view.
    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentEditorInfo = attribute
        sensitiveEditor = attribute?.let(::isPasswordInput) == true
        noPersonalizedLearning = attribute?.let { (it.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0 } == true
        numericEditor = attribute?.let(::isNumericInput) == true
        phoneEditor = attribute?.let(::isPhoneInput) == true
        editorNoSuggestions = if (attribute != null) hasNoSuggestions(attribute) else false
        editorAutoComplete = if (attribute != null) hasAutoComplete(attribute) else false
        editorAutoCorrect = if (attribute != null) editorAllowsAutoCorrect(attribute) else false
        typedWordCounts.clear()

        if (!restarting || sessionToken == null) {
            sessionToken = java.util.UUID.randomUUID().toString()
        }
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

        if (oldSelStart != newSelStart || oldSelEnd != newSelEnd) {
            cancelSuggestions()
            grammarJob?.cancel()
            grammarJob = null
            grammarSnapshot = null
            clearCandidates()
        }
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        if (!::keyboardView.isInitialized) return

        // Synchronize language with system-selected subtype, fallback to prefs
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentSubtype = imm.currentInputMethodSubtype
        val systemLang = currentSubtype?.locale?.takeIf { it.isNotEmpty() }
            ?.substringBefore('_')?.lowercase(Locale.ROOT)
            ?: currentSubtype?.languageTag?.substringBefore('-')?.lowercase(Locale.ROOT)
        val systemIndex = systemLang?.let { languages.indexOf(it) } ?: -1
        if (systemIndex >= 0) {
            langIndex = systemIndex
            prefs.languageIndex = systemIndex
        } else {
            langIndex = prefs.languageIndex.coerceIn(0, languages.lastIndex)
        }

        shiftState = 0
        symbolActive = false
        showEmojiPanel(false)
        romanBuffer.clear()
        grammarJob?.cancel()
        grammarSnapshot = null

        val layoutRes = when {
            phoneEditor -> R.xml.keyboard_phone
            numericEditor -> R.xml.keyboard_numeric
            else -> R.xml.keyboard_qwerty
        }
        keyboardView.keyboard = Keyboard(this, layoutRes)
        keyboardView.setOnKeyboardActionListener(this)
        keyboardView.setShifted(false)
        updateKeyLabels()
        clearCandidates()
        registerClipboardListenerIfAllowed()
        val token = sessionToken ?: return
        ClipboardInsertBus.registerListener(token) { deliverPendingInsert() }
        deliverPendingInsert()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        deliverPendingInsert()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        flushTransliteration()
        unregisterClipboardListener()
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        flushTransliteration()

        sessionToken?.let {
            ClipboardInsertBus.unregisterListener(it)
            ClipboardInsertBus.clear(it)
        }
        sessionToken = null
        typedWordCounts.clear()
        cancelSuggestions()
        grammarJob?.cancel()
        grammarJob = null
        grammarSnapshot = null

        super.onFinishInput()
    }

    override fun onUnbindInput() {
        flushTransliteration()

        sessionToken?.let {
            ClipboardInsertBus.unregisterListener(it)
            ClipboardInsertBus.clear(it)
        }
        sessionToken = null
        typedWordCounts.clear()
        cancelSuggestions()
        grammarJob?.cancel()
        grammarJob = null
        grammarSnapshot = null

        super.onUnbindInput()
    }

    override fun onDestroy() {
        grammarJob?.cancel()
        unregisterClipboardListener()
        scope.cancel()
        super.onDestroy()
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        if (!::keyboardView.isInitialized) return
        val tag = newSubtype?.locale?.takeIf { it.isNotEmpty() }
            ?: newSubtype?.languageTag
            ?: return
        val language = tag.substringBefore('_').substringBefore('-').lowercase(Locale.ROOT)
        val index = languages.indexOfFirst { it == language }
        if (index >= 0 && index != langIndex) {
            langIndex = index
            prefs.languageIndex = index
            shiftState = 0
            keyboardView.setShifted(false)
            updateKeyLabels()
            clearCandidates()
            Toast.makeText(
                this,
                getString(R.string.language_changed, currentLang().uppercase(Locale.ROOT)),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun currentLang(): String = languages[langIndex]

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            CODE_DELETE -> handleDelete()
            CODE_SHIFT -> handleShift()
            CODE_LANG -> switchLanguage()
            CODE_CLIP -> openClipboard()
            CODE_GRAMMAR -> runGrammarCheck()
            CODE_ENTER -> handleEnter()
            CODE_SYMBOL -> toggleSymbols()
            CODE_EMOJI -> showEmojiPanel(!emojiVisible)
            else -> handleChar(primaryCode)
        }
    }

    private fun handleShift() {
        if (symbolActive || numericEditor || phoneEditor) return
        val now = System.currentTimeMillis()
        val doubleTap = now - lastShiftTap in 1..DOUBLE_TAP_MS
        lastShiftTap = now

        shiftState = when {
            doubleTap -> 2
            shiftState == 0 -> 1
            shiftState == 1 -> 0
            else -> 0
        }
        keyboardView.setShifted(shiftState != 0)
        updateKeyLabels()
        keyboardView.invalidateAllKeys()
    }

    private fun toggleSymbols() {
        if (numericEditor || phoneEditor) return
        flushTransliteration()
        symbolActive = !symbolActive
        shiftState = 0
        clearCandidates()
        val layoutRes = if (symbolActive) R.xml.keyboard_symbols else R.xml.keyboard_qwerty
        keyboardView.keyboard = Keyboard(this, layoutRes)
        keyboardView.setShifted(false)
        updateKeyLabels()
        keyboardView.invalidateAllKeys()
    }

    private fun showEmojiPanel(show: Boolean) {
        if (show) flushTransliteration()
        emojiVisible = show
        if (!::emojiPanel.isInitialized) return
        emojiPanel.visibility = if (show) View.VISIBLE else View.GONE
        keyboardView.visibility = if (show) View.GONE else View.VISIBLE

        val scroller = candidateBar.parent as? View
        scroller?.visibility = if (show) View.GONE else View.VISIBLE
        if (show) clearCandidates()
    }

    private fun updateKeyLabels() {
        val keyboard = keyboardView.keyboard ?: return
        keyboard.keys.forEach { key ->
            val code = key.codes.firstOrNull() ?: return@forEach
            if (code in 97..122) {
                key.label = if (shiftState == 0) code.toChar().toString()
                else code.toChar().uppercase(Locale.ROOT)
            }
        }
    }

    private fun handleChar(code: Int) {
        if (code == 32) {
            flushTransliteration()
            if (!sensitiveEditor && !editorNoSuggestions && prefs.autoCorrect && currentLang() == "en") {
                tryAutoCorrect()
            }
            if (!sensitiveEditor && !editorNoSuggestions) maybeLearnCurrentWord()
            safeCommitText(" ")
            afterWordMaybe()
            return
        }

        val rawChar = try {
            String(Character.toChars(code))
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("NxtGenIME", "Invalid key code: $code", e)
            return
        }
        val isLetter = rawChar.any(Char::isLetter)

        if (numericEditor || phoneEditor || sensitiveEditor || currentLang() == "en") {
            val autoCap = isLetter && shiftState == 0 && shouldAutoCap()
            val output = when {
                !isLetter -> rawChar
                shiftState != 0 || autoCap -> rawChar.uppercase(Locale.ROOT)
                else -> rawChar
            }
            safeCommitText(output)
            if (isLetter && shiftState == 1) {
                shiftState = 0
                keyboardView.setShifted(false)
                updateKeyLabels()
                keyboardView.invalidateAllKeys()
            }
            if (!sensitiveEditor) afterWordMaybe()
            return
        }

        // Hindi/Bengali transliteration
        if (rawChar.any(Char::isLetterOrDigit)) {
            romanBuffer.append(rawChar)
            safeSetComposingText(romanBuffer.toString(), 1)
        } else {
            flushTransliteration()
            safeCommitText(rawChar)
            if (!sensitiveEditor) afterWordMaybe()
        }
    }

    /** True when the next letter should be capitalized (sentence start / after . ! ? / newline). */
    private fun shouldAutoCap(): Boolean {
        if (!prefs.autoCapitalize || sensitiveEditor || editorNoSuggestions) return false
        if (symbolActive || currentLang() != "en") return false
        val before = safeGetTextBeforeCursor(2) ?: return false
        if (before.endsWith("\n")) return true
        val trimmed = before.trimEnd()
        if (trimmed.isEmpty()) return true
        return when (trimmed.last()) {
            '.', '!', '?' -> true
            else -> false
        }
    }

    private fun flushTransliteration() {
        if (sensitiveEditor) {
            romanBuffer.clear()
            safeFinishComposingText()
            return
        }
        if (currentLang() == "en" || romanBuffer.isEmpty()) return

        val text = PhoneticTransliterator.transliterate(romanBuffer.toString(), currentLang())
        safeSetComposingText("", 1)
        if (safeCommitText(text)) {
            romanBuffer.clear()
        }
    }

    private fun handleDelete() {
        val selected = safeGetSelectedText()
        if (!selected.isNullOrEmpty()) {
            safeCommitText("")
            return
        }

        if (romanBuffer.isNotEmpty() && !sensitiveEditor && currentLang() != "en") {
            deleteLastCodePoint()
            if (romanBuffer.isEmpty()) {
                safeSetComposingText("", 1)
                safeFinishComposingText()
            } else {
                safeSetComposingText(romanBuffer.toString(), 1)
            }
            return
        }
        val before = safeGetTextBeforeCursor(2)
        val deleteCount = if (before != null && before.length == 2 && Character.isSurrogatePair(before[0], before[1])) 2 else 1
        if (safeDeleteSurroundingText(deleteCount, 0)) {
            if (!sensitiveEditor) afterWordMaybe()
        }
    }

    private fun deleteLastCodePoint() {
        if (romanBuffer.isEmpty()) return
        val last = romanBuffer[romanBuffer.length - 1]
        if (last.isLowSurrogate() && romanBuffer.length >= 2 && romanBuffer[romanBuffer.length - 2].isHighSurrogate()) {
            romanBuffer.delete(romanBuffer.length - 2, romanBuffer.length)
        } else {
            romanBuffer.deleteCharAt(romanBuffer.length - 1)
        }
    }

    private fun switchLanguage() {
        if (sensitiveEditor || numericEditor || phoneEditor) return
        flushTransliteration()
        symbolActive = false
        showEmojiPanel(false)
        langIndex = (langIndex + 1) % languages.size
        prefs.languageIndex = langIndex
        shiftState = 0
        keyboardView.keyboard = Keyboard(this, R.xml.keyboard_qwerty)
        keyboardView.setShifted(false)
        updateKeyLabels()
        keyboardView.invalidateAllKeys()
        clearCandidates()
        Toast.makeText(
            this,
            getString(R.string.language_changed, currentLang().uppercase(Locale.ROOT)),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openClipboard() {
        if (sensitiveEditor || !prefs.clipboardHistoryEnabled) {
            Toast.makeText(this, R.string.clipboard_disabled_for_field, Toast.LENGTH_SHORT).show()
            return
        }
        flushTransliteration()
        val token = sessionToken ?: return
        startActivity(
            Intent(this, ClipboardHistoryActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ClipboardHistoryActivity.EXTRA_FROM_IME, true)
                .putExtra(ClipboardHistoryActivity.EXTRA_SESSION_TOKEN, token)
        )
    }

    private fun deliverPendingInsert() {
        if (currentInputConnection == null) return
        if (sensitiveEditor) return
        val token = sessionToken ?: return
        ClipboardInsertBus.consume(token) { pendingText ->
            safeCommitText(pendingText)
        }
        clearCandidates()
    }

    private fun handleEnter() {
        flushTransliteration()
        val info = currentEditorInfo
        val action = (info?.imeOptions ?: EditorInfo.IME_ACTION_NONE) and EditorInfo.IME_MASK_ACTION
        val noEnter = (info?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_ENTER_ACTION
        val usable = info != null &&
                action != EditorInfo.IME_ACTION_NONE &&
                action != EditorInfo.IME_ACTION_UNSPECIFIED &&
                noEnter == 0
        if (usable) {
            try {
                currentInputConnection?.performEditorAction(action)
            } catch (e: Exception) {
                safeCommitText("\n")
            }
        } else {
            safeCommitText("\n")
        }
    }

    private fun afterWordMaybe() {
        if (sensitiveEditor || numericEditor || phoneEditor || editorNoSuggestions) {
            clearCandidates()
            return
        }
        if (currentLang() != "en" && romanBuffer.isNotEmpty()) {
            clearCandidates()
            return
        }
        val lang = currentLang() // capture now — never re-read inside the coroutine
        val text = safeGetTextBeforeCursor(100) ?: return
        val match = Regex("""(\p{L}+)([^\p{L}]*)$""").find(text) ?: run {
            clearCandidates()
            return
        }
        val word = match.groupValues[1]
        if (word.length < 2) {
            clearCandidates()
            return
        }
        val lower = word.lowercase(Locale.ROOT)
        if (!suggestionEngine.isLoaded(lang)) {
            clearCandidates()
            return
        }
        if (suggestionEngine.contains(lang, lower)) {
            clearCandidates()
            return
        }

        suggestionJob?.cancel()
        val requestId = ++suggestionRequestId
        suggestionJob = scope.launch {
            kotlinx.coroutines.delay(120L)
            if (requestId != suggestionRequestId || !isActive) return@launch
            val suggestions = withContext(Dispatchers.Default) {
                suggestionEngine.suggest(lang, lower, max = 5, maxDistance = 2)
            }
            if (requestId == suggestionRequestId && isActive) {
                if (suggestions.isEmpty()) clearCandidates() else showCandidates(suggestions, word)
            }
        }
    }

    private fun showCandidates(items: List<String>, oldWord: String) {
        clearCandidates()
        val padH = dp(12)
        val padV = dp(4)
        items.forEach { suggestion ->
            val tv = TextView(this).apply {
                text = preserveCapitalization(oldWord, suggestion)
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(padH, padV, padH, padV)
                textSize = 16f
                setOnClickListener {
                    replaceLastWord(oldWord, text.toString())
                }
            }
            candidateBar.addView(tv)
        }
    }

    /** Replaces the word (and any trailing punctuation/space) with the suggestion, re-committing the trailing part. */
    private fun replaceLastWord(oldWord: String, newWord: String) {
        val before = safeGetTextBeforeCursor(oldWord.length + 10) ?: return
        val match = Regex("""(\p{L}+)([^\p{L}]*)$""").find(before)
        if (match != null) {
            val matchedWord = match.groupValues[1]
            val trailing = match.groupValues[2]
            safeDeleteSurroundingText(matchedWord.length + trailing.length, 0)
            safeCommitText(newWord + trailing)
        }
        clearCandidates()
    }

    private fun cancelSuggestions() {
        suggestionJob?.cancel()
        suggestionJob = null
        ++suggestionRequestId
    }

    private fun clearCandidates() {
        ++suggestionRequestId
        if (::candidateBar.isInitialized) candidateBar.removeAllViews()
    }

    private fun runGrammarCheck() {
        if (sensitiveEditor || numericEditor || phoneEditor) return
        flushTransliteration()

        val maxChars = 2000
        val before = safeGetTextBeforeCursor(maxChars) ?: ""
        val selected = safeGetSelectedText() ?: ""

        val remaining = maxChars - before.length - selected.length
        val after = if (remaining > 0) {
            safeGetTextAfterCursor(remaining) ?: ""
        } else {
            ""
        }

        val text = before + selected + after
        if (text.isBlank()) return

        val cursorStart = before.length
        val cursorEnd = cursorStart + selected.length

        grammarJob?.cancel()

        val lang = currentLang()
        grammarSnapshot = GrammarSnapshot(text, cursorStart, cursorEnd, lang, 0, text.length)

        grammarJob = scope.launch {
            val issues = withContext(kotlinx.coroutines.Dispatchers.IO) {
                if (prefs.useOnlineGrammar && isNetworkAvailable() && lang == "en") {
                    try {
                        onlineGrammar.check(text, lang)
                    } catch (e: Exception) {
                        offlineGrammar.check(text)
                    }
                } else {
                    offlineGrammar.check(text)
                }
            }
            if (grammarJob?.isActive == true) showGrammarIssues(issues)
        }
    }

    private fun showGrammarIssues(issues: List<GrammarIssue>) {
        clearCandidates()
        if (issues.isEmpty()) {
            addCandidate(getString(R.string.no_grammar_issues), false) {}
            return
        }
        val snapshot = grammarSnapshot ?: return
        val cursor = snapshot.cursorStart
        issues.take(8).forEach { issue ->
            val start = issue.start
            val end = start + issue.length
            val fixable = end <= cursor
            val label = if (issue.replacements.isNotEmpty()) {
                "${issue.message} → ${issue.replacements.first()}"
            } else {
                issue.message
            }
            if (fixable) {
                addCandidate(label, true) { applyGrammarFix(issue) }
            } else {
                addCandidate(label, false) {}
            }
        }
    }

    private fun applyGrammarFix(issue: GrammarIssue) {
        val snapshot = grammarSnapshot ?: return

        val currentBefore = safeGetTextBeforeCursor(2000) ?: ""
        val currentSelected = safeGetSelectedText() ?: ""
        val remaining = 2000 - currentBefore.length - currentSelected.length
        val currentAfter = if (remaining > 0) safeGetTextAfterCursor(remaining) ?: "" else ""

        val currentText = currentBefore + currentSelected + currentAfter
        val currentCursorStart = currentBefore.length
        val currentCursorEnd = currentCursorStart + currentSelected.length

        if (currentText != snapshot.text ||
            currentCursorStart != snapshot.cursorStart ||
            currentCursorEnd != snapshot.cursorEnd ||
            currentLang() != snapshot.language) {

            android.widget.Toast.makeText(this, R.string.text_changed_recheck, android.widget.Toast.LENGTH_SHORT).show()
            clearCandidates()
            return
        }

        val start = issue.start
        val end = start + issue.length
        val cursor = currentCursorStart
        val replacement = issue.replacements.firstOrNull() ?: return

        if (start < 0 || end < start || end > cursor || end > snapshot.text.length) return

        val middle = currentBefore.substring(end, cursor)
        val connection = currentInputConnection ?: return
        connection.beginBatchEdit()
        try {
            safeDeleteSurroundingText(cursor - start, 0)
            safeCommitText(replacement)
            if (middle.isNotEmpty()) safeCommitText(middle)
        } finally {
            connection.endBatchEdit()
        }
        grammarSnapshot = null
        clearCandidates()
    }

    private fun addCandidate(text: String, clickable: Boolean, onClick: () -> Unit) {
        val padH = dp(12)
        val padV = dp(4)
        val tv = TextView(this).apply {
            this.text = text
            contentDescription = text
            minimumHeight = dp(48)
            minimumWidth = dp(48)
            setTextColor(if (clickable) 0xFFFFFFFF.toInt() else 0xFFAAAAAA.toInt())
            setPadding(padH, padV, padH, padV)
            textSize = 15f
            setBackgroundColor(0xFF2A2A2A.toInt())
        }
        tv.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { marginEnd = dp(8) }
        if (clickable) tv.setOnClickListener { onClick() }
        candidateBar.addView(tv)
    }

    private fun saveClipboard() {
        val clip = try {
            clipboardManager.primaryClip
        } catch (_: SecurityException) {
            return
        } ?: return

        val extras = clip.description?.extras
        if (extras?.getBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, false) == true) {
            return
        }

        if (clip.itemCount <= 0) return
        val text = try {
            clip.getItemAt(0).coerceToText(this).toString().trim()
        } catch (e: Exception) {
            return
        }
        if (text.isEmpty()) return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            clipboardMutex.withLock {
                clipboardRepo.addIfNew(text)
            }
        }
    }

    private fun registerClipboardListenerIfAllowed() {
        if (sensitiveEditor || !prefs.clipboardHistoryEnabled || clipboardRegistered) return
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)
        clipboardRegistered = true
    }

    private fun unregisterClipboardListener() {
        if (!clipboardRegistered) return
        try {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            android.util.Log.e("NxtGenIME", "Failed to unregister clipboard listener", e)
        }
        clipboardRegistered = false
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun lastWordBeforeCursor(): String? =
        safeGetTextBeforeCursor(100)?.let {
            Regex("""(\p{L}+)([^\p{L}]*)$""").find(it)?.groupValues?.get(1)
        }

    private fun tryAutoCorrect(): Boolean {
        val lang = currentLang()
        if (!suggestionEngine.isLoaded(lang)) return false
        val word = lastWordBeforeCursor() ?: return false
        if (word.length < 2) return false
        val lower = word.lowercase(Locale.ROOT)
        if (suggestionEngine.contains(lang, lower)) return false
        val suggestion = suggestionEngine.suggest(lang, lower, 1, 1).firstOrNull() ?: return false
        if (safeDeleteSurroundingText(word.length, 0)) {
            safeCommitText(preserveCapitalization(word, suggestion))
            return true
        }
        return false
    }

    /** Learns a word only after it has been typed LEARN_THRESHOLD times in this session. */
    private fun maybeLearnCurrentWord() {
        if (!prefs.learnWords || noPersonalizedLearning || sensitiveEditor) return
        if (!suggestionEngine.isLoaded(currentLang())) return
        val word = lastWordBeforeCursor() ?: return
        if (word.length < 2) return
        val lower = word.lowercase(Locale.ROOT)
        if (suggestionEngine.contains(currentLang(), lower)) return
        val language = currentLang()
        val key = "$language:$lower"
        val count = (typedWordCounts[key] ?: 0) + 1
        typedWordCounts[key] = count
        if (count < LEARN_THRESHOLD) return
        suggestionEngine.addUserWord(language, lower)
        scope.launch(Dispatchers.IO) {
            userWordRepo.add(language, lower)
        }
    }

    private fun preserveCapitalization(original: String, suggestion: String): String = when {
        original.isNotEmpty() && original.all(Char::isUpperCase) -> suggestion.uppercase(Locale.ROOT)
        original.firstOrNull()?.isUpperCase() == true -> suggestion.replaceFirstChar { it.uppercase(Locale.ROOT) }
        else -> suggestion
    }

    private fun isPasswordInput(info: EditorInfo): Boolean {
        val inputType = info.inputType
        val clazz = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return (clazz == InputType.TYPE_CLASS_TEXT && variation in setOf(
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        )) || (clazz == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    private fun isNumericInput(info: EditorInfo): Boolean =
        (info.inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER

    private fun isPhoneInput(info: EditorInfo): Boolean =
        (info.inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_PHONE


    private fun hasNoSuggestions(info: EditorInfo): Boolean =
        (info.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0

    private fun hasAutoComplete(info: EditorInfo): Boolean =
        (info.inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0

    private fun editorAllowsAutoCorrect(info: EditorInfo): Boolean =
        (info.inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) != 0

    private fun safeCommitText(text: String): Boolean {
        return try {
            currentInputConnection?.commitText(text, 1) == true
        } catch (_: Exception) {
            false
        }
    }

    private fun safeDeleteSurroundingText(before: Int, after: Int): Boolean {
        if (before < 0 || after < 0) return false
        return try {
            currentInputConnection?.deleteSurroundingText(before, after) == true
        } catch (_: Exception) {
            false
        }
    }

    private fun safeSetComposingText(text: String, pos: Int): Boolean {
        return try {
            currentInputConnection?.setComposingText(text, pos) == true
        } catch (_: Exception) {
            false
        }
    }

    private fun safeFinishComposingText(): Boolean {
        return try {
            currentInputConnection?.finishComposingText() == true
        } catch (_: Exception) {
            false
        }
    }


    private fun safeGetSelectedText(): String? =
        try {
            currentInputConnection?.getSelectedText(0)?.toString()
        } catch (_: Exception) {
            null
        }

    private fun safeGetTextBeforeCursor(max: Int): String? =
        try {
            currentInputConnection?.getTextBeforeCursor(max, 0)?.toString()
        } catch (_: Exception) {
            null
        }

    private fun safeGetTextAfterCursor(max: Int): String? =
        try {
            currentInputConnection?.getTextAfterCursor(max, 0)?.toString()
        } catch (_: Exception) {
            null
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onPress(primaryCode: Int) {
        if (prefs.soundEnabled) playClick(primaryCode)
        if (prefs.vibrationEnabled) vibrate()
    }

    override fun onRelease(primaryCode: Int) = Unit
    override fun onText(text: CharSequence?) = Unit
    override fun swipeLeft() = Unit
    override fun swipeRight() = Unit
    override fun swipeDown() = Unit
    override fun swipeUp() = Unit

    private fun playClick(keyCode: Int) {
        val audio = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val sound = when (keyCode) {
            CODE_DELETE -> AudioManager.FX_KEYPRESS_DELETE
            CODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            32 -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
        audio.playSoundEffect(sound, -1f)
    }

    private fun vibrate() {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() != true) return
        try {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } catch (_: Exception) {
        }
    }
}