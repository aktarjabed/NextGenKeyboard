package com.aktarjabed.nxtgenkeyboard.util

import android.content.Context
import android.content.SharedPreferences

class AppPrefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("nxtgen_prefs", Context.MODE_PRIVATE)

    var languageIndex: Int
        get() = sp.getInt("lang", 0)
        set(value) = sp.edit().putInt("lang", value).apply()

    var useOnlineGrammar: Boolean
        get() = sp.getBoolean("online_grammar", false)
        set(value) = sp.edit().putBoolean("online_grammar", value).apply()

    var autoCorrect: Boolean
        get() = sp.getBoolean("auto_correct", false)
        set(value) = sp.edit().putBoolean("auto_correct", value).apply()

    var autoCapitalize: Boolean
        get() = sp.getBoolean("auto_capitalize", true)
        set(value) = sp.edit().putBoolean("auto_capitalize", value).apply()

    var learnWords: Boolean
        get() = sp.getBoolean("learn_words", true)
        set(value) = sp.edit().putBoolean("learn_words", value).apply()

    var soundEnabled: Boolean
        get() = sp.getBoolean("sound_enabled", true)
        set(value) = sp.edit().putBoolean("sound_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = sp.getBoolean("vibration_enabled", true)
        set(value) = sp.edit().putBoolean("vibration_enabled", value).apply()

    var clipboardHistoryEnabled: Boolean
        get() = sp.getBoolean("clipboard_history_enabled", false)
        set(value) = sp.edit().putBoolean("clipboard_history_enabled", value).apply()
}