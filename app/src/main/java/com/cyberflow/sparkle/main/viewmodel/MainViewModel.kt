package com.cyberflow.sparkle.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import com.cyberflow.base.model.DailyHoroScopeData
import com.cyberflow.base.net.Api
import com.cyberflow.base.viewmodel.BaseViewModel
import com.drake.net.Post

class MainViewModel : BaseViewModel() {

    var horoScopeData: MutableLiveData<DailyHoroScopeData> = MutableLiveData()


    fun getDailyHoroscope() = scopeNetLife {
        horoScopeData.value = Post<DailyHoroScopeData>(Api.DAILY_HOROSCOPE) {}.await()
    }
}