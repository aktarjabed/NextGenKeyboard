package com.aktarjabed.nxtgenkeyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.aktarjabed.nxtgenkeyboard.R
import com.aktarjabed.nxtgenkeyboard.util.AppPrefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val prefs = AppPrefs(this)

        val dp = { value: Int -> (value * resources.displayMetrics.density).toInt() }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        layout.addView(TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 22f
        })

        layout.addView(TextView(this).apply {
            text = getString(R.string.privacy_summary)
            textSize = 14f
            setPadding(0, dp(12), 0, dp(20))
        })

        val online = CheckBox(this).apply {
            text = getString(R.string.online_grammar)
            isChecked = prefs.useOnlineGrammar
        }
        var ignoreOnlineCheck = false
        online.setOnCheckedChangeListener { _, checked ->
            if (ignoreOnlineCheck) return@setOnCheckedChangeListener
            if (checked) {
                ignoreOnlineCheck = true
                online.isChecked = false
                ignoreOnlineCheck = false

                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(R.string.online_grammar)
                    .setMessage(R.string.online_grammar_warning)
                    .setPositiveButton(R.string.enable) { _, _ ->
                        prefs.useOnlineGrammar = true
                        ignoreOnlineCheck = true
                        online.isChecked = true
                        ignoreOnlineCheck = false
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setOnCancelListener { }
                    .show()
            } else {
                prefs.useOnlineGrammar = false
            }
        }

        val clipboard = CheckBox(this).apply {
            text = getString(R.string.clipboard_history)
            isChecked = prefs.clipboardHistoryEnabled
        }
        var ignoreClipboardCheck = false
        clipboard.setOnCheckedChangeListener { _, checked ->
            if (ignoreClipboardCheck) return@setOnCheckedChangeListener
            if (checked) {
                ignoreClipboardCheck = true
                clipboard.isChecked = false
                ignoreClipboardCheck = false

                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(R.string.clipboard_history)
                    .setMessage(R.string.clipboard_warning)
                    .setPositiveButton(R.string.enable) { _, _ ->
                        prefs.clipboardHistoryEnabled = true
                        ignoreClipboardCheck = true
                        clipboard.isChecked = true
                        ignoreClipboardCheck = false
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setOnCancelListener { }
                    .show()
            } else {
                prefs.clipboardHistoryEnabled = false
            }
        }

        val autocorrect = CheckBox(this).apply {
            text = getString(R.string.autocorrect)
            isChecked = prefs.autoCorrect
            setOnCheckedChangeListener { _, value -> prefs.autoCorrect = value }
        }

        val autoCap = CheckBox(this).apply {
            text = getString(R.string.auto_capitalization)
            isChecked = prefs.autoCapitalize
            setOnCheckedChangeListener { _, value -> prefs.autoCapitalize = value }
        }

        val learnWords = CheckBox(this).apply {
            text = getString(R.string.learn_new_words)
            isChecked = prefs.learnWords
            setOnCheckedChangeListener { _, value -> prefs.learnWords = value }
        }

        val sound = CheckBox(this).apply {
            text = getString(R.string.key_sound)
            isChecked = prefs.soundEnabled
            setOnCheckedChangeListener { _, value -> prefs.soundEnabled = value }
        }

        val vibration = CheckBox(this).apply {
            text = getString(R.string.key_vibration)
            isChecked = prefs.vibrationEnabled
            setOnCheckedChangeListener { _, value -> prefs.vibrationEnabled = value }
        }

        val systemSettings = Button(this).apply {
            text = getString(R.string.open_keyboard_settings)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        val history = Button(this).apply {
            text = getString(R.string.open_clipboard_history)
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, ClipboardHistoryActivity::class.java))
            }
        }

        layout.addView(online)
        layout.addView(clipboard)
        layout.addView(autocorrect)
        layout.addView(autoCap)
        layout.addView(learnWords)
        layout.addView(sound)
        layout.addView(vibration)
        layout.addView(systemSettings)
        layout.addView(history)

        val scrollView = ScrollView(this).apply {
            addView(layout)
        }

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(scrollView)
    }
}