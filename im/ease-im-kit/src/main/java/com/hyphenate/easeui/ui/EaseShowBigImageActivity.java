/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cyberflow.mimolite.common.app.BaseYXApp;
import com.google.android.gms.common.util.ScopeUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;
import com.hyphenate.util.EMLog;
import com.luck.picture.lib.utils.ToastUtils;

import java.io.File;

/**
 * download and show original image
 */
public class EaseShowBigImageActivity extends EaseBaseActivity {
    private static final String TAG = "ShowBigImage";
    private ProgressDialog pd;
    private EasePhotoView image;
    private ImageView ivSave, ivShare;
    private int default_res = R.drawable.ease_default_image;
    private String filename;
    private Bitmap bitmap = null ;
    private boolean isDownloaded;
    private Uri uri = null;


    @SuppressLint({"NewApi", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFitSystemForTheme(true, R.color.black, true);
        setContentView(R.layout.ease_activity_show_big_image);
        super.onCreate(savedInstanceState);
        image = (EasePhotoView) findViewById(R.id.image);

        ivSave = (ImageView) findViewById(R.id.iv_save);
        ivShare = (ImageView) findViewById(R.id.iv_share);
        ProgressBar loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
        default_res = getIntent().getIntExtra("default_image", R.drawable.ease_default_avatar);
        uri = getIntent().getParcelableExtra("uri");
        filename = getIntent().getExtras().getString("filename");
        String msgId = getIntent().getExtras().getString("messageId");
        EMLog.d(TAG, "show big msgId:" + msgId);

        //show the image if it exist in local path
        if (EaseFileUtils.isFileExistByUri(this, uri)) {
            Glide.with(this).asBitmap().load(uri).into(target);
        } else if (msgId != null) {
            downloadImage(msgId);
        } else {
            failDisplay();
        }
        image.setOnPhotoTapListener((view, x, y) -> {
            finish();
        });
        ivSave.setOnClickListener(v -> {
            saveImageToLocal();
        });
        ivShare.setOnClickListener(v -> {
            ToastUtils.showToast(this, "-------coming soon-------");
        });
    }

    CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            bitmap = resource;
            image.setImageBitmap(bitmap);
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {}
    };

    private void failDisplay() {
        ivSave.setVisibility(View.INVISIBLE);
        ivShare.setVisibility(View.INVISIBLE);
        image.setImageResource(default_res);
    }

    // save file to local sdcard
    private void saveImageToLocal() {
        if(bitmap == null){
            ToastUtils.showToast(this, "image not exist");
            return;
        }
        HandlerThread thread = new HandlerThread("MyHandlerThread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        Runnable runnable = () -> {
            try {
                String result = EaseFileUtils.storeImage(bitmap, BaseYXApp.instance.getApplicationContext());
                runOnUiThread(() -> {
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        handler.post(runnable);
    }


    /**
     * download image
     *
     * @param msgId
     */
    @SuppressLint("NewApi")
    private void downloadImage(final String msgId) {
        EMLog.e(TAG, "download with messageId: " + msgId);
        String str1 = getResources().getString(R.string.Download_the_pictures);
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(str1);
        pd.show();
        final EMMessage msg = EMClient.getInstance().chatManager().getMessage(msgId);
        final EMCallBack callback = new EMCallBack() {
            public void onSuccess() {
                EMLog.e(TAG, "onSuccess");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing() && !isDestroyed()) {
                            if (pd != null) {
                                pd.dismiss();
                            }
                            isDownloaded = true;
                            Uri localUrlUri = ((EMImageMessageBody) msg.getBody()).getLocalUri();
                            Glide.with(EaseShowBigImageActivity.this)
                                    .asBitmap()
                                    .load(localUrlUri)
                                    .apply(new RequestOptions().error(default_res))
                                    .into(target);
                        }
                    }
                });
            }

            public void onError(final int error, String message) {
                EMLog.e(TAG, "offline file transfer error:" + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
                            return;
                        }

                        failDisplay();
                        pd.dismiss();
                        if (error == EMError.FILE_NOT_FOUND) {
                            Toast.makeText(getApplicationContext(), R.string.Image_expired, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            public void onProgress(final int progress, String status) {
                EMLog.d(TAG, "Progress: " + progress);
                final String str2 = getResources().getString(R.string.Download_the_pictures_new);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (EaseShowBigImageActivity.this.isFinishing() || EaseShowBigImageActivity.this.isDestroyed()) {
                            return;
                        }
                        pd.setMessage(str2 + progress + "%");
                    }
                });
            }
        };

        msg.setMessageStatusCallback(callback);

        EMClient.getInstance().chatManager().downloadAttachment(msg);
    }

    @Override
    public void onBackPressed() {
        if (isDownloaded)
            setResult(RESULT_OK);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
