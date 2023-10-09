package com.cyberflow.sparkle.chat.common.model;

import com.cyberflow.sparkle.chat.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.domain.EaseEmojiconGroupEntity;

import java.util.Arrays;

public class EmojiconExampleGroupData {

    private static int[] icons = new int[]{
            R.drawable.icon_01,
            R.drawable.icon_02,
            R.drawable.icon_03,
            R.drawable.icon_04,
            R.drawable.icon_05,
            R.drawable.icon_06,
            R.drawable.icon_07,
            R.drawable.icon_08,
            R.drawable.icon_09,
            R.drawable.icon_10,
    };

    private static int[] bigIcons = new int[]{
            R.drawable.icon_01,
            R.drawable.icon_02,
            R.drawable.icon_03,
            R.drawable.icon_04,
            R.drawable.icon_05,
            R.drawable.icon_06,
            R.drawable.icon_07,
            R.drawable.icon_08,
            R.drawable.icon_09,
            R.drawable.icon_10,
    };


    private static String[] names = new String[]{
            "thank you",
            "大哭",
            "开心",
            "打call",
            "施魔法",
            "早上好",
            "求好运",
            "生气",
            "通用文字",
            "震惊"
    };

    private static final EaseEmojiconGroupEntity DATA = createData();

    private static EaseEmojiconGroupEntity createData() {
        EaseEmojiconGroupEntity emojiconGroupEntity = new EaseEmojiconGroupEntity();
        EaseEmojicon[] datas = new EaseEmojicon[icons.length];
        for (int i = 0; i < icons.length; i++) {
            datas[i] = new EaseEmojicon(icons[i], null, Type.BIG_EXPRESSION);
            datas[i].setBigIcon(bigIcons[i]);
            //you can replace this to any you want
            datas[i].setName(names[i]);
            datas[i].setIdentityCode("em" + i);
        }
        emojiconGroupEntity.setEmojiconList(Arrays.asList(datas));
        emojiconGroupEntity.setIcon(R.drawable.icon_00);
        emojiconGroupEntity.setType(Type.BIG_EXPRESSION);
        return emojiconGroupEntity;
    }


    public static EaseEmojiconGroupEntity getData() {
        return DATA;
    }
}
