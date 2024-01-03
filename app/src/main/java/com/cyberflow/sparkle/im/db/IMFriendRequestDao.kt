package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberflow.base.model.IMFriendRequest

@Dao
interface IMFriendRequestDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: IMFriendRequest)

    @Delete
    suspend fun delete(vararg data: IMFriendRequest)

    @Query("delete from im_friend_request")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg data: IMFriendRequest)

    @Query("select * from im_friend_request")
    suspend fun getAll(): List<IMFriendRequest>
}