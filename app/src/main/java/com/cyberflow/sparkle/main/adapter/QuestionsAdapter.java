package com.cyberflow.sparkle.main.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class QuestionsAdapter extends BannerAdapter<String, QuestionsAdapter.BannerViewHolder> {

    public QuestionsAdapter(List<String> mDatas) {
        super(mDatas);
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        TextView imageView = new TextView(parent.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new BannerViewHolder(imageView);
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