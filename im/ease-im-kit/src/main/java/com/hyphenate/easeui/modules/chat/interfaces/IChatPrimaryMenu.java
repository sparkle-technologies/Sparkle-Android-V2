package com.hyphenate.easeui.modules.chat.interfaces;

import android.widget.EditText;

public interface IChatPrimaryMenu {

    /**
     * 是否点击了 Hi Cora 按钮  只能取一次
     * @return
     */
    boolean isHiCoraCliked();


    /**
     * 隐藏 Hi Cora 按钮
     */
    void hideHiCoraBtn(boolean hide);

    /**
     * 清除 等待模式  可编辑或者发送
     */
    void endWaitingStatus();

    /**
     * 开始 等待模式  无法编辑或者发送
     */
    void startWaitingStatus();

    /**
     * Hi Cora 模式
     */
    void showHiCoraStatus();

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
