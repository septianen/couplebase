package com.couplebase.core.common

import platform.Foundation.NSUUID

actual fun generateUuid(): String = NSUUID().UUIDString()
