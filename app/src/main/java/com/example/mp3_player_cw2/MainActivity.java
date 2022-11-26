package com.example.mp3_player_cw2;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    MP3Player mp3Player;
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

        playButton.setOnClickListener(v -> myService.playSong());

        stopButton.setOnClickListener(v -> {
            myService.stopSong();
            endTime.setText("0 sec");
            songName.setText("Song Name");
        });

        pauseButton.setOnClickListener(v -> myService.pauseSong());

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

    @SuppressLint("SetTextI18n")
    public void setUpSeekBar(){
        progress = myService.getProgress()/1000;

        seekBar.setProgress(progress);
        startTime.setText(progress + " sec");

        runnable = this::setUpSeekBar;
        handler.postDelayed(runnable, 0);
    }

    private void initWidgets() {
        mp3Player = new MP3Player();
        pauseButton = findViewById(R.id.pauseButton);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        songName = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekBar);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        handler = new Handler();
    }

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

                stopSong();
                playSong(uri);
                setEndTime();
                setSongPath();
                setUpSeekBar();
        });
    }

    private void setSongPath() {
        songName.setText(myService.getFilePath());
    }

    @SuppressLint("SetTextI18n")
    private void setEndTime() {
        endTime.setText(myService.getDuration() + " sec");
        seekBar.setMax(Integer.parseInt(myService.getDuration()));
    }

    private void playSong(String uri) {
        myService.playSong(uri);
    }

    private void stopSong(){
        myService.stopSong();
    }
    public void showNotification(){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, intent, 0);
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREV);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent
        , PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
