package com.example.thi_gk_18062691_nguyentuananh;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView imgNhac;
    ImageButton btnPlay;
    private ServiceConnection serviceConnection;

    private boolean isConnected;
    private MyService myService;
    private int count = 0;
    private int totalTime = 0;
    TextView txtStart;
    TextView txtEnd;
    private ProgressBar progressB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgNhac = findViewById(R.id.imgnhac);
        txtStart = findViewById(R.id.txtStart);
        txtEnd = findViewById(R.id.txtEnd);
        progressB = findViewById(R.id.progressBar);
        totalTime = MediaPlayer.create(getBaseContext(), R.raw.bai).getDuration();
        Date date = new Date(totalTime);
        txtEnd.setText("" + milliSecondsToTimer((long) totalTime));
        Animation animation =
                AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.aniblink);

        imgNhac.startAnimation(animation);
        connectService();
        initView();
    }
    private void connectService() {

        Intent intent = new Intent(this, MyService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyService.MyBinder myBinder = (MyService.MyBinder) service;

                myService = myBinder.getService();
                isConnected = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnected = false;
                myService = null;
            }
        };
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    void initView()
    {
        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isConnected){
                    return;
                }

                // play and stop
                if(count++ % 2 == 0) {
                    myService.playMusic();
                    Toast.makeText(myService, "Play music", Toast.LENGTH_SHORT).show();
                    if(myService.getMp().isPlaying()) {
                        totalTime = totalTime = myService.getMp().getDuration();
                        txtStart.post(mUpdateTime);
                        progressB.post(mUpdateProgress);
                    }

                } else {
                    myService.stopMusic();
                    Toast.makeText(myService, "Stop music", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }
    private Runnable mUpdateProgress = new Runnable() {
        public void run() {
            int currentDuration;
            if (myService.getMp().isPlaying()) {
                currentDuration = myService.getMp().getCurrentPosition();
                double time = currentDuration * 1.0 / totalTime * 100;
                progressB.setProgress((int) time);
                progressB.postDelayed(this, 1000);
            }else {
                progressB.removeCallbacks(this);
            }
        }
    };

    private Runnable mUpdateTime = new Runnable() {
        public void run() {
            int currentDuration;
            if (myService.getMp().isPlaying()) {
                currentDuration = myService.getMp().getCurrentPosition();
                updatePlayer(currentDuration);
                txtStart.postDelayed(this, 1000);
            }else {
                txtStart.removeCallbacks(this);
            }
        }
    };

    private void updatePlayer(int currentDuration){
        txtStart.setText("" + milliSecondsToTimer((long) currentDuration));
    }

    public  String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) finalTimerString += (hours > 9 ? hours : "0" + hours) + ":";
        finalTimerString += (minutes > 9 ? minutes : "0" + minutes) + ":";
        finalTimerString += (seconds > 9 ? seconds : "0" + seconds);

        // return timer string
        return finalTimerString;
    }
}