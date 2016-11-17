package com.nhancv.picker.dateview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nhancv.picker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by nhancao on 11/13/16.
 */

public class NDatePicker extends LinearLayout {
    private final Calendar calendar = Calendar.getInstance();
    private NCalendarView.CompactCalendarViewListener listener;
    private ImageView btCompactDatePickerLeft;
    private ImageView btCompactDatePickerRight;
    private TextView tvCompactDatePicker;
    private NCalendarView vCompactDatePicker;

    public NDatePicker(Context context) {
        this(context, null);
    }

    public NDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_calendar_date_picker, this);
        setupView();
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setListener(NCalendarView.CompactCalendarViewListener listener) {
        this.listener = listener;
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

    public void setDate(Date date) {
        updateHeaderDate(date);
        NCalendarView vCompactDatePicker = getCalendarView();
        vCompactDatePicker.setCurrentDate(date);
    }

    public void setDate(String inputDate, String formatDate) {
        Date date = parseDate(inputDate, formatDate);
        if (date == null) return;
        setDate(date);
    }

    private void setupView() {

        btCompactDatePickerLeft = (ImageView) findViewById(R.id.btCompactDatePickerLeft);
        btCompactDatePickerRight = (ImageView) findViewById(R.id.btCompactDatePickerRight);
        tvCompactDatePicker = (TextView) findViewById(R.id.tvCompactDatePicker);
        vCompactDatePicker = (NCalendarView) findViewById(R.id.vCompactDatePicker);

        final Calendar tmp = Calendar.getInstance();
        tmp.setTime(vCompactDatePicker.getFirstDayOfCurrentMonth());
        tmp.set(Calendar.MONTH, tmp.get(Calendar.MONTH) + 1);
        updateHeaderDate(tmp.getTime());

        btCompactDatePickerLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                vCompactDatePicker.showPreviousMonth();
            }
        });
        btCompactDatePickerRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                vCompactDatePicker.showNextMonth();
            }
        });
        tvCompactDatePicker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Date current = Calendar.getInstance().getTime();
                vCompactDatePicker.setCurrentDate(current);
                updateHeaderDate(current);
            }
        });
        vCompactDatePicker.setListener(new NCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                calendar.setTime(dateClicked);
                if (listener != null) listener.onDayClick(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                calendar.setTime(firstDayOfNewMonth);
                Calendar tmp = Calendar.getInstance();
                tmp.setTime(firstDayOfNewMonth);
                tmp.set(Calendar.MONTH, tmp.get(Calendar.MONTH) + 1);
                updateHeaderDate(tmp.getTime());
                if (listener != null) listener.onMonthScroll(firstDayOfNewMonth);
            }
        });
    }

    public TextView getHeaderDatePicker() {
        return tvCompactDatePicker;
    }

    public ImageView getHeaderDatePickerRight() {
        return btCompactDatePickerRight;
    }

    public ImageView getHeaderDatePickerLeft() {
        return btCompactDatePickerLeft;
    }

    public NCalendarView getCalendarView() {
        return (NCalendarView) findViewById(R.id.vCompactDatePicker);
    }

    public void updateHeaderDate(Date updateDate) {
        TextView tvCompactDatePicker = (TextView) findViewById(R.id.tvCompactDatePicker);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM yyyy", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        tvCompactDatePicker.setText(simpleDateFormat.format(updateDate));
    }

}
