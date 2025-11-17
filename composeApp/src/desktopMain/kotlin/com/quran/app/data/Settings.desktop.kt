package com.quran.app.data

import java.util.prefs.Preferences

actual class Settings {
    private val prefs: Preferences

    actual constructor() {
        prefs = Preferences.userRoot().node("com/quran/app")
    }

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.get(key, defaultValue)
    }

    actual fun remove(key: String) {
        prefs.remove(key)
        prefs.flush()
    }

    actual fun clear() {
        prefs.clear()
        prefs.flush()
    }
}
