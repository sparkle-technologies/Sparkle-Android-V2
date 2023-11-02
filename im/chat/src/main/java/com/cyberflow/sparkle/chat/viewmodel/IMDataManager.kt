package com.cyberflow.sparkle.chat.viewmodel

import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.model.User
import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.domain.EaseUser


class IMDataManager private constructor() {

    companion object {
        val instance: IMDataManager by lazy { IMDataManager() }
        const val TAG = "IMDataManager"
    }

    private var inviteCacheData : List<EMMessage>?  =  null
    private var contactCacheData : List<EaseUser>?  =  null
    private var conversationCacheData : List<IMUserInfo>?  =  null   //for share page

    fun getInviteData(): List<EMMessage> {
        return inviteCacheData.orEmpty()
    }

    fun getContactData(): List<EaseUser> {
        return contactCacheData.orEmpty()
    }

    fun setInviteData(list: List<EMMessage>?) {
        inviteCacheData = list
    }

    fun setContactData(data: List<EaseUser>?) {
        contactCacheData = data
    }

    fun setConversationData(data: List<IMUserInfo>?) {
        conversationCacheData = data
    }

    fun getConversationData(): List<IMUserInfo> {
        return conversationCacheData.orEmpty()
    }


    // for profile page
    private var emMessage: EMMessage? = null
    fun getEmMessage(): EMMessage? {
        return emMessage
    }
    fun setEmMessage(emMessage: EMMessage?) {
        this.emMessage = emMessage
    }

    private var user: User? = null
    fun getUser(): User? {
        return user
    }
    fun setUser(user: User?) {
        this.user = user
    }

    fun clearCache(){
        inviteCacheData = null
        contactCacheData = null
        conversationCacheData = null
        emMessage = null
        user = null
    }
}
