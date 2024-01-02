package com.cyberflow.sparkle.profile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.profile.view.Bond

class CompatibilityViewModel : BaseViewModel() {

    var bondData: MutableLiveData<Bond> = MutableLiveData()

}