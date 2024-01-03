package com.cyberflow.sparkle.chat.viewmodel

import android.net.Uri
import com.cyberflow.base.model.User
import com.hyphenate.chat.EMMessage


class IMDataManager private constructor() {

    companion object {
        val instance: IMDataManager by lazy { IMDataManager() }
        const val TAG = "IMDataManager"
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

    private var shareMsg: EMMessage? = null
    fun getShareMsg(): EMMessage? {
        return shareMsg
    }
    fun setShareMsg(shareMsg: EMMessage?) {
        this.shareMsg = shareMsg
    }

    fun clearCache(){
        openUidProfile = null
        forwardMsg = null
        forwardImageUri = null
        user = null
        shareMsg = null
    }
}
