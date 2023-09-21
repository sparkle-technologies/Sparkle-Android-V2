package com.hyphenate.easeui.modules.chat;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatPrimaryMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatPrimaryMenu;

public class EaseChatPrimaryMenu extends RelativeLayout implements IChatPrimaryMenu, View.OnClickListener, EaseInputEditText.OnEditTextChangeListener, TextWatcher {
    private final static String TAG = EaseChatPrimaryMenu.class.getSimpleName();
    private LinearLayout rlBottom;
    private EaseInputEditText editText;
    private RelativeLayout faceLayout;
    private ImageView faceNormal;
    private ImageView faceChecked;
    private CheckBox buttonMore;
    private CheckBox buttonSend;

    private EaseChatPrimaryMenuListener listener;

    protected Activity activity;

    public EaseChatPrimaryMenu(Context context) {
        this(context, null);
    }

    public EaseChatPrimaryMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatPrimaryMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_primary_menu, this);
        activity = (Activity) context;
        initViews();
    }

    private void initViews() {
        rlBottom = findViewById(R.id.rl_bottom);
        editText = findViewById(R.id.et_sendmessage);
        faceLayout = findViewById(R.id.rl_face);
        faceNormal = findViewById(R.id.iv_face_normal);
        faceChecked = findViewById(R.id.iv_face_checked);
        buttonMore = findViewById(R.id.btn_more);
        buttonSend = findViewById(R.id.btn_send);

        showNormalStatus();

        initListener();
    }

    private void initListener() {
        buttonSend.setOnClickListener(this);
        buttonMore.setOnClickListener(this);
        faceNormal.setOnClickListener(this);
        faceChecked.setOnClickListener(this);
        editText.setOnClickListener(this);
        editText.setOnEditTextChangeListener(this);
        editText.addTextChangedListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        editText.removeTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_send) {  //发送
            if (listener != null) {
                String s = editText.getText().toString();
                editText.setText("");
                listener.onSendBtnClicked(s);
            }
        }else if (id == R.id.btn_more) {//切换到更多模式
            showMoreStatus();
        } else if (id == R.id.et_sendmessage) {//切换到文本模式
            showTextStatus();
        } else if (id == R.id.iv_face_normal) {//切换到表情模式
            showEmojiconStatus();
        }else if (id == R.id.iv_face_checked) {//切换到输入模式
            showTextStatus();
        }
    }

    @Override
    public void showNormalStatus() {
        Log.e(TAG, "showNormalStatus: " );
        showEmojOrKeyboard(true);
        checkSendButton();
        if (listener != null) {
            listener.onOutSideClicked();
        }
    }

    @Override
    public void showTextStatus() {
        Log.e(TAG, "showTextStatus: " );
        checkSendButton();
        if (listener != null) {
            listener.onToggleTextBtnClicked();
        }
    }

    @Override
    public void showEmojiconStatus() {
        Log.e(TAG, "showEmojiconStatus: " );
        showEmojOrKeyboard(faceNormal.getVisibility() == INVISIBLE);
        if (listener != null) {
            listener.onToggleEmojiconClicked();
        }
    }

    @Override
    public void showMoreStatus() {
        Log.e(TAG, "showMoreStatus: " );
        showEmojOrKeyboard(true);
        if (listener != null) {
            listener.onToggleExtendClicked(buttonMore.isChecked());
        }
    }

    @Override
    public void onEmojiconInputEvent(CharSequence emojiContent) {
        editText.append(emojiContent);
    }

    @Override
    public void onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(editText.getText())) {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            editText.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onTextInsert(CharSequence text) {
        int start = editText.getSelectionStart();
        Editable editable = editText.getEditableText();
        editable.insert(start, text);
        showTextStatus();
    }

    @Override
    public EditText getEditText() {
        return editText;
    }

    @Override
    public void onClickKeyboardSendBtn(String content) {
        if (listener != null) {
            listener.onSendBtnClicked(content);
        }
    }

    @Override
    public void onEditTextHasFocus(boolean hasFocus) {
        if (listener != null) {
            listener.onEditTextHasFocus(hasFocus);
        }
        if(hasFocus){
            editText.setHint("");
        }else{
            editText.setHint(getContext().getString(com.cyberflow.sparkle.lib_resource.R.string.send_message));
        }
    }

    private void checkSendButton() {
        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            buttonMore.setVisibility(VISIBLE);
            buttonSend.setVisibility(GONE);
        } else {
            buttonMore.setVisibility(GONE);
            buttonSend.setVisibility(VISIBLE);
        }
    }

    @Override
    public void showEmojOrKeyboard(boolean showEmoj) {
        faceNormal.setVisibility(showEmoj ? View.VISIBLE : View.INVISIBLE);
        faceChecked.setVisibility(!showEmoj ? View.VISIBLE : View.INVISIBLE);
    }


    @Override
    public void setEaseChatPrimaryMenuListener(EaseChatPrimaryMenuListener listener) {
        this.listener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.e("TAG", this.getClass().getSimpleName() + " onTextChanged s:" + s);
        checkSendButton();
        if (listener != null) {
            listener.onTyping(s, start, before, count);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.e("TAG", this.getClass().getSimpleName() + " afterTextChanged s:" + s);
    }
}

