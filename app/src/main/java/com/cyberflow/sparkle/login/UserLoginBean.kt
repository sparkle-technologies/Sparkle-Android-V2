package com.cyberflow.sparkle.login

data class UserLoginBean(
    val id_token: String,
    val invisible_image_url: String,
    val invisible_seq: List<Int>,
    val token: String,
    val user: User
)

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

data class LocationInfo(
    val latitude: Int,
    val location: String,
    val longitude: Int
)

data class BirthplaceInfo(
    val latitude: Int,
    val location: String,
    val longitude: Int
)