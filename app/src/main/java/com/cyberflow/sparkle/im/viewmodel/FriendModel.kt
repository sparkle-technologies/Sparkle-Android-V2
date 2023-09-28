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
    var name: String = "Alice",
    var msg: String = "Hi, glad to see you... ",
    var status: Int = 0,   // 0: normal, 1: added ,2: rejected
    var txt: String = "",
    val url: String? = "https://image.api.playstation.com/pr/bam-art/129/926/57378d6b-29b7-4d35-81e9-9be992d8441f.jpg?thumb=true",
    val emMessage: EMMessage? = null,
    override var itemPosition: Int = 0
) : ItemPosition

data class ContactListHeader(override var itemHover: Boolean = true) : ItemHover

@Serializable
data class ContactList(var list: List<Any> = listOf())

@Serializable
data class Contact(
    var name: String = "Alice",
    var txt: String = "",
    val url: String? = "https://image.api.playstation.com/pr/bam-art/129/926/57378d6b-29b7-4d35-81e9-9be992d8441f.jpg?thumb=true",
    val last : Boolean = false
)

data class ContactLetter(val letter: String, override var itemPosition: Int = 0) : ItemPosition


