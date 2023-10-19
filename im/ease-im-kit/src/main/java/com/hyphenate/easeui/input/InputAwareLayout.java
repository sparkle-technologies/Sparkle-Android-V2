/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.hyphenate.easeui.input;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyphenate.easeui.input.util.ServiceUtil;

/**
 * 聊天页面  最外面的布局  继承 KeyboardAwareLinearLayout 是个线性布局   可得到键盘打开事件以及键盘的高度
 * 布局 设置 android:fitsSystemWindows="true"  当打开键盘时 会把布局顶上去 适应键盘  触发 onMeasure事件 将键盘高度传递给当前布局
 * EditText 对应输入框
 * inputView 对应三个方法   show  hide  isShowing   例如表情区域  更多区域
 * show方法 将俩个view绑定起来
 */
public class InputAwareLayout extends KeyboardAwareLinearLayout implements KeyboardAwareLinearLayout.OnKeyboardShownListener {

    private final static String TAG = "InputAwareLayout";

    private InputView current;

    public InputAwareLayout(Context context) {
        this(context, null);
    }

    public InputAwareLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputAwareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addOnKeyboardShownListener(this);
    }

    @Override
    public void onKeyboardShown() {
//        Log.e(TAG, "onKeyboardShown: " );
        hideAttachedInput(true);
    }

    public void show(@NonNull final EditText imeTarget, @NonNull final InputView input) {
        if (isKeyboardOpen()) {
            hideSoftkey(imeTarget, new Runnable() {
                @Override
                public void run() {
                    hideAttachedInput(true);
                    input.show(getKeyboardHeight(), true);
                    current = input;
                }
            });
        } else {
            if (current != null) current.hide(true);
            input.show(getKeyboardHeight(), current != null);
            current = input;
        }
    }

    public InputView getCurrentInput() {
        return current;
    }

    public void hideCurrentInput(EditText imeTarget) {
        if (isKeyboardOpen()) hideSoftkey(imeTarget, null);
        else hideAttachedInput(false);
    }

    public void hideAttachedInput(boolean instant) {
        if (current != null) current.hide(instant);
        current = null;
    }

    public boolean isInputOpen() {
        return (isKeyboardOpen() || (current != null && current.isShowing()));
    }

    public void showSoftkey(final EditText inputTarget) {
        postOnKeyboardOpen(new Runnable() {
            @Override
            public void run() {
                hideAttachedInput(true);
            }
        });
        inputTarget.post(new Runnable() {
            @Override
            public void run() {
                inputTarget.requestFocus();
                ServiceUtil.getInputMethodManager(inputTarget.getContext()).showSoftInput(inputTarget, 0);
            }
        });
    }

    public void hideSoftkey(final EditText inputTarget, @Nullable Runnable runAfterClose) {
        if (runAfterClose != null) postOnKeyboardClose(runAfterClose);

        ServiceUtil.getInputMethodManager(inputTarget.getContext())
                .hideSoftInputFromWindow(inputTarget.getWindowToken(), 0);
    }

    public interface InputView {
        void show(int height, boolean immediate);

        void hide(boolean immediate);

        boolean isShowing();
    }
}

