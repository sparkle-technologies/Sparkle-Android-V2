package com.hyphenate.easeui.widget.emojicon;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.card.MaterialCardView;
import com.hyphenate.easeui.R;

import java.util.ArrayList;
import java.util.List;

public class EaseEmojiconScrollTabBar extends RelativeLayout {

    private Context context;
    private HorizontalScrollView scrollView;
    private LinearLayout tabContainer;

    private List<MaterialCardView> tabList = new ArrayList<MaterialCardView>();
    private EaseScrollTabBarItemClickListener itemClickListener;

    public EaseEmojiconScrollTabBar(Context context) {
        this(context, null);
    }

    public EaseEmojiconScrollTabBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public EaseEmojiconScrollTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.ease_widget_emojicon_tab_bar, this);

        scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);
        tabContainer = (LinearLayout) findViewById(R.id.tab_container);
    }

    /**
     * add tab
     *
     * @param icon
     */
    public void addTab(int icon) {
        View tabView = View.inflate(context, R.layout.ease_scroll_tab_item, null);
        ImageView imageView = (ImageView) tabView.findViewById(R.id.iv_icon);
        imageView.setImageResource(icon);
//        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(DensityUtil.dip2px(context, tabWidth), LayoutParams.MATCH_PARENT);
//        imageView.setLayoutParams(imgParams);

        MaterialCardView cardview = (MaterialCardView) tabView.findViewById(R.id.cardview);
        tabContainer.addView(tabView);
        tabList.add(cardview);
        final int position = tabList.size() - 1;
        imageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
    }

    /**
     * remove tab
     *
     * @param position
     */
    public void removeTab(int position) {
        tabContainer.removeViewAt(position);
        tabList.remove(position);
    }

    public void selectedTo(int position) {
        scrollTo(position);
        for (int i = 0; i < tabList.size(); i++) {
            if (position == i) {
                tabList.get(i).setStrokeColor(ContextCompat.getColor(context, R.color.black));
            } else {
                tabList.get(i).setStrokeColor(ContextCompat.getColor(context, R.color.transparent));
            }
        }
    }

    private void scrollTo(final int position) {
        int childCount = tabContainer.getChildCount();
        if (position < childCount) {
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    int mScrollX = tabContainer.getScrollX();
                    int childX = (int) ViewCompat.getX(tabContainer.getChildAt(position));

                    if (childX < mScrollX) {
                        scrollView.scrollTo(childX, 0);
                        return;
                    }

                    int childWidth = (int) tabContainer.getChildAt(position).getWidth();
                    int hsvWidth = scrollView.getWidth();
                    int childRight = childX + childWidth;
                    int scrollRight = mScrollX + hsvWidth;

                    if (childRight > scrollRight) {
                        scrollView.scrollTo(childRight - scrollRight, 0);
                        return;
                    }
                }
            });
        }
    }


    public void setTabBarItemClickListener(EaseScrollTabBarItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public interface EaseScrollTabBarItemClickListener {
        void onItemClick(int position);
    }

}
