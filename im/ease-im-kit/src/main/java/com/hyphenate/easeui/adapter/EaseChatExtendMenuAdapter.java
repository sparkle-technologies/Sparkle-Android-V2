package com.hyphenate.easeui.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberflow.sparkle.widget.ShadowImgButton;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.interfaces.OnItemClickListener;
import com.hyphenate.easeui.modules.chat.EaseChatExtendMenu.ChatMenuItemModel;

public class EaseChatExtendMenuAdapter extends EaseBaseChatExtendMenuAdapter<EaseChatExtendMenuAdapter.ViewHolder, ChatMenuItemModel> {
    private OnItemClickListener itemListener;

    @Override
    protected int getItemLayoutId() {
        return R.layout.ease_chat_menu_item;
    }

    @Override
    protected EaseChatExtendMenuAdapter.ViewHolder easeCreateViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EaseChatExtendMenuAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ChatMenuItemModel item = mData.get(position);
        holder.imageView.updateSrc(item.image);

        holder.textView.setText(item.name);

        holder.imageView.setClickListener(new ShadowImgButton.ShadowClickListener() {
            @Override
            public void clicked() {
                if (item.clickListener != null) {
                    item.clickListener.onChatExtendMenuItemClick(item.id, holder.itemView);
                }
                if (itemListener != null) {
                    itemListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ShadowImgButton imageView;
        private TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ShadowImgButton) itemView.findViewById(R.id.image);
            textView = (TextView) itemView.findViewById(R.id.text);
        }
    }
}

