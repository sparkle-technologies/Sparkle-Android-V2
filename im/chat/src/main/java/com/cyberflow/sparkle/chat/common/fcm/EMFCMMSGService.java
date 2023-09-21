package com.cyberflow.sparkle.chat.common.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cyberflow.sparkle.chat.DemoHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hyphenate.chat.EMClient;


/**
 * Created by zhangsong on 17-9-15.
 * FCM push service
 */
public class EMFCMMSGService extends FirebaseMessagingService {
    private static final String TAG = "EMFCMMSGService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            String message = remoteMessage.getData().get("alert");
            Log.e(TAG, "onMessageReceived: " + message);
            DemoHelper.getInstance().getNotifier().notify(message);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG, "onNewToken: " + token);
        // 若要对该应用实例发送消息或管理服务端的应用订阅，将 FCM 注册 token 发送至你的应用服务器。
        if(EMClient.getInstance().isSdkInited()){
            EMClient.getInstance().sendFCMTokenToServer(token);
        }
    }
}
