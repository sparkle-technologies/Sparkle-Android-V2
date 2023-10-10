package com.cyberflow.sparkle.chat.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.cyberflow.base.BaseApp;
import com.cyberflow.base.act.BaseVBAct;
import com.cyberflow.base.util.bus.LiveDataBus;
import com.cyberflow.sparkle.chat.DemoHelper;
import com.cyberflow.sparkle.chat.R;
import com.cyberflow.sparkle.chat.common.constant.DemoConstant;
import com.cyberflow.sparkle.chat.common.model.EmojiconExampleGroupData;
import com.cyberflow.sparkle.chat.common.utils.RecyclerViewUtils;
import com.cyberflow.sparkle.chat.ui.dialog.DeleteMsgDialog;
import com.cyberflow.sparkle.chat.ui.dialog.DemoDialogFragment;
import com.cyberflow.sparkle.chat.ui.dialog.DemoListDialogFragment;
import com.cyberflow.sparkle.chat.ui.dialog.LabelEditDialogFragment;
import com.cyberflow.sparkle.chat.ui.dialog.SimpleDialogFragment;
import com.cyberflow.sparkle.chat.viewmodel.MessageViewModel;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCustomMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.modules.chat.EaseChatExtendMenu;
import com.hyphenate.easeui.modules.chat.EaseChatFragment;
import com.hyphenate.easeui.modules.chat.EaseChatInputMenu;
import com.hyphenate.easeui.modules.chat.EaseChatMessageListLayout;
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu;
import com.hyphenate.easeui.modules.chat.interfaces.OnRecallMessageResultListener;
import com.hyphenate.easeui.modules.menu.EasePopupWindowHelper;
import com.hyphenate.easeui.modules.menu.MenuItemBean;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.easeui.utils.GlideEngine;
import com.hyphenate.easeui.utils.PicSelectorHelper;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.luck.lib.camerax.CameraImageEngine;
import com.luck.lib.camerax.SimpleCameraX;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.config.SelectModeConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnCameraInterceptListener;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.luck.picture.lib.permissions.PermissionUtil;
import com.luck.picture.lib.utils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class ChatFragment extends EaseChatFragment implements OnRecallMessageResultListener, EasyPermissions.PermissionCallbacks {
    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_USER_CARD = 20;
    private static final int REQUEST_CODE_CAMERA = 110;
    private static final int REQUEST_CODE_STORAGE_PICTURE = 111;
    private static final int REQUEST_CODE_STORAGE_VIDEO = 112;
    private static final int REQUEST_CODE_STORAGE_FILE = 113;
    private static final int REQUEST_CODE_LOCATION = 114;
    private static final int REQUEST_CODE_VOICE = 115;
    private MessageViewModel viewModel;
    protected ClipboardManager clipboard;

    private static final int REQUEST_CODE_SELECT_AT_USER = 15;
    private static final String[] labels = {
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_politics),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_yellow_related),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_advertisement),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_abuse),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_violent),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_contraband),
            BaseApp.getInstance().getApplicationContext().getString(R.string.tab_other),
    };
    private OnFragmentInfoListener infoListener;
    private Dialog dialog;
    private boolean isFirstMeasure = true;

    @Override
    public void initView() {
        super.initView();
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        EaseChatMessageListLayout messageListLayout = chatLayout.getChatMessageListLayout();
        //设置聊天列表背景
        messageListLayout.setBackground(new ColorDrawable(Color.parseColor("#F1F1FB")));
        setSwindleLayoutInChatFragemntHead();
        //设置是否显示昵称
        messageListLayout.showNickname(true);

        //设置默认头像
        //messageListLayout.setAvatarDefaultSrc(ContextCompat.getDrawable(mContext, R.drawable.ease_default_avatar));
        //设置头像形状
        //messageListLayout.setAvatarShapeType(1);
        //设置文本字体大小
        messageListLayout.setItemTextSize((int) EaseCommonUtils.sp2px(mContext, 18));
        //设置文本字体颜色
//        messageListLayout.setItemTextColor(ContextCompat.getColor(mContext, R.color.red));
        //设置时间线的背景
        //messageListLayout.setTimeBackground(ContextCompat.getDrawable(mContext, R.color.gray_normal));
        //设置时间线的文本大小
        //messageListLayout.setTimeTextSize((int) EaseCommonUtils.sp2px(mContext, 18));
        //设置时间线的文本颜色
        //messageListLayout.setTimeTextColor(ContextCompat.getColor(mContext, R.color.black));
        //设置聊天列表样式：两侧及均位于左侧
        //messageListLayout.setItemShowType(EaseChatMessageListLayout.ShowType.LEFT);

        //获取到菜单输入父控件
        EaseChatInputMenu chatInputMenu = chatLayout.getChatInputMenu();
        //获取到菜单输入控件
        chatLayout.setTargetLanguageCode(DemoHelper.getInstance().getModel().getTargetLanguage());

        initAnima();
        initCustomView();
    }


    private void setSwindleLayoutInChatFragemntHead() {
        EaseChatMessageListLayout messageListLayout = chatLayout.getChatMessageListLayout();
//        RelativeLayout listLayoutParent = (RelativeLayout) (messageListLayout.getParent());

        //  dont need header view anymore, keep it for the future
//        View view = LayoutInflater.from(mContext).inflate(R.layout.demo_chat_swindle, listLayoutParent, false);
//        listLayoutParent.addView(view);
      /*  listLayoutParent.post(new Runnable() {
            @Override
            public void run() {
                messageListLayout.setPadding(0, view.getMeasuredHeight(), 0, 0);
            }
        });*/

    }

    private void addItemMenuAction() {
        MenuItemBean itemMenu = new MenuItemBean(0, R.id.action_chat_forward, 11, getString(R.string.action_forward));
        itemMenu.setResourceId(com.hyphenate.easeui.R.drawable.ease_chat_item_menu_forward);
        chatLayout.addItemMenu(itemMenu);
//        MenuItemBean itemMenu1 = new MenuItemBean(0, com.hyphenate.easeui.R.id.action_chat_label, 12, getString(R.string.action_report_label));
//        itemMenu1.setResourceId(R.drawable.d_exclamationmark_in_triangle);
//        chatLayout.addItemMenu(itemMenu1);
//        chatLayout.setReportYourSelf(false);
    }

    private void resetChatExtendMenu() {
        IChatExtendMenu chatExtendMenu = chatLayout.getChatInputMenu().getChatExtendMenu();
        chatExtendMenu.clear();
        // seven menu in total, include camera, library, send token, send nft, daily horoscope, horoscope, compatibility
        for (int i = 0; i < EaseChatExtendMenu.itemdrawables.length; i++) {
            chatExtendMenu.registerMenuItem(EaseChatExtendMenu.itemStrings[i], EaseChatExtendMenu.itemdrawables[i], EaseChatExtendMenu.itemIds[i]);
        }

        //添加扩展表情
        chatLayout.getChatInputMenu().getEmojiconMenu().addEmojiconGroup(EmojiconExampleGroupData.getData());
    }

    @Override
    public void initListener() {
        super.initListener();
        chatLayout.setOnRecallMessageResultListener(this);
        listenerRecyclerViewItemFinishLayout();
    }

    @Override
    public void initData() {
        super.initData();
        resetChatExtendMenu();
        addItemMenuAction();

        chatLayout.getChatInputMenu().getPrimaryMenu().getEditText().setText(getUnSendMsg());
        chatLayout.turnOnTypingMonitor(DemoHelper.getInstance().getModel().isShowMsgTyping());

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));

        LiveDataBus.get().with(DemoConstant.MESSAGE_CALL_SAVE, Boolean.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event) {
                chatLayout.getChatMessageListLayout().refreshToLatest();
            }
        });

        LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isMessageChange()) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });

        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isMessageChange()) {
                chatLayout.getChatMessageListLayout().refreshToLatest();
            }
        });
        LiveDataBus.get().with(DemoConstant.CONVERSATION_READ, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isMessageChange()) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });

        //更新用户属性刷新列表
        LiveDataBus.get().with(DemoConstant.CONTACT_ADD, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event != null) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });

        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event != null) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });

        LiveDataBus.get().with(DemoConstant.GROUP_MEMBER_ATTRIBUTE_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event != null) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });
    }

    private void listenerRecyclerViewItemFinishLayout() {
        if (chatLayout == null || chatType != EaseConstant.CHATTYPE_GROUP) {
            return;
        }
        EaseChatMessageListLayout chatMessageListLayout = chatLayout.getChatMessageListLayout();
        if (chatMessageListLayout == null || chatMessageListLayout.getChildCount() <= 0) {
            return;
        }
        View swipeView = chatMessageListLayout.getChildAt(0);
        if (!(swipeView instanceof SwipeRefreshLayout)) {
            return;
        }
        if (((SwipeRefreshLayout) swipeView).getChildCount() <= 0) {
            return;
        }
        RecyclerView recyclerView = null;
        for (int i = 0; i < ((SwipeRefreshLayout) swipeView).getChildCount(); i++) {
            View child = ((SwipeRefreshLayout) swipeView).getChildAt(i);
            if (child instanceof RecyclerView) {
                recyclerView = (RecyclerView) child;
                break;
            }
        }
        if (recyclerView == null || chatMessageListLayout.getMessageAdapter() == null) {
            return;
        }
        EaseMessageAdapter messageAdapter = chatMessageListLayout.getMessageAdapter();
        RecyclerView finalRecyclerView = recyclerView;
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (isFirstMeasure && finalRecyclerView.getLayoutManager() != null && messageAdapter.getData() != null
                    && ((LinearLayoutManager) finalRecyclerView.getLayoutManager()).findLastVisibleItemPosition() == messageAdapter.getData().size() - 1) {
                isFirstMeasure = false;
                int[] positionArray = RecyclerViewUtils.rangeMeasurement(finalRecyclerView);

            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int[] positionArray = RecyclerViewUtils.rangeMeasurement(recyclerView);
                }
            }
        });
    }


    private void showDeliveryDialog() {

    }

    // 视频通话
    private void showSelectDialog() {

    }

    @Override
    public void onUserAvatarClick(String username) {
        if (!TextUtils.equals(username, DemoHelper.getInstance().getCurrentUser())) {
            EaseUser user = DemoHelper.getInstance().getUserInfo(username);
            if (user == null) {
                user = new EaseUser(username);
            }
            boolean isFriend = DemoHelper.getInstance().getModel().isContact(username);
            if (isFriend) {
                user.setContact(0);
            } else {
                user.setContact(3);
            }

            // 跳到
//            ContactDetailActivity.actionStart(mContext, user);
        } else {
            // 跳到个人详情页
//            UserDetailActivity.actionStart(mContext, null, null);
        }
    }

    @Override
    public void onUserAvatarLongClick(String username) {

    }

    @Override
    public boolean onBubbleLongClick(View v, EMMessage message) {
        return true;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!chatLayout.getChatMessageListLayout().isGroupChat()) {
            return;
        }
        if (count == 1 && "@".equals(String.valueOf(s.charAt(start)))) {
//            PickAtUserActivity.actionStartForResult(ChatFragment.this, conversationId, REQUEST_CODE_SELECT_AT_USER);
        }
    }

    @Override
    public boolean onBubbleClick(EMMessage message) {
        return false;
    }

    private Animation upAnima;
    private Animation downAnima;

    private void initAnima() {
        upAnima = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        downAnima = AnimationUtils.loadAnimation(mContext, R.anim.bottom_down);
    }

    private void initCustomView() {
        findViewById(com.hyphenate.easeui.R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomView(false);
            }
        });
    }

    TextView tvAmount;

    private void showCustomView(boolean isShow) {
        sendTokenLayout.startAnimation(isShow ? upAnima : downAnima);
        sendTokenLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);

        ImageView ivAvatar = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.iv_avatar);
        TextView tvReceiverName = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_receiver_name);
        TextView tvReceiverAddress = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_receiver_address);
        tvAmount = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_amount);
        ImageView ivClearAmount = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.iv_clear_amount);
        View bgSelectedToken = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.bg_selected_token);
        ImageView ivTokenIcon = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.iv_token_icon);
        TextView tvTokenName = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_token_name);
        TextView tvTokenBalance = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_token_balance);

        EditText etAddMessage = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.et_add_message);




        /*TextView tvNum_1 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_1);
        TextView tvNum_2 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_2);
        TextView tvNum_3 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_3);
        TextView tvNum_4 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_4);
        TextView tvNum_5 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_5);
        TextView tvNum_6 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_6);
        TextView tvNum_7 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_7);
        TextView tvNum_8 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_8);
        TextView tvNum_9 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_9);
        TextView tvNum_0 = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_0);
        TextView tvNum_dot = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_num_dot);
        ImageView iv_delete = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.iv_delete);
        TextView tvTransfer = sendTokenLayout.findViewById(com.hyphenate.easeui.R.id.tv_transfer);

        // show receiver info like: avatar, name, address
        // show selected token icon, name, balance
        // input transfer amount via keyboard numbers
        // todo : need integrate with third-party wallet sdk like uniPass

        tvNum_1.setOnClickListener(v -> {
            input("1");
        });
        tvNum_2.setOnClickListener(v -> {
            input("2");
        });
        tvNum_3.setOnClickListener(v -> {
            input("3");
        });
        tvNum_4.setOnClickListener(v -> {
            input("4");
        });
        tvNum_5.setOnClickListener(v -> {
            input("5");
        });
        tvNum_6.setOnClickListener(v -> {
            input("6");
        });
        tvNum_7.setOnClickListener(v -> {
            input("7");
        });
        tvNum_8.setOnClickListener(v -> {
            input("8");
        });
        tvNum_9.setOnClickListener(v -> {
            input("9");
        });
        tvNum_0.setOnClickListener(v -> {
            input("0");
        });
        tvNum_dot.setOnClickListener(v -> {
            input(".");
        });
        iv_delete.setOnClickListener(v -> {
            delete();
        });*/


    }

    private String inputNums = "";
    private final static int MAX_AMOUNT = 100000;

    DecimalFormat df = new DecimalFormat("#0.00");

    /**
     * max number 100,000.00
     * format like  23 to 23.00   or   1234.34 to 1,234.34
     *
     * @param s like 1234567890.
     */
    private void input(String s) {
        if (s.equals(".") && inputNums.contains(".")) {
            ToastUtils.showToast(mContext, "input error: can not input decimal point");
            return;
        }
        if (inputNums.contains(".")) {
            int idx = inputNums.indexOf(".");
            if ((inputNums.length() - 1) - idx >= 2) {  // 123.22  idx=3  len=6
                ToastUtils.showToast(mContext, "input error: can not input more decimal numbers");
                return;
            }
        }
        String tmp = inputNums + s;
        try {
            double d = Double.parseDouble(tmp);
            if (d > MAX_AMOUNT) {
                ToastUtils.showToast(mContext, "exceed maximum amount 100,000");
                return;
            }
            String real = df.format(d);
            Log.e(TAG, "input: " + real);
            inputNums += s;
            tvAmount.setText(inputNums.isEmpty() ? "0" : inputNums);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete() {
        if (inputNums.length() > 1) {
            inputNums = inputNums.substring(0, inputNums.length() - 1);
        } else {
            inputNums = "";
        }
        tvAmount.setText(inputNums.isEmpty() ? "0" : inputNums);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onChatExtendMenuItemClick(View view, int itemId) {
        if (itemId == EaseChatExtendMenu.itemIds[0]) {    // camera + record video
            if (checkIfHasPermissions(Manifest.permission.CAMERA, REQUEST_CODE_CAMERA)) {
                takePictureOrRecordVideo();
            }
        }
        if (itemId == EaseChatExtendMenu.itemIds[1]) {   // gallery =  image + video
            selectPictureOrVideoFromGallery();
        }
        if (itemId == EaseChatExtendMenu.itemIds[2]) {
            showCustomView(true);
        }
        if (itemId == EaseChatExtendMenu.itemIds[3]) { // todo  send nft

        }
        if (itemId == EaseChatExtendMenu.itemIds[4]) {  // todo  daily horoscope

        }
        if (itemId == EaseChatExtendMenu.itemIds[5]) { // todo horoscope

        }
        if (itemId == EaseChatExtendMenu.itemIds[6]) {  // todo  compatibility

        }
    }

    private void takePictureOrRecordVideo() {
        PictureSelector.create(this)
                .openCamera(SelectMimeType.ofAll())
                .setCameraInterceptListener(getCustomCameraEvent())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                       /* Log.e(TAG, "onResult: ");
                        for (int i = 0; i < result.size(); i++) {
                            Log.e(TAG, "onResult: " + result.get(i).getPath());
                        }*/
                        if(result.isEmpty()){
                            return;
                        }
                        LocalMedia select = result.get(0);
                        Uri uri = Uri.parse(select.getPath());
                        if (PictureMimeType.isHasVideo(select.getMimeType())) {
                            MediaPlayer mediaPlayer = new MediaPlayer();
                            try {
                                mediaPlayer.setDataSource(mContext, uri);
                                mediaPlayer.prepare();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int duration = mediaPlayer.getDuration() / 1000;
                            EMLog.d(TAG, "path = " + uri.getPath() + ",duration=" + duration);
                            EaseFileUtils.saveUriPermission(mContext, uri, null);
                            chatLayout.sendVideoMessage(uri, duration);
                        }

                        if (PictureMimeType.isHasImage(select.getMimeType())) {
                            Uri restoreImageUri = ImageUtils.checkDegreeAndRestoreImage(mContext, uri);
                            chatLayout.sendImageMessage(restoreImageUri);
                        }
                    }

                    @Override
                    public void onCancel() {
                        Log.e(TAG, "onCancel: ");
                    }
                });
    }

    private void selectPictureOrVideoFromGallery() {
        PictureSelector.create(getActivity()).openGallery(SelectMimeType.ofAll())
                .setSelectorUIStyle(PicSelectorHelper.getSelectMainStyle(getActivity()))
                .setImageEngine(GlideEngine.Companion.createGlideEngine())
                .isDisplayCamera(false)
                .setSelectionMode(SelectModeConfig.SINGLE)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(ArrayList<LocalMedia> result) {
                        if (result != null && result.size() > 0) {
                            LocalMedia select = result.get(0);

                            Log.e(TAG, "path: " + select.getPath());
                            Log.e(TAG, "getAvailablePath: " + select.getAvailablePath());
                            Log.e(TAG, "getRealPath: " + select.getRealPath());

                            String currentEditPath = select.getAvailablePath();
                            Uri mapped = PictureMimeType.isContent(currentEditPath) ? Uri.parse(currentEditPath) : Uri.fromFile(new File(currentEditPath));

                            Log.e(TAG, "onResult:  finally " + mapped.getPath());
                            if (PictureMimeType.isHasImage(select.getMimeType())) {  // image logic
                                Uri selectedImage = mapped;
                                if (selectedImage != null) {
                                    String filePath = EaseFileUtils.getFilePath(mContext, selectedImage);
                                    if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                                        chatLayout.sendImageMessage(Uri.parse(filePath));
                                    } else {
                                        EaseFileUtils.saveUriPermission(mContext, selectedImage, null);
                                        chatLayout.sendImageMessage(selectedImage);
                                    }
                                }
                            } else if (PictureMimeType.isHasVideo(select.getMimeType())) {   // video logic
                                MediaPlayer mediaPlayer = new MediaPlayer();
                                try {
                                    mediaPlayer.setDataSource(mContext, mapped);
                                    mediaPlayer.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                int duration = mediaPlayer.getDuration() / 1000;
                                EaseFileUtils.saveUriPermission(mContext, mapped, null);
                                Log.e(TAG, "onResult: path=" + mapped.getPath() +"\t duration=" + duration);
                                chatLayout.sendVideoMessage(mapped, duration);
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }


    private OnCameraInterceptListener getCustomCameraEvent() {
        return new MeOnCameraInterceptListener();
    }

    /**
     * 自定义拍照
     */
    private class MeOnCameraInterceptListener implements OnCameraInterceptListener {

        @Override
        public void openCamera(Fragment fragment, int cameraMode, int requestCode) {
            SimpleCameraX camera = SimpleCameraX.of();
            camera.isAutoRotation(true);
            camera.setCameraMode(cameraMode);
            camera.setVideoFrameRate(25);
            camera.setVideoBitRate(3 * 1024 * 1024);
            camera.isDisplayRecordChangeTime(true);
            camera.isManualFocusCameraPreview(false);
            camera.isZoomCameraPreview(true);
//            camera.setOutputPathDir(getSandboxCameraOutputPath());
            camera.setImageEngine(new CameraImageEngine() {
                @Override
                public void loadImage(Context context, String url, ImageView imageView) {
                    Glide.with(context).load(url).into(imageView);
                }
            });
            camera.start(fragment.requireActivity(), fragment, requestCode);
        }
    }

    @Override
    public void onChatError(int code, String errorMsg) {
        if (infoListener != null) {
            infoListener.onChatError(code, errorMsg);
        }
    }

    @Override
    public void onOtherTyping(String action) {
        if (infoListener != null) {
            infoListener.onOtherTyping(action);
        }
    }

    @Override
    public boolean onRecordTouch(View v, MotionEvent event) {
        if (!checkIfHasPermissions(Manifest.permission.RECORD_AUDIO, REQUEST_CODE_VOICE)) {
            return false;
        }
        return super.onRecordTouch(v, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SELECT_AT_USER:
                    if (data != null) {
                        String username = data.getStringExtra("username");
                        chatLayout.inputAtUsername(username, false);
                    }
                    break;
                case REQUEST_CODE_SELECT_USER_CARD:
                    if (data != null) {
                        EaseUser user = (EaseUser) data.getSerializableExtra("user");
                        if (user != null) {
                            sendUserCardMessage(user);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Send user card message
     *
     * @param user
     */
    private void sendUserCardMessage(EaseUser user) {
        EMMessage message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM);
        EMCustomMessageBody body = new EMCustomMessageBody(DemoConstant.USER_CARD_EVENT);
        Map<String, String> params = new HashMap<>();
        params.put(DemoConstant.USER_CARD_ID, user.getUsername());
        params.put(DemoConstant.USER_CARD_NICK, user.getNickname());
        params.put(DemoConstant.USER_CARD_AVATAR, user.getAvatar());
        body.setParams(params);
        message.setBody(body);
        message.setTo(conversationId);
        chatLayout.sendMessage(message);
    }

    @Override
    public void onStop() {
        super.onStop();
        //保存未发送的文本消息内容
        if (mContext != null && mContext.isFinishing()) {
            if (chatLayout.getChatInputMenu() != null) {
                saveUnSendMsg(chatLayout.getInputContent());
                LiveDataBus.get().with(DemoConstant.MESSAGE_NOT_SEND).postValue(true);
            }
        }
    }

    /**
     * 保存未发送的文本消息内容
     *
     * @param content
     */
    private void saveUnSendMsg(String content) {
        DemoHelper.getInstance().getModel().saveUnSendMsg(conversationId, content);
    }

    private String getUnSendMsg() {
        return DemoHelper.getInstance().getModel().getUnSendMsg(conversationId);
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, EMMessage message, View v) {
        //默认两分钟后，即不可撤回
        if (System.currentTimeMillis() - message.getMsgTime() > 2 * 60 * 1000) {
            helper.findItemVisible(com.hyphenate.easeui.R.id.action_chat_recall, false);
        }else{
            helper.findItemVisible(com.hyphenate.easeui.R.id.action_chat_delete, false);
        }
        EMMessage.Type type = message.getType();
        helper.findItemVisible(R.id.action_chat_forward, false);
        switch (type) {
            case TXT:
                if (!message.getBooleanAttribute(DemoConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false)
                        && !message.getBooleanAttribute(DemoConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    helper.findItemVisible(R.id.action_chat_forward, true);
                }
                if (v.getId() == com.hyphenate.easeui.R.id.subBubble) {
                    helper.findItemVisible(R.id.action_chat_forward, false);
                }
                break;
            case IMAGE:
                helper.findItemVisible(R.id.action_chat_forward, true);
                break;
        }

        if (chatType == DemoConstant.CHATTYPE_CHATROOM) {
            helper.findItemVisible(R.id.action_chat_forward, true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, EMMessage message) {
        if (item.getItemId() == R.id.action_chat_forward) {
//            ForwardMessageActivity.actionStart(mContext, message.getMsgId());
            return true;
        } else if (item.getItemId() == com.hyphenate.easeui.R.id.action_chat_delete) {
            showDeleteDialog(message);
            return true;
        } else if (item.getItemId() == com.hyphenate.easeui.R.id.action_chat_recall) {
            showProgressBar();
            chatLayout.recallMessage(message);
            return true;
        } else if (item.getItemId() == com.hyphenate.easeui.R.id.action_chat_reTranslate) {
            new AlertDialog.Builder(getContext())
                    .setTitle(mContext.getString(R.string.using_translate))
                    .setMessage(mContext.getString(R.string.retranslate_prompt))
                    .setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            chatLayout.translateMessage(message, false);
                        }
                    }).show();
            return true;
        } else if (item.getItemId() == com.hyphenate.easeui.R.id.action_chat_label) {
            showLabelDialog(message);
            return true;
        }
        return false;
    }

    private void showProgressBar() {
        View view = View.inflate(mContext, R.layout.demo_layout_progress_recall, null);
        dialog = new Dialog(mContext, R.style.dialog_recall);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(view, layoutParams);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void showDeleteDialog(EMMessage message) {
        DeleteMsgDialog dialog = new DeleteMsgDialog();
        dialog.setOnItemClickListener((v, position) -> chatLayout.deleteMessage(message));
        dialog.show(getChildFragmentManager(), "DeleteMsgDialog");
    }

    private void showLabelDialog(EMMessage message) {
        new DemoListDialogFragment.Builder((BaseVBAct) mContext)
                .setData(labels)
                .setCancelColorRes(R.color.black)
                .setWindowAnimations(R.style.animate_dialog)
                .setOnItemClickListener(new DemoListDialogFragment.OnDialogItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        showLabelDialog(message, labels[position]);
                    }
                })
                .show();
    }

    private void showLabelDialog(EMMessage message, String label) {
        new LabelEditDialogFragment.Builder((BaseVBAct) mContext)
                .setOnConfirmClickListener(new LabelEditDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirm(View view, String reason) {
                        EMLog.e("ReportMessage：", "msgId: " + message.getMsgId() + "label: " + label + " reason: " + reason);
                        new SimpleDialogFragment.Builder((BaseVBAct) mContext)
                                .setTitle(getString(R.string.report_delete_title))
                                .setConfirmColor(R.color.em_color_brand)
                                .setOnConfirmClickListener(getString(R.string.confirm), new DemoDialogFragment.OnConfirmClickListener() {
                                    @Override
                                    public void onConfirmClick(View view) {
                                        EMClient.getInstance().chatManager().asyncReportMessage(message.getMsgId(), label, reason, new EMCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                EMLog.e("ReportMessage：", "onSuccess 举报成功");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), "举报成功", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onError(int code, String error) {
                                                EMLog.e("ReportMessage：", "onError 举报失败: code " + code + "  : " + error);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(), "举报失败： code: " + code + " desc: " + error, Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onProgress(int progress, String status) {

                                            }
                                        });
                                    }
                                })
                                .showCancelButton(true)
                                .show();
                    }
                }).show();
    }

    public void setOnFragmentInfoListener(OnFragmentInfoListener listener) {
        this.infoListener = listener;
    }

    @Override
    public void recallSuccess(EMMessage message) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void recallFail(int code, String errorMsg) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public interface OnFragmentInfoListener {
        void onChatError(int code, String errorMsg);

        void onOtherTyping(String action);
    }

    @Override
    public void translateMessageFail(EMMessage message, int code, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(mContext.getString(R.string.unable_translate))
                .setMessage(error + ".")
                .setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private boolean checkIfHasPermissions(String permission, int requestCode) {
        if (!EasyPermissions.hasPermissions(mContext, permission)) {
            String rationale = "";
            if (requestCode == REQUEST_CODE_CAMERA) {
                rationale = getString(R.string.demo_chat_request_camera_permission);
            } else if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                rationale = getString(R.string.demo_chat_request_read_external_storage_permission, getString(R.string.demo_chat_photo));
            } else if (requestCode == REQUEST_CODE_STORAGE_VIDEO) {
                rationale = getString(R.string.demo_chat_request_read_external_storage_permission, getString(R.string.demo_chat_video));
            } else if (requestCode == REQUEST_CODE_STORAGE_FILE) {
                rationale = getString(R.string.demo_chat_request_read_external_storage_permission, getString(R.string.demo_chat_file));
            } else if (requestCode == REQUEST_CODE_LOCATION) {
                rationale = getString(R.string.demo_chat_request_location_permission);
            } else if (requestCode == REQUEST_CODE_VOICE) {
                rationale = getString(R.string.demo_chat_request_audio_permission);
            }
            EasyPermissions.requestPermissions(this, rationale, requestCode, permission);
            return false;
        }
        return true;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            takePictureOrRecordVideo();
        } else if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
            selectPicFromLocal();
        } else if (requestCode == REQUEST_CODE_STORAGE_VIDEO) {
            selectVideoFromLocal();
        } else if (requestCode == REQUEST_CODE_STORAGE_FILE) {
            selectFileFromLocal();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                Toast.makeText(getContext(), getString(R.string.demo_chat_request_camera_permission), Toast.LENGTH_LONG).show();
                PermissionUtil.goIntentSetting(this, requestCode);
                break;
            case REQUEST_CODE_STORAGE_PICTURE:
                break;
        }
    }
}