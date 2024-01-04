package com.cyberflow.sparkle.login.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchFrameLayout extends FrameLayout {

    private final static String TAG = "TouchFrameLayout";

    public TouchFrameLayout(Context context) {
        this(context, null);
    }

    public TouchFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean clickable = false;

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public boolean isClickable() {
        return clickable;
    }

    public interface MoveListener {
        void move();
    }

    private MoveListener moveListener;

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    private long lastClickTime = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!clickable) {
            if (moveListener != null && System.currentTimeMillis() - lastClickTime > 1000) {
                moveListener.move();
                lastClickTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
