package com.cyberflow.base.model

data class RegisterRequestBean(
    var birth_time: String = "",
    var birthdate: String = "",
    var birthplace_info: BirthplaceInfo? = null ,
    var gender: Int = 0,    // 1：Man 2：Women
    var location_info: LocationInfo? = null,
    var nick: String = "",
    var profile_permission: Int = 1,  // default = 1-everyone 2-everyone in sparkle 3-only friend 4-only me
    var signature: String = ""
)