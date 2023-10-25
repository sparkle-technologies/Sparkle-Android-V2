package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface IMUserInfoDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg imUserInfo: IMUserInfo)

    @Delete
    suspend fun delete(vararg imUserInfo: IMUserInfo)

    @Query("delete from im_user_info_cache")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg imUserInfo: IMUserInfo)

    @Query("select * from im_user_info_cache")
    suspend fun getAll(): List<IMUserInfo>
}