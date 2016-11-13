package com.nhancv.picker.timeview.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.nhancv.picker.R;
import com.nhancv.picker.timeview.WheelPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by nhancao on 11/13/16.
 */

public class WheelTimePicker extends LinearLayout {

    private final Calendar calendar = Calendar.getInstance();

    public WheelTimePicker(Context context) {
        this(context, null);
    }

    public WheelTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_wheel_time_picker, this);
        setupView();

    }

    private void setupView() {
        WheelPicker vHourTimePicker = (WheelPicker) findViewById(R.id.vWheelHourTimePicker);
        WheelPicker vMinuteTimePicker = (WheelPicker) findViewById(R.id.vWheelMinuteTimePicker);
        WheelPicker vTypeTimePicker = (WheelPicker) findViewById(R.id.vWheelTypeTimePicker);

        int hour = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int am = calendar.get(Calendar.AM_PM);

        final List<String> hourList = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            hourList.add(formatWithLeadingZero(2, String.valueOf(i)));
        }
        vHourTimePicker.setData(hourList);
        int position = hour - 1;
        if (hour == 0) position = 11;
        vHourTimePicker.setSelectedItemPosition(position);

        vHourTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                int hour = position + 1;
                if (position == 11) hour = 0;
                calendar.set(Calendar.HOUR, hour);
            }
        });

        List<String> minuteList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minuteList.add(formatWithLeadingZero(2, String.valueOf(i)));
        }
        vMinuteTimePicker.setData(minuteList);
        vMinuteTimePicker.setSelectedItemPosition(minutes);
        vMinuteTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                calendar.set(Calendar.MINUTE, position);
            }
        });

        String[] typeTime = new String[]{"AM", "PM"};
        vTypeTimePicker.setData(Arrays.asList(typeTime));
        vTypeTimePicker.setSelectedItemPosition(am);
        vTypeTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                calendar.set(Calendar.AM_PM, position);
            }
        });

    }

    public Calendar getCalendar() {
        return calendar;
    }

    private String formatWithLeadingZero(int length, String content) {
        if (content == null) return null;
        String pattern = "";
        for (int i = 0; i < length; i++) {
            pattern += "0";
        }
        return (pattern + content).substring(content.length());
    }

}
