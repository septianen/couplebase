package com.couplebase.core.common

import java.util.UUID

actual fun generateUuid(): String = UUID.randomUUID().toString()
