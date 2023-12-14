package com.cyberflow.base.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Serializable
data class IMUserSearchList(var total: Int = 0, var list: List<IMSearchData>? = null)

data class IMMyFriendsList(var total: Int = 0, var list: List<IMSearchData>? = null)
data class IMSearchFriendHead(var name: String = "", var type: Int = 0, var showMore: Boolean = false)

@Serializable
data class IMSearchData(
    var nick: String = "",
    var gender: Int = 0,  // 1=man  2=women
    var avatar: String = "",
    var ca_wallet: String = "",
    var open_uid: String = "",
    var wallet_address: String = ""
) : java.io.Serializable


@Serializable
data class IMFriendRequestList(
    var friend_req_list: List<IMFriendRequest>? = null
)

@Serializable
@Entity(tableName = "im_friend_request")
data class IMFriendRequest(
    @PrimaryKey
    var from_open_uid: String = "",
    var avatar_style: Int = 0, // 1=man  2=women
    var avatar: String = "",
    var nick: String = "",
    var req_msg: String = ""
)


@Serializable
data class IMFriendList(
    var friend_list: List<IMFriendInfo>? = null
)

@Serializable
@Entity(tableName = "im_friend_info")
data class IMFriendInfo(
    @PrimaryKey
    var open_uid: String = "",
    var nick: String = "",
    var avatar_style: Int = 1, // 1=man  2=women
    var avatar: String = "",
    var feed_avatar: String = "",
    var feed_card_color: String = "#ffffff",
)

@Serializable
@Entity(tableName = "im_conversation_cache")
data class IMConversationCache(
    @PrimaryKey
    var open_uid: String,
    var nick: String = "",
    var gender: Int = 0,
    var avatar: String = "",
    var feed_avatar: String = "",
    var num: Int = 1,
    var bgColor: String = ""
)

@Serializable
data class IMQuestionList(
    var questions: List<String>? = null
)

@Serializable
data class AIOResult(
    val result: String,
    val tarotCards: List<TarotCard>
)

@Serializable
data class TarotCard(
    val imgUrl: String,
    val name: String,
    val uprightOrReversed: String
)