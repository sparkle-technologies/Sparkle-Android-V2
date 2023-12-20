package com.cyberflow.sparkle.chat;

import static com.hyphenate.easeui.utils.EaseUserUtils.getGroupUserInfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.cyberflow.base.BaseApp;
import com.cyberflow.sparkle.chat.common.db.DemoDbHelper;
import com.cyberflow.sparkle.chat.common.delegates.ChatRecallAdapterDelegate;
import com.cyberflow.sparkle.chat.common.manager.UserProfileManager;
import com.cyberflow.sparkle.chat.common.model.DemoModel;
import com.cyberflow.sparkle.chat.common.model.EmojiconExampleGroupData;
import com.cyberflow.sparkle.chat.common.receiver.HeadsetReceiver;
import com.cyberflow.sparkle.chat.common.utils.ChatPresenter;
import com.cyberflow.sparkle.chat.common.utils.FetchUserInfoList;
import com.cyberflow.sparkle.chat.common.utils.FetchUserRunnable;
import com.cyberflow.sparkle.chat.common.utils.PreferenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMPushManager;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.delegate.EaseCustomAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseExpressionAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseImageAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseTextAdapterDelegate;
import com.hyphenate.easeui.delegate.EaseVideoAdapterDelegate;
import com.hyphenate.easeui.domain.EaseAvatarOptions;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.manager.EaseMessageTypeSetManager;
import com.hyphenate.easeui.model.EaseNotifier;
import com.hyphenate.easeui.provider.EaseEmojiconInfoProvider;
import com.hyphenate.easeui.provider.EaseSettingsProvider;
import com.hyphenate.easeui.provider.EaseUserProfileProvider;
import com.hyphenate.push.EMPushConfig;
import com.hyphenate.push.EMPushHelper;
import com.hyphenate.push.EMPushType;
import com.hyphenate.push.PushListener;
import com.hyphenate.util.EMLog;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 作为hyphenate-sdk的入口控制类，获取sdk下的基础类均通过此类
 */
public class DemoHelper {
    private static final String TAG = DemoHelper.class.getSimpleName();

    public boolean isSDKInit;//SDK是否初始化
    private static DemoHelper mInstance;
    private DemoModel demoModel = null;
    private Map<String, EaseUser> contactList;
    private UserProfileManager userProManager;
    private Context mainContext;

    private String tokenUrl = "http://a1.easemob.com/token/rtcToken/v1";
    private String uIdUrl = "http://a1.easemob.com/channel/mapper";

    private FetchUserRunnable fetchUserRunnable;
    private Thread fetchUserTread;
    private FetchUserInfoList fetchUserInfoList;


    private DemoHelper() {
    }

    public void init(Context context) {
        demoModel = new DemoModel(context);  // SP/数据库 的存取
        //初始化IM SDK
        if (initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            EMClient.getInstance().setDebugMode(true);
            // set Call options
            setCallOptions(context);
            //初始化推送
            initPush(context);
            //初始化ease ui相关
            initEaseUI(context);
            //注册对话类型
            registerConversationType();
            startFetchUserRunnable();
        }
    }

    public static DemoHelper getInstance() {
        if (mInstance == null) {
            synchronized (DemoHelper.class) {
                if (mInstance == null) {
                    mInstance = new DemoHelper();
                }
            }
        }
        return mInstance;
    }

    //获取用户信息
    private void startFetchUserRunnable() {
        fetchUserInfoList = FetchUserInfoList.getInstance();
        fetchUserRunnable = new FetchUserRunnable();
        fetchUserTread = new Thread(fetchUserRunnable);
        fetchUserTread.start();
    }

    /**
     * 初始化SDK
     *
     * @param context
     * @return
     */
    private boolean initSDK(Context context) {
        // 根据项目需求对SDK进行配置
        EMOptions options = initChatOptions(context);  // 聊天  推送 服务器地址 等 参数配置
        // 初始化SDK
        isSDKInit = EaseIM.getInstance().init(context, options);
        //设置删除用户属性数据超时时间
        demoModel.setUserInfoTimeOut(30 * 60 * 1000);
        mainContext = context;
        return isSDKInit();
    }


    /**
     * 注册对话类型
     */
    private void registerConversationType() {
        EaseMessageTypeSetManager.getInstance()
                .addMessageType(EaseExpressionAdapterDelegate.class)       //自定义表情
//                .addMessageType(EaseFileAdapterDelegate.class)             //文件
                .addMessageType(EaseImageAdapterDelegate.class)            //图片
//                .addMessageType(EaseLocationAdapterDelegate.class)         //定位
                .addMessageType(EaseVideoAdapterDelegate.class)            //视频
//                .addMessageType(EaseVoiceAdapterDelegate.class)            //声音
                .addMessageType(ChatRecallAdapterDelegate.class)           //消息撤回
//                .addMessageType(ChatUserCardAdapterDelegate.class)         //名片消息
                .addMessageType(EaseCustomAdapterDelegate.class)           //自定义消息
                .setDefaultMessageType(EaseTextAdapterDelegate.class);       //文本
    }

    /**
     * 判断是否之前登录过
     *
     * @return
     */
    public boolean isLoggedIn() {
        return getEMClient().isLoggedInBefore();
    }

    /**
     * 获取IM SDK的入口类
     * 主要完成登录、退出、连接管理等功能。也是获取其他模块的入口
     *
     * @return
     */
    public EMClient getEMClient() {
        return EMClient.getInstance();
    }

    /**
     * 获取contact manager
     * 负责好友的添加删除，黑名单的管理
     *
     * @return
     */
    public EMContactManager getContactManager() {
        return getEMClient().contactManager();
    }

    /**
     * get EMChatManager
     * 管理消息的收发，完成会话管理等功能
     *
     * @return
     */
    public EMChatManager getChatManager() {
        return getEMClient().chatManager();
    }

    /**
     * get push manager
     * 离线消息推送管理类，针对 GCM、小米、华为等离线推送以及 APNS
     *
     * @return
     */
    public EMPushManager getPushManager() {
        return getEMClient().pushManager();
    }

    /**
     * get conversation
     *
     * @param username
     * @param type
     * @param createIfNotExists
     * @return
     */
    public EMConversation getConversation(String username, EMConversation.EMConversationType type, boolean createIfNotExists) {
        return getChatManager().getConversation(username, type, createIfNotExists);
    }

    public String getCurrentUser() {
        return getEMClient().getCurrentUser();
    }

    /**
     * ChatPresenter中添加了网络连接状态监听，多端登录监听，群组监听，联系人监听，聊天室监听
     *
     * @param context
     */
    private void initEaseUI(Context context) {
        Log.e(TAG, "initEaseUI: ");
        //添加ChatPresenter,ChatPresenter中添加了网络连接状态监听，
        EaseIM.getInstance().addChatPresenter(ChatPresenter.getInstance());
        EaseIM.getInstance()
                .setSettingsProvider(new EaseSettingsProvider() {
                    @Override
                    public boolean isMsgNotifyAllowed(EMMessage message) {
                        if (message == null) {
                            return demoModel.getSettingMsgNotification();
                        }
                        if (!demoModel.getSettingMsgNotification()) {
                            return false;
                        } else {
                            String chatUsename = null;
                            List<String> notNotifyIds = null;
                            // get user or group id which was blocked to show message notifications
                            if (message.getChatType() == EMMessage.ChatType.Chat) {
                                chatUsename = message.getFrom();
                                notNotifyIds = demoModel.getDisabledIds();
                            } else {
                                chatUsename = message.getTo();
                                notNotifyIds = demoModel.getDisabledGroups();
                            }

                            if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }

                    @Override
                    public boolean isMsgSoundAllowed(EMMessage message) {
                        return demoModel.getSettingMsgSound();
                    }

                    @Override
                    public boolean isMsgVibrateAllowed(EMMessage message) {
                        return demoModel.getSettingMsgVibrate();
                    }

                    @Override
                    public boolean isSpeakerOpened() {
                        return demoModel.getSettingMsgSpeaker();
                    }
                })
                .setEmojiconInfoProvider(new EaseEmojiconInfoProvider() {
                    @Override
                    public EaseEmojicon getEmojiconInfo(String emojiconIdentityGroupCode, String emojiconIdentityCode) {
//                        Log.e(TAG, "getEmojiconInfo: emojiconIdentityGroupCode=" + emojiconIdentityGroupCode + "\t emojiconIdentityCode=" + emojiconIdentityCode );
                        EaseEmojiconGroupEntity data = EmojiconExampleGroupData.getData();
                        for (EaseEmojicon emojicon : data.getEmojiconList()) {
//                            Log.e(TAG, "getEmojiconInfo: emojicon.getIdentityCode()="+ emojicon.getIdentityCode() );
                            if (emojicon.getIdentityCode().equals(emojiconIdentityCode)) {
                                return emojicon;
                            }
                        }
                        return null;
                    }

                    @Override
                    public Map<String, Object> getTextEmojiconMapping() {
                        return null;
                    }
                })
                .setAvatarOptions(getAvatarOptions())
                .setUserProvider(new EaseUserProfileProvider() {
                    @Override
                    public EaseUser getUser(String username) {
                        return getUserInfo(username);
                    }

                    @Override
                    public EaseUser getGroupUser(String groupId, String userId) {
                        return getGroupUserInfo(groupId, userId);
                    }
                });
    }

    /**
     * 统一配置头像
     *
     * @return
     */
    private EaseAvatarOptions getAvatarOptions() {
        EaseAvatarOptions avatarOptions = new EaseAvatarOptions();
        avatarOptions.setAvatarShape(1);
        return avatarOptions;
    }


    public EaseUser getUserInfo(String username) {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        EaseUser user = null;
        if (username.equals(EMClient.getInstance().getCurrentUser())) {
            user = getUserProfileManager().getCurrentUserInfo();
            return user;
        }
        user = getContactList().get(username);
        if (user == null) {
            //找不到更新会话列表 继续查找
            updateContactList();
            user = getContactList().get(username);
            //如果还找不到从服务端异步拉取 然后通知UI刷新列表
            if (user == null) {
                user = new EaseUser(username);
            }
        }
        return user;
    }

//    private final static String APP_KEY_TEST = "1111230615161307#sparkletest";
//    private final static String APP_KEY_TEST = "1111230615161307#demo";
    private final static String APP_KEY_TEST = "1183231031159952#sparkle";  // pro


    /**
     * 根据自己的需要进行配置
     *
     * @param context
     * @return
     */
    private EMOptions initChatOptions(Context context) {
        Log.d(TAG, "init HuanXin Options");
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);  // 设置是否自动接受加好友邀请,默认是true
        options.setRequireAck(true);// 设置是否需要接受方已读确认
        options.setRequireDeliveryAck(false);// 设置是否需要接受方送达确认,默认false
        options.setFpaEnable(true);//设置fpa开关，默认false
        options.setEnableStatistics(true);// 开启本地消息流量统计
        /**
         * NOTE:你需要设置自己申请的账号来使用三方推送功能，详见集成文档
         */
        EMPushConfig.Builder builder = new EMPushConfig.Builder(context);

        builder.enableFCM("782795210914");  // from firebase server : https://console.firebase.google.com/project/sparkle-android-app/settings/cloudmessaging?hl=zh-cn

      /*  builder.enableVivoPush() // 需要在AndroidManifest.xml中配置appId和appKey
                .enableMeiZuPush("134952", "f00e7e8499a549e09731a60a4da399e3")
                .enableMiPush("2882303761517426801", "5381742660801")
                .enableOppoPush("7eac7f0e69a24dbda40b3a8e7aa93a7c", "2b10d0d9e2004817888fe6ffd1a37688")
                .enableHWPush() // 需要在AndroidManifest.xml中配置appId
                .enableFCM("782795210914");*/

        options.setPushConfig(builder.build());

        options.setAppKey(APP_KEY_TEST);

        // 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
//        options.allowChatroomOwnerLeave(demoModel.isChatroomOwnerLeaveAllowed());
        // 设置退出(主动和被动退出)群组时是否删除聊天消息
//        options.setDeleteMessagesAsExitGroup(demoModel.isDeleteMessagesAsExitGroup());
        // 设置是否自动接受加群邀请
//        options.setAutoAcceptGroupInvitation(demoModel.isAutoAcceptGroupInvitation());
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
        options.setAutoTransferMessageAttachments(demoModel.isSetTransferFileByUser());
        // 是否自动下载缩略图，默认是true为自动下载
        options.setAutoDownloadThumbnail(demoModel.isSetAutodownloadThumbnail());
        return options;
    }

    private void setCallOptions(Context context) {
        HeadsetReceiver headsetReceiver = new HeadsetReceiver();
        IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(headsetReceiver, headsetFilter);
    }

    public void initPush(Context context) {
        if (EaseIM.getInstance().isMainProcess(context)) {
            EMPushHelper.getInstance().setPushListener(new PushListener() {
                @Override
                public void onError(EMPushType pushType, long errorCode) {
                    // TODO: 返回的errorCode仅9xx为环信内部错误，可从EMError中查询，其他错误请根据pushType去相应第三方推送网站查询。
                    EMLog.e("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
                }

                @Override
                public boolean isSupportPush(EMPushType pushType, EMPushConfig pushConfig) {
                    // 由外部实现代码判断设备是否支持FCM推送
                    if (pushType == EMPushType.FCM) {
                        EMLog.d("FCM", "GooglePlayServiceCode:" + GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context));
                        /*FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                            if(!task.isSuccessful()){
                                return;
                            }
                            String token = task.getResult();
                            EMLog.d("FCM", token);
                            EMClient.getInstance().sendFCMTokenToServer(token);
                        });*/
                        return demoModel.isUseFCM() && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
                    }
                    return super.isSupportPush(pushType, pushConfig);
                }
            });
        }
    }

    /**
     * logout
     *
     * @param unbindDeviceToken whether you need unbind your device token
     * @param callback          callback
     */
    public void logout(boolean unbindDeviceToken, final EMCallBack callback) {
        Log.d(TAG, "logout: " + unbindDeviceToken);
        if (fetchUserTread != null && fetchUserRunnable != null) {
            fetchUserRunnable.setStop(true);
        }

        logoutFromIM(unbindDeviceToken, callback);

    }

    private void logoutFromIM(boolean unbindDeviceToken, final EMCallBack callback) {
        EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                DemoHelper.getInstance().getModel().setPhoneNumber("");
                logoutSuccess();
                //reset();
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                Log.d(TAG, "logout: onSuccess");
                //reset();
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    /**
     * 关闭当前进程
     */
    public void killApp() {
        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    /**
     * 退出登录后，需要处理的业务逻辑
     */
    public void logoutSuccess() {
        Log.d(TAG, "logout: onSuccess");
        setAutoLogin(false);
        DemoDbHelper.getInstance(BaseApp.getInstance()).closeDb();
        getUserProfileManager().reset();
    }

    public EaseAvatarOptions getEaseAvatarOptions() {
        return EaseIM.getInstance().getAvatarOptions();
    }

    public DemoModel getModel() {
        if (demoModel == null) {
            demoModel = new DemoModel(BaseApp.getInstance());
        }
        return demoModel;
    }

    public String getCurrentLoginUser() {
        return getModel().getCurrentUsername();
    }

    /**
     * get instance of EaseNotifier
     *
     * @return
     */
    public EaseNotifier getNotifier() {
        return EaseIM.getInstance().getNotifier();
    }

    /**
     * 设置本地标记，是否自动登录
     *
     * @param autoLogin
     */
    public void setAutoLogin(boolean autoLogin) {
        PreferenceManager.getInstance().setAutoLogin(autoLogin);
    }

    /**
     * 获取本地标记，是否自动登录
     *
     * @return
     */
    public boolean getAutoLogin() {
        return PreferenceManager.getInstance().getAutoLogin();
    }

    /**
     * 设置SDK是否初始化
     *
     * @param init
     */
    public void setSDKInit(boolean init) {
        isSDKInit = init;
    }

    public boolean isSDKInit() {
        return isSDKInit;
    }

    /**
     * 向数据库中插入数据
     *
     * @param object
     */
    public void insert(Object object) {
        demoModel.insert(object);
    }

    /**
     * update
     *
     * @param object
     */
    public void update(Object object) {
        demoModel.update(object);
    }

    /**
     * update user list
     *
     * @param users
     */
    public void updateUserList(List<EaseUser> users) {
        demoModel.updateContactList(users);
    }

    /**
     * 更新过期的用户属性数据
     */
    public void updateTimeoutUsers() {
        List<String> userIds = demoModel.selectTimeOutUsers();
        if (userIds != null && userIds.size() > 0) {
            if (fetchUserInfoList != null) {
                for (int i = 0; i < userIds.size(); i++) {
                    fetchUserInfoList.addUserId(userIds.get(i));
                }
                fetchUserRunnable.setStop(false);
            }
        }
    }

    /**
     * get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        if (isLoggedIn() && contactList == null) {
            updateTimeoutUsers();
            contactList = demoModel.getAllUserList();
        }

        // return a empty non-null object to avoid app crash
        if (contactList == null) {
            return new Hashtable<String, EaseUser>();
        }
        return contactList;
    }

    public void reLoadUserInfoFromDb() {
        contactList = demoModel.getAllUserList();
    }

    /**
     * update contact list
     */
    public void updateContactList() {
        if (isLoggedIn()) {
            updateTimeoutUsers();
            contactList = demoModel.getContactList();
        }
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
    }

    /**
     * 展示通知设置页面
     */
    public void showNotificationPermissionDialog() {
        EMPushType pushType = EMPushHelper.getInstance().getPushType();
    }

    /**
     * 删除联系人
     *
     * @param username
     * @return
     */
    public synchronized int deleteContact(String username) {
        if (TextUtils.isEmpty(username)) {
            return 0;
        }
        DemoDbHelper helper = DemoDbHelper.getInstance(BaseApp.getInstance());
        if (helper.getUserDao() == null) {
            return 0;
        }
        int num = helper.getUserDao().deleteUser(username);
        if (helper.getInviteMessageDao() != null) {
            helper.getInviteMessageDao().deleteByFrom(username);
        }
        EMClient.getInstance().chatManager().deleteConversation(username, false);
        getModel().deleteUsername(username, false);
        Log.e(TAG, "delete num = " + num);
        return num;
    }

    /**
     * 检查是否是第一次安装登录
     * 默认值是true, 需要在用api拉取完会话列表后，就其置为false.
     *
     * @return
     */
    public boolean isFirstInstall() {
        return getModel().isFirstInstall();
    }

    /**
     * 将状态置为非第一次安装，在调用获取会话列表的api后调用
     * 并将会话列表是否来自服务器置为true
     */
    public void makeNotFirstInstall() {
        getModel().makeNotFirstInstall();
    }

    /**
     * 检查会话列表是否从服务器返回数据
     *
     * @return
     */
    public boolean isConComeFromServer() {
        return getModel().isConComeFromServer();
    }

    /**
     * Determine if it is from the current user account of another device
     *
     * @param username
     * @return
     */
    public boolean isCurrentUserFromOtherDevice(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        if (username.contains("/") && username.contains(EMClient.getInstance().getCurrentUser())) {
            return true;
        }
        return false;
    }


    /**
     * data sync listener
     */
    public interface DataSyncListener {
        /**
         * sync complete
         *
         * @param success true：data sync successful，false: failed to sync data
         */
        void onSyncComplete(boolean success);
    }
}
