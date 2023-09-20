package com.cyberflow.base.model

import kotlinx.serialization.Serializable


@Serializable
data class LoginResponseBean(
    val id_token: String,
    val invisible_image_url: String,
    val invisible_seq: List<Int>,
    val token: String,
    val user: User
)


