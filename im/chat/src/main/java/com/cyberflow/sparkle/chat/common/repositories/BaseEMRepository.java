package com.cyberflow.sparkle.chat.common.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cyberflow.base.BaseApp;
import com.cyberflow.sparkle.chat.DemoHelper;
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper;
import com.cyberflow.sparkle.chat.common.db.dao.EmUserDao;
import com.cyberflow.sparkle.chat.common.db.dao.InviteMessageDao;
import com.cyberflow.sparkle.chat.common.db.dao.MsgTypeManageDao;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMPushManager;
import com.hyphenate.easeui.manager.EaseThreadManager;

public class BaseEMRepository {

    /**
     * return a new liveData
     *
     * @param item
     * @param <T>
     * @return
     */
    public <T> LiveData<T> createLiveData(T item) {
        return new MutableLiveData<>(item);
    }

    /**
     * login before
     *
     * @return
     */
    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    /**
     * 获取本地标记，是否自动登录
     *
     * @return
     */
    public boolean isAutoLogin() {
        return DemoHelper.getInstance().getAutoLogin();
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    public String getCurrentUser() {
        return DemoHelper.getInstance().getCurrentUser();
    }

    /**
     * EMChatManager
     *
     * @return
     */
    public EMChatManager getChatManager() {
        return DemoHelper.getInstance().getEMClient().chatManager();
    }

    /**
     * EMContactManager
     *
     * @return
     */
    public EMContactManager getContactManager() {
        return DemoHelper.getInstance().getContactManager();
    }

    /**
     * EMGroupManager
     *
     * @return
     */
    public EMGroupManager getGroupManager() {
        return DemoHelper.getInstance().getEMClient().groupManager();
    }


    /**
     * EMPushManager
     *
     * @return
     */
    public EMPushManager getPushManager() {
        return DemoHelper.getInstance().getPushManager();
    }

    /**
     * init room
     */
    public void initDb() {
        DemoDbHelper.getInstance(BaseApp.getInstance()).initDb(getCurrentUser());
    }

    /**
     * EmUserDao
     *
     * @return
     */
    public EmUserDao getUserDao() {
        return DemoDbHelper.getInstance(BaseApp.getInstance()).getUserDao();
    }

    /**
     * get MsgTypeManageDao
     *
     * @return
     */
    public MsgTypeManageDao getMsgTypeManageDao() {
        return DemoDbHelper.getInstance(BaseApp.getInstance()).getMsgTypeManageDao();
    }

    /**
     * get invite message dao
     *
     * @return
     */
    public InviteMessageDao getInviteMessageDao() {
        return DemoDbHelper.getInstance(BaseApp.getInstance()).getInviteMessageDao();
    }

    /**
     * 在主线程执行
     *
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * 在异步线程
     *
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnIOThread(runnable);
    }

    public Context getContext() {
        return BaseApp.getInstance().getApplicationContext();
    }

}
