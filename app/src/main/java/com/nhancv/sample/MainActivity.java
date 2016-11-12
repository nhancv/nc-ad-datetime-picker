package com.nhancv.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.nhancv.picker.timeview.widgets.WheelTimePicker;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WheelTimePicker vTimePicker = (WheelTimePicker) findViewById(R.id.vTimePicker);
        Button btGetTime = (Button) findViewById(R.id.btGetTime);
        btGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: " + vTimePicker.getCalendar().toString());
            }
        });

    }

}
