package com.hyphenate.easeui.viewholder;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.ui.dialog.LoadingDialogHolder;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.GlideEngine;
import com.hyphenate.easeui.utils.PicSelectorHelper;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowImage;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.UriUtils;
import com.hyphenate.util.VersionUtils;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener;
import com.luck.picture.lib.utils.BitmapUtils;
import com.luck.picture.lib.utils.SdkVersionUtils;

import java.util.ArrayList;

public class EaseImageViewHolder extends EaseChatRowViewHolder {

    public EaseImageViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    public static EaseChatRowViewHolder create(ViewGroup parent,
                                               boolean isSender, MessageListItemClickListener itemClickListener) {
        return new EaseImageViewHolder(new EaseChatRowImage(parent.getContext(), isSender), itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
//        Log.e("TAG", " EaseChatRowViewHolder onBubbleClick: " );
        EMImageMessageBody imgBody = (EMImageMessageBody) message.getBody();
        if (EMClient.getInstance().getOptions().getAutodownloadThumbnail()) {
            if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED) {
                getChatRow().updateView(message);
                // retry download with click event of user
                EMClient.getInstance().chatManager().downloadThumbnail(message);
            }
        } else {
            if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING ||
                    imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED) {
                // retry download with click event of user
                EMClient.getInstance().chatManager().downloadThumbnail(message);
                getChatRow().updateView(message);
                return;
            }
        }
        if (!EaseIM.getInstance().getConfigsManager().enableSendChannelAck()) {
            //此处不再单独发送read_ack消息，改为进入聊天页面发送channel_ack
            //新消息在聊天页面的onReceiveMessage方法中，排除视频，语音和文件消息外，发送read_ack消息
            if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
                    && message.getChatType() == EMMessage.ChatType.Chat) {
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        checkImgResource(imgBody, message);
//        Log.e("TAG", "onBubbleClick: -------image------- ");
    }

    private void checkImgResource(EMImageMessageBody imgBody, EMMessage message) {
        Uri imgUri = imgBody.getLocalUri();
        //检查Uri读权限
        EaseFileUtils.takePersistableUriPermission(getContext(), imgUri);  // content://media/external/images/media/147
        Log.e("Tag", "big image uri: " + imgUri + "  exist: " + EaseFileUtils.isFileExistByUri(getContext(), imgUri));

//        LocalMedia localMedia = LocalMedia.generateHttpAsLocalMedia("https://img1.baidu.com/it/u=3541202100,4218727384&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
//        goPreview(localMedia);

        if (EaseFileUtils.isFileExistByUri(getContext(), imgUri)) {      // already download
            String imgPath = "";
            if (VersionUtils.isTargetQ(getContext())) {
                imgPath = imgUri.toString();                           // 高版本
            } else {
                imgPath = UriUtils.getFilePath(getContext(), imgUri);  // 低版本
            }
            Log.e("TAG", "onBubbleClick: imgPath=" + imgPath);
            LocalMedia localMedia = buildLocalMedia(getContext(), imgPath);
            localMedia.setForward_msg_id(message.getMsgId());
            goPreview(localMedia);
        } else {  // download first, then go preview it
//            String finename = imgBody.getFileName();
//            LocalMedia localMedia = LocalMedia.generateHttpAsLocalMedia("https://ww1.sinaimg.cn/bmiddle/bcd10523ly1g96mg4sfhag20c806wu0x.gif");
            String msgId = message.getMsgId();   // for download
            downloadImg(msgId, uri -> {
                String imgPath = UriUtils.getFilePath(getContext(), uri);
                LocalMedia localMedia = buildLocalMedia(getContext(), imgPath);
                localMedia.setForward_msg_id(msgId);
                goPreview(localMedia);
            });
        }
    }

    private LocalMedia buildLocalMedia(Context context, String absolutePath) {
        LocalMedia media = LocalMedia.generateLocalMedia(context, absolutePath);
        media.setChooseModel(SelectMimeType.TYPE_ALL);
        if (SdkVersionUtils.isQ() && !PictureMimeType.isContent(absolutePath)) {
            media.setSandboxPath(absolutePath);
        } else {
            media.setSandboxPath(null);
        }
        if (PictureMimeType.isHasImage(media.getMimeType())) {
            BitmapUtils.rotateImage(context, absolutePath);
        }
        return media;
    }

    private void goPreview(LocalMedia localMedia) {
        localMedia.setPath(SdkVersionUtils.isQ() ? localMedia.getPath() : localMedia.getRealPath());
        int pos = getChatRow().position;

        mItemClickListener.onPicturePreview(localMedia, pos);

        if(true)
            return;

        Log.e("TAG", "goPreview:  pos=" + pos);
        Log.e("TAG", "goPreview:  localMedia.path=" + localMedia.getPath());

        ArrayList<LocalMedia> list = new ArrayList<LocalMedia>(1);
        list.add(localMedia);

        AppCompatActivity act = ((AppCompatActivity) getContext());

//        RecyclerView rv =  ((EaseMessageAdapter)getAdapter()).rv;

        PictureSelector.create(act)
                .openPreview()
                .setImageEngine(GlideEngine.Companion.createGlideEngine())
                .setSelectorUIStyle(PicSelectorHelper.getSelectMainStyle(getContext()))
                .isPreviewFullScreenMode(true)
                .isVideoPauseResumePlay(true)
                .isUseSystemVideoPlayer(true)
//                .isPreviewZoomEffect(true, rv)   // Must be class androidx.recyclerview.widget.RecyclerView or class android.widget.ListView
                .setExternalPreviewEventListener(new OnExternalPreviewEventListener() {
                    @Override
                    public void onPreviewDelete(int position) {

                    }

                    @Override
                    public boolean onLongPressDownload(Context context, LocalMedia media) {
                        return false;
                    }
                }).startActivityPreview(0, true, list);
    }

    @Override
    protected void handleReceiveMessage(EMMessage message) {
        super.handleReceiveMessage(message);
        getChatRow().updateView(message);
    }

    private void downloadImg(final String msgId, DownloadCallBack dCallBack) {
        final EMMessage msg = EMClient.getInstance().chatManager().getMessage(msgId);
        final EMCallBack callback = new EMCallBack() {
            public void onSuccess() {
                Uri localUrlUri = ((EMImageMessageBody) msg.getBody()).getLocalUri();
                if(getContext()  instanceof  Activity){
                    ((Activity) getContext()).runOnUiThread(() -> {
                        LoadingDialogHolder.INSTANCE.getLoadingDialog().hide();
                    });
                }
                if (dCallBack != null) {
                    dCallBack.succeed(localUrlUri);
                }
            }

            public void onError(final int error, String message) {
                EMLog.e("TAG", "offline file transfer error:" + message);
                if(getContext()  instanceof  Activity){
                    ((Activity) getContext()).runOnUiThread(() -> {
                        LoadingDialogHolder.INSTANCE.getLoadingDialog().hide();
                    });
                }
            }

            public void onProgress(final int progress, String status) {
                EMLog.d("TAG", "Progress: " + progress);
            }
        };
        msg.setMessageStatusCallback(callback);
        LoadingDialogHolder.INSTANCE.getLoadingDialog().show(getContext());
        EMClient.getInstance().chatManager().downloadAttachment(msg);
    }

    interface DownloadCallBack {
        void succeed(Uri uri);
    }
}
