package com.cyberflow.sparkle.im.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyberflow.base.model.Compatibility

@Dao
interface CompatibilityCacheDao{

    @Query("select * from cache_compatibility_request where requestKey = :requestKey")
    suspend fun fetch(requestKey: String): Compatibility

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg data: Compatibility)

    @Query("delete from cache_compatibility_request")
    suspend fun deleteAll()

    @Query("select * from cache_compatibility_request")
    suspend fun getAll(): List<Compatibility>
}