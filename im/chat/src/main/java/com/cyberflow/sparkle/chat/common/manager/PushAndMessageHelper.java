package com.cyberflow.sparkle.chat.common.manager;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.cyberflow.base.BaseApp;
import com.cyberflow.base.net.GsonConverter;
import com.cyberflow.base.util.bus.LiveDataBus;
import com.cyberflow.sparkle.chat.DemoHelper;
import com.cyberflow.sparkle.chat.R;
import com.cyberflow.sparkle.chat.common.constant.DemoConstant;
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessage;
import com.cyberflow.sparkle.chat.common.db.entity.InviteMessageStatus;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMFileHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于处理推送及消息相关
 */
public class PushAndMessageHelper {

    private static boolean isLock;


    /**
     * 转发消息
     *
     * @param toChatUsername
     * @param msgId
     */
    public static void sendForwardMessage(String toChatUsername, String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return;
        }
        EMMessage message = DemoHelper.getInstance().getChatManager().getMessage(msgId);
        EMMessage.Type type = message.getType();
        switch (type) {
            case TXT:
                if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                    String emojiconMap = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION, null);
                    HashMap<String, String> map = GsonConverter.Companion.getGson().fromJson(emojiconMap, HashMap.class);
                    String emojiconGroupId = map.get(EaseConstant.MESSAGE_ATTR_EXPRESSION_GROUP_ID);
                    String emojiconId = map.get(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID);
                    sendBigExpressionMessage(toChatUsername, ((EMTextMessageBody) message.getBody()).getMessage(), emojiconGroupId, emojiconId);
                } else {
                    // get the content and send it
                    String content = ((EMTextMessageBody) message.getBody()).getMessage();
                    sendTextMessage(toChatUsername, content);
                }
                break;
            case IMAGE:
                // send image
                Uri uri = getImageForwardUri((EMImageMessageBody) message.getBody());
                if (uri != null) {
                    sendImageMessage(toChatUsername, uri);
                } else {
                    LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR).postValue(BaseApp.getInstance().getApplicationContext().getString(R.string.no_image_resource));
                }
                break;
            case VIDEO:
                if(message.getBody() instanceof EMVideoMessageBody){
                    EMVideoMessageBody body = (EMVideoMessageBody)message.getBody();
                    int videoLength = body.getDuration();
                    Uri videoUri = body.getLocalUri();
                    //EMFileHelper.getInstance().formatInUri("");
                    String thumbPath = body.getLocalThumb();

//                    Log.e("TAG", "getDuration: " + body.getDuration() );
//                    Log.e("TAG", "getThumbnailUrl: " + body.getThumbnailUrl() );
//                    Log.e("TAG", "getLocalThumb: " + body.getLocalThumb() );
//                    Log.e("TAG", "getLocalUri: " + body.getLocalUri() );
//                    Log.e("TAG", "getRemoteUrl: " + body.getRemoteUrl() );

                    EMMessage newMsg = EMMessage.createVideoSendMessage(videoUri, thumbPath, videoLength, toChatUsername);
                    sendMessage(newMsg);
                }
                break;
        }
    }

    public static Uri getImageForwardUri(EMImageMessageBody body) {
        if (body == null) {
            return null;
        }
        Uri localUri = body.getLocalUri();
        Context context = BaseApp.getInstance().getApplicationContext();
        if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            return localUri;
        }
        localUri = body.thumbnailLocalUri();
        if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            return localUri;
        }
        return null;
    }

    /**
     * 获取系统消息内容
     *
     * @param msg
     * @return
     */
    public static String getSystemMessage(InviteMessage msg) {
        InviteMessageStatus status = msg.getStatusEnum();
        if (status == null) {
            return "";
        }
        String messge;
        Context context = BaseApp.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.getFrom());
                break;
            case BEAGREED:
                messge = builder.toString();
                break;
            case BEAPPLYED:
            case GROUPINVITATION:
                messge = String.format(builder.toString(), msg.getFrom(), msg.getGroupName());
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.getGroupInviter());
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.getFrom());
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * 获取系统消息内容
     *
     * @param msg
     * @return
     */
    public static String getSystemMessage(EMMessage msg) throws HyphenateException {
        String messageStatus = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);
        if (TextUtils.isEmpty(messageStatus)) {
            return "";
        }
        InviteMessageStatus status = InviteMessageStatus.valueOf(messageStatus);
        if (status == null) {
            return "";
        }
        String messge;
        Context context = BaseApp.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case BEAGREED:
            case MULTI_DEVICE_GROUP_LEAVE:
                messge = builder.toString();
                break;
            case BEAPPLYED:
            case GROUPINVITATION:
                String name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_NAME);
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM), name);
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * 获取系统消息内容
     *
     * @param msg
     * @return
     */
    public static String getSystemMessage(Map<String, Object> msg) throws NullPointerException {
        String messageStatus = (String) msg.get(DemoConstant.SYSTEM_MESSAGE_STATUS);
        if (TextUtils.isEmpty(messageStatus)) {
            return "";
        }
        InviteMessageStatus status = InviteMessageStatus.valueOf(messageStatus);
        if (status == null) {
            return "";
        }
        String messge;
        Context context = BaseApp.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case BEAGREED:
            case MULTI_DEVICE_GROUP_LEAVE:
                messge = builder.toString();
                break;
            case BEAPPLYED:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM), (String) msg.get(DemoConstant.SYSTEM_MESSAGE_NAME));
                break;
            case GROUPINVITATION:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_INVITER), (String) msg.get(DemoConstant.SYSTEM_MESSAGE_NAME));
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_INVITER));
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * send big expression message
     *
     * @param toChatUsername
     * @param name
     * @param identityCode
     */
    private static void sendBigExpressionMessage(String toChatUsername, String name,String identityGroupCode,  String identityCode) {
        EMMessage message = EaseCommonUtils.createExpressionMessage(toChatUsername, name, identityGroupCode, identityCode);
        sendMessage(message);
    }

    /**
     * 发送文本消息
     *
     * @param toChatUsername
     * @param content
     */
    private static void sendTextMessage(String toChatUsername, String content) {
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    private final static int MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * send image message
     *
     * @param toChatUsername
     * @param imageUri
     */
    private static void sendImageMessage(String toChatUsername, Uri imageUri) {
        boolean sendOriginalImg = true;
        if(EMFileHelper.getInstance().isFileExist(imageUri)) {
            if(EMFileHelper.getInstance().getFileLength(imageUri) > MAX_IMAGE_SIZE) {
                sendOriginalImg = false;
            }
        }
        EMMessage message = EMMessage.createImageSendMessage(imageUri, sendOriginalImg, toChatUsername);
        sendMessage(message);
    }

    /**
     * send image message
     *
     * @param toChatUsername
     * @param imagePath
     */
    private static void sendImageMessage(String toChatUsername, String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, true, toChatUsername);
        sendMessage(message);
    }


    /**
     * send message
     *
     * @param message
     */
    private static void sendMessage(EMMessage message) {
        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD)
                        .postValue(new EaseEvent(BaseApp.getInstance().getString(R.string.has_been_send), EaseEvent.TYPE.MESSAGE));
            }

            @Override
            public void onError(int code, String error) {
                LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_SEND_ERROR).postValue(error);
            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
        // send message
        EMClient.getInstance().chatManager().sendMessage(message);

    }
}
