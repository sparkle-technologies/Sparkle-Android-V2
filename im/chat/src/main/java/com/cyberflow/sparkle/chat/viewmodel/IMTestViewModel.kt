package com.cyberflow.sparkle.chat.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.enums.Status
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.cyberflow.sparkle.chat.common.repositories.EMClientRepository
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMUserInfo
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.luck.picture.lib.utils.ToastUtils.showToast
import kotlinx.coroutines.launch


class IMTestViewModel : BaseViewModel() {

    var txt = MutableLiveData<String>("hi")

    var mRepository: EMClientRepository? = null
    var mchatRepository: EMChatManagerRepository? = null
    var mContactRepository: EMContactManagerRepository? = null

    var initObservable: MediatorLiveData<Resource<Boolean>> = MediatorLiveData()

    var createAccountObservable: MediatorLiveData<Resource<String>> = MediatorLiveData()
    var loginObservable: MediatorLiveData<Resource<EaseUser>> = MediatorLiveData()
    var logoutObservable: MediatorLiveData<Resource<Boolean>> = MediatorLiveData()

    var contactListObservable: MediatorLiveData<Resource<List<EaseUser>>> = MediatorLiveData()
    var conversationDBListObservable: MediatorLiveData<Resource<List<Any>>> = MediatorLiveData()
    var conversationListObservable: MediatorLiveData<Resource<List<EaseConversationInfo>>> =
        MediatorLiveData()

    fun init() {
        mRepository = EMClientRepository()
        mchatRepository = EMChatManagerRepository()
        mContactRepository = EMContactManagerRepository()
    }

    fun checkLogin() {
        mRepository?.also {
            initObservable?.addSource(it.loadAllInfoFromHX()) { response ->
                initObservable.value = response
            }
        }
    }

    fun create(name: String, pwd: String) {
        mRepository?.also {
            createAccountObservable?.addSource(it.registerToHx(name, pwd)) { response ->
                createAccountObservable.value = response
            }
        }
    }

    fun login(name: String, pwd: String) {
        mRepository?.also {
            loginObservable?.addSource(it.loginToServer(name, pwd, false)) { response ->
                Log.e("TAG", "viewmodel  login: ${response.data}")
                loginObservable.value = response
            }
        }
    }

    fun logout() {
        mRepository?.also {
            logoutObservable?.addSource(it.logout(false)) { response ->
                logoutObservable.value = response
            }
        }
    }

    var friendObservable: MediatorLiveData<Resource<Boolean>> = MediatorLiveData()
    fun addFriend(username: String, reason: String) {
        mContactRepository?.also {
            friendObservable?.addSource(it.addContact(username, reason)) { response ->
                friendObservable.value = response
            }
        }
    }

    fun acceptFriend(username: String) {
        mContactRepository?.also {
            friendObservable?.addSource(it.acceptInvitation(username)) { response ->
                friendObservable.value = response
            }
        }
    }

    fun refuseFriend(username: String) {
        mContactRepository?.also {
            friendObservable?.addSource(it.declineInvitation(username)) { response ->
                friendObservable.value = response
            }
        }
    }

    fun getContactList() {
        mContactRepository?.also {
            contactListObservable?.addSource(it.getContactList(true)) { response ->
                contactListObservable.value = response
            }
        }
    }

    fun getConversationListFromDB() {
        mchatRepository?.also {
            conversationDBListObservable?.addSource(it.loadConversationList()) { response ->
                conversationDBListObservable.value = response
            }
        }
    }

    fun getConversationListFromServer(pageNum: Int, pageSize: Int) {
        mchatRepository?.also {
            conversationListObservable?.addSource(
                it.fetchConversationsFromServer(
                    pageNum,
                    pageSize
                )
            ) { response ->
                conversationListObservable.value = response
            }
        }
    }

    fun getConversationListFromServer() {
        mchatRepository?.also {
            conversationListObservable?.addSource(it.fetchConversationsFromServer()) { response ->
                conversationListObservable.value = response
            }
        }
    }

    fun updateInfo(username: String) {
        viewModelScope.launch {
            val userInfo = EMUserInfo()
            userInfo.userId = EMClient.getInstance().currentUser
            userInfo.nickname = "micheal"
            userInfo.avatarUrl = "https://www.easemob.com"
            userInfo.birth = "2000.10.10"
            userInfo.signature = "hello world"
            userInfo.phoneNumber = "13333333333"
            userInfo.email = "123456@qq.com"
            userInfo.gender = 1
            EMClient.getInstance().userInfoManager()
                .updateOwnInfo(userInfo, object : EMValueCallBack<String?> {
                    override fun onSuccess(value: String?) {}
                    override fun onError(error: Int, errorMsg: String) {}
                })
        }
    }
}

fun <T> Activity.parseResource(response: Resource<T>?, callback: OnResourceParseCallback<T>) {
    if (response == null) {
        return
    }
    if (response.status === Status.SUCCESS) {
        callback.hideLoading()
        callback.onSuccess(response.data)
    } else if (response.status === Status.ERROR) {
        callback.hideLoading()
        if (!callback.hideErrorMsg) {
            showToast(this, response.message)
        }
        callback.onError(response.errorCode, response.message)
    } else if (response.status === Status.LOADING) {
        callback.onLoading(response.data)
    }
}
