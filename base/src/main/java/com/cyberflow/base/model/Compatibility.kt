package com.cyberflow.base.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(tableName = "cache_compatibility_request")
@Serializable
data class Compatibility(
    @PrimaryKey
    val requestKey: String,
    val data: String
)

@Serializable
data class CompatibilityItem(
    val constellation_a: String,
    val constellation_b: String,
    val content: String
)