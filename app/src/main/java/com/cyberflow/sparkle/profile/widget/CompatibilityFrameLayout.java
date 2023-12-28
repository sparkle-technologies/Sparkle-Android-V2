package com.cyberflow.sparkle.profile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class CompatibilityFrameLayout extends FrameLayout {

    private final static String TAG = "MyFrame";

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

    // 只要手指在 center 的范围内  而且scrollview 没有越过 center 就触发
    private float startY = 0f;
    private int bottom =0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
                if(startY < bottom){
                    circle.dispatchTouchEvent(ev);
                }
                break;
        }

        // 起始点在下面 再滑也不能带动
        int[] location = new int[2];
        if(bottom <= 0){
            center.getLocationOnScreen(location);
            int fy = location[1];
            bottom = fy + center.getMeasuredHeight();
        }

        Log.e(TAG, "bottom=" + bottom + " ev.getRawY()=" + ev.getRawY());

        // 手指在转盘之下
        if(ev.getRawY() > bottom){
            Log.e(TAG, "手指在转盘之下" );
            return super.dispatchTouchEvent(ev);
        }

        scrollView.getLocationOnScreen(location);
        int top = location[1];  // view距离 屏幕顶边的距离（即y轴方向）

        if(ev.getRawY() > top){
            Log.e(TAG, "手指在scrollview之下" );
            return super.dispatchTouchEvent(ev);
        }

        Log.e(TAG, " top=" + top );

        //  ev.getRawY()=513.60077 bottom=658, top=883,
        if(ev.getRawY() < bottom && ev.getRawY() < top){
            circle.dispatchTouchEvent(ev);
            Log.e(TAG, " 被拦截了" );
            return true;
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
