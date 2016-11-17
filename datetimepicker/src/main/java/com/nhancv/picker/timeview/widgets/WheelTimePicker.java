package com.nhancv.picker.timeview.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.nhancv.picker.R;
import com.nhancv.picker.timeview.WheelPicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by nhancao on 11/13/16.
 */

public class WheelTimePicker extends LinearLayout {

    private final Calendar calendar = Calendar.getInstance();
    private WheelPicker hourTimePicker;
    private WheelPicker minuteTimePicker;
    private WheelPicker typeTimePicker;

    public WheelTimePicker(Context context) {
        this(context, null);
    }

    public WheelTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_wheel_time_picker, this);
        setupView();
    }

    private void setupView() {
        hourTimePicker = (WheelPicker) findViewById(R.id.vWheelHourTimePicker);
        minuteTimePicker = (WheelPicker) findViewById(R.id.vWheelMinuteTimePicker);
        typeTimePicker = (WheelPicker) findViewById(R.id.vWheelTypeTimePicker);

        //hour list
        final List<String> hourList = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            hourList.add(formatWithLeadingZero(2, String.valueOf(i)));
        }
        hourTimePicker.setData(hourList);
        //minute list
        List<String> minuteList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minuteList.add(formatWithLeadingZero(2, String.valueOf(i)));
        }
        minuteTimePicker.setData(minuteList);
        //type list
        String[] typeTime = new String[]{"AM", "PM"};
        typeTimePicker.setData(Arrays.asList(typeTime));

        setTime(calendar);

        hourTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                int hour = position + 1;
                if (position == 11) hour = 0;
                calendar.set(Calendar.HOUR, hour);
            }
        });
        minuteTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                calendar.set(Calendar.MINUTE, position);
            }
        });
        typeTimePicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                calendar.set(Calendar.AM_PM, position);
            }
        });
    }


    public Date parseDate(String inputDate, String formatDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatDate, Locale.US);
            return format.parse(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setTime(String inputDate, String formatDate) {
        Date time = parseDate(inputDate, formatDate);
        if (time == null) return;
        setTime(time);
    }

    public void setTime(Date time) {
        calendar.setTime(time);
        setTime(calendar);
    }

    public void setTime(final Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int am = calendar.get(Calendar.AM_PM);

        int position = hour - 1;
        if (hour == 0) position = 11;
        hourTimePicker.setSelectedItemPosition(position);
        minuteTimePicker.setSelectedItemPosition(minutes);
        typeTimePicker.setSelectedItemPosition(am);
    }

    public WheelPicker getHourTimePicker() {
        return hourTimePicker;
    }

    public WheelPicker getMinuteTimePicker() {
        return minuteTimePicker;
    }

    public WheelPicker getTypeTimePicker() {
        return typeTimePicker;
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
