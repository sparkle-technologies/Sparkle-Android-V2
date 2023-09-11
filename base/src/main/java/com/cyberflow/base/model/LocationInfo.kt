package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationInfo(
    var latitude: Double = 0.0,
    var location: String = "",
    var longitude: Double = 0.0
)