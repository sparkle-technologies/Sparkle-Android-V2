package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class ManyImageData(
    var image_list: ImageList? = null ,
    var original_h5: String = "",
    var updated_at: Int = 0
)