package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cyberflow.base.net.GsonConverter;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseIM;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.constants.EaseConstant;
import com.hyphenate.easeui.domain.EaseEmojicon;

import java.util.HashMap;

/**
 * big emoji icons
 */
public class EaseChatRowBigExpression extends EaseChatRowText {
    private ImageView imageView;

    public EaseChatRowBigExpression(Context context, boolean isSender) {
        super(context, isSender);
    }

    public EaseChatRowBigExpression(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.ease_row_received_bigexpression
                : R.layout.ease_row_sent_bigexpression, this);
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    public void onSetUpView() {
        String emojiconMap = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION, null);
//        Log.e(TAG, "createExpressionMessage: emojiconMap" + emojiconMap );
        HashMap<String, String> map = GsonConverter.Companion.getGson().fromJson(emojiconMap, HashMap.class);
        String emojiconGroupId = map.get(EaseConstant.MESSAGE_ATTR_EXPRESSION_GROUP_ID);
        String emojiconId = map.get(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID);

        EaseEmojicon emojicon = null;
        if (EaseIM.getInstance().getEmojiconInfoProvider() != null) {
            emojicon = EaseIM.getInstance().getEmojiconInfoProvider().getEmojiconInfo(emojiconGroupId, emojiconId);
        }
        if (emojicon != null) {
            if (emojicon.getBigIcon() != 0) {

                Glide.with(context).load(emojicon.getBigIcon())
                        .apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
                        .into(imageView);
            } else if (emojicon.getBigIconPath() != null) {
                Glide.with(context).load(emojicon.getBigIconPath())
                        .apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ease_default_expression);
            }
        }
    }
}
