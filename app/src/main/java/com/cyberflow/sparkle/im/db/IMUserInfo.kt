package com.cyberflow.sparkle.im.db

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
    var avatar: String = "",
    var ca_wallet: String = "",
    var created_at: String = "",
    var nick: String = "",
    var open_uid: String = "",
    var task_completed: Boolean = false,
    var updated_at: String = "",
    var wallet_address: String = ""
) : java.io.Serializable