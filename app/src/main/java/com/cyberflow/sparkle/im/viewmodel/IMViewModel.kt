package com.cyberflow.sparkle.im.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository

class IMViewModel : BaseViewModel() {

    val mContactRepository = EMContactManagerRepository()

    var friendObservable: MediatorLiveData<Resource<Boolean>> = MediatorLiveData()
    fun addFriend(username: String, reason: String) {
        Log.e("TAG", "addFriend: username=$username reason=$reason")
        mContactRepository?.also {
            friendObservable?.addSource(it.addContact(username, reason)) { response ->
                friendObservable.value = response
            }
        }
    }

}