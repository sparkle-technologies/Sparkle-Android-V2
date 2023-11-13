package com.cyberflow.sparkle.main.widget.calendar;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyberflow.sparkle.R;

import java.util.Calendar;
import java.util.List;

public class CalendarDateAdapter extends BaseAdapter {
    private Context context;
    private List<DateBean> mData;

    public CalendarDateAdapter(Context context, List<DateBean> mData) {
        this.context = context;
        this.mData = mData;
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
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = View.inflate(context, R.layout.calender_data_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tvTop = view.findViewById(R.id.tv_top);
            viewHolder.ivSelected = view.findViewById(R.id.iv_selected);
            viewHolder.tvData = view.findViewById(R.id.tv_data);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        DateBean data = mData.get(i);
        if (data.getDay() != 0) {
            viewHolder.tvData.setText("" + data.getDay());
        } else {
            viewHolder.tvData.setText("");
            viewHolder.ivSelected.setVisibility(View.GONE);
        }

        //选中日期 表示为今天
        Calendar calendar = Calendar.getInstance();
        if (data.getYear() == calendar.get(Calendar.YEAR) && data.getMonth() == (calendar.get(Calendar.MONTH) + 1) && data.getDay() == calendar.get(Calendar.DAY_OF_MONTH)) {
            viewHolder.tvTop.setVisibility(View.VISIBLE);
            viewHolder.ivSelected.setImageResource(com.cyberflow.base.resources.R.drawable.main_bg_calendar_selected_yellow);
            viewHolder.tvData.setBackgroundResource(com.cyberflow.base.resources.R.drawable.main_bg_calendar_clicked);
        }
        return view;
    }

    static class ViewHolder {
        public TextView tvTop;
        public ImageView ivSelected;
        public TextView tvData;
    }
}
