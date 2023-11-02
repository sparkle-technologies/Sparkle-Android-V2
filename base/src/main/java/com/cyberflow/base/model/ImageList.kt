package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageList(
    var avatar: String = "",
    var avatar_native: String = "",
    var friends_feed_native: String = "",
    var profile_native: String = "",
    var sbt: String = ""
)