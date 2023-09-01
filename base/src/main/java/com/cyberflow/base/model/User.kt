package com.cyberflow.base.model

data class User(
    val avatar: String,
    val bind_list: Any,
    val birth_time: String,
    val birthdate: String,
    val birthplace_info: BirthplaceInfo,
    val ca_wallet: String,
    val code: String,
    val default_nft_list: Any,
    val gender: Int,
    val label_list: Any,
    val label_list_v2: Any,
    val location_info: LocationInfo,
    val nft_list: Any,
    val nick: String,
    val open_uid: String,
    val signature: String,
    val star_sign: Any,
    val task_completed: Boolean,
    val wallet_address: String
)