package com.cyberflow.sparkle.im.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.BaseApp
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository
import com.cyberflow.sparkle.im.db.IMUserSearchList
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.manager.EaseSystemMsgManager
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.utils.EaseCommonUtils
import kotlinx.coroutines.launch

class IMViewModel : BaseViewModel() {

    var imUserListData: MutableLiveData<IMUserSearchList> = MutableLiveData()

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

    val contactObservable = SingleSourceLiveData<Resource<List<EaseUser>>>()
    fun loadContactList(server: Boolean) {
        contactObservable.setSource(mRepository.getContactList(server));
    }

    val userInfoObservable = SingleSourceLiveData<Resource<EaseUser>>()
    val mRepository = EMContactManagerRepository()

    val inviteMsgObservable = SingleSourceLiveData<List<EMMessage>>()

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
            }
            inviteMsgObservable.setSource(MutableLiveData(emMessages))
        }
    }

    val acceptFriendObservable = SingleSourceLiveData<String>()
    fun acceptFriend(msg : EMMessage?){
        if(msg==null) {
            acceptFriendObservable.postValue("")
            return
        }
        viewModelScope.launch {
            val name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
            EMClient.getInstance().contactManager().acceptInvitation(name)
            val message = BaseApp.instance?.getString(com.cyberflow.sparkle.chat.R.string.demo_system_agree_invite, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM))
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name)
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message)
            val body = EMTextMessageBody(message)
            msg.body = body
            EaseSystemMsgManager.getInstance().updateMessage(msg)
            acceptFriendObservable.postValue(name)
            LiveDataBus.get().with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent())
        }
    }

    val deleteMsgObservable = SingleSourceLiveData<Boolean>()
    fun deleteMessage(msgId : String?){
        viewModelScope.launch {
            EMClient.getInstance().chatManager().getConversation(DemoConstant.DEFAULT_SYSTEM_MESSAGE_ID, EMConversation.EMConversationType.Chat, true).removeMessage(msgId)
            deleteMsgObservable.postValue(true)
            LiveDataBus.get().with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent())
        }
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
}