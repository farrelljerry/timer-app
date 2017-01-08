package com.jerryrolfing.speechtimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

public class TimeSelect extends AppCompatActivity {

    Button saveButton;
    Button cancelButton;

    String intervalMessage;
    long intervalTime;  //seconds
    String tag; //used as a holder to pass back information sent in incoming intent

    NumberPicker npIntervalHours;
    NumberPicker npIntervalMinutes;
    NumberPicker npIntervalSeconds;
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_select);

        saveButton=(Button) findViewById(R.id.saveButton);
        cancelButton=(Button) findViewById(R.id.cancelButton);

        tvMessage = (TextView) findViewById(R.id.edit_intervalText);
        npIntervalHours=(NumberPicker) findViewById(R.id.np_intervalHours);
        npIntervalMinutes=(NumberPicker) findViewById(R.id.np_intervalMinutes);
        npIntervalSeconds=(NumberPicker) findViewById(R.id.np_intervalSeconds);
        npIntervalHours.setMinValue(0);
        npIntervalHours.setMaxValue(23);
        npIntervalMinutes.setMinValue(0);
        npIntervalMinutes.setMaxValue(59);
        npIntervalSeconds.setMinValue(0);
        npIntervalSeconds.setMaxValue(59);

        tag=getIntent().getStringExtra("TAG");
        intervalMessage=getIntent().getStringExtra("MESSAGE");
        tvMessage.setText(intervalMessage);

        intervalTime=getIntent().getLongExtra("INTERVAL",0L);
        int s=(int) (intervalTime%60);
        int m=(int) (((intervalTime-s)%3600)/60);
        int h=(int) ((intervalTime-s-m*60)/3600);
        npIntervalHours.setValue(h);
        npIntervalMinutes.setValue(m);
        npIntervalSeconds.setValue(s);

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent output=new Intent();

                int h=npIntervalHours.getValue();
                int m=npIntervalMinutes.getValue();
                int s=npIntervalSeconds.getValue();
                long i=(long) (s+m*60+h*3600);
                String message=tvMessage.getText().toString();
                output.putExtra("MESSAGE",message);
                output.putExtra("INTERVAL",i);
                output.putExtra("TAG",tag);
                setResult(Activity.RESULT_OK,output);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View view) {
                Intent output=new Intent();
                setResult(Activity.RESULT_CANCELED,output);
                finish();
            }
        });

    }
}
