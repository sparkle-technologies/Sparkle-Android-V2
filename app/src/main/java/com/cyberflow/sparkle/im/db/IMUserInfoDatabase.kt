package com.cyberflow.sparkle.im.db

import androidx.room.Database

@Database(entities = [IMUserInfo::class], version = 1)
abstract class IMUserInfoDatabase : androidx.room.RoomDatabase() {
    abstract fun imUserInfoDao(): IMUserInfoDao
}