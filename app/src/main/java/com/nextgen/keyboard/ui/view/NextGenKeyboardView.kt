package com.nextgen.keyboard.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nextgen.keyboard.R
import com.nextgen.keyboard.data.model.Clip
import com.nextgen.keyboard.data.model.KeyboardLayout
import com.nextgen.keyboard.data.repository.ClipboardRepository
import com.nextgen.keyboard.feature.swipe.SwipeDetector
import com.nextgen.keyboard.ui.adapter.ClipboardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.sqrt

@AndroidEntryPoint
class NextGenKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    @Inject
    lateinit var clipboardRepository: ClipboardRepository

    private var keyPressListener: ((String) -> Unit)? = null
    private var clipboardPopup: PopupWindow? = null
    private var variationsPopup: PopupWindow? = null

    private val swipeDetector = SwipeDetector(
        onWordPredicted = { word ->
            if (word.isNotEmpty() && isSwipeEnabled) {
                keyPressListener?.invoke("$word ")
                showSwipePrediction(word)
                if (isHapticEnabled) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }
        },
        onSwipeProgress = { progress ->
            updateSwipeVisualFeedback(progress)
        }
    )

    private val keyBounds = mutableMapOf<String, Rect>()
    private var lastTouchKey: String? = null
    private var swipePreview: TextView? = null
    private val keyboardScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentLayout: KeyboardLayout = KeyboardLayout.Qwerty
    private var isSwipeEnabled = true
    private var isHapticEnabled = true
    private var clipboardButton: Button? = null
    private var isDarkMode = true

    init {
        try {
            orientation = VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, R.color.keyboard_background))
            isFocusable = true
            isFocusableInTouchMode = true
            isHapticFeedbackEnabled = true
            setupSwipePreview()
            updateKeyboardLayout(currentLayout)
            Timber.d("NextGenKeyboardView initialized")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing keyboard view")
        }
    }

    private fun setupSwipePreview() {
        try {
            swipePreview = TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                setBackgroundResource(R.drawable.swipe_preview_background)
                setTextColor(ContextCompat.getColor(context, R.color.neon_blue))
                textSize = 14f
                gravity = Gravity.CENTER
                isVisible = false
                setPadding(16, 8, 16, 8)
            }
            addView(swipePreview)
        } catch (e: Exception) {
            Timber.e(e, "Error setting up swipe preview")
        }
    }

    private fun showSwipePrediction(word: String) {
        try {
            swipePreview?.let { preview ->
                preview.text = word
                preview.isVisible = true
                preview.alpha = 1f
                keyboardScope.launch {
                    delay(1500)
                    preview.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction { preview.isVisible = false }
                        .start()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error showing swipe prediction")
        }
    }

    private fun updateSwipeVisualFeedback(progress: Float) {
        try {
            swipePreview?.alpha = progress
        } catch (e: Exception) {
            Timber.e(e, "Error updating swipe feedback")
        }
    }

    fun updateKeyboardLayout(layout: KeyboardLayout) {
        try {
            currentLayout = layout
            rebuildKeyboardView()
            Timber.d("Layout updated to: ${layout.name}")
        } catch (e: Exception) {
            Timber.e(e, "Error updating keyboard layout")
        }
    }

    private fun rebuildKeyboardView() {
        try {
            removeAllViews()
            addView(swipePreview)
            currentLayout.rows.forEach { rowKeys ->
                addKeyRow(rowKeys)
            }
            requestLayout()
        } catch (e: Exception) {
            Timber.e(e, "Error rebuilding keyboard view")
        }
    }

    private fun addKeyRow(keys: List<String>) {
        try {
            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER
            }

            keys.forEach { key ->
                val keyView = createKeyView(key)
                if (key == "📋") clipboardButton = keyView
                row.addView(keyView)
            }

            addView(row)
        } catch (e: Exception) {
            Timber.e(e, "Error adding key row")
        }
    }

    private fun createKeyView(key: String): Button {
        return Button(context, null, 0, R.style.KeyboardKeyStyle).apply {
            text = key
            layoutParams = LayoutParams(
                0,
                resources.getDimensionPixelSize(R.dimen.key_height),
                when (key) {
                    "SPACE" -> 3f
                    "⌫", "↵", "📋" -> 1.5f
                    else -> 1f
                }
            ).apply {
                val margin = resources.getDimensionPixelSize(R.dimen.key_margin)
                setMargins(margin, margin, margin, margin)
            }

            setOnClickListener { handleKeyClick(key) }
            setOnLongClickListener {
                handleKeyLongPress(key)
                true
            }

            post {
                try {
                    val bounds = Rect()
                    getHitRect(bounds)
                    keyBounds[key] = bounds
                } catch (e: Exception) {
                    Timber.e(e, "Error calculating key bounds for: $key")
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSwipeEnabled) return super.onTouchEvent(event)

        try {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val key = getKeyAtPosition(x, y)
                    lastTouchKey = key
                    key?.let {
                        if (it.length == 1 && currentLayout in listOf(
                                KeyboardLayout.Qwerty,
                                KeyboardLayout.Dvorak,
                                KeyboardLayout.Colemak
                            )) {
                            swipeDetector.startSwipe(x, y)
                            swipeDetector.addKeyToTrail(it)
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentKey = getKeyAtPosition(x, y)
                    if (currentKey != null && currentKey != lastTouchKey) {
                        lastTouchKey = currentKey
                        if (currentKey.length == 1 && currentLayout in listOf(
                                KeyboardLayout.Qwerty,
                                KeyboardLayout.Dvorak,
                                KeyboardLayout.Colemak
                            )) {
                            swipeDetector.addSwipePoint(x, y, currentKey)
                        }
                    } else {
                        swipeDetector.addSwipePoint(x, y)
                    }
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (swipeDetector.isActive()) {
                        swipeDetector.endSwipe()
                    }
                    lastTouchKey = null
                    return true
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling touch event")
        }

        return super.onTouchEvent(event)
    }

    private fun getKeyAtPosition(x: Float, y: Float): String? {
        try {
            val point = Point(x.toInt(), y.toInt())
            return keyBounds.entries.find { it.value.contains(point.x, point.y) }?.key
        } catch (e: Exception) {
            Timber.e(e, "Error getting key at position")
            return null
        }
    }

    private fun handleKeyClick(key: String) {
        try {
            if (isHapticEnabled) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }

            when (key) {
                "⌫" -> keyPressListener?.invoke("BACKSPACE")
                "SPACE" -> keyPressListener?.invoke(" ")
                "↵" -> keyPressListener?.invoke("ENTER")
                "📋" -> showClipboardPopup()
                else -> keyPressListener?.invoke(key)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling key click: $key")
        }
    }

    private fun handleKeyLongPress(key: String) {
        try {
            if (isHapticEnabled) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }

            when (key) {
                "⌫" -> keyPressListener?.invoke("DELETE_WORD")
                "SPACE" -> showKeyboardSettings()
                else -> showKeyVariations(key)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling long press: $key")
        }
    }

    private fun showClipboardPopup() {
        try {
            val popupView = LayoutInflater.from(context).inflate(R.layout.clipboard_popup, null)
            setupClipboardPopup(popupView)

            clipboardPopup = PopupWindow(
                popupView,
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                showAtLocation(this@NextGenKeyboardView, Gravity.BOTTOM, 0, 0)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to show clipboard popup")
            Toast.makeText(context, "Error opening clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClipboardPopup(popupView: ViewGroup) {
        try {
            val recyclerView = popupView.findViewById<RecyclerView>(R.id.clipboard_recycler)
            val searchEdit = popupView.findViewById<EditText>(R.id.search_edit)
            val closeButton = popupView.findViewById<Button>(R.id.close_button)

            val adapter = ClipboardAdapter(
                onItemClick = { clip ->
                    keyPressListener?.invoke("${clip.content} ")
                    clipboardPopup?.dismiss()
                    if (isHapticEnabled) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                },
                onPinClick = { clip ->
                    keyboardScope.launch {
                        try {
                            val updatedClip = clip.copy(isPinned = !clip.isPinned)
                            clipboardRepository.updateClip(updatedClip)
                        } catch (e: Exception) {
                            Timber.e(e, "Error updating clip pin status")
                        }
                    }
                }
            )

            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
                setHasFixedSize(true)
            }

            keyboardScope.launch {
                try {
                    combine(
                        clipboardRepository.getPinnedClips(),
                        clipboardRepository.getRecentClips()
                    ) { pinned, recent -> pinned + recent }
                        .collect { clips -> adapter.submitList(clips) }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading clipboard data")
                }
            }

            var searchJob: Job? = null
            searchEdit.addTextChangedListener { text ->
                searchJob?.cancel()
                searchJob = keyboardScope.launch {
                    try {
                        delay(300)
                        val query = text.toString()
                        if (query.isBlank()) {
                            val clips = combine(
                                clipboardRepository.getPinnedClips(),
                                clipboardRepository.getRecentClips()
                            ) { pinned, recent -> pinned + recent }.first()
                            adapter.submitList(clips)
                        } else {
                            val results = clipboardRepository.searchClips(query).getOrElse { emptyList() }
                            adapter.submitList(results)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error searching clips")
                    }
                }
            }

            closeButton.setOnClickListener { clipboardPopup?.dismiss() }
        } catch (e: Exception) {
            Timber.e(e, "Error setting up clipboard popup")
        }
    }

    private fun showKeyboardSettings() {
        Toast.makeText(context, "Long press Space: Open Keyboard Settings", Toast.LENGTH_SHORT).show()
    }

    private fun showKeyVariations(key: String) {
        try {
            val variations = when (key.uppercase()) {
                "A" -> listOf("à", "á", "â", "ã", "ä", "å", "ā", "ă")
                "E" -> listOf("è", "é", "ê", "ë", "ē", "ė", "ę")
                "I" -> listOf("ì", "í", "î", "ï", "ī", "į")
                "O" -> listOf("ò", "ó", "ô", "õ", "ö", "ø", "ō")
                "U" -> listOf("ù", "ú", "û", "ü", "ū", "ų")
                "C" -> listOf("ç", "ć", "č")
                "N" -> listOf("ñ", "ń")
                "S" -> listOf("ß", "ś", "š")
                "0" -> listOf("°", "₀", "⁰")
                "-" -> listOf("–", "—", "−", "_")
                "?" -> listOf("¿", "‽")
                "!" -> listOf("¡")
                else -> return
            }

            showVariationsPopup(key, variations)
        } catch (e: Exception) {
            Timber.e(e, "Error showing key variations for: $key")
        }
    }

    private fun showVariationsPopup(baseKey: String, variations: List<String>) {
        try {
            val popupView = LinearLayout(context).apply {
                orientation = HORIZONTAL
                setBackgroundResource(R.drawable.swipe_preview_background)
                setPadding(16, 8, 16, 8)
            }

            variations.forEach { variant ->
                val button = Button(context, null, 0, R.style.KeyboardKeyStyle).apply {
                    text = variant
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.key_height),
                        resources.getDimensionPixelSize(R.dimen.key_height)
                    ).apply {
                        val margin = resources.getDimensionPixelSize(R.dimen.key_margin)
                        setMargins(margin, 0, margin, 0)
                    }
                    setOnClickListener {
                        keyPressListener?.invoke(variant)
                        if (isHapticEnabled) {
                            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        variationsPopup?.dismiss()
                    }
                }
                popupView.addView(button)
            }

            variationsPopup = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                showAtLocation(this@NextGenKeyboardView, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error showing variations popup")
        }
    }

    fun setPasswordMode(isPasswordField: Boolean) {
        try {
            isSwipeEnabled = !isPasswordField
            clipboardButton?.isEnabled = !isPasswordField
            clipboardButton?.alpha = if (isPasswordField) 0.3f else 1.0f

            val bgColor = if (isPasswordField) {
                R.color.password_mode_background
            } else {
                if (isDarkMode) R.color.keyboard_background else R.color.light_background
            }
            setBackgroundColor(ContextCompat.getColor(context, bgColor))

            Timber.d("Password mode: $isPasswordField")
        } catch (e: Exception) {
            Timber.e(e, "Error setting password mode")
        }
    }

    fun setOnKeyPressListener(listener: (String) -> Unit) {
        keyPressListener = listener
    }

    fun updateTheme(isDark: Boolean) {
        try {
            isDarkMode = isDark
            val bgColor = if (isDark) R.color.keyboard_background else R.color.light_background
            val keyBg = if (isDark) R.color.key_background else R.color.light_key_background
            val keyText = if (isDark) R.color.key_text else R.color.light_key_text

            setBackgroundColor(ContextCompat.getColor(context, bgColor))

            for (i in 0 until childCount) {
                val row = getChildAt(i) as? LinearLayout ?: continue
                for (j in 0 until row.childCount) {
                    (row.getChildAt(j) as? Button)?.apply {
                        setBackgroundColor(ContextCompat.getColor(context, keyBg))
                        setTextColor(ContextCompat.getColor(context, keyText))
                    }
                }
            }

            Timber.d("Theme updated: ${if (isDark) "Dark" else "Light"}")
        } catch (e: Exception) {
            Timber.e(e, "Error updating theme")
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        isHapticEnabled = enabled
    }

    fun setSwipeEnabled(enabled: Boolean) {
        isSwipeEnabled = enabled
    }

    override fun onDetachedFromWindow() {
        try {
            keyboardScope.cancel()
            clipboardPopup?.dismiss()
            variationsPopup?.dismiss()
        } catch (e: Exception) {
            Timber.e(e, "Error in onDetachedFromWindow")
        } finally {
            super.onDetachedFromWindow()
        }
    }

    private data class Point(val x: Int, val y: Int)
}