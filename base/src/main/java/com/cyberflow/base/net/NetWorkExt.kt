package com.cyberflow.base.net

import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.cyberflow.base.BaseApp
import com.drake.net.NetConfig
import com.drake.net.okhttp.setConverter
import java.util.concurrent.TimeUnit

fun initNetUnsplash() {
    NetConfig.initialize("https://api.unsplash.com/") {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        writeTimeout(30, TimeUnit.SECONDS)
//        setConverter(SerializationConverter())
        setConverter(GsonConverter())
        addInterceptor(ChuckerInterceptor(BaseApp.instance!!))
        retryOnConnectionFailure(true)
//            addInterceptor()
//            cache()
    }
}