package com.jerryrolfing.speechtimer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpeechActivity extends AppCompatActivity implements OnInitListener {

    private Button oneButton;
    private Button twoButton;

    private TextView tvWorkoutName;
    private TextView timerValue;
    private TextView intervalValue;
    private TextView intervalText;
    private ListView intervalList;

    private Handler customHandler = new Handler();

    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    boolean running = false;  //indicates if the timer is currently counting

    //To store information about current workout
    String workoutName="Workout 1";
    long warmup = 1L;
    String warmupMessage = "Begin warmup";
    long cooldown = 2L;
    String cooldownMessage = "Begin cooldown";
    String finalMessage = "Finished";
    long[] workoutIntervals = {5L, 4L, 3L,2L,1L,2L,3L,4L,5L,6L};
    String[] workoutMessages = {"Start of 1", "Start of 2", "Start of 3","Start of 4","Start of 5","Start of 6","Start of 7","Start of 8","Start of 9","Last interval"};
    int repeats = 4;

    //To store expanded interval list with all repeats and warmup/cooldown
    long[] intervals;
    String[] messages;

    String[] intervalListArray;
    ArrayAdapter<String> arrayAdapter;

    int interval = 0;

    private static final int TTS_INTENT_RESULT = 0;
    private static final int EDIT_INTERVALS_RESULT = 1;
    private TextToSpeech myTTS;

    String fileWorkoutName="workouts";     //file workouts get saved in

    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case (TTS_INTENT_RESULT):{
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(this, this);
                } else {
                    //no data - install it now
                    Intent installTTSIntent = new Intent();
                    installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
                break;
            }
            case (EDIT_INTERVALS_RESULT):{
                if (resultCode==RESULT_OK){
                    workoutName=data.getStringExtra("WORKOUTNAME");
                    warmupMessage=data.getStringExtra("WARMUPMESSAGE");
                    warmup=data.getLongExtra("WARMUP",0L);
                    cooldownMessage=data.getStringExtra("COOLDOWNMESSAGE");
                    cooldown=data.getLongExtra("COOLDOWN",0L);
                    repeats=data.getIntExtra("REPEATS",0);
                    workoutMessages=data.getStringArrayExtra("MESSAGES");
                    workoutIntervals=data.getLongArrayExtra("INTERVALS");
                    tvWorkoutName.setText(workoutName);
                    fillIntervalArray();
                    List<String> intervalArrayList = new ArrayList<>(Arrays.asList(intervalListArray));
                    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, intervalArrayList);
                    intervalList.setAdapter(arrayAdapter);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_INTENT_RESULT);

        tvWorkoutName= (TextView) findViewById(R.id.workoutName);
        timerValue = (TextView) findViewById(R.id.timerValue);
        intervalValue = (TextView) findViewById(R.id.intervalValue);
        intervalText = (TextView) findViewById(R.id.intervalText);
        intervalList = (ListView) findViewById(R.id.intervalList);

        tvWorkoutName.setText(workoutName);
        fillIntervalArray();
        List<String> intervalArrayList = new ArrayList<>(Arrays.asList(intervalListArray));
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, intervalArrayList);
        intervalList.setAdapter(arrayAdapter);

        oneButton = (Button) findViewById(R.id.oneButton);

        oneButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (!running){  //not running so button is acting as "Start"
                    running = true;
                    if (startTime == 0) {
                        fillIntervalArray();
                        speakTTS(messages[0]);
                    }
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    oneButton.setText("Pause");
                }
                else {  //button in acting as "Pause"
                    running=false;
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                    oneButton.setText("Resume");
                }

            }
        });

        twoButton = (Button) findViewById(R.id.twoButton);

        twoButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                running = false;
                interval = 0;                 //Reset timer to start over from beginning when start button is pressed
                startTime = 0;
                timeSwapBuff=0;
                intervalValue.setText("Finished");
                intervalText.setText("");
                timerValue.setText("00:00:00");
                customHandler.removeCallbacks(updateTimerThread);
                oneButton.setText("Start");
            }
        });
    }

    private void speakTTS(String speech) {

        //speak straight away
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            intervalValue.setText(convertLongToTimeString(intervals[interval]));
            intervalText.setText(messages[interval]);
            intervalList.setSelection(interval);
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;
            long updatedTimeTemp = intervals[interval] * 1000 - updatedTime;
            if (updatedTimeTemp <= 0) {
                interval = interval + 1;
                speakTTS(messages[interval]);
                if (interval == intervals.length) //Finished with last interval
                {
                    running = false;
                    interval = 0;                 //Reset timer to start over from beginning when start button is pressed
                    startTime = 0;
                    timeSwapBuff=0;
                    intervalValue.setText("Finished");
                    intervalText.setText(finalMessage);
                    timerValue.setText("00:00:00");
                    customHandler.removeCallbacks(updateTimerThread);
                    oneButton.setText("Start");
                    return;
                }
                startTime = SystemClock.uptimeMillis();
                timeSwapBuff = 0;
                updatedTime = 0;
                updatedTimeTemp = intervals[interval] * 1000 - updatedTime;
            }
            updatedTime = updatedTimeTemp;
            long secs = updatedTime / 1000;
            long mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText(convertLongToTimeString(secs)+"."+String.format("%03d",milliseconds));
            customHandler.postDelayed(this, 0);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speech, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_EditIntervals: {
                Intent intent = new Intent(this, EditIntervals.class);
                intent.putExtra("WORKOUTNAME", workoutName);
                intent.putExtra("WARMUPMESSAGE", warmupMessage);
                intent.putExtra("WARMUP", warmup);
                intent.putExtra("COOLDOWNMESSAGE", cooldownMessage);
                intent.putExtra("COOLDOWN", cooldown);
                intent.putExtra("INTERVALS", workoutIntervals);
                intent.putExtra("MESSAGES", workoutMessages);
                intent.putExtra("REPEATS", repeats);
                startActivityForResult(intent, EDIT_INTERVALS_RESULT);
                break;
            }
            case R.id.action_settings: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillIntervalArray() {
        int totalIntervals = 2 + repeats * workoutIntervals.length;
        intervals = new long[totalIntervals];
        messages = new String[totalIntervals + 1];

        intervals[0] = warmup;
        messages[0] = warmupMessage;
        int i = 1;
        for (int r = 0; r < repeats; r++) {
            for (int j = 0; j < workoutIntervals.length; j++) {
                intervals[i] = workoutIntervals[j];
                messages[i] = workoutMessages[j];
                i++;
            }
        }
        intervals[i] = cooldown;
        messages[i] = cooldownMessage;
        messages[i + 1] = finalMessage;

        intervalListArray = new String[intervals.length];
        for (int j = 0; j < intervals.length; j++) {
            intervalListArray[j] = convertLongToTimeString(intervals[j])+" "+messages[j];
        }
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