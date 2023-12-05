package com.cyberflow.base.net

import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.CacheUtil
import com.drake.net.NetConfig
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.drake.net.okhttp.setDialogFactory
import com.drake.net.okhttp.setErrorHandler
import com.drake.tooltip.dialog.BubbleDialog
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "NetWorkExt"

fun initNetSpark(cacheDir: File) {
    NetConfig.initialize(Api.HOST) {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(20, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
        cache(Cache(cacheDir, 1024 * 1024 * 128))
        setDebug(true)
        setErrorHandler(NetworkingErrorHandler())
        setConverter(SerializationConverter())
        addInterceptor(LogRecordInterceptor(true))
        addInterceptor(HeaderInterceptor())
        addInterceptor(ResponseHeaderInterceptor())
        addInterceptor(ChuckerInterceptor(BaseApp.instance!!))
        setDialogFactory{
            BubbleDialog(it, "loading")
        }
    }
}

class HeaderInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val original: Request = chain.request()

        // todo optimization
        val token = CacheUtil.getUserInfo()?.token.orEmpty()
        var local = "zh-Hans-CN"
        val current = MultiLanguages.getAppLanguage()
        if (current.language.equals(LocaleContract.getEnglishLocale().language)) {
            local = "en_US"
        }
        Log.e(TAG, "intercept: x-token=$token" )
        val requestBuilder: Request.Builder = original.newBuilder()
            .addHeader("x-token", token)
            .addHeader("Accept-Language", local)
            .addHeader("User-Agent", "Sparkle/1.0 (iPhone; iOS 16.4; Scale/3.00)")
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