package com.cyberflow.sparkle.im

import android.content.Context
import androidx.room.Room
import com.cyberflow.sparkle.im.db.IMUserInfoDatabase


class DBManager private constructor(){

    companion object {
        const val TAG = "DBManager"
        val instance: DBManager by lazy { DBManager() }
    }

    var db: IMUserInfoDatabase? = null

    fun initDB(context: Context) {
        db = Room.databaseBuilder(context, IMUserInfoDatabase::class.java, "IMDB")
            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
    }

    fun closeDB(){
        db?.close()
    }
}
