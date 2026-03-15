package com.couplebase.core.datastore

private fun jsGetItem(key: String): JsString? =
    js("window.localStorage.getItem(key)")

private fun jsSetItem(key: String, value: String): JsAny? =
    js("window.localStorage.setItem(key, value)")

private fun jsRemoveItem(key: String): JsAny? =
    js("window.localStorage.removeItem(key)")

private fun jsClear(): JsAny? =
    js("window.localStorage.clear()")

actual class PlatformStorage {

    actual fun getString(key: String): String? = jsGetItem(key)?.toString()

    actual fun putString(key: String, value: String) {
        jsSetItem(key, value)
    }

    actual fun remove(key: String) {
        jsRemoveItem(key)
    }

    actual fun clear() {
        jsClear()
    }
}
