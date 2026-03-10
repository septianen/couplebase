package com.couplebase.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Maps each emitted value of a Flow into a Result.Success,
 * catching any exceptions as Result.Error.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> { Result.Success(it) }
        .catch { emit(Result.Error(it, it.message)) }
