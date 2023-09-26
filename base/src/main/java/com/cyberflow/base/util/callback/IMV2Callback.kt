package com.cyberflow.base.util.callback

/**
 * 新版泛用型回调
 */
interface IMV2Callback<T> {
    fun onEvent(event: T)
}

data class IMLoginResponse(val success: Boolean, val message: String? = null)