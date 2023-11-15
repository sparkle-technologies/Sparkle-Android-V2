package com.cyberflow.sparkle.main.widget.calendar;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberflow.sparkle.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarView> {
    private List<Calendar> calendar = new ArrayList<>();
    private boolean weekMode = false;
    private CalendarDialog.Callback callback;

    public CalendarAdapter(CalendarDialog.Callback callback, boolean _weekMode) {
        this.callback = callback;
        this.weekMode = _weekMode;
    }

    @NonNull
    @Override
    public CalendarView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CalendarView(LayoutInflater.from(parent.getContext()).inflate(R.layout.calender_view, parent, false), callback, weekMode);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarView holder, int position) {
        if (calendar.size() != 0)
            holder.initData(calendar.get(position));
    }

    @Override
    public int getItemCount() {
        return calendar.size();
    }

    public void refreshData(List<Calendar> data) {
        for (int i = 0; i < data.size(); i++) {
            calendar.add(data.get(i));
        }
        notifyDataSetChanged();

    }
}
