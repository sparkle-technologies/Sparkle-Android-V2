package com.hyphenate.easeui.modules.chat;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;
import com.hyphenate.easeui.modules.chat.interfaces.EaseEmojiconMenuListener;
import com.hyphenate.easeui.modules.chat.interfaces.IChatEmojiconMenu;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconPagerView;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconScrollTabBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EaseEmojiconMenu extends LinearLayout implements IChatEmojiconMenu {
    private int emojiconColumns;
    private int bigEmojiconColumns;
    private EaseEmojiconScrollTabBar tabBar;
    private EaseEmojiconPagerView pagerView;

    private TextView btnSend;
    private ImageView btnDelete;

    private View lay_send;

    private List<EaseEmojiconGroupEntity> emojiconGroupList = new ArrayList<>();
    private EaseEmojiconMenuListener listener;
    private static final int defaultColumns = 7;
    private static final int defaultBigColumns = 4;

    public EaseEmojiconMenu(Context context) {
        this(context, null);
    }

    public EaseEmojiconMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseEmojiconMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ease_widget_emojicon, this);
        pagerView = (EaseEmojiconPagerView) findViewById(R.id.pager_view);
        tabBar = (EaseEmojiconScrollTabBar) findViewById(R.id.tab_bar);
        lay_send = findViewById(R.id.lay_send);
        btnSend = findViewById(R.id.btn_send);
        btnDelete = findViewById(R.id.btn_delete);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseEmojiconMenu);
        emojiconColumns = ta.getInt(R.styleable.EaseEmojiconMenu_emojiconColumns, defaultColumns);
        bigEmojiconColumns = ta.getInt(R.styleable.EaseEmojiconMenu_bigEmojiconRows, defaultBigColumns);
        ta.recycle();
    }

    public void init() {
        init(null);
    }

    public void init(List<EaseEmojiconGroupEntity> groupEntities) {
        if (groupEntities == null || groupEntities.size() == 0) {
            groupEntities = new ArrayList<>();
            groupEntities.add(new EaseEmojiconGroupEntity(R.drawable.ee_0, Arrays.asList(EaseDefaultEmojiconDatas.getData())));
        }
        for (EaseEmojiconGroupEntity groupEntity : groupEntities) {
            emojiconGroupList.add(groupEntity);
            tabBar.addTab(groupEntity.getIcon());
        }

        pagerView.setPagerViewListener(new EmojiconPagerViewListener());
        pagerView.init(emojiconGroupList, emojiconColumns, bigEmojiconColumns);

        tabBar.setTabBarItemClickListener(new EaseEmojiconScrollTabBar.EaseScrollTabBarItemClickListener() {

            @Override
            public void onItemClick(int position) {
                pagerView.setGroupPostion(position);
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteImageClicked();
            }
        });

        btnSend.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSendImageClicked();
            }
        });
    }

    /**
     * add emojicon group
     *
     * @param groupEntity
     */
    public void addEmojiconGroup(EaseEmojiconGroupEntity groupEntity) {
        emojiconGroupList.add(groupEntity);
        pagerView.addEmojiconGroup(groupEntity, true);
        tabBar.addTab(groupEntity.getIcon());
    }

    /**
     * add emojicon group list
     *
     * @param groupEntitieList
     */
    public void addEmojiconGroup(List<EaseEmojiconGroupEntity> groupEntitieList) {
        for (int i = 0; i < groupEntitieList.size(); i++) {
            EaseEmojiconGroupEntity groupEntity = groupEntitieList.get(i);
            emojiconGroupList.add(groupEntity);
            pagerView.addEmojiconGroup(groupEntity, i == groupEntitieList.size() - 1 ? true : false);
            tabBar.addTab(groupEntity.getIcon());
        }

    }

    /**
     * remove emojicon group
     *
     * @param position
     */
    public void removeEmojiconGroup(int position) {
        emojiconGroupList.remove(position);
        pagerView.removeEmojiconGroup(position);
        tabBar.removeTab(position);
    }

    public void setTabBarVisibility(boolean isVisible) {
        if (!isVisible) {
            tabBar.setVisibility(View.GONE);
        } else {
            tabBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setEmojiconMenuListener(EaseEmojiconMenuListener listener) {
        this.listener = listener;
    }

    private class EmojiconPagerViewListener implements EaseEmojiconPagerView.EaseEmojiconPagerViewListener {

        @Override
        public void onPagerViewInited(int groupMaxPageSize, int firstGroupPageSize) {
            tabBar.selectedTo(0);
        }

        @Override
        public void onGroupPositionChanged(int groupPosition, int pagerSizeOfGroup) {
            tabBar.selectedTo(groupPosition);
            lay_send.setVisibility(groupPosition == 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onGroupInnerPagePostionChanged(int oldPosition, int newPosition) {
        }

        @Override
        public void onGroupPagePostionChangedTo(int position) {
        }

        @Override
        public void onGroupMaxPageSizeChanged(int maxCount) {
        }

        @Override
        public void onDeleteImageClicked() {
            if (listener != null) {
                listener.onDeleteImageClicked();
            }
        }

        @Override
        public void onExpressionClicked(EaseEmojicon emojicon) {
            if (listener != null) {
                listener.onExpressionClicked(emojicon);
            }
        }
    }
}

