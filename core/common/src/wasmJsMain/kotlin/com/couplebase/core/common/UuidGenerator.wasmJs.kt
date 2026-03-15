package com.couplebase.core.common

private fun cryptoRandomUUID(): JsString = js("crypto.randomUUID()")

actual fun generateUuid(): String = cryptoRandomUUID().toString()
