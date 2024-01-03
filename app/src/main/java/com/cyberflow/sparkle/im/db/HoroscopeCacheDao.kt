package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberflow.base.model.DBHoroscope

@Dao
interface HoroscopeCacheDao{

    @Query("select * from cache_horoscope_request where requestKey = :requestKey")
    suspend fun fetch(requestKey: String): DBHoroscope

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: DBHoroscope)

    @Delete
    suspend fun delete(vararg data: DBHoroscope)

    @Query("delete from cache_horoscope_request")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg data: DBHoroscope)

    @Query("select * from cache_horoscope_request")
    suspend fun getAll(): List<DBHoroscope>
}