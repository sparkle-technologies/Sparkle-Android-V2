package com.hyphenate.easeui.modules.chat.interfaces;

import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.hyphenate.easeui.modules.chat.EaseInputMenuStyle;

public interface IChatPrimaryMenu {

    /**
     * 常规模式
     */
    void showNormalStatus();

    /**
     * 文本输入模式
     */
    void showTextStatus();

    /**
     * 表情输入模式
     */
    void showEmojiconStatus();

    /**
     * 更多模式
     */
    void showMoreStatus();

    void showEmojOrKeyboard(boolean showEmoj);

    /**
     * 输入表情
     *
     * @param emojiContent
     */
    void onEmojiconInputEvent(CharSequence emojiContent);

    /**
     * 删除表情
     */
    void onEmojiconDeleteEvent();

    /**
     * 输入文本
     *
     * @param text
     */
    void onTextInsert(CharSequence text);

    /**
     * 获取EditText
     *
     * @return
     */
    EditText getEditText();


    /**
     * 设置监听
     *
     * @param listener
     */
    void setEaseChatPrimaryMenuListener(EaseChatPrimaryMenuListener listener);
}
