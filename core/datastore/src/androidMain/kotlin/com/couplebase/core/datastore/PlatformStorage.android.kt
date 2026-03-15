package com.couplebase.core.datastore

import android.content.Context
import android.content.SharedPreferences

actual class PlatformStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("couplebase_prefs", Context.MODE_PRIVATE)

    actual fun getString(key: String): String? = prefs.getString(key, null)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
