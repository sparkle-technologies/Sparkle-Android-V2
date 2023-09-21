package com.cyberflow.sparkle.chat.common.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.cyberflow.base.BaseApp;
import com.cyberflow.base.util.bus.LiveDataBus;
import com.cyberflow.sparkle.chat.DemoHelper;
import com.cyberflow.sparkle.chat.R;
import com.cyberflow.sparkle.chat.common.constant.DemoConstant;
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper;
import com.cyberflow.sparkle.chat.common.db.dao.EmUserDao;
import com.cyberflow.sparkle.chat.common.db.entity.EmUserEntity;
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus;
import com.cyberflow.sparkle.chat.common.interfaceOrImplement.ResultCallBack;
import com.cyberflow.sparkle.chat.common.manager.PushAndMessageHelper;
import com.cyberflow.sparkle.chat.common.repositories.EMContactManagerRepository;
import com.cyberflow.sparkle.chat.common.repositories.EMPushManagerRepository;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMConversationListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMultiDeviceListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMUserInfo;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseAtMessageHelper;
import com.hyphenate.easeui.manager.EaseChatPresenter;
import com.hyphenate.easeui.manager.EaseSystemMsgManager;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 主要用于chat过程中的全局监听，并对相应的事件进行处理
 * {@link #init()}方法建议在登录成功以后进行调用
 */
public class ChatPresenter extends EaseChatPresenter {
    private static final String TAG = ChatPresenter.class.getSimpleName();
    private static final int HANDLER_SHOW_TOAST = 0;
    private static ChatPresenter instance;
    private LiveDataBus messageChangeLiveData;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    private boolean isPushConfigsWithServer = false;
    private Context appContext;
    protected Handler handler;
    private ChatConnectionListener connectionListener;
    private ChatMultiDeviceListener multiDeviceListener;
    private ChatContactListener contactListener;
    private ChatConversationListener conversationListener;

    Queue<String> msgQueue = new ConcurrentLinkedQueue<>();

    private ChatPresenter() {
        appContext = BaseApp.getInstance();
        initHandler(appContext.getMainLooper());
        messageChangeLiveData = LiveDataBus.get();
        connectionListener = new ChatConnectionListener();
        multiDeviceListener = new ChatMultiDeviceListener();
        contactListener = new ChatContactListener();
        conversationListener = new ChatConversationListener();
        //添加网络连接状态监听
        DemoHelper.getInstance().getEMClient().addConnectionListener(connectionListener);
        //添加多端登录监听
        DemoHelper.getInstance().getEMClient().addMultiDeviceListener(multiDeviceListener);
        //添加联系人监听
        DemoHelper.getInstance().getContactManager().setContactListener(contactListener);
        //添加对会话的监听（监听已读回执）
        DemoHelper.getInstance().getChatManager().addConversationListener(conversationListener);
    }

    public static ChatPresenter getInstance() {
        if (instance == null) {
            synchronized (ChatPresenter.class) {
                if (instance == null) {
                    instance = new ChatPresenter();
                }
            }
        }
        return instance;
    }

    /**
     * 将需要登录成功进入MainActivity中初始化的逻辑，放到此处进行处理
     */
    public void init() {

    }

    public void clear() {
        if (connectionListener != null) {
            DemoHelper.getInstance().getEMClient().removeConnectionListener(connectionListener);
        }
        if (multiDeviceListener != null) {
            DemoHelper.getInstance().getEMClient().removeMultiDeviceListener(multiDeviceListener);
        }
        if (contactListener != null) {
            DemoHelper.getInstance().getContactManager().removeContactListener(contactListener);
        }
        if (conversationListener != null) {
            DemoHelper.getInstance().getChatManager().removeConversationListener(conversationListener);
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        instance = null;
    }

    public void initHandler(Looper looper) {
        handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                Object obj = msg.obj;
                switch (msg.what) {
                    case HANDLER_SHOW_TOAST:
                        if (obj instanceof String) {
                            String str = (String) obj;
                            //ToastUtils.showToast(str);
                            Toast.makeText(appContext, str, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        while (!msgQueue.isEmpty()) {
            showToast(msgQueue.remove());
        }
    }

    void showToast(@StringRes int mesId) {
        showToast(context.getString(mesId));
    }

    void showToast(final String message) {
        Log.d(TAG, "receive invitation to join the group：" + message);
        if (handler != null) {
            Message msg = Message.obtain(handler, HANDLER_SHOW_TOAST, message);
            handler.sendMessage(msg);
        } else {
            msgQueue.add(message);
        }
    }

    @Override
    public void onMessageReceived(List<EMMessage> messages) {
        super.onMessageReceived(messages);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        for (EMMessage message : messages) {
            EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
            EMLog.d(TAG, "onMessageReceived: " + message.getType());
            // 如果设置群组离线消息免打扰，则不进行消息通知
            List<String> disabledIds = DemoHelper.getInstance().getPushManager().getNoPushGroups();
            if (disabledIds != null && disabledIds.contains(message.conversationId())) {
                return;
            }
            // in background, do not refresh UI, notify it in notification bar
           /* if (!BaseYXApp.instance.getLifecycleCallbacks().isFront()) {
                getNotifier().notify(message);
            }*/
            getNotifier().notify(message); //
            //notify new message
            getNotifier().vibrateAndPlayTone(message);
        }
    }


    /**
     * 判断是否已经启动了MainActivity
     *
     * @return
     */
    private synchronized boolean isAppLaunchMain() {
       /* List<Activity> activities = BaseYXApp.instance.getLifecycleCallbacks().getActivityList();
        if (activities != null && !activities.isEmpty()) {
            for (int i = activities.size() - 1; i >= 0; i--) {
                if (activities.get(i) instanceof MainActivity) {
                    return true;
                }
            }
        }
        return false;*/
        return true;
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> messages) {
        super.onCmdMessageReceived(messages);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_CMD_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
    }

    @Override
    public void onMessageRead(List<EMMessage> messages) {
        super.onMessageRead(messages);
        /*
        if (!(BaseYXApp.instance.getLifecycleCallbacks().current() instanceof ChatActivity)) {
            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        }*/
    }

    @Override
    public void onMessageRecalled(List<EMMessage> messages) {

        for (EMMessage msg : messages) {
            if (msg.getChatType() == EMMessage.ChatType.GroupChat && EaseAtMessageHelper.get().isAtMeMsg(msg)) {
                EaseAtMessageHelper.get().removeAtMeGroup(msg.getTo());
            }
            EMMessage msgNotification = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            String text = null;
            String recaller = msg.getRecaller();
            String from = msg.getFrom();
            if ((!TextUtils.isEmpty(recaller)) && !TextUtils.equals(recaller, from)) {
                text = String.format(context.getString(R.string.msg_recall_by_another), recaller, from);
            } else {
                text = String.format(context.getString(R.string.msg_recall_by_user), from);
            }
            EMTextMessageBody txtBody = new EMTextMessageBody(text);
            msgNotification.addBody(txtBody);
            msgNotification.setDirection(msg.direct());
            msgNotification.setFrom(msg.getFrom());
            msgNotification.setTo(msg.getTo());
            msgNotification.setUnread(false);
            msgNotification.setMsgTime(msg.getMsgTime());
            msgNotification.setLocalTime(msg.getMsgTime());
            msgNotification.setChatType(msg.getChatType());
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALL, true);
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALLER, recaller);
            msgNotification.setStatus(EMMessage.Status.SUCCESS);
            EMClient.getInstance().chatManager().saveMessage(msgNotification);
        }

        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
    }

    private class ChatConversationListener implements EMConversationListener {

        @Override
        public void onConversationUpdate() {

        }

        @Override
        public void onConversationRead(String from, String to) {
            EaseEvent event = EaseEvent.create(DemoConstant.CONVERSATION_READ, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.CONVERSATION_READ).postValue(event);
        }
    }

    private class ChatConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {
            EMLog.i(TAG, "onConnected");
            if (!DemoHelper.getInstance().isLoggedIn()) {
                return;
            }
            if (!isGroupsSyncedWithServer) {
                EMLog.i(TAG, "isGroupsSyncedWithServer");

                isGroupsSyncedWithServer = true;
            }
            if (!isContactsSyncedWithServer) {
                EMLog.i(TAG, "isContactsSyncedWithServer");
                new EMContactManagerRepository().getContactList(new ResultCallBack<List<EaseUser>>() {
                    @Override
                    public void onSuccess(List<EaseUser> value) {
                        EmUserDao userDao = DemoDbHelper.getInstance(BaseApp.getInstance()).getUserDao();
                        if (userDao != null) {
                            userDao.clearUsers();
                            userDao.insert(EmUserEntity.parseList(value));
                        }
                        if (value.size() > 0) {
                            FetchUserInfoList fetchUserInfoList = FetchUserInfoList.getInstance();
                            for (EaseUser user : value) {
                                fetchUserInfoList.addUserId(user.getUsername());
                            }
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        EMLog.e(TAG, "onConnected getContactList " + error + "  " + errorMsg);
                    }
                });
                isContactsSyncedWithServer = true;
            }
            if (!isBlackListSyncedWithServer) {
                EMLog.i(TAG, "isBlackListSyncedWithServer");
//                new EMContactManagerRepository().getBlackContactList(null);
                isBlackListSyncedWithServer = true;
            }
            if (!isPushConfigsWithServer) {
                EMLog.i(TAG, "isPushConfigsWithServer");
                //首先获取push配置，否则获取push配置项会为空
                new EMPushManagerRepository().fetchPushConfigsFromServer();
                isPushConfigsWithServer = true;
            }
        }

        /**
         * 用来监听账号异常
         *
         * @param error
         */
        @Override
        public void onDisconnected(int error) {
            EMLog.i(TAG, "onDisconnected =" + error);
            String event = null;
            if (error == EMError.USER_REMOVED) {
                event = DemoConstant.ACCOUNT_REMOVED;
            } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE
                    || error == EMError.USER_BIND_ANOTHER_DEVICE
                    || error == EMError.USER_DEVICE_CHANGED
                    || error == EMError.USER_LOGIN_TOO_MANY_DEVICES) {
                event = DemoConstant.ACCOUNT_CONFLICT;
            } else if (error == EMError.SERVER_SERVICE_RESTRICTED
                    || error == EMError.APP_ACTIVE_NUMBER_REACH_LIMITATION) {
                event = DemoConstant.ACCOUNT_FORBIDDEN;
            } else if (error == EMError.USER_KICKED_BY_CHANGE_PASSWORD) {
                event = DemoConstant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD;
            } else if (error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
                event = DemoConstant.ACCOUNT_KICKED_BY_OTHER_DEVICE;
            }
            if (!TextUtils.isEmpty(event)) {
                LiveDataBus.get().with(DemoConstant.ACCOUNT_CHANGE).postValue(new EaseEvent(event, EaseEvent.TYPE.ACCOUNT));
                EMLog.i(TAG, event);
            }
        }
    }

    private class ChatContactListener implements EMContactListener {

        @Override
        public void onContactAdded(String username) {
            EMLog.i("ChatContactListener", "onContactAdded");
            String[] userId = new String[1];
            userId[0] = username;
            EMClient.getInstance().userInfoManager().fetchUserInfoByUserId(userId, new EMValueCallBack<Map<String, EMUserInfo>>() {
                @Override
                public void onSuccess(Map<String, EMUserInfo> value) {
                    EMUserInfo userInfo = value.get(username);
                    EmUserEntity entity = new EmUserEntity();
                    entity.setUsername(username);
                    if (userInfo != null) {
                        entity.setNickname(userInfo.getNickName());
                        entity.setEmail(userInfo.getEmail());
                        entity.setAvatar(userInfo.getAvatarUrl());
                        entity.setBirth(userInfo.getBirth());
                        entity.setGender(userInfo.getGender());
                        entity.setExt(userInfo.getExt());
                        entity.setContact(0);
                        entity.setSign(userInfo.getSignature());
                    }
                    DemoHelper.getInstance().getModel().insert(entity);
                    DemoHelper.getInstance().updateContactList();
                    EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_ADD, EaseEvent.TYPE.CONTACT);
                    event.message = username;
                    messageChangeLiveData.with(DemoConstant.CONTACT_ADD).postValue(event);

                    showToast(context.getString(R.string.demo_contact_listener_onContactAdded, username));
                    EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactAdded, username));
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.i(TAG, context.getString(R.string.demo_contact_get_userInfo_failed) + username + "error:" + error + " errorMsg:" + errorMsg);
                }
            });
        }

        @Override
        public void onContactDeleted(String username) {
            EMLog.i("ChatContactListener", "onContactDeleted");
            boolean deleteUsername = DemoHelper.getInstance().getModel().isDeleteUsername(username);
            int num = DemoHelper.getInstance().deleteContact(username);
            DemoHelper.getInstance().updateContactList();
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_DELETE, EaseEvent.TYPE.CONTACT);
            event.message = username;
            messageChangeLiveData.with(DemoConstant.CONTACT_DELETE).postValue(event);

            if (deleteUsername || num == 0) {
                showToast(context.getString(R.string.demo_contact_listener_onContactDeleted, username));
                EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactDeleted, username));
            } else {
                //showToast(context.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
                EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
            }
        }


        @Override
        public void onContactInvited(String username, String reason) {
            EMLog.i("ChatContactListener", "onContactInvited");
            List<EMMessage> allMessages = EaseSystemMsgManager.getInstance().getAllMessages();
            if (allMessages != null && !allMessages.isEmpty()) {
                for (EMMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if (ext != null && !ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM) && TextUtils.equals(username, (String) ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                        EaseSystemMsgManager.getInstance().removeMessage(message);
                    }
                }
            }

            Map<String, Object> ext = EaseSystemMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEINVITEED.name());
            EMMessage message = EaseSystemMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);

            showToast(context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
        }

        @Override
        public void onFriendRequestAccepted(String username) {
            EMLog.i("ChatContactListener", "onFriendRequestAccepted");
            List<EMMessage> allMessages = EaseSystemMsgManager.getInstance().getAllMessages();
            if (allMessages != null && !allMessages.isEmpty()) {
                for (EMMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if (ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                            && TextUtils.equals(username, (String) ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                        updateMessage(message);
                        return;
                    }
                }
            }
            Map<String, Object> ext = EaseSystemMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
            EMMessage message = EaseSystemMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);

            showToast(context.getString(InviteMessageStatus.BEAGREED.getMsgContent()));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEAGREED.getMsgContent()));
        }

        @Override
        public void onFriendRequestDeclined(String username) {
            EMLog.i("ChatContactListener", "onFriendRequestDeclined");
            Map<String, Object> ext = EaseSystemMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEREFUSED.name());
            EMMessage message = EaseSystemMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);

            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);
            showToast(context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
        }
    }


    private void updateMessage(EMMessage message) {
        message.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
        EMTextMessageBody body = new EMTextMessageBody(PushAndMessageHelper.getSystemMessage(message.ext()));
        message.addBody(body);
        EaseSystemMsgManager.getInstance().updateMessage(message);
    }

    private class ChatMultiDeviceListener implements EMMultiDeviceListener {


        @Override
        public void onContactEvent(int event, String target, String ext) {
            EMLog.i(TAG, "onContactEvent event" + event);
            DemoDbHelper dbHelper = DemoDbHelper.getInstance(BaseApp.getInstance());
            String message = null;
            switch (event) {
                case CONTACT_REMOVE: //好友已经在其他机子上被移除
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_REMOVE");
                    message = DemoConstant.CONTACT_REMOVE;
                    if (dbHelper.getUserDao() != null) {
                        dbHelper.getUserDao().deleteUser(target);
                    }
                    removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: 2020/1/16 0016 确认此处逻辑，是否是删除当前的target
                    DemoHelper.getInstance().getChatManager().deleteConversation(target, false);

                    showToast("CONTACT_REMOVE");
                    break;
                case CONTACT_ACCEPT: //好友请求已经在其他机子上被同意
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_ACCEPT");
                    message = DemoConstant.CONTACT_ACCEPT;
                    EmUserEntity entity = new EmUserEntity();
                    entity.setUsername(target);
                    if (dbHelper.getUserDao() != null) {
                        dbHelper.getUserDao().insert(entity);
                    }
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT);

                    showToast("CONTACT_ACCEPT");
                    break;
                case CONTACT_DECLINE: //好友请求已经在其他机子上被拒绝
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_DECLINE");
                    message = DemoConstant.CONTACT_DECLINE;
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE);

                    showToast("CONTACT_DECLINE");
                    break;
                case CONTACT_BAN: //当前用户在其他设备加某人进入黑名单
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_BAN");
                    message = DemoConstant.CONTACT_BAN;
                    if (dbHelper.getUserDao() != null) {
                        dbHelper.getUserDao().deleteUser(target);
                    }
                    removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM);
                    DemoHelper.getInstance().getChatManager().deleteConversation(target, false);
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN);

                    showToast("CONTACT_BAN");
                    break;
                case CONTACT_ALLOW: // 好友在其他设备被移出黑名单
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_ALLOW");
                    message = DemoConstant.CONTACT_ALLOW;
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW);

                    showToast("CONTACT_ALLOW");
                    break;
            }
            if (!TextUtils.isEmpty(message)) {
                EaseEvent easeEvent = EaseEvent.create(message, EaseEvent.TYPE.CONTACT);
                messageChangeLiveData.with(message).postValue(easeEvent);
            }
        }

        @Override
        public void onGroupEvent(int event, String groupId, List<String> usernames) {
            EMLog.i(TAG, "onGroupEvent event" + event);
            String message = null;
            switch (event) {
                case GROUP_CREATE:
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_CREATE);

                    showToast("GROUP_CREATE");
                    break;
                case GROUP_DESTROY:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_DESTROY);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_DESTROY");
                    break;
                case GROUP_JOIN:
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_JOIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_JOIN");
                    break;
                case GROUP_LEAVE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_LEAVE");
                    break;
                case GROUP_APPLY:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY);

                    showToast("GROUP_APPLY");
                    break;
                case GROUP_APPLY_ACCEPT:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT);

                    showToast("GROUP_APPLY_ACCEPT");
                    break;
                case GROUP_APPLY_DECLINE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE);

                    showToast("GROUP_APPLY_DECLINE");
                    break;
                case GROUP_INVITE:
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE);

                    showToast("GROUP_INVITE");
                    break;
                case GROUP_INVITE_ACCEPT:
                    String st3 = context.getString(R.string.Invite_you_to_join_a_group_chat);
                    EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                    msg.setChatType(EMMessage.ChatType.GroupChat);
                    // TODO: person, reason from ext
                    String from = "";
                    if (usernames != null && usernames.size() > 0) {
                        msg.setFrom(usernames.get(0));
                    }
                    msg.setTo(groupId);
                    msg.setMsgId(UUID.randomUUID().toString());
                    msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true);
                    msg.addBody(new EMTextMessageBody(msg.getFrom() + " " + st3));
                    msg.setStatus(EMMessage.Status.SUCCESS);
                    // save invitation as messages
                    EMClient.getInstance().chatManager().saveMessage(msg);

                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_INVITE_ACCEPT");
                    break;
                case GROUP_INVITE_DECLINE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE);

                    showToast("GROUP_INVITE_DECLINE");
                    break;
                case GROUP_KICK:
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_KICK);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_KICK");
                    break;
                case GROUP_BAN:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_BAN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_BAN");
                    break;
                case GROUP_ALLOW:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW);

                    showToast("GROUP_ALLOW");
                    break;
                case GROUP_BLOCK:
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_BLOCK);

                    showToast("GROUP_BLOCK");
                    break;
                case GROUP_UNBLOCK:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_UNBLOCK);

                    showToast("GROUP_UNBLOCK");
                    break;
                case GROUP_ASSIGN_OWNER:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER);

                    showToast("GROUP_ASSIGN_OWNER");
                    break;
                case GROUP_ADD_ADMIN:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_ADD_ADMIN");
                    break;
                case GROUP_REMOVE_ADMIN:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_REMOVE_ADMIN");
                    break;
                case GROUP_ADD_MUTE:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE);

                    showToast("GROUP_ADD_MUTE");
                    break;
                case GROUP_REMOVE_MUTE:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE);

                    showToast("GROUP_REMOVE_MUTE");
                    break;
                case GROUP_METADATA_CHANGED:
                    EMLog.d(TAG, "EVENT GROUP_METADATA_CHANGED");
                    break;
                default:
                    break;
            }
            if (!TextUtils.isEmpty(message)) {
                EaseEvent easeEvent = EaseEvent.create(message, EaseEvent.TYPE.GROUP);
                messageChangeLiveData.with(message).postValue(easeEvent);
            }
        }
    }

    /**
     * 移除目标所有的消息记录，如果目标被删除
     *
     * @param target
     */
    private void removeTargetSystemMessage(String target, String params) {
        EMConversation conversation = EaseSystemMsgManager.getInstance().getConversation();
        List<EMMessage> messages = conversation.getAllMessages();
        if (messages != null && !messages.isEmpty()) {
            for (EMMessage message : messages) {
                String from = null;
                try {
                    from = message.getStringAttribute(params);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                if (TextUtils.equals(from, target)) {
                    conversation.removeMessage(message.getMsgId());
                }
            }
        }
    }

    /**
     * 移除目标所有的消息记录，如果目标被删除
     *
     * @param target1
     */
    private void removeTargetSystemMessage(String target1, String params1, String target2, String params2) {
        EMConversation conversation = EaseSystemMsgManager.getInstance().getConversation();
        List<EMMessage> messages = conversation.getAllMessages();
        if (messages != null && !messages.isEmpty()) {
            for (EMMessage message : messages) {
                String targetParams1 = null;
                String targetParams2 = null;
                try {
                    targetParams1 = message.getStringAttribute(params1);
                    targetParams2 = message.getStringAttribute(params2);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                if (TextUtils.equals(targetParams1, target1) && TextUtils.equals(targetParams2, target2)) {
                    conversation.removeMessage(message.getMsgId());
                }
            }
        }
    }


    private void notifyNewInviteMessage(EMMessage msg) {
        // notify there is new message
        getNotifier().vibrateAndPlayTone(null);
    }

    private void updateContactNotificationStatus(String from, String reason, InviteMessageStatus status) {
        EMMessage msg = null;
        EMConversation conversation = EaseSystemMsgManager.getInstance().getConversation();
        List<EMMessage> allMessages = conversation.getAllMessages();
        if (allMessages != null && !allMessages.isEmpty()) {
            for (EMMessage message : allMessages) {
                Map<String, Object> ext = message.ext();
                if (ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                        && TextUtils.equals(from, (String) ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                    msg = message;
                }
            }
        }

        if (msg != null) {
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
            EaseSystemMsgManager.getInstance().updateMessage(msg);
        } else {
            // save invitation as message
            Map<String, Object> ext = EaseSystemMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, from);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
            msg = EaseSystemMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);
            notifyNewInviteMessage(msg);
        }
    }

    private void saveGroupNotification(String groupId, String groupName, String inviter, String reason, InviteMessageStatus status) {
        Map<String, Object> ext = EaseSystemMsgManager.getInstance().createMsgExt();
        ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, groupId);
        ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
        ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
        ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
        ext.put(DemoConstant.SYSTEM_MESSAGE_INVITER, inviter);
        ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
        EMMessage message = EaseSystemMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

        notifyNewInviteMessage(message);
    }


    private void setChatRoomEvent(String roomId, EaseEvent.TYPE type) {
        EaseEvent easeEvent = new EaseEvent(DemoConstant.CHAT_ROOM_CHANGE, type);
        easeEvent.message = roomId;
        messageChangeLiveData.with(DemoConstant.CHAT_ROOM_CHANGE).postValue(easeEvent);
    }

    private String getContentFromList(List<String> members) {
        StringBuilder sb = new StringBuilder();
        for (String member : members) {
            if (!TextUtils.isEmpty(sb.toString().trim())) {
                sb.append(",");
            }
            sb.append(member);
        }
        String content = sb.toString();
        if (content.contains(EMClient.getInstance().getCurrentUser())) {
            content = context.getString(R.string.you);
        }
        return content;
    }
}
