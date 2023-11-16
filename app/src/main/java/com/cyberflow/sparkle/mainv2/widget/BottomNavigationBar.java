package com.cyberflow.sparkle.mainv2.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberflow.sparkle.R;
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.loader.AssetStreamLoader;

// https://blog.csdn.net/weixin_47884471/article/details/123619002
public class BottomNavigationBar extends LinearLayout implements View.OnClickListener {

    private Paint paint;
    private Paint borderPaint;
    private Path path;
    private float width;
    private onBottomNavClickListener listener;
    private RelativeLayout lay1, lay2, lay3, lay4, lay5;

    private ImageView img1, img2, img3, img4;
    private ImageView imgCenter;

    private View layUnread;
    private TextView tvUnread;

    public void setViewPager(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }

    private int currentPosition = 0; //上次的页面

    private PointF b;
    private PointF c;
    private PointF a;
    private PointF a2;
    private PointF b2;
    private PointF c2;
    private PointF a3;
    private PointF b3;
    private PointF c3;
    private ViewPager2 viewPager;

    public BottomNavigationBar(Context context) {
        super(context);
        init(context);
    }

    public BottomNavigationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        path = new Path();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG); //初始化油漆画笔
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(dip2px(1));

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_nav_bar, this);

        lay1 = view.findViewById(R.id.lay1);
        lay2 = view.findViewById(R.id.lay2);
        lay3 = view.findViewById(R.id.lay3);
        lay4 = view.findViewById(R.id.lay4);
        lay5 = view.findViewById(R.id.lay5);

        img1 = view.findViewById(R.id.iv1);
        img2 = view.findViewById(R.id.iv2);
        imgCenter = view.findViewById(R.id.iv3);
        img3 = view.findViewById(R.id.iv4);
        img4 = view.findViewById(R.id.iv5);

        layUnread = view.findViewById(R.id.lay_unread);
        tvUnread = view.findViewById(R.id.tv_unread);

        setWillNotDraw(false);

        //2、通过Resources获取
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels; //设备屏幕宽度

        lay1.setOnClickListener(this);
        lay2.setOnClickListener(this);
        lay3.setOnClickListener(this);
        lay4.setOnClickListener(this);
        lay5.setOnClickListener(this);

        a = new PointF(0, 0);
        b = new PointF(0, 0);
        c = new PointF(0, 0);

        a2 = new PointF(0, 0);
        b2 = new PointF(0, 0);
        c2 = new PointF(0, 0);

        a3 = new PointF(0, 0);
        b3 = new PointF(0, 0);
        c3 = new PointF(0, 0);

        initAnimation();

        setCurrentPage(0);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int high = 90;//底部导航的高度
        int marginTop = 30;//剧顶边高度
        int centerWidth = dip2px(75);
        int borderHigh = 7;

        int centerPoint = (int) width / 2;   // 360
        int startPoint = centerPoint - centerWidth / 2; // 360 - 72 = 288

        int x1 = startPoint;  // 288
        int x3 = (centerPoint + startPoint) / 2;  // (360+288)/2=
        int x2 = (x1 + x3) / 2;

        a.x = x1;
        a.y = dip2px(marginTop);
        b.x = x2;
        b.y = dip2px(marginTop);
        c.x = x3;
        c.y = dip2px(marginTop - borderHigh);

        a2.x = c.x;
        a2.y = c.y;
        b2.x = width / 2;
        b2.y = 23;   // 往大了调  可无限贴近那个猫头鹰
        c2.x = width - x3;
        c2.y = dip2px(marginTop - borderHigh);

        a3.x = c2.x;
        a3.y = c2.y;
        b3.x = width - x2;
        b3.y = dip2px(marginTop);
        c3.x = width - x1;
        c3.y = dip2px(marginTop);

        paint.setColor(Color.WHITE);
        paint.setShadowLayer(30, 0, 20, Color.parseColor("#d4d5d9"));

        //moveTo 用来移动画笔
        path.moveTo(0, dip2px(marginTop));//设置下一个轮廓线的起始点(x,y)。第一个点

        path.lineTo(a.x, a.y); //绘制到贝塞尔曲线第一个点 也就是a1点

        path.quadTo(b.x, b.y, c.x, c.y);//第左边曲线
        path.quadTo(b2.x, b2.y, c2.x, c2.y);//中间曲线
        path.quadTo(b3.x, b3.y, c3.x, c3.y);//第右边曲线

        path.lineTo(width, dip2px(marginTop)); //画线
        path.lineTo(width, dip2px(high));//画线
        path.lineTo(0, dip2px(high));

        path.close();
        canvas.drawPath(path, paint); //绘制路径 使用指定的油漆绘制指定的路径。路径将根据绘画的风格被填充或框起来。

        canvas.drawPath(path, borderPaint);

        super.onDraw(canvas);
    }


    /**
     * 根据屏幕的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lay1) {
            if (listener != null) {
                listener.onIconClick(v.getId(), 0);
            }
            setCurrentPage(0);
        } else if (v.getId() == R.id.lay2) {
            if (listener != null) {
                listener.onIconClick(v.getId(), 1);
            }
            setCurrentPage(1);
        } else if (v.getId() == R.id.lay3) {
            if (listener != null) {
                listener.onCenterIconClick();
            }
            setCurrentPage(2);
        } else if (v.getId() == R.id.lay4) {
            if (listener != null) {
                listener.onIconClick(v.getId(), 2);
            }
            setCurrentPage(3);
        } else if (v.getId() == R.id.lay5) {
            if (listener != null) {
                listener.onIconClick(v.getId(), 3);
            }
            setCurrentPage(4);
        }
        if (viewPager != null) {
            viewPager.setCurrentItem(currentPosition, false);
        }
    }

    public void setCurrentPage(int page) {
        setUnSelect(currentPosition);
        currentPosition = page;
        if (page == 0) {
            img1.setImageDrawable(adFriendOn);
            adFriendOn.start();
        } else if (page == 1) {
            img2.setImageDrawable(adFeedOn);
            adFeedOn.start();
        } else if (page == 2) {
            imgCenter.setImageDrawable(adOwlOn);
            adOwlOn.start();
        } else if (page == 3) {
            img3.setImageDrawable(adNotifyOn);
            adNotifyOn.start();
        } else if (page == 4) {
            img4.setImageDrawable(adProfileOn);
            adProfileOn.start();
        }
    }

//    private int[] normalIcon = {R.drawable.nav_friends_off, R.drawable.nav_feed_off, R.drawable.nav_eye_close, R.drawable.nav_notify_off, R.drawable.nav_profile_off};

    private void setUnSelect(int position) {
        switch (position) {
            case 0:
                img1.setImageDrawable(adFriendOff);
                adFriendOff.start();
                break;
            case 1:
                img2.setImageDrawable(adFeedOff);
                adFeedOff.start();
                break;
            case 2:
                adOwlOff.reset();
                imgCenter.setImageDrawable(adOwlOff);
                adOwlOff.start();
                break;
            case 3:
                img3.setImageDrawable(adNotifyOff);
                adNotifyOff.start();
                break;
            case 4:
                img4.setImageDrawable(adProfileOff);
                adProfileOff.start();
                break;
        }
    }

    private APNGDrawable adOwlOn;
    private APNGDrawable adFriendOn;
    private APNGDrawable adFeedOn;
    private APNGDrawable adNotifyOn;
    private APNGDrawable adProfileOn;

    private APNGDrawable adOwlOff;
    private APNGDrawable adFriendOff;
    private APNGDrawable adFeedOff;
    private APNGDrawable adNotifyOff;
    private APNGDrawable adProfileOff;


    private void initAnimation() {
        AssetStreamLoader asOwlOn = new AssetStreamLoader(getContext(), "owl_on.png");
        AssetStreamLoader asFriendOn = new AssetStreamLoader(getContext(), "friends_on.png");
        AssetStreamLoader asFeedOn = new AssetStreamLoader(getContext(), "feed_on.png");
        AssetStreamLoader asNotifyOn = new AssetStreamLoader(getContext(), "notify_on.png");
        AssetStreamLoader asProfileOn = new AssetStreamLoader(getContext(), "profile_on.png");

        AssetStreamLoader asOwlOff = new AssetStreamLoader(getContext(), "owl_off.png");
        AssetStreamLoader asFriendOff = new AssetStreamLoader(getContext(), "friends_off.png");
        AssetStreamLoader asFeedOff = new AssetStreamLoader(getContext(), "feed_off.png");
        AssetStreamLoader asNotifyOff = new AssetStreamLoader(getContext(), "notify_off.png");
        AssetStreamLoader asProfileOff = new AssetStreamLoader(getContext(), "profile_off.png");

        adOwlOn = new APNGDrawable(asOwlOn);
        adFriendOn = new APNGDrawable(asFriendOn);
        adFeedOn = new APNGDrawable(asFeedOn);
        adNotifyOn = new APNGDrawable(asNotifyOn);
        adProfileOn = new APNGDrawable(asProfileOn);

        adOwlOff = new APNGDrawable(asOwlOff);
        adFriendOff = new APNGDrawable(asFriendOff);
        adFeedOff = new APNGDrawable(asFeedOff);
        adNotifyOff = new APNGDrawable(asNotifyOff);
        adProfileOff = new APNGDrawable(asProfileOff);

        adOwlOn.setLoopLimit(1);
        adFriendOn.setLoopLimit(1);
        adFeedOn.setLoopLimit(1);
        adNotifyOn.setLoopLimit(1);
        adProfileOn.setLoopLimit(1);

        adOwlOff.setLoopLimit(1);
        adFriendOff.setLoopLimit(1);
        adFeedOff.setLoopLimit(1);
        adNotifyOff.setLoopLimit(1);
        adProfileOff.setLoopLimit(1);

    }

    public interface onBottomNavClickListener {
        void onIconClick(int viewId, int i);

        void onCenterIconClick();
    }

    public void setOnListener(onBottomNavClickListener listener) {
        this.listener = listener;
    }

    public void setNum(int num) {
        if (num > 0) {
            layUnread.setVisibility(View.VISIBLE);
            tvUnread.setText(String.valueOf(num));
        } else {
            layUnread.setVisibility(View.INVISIBLE);
        }
    }
}
