package com.cyberflow.sparkle.chat.viewmodel

import com.hyphenate.chat.EMMessage
import com.hyphenate.easeui.domain.EaseUser


class IMDataManager private constructor() {

    companion object {
        val instance: IMDataManager by lazy { IMDataManager() }
        const val TAG = "IMDataManager"
    }

    private var inviteCacheData : List<EMMessage>?  =  null
    private var contactCacheData : List<EaseUser>?  =  null

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

    fun clearCache(){
        inviteCacheData = null
        contactCacheData = null
    }
}
