package com.cyberflow.sparkle.main.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.BaseApp
import com.cyberflow.base.model.DailyHoroScopeData
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.DemoHelper
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper
import com.cyberflow.sparkle.chat.common.db.dao.InviteMessageDao
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.enums.Status
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.OnResourceParseCallback
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.utils.EaseCommonUtils
import com.luck.picture.lib.utils.ToastUtils.showToast
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel {

    var horoScopeData: MutableLiveData<DailyHoroScopeData> = MutableLiveData()

    constructor() : super()


    fun getDailyHoroscope() = scopeNetLife {
//        horoScopeData.value = Post<DailyHoroScopeData>(Api.DAILY_HOROSCOPE) {}.await()
    }


    // ------------------------------------------- IM -------------------------------------------


    fun refreshIMData(){
        //需要两个条件，判断是否触发从服务器拉取会话列表的时机，一是第一次安装，二则本地数据库没有会话列表数据
        if (DemoHelper.getInstance().isFirstInstall && EMClient.getInstance().chatManager().allConversations.isEmpty()) {
            fetchConversationsFromServer()
        } else {
            getConversationFromCache()
        }

        checkUnreadMsg()
        loadFriendRequestMessages()
    }

    val conversationInfoObservable = SingleSourceLiveData<Resource<List<EaseConversationInfo>>>()
    fun fetchConversationsFromServer() {  // get conversations from server
        val mRepository = EMChatManagerRepository()
        conversationInfoObservable.setSource(mRepository.fetchConversationsFromServer())
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

    val homeUnReadObservable = MutableLiveData<String>()

    val inviteMessageDao: InviteMessageDao? by lazy {
        DemoDbHelper.getInstance(BaseApp.instance).inviteMessageDao
    }

    fun checkUnreadMsg() {
        viewModelScope.launch {
            var unreadCount = 0
            inviteMessageDao?.also {
                unreadCount = it.queryUnreadCount()
            }
            val unreadMessageCount = DemoHelper.getInstance().chatManager.unreadMessageCount
            val count = getUnreadCount(unreadCount + unreadMessageCount)
            homeUnReadObservable.postValue(count.orEmpty())
        }
    }

    private fun getUnreadCount(count: Int): String? {
        if (count <= 0) {
            return null
        }
        return if (count > 99) {
            "99+"
        } else count.toString()
    }

    val inviteMsgObservable =
        SingleSourceLiveData<List<EMMessage>>()

    fun loadFriendRequestMessages() {
        viewModelScope.launch {
            val emMessages = EMClient.getInstance().chatManager().searchMsgFromDB(
                EMMessage.Type.TXT,
                System.currentTimeMillis(),
                1000,
                EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID,
                EMConversation.EMSearchDirection.UP
            ).sortedBy {
                it.msgTime
            }.filter {
                val statusParam = it.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS)
                val status = InviteMessageStatus.valueOf(statusParam)
                status == InviteMessageStatus.BEINVITEED
            }.orEmpty()
            inviteMsgObservable.setSource(MutableLiveData(emMessages))
        }
    }
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