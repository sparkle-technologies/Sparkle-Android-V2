package com.cyberflow.base.net

import android.util.Log
import com.drake.net.Net
import com.drake.net.exception.ConvertException
import com.drake.net.exception.DownloadFileException
import com.drake.net.exception.HttpFailureException
import com.drake.net.exception.NetConnectException
import com.drake.net.exception.NetException
import com.drake.net.exception.NetSocketTimeoutException
import com.drake.net.exception.NoCacheException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ResponseException
import com.drake.net.exception.ServerResponseException
import com.drake.net.exception.URLParseException
import com.drake.net.interfaces.NetErrorHandler
import com.drake.net.utils.TipUtils
import java.net.UnknownHostException

class NetworkingErrorHandler : NetErrorHandler {
    override fun onError(e: Throwable) {

        Log.e("NetworkingErrorHandler", "onError: $e" )

        val message = when (e) {
            is UnknownHostException -> "UnknownHostException"
            is URLParseException -> "URLParseException"
            is NetConnectException -> "NetConnectException"
            is NetSocketTimeoutException ->  "NetSocketTimeoutException, ${e.message}"
            is DownloadFileException -> "DownloadFileException"
            is ConvertException -> "ConvertException"
            is RequestParamsException -> "RequestParamsException"
            is ServerResponseException -> "ServerResponseException"
            is NullPointerException -> "NullPointerException"
            is NoCacheException -> "NoCacheException"
            is ResponseException -> {
                if (e.tag == HttpResponseErrorCode.ErrTokenExpired) {
                    /* CacheUtil.logout()
                    ARouter.getInstance().build(PageConst.Login.PAGE_LOGIN_BY_WALLET)
                        .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .navigation()*/
                }
                Log.e("NetworkingErrorHandler", "onError: e.tag=${e.tag}" )

                HttpResponseErrorCode.handleErrorCode(e.tag.toString().toInt())
            }
            is HttpFailureException -> "HttpFailureException"
            is NetException -> "NetException"
            else -> "OtherError"
        }
        Net.debug(e)
        Log.e("TAG", "onError: message= $message" )
        TipUtils.toast(message)
    }
}