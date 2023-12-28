package com.cyberflow.sparkle.profile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CompatibilityFrameLayout extends FrameLayout {

    private final static String TAG = "CompatibilityFrameLayout";

    public CompatibilityFrameLayout(Context context) {
        this(context, null);
    }

    public CompatibilityFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompatibilityFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private View scrollView;
    private View center;
    private View circle;

    public void setViews(View scrollView, View center, View circle) {
        this.scrollView = scrollView;
        this.center = center;
        this.circle = circle;
    }

    private View bottomView;
    private View tv;
    private int gap = 0;

    public void setTxtStrict(View bottom, View tv, int gap) {
        this.bottomView = bottom;
        this.tv = tv;
        this.gap = gap;
    }

    // 只要手指在 center 的范围内  而且scrollview 没有越过 center 就触发
    private float startY = 0f;
    private int bottom = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 起始点在下面 再滑也不能带动
        int[] location = new int[2];
        if (bottom <= 0) {
            center.getLocationOnScreen(location);
            int fy = location[1];
            bottom = fy + center.getMeasuredHeight();
        }

        scrollView.getLocationOnScreen(location);
        int top = location[1];  // view距离 屏幕顶边的距离（即y轴方向）

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                if (startY < bottom) {
                    circle.dispatchTouchEvent(ev);
                }
                break;
        }

        if (ev.getRawY() < bottom && ev.getRawY() < top) {
            circle.dispatchTouchEvent(ev);
            Log.e(TAG, " 被拦截了");
            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (ev.getRawY() - startY < 0) {
                tv.getLocationOnScreen(location);
                int tvTop = location[1];
                int tvBottom = tvTop + tv.getMeasuredHeight();
                bottomView.getLocationOnScreen(location);
                int bottomViewTop = location[1];
//                Log.e(TAG, "ACTION_MOVE   tvBottom=" + tvBottom + " gap=" + gap + ",  bottomViewTop=" + bottomViewTop);
                if ( (bottomViewTop > tvBottom) && (bottomViewTop - tvBottom > gap)) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();

        return y >= top && y <= bottom && x >= left && x <= right;
    }
}
