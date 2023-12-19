package com.cyberflow.base.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.cyberflow.base.net.GsonConverter
import kotlinx.serialization.Serializable
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Serializable
data class DailyHoroScopeData(
    val total_score: Int,
    val love_score: Int,
    var love_progress_list: List<HoroProgress>?,
    val wealth_score: Int,
    var wealth_progress_list: List<HoroProgress>?,
    val career_score: Int,
    var career_progress_list: List<HoroProgress>?
)

@Serializable
data class HoroProgress(
    val content: String,
    val title: String,
)

@Serializable
data class YearlyHoroScopeData(
    val progress_content: String,
)

@Entity(tableName = "cache_horoscope_request")
//@TypeConverters(MyConverters::class)
@Serializable
data class DBHoroscope(
    @PrimaryKey
    val requestKey: String,
    val data: String
)

// room数据库 复杂字段转换器  https://www.cnblogs.com/mymy-android/p/14943884.html
class MyConverters {
    @TypeConverter
    fun progressToStr(progress: MutableList<HoroProgress>? = null): String? {
        progress ?: return null
        return GsonConverter.gson.toJson(progress)
    }

    @TypeConverter
    fun strToProgress(str: String?): MutableList<HoroProgress>? {
        str ?: return null
        return GsonConverter.gson.fromJson(str, ParameterizedTypeImpl(HoroProgress::class.java))
    }
}

//ParameterizedType
class ParameterizedTypeImpl(private val clz: Class<*>) : ParameterizedType {
    override fun getRawType(): Type = List::class.java
    override fun getOwnerType(): Type? = null
    override fun getActualTypeArguments(): Array<Type> = arrayOf(clz)
}
