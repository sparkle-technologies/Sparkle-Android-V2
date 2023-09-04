package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationInfo(
    var latitude: Int = 0,
    var location: String = "",
    var longitude: Int = 0
)