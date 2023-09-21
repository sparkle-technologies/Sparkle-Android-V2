package com.cyberflow.base.util

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 注意：使用需要格外注意，不要掩盖问题
 */
object SafeGlobalScope : CoroutineScope {

    private const val TAG = "SafeGlobalScope"

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, Log.getStackTraceString(throwable))
    }

    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext + exceptionHandler
}