package com.cyberflow.sparkle.profile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.cyberflow.base.net.Api
import com.cyberflow.base.viewmodel.BaseViewModel
import com.drake.net.Post
import com.drake.net.utils.scopeNet

class ProfileViewModel : BaseViewModel() {

    var acceptFriendObservable: MutableLiveData<String> = MutableLiveData()

    fun acceptFriend(openUid : String?) = scopeNet {
        acceptFriendObservable.value = Post<String>(Api.RELATIONSHIP_FRIEND_ACCEPT) {
            json("open_uid" to openUid)
        }.await()
    }
}