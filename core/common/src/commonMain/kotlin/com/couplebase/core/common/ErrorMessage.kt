package com.couplebase.core.common

/**
 * Maps exceptions to user-friendly error messages.
 */
fun Result.Error.toUserMessage(): String {
    return message ?: when {
        exception.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
            "No internet connection. Please check your network."
        exception.message?.contains("timeout", ignoreCase = true) == true ->
            "Request timed out. Please try again."
        exception.message?.contains("401", ignoreCase = true) == true ->
            "Session expired. Please log in again."
        exception.message?.contains("403", ignoreCase = true) == true ->
            "You don't have permission to perform this action."
        exception.message?.contains("404", ignoreCase = true) == true ->
            "The requested resource was not found."
        exception.message?.contains("500", ignoreCase = true) == true ->
            "Server error. Please try again later."
        else -> "Something went wrong. Please try again."
    }
}
