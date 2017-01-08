package com.jerryrolfing.speechtimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditIntervals extends AppCompatActivity {

    TextView tvWorkoutName;
    TextView tvWarmup;
    TextView tvCooldown;
    TextView tvRepeats;
    TextView[] tvIntervals;
    LinearLayout llEdit;

    Button saveButton;
    Button cancelButton;

    long warmupTime;
    String warmupMessage;
    long cooldownTime;
    String cooldownMessage;
    long[] intervals;
    String[] messages;

    String[] intervalListArray;
    ArrayAdapter<String> arrayAdapter;

    private static final int TIME_SELECT_WARMUP_RESULT = 1;
    private static final int TIME_SELECT_COOLDOWN_RESULT = 2;
    private static final int TIME_SELECT_INTERVAL_RESULT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_intervals);

        tvWorkoutName=(TextView) findViewById(R.id.workoutName);
        tvWarmup = (TextView) findViewById(R.id.warmup);
        tvCooldown=(TextView) findViewById(R.id.cooldown);
        tvRepeats=(TextView) findViewById(R.id.repeats);
        llEdit=(LinearLayout) findViewById(R.id.activity_edit_intervals);
        saveButton = (Button) findViewById(R.id.save);
        cancelButton = (Button) findViewById(R.id.cancel);

        //pull data out of intent passed from calling Activity
        tvWorkoutName.setText(getIntent().getStringExtra("WORKOUTNAME"));
        warmupMessage=getIntent().getStringExtra("WARMUPMESSAGE");
        warmupTime=getIntent().getLongExtra("WARMUP",0L);
        cooldownMessage=getIntent().getStringExtra("COOLDOWNMESSAGE");
        cooldownTime=getIntent().getLongExtra("COOLDOWN",0L);
        intervals=getIntent().getLongArrayExtra("INTERVALS");
        messages=getIntent().getStringArrayExtra("MESSAGES");
        tvRepeats.setText(Integer.toString(getIntent().getIntExtra("REPEATS",0)));

        tvWarmup.setText(convertLongToTimeString(warmupTime)+"    "+ warmupMessage);
        tvCooldown.setText(convertLongToTimeString(cooldownTime)+"    "+cooldownMessage);

        //setup for intervals
        tvIntervals=new TextView[intervals.length];
        for (int i=0;i<intervals.length;i++){
            tvIntervals[i]=new TextView(this);
            tvIntervals[i].setTextSize(30);
            tvIntervals[i].setTag(i);
            tvIntervals[i].setText(convertLongToTimeString(intervals[i])+"    "+messages[i]);
            llEdit.addView(tvIntervals[i]);
            //setup ClickListener for intervals
            tvIntervals[i].setOnClickListener(new TextView.OnClickListener(){
                public void onClick(View view){
                    int j=(int) view.getTag();
                    Intent intent = new Intent(EditIntervals.this, TimeSelect.class);
                    intent.putExtra("MESSAGE",messages[j]);
                    intent.putExtra("INTERVAL",intervals[j]);
                    intent.putExtra("TAG",Integer.toString(j));
                    startActivityForResult(intent,TIME_SELECT_INTERVAL_RESULT);
                }
            });
        }

        //setup ClickListener for warmup
        tvWarmup.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                startWarmupEdit();
            }
        });

        //setup ClickListener for cooldown
        tvCooldown.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                startCooldownEdit();
            }
        });

        tvWarmup.setOnClickListener(new TextView.OnClickListener(){
            public void onClick(View view){
                startWarmupEdit();
            }
        });

        tvWorkoutName.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s){}
            public void beforeTextChanged(CharSequence s,int start,int count, int after){}
            public void onTextChanged(CharSequence s,int start,int before, int count){}
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent output=new Intent();
                output.putExtra("WARMUPMESSAGE",warmupMessage);
                output.putExtra("WARMUP",warmupTime);
                output.putExtra("COOLDOWNMESSAGE",cooldownMessage);
                output.putExtra("COOLDOWN",cooldownTime);
                output.putExtra("REPEATS",String.valueOf(tvRepeats.getText()));
                output.putExtra("INTERVALS",intervals);
                output.putExtra("MESSAGES",messages);
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

    private void startWarmupEdit(){
        Intent intent = new Intent(EditIntervals.this, TimeSelect.class);
        intent.putExtra("MESSAGE",warmupMessage);
        intent.putExtra("INTERVAL",warmupTime);
        intent.putExtra("TAG","0");
        startActivityForResult(intent,TIME_SELECT_WARMUP_RESULT);
    }

    private void startCooldownEdit(){
        Intent intent = new Intent(EditIntervals.this, TimeSelect.class);
        intent.putExtra("MESSAGE",cooldownMessage);
        intent.putExtra("INTERVAL",cooldownTime);
        intent.putExtra("TAG","0");
        startActivityForResult(intent,TIME_SELECT_COOLDOWN_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case (TIME_SELECT_WARMUP_RESULT):{
                if (resultCode == RESULT_OK) {
                    warmupMessage=data.getStringExtra("MESSAGE");
                    warmupTime=data.getLongExtra("INTERVAL",0L);
                    tvWarmup.setText(convertLongToTimeString(warmupTime)+"    "+ warmupMessage);
                    break;
                }
            }
            case (TIME_SELECT_COOLDOWN_RESULT):{
                if (resultCode == RESULT_OK) {
                    cooldownMessage = data.getStringExtra("MESSAGE");
                    cooldownTime = data.getLongExtra("INTERVAL", 0L);
                    tvCooldown.setText(convertLongToTimeString(cooldownTime)+"    "+cooldownMessage);
                    break;
                }
            }
            case (TIME_SELECT_INTERVAL_RESULT):{
                if (resultCode == RESULT_OK) {
                    int i=Integer.parseInt(data.getStringExtra("TAG"));
                    System.out.println("1111111111111111111111111111111111111111111111111111          "+i);
                    messages[i] = data.getStringExtra("MESSAGE");
                    intervals[i] = data.getLongExtra("INTERVAL", 0L);
                    tvIntervals[i].setText(convertLongToTimeString(intervals[i])+"    "+messages[i]);
                    break;
                }
            }
        }
    }

    public long convertTimeStringToLong(String s){
        //takes a string as hh:mm:ss and returns a number of seconds
        long sec=Long.parseLong(s.substring(6));
        long min=Long.parseLong(s.substring(3,5));
        long hour=Long.parseLong(s.substring(0,2));
        long t=sec+min*60+hour*3600;
        return t;
    }

    public String convertLongToTimeString(long l){
        //takes number of seconds and returns hh:mm:ss
        long secs = l;
        long mins = secs / 60;
        secs = secs % 60;
        long hours=mins/60;
        mins=mins%60;
        return (String.format("%02d",hours)+":"
                + String.format("%02d",mins) + ":"
                + String.format("%02d", secs));
    }

}

