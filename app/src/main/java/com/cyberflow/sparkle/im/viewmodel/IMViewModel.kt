package com.cyberflow.sparkle.im.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendList
import com.cyberflow.base.model.IMFriendRequest
import com.cyberflow.base.model.IMFriendRequestList
import com.cyberflow.base.model.IMUserSearchList
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.bus.LiveDataBus
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.constant.DemoConstant
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository
import com.cyberflow.sparkle.im.DBManager
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseUser
import com.hyphenate.easeui.model.EaseEvent
import com.hyphenate.easeui.modules.conversation.model.EaseConversationInfo
import com.hyphenate.easeui.utils.EaseCommonUtils
import kotlinx.coroutines.launch

class IMViewModel : BaseViewModel() {


    var inviteMsgObservable: MutableLiveData<IMFriendRequestList> = MutableLiveData()
    fun loadFriendRequestMessages() = scopeNet {
        inviteMsgObservable.value = Post<IMFriendRequestList>(Api.RELATIONSHIP_FRIEND_REQUEST_LIST) {}.await()
    }

    var acceptFriendObservable: MutableLiveData<String> = MutableLiveData()
    fun acceptFriend(openUid : String?) = scopeNet {
        Post<String>(Api.RELATIONSHIP_FRIEND_ACCEPT) {
            json("open_uid" to openUid)
        }.await()
        acceptFriendObservable.value = openUid.orEmpty()
    }

    var deleteMsgObservable: MutableLiveData<String> = MutableLiveData()
    fun deleteMessage(openUid : String?) = scopeNet {
        deleteMsgObservable.value = Post<String>(Api.RELATIONSHIP_FRIEND_REJECT) {
            json("open_uid" to openUid)
        }.await()
    }

    var contactObservable: MutableLiveData<IMFriendList> = MutableLiveData()
    fun loadContactList()  = scopeNet {
        contactObservable.value = Post<IMFriendList>(Api.RELATIONSHIP_FRIEND_LIST) {}.await()
    }


    // same as com.cyberflow.sparkle.main.viewmodel.MainViewModel.saveInviteData2DB
    fun saveInviteData2DB(data: List<IMFriendRequest>?) {
        viewModelScope.launch {
            DBManager.instance.db?.imFriendRequestDao()?.deleteAll()
            if(data.isNullOrEmpty()){
            }else{
                DBManager.instance.db?.imFriendRequestDao()?.insert(*data.toTypedArray())
            }
        }
    }

    // same as com.cyberflow.sparkle.main.viewmodel.MainViewModel.saveContactData2DB
    fun saveContactData2DB(data: List<IMFriendInfo>?) {
        viewModelScope.launch {
            DBManager.instance.db?.imFriendInfoDao()?.deleteAll()
            if(data.isNullOrEmpty()){
            }else{
                DBManager.instance.db?.imFriendInfoDao()?.insert(*data.toTypedArray())
            }
        }
    }

    /********************************** old **********************************************************/

    fun IM_addFriend(username: String, reason: String) {
        Log.e("TAG", "addFriend: username=$username reason=$reason")
        viewModelScope.launch {
            mContactRepository?.addContact(username, reason)   // todo  it will be replaced by our server
        }
    }

    fun IM_acceptFriend(fromUid : String?){
        Log.e("TAG", "acceptFriend: fromUid=$fromUid")
        viewModelScope.launch {
            mContactRepository?.acceptInvitation(fromUid?.replace("-", "_"))  // todo  it will be replaced by our server
            LiveDataBus.get().with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent())
        }
    }


    var imUserListData: MutableLiveData<IMUserSearchList> = MutableLiveData()

    val mContactRepository = EMContactManagerRepository()


    val userInfoObservable = SingleSourceLiveData<Resource<EaseUser>>()
    val mRepository = EMContactManagerRepository()

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