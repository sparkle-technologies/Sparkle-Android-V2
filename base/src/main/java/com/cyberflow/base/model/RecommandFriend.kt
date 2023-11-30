package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommandFriendList(var friends: List<RecommandFriend>? = null )

@Serializable
data class RecommandFriend(
    var open_uid: String? = "",
    var avatar: String? = "",
    var nick: String? = "",
)


@Serializable
data class BondList(var bond_list: List<BondItem>? = null )

@Serializable
data class BondItem(
    var id: String? = "",
    var avatar: String? = "",
    var nick: String? = "",
    var sun_sign: String? = "",
    var score: Int = 0,
    var ranking: Int = 0,
)

@Serializable
data class BondDetail(
    val aspect_info_list: List<AspectInfo>,
    val bond_score: Int,
    val commitment_score: Int,
    val from_avatar: String,
    val from_ca: String,
    val from_nick: String,
    val from_open_uid: String,
    val from_sun_sign: String,
    val from_ticket: Int,
    val fun_score: Int,
    val intimacy_score: Int,
    val rapport_score: Int,
    val spark_score: Int,
    val to_avatar: String,
    val to_ca: String,
    val to_nick: String,
    val to_open_uid: String,
    val to_sun_sign: String,
    val to_ticket: Int,
    val total_score: Int
)

@Serializable
data class AspectInfo(
    val aspect: String,
    val plant_in: String,
    val plant_out: String,
    val progress: String
)
