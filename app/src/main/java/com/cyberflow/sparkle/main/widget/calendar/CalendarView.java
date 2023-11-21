package com.cyberflow.sparkle.main.widget.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberflow.sparkle.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarView extends RecyclerView.ViewHolder {
    private final static String TAG = "CalendarView";
    private GridView gridView;
    private int year = 0;
    private int month = 0;
    private CalendarDateAdapter calendarDateAdapter;
    private Context context;

    private CalendarDialog.Callback callback;
    private boolean weekMode = false;

    private DateBean birthDate;

    public CalendarView(@NonNull View itemView, CalendarDialog.Callback callback, boolean _weekMode, DateBean _birth) {
        super(itemView);
        this.callback = callback;
        this.weekMode = _weekMode;
        this.birthDate = _birth;
        gridView = itemView.findViewById(R.id.wgvCalendar);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        context = itemView.getContext();
        initEvent();
    }

    private void initEvent() {
        gridView.getSelectedItem();
        gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            DateBean selected = (DateBean) calendarDateAdapter.getItem(position);

            int min = birthDate.getYear() * 365 + birthDate.getMonth()*30 + birthDate.getDay();
            int max = 2100 * 365 + 1*30 + 1;
            int count = selected.getYear()* 365 + selected.getMonth()*30 + selected.getDay();
            if(count < min || count > max){
                return;
            }

            for (int i = 0; i < adapterView.getCount(); i++) {
                View v = adapterView.getChildAt(i);
                CalendarDateAdapter.ViewHolder vh = ((CalendarDateAdapter.ViewHolder) v.getTag());
                DateBean current = (DateBean) calendarDateAdapter.getItem(i);

                if(weekMode){
                    boolean isSame = isSameWeek(selected, current);
                    if (current.isThisMonth()) {
                        vh.tvData.setTextColor(Color.BLACK);
                    } else {
                        vh.tvData.setTextColor(ResourcesCompat.getColor(context.getResources(), com.cyberflow.base.resources.R.color.color_7D7D80, null));
                    }

                    if (isSame) {
                        if(isToday(current)){
                            vh.tvData.setTextColor(Color.BLACK);
                        }else{
                            vh.tvData.setTextColor(Color.WHITE);
                        }
                        if (i % 7 == 0) {
                            vh.root.setBackgroundResource(com.cyberflow.base.resources.R.drawable.main_bg_horoscope_calendar_select_half_left);
                        } else if (i % 7 == 6) {
                            vh.root.setBackgroundResource(com.cyberflow.base.resources.R.drawable.main_bg_horoscope_calendar_select_half_right);
                        } else {
                            vh.root.setBackgroundResource(com.cyberflow.base.resources.R.color.black);
                        }
                    } else {
                        vh.root.setBackground(null);
                    }

                }else{
                    if (i == position) {
                        vh.tvData.setTextColor(Color.BLACK);
                        vh.tvData.setBackgroundResource(com.cyberflow.base.resources.R.drawable.main_bg_calendar_clicked);
                    } else {
                        vh.tvData.setBackground(null);
                    }
                }
            }

            if(callback!=null){
                gridView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSelected(selected);
                    }
                }, 200);
            }
        });
    }

    public void initData(Calendar calendar) {
        ArrayList<DateBean> data = new ArrayList<>();

        if(weekMode){
            int dayOfWeek = getMonthOneDayWeek(calendar);
            if (dayOfWeek > 0) {
                Calendar previous = (Calendar) calendar.clone();  // 上个月   30
                previous.add(Calendar.MONTH, -1);
                int totalDays = previous.getActualMaximum(Calendar.DAY_OF_MONTH);
//            Log.e(TAG, "initData: dayOfWeek=" + dayOfWeek + "\t  totalDays=" + totalDays );
                for (int i = totalDays - dayOfWeek; i < totalDays; i++) {
                    //填充空白的
                    data.add(new DateBean(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), i + 1, false));
                }
            }
        }else{
            for (int i = 0; i < getMonthOneDayWeek(calendar); i++) {  //获取第一天是星期几然后计算出需要填充的空白数据
                data.add(new DateBean(0, 0, 0, false));
            }
        }

        //填充数据
        for (int i = 0; i < getMonthMaxData(calendar); i++) {
            data.add(new DateBean(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, i + 1, true));
        }

        if (weekMode) {
            int left = data.size() % 7;
            if (left > 0) {
                for (int i = 0; i < 7 - left; i++) {
                    DateBean dateBean = new DateBean(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 2, i + 1, false);
                    dateBean.setThisMonth(false);
                    data.add(dateBean);
                }
            }
        }

        calendarDateAdapter = new CalendarDateAdapter(context, data);
        gridView.setAdapter(calendarDateAdapter);
        setGridViewHeight(gridView, data.size());
    }

    //获取第一天为星期几
    private int getMonthOneDayWeek(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    //获取当月有几天
    private int getMonthMaxData(Calendar calendar) {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static boolean isSameWeek(Calendar cal1, Calendar cal2) {
        int week1 = cal1.get(Calendar.WEEK_OF_YEAR);
        int year1 = cal1.get(Calendar.YEAR);

        int week2 = cal2.get(Calendar.WEEK_OF_YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        return week1 == week2 && year1 == year2;
    }

    public static boolean isSameWeek(DateBean dateBean1, DateBean dateBean2) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        try {
            Date date1 = dateFormat.parse(dateBean1.getYear() + "-" + dateBean1.getMonth() + "-" + dateBean1.getDay());
            Date date2 = dateFormat.parse(dateBean2.getYear() + "-" + dateBean2.getMonth() + "-" + dateBean2.getDay());
            cal1.setTime(date1);
            cal2.setTime(date2);
            return isSameWeek(cal1, cal2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    Calendar calendar = Calendar.getInstance();

    private boolean isToday(DateBean dateBean) {
        return (dateBean.getDay() == calendar.get(Calendar.DAY_OF_MONTH) && dateBean.getMonth() == (calendar.get(Calendar.MONTH) + 1) && dateBean.getYear() == calendar.get(Calendar.YEAR));
    }

    public static void setGridViewHeight(GridView gridView, int size) {
        // 获取listview的adapter
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int col = 7;
        int totalHeight = 0;
        int hang;
        if (size % col == 0) {
            hang = size / col;
        } else {
            hang = size / col + 1;
        }
        View listItem = listAdapter.getView(8, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight() * hang;
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }
}
