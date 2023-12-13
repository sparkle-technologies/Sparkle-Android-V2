package com.cyberflow.sparkle.main.adapter;


import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class QuestionsAdapter extends BannerAdapter<String, QuestionsAdapter.BannerViewHolder> {

    public QuestionsAdapter(List<String> mDatas) {
        super(mDatas);
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setTextColor(ResourcesCompat.getColor(parent.getContext().getResources(), com.cyberflow.base.resources.R.color.color_8B82DB, null));
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        return new BannerViewHolder(tv);
    }

    @Override
    public void onBindView(BannerViewHolder holder, String data, int position, int size) {
        holder.textView.setText(data);
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public BannerViewHolder(@NonNull TextView view) {
            super(view);
            this.textView = view;
        }
    }
}