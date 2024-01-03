package com.cyberflow.sparkle.main.widget.calendar;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.cyberflow.sparkle.R;

import java.util.Calendar;
import java.util.List;

public class CalendarDateAdapter extends BaseAdapter {
    private Context context;
    private List<DateBean> mData;

    private DateBean birthDate;
    private DateBean currentDate;

    public CalendarDateAdapter(Context context, List<DateBean> mData, DateBean _birth, DateBean _current) {
        this.context = context;
        this.mData = mData;
        this.birthDate = _birth;
        this.currentDate = _current;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = View.inflate(context, R.layout.calender_data_item, null);
            viewHolder = new ViewHolder();
            viewHolder.root = view.findViewById(R.id.root);
            viewHolder.tvTop = view.findViewById(R.id.tv_top);
            viewHolder.ivSelected = view.findViewById(R.id.iv_selected);
            viewHolder.tvData = view.findViewById(R.id.tv_data);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        DateBean data = mData.get(i);
        if (data.getDay() != 0) {
            if (data.isThisMonth()) {
                viewHolder.tvData.setText("" + data.getDay());
                viewHolder.tvData.setTextColor(Color.BLACK);
            } else {
                viewHolder.tvData.setText("" + data.getDay());
                viewHolder.tvData.setTextColor(ResourcesCompat.getColor(context.getResources(), com.cyberflow.base.resources.R.color.color_7D7D80, null));
            }
        } else {
            viewHolder.tvData.setText("");
            viewHolder.ivSelected.setVisibility(View.GONE);
        }

        int birth = birthDate.getYear() * 365 + birthDate.getMonth()*30 + birthDate.getDay();
        int select = data.getYear() * 365 + data.getMonth()*30 + data.getDay();
        if(select < birth){
            viewHolder.tvData.setTextColor(ResourcesCompat.getColor(context.getResources(), com.cyberflow.base.resources.R.color.color_7D7D80, null));
        }

        //选中日期 表示为今天
        if (data.getYear() == currentDate.getYear()  && data.getMonth() == currentDate.getMonth() && data.getDay() == currentDate.getDay()) {
            viewHolder.tvTop.setVisibility(View.VISIBLE);
            viewHolder.ivSelected.setImageResource(com.cyberflow.base.resources.R.drawable.main_bg_calendar_selected_yellow);
            viewHolder.tvData.setBackgroundResource(com.cyberflow.base.resources.R.drawable.main_bg_calendar_clicked);
            viewHolder.tvData.setTextColor(Color.BLACK);
        }
        return view;
    }

    static class ViewHolder {
        public View root;
        public TextView tvTop;
        public ImageView ivSelected;
        public TextView tvData;
    }
}
