package com.example.mp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;



public class PlayerService extends Service {

    //Initializing Variables
    public static final String CHANNEL_ID = "ServiceChannel";
    MainActivity mainActivity;
    MP3Player mp3Player = new MP3Player();
    protected String filePath = null;
    private final IBinder myBinder = new MyLocalBinder();

    public PlayerService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {




        /*When application runs checks if service is running or not
        * if not it runs with Notification UI to control Services*/
        if(intent.getAction() != null && intent.getAction().equals("START_SERVICE")) {

            createNotificationChannel();
            //Create Notification
            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);

            //Pause && Play Intent Button
            Intent pauseIntent = new Intent(this,PlayerService.class);
            pauseIntent.setAction("ACTION_PAUSE");
            PendingIntent pendingPauseIntent = PendingIntent.getService(this,0,
                    pauseIntent,0);
            NotificationCompat.Action pauseAction = new
                    NotificationCompat.Action(R.drawable.ic_pause,
                    "PAUSE",pendingPauseIntent);

            //Close Button on Notification
            Intent closeIntent = new Intent(this,PlayerService.class);
            closeIntent.setAction("ACTION_CLOSE");
            PendingIntent closePendingIntent = PendingIntent.getService(this,0,
                    closeIntent,0);
            NotificationCompat.Action closeAction = new
                    NotificationCompat.Action(android.R.drawable.ic_menu_close_clear_cancel,
                    "CLOSE",closePendingIntent);




            //Build Notification
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Tariq MP3 Player")
                    .setContentText(" ")
                    .setSmallIcon(R.drawable.ic_music)
                    .setContentIntent(pendingIntent)
                    .addAction(pauseAction)
                    .addAction(closeAction)
                    .build();

            //Build Notification in Foreground
            startForeground(1, notification);
        }else if(intent.getAction() != null && intent.getAction().equals("ACTION_PAUSE"))
        {
            /*
            * Pause and Play */
            if(getMP3State().equals(MP3Player.MP3PlayerState.PAUSED))
            {
                mp3Player.play();
            }else if(getMP3State().equals(MP3Player.MP3PlayerState.PLAYING))
            {
                mp3Player.pause();
            }

        }else if(intent.getAction() != null && intent.getAction().equals("ACTION_CLOSE")){
            stopMp3File();
            stopSelf();
            mainActivity.stopServiceClass();
        }
        return START_NOT_STICKY;
    }


    /*get file path from mp3Player Activity*/
    public String getFilePathMP3File()
    {
        filePath = mp3Player.getFilePath();
        return filePath;
    }
    /*
    * Create Notification Channel
    * */
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID
                            ,"Example Service"
                            , NotificationManager.
                            IMPORTANCE_DEFAULT);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);


        }

    }
    //set the activity to main to control mainactivity
    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    //Play Music
    public void playMp3File() {
        mp3Player.play();
        Log.d("PlayerService","Play File");
    }
    //Pause Music
    public void pauseMp3File() {
        mp3Player.pause();
        Log.d("PlayerService","Pause File");
    }
    //Stop Music
    public void stopMp3File() {
        mp3Player.stop();
        filePath = null;
    }
    //Load mp3player and start music
    public void loadMp3File(MainActivity mainActivity,String filePath){
        mp3Player.load(mainActivity,filePath);
    }
    //update progress of music
    public void setTimeMp3File(int ms) {
        mp3Player.goToTime(ms);
    }



    //Return current state of music
    public MP3Player.MP3PlayerState getMP3State(){
        return mp3Player.getState();

    }




    public class MyLocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    //return progress of music
    public int getMp3Progress() { return mp3Player.getProgress(); }
    //returns total duration of music
    public int getMp3Duration() { return mp3Player.getDuration(); }

    @Override
    public void onCreate() {
        Log.d("PlayerService","onCreate()");
    }

    @Override
    public void onDestroy() {

        Log.d("PlayerService","onDestroy()");
        super.onDestroy();

    }

}
