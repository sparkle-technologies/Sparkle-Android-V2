package com.cyberflow.sparkle.im.db

import androidx.room.Database
import com.cyberflow.base.model.DBHoroscope
import com.cyberflow.base.model.IMConversationCache
import com.cyberflow.base.model.IMFriendInfo
import com.cyberflow.base.model.IMFriendRequest

@Database(entities = [IMFriendRequest::class, IMFriendInfo::class, IMConversationCache::class, DBHoroscope::class], version = 2)
abstract class IMUserInfoDatabase : androidx.room.RoomDatabase() {
    abstract fun imFriendRequestDao(): IMFriendRequestDao
    abstract fun imFriendInfoDao(): IMFriendInfoDao
    abstract fun imConversationCacheDao(): IMConversationCacheDao
    abstract fun horoscopeCacheDao(): HoroscopeCacheDao
}