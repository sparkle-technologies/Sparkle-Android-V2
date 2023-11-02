package com.cyberflow.sparkle.chat.common.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.cyberflow.sparkle.chat.common.db.converter.DateConverter;
import com.cyberflow.sparkle.chat.common.db.dao.AppKeyDao;
import com.cyberflow.sparkle.chat.common.db.dao.EmUserDao;
import com.cyberflow.sparkle.chat.common.db.dao.InviteMessageDao;
import com.cyberflow.sparkle.chat.common.db.dao.MsgTypeManageDao;
import com.cyberflow.sparkle.chat.common.db.entity.AppKeyEntity;
import com.cyberflow.sparkle.chat.common.db.entity.EmUserEntity;
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessage;
import com.cyberflow.sparkle.chat.common.db.entity.MsgTypeManageEntity;

@Database(entities = {EmUserEntity.class,
        InviteMessage.class,
        MsgTypeManageEntity.class,
        AppKeyEntity.class},
        version = 17)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract EmUserDao userDao();

    public abstract InviteMessageDao inviteMessageDao();

    public abstract MsgTypeManageDao msgTypeManageDao();

    public abstract AppKeyDao appKeyDao();
}
