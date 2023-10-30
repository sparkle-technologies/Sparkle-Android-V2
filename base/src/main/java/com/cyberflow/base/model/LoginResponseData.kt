package com.cyberflow.base.model

import kotlinx.serialization.Serializable


@Serializable
data class LoginResponseData(
    var token: String = "",
    var id_token: String = "",
    var im_token: String = "",
    val user: User?,
    var invisible_image_url: String = "",
    var invisible_seq: List<Int>?
)


@Serializable
data class DetailResponseData(
    val user: User?
)