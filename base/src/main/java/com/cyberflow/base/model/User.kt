package com.cyberflow.base.model

import kotlinx.serialization.Serializable



@Serializable
data class User(
    var avatar: String = "",
    var bind_list: List<BindBean>? = null ,
    var birth_time: String = "",
    var birthdate: String = "",
    var birthplace_info: LocationInfo? = null ,
    var ca_wallet: String = "",
    var code: String = "",
    var default_nft_list: List<NftItem>? = null ,
    var gender: Int = 0,
    var label_list: List<String>? = null ,
    var label_list_v2: List<String>? = null ,
    var location_info: LocationInfo? = null ,
    var nft_list: List<NftItem>? = null ,
    var nick: String = "",
    var open_uid: String = "",
    var profile_permission: Int = 0,
    var signature: String = "",
    var star_sign: List<StartSignItem>? = null ,
    var task_completed: Boolean = false,
    var wallet_address: String = ""
){
    fun getIMAccount() :String = open_uid.replace("-", "_")
}

@Serializable
data class BindBean(
    var type: String = "",
    var nick: String = "",
)


@Serializable
data class NftItem(
    var url: String = "",
    var sequence: Int = 0,
)

@Serializable
data class StartSignItem(
    var PlanetEnglish: String = "",
    var SignEnglish: String = "",
    var Desc: String = "",
    var Labels: List<String>,
)