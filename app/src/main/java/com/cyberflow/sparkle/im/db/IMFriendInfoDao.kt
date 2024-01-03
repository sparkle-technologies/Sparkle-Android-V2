package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberflow.base.model.IMFriendInfo

@Dao
interface IMFriendInfoDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: IMFriendInfo)

    @Delete
    suspend fun delete(vararg data: IMFriendInfo)

    @Query("delete from im_friend_info")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg data: IMFriendInfo)

    @Query("select * from im_friend_info")
    suspend fun getAll(): List<IMFriendInfo>
}