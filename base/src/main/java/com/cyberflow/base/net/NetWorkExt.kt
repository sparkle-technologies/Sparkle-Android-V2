package com.cyberflow.base.net

import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.CacheUtil
import com.drake.net.NetConfig
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.drake.net.okhttp.setErrorHandler
import com.drake.net.okhttp.setRequestInterceptor
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "NetWorkExt"

fun initNetSpark() {
    NetConfig.initialize(Api.HOST) {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(20, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        setDebug(true)
        setErrorHandler(NetworkingErrorHandler())
//        setConverter(SerializationConverter())
        setConverter(GsonConverter())
        addInterceptor(HeaderInterceptor())
        addInterceptor(ResponseHeaderInterceptor())
        addInterceptor(ChuckerInterceptor(BaseApp.instance!!))
    }
}

class HeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()
        val token = CacheUtil.getUserInfo()?.token.orEmpty()
        val requestBuilder: Request.Builder = original.newBuilder()
            .addHeader("x-token", token)
        val request: Request = requestBuilder.build()
        return chain.proceed(request)
    }
}

class ResponseHeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        response.header("new-token")?.also { newToken ->
            CacheUtil.getUserInfo()?.also { cache ->
                Log.e(TAG, "ResponseHeaderInterceptor  intercept:  更新token -----   new-token: $newToken ")
                cache.token = newToken
                CacheUtil.setUserInfo(cache)
            }
        }
        return response
    }
}