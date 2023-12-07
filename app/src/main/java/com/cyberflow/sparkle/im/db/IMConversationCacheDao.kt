package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyberflow.base.model.IMConversationCache

@Dao
interface IMConversationCacheDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: IMConversationCache)

    @Delete
    suspend fun delete(vararg data: IMConversationCache)

    @Query("delete from im_conversation_cache")
    suspend fun deleteAll()

    @Update
    suspend fun update(vararg data: IMConversationCache)

    @Query("select * from im_conversation_cache")
    suspend fun getAll(): List<IMConversationCache>
}