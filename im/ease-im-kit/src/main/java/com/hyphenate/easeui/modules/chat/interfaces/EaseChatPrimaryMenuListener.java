package com.hyphenate.easeui.modules.chat.interfaces;

public interface EaseChatPrimaryMenuListener {

    void onOutSideClicked();

    /**
     * when send button clicked
     *
     * @param content
     */
    void onSendBtnClicked(String content);

    /**
     * when typing on the edit-text layout.
     */
    void onTyping(CharSequence s, int start, int before, int count);

    /**
     * toggle on/off text button
     */
    void onToggleTextBtnClicked();

    /**
     * toggle on/off extend menu
     *
     * @param extend
     */
    void onToggleExtendClicked(boolean extend);

    /**
     * toggle on/off emoji icon
     *
     */
    void onToggleEmojiconClicked();


    /**
     * if edit text has focus
     */
    void onEditTextHasFocus(boolean hasFocus);

}