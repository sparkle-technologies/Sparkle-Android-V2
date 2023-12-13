package com.hyphenate.easeui.modules.chat.interfaces;

import com.hyphenate.easeui.modules.menu.EaseChatFinishReason;

/**
 * 用于AIO 塔罗牌
 */
public interface OnAIOListener {

    void onChatFinish(EaseChatFinishReason reason, String id);
}
