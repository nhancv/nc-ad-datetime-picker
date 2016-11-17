package com.nhancv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nhancv.picker.dateview.NDatePicker;
import com.nhancv.picker.timeview.widgets.WheelTimePicker;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final NDatePicker vDatePicker = (NDatePicker) findViewById(R.id.vDatePicker);

        final WheelTimePicker vTimePicker = (WheelTimePicker) findViewById(R.id.vTimePicker);
        Button btGetTime = (Button) findViewById(R.id.btGetTime);
        btGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: vDate: " + new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(vDatePicker.getCalendar().getTime()));
                Log.e(TAG, "onClick vTime: " + new SimpleDateFormat("hh:mm:ss a", Locale.US).format(vTimePicker.getCalendar().getTime()));
            }
        });

        Button btSetTime = (Button) findViewById(R.id.btSetTime);
        btSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String time = "14/01/2016 20:55";
                String format = "dd/MM/yyyy hh:mm";
                vDatePicker.setDate(time, format);
                vTimePicker.setTime(time, format);
            }
        });




    }

}
