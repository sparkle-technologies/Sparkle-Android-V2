package com.cyberflow.base.util.callback

sealed class IMActionResult {

    class Success(val data: Any? = null) : IMActionResult()

    class Failure(val code: Int, val msg: String) : IMActionResult() {
        override fun toString(): String {
            return "Failure(code=$code, msg='$msg')"
        }
    }
}