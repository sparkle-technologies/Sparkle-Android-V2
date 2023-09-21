package com.hyphenate.easeui.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cyberflow.mimolite.common.app.mvi.LoadingDialogHolder;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.MessageListItemClickListener;
import com.hyphenate.easeui.ui.EaseShowLocalVideoActivity;
import com.hyphenate.easeui.ui.EaseShowVideoActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVideo;
import com.hyphenate.util.EMLog;
import com.luck.picture.lib.utils.ToastUtils;

import java.io.File;

public class EaseVideoViewHolder extends EaseChatRowViewHolder {
    private static final String TAG = EaseVideoViewHolder.class.getSimpleName();

    public EaseVideoViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    public static EaseChatRowViewHolder create(ViewGroup parent,
                                               boolean isSender, MessageListItemClickListener itemClickListener) {
        return new EaseVideoViewHolder(new EaseChatRowVideo(parent.getContext(), isSender), itemClickListener);
    }

    @Override
    public void onBubbleClick(EMMessage message) {
        super.onBubbleClick(message);
        EMVideoMessageBody videoBody = (EMVideoMessageBody) message.getBody();
        EMLog.d(TAG, "video view is on click");
        if (EMClient.getInstance().getOptions().getAutodownloadThumbnail()) {

        } else {
            if (videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING ||
                    videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.FAILED) {
                // retry download with click event of user
                EMClient.getInstance().chatManager().downloadThumbnail(message);
                return;
            }
        }
        if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
                && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Log.e("TAG", "onBubbleClick: -------video------- ");
        Intent intent = new Intent(getContext(), EaseShowVideoActivity.class);
        intent.putExtra("msg", message);
//        getContext().startActivity(intent);

        checkVideoResource(message);

    }

    private void checkVideoResource(EMMessage message) {
        EMVideoMessageBody messageBody = (EMVideoMessageBody) message.getBody();

        Uri localFilePath = messageBody.getLocalUri();
        EMLog.d(TAG, "localFilePath = " + localFilePath);
        EMLog.d(TAG, "local filename = " + messageBody.getFileName());
        EMMessage.Status status = message.status();
        EMLog.d(TAG, "message status: " + status);

        //检查Uri读权限
        EaseFileUtils.takePersistableUriPermission(getContext(), localFilePath);

        if (EaseFileUtils.isFileExistByUri(getContext(), localFilePath)) {
            // go preview
            goPreview(localFilePath);
        } else {
            if (message.status() == EMMessage.Status.SUCCESS) {
                EMLog.d(TAG, "download remote video file");
                downloadVideo(message, uri -> {
                    goPreview(uri);
                });
            } else {
                EMLog.d(TAG, "video file not exist, localFilePath: " + localFilePath);
                Toast.makeText(getContext(), R.string.video_file_not_exist, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goPreview(Uri uri) {
        EaseShowLocalVideoActivity.actionStart(getContext(), uri.toString());
    }

    private void downloadVideo(final EMMessage message, EaseImageViewHolder.DownloadCallBack dCallBack) {

        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                if(getContext()  instanceof Activity){
                    ((Activity) getContext()).runOnUiThread(() -> {
                        LoadingDialogHolder.INSTANCE.getLoadingDialog().hide();
                    });
                }
                Uri localUrlUri = ((EMVideoMessageBody) message.getBody()).getLocalUri();
                if (dCallBack != null) {
                    dCallBack.succeed(localUrlUri);
                }
            }

            @Override
            public void onProgress(final int progress, String status) {
                Log.d("ease", "download video progress:" + progress);
            }

            @Override
            public void onError(final int error, String msg) {
                Log.e("###", "offline file transfer error:" + msg);
                if(getContext()  instanceof  Activity){
                    ((Activity) getContext()).runOnUiThread(() -> {
                        LoadingDialogHolder.INSTANCE.getLoadingDialog().hide();
                    });
                }
                Uri localFilePath = ((EMVideoMessageBody) message.getBody()).getLocalUri();
                String filePath = EaseFileUtils.getFilePath(getContext(), localFilePath);
                if (TextUtils.isEmpty(filePath)) {
                    getContext().getContentResolver().delete(localFilePath, null, null);
                } else {
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (error == EMError.FILE_NOT_FOUND) {
                    Toast.makeText(getContext(), R.string.Video_expired, Toast.LENGTH_SHORT).show();
                }
            }
        });
        LoadingDialogHolder.INSTANCE.getLoadingDialog().show(getContext());
        EMClient.getInstance().chatManager().downloadAttachment(message);
    }
}
