package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.utils.EaseSmileUtils;

import java.util.List;

public class EmojiconGridAdapter extends ArrayAdapter<EaseEmojicon> {

    private Type emojiconType;


    public EmojiconGridAdapter(Context context, int textViewResourceId, List<EaseEmojicon> objects, EaseEmojicon.Type emojiconType) {
        super(context, textViewResourceId, objects);
        this.emojiconType = emojiconType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (emojiconType == Type.BIG_EXPRESSION) {
                convertView = View.inflate(getContext(), R.layout.ease_row_big_expression, null);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_expression);
                TextView textView = (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder = new ViewHolder();
                viewHolder.imageView = imageView;
                viewHolder.textView = textView;
            } else {
                convertView = View.inflate(getContext(), R.layout.ease_row_expression, null);
                TextView tvExpression = (TextView) convertView.findViewById(R.id.tv_expression);
                viewHolder = new ViewHolder();
                viewHolder.tvExpression = tvExpression;
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        EaseEmojicon emojicon = getItem(position);
        if (emojiconType == Type.BIG_EXPRESSION) {
            if (viewHolder.textView != null && emojicon.getName() != null) {
                viewHolder.textView.setText(emojicon.getName());
            }
            if (EaseSmileUtils.DELETE_KEY.equals(emojicon.getEmojiText())) {
                viewHolder.imageView.setImageResource(R.drawable.ease_delete_expression);
            } else {
                if (emojicon.getIcon() != 0) {
                    viewHolder.imageView.setImageResource(emojicon.getIcon());
                } else if (emojicon.getIconPath() != null) {
                    Glide.with(getContext()).load(emojicon.getIconPath())
                            .apply(RequestOptions.placeholderOf(R.drawable.ease_default_expression))
                            .into(viewHolder.imageView);
                }
            }
        } else {
            viewHolder.tvExpression.setText(emojicon.getEmojiText());
        }
        return convertView;
    }

    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView tvExpression;
    }
}


