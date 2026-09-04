package com.aktarjabed.nxtgenkeyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aktarjabed.nxtgenkeyboard.R
import com.aktarjabed.nxtgenkeyboard.util.AppPrefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPrefs(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        layout.addView(TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 22f
        })

        layout.addView(TextView(this).apply {
            text = getString(R.string.privacy_summary)
            textSize = 14f
            setPadding(0, 12, 0, 20)
        })

        val online = CheckBox(this).apply {
            text = getString(R.string.online_grammar)
            isChecked = prefs.useOnlineGrammar
        }
        online.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(R.string.online_grammar)
                    .setMessage(R.string.online_grammar_warning)
                    .setPositiveButton(R.string.enable) { _, _ -> prefs.useOnlineGrammar = true }
                    .setNegativeButton(R.string.cancel) { _, _ -> online.isChecked = false }
                    .show()
            } else {
                prefs.useOnlineGrammar = false
            }
        }

        val clipboard = CheckBox(this).apply {
            text = getString(R.string.clipboard_history)
            isChecked = prefs.clipboardHistoryEnabled
        }
        clipboard.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle(R.string.clipboard_history)
                    .setMessage(R.string.clipboard_warning)
                    .setPositiveButton(R.string.enable) { _, _ -> prefs.clipboardHistoryEnabled = true }
                    .setNegativeButton(R.string.cancel) { _, _ -> clipboard.isChecked = false }
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

        setContentView(layout)
    }
}