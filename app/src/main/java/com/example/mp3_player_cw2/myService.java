package com.example.mp3_player_cw2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class myService extends Service{

    private MP3Player mp3;
    String filePath;

    public myLocalBinder localBinder = new myLocalBinder();

    public myService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }


    public class myLocalBinder extends Binder {
        myService getBoundService(){
            return myService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mp3 = new MP3Player();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp3.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        filePath = intent.getStringExtra("Path");
        mp3.load(filePath);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        return START_STICKY;
    }

    public String getDuration(){
        return String.valueOf(mp3.getDuration()/1000);
    }

    public int getProgress(){
        return mp3.getProgress();
    }

    public String getFilePath(){
        return mp3.getFilePath();
    }
    public void playSong(String s){
        mp3.load(s);
    }
    public void stopSong(){
        mp3.stop();
    }
    public void pauseSong(){
        mp3.pause();
    }
    public void playSong(){
        mp3.play();
    }
    public void seekTo(int i){
        mp3.mediaPlayer.seekTo(i);
    }

}