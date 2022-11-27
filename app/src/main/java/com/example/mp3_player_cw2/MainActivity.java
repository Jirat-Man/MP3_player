package com.example.mp3_player_cw2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ListView;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.example.mp3_player_cw2.myService.myLocalBinder;


public class MainActivity extends AppCompatActivity {
    ImageButton pauseButton;
    ImageButton playButton;
    ImageButton stopButton;
    TextView songName;
    TextView startTime;
    TextView endTime;
    SeekBar seekBar;
    int progress = 0;

    Intent serviceIntent;

    myService myService = new myService();
    boolean isConnected = false;

    Runnable runnable;
    Handler handler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidgets();
        listFile();

        playButton.setOnClickListener(v -> {                    //handle play button event
            myService.playSong();
            Notify();
        });

        stopButton.setOnClickListener(v -> {                    //handle stop button event
            myService.stopSong();
            endTime.setText("0 sec");
            songName.setText("Song Name");
            deleteNotification(this, 1);
        });

        pauseButton.setOnClickListener(v -> {                   //handle pause button event
            myService.pauseSong();
            deleteNotification(this, 1);
        });

        //handle seekbar event
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    myService.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //delete notification
    public static void deleteNotification(Context ctx, int notifyId) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
            nMgr.cancel(notifyId);
        }

    //Connect Activity to Service
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            myLocalBinder binder = (myLocalBinder) service;
            myService = binder.getBoundService();
            isConnected = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isConnected = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        serviceIntent = new Intent(getApplicationContext(), myService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    //Update seekbar with the progress of the song
    @SuppressLint("SetTextI18n")
    public void updateSeekbar(){
        progress = myService.getProgress()/1000;

        seekBar.setProgress(progress);
        startTime.setText(progress + " sec");

        runnable = this::updateSeekbar;
        handler.postDelayed(runnable, 0);
    }

    //initialise all the views on the main activity
    private void initWidgets() {
        pauseButton = findViewById(R.id.pauseButton);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        songName = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        handler = new Handler();
    }

    //list all the files in the device
    @SuppressLint("SetTextI18n")
    public void listFile(){
        final ListView lv = findViewById(R.id.listView);
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Audio.Media.IS_MUSIC + "!= 0",
                null,
                null);
        lv.setAdapter(new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                cursor,
                new String[] { MediaStore.Audio.Media.DATA},
                new int[] { android.R.id.text1 }));
        lv.setOnItemClickListener((myAdapter, myView, myItemInt, mylng) -> {
            Cursor c = (Cursor) lv.getItemAtPosition(myItemInt);
            @SuppressLint("Range") String uri =
                    c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));

                stopSong();     //stop song
                playSong(uri);  //playing song in uri
                setEndTime();   //set seekbar max and endTime textview
                setSongPath();  //set song path
                updateSeekbar();    //update seekbar
                Notify();       //notify users that a song is playing
        });
    }

    //create notification channel
    private void Notify() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "channel_id")
                .setSmallIcon(R.drawable.yellow)
                .setContentTitle("Song Playing")
                .setContentText(myService.getFilePath())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1,builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("channel_id", "channel_id", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);

                manager.createNotificationChannel(channel);
            }
    }

    //set textview as file path
    private void setSongPath() {
        songName.setText(myService.getFilePath());
    }

    //set textview as end time of the song
    @SuppressLint("SetTextI18n")
    private void setEndTime() {
        endTime.setText(myService.getDuration() + " sec");
        seekBar.setMax(Integer.parseInt(myService.getDuration()));
    }

    //start song
    private void playSong(String uri) {
        myService.playSong(uri);
    }

    //stop song
    private void stopSong(){
        myService.stopSong();
    }

    //delete notification when app is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteNotification(this, 1);
    }
}
