package com.cyberflow.sparkle.main.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.BaseApp
import com.cyberflow.base.model.IMConversationCache
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendList
import com.cyberflow.base.model.IMFriendRequest
import com.cyberflow.base.model.IMFriendRequestList
import com.cyberflow.base.model.IMQuestionList
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.common.enums.Status
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.cyberflow.sparkle.im.DBManager
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.luck.picture.lib.utils.ToastUtils.showToast
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {


    var inviteMsgObservable: MutableLiveData<IMFriendRequestList> = MutableLiveData()
    fun loadFriendRequestMessages() = scopeNet {
        inviteMsgObservable.value = Post<IMFriendRequestList>(Api.RELATIONSHIP_FRIEND_REQUEST_LIST) {}.await()
    }

    var contactObservable: MutableLiveData<IMFriendList> = MutableLiveData()
    fun loadContactList()  = scopeNet {
        contactObservable.value = Post<IMFriendList>(Api.IM_QUESTIONS) {}.await()
    }

    var imQuestionsObservable: MutableLiveData<IMQuestionList> = MutableLiveData()
    fun loadIMQuestions() = scopeNet {
        imQuestionsObservable.value = Post<IMQuestionList>(Api.RELATIONSHIP_FRIEND_LIST) {}.await()
    }

    // ------------------------------------------- IM -------------------------------------------

    // 会先刷新 新朋友消息 或 联系人   然后会继续拿会话信息
    fun loadContactAndRequestListData(){
        loadFriendRequestMessages()
        loadContactList()
    }

    fun freshConversationData(){
        //需要两个条件，判断是否触发从服务器拉取会话列表的时机，一是第一次安装，二则本地数据库没有会话列表数据
        if (DemoHelper.getInstance().isFirstInstall && EMClient.getInstance().chatManager().allConversations.isEmpty()) {
            fetchConversationsFromServer()
        } else {
            getConversationFromCache()
        }
    }

    val conversationInfoObservable = SingleSourceLiveData<Resource<List<EaseConversationInfo>>>()
    val chatRepository = EMChatManagerRepository()

    fun fetchConversationsFromServer() {  // get conversations from server
        conversationInfoObservable.setSource(chatRepository.fetchConversationsFromServer())
    }

    val conversationCacheObservable = MutableLiveData<List<EaseConversationInfo>>()
    fun getConversationFromCache() {
        viewModelScope.launch {
            val cache = EMClient.getInstance().chatManager().allConversations
            if (cache.isEmpty()) {
                conversationCacheObservable.postValue(emptyList())
                return@launch
            }
            // transfer EMCoversation to EaseConversationInfo
            val newList = cache.filter {
                !it.value.conversationId().equals(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID)
            }.map {
                EaseConversationInfo().apply {
                    info = it.value
                    val conversation = it.value
                    val extField: String = conversation.extField
                    val lastMsgTime: Long =
                        if (conversation.lastMessage == null) 0 else conversation.lastMessage.msgTime
                    if (!TextUtils.isEmpty(extField) && EaseCommonUtils.isTimestamp(extField)) {
                        isTop = true
                        val makeTopTime = extField.toLong()
                        timestamp = if (makeTopTime > lastMsgTime) {
                            makeTopTime
                        } else {
                            lastMsgTime
                        }
                    } else {
                        timestamp = lastMsgTime
                    }
                }
            }.sortedBy {
                it.isTop
            }.sortedByDescending {
                it.timestamp
            }
            conversationCacheObservable.postValue(newList)
        }
    }

    fun saveInviteData2DB(data: List<IMFriendRequest>?) {
        viewModelScope.launch {
            DBManager.instance.db?.imFriendRequestDao()?.deleteAll()
            if(data.isNullOrEmpty()){

            }else{
                DBManager.instance.db?.imFriendRequestDao()?.insert(*data.toTypedArray())
            }
        }
    }

    fun saveContactData2DB(data: List<IMFriendInfo>?) {
        viewModelScope.launch {
            DBManager.instance.db?.imFriendInfoDao()?.deleteAll()
            if(data.isNullOrEmpty()){
            }else{
                DBManager.instance.db?.imFriendInfoDao()?.insert(*data.toTypedArray())
            }
        }
    }

    fun saveConversationData2DB(unRead: HashMap<String, Int>, data: ArrayList<IMFriendInfo>) {
        viewModelScope.launch {
            DBManager.instance.db?.imConversationCacheDao()?.deleteAll()
            if(data.isNullOrEmpty()){
            }else{
                val cache = data.map {
                    IMConversationCache(
                        open_uid = it.open_uid,
                        nick = it.nick,
                        gender = 1,
                        avatar = it.avatar,
                        num = unRead[it.open_uid.replace("-", "_")] ?: 0,
                        bgColor = "#ffffff",
                        )
                }
                DBManager.instance.db?.imConversationCacheDao()?.insert(*cache.toTypedArray())
            }
        }
    }

    var contactList: List<IMFriendInfo>? = null
    var conversationList: List<EaseConversationInfo>? = null

}

fun <T> parseResource(response: Resource<T>?, callback: OnResourceParseCallback<T>) {
    if (response == null) {
        return
    }
    if (response.status === Status.SUCCESS) {
        callback.hideLoading()
        callback.onSuccess(response.data)
    } else if (response.status === Status.ERROR) {
        callback.hideLoading()
        if (!callback.hideErrorMsg) {
            showToast(BaseApp.instance, response.message)
        }
        callback.onError(response.errorCode, response.message)
    } else if (response.status === Status.LOADING) {
        callback.onLoading(response.data)
    }
}