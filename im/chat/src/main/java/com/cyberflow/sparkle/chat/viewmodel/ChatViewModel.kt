package com.cyberflow.sparkle.chat.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import com.cyberflow.base.util.bus.SingleSourceLiveData
import com.cyberflow.sparkle.chat.common.net.Resource
import com.cyberflow.sparkle.chat.common.repositories.EMChatManagerRepository
import com.hyphenate.chat.EMChatRoom

class ChatViewModel : ConversationListViewModel() {

    private var chatManagerRepository: EMChatManagerRepository? = null
    private var chatRoomObservable: SingleSourceLiveData<Resource<EMChatRoom?>?>? = null
    private var makeConversationReadObservable: SingleSourceLiveData<Resource<Boolean?>?>? = null
    private var getNoPushUsersObservable: SingleSourceLiveData<Resource<List<String?>?>?>? = null
    private var setNoPushUsersObservable: SingleSourceLiveData<Resource<Boolean?>?>? = null

    fun ChatViewModel(application: Application) {
        chatManagerRepository = EMChatManagerRepository()
        chatRoomObservable =
            SingleSourceLiveData()
        makeConversationReadObservable =
            SingleSourceLiveData()
        getNoPushUsersObservable =
            SingleSourceLiveData()
        setNoPushUsersObservable =
            SingleSourceLiveData()
    }

    fun getChatRoomObservable(): LiveData<Resource<EMChatRoom?>?>? {
        return chatRoomObservable
    }

    fun getNoPushUsersObservable(): LiveData<Resource<List<String?>?>?>? {
        return getNoPushUsersObservable
    }

    fun setNoPushUsersObservable(): LiveData<Resource<Boolean?>?>? {
        return setNoPushUsersObservable
    }

    fun makeConversationReadByAck(conversationId: String?) {
        makeConversationReadObservable?.setSource(
            chatManagerRepository!!.makeConversationReadByAck(
                conversationId
            )
        )
    }

    /**
     * 设置单聊用户聊天免打扰
     *
     * @param userId 用户名
     * @param noPush 是否免打扰
     */
    fun setUserNotDisturb(userId: String?, noPush: Boolean) {
        setNoPushUsersObservable?.setSource(
            chatManagerRepository!!.setUserNotDisturb(
                userId,
                noPush
            )
        )
    }

    /**
     * 获取聊天免打扰用户
     */
    fun getNoPushUsers() {
        getNoPushUsersObservable?.setSource(chatManagerRepository!!.noPushUsers)
    }

    fun getMakeConversationReadObservable(): LiveData<Resource<Boolean?>?>? {
        return makeConversationReadObservable
    }


}
