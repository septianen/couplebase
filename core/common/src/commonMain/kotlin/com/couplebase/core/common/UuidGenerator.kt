package com.couplebase.core.common

/**
 * Multiplatform UUID generator.
 * Uses expect/actual to delegate to platform-specific implementations.
 */
expect fun generateUuid(): String
