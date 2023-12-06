package com.cyberflow.base.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


// for interact with IM
@Serializable
data class IMUserInfoList(
    var user_info_list: List<IMUserInfo>? = null
)

@Serializable
data class IMUserSearchList(var total: Int = 0, var list: List<IMSearchData>? = null)

data class IMMyFriendsList(var total: Int = 0, var list: List<IMSearchData>? = null)
data class IMSearchFriendHead(var name: String = "", var type: Int = 0, var showMore: Boolean = false)

const val TYPE_MY_FRIENDS = 0
const val TYPE_ADD_FRIENDS = 1

@Serializable
@Entity(tableName = "im_user_info_cache")
data class IMUserInfo(
    @PrimaryKey var open_uid: String,
    var gender: Int = 0, // 1=man  2=women
    var nick: String = "",
    var avatar: String = "",
    var wallet_address: String = "",
    var ca_wallet: String = "",
    var signature: String = "",
    var feed_avatar: String = "",
    var feed_card_color: String = "",
)

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
    var from_open_uid: String = "",
//    var gender: Int = 0, // 1=man  2=women
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
    var open_uid: String = "",
//    var gender: Int = 0, // 1=man  2=women
    var avatar: String = "",
    var nick: String = "",
)