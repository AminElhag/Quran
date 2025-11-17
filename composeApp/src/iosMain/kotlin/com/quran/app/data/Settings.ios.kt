package com.quran.app.data

import platform.Foundation.NSUserDefaults

actual class Settings {
    private val userDefaults: NSUserDefaults

    actual constructor() {
        userDefaults = NSUserDefaults.standardUserDefaults
    }

    actual fun putString(key: String, value: String) {
        userDefaults.setObject(value, forKey = key)
        userDefaults.synchronize()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return userDefaults.stringForKey(key) ?: defaultValue
    }

    actual fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }

    actual fun clear() {
        val keys = userDefaults.dictionaryRepresentation().keys
        keys.forEach { key ->
            userDefaults.removeObjectForKey(key as String)
        }
        userDefaults.synchronize()
    }
}
