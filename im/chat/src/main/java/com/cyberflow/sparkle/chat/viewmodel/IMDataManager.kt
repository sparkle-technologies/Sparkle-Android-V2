package com.cyberflow.sparkle.chat.viewmodel

import android.net.Uri
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendRequest
import com.cyberflow.base.model.IMUserInfo
import com.cyberflow.base.model.User
import com.hyphenate.chat.EMMessage


class IMDataManager private constructor() {

    companion object {
        val instance: IMDataManager by lazy { IMDataManager() }
        const val TAG = "IMDataManager"
    }

    private var inviteCacheData : List<IMFriendRequest>?  =  null
    private var contactCacheData : List<IMFriendInfo>?  =  null
    private var conversationCacheData : List<IMUserInfo>?  =  null   // for share page & forwrd page

    fun getInviteData(): List<IMFriendRequest> {
        return inviteCacheData.orEmpty()
    }

    fun setInviteData(list: List<IMFriendRequest>?) {
        inviteCacheData = list
    }

    fun getContactData(): List<IMFriendInfo> {
        return contactCacheData.orEmpty()
    }

    fun setContactData(data: List<IMFriendInfo>?) {
        contactCacheData = data
    }

    fun setConversationData(data: List<IMUserInfo>?) {
        conversationCacheData = data
    }

    fun getConversationData(): List<IMUserInfo> {
        return conversationCacheData.orEmpty()
    }

    // for profile page
    private var openUidProfile: String? = null
    fun getOpenUidProfile(): String? {
        return openUidProfile
    }
    fun setOpenUidProfile(openUidProfile: String?) {
        this.openUidProfile = openUidProfile
    }

    private var forwardMsg: EMMessage? = null   // for forward page
    fun getForwardMsg(): EMMessage? {
        return forwardMsg
    }
    fun setForwardMsg(forwardMsg: EMMessage?) {
        this.forwardMsg = forwardMsg
    }

    private var forwardImageUri : Uri? = null   // not forward, share image to contact
    fun getForwardImageUri(): Uri? {
        return forwardImageUri
    }
    fun setForwardImageUri(forwardImageUri: Uri?) {
        this.forwardImageUri = forwardImageUri
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
        openUidProfile = null
        forwardMsg = null
        forwardImageUri = null
        user = null
    }
}
