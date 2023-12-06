package com.cyberflow.sparkle.im.viewmodel

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cyberflow.base.model.IMFriendList
import com.cyberflow.base.model.IMFriendRequestList
import com.cyberflow.base.model.IMUserInfoList
import com.cyberflow.base.model.IMUserSearchList
import com.cyberflow.base.net.Api
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.base.viewmodel.BaseViewModel
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.hyphenate.chat.EMClient
import com.hyphenate.easeui.constants.EaseConstant
import com.hyphenate.easeui.domain.EaseUser
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
        acceptFriendObservable.value = Post<String>(Api.RELATIONSHIP_FRIEND_ACCEPT) {
            json("open_uid" to openUid)
        }.await()
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

    /********************************** old **********************************************************/


    var imUserListData: MutableLiveData<IMUserSearchList> = MutableLiveData()

    val mContactRepository = EMContactManagerRepository()


    var imNewFriendListData: MutableLiveData<IMUserInfoList> = MutableLiveData()

    fun getIMNewFriendInfoList(openUidList: List<String>?) = scopeNet {
        imNewFriendListData.value = Post<IMUserInfoList>(Api.IM_BATCH_USER_INFO) {
            json("scene" to "0", "open_uid_list" to openUidList)
        }.await()
    }

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