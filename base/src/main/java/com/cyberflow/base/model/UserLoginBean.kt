package com.cyberflow.base.model

data class UserLoginBean(
    val id_token: String,
    val invisible_image_url: String,
    val invisible_seq: List<Int>,
    val token: String,
    val user: User
)


