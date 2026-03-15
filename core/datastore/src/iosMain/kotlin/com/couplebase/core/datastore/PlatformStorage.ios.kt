package com.couplebase.core.datastore

import platform.Foundation.NSUserDefaults

actual class PlatformStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getString(key: String): String? = defaults.stringForKey(key)

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    actual fun clear() {
        val domain = defaults.volatileDomainForName("couplebase")
        domain.allKeys.forEach { key ->
            defaults.removeObjectForKey(key as String)
        }
    }
}
