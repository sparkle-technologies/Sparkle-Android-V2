package com.hyphenate.easeui.modules.chat;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.input.InputAwareLayout;
import com.hyphenate.easeui.input.KeyboardHeightFrameLayout;
import com.hyphenate.easeui.modules.chat.interfaces.ChatInputMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatExtendMenuItemClickListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseChatPrimaryMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.EaseEmojiconMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatEmojiconMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatExtendMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatInputMenu;
import com.hyphenate.easeui.modules.chat.interfaces.IChatPrimaryMenu;
import com.hyphenate.easeui.utils.EaseSmileUtils;


public class EaseChatInputMenu extends LinearLayout implements
        IChatInputMenu,
        EaseChatPrimaryMenuListener, EaseEmojiconMenuListener,
        EaseChatExtendMenuItemClickListener {
    private static final String TAG = EaseChatInputMenu.class.getSimpleName();
    private LinearLayout chatMenuContainer;

    private IChatPrimaryMenu primaryMenu;
    private IChatEmojiconMenu emojiconMenu;
    private IChatExtendMenu extendMenu;
    private ChatInputMenuListener menuListener;

    protected Activity activity;

    public EaseChatInputMenu(Context context) {
        this(context, null);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseChatInputMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        activity = (Activity) context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_chat_input_menu_container, this);
    }

    EaseChatPrimaryMenu inputContainerLinearLayout;   // 输入框 表情按钮 更多按钮   
    KeyboardHeightFrameLayout emotionContainerFrameLayout;  
    EaseEmojiconMenu emotionLayout;   // 表情区域 
    KeyboardHeightFrameLayout extContainerFrameLayout;
    EaseChatExtendMenu extViewPager;   // 更多图标区域

    // 加载外部的父布局 
    public void init(InputAwareLayout rootLinearLayout) {
        this.rootLinearLayout = rootLinearLayout;
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        chatMenuContainer = findViewById(R.id.chat_menu_container);
        inputContainerLinearLayout = findViewById(R.id.inputContainerLinearLayout);
        emotionContainerFrameLayout = findViewById(R.id.emotionContainerFrameLayout);
        emotionLayout = findViewById(R.id.emotionLayout);
        extContainerFrameLayout = findViewById(R.id.extContainerFrameLayout);
        extViewPager = findViewById(R.id.extViewPager);

        primaryMenu = inputContainerLinearLayout;
        emojiconMenu = emotionLayout;
        extendMenu = extViewPager;

        primaryMenu.setEaseChatPrimaryMenuListener(this);
        emojiconMenu.setEmojiconMenuListener(this);
        extendMenu.setEaseChatExtendMenuItemClickListener(this);

        initData();
    }
    
    private void initData() {
        emotionLayout.init();
        extViewPager.init();
    }
    

    @Override
    public void setChatInputMenuListener(ChatInputMenuListener listener) {
        this.menuListener = listener;
    }

    @Override
    public IChatPrimaryMenu getPrimaryMenu() {
        return primaryMenu;
    }

    @Override
    public IChatEmojiconMenu getEmojiconMenu() {
        return emojiconMenu;
    }

    @Override
    public IChatExtendMenu getChatExtendMenu() {
        return extendMenu;
    }

    @Override
    public void hideExtendContainer() {
        Log.e(TAG, "hideExtendContainer: " );
    }

    @Override
    public void showEmojiconMenu(boolean show) {
        Log.e(TAG, "showEmojiconMenu: " );
    }

    @Override
    public void showExtendMenu(boolean show) {
        Log.e(TAG, "showExtendMenu: " );
    }

    @Override
    public void hideSoftKeyboard() {
        Log.e(TAG, "hideSoftKeyboard: " ); 
    }
    
    @Override
    public boolean onBackPressed() {
        Log.e(TAG, "onBackPressed: " );
        return true;
    }

    @Override
    public void onSendBtnClicked(String content) {
        Log.e(TAG, "onSendBtnClicked  menuListener.onSendMessage  content = " + content);
        if (menuListener != null) {
            menuListener.onSendMessage(content);
        }
    }

    @Override
    public void onTyping(CharSequence s, int start, int before, int count) {
        Log.e(TAG, "onTyping:  menuListener.onTyping  s = " + s);
        if (menuListener != null) {
            menuListener.onTyping(s, start, before, count);
        }
    }


    @Override
    public void onEditTextHasFocus(boolean hasFocus) {
        Log.e(TAG, "onEditTextHasFocus: hasFocus = " + hasFocus);
    }

    @Override
    public void onExpressionClicked(Object emojicon) {
        Log.e(TAG, "onExpressionClicked");
        if (emojicon instanceof EaseEmojicon) {
            EaseEmojicon easeEmojicon = (EaseEmojicon) emojicon;
            if (easeEmojicon.getType() != EaseEmojicon.Type.BIG_EXPRESSION) {
                if (easeEmojicon.getEmojiText() != null) {
                    primaryMenu.onEmojiconInputEvent(EaseSmileUtils.getSmiledText(getContext(), easeEmojicon.getEmojiText()));
                }
            } else {
                if (menuListener != null) {
                    menuListener.onExpressionClicked(emojicon);
                }
            }
        } else {
            if (menuListener != null) {
                menuListener.onExpressionClicked(emojicon);
            }
        }
    }

    @Override
    public void onSendImageClicked() {
        Log.e(TAG, "onSendImageClicked");
        if (menuListener != null) {
            String content = primaryMenu.getEditText().getText().toString();
            if(content == null || content.isEmpty()){
                return;
            }
            primaryMenu.getEditText().setText("");
            menuListener.onSendMessage(content);
        }
    }

    @Override
    public void onDeleteImageClicked() {
        Log.e(TAG, "onDeleteImageClicked");
        primaryMenu.onEmojiconDeleteEvent();
    }

    @Override
    public void onChatExtendMenuItemClick(int itemId, View view) {
        Log.e(TAG, "onChatExtendMenuItemClick itemId = " + itemId);
        if (menuListener != null) {
            menuListener.onChatExtendMenuItemClick(itemId, view);
        }
    }

    public void onKeyboardShown() {
        hideEmotionLayout();
    }

    public void onKeyboardHidden() {
        // do nothing
    }

    private OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener;

    public void setOnConversationInputPanelStateChangeListener(OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener) {
        this.onConversationInputPanelStateChangeListener = onConversationInputPanelStateChangeListener;
    }

    private InputAwareLayout rootLinearLayout;

    private void showEmotionLayout() {
        rootLinearLayout.show(primaryMenu.getEditText(), emotionContainerFrameLayout);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideEmotionLayout() {
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    private void showConversationExtension() {
        rootLinearLayout.show(primaryMenu.getEditText(), extContainerFrameLayout);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideConversationExtension() {
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    // 关闭键盘   按返回键  触碰消息列表空白区域
    void closeConversationInputPanel() {
        rootLinearLayout.hideAttachedInput(true);
        rootLinearLayout.hideCurrentInput(primaryMenu.getEditText());
    }


    // 点击外面外面区域  收起键盘  隐藏表情区和更多
    @Override
    public void onOutSideClicked() {
        primaryMenu.showEmojOrKeyboard(true);
        closeConversationInputPanel();
//        rootLinearLayout.hideSoftkey(primaryMenu.getEditText(), null);
//        hideConversationExtension();
    }

    // 点击输入框
    @Override
    public void onToggleTextBtnClicked() {
        Log.e(TAG, "onToggleTextBtnClicked");
        primaryMenu.showEmojOrKeyboard(true);
        hideConversationExtension();
        rootLinearLayout.showSoftkey(primaryMenu.getEditText());
    }

    // 点击更多
    @Override
    public void onToggleExtendClicked(boolean extend) {
        Log.e(TAG, "onToggleExtendClicked extend:" + extend);
        primaryMenu.showEmojOrKeyboard(true);
        if(rootLinearLayout.getCurrentInput() == extContainerFrameLayout){
            hideConversationExtension();
            rootLinearLayout.showSoftkey(primaryMenu.getEditText());
        }else{
            showConversationExtension();
        }
    }

    // 点击表情图标
    @Override
    public void onToggleEmojiconClicked() {
        Log.e(TAG, "onToggleEmojiconClicked extend:" );
        if(rootLinearLayout.getCurrentInput() == emotionContainerFrameLayout){
            primaryMenu.showEmojOrKeyboard(true);
            hideEmotionLayout();
            rootLinearLayout.showSoftkey(primaryMenu.getEditText());
        }else{
            primaryMenu.showEmojOrKeyboard(false);
            showEmotionLayout();
        }
    }

    public interface OnConversationInputPanelStateChangeListener {
        /**
         * 输入面板展开
         */
        void onInputPanelExpanded();

        /**
         * 输入面板关闭
         */
        void onInputPanelCollapsed();
    }
}

