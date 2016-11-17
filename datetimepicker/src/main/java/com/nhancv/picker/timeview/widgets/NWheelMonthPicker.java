package com.nhancv.picker.timeview.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.nhancv.picker.timeview.NWheelPicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NWheelMonthPicker extends NWheelPicker implements IWheelMonthPicker {
    private int mSelectedMonth;

    public NWheelMonthPicker(Context context) {
        this(context, null);
    }

    public NWheelMonthPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 12; i++)
            data.add(i);
        super.setData(data);

        mSelectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        updateSelectedYear();
    }

    private void updateSelectedYear() {
        setSelectedItemPosition(mSelectedMonth - 1);
    }

    @Override
    public void setData(List data) {
        throw new UnsupportedOperationException("You can not invoke setData in WheelMonthPicker");
    }

    @Override
    public int getSelectedMonth() {
        return mSelectedMonth;
    }

    @Override
    public void setSelectedMonth(int month) {
        mSelectedMonth = month;
        updateSelectedYear();
    }

    @Override
    public int getCurrentMonth() {
        return Integer.valueOf(String.valueOf(getData().get(getCurrentItemPosition())));
    }
}