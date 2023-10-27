package com.cyberflow.sparkle.im.viewmodel

import com.drake.brv.item.ItemHover
import com.drake.brv.item.ItemPosition
import com.hyphenate.chat.EMMessage
import kotlinx.serialization.Serializable


data class FriendRequestEmpty(override var itemPosition: Int = 0) : ItemPosition
data class FriendRequestHeader(var count: Int = 0, override var itemHover: Boolean = true) :
    ItemHover

data class FriendRequestList(var initial: String = "", var list: List<FriendRequest> = listOf())
data class FriendRequest(
    var name: String = "",
    var msg: String = "",
    var status: Int = 0,   // 0: normal, 1: added ,2: rejected
    var gender: Int = 0,
    var url: String = "",
    val emMessage: EMMessage? = null,
    override var itemPosition: Int = 0
) : ItemPosition{
    fun freshTxt() : String {
        return if (status == 1) "Accepted" else "Refused"
    }
}

const val STATUS_NORMAL = 0
const val STATUS_ADDED = 1
const val STATUS_REJECTED = 2

data class ContactListHeader(override var itemHover: Boolean = true) : ItemHover

@Serializable
data class RecentContactList(var list: List<Contact> = listOf())
data class SearchContactList(var list: List<Contact> = listOf())

data class ContactList(var list: List<Any> = listOf())

@Serializable
data class Contact(
    var name: String = "",
    var txt: String = "",
    val avatar: String = "",
    val gender: Int = 0,
    val url: String = "",
    val openUid: String = "",
    var last : Boolean = false
)

data class ContactLetter(val letter: String, override var itemPosition: Int = 0) : ItemPosition


