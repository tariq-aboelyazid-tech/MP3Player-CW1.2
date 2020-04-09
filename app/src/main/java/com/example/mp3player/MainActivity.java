package com.example.mp3player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mp3player.PlayerService.MyLocalBinder;

public class MainActivity extends AppCompatActivity {

    public SeekBar progressBarDuration;
    PlayerService
            mp3Player = new PlayerService();
    String filePath;

    Intent playerIntent;
    private ImageView loadBtn,pauseBtn,stopBtn;
    protected TextView timeView, durationView;
    protected boolean isPlaying = false,isBound;
    private boolean STOP_FREQ_UPDATE = false;


    /*Check if music is playing*/
    public boolean isPlaying(){
        if(mp3Player.getMP3State().equals(MP3Player.MP3PlayerState.PLAYING)){
            isPlaying = true;
           // pauseBtn.setText("PAUSE");
            pauseBtn.setImageResource(R.drawable.ic_pause);

            if(filePath == null){
                filePath = mp3Player.getFilePathMP3File();
                durationView.setText(getTimeFormated(mp3Player.getMp3Duration()));

            }
        }else{

            isPlaying = false;
           // pauseBtn.setText("PLAY");
            pauseBtn.setImageResource(R.drawable.ic_play);
        }
        return isPlaying;
    }

    // Update progress bar every second respective to music played
    private Runnable updateEverySecond = new Runnable() {
        @Override
        public void run() {
            if (!STOP_FREQ_UPDATE && (mp3Player.getMp3Duration() != 0)) {
                int progressValue = mp3Player.getMp3Progress() *
                        progressBarDuration.getMax() / mp3Player.getMp3Duration();

                progressBarDuration.setProgress(progressValue);

                if (isPlaying()) {
                    progressBarDuration.postDelayed(updateEverySecond, 1000);
                }
                // update time TextViews
                timeView.setText(getTimeFormated(mp3Player.getMp3Progress()));
                durationView.setText(getTimeFormated(mp3Player.getMp3Duration()));
                progressBarDuration.setEnabled(true);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        loadBtn = findViewById(R.id.loadBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        stopBtn = findViewById(R.id.stopBtn);




        timeView = findViewById(R.id.timeView);
        durationView = findViewById(R.id.durationView);
        progressBarDuration = findViewById(R.id.progressBarDuration);

        progressBarDuration.setEnabled(false);

        /*
        * Load Music file to player from files
        * */
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/mp3/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select MP3 file"), 0);
            }
        });

        /*
        * Pause and Play Music Btn
        * */
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) {
                    mp3Player.pauseMp3File();
                    pauseBtn.setImageResource(R.drawable.ic_play);
                    isPlaying = false;

                } else if (!isPlaying() && filePath != null) {
                    mp3Player.playMp3File();
                    pauseBtn.setImageResource(R.drawable.ic_pause);

                    progressBarDuration.postDelayed(updateEverySecond,1000);
                    isPlaying = true;
                }
            }
        });
        /*
        * Stop Music Btn
        * */
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePath != null || isPlaying()) {
                    mp3Player.stopMp3File();
                    durationView.setText("00:00");
                    timeView.setText("00:00");
                    progressBarDuration.setProgress(0);
                    progressBarDuration.setEnabled(false);
                    isPlaying = false;
                }
            }
        });


        progressBarDuration.postDelayed(updateEverySecond, 1000);
        timeView.setText(getTimeFormated(mp3Player.getMp3Progress()));

        /*Change mp3 progress by changing seekbar position*/
        progressBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                /*Changes time as user changes music progress position*/
                if (isPlaying()) {

                    mp3Player.setTimeMp3File(mp3Player.getMp3Duration()
                            *   progressBarDuration.getProgress() / progressBarDuration.getMax());
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK && data != null) {
                /*
                * Stops current music and load new music to player
                * */
                if (isPlaying()) {

                    mp3Player.stopMp3File();
                    isPlaying = false;

                }
                filePath = data.getData().toString();
                mp3Player.loadMp3File(this, filePath);
                mp3Player.setMainActivity(this);
                progressBarDuration.setEnabled(true);
                progressBarDuration.setProgress(mp3Player.getMp3Progress());
                progressBarDuration.postDelayed(updateEverySecond, 1000);
                durationView.setText(getTimeFormated(mp3Player.getMp3Duration()));
                isPlaying = true;

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*Prevent service leakage */
        if(isBound){
            unbindService(myConnection);
            isBound = false;
        }
    }



    @Override
    protected void onStart() {


        /*Change mp3 progress by changing seekbar position*/
        progressBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                /*Changes time as user changes music progress position*/
                if (isPlaying()) {

                    mp3Player.setTimeMp3File(mp3Player.getMp3Duration()
                            *   progressBarDuration.getProgress() / progressBarDuration.getMax());
                }

            }
        });
       //To update progress after destroying application and reopen from notification
        progressBarDuration.postDelayed(updateEverySecond, 1000);
        super.onStart();
        startServiceClass();


    }




    /*
    * Start Service with Notification UI to control Music
    * in Background
    * */
    public void startServiceClass(){
        if(playerIntent == null) {
            playerIntent = new Intent(this, PlayerService.class);
            playerIntent.setAction("START_SERVICE");
            ContextCompat.startForegroundService(this, playerIntent);
            bindService(playerIntent, myConnection, Context.BIND_AUTO_CREATE);
        }

    }




    /*Stop Service when close notification*/
    public void stopServiceClass(){
        Intent playerIntent = new Intent(this,PlayerService.class);
        stopService(playerIntent);

    }

    /*
    * Connect mainactivity to service
    * */
    protected ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MyLocalBinder binder = (MyLocalBinder) service;
            Log.d("MP3Player", "Service CONNECTED");
            mp3Player = binder.getService();
            isBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MP3Player", "Service DISCONNECTED");
            isBound = false;
        }
    };

    /*
    * Formats duration and progress into hours, minutes and seconds
    * */
    public String getTimeFormated(int duration) {
        String formatedTime = "", secFormat = "", minFormated = "00", hourFormated = "";
        int sec = (duration / 1000) % 60;
        int min = (duration / (1000 * 60)) % 60;
        int hour = (duration / (1000 * 3600)) % (24 * 3600);


        if (sec > 9) {
            secFormat = String.valueOf(sec);
        } else if (sec < 10) {
            secFormat = "0" + sec;
        }

        if (min > 9) {
            minFormated = String.valueOf(min);
        } else if (min < 10) {
            minFormated = "0" + min;
        }
        formatedTime = minFormated + ":" + secFormat;

        if (hour > 0) {
            hourFormated = String.valueOf(hour);
            formatedTime = hourFormated + ":" + formatedTime;
        }

        return formatedTime;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        //super.onBackPressed();
    }
}
