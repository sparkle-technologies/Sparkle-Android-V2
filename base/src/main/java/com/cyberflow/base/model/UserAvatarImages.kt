package com.cyberflow.base.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserAvatarImages(
    val original_h5: String? = null,
    val original_native: String,
    val image_list: ImageList
) : Parcelable

@Parcelize
data class ImageList(
    val native_head: String?,
    val avatar: String? = null,
    val head: String? = null
) : Parcelable

