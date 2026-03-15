package com.couplebase.core.datastore

expect class PlatformStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
    fun clear()
}
