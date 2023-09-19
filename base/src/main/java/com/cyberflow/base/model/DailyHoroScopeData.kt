package com.cyberflow.base.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyHoroScopeData(
    val total_score: Int,
    val love_score: Int,
    val love_progress_list: List<HoroProgress>?,
    val wealth_score: Int,
    val wealth_progress_list: List<HoroProgress>?,
    val career_score: Int,
    val career_progress_list: List<HoroProgress>?
)

@Serializable
data class HoroProgress(
    val content: String,
    val title: String,
    val star_time: Long,
    val end_time: Long,
)