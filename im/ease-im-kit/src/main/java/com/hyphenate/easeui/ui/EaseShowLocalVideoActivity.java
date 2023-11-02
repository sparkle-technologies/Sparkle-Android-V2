package com.hyphenate.easeui.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.player.EasyVideoCallback;
import com.hyphenate.easeui.player.EasyVideoPlayer;
import com.hyphenate.easeui.ui.base.EaseBaseActivity;
import com.hyphenate.util.UriUtils;
import com.luck.picture.lib.basic.PictureMediaScannerConnection;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnCallbackListener;
import com.luck.picture.lib.utils.DownloadFileUtils;
import com.luck.picture.lib.utils.ToastUtils;

public class EaseShowLocalVideoActivity extends EaseBaseActivity implements EasyVideoCallback {

    private final static String TAG = "ShowVideoActivity";

    private EasyVideoPlayer evpPlayer;
    private Uri uri;

    public static void actionStart(Context context, String path) {
        Intent intent = new Intent(context, EaseShowLocalVideoActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setFitSystemForTheme(true, R.color.black, true);
        setContentView(R.layout.ease_activity_show_local_video);
        initIntent(getIntent());
        initView();
        initListener();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initIntent(intent);
        if (uri != null) {
            evpPlayer.setSource(uri);
        }
    }

    String path = "";

    public void initIntent(Intent intent) {
        path = intent.getStringExtra("path");
        Log.e(TAG, "initIntent: path=" + path );
        if (!TextUtils.isEmpty(path)) {
            uri = Uri.parse(path);
        }
        if (uri == null) {
            finish();
        }
    }

    private ImageView ivSave, ivShare, ivDelete;

    public void initView() {
        evpPlayer = findViewById(R.id.evp_player);
        ivSave = findViewById(R.id.iv_save);
        ivShare = findViewById(R.id.iv_share);
        ivDelete = findViewById(R.id.iv_delete);
    }

    public void initListener() {
        evpPlayer.setCallback(this);
        ivSave.setOnClickListener(v -> {
            if (uri != null) {
                saveVideo();
            }
        });
        ivShare.setOnClickListener(v -> {
            /*TheRouter.build(PageConst.IM.PAGE_IM_FORWARD)
                    .withString("forward_msg_id", msg_id)
                    .navigation();*/
        });
        ivDelete.setOnClickListener(v -> {
            finish();
        });
    }

    private String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private void saveVideo() {
        if (ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "saveVideo: got permission   ");
            realSave();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(storagePermission)) {
                    Log.e(TAG, "saveVideo: explain to user");
                    Toast.makeText(this, R.string.need_permission_to_download_video, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "saveVideo: ask user to give permission----1");
                    requestPermissionLauncher.launch(storagePermission);
                }
            } else {
                Log.e(TAG, "saveVideo: ask user to give permission----2");
                requestPermissionLauncher.launch(storagePermission);
            }
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.e(TAG, "ActivityResultLauncher  isGranted");
                    realSave();
                } else {
                    Log.e(TAG, "ActivityResultLauncher  not granted");
                    Toast.makeText(this, R.string.permission_request_failed, Toast.LENGTH_SHORT).show();
                }
            });
    private void realSave() {
        if(uri == null){
            return;
        }

//        Log.e(TAG, "realSave: isExist=" +  UriUtils.isFileExistByUri(getApplicationContext(), Uri.parse(path)) );

       /* if(UriUtils.isFileExistByUri(getApplicationContext(), Uri.parse(path))){
            ToastUtils.showToast(getApplicationContext(), getString(com.luck.picture.lib.R.string.ps_save_success));
            return;
        }*/

        String videoPath = UriUtils.getFilePath(getApplicationContext(), uri);

        /**
         * 低版本  android 9  api=28
         * path=file:///storage/emulated/0/DCIM/Camera/VID_20231009_190155.mp4
         * videoPath=/storage/emulated/0/DCIM/Camera/VID_20231009_190155.mp4
         * media.getMimeType()=video/mp4
         *
         * 高版本  android 12   api=31
         * path= content://media/external/video/media/247
         * videoPath=
         */

        Log.e(TAG, "realSave: videoPath=" +  videoPath );
        if(videoPath.isEmpty() || videoPath == null ){
            videoPath = path;
        }
        Log.e(TAG, "realSave: videoPath=" +  videoPath );
        LocalMedia media = LocalMedia.generateLocalMedia(getApplicationContext(), videoPath);
        media.setChooseModel(SelectMimeType.TYPE_VIDEO);

        Log.e(TAG, "realSave: media.getMimeType()=" +  media.getMimeType() );

        DownloadFileUtils.saveLocalFile(this, videoPath, media.getMimeType(), new OnCallbackListener<String>() {
            @Override
            public void onCall(String realPath) {
                if (TextUtils.isEmpty(realPath)) {
                    String errorMsg;
                    if (PictureMimeType.isHasAudio(media.getMimeType())) {
                        errorMsg = getString(com.luck.picture.lib.R.string.ps_save_audio_error);
                    } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                        errorMsg = getString(com.luck.picture.lib.R.string.ps_save_video_error);
                    } else {
                        errorMsg = getString(com.luck.picture.lib.R.string.ps_save_image_error);
                    }
                    ToastUtils.showToast(getApplicationContext(), errorMsg);
                } else {
                    new PictureMediaScannerConnection(getApplicationContext(), realPath);
                    ToastUtils.showToast(getApplicationContext(), getString(com.luck.picture.lib.R.string.ps_save_success) + "\n" + realPath);
                }
            }
        });
    }

    public void initData() {
        evpPlayer.setAutoPlay(true);
        if (uri != null) {
            evpPlayer.setSource(uri);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (evpPlayer != null) {
            evpPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (evpPlayer != null) {
            evpPlayer.release();
            evpPlayer = null;
        }
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        Log.e(TAG, "onStarted: ");
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
        Log.e(TAG, "onPaused: ");
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        Log.e(TAG, "onPreparing: ");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        Log.e(TAG, "onPrepared: ");
    }

    @Override
    public void onBuffering(int percent) {
        Log.e(TAG, "onBuffering: percent=" + percent);
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        Log.e(TAG, "onError: ");
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        Log.e(TAG, "onCompletion: ");
    }

    @Override
    public void onClickVideoFrame(EasyVideoPlayer player) {
        Log.e(TAG, "onClickVideoFrame: ");
    }
}

