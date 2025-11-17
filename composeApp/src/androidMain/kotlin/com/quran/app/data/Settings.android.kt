package com.quran.app.data

import android.content.Context
import android.content.SharedPreferences

actual class Settings {
    private val prefs: SharedPreferences

    actual constructor() {
        prefs = ApplicationContextHolder.context.getSharedPreferences(
            "quran_app_prefs",
            Context.MODE_PRIVATE
        )
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}

object ApplicationContextHolder {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
