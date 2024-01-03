package com.cyberflow.sparkle.main.viewmodel


// 暂时不放数据库  放内存  后面接口还要优化的
class HoroscopeDataManager private constructor() {

    companion object {
        const val TAG = "HoroscopeDataManager"
        val instance: HoroscopeDataManager by lazy { HoroscopeDataManager() }
    }

    private var dailyCache : List<String>?  =  null

    fun clearCache(){
        dailyCache = null

    }
}
