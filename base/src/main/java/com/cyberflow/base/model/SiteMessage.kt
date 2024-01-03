package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class SiteMessageList(
    var list: List<SiteMessage>? = null,
    var total: Int = 0
)

@Serializable
data class SiteMessage (
    var message_type: Int = 0,
    var `object`: SMFriendRequest? = null,
    var text: String = "",
    var have_read: Boolean = false,
    var timestamp: Long = 0
)

@Serializable
data class SMFriendRequest(
    var nick: String = "",
    var avatar: String = "",
    var open_uid: String = ""
)