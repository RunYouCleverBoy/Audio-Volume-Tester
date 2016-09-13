package com.playgrounds.audioplayground;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final int[] ALL_STREAMS = new int[]{AudioManager.STREAM_ALARM, AudioManager.STREAM_DTMF, AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM, AudioManager.STREAM_VOICE_CALL};
    private Timer timer;
    private boolean isPaused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateRingerState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        killTimer();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        killTimer();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPaused) {
                    killTimer();
                } else {
                    updateRingerState();
                }
            }
        }, 1000, 1000);
    }

    private void killTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateRingerState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
                TextView ringerStatus = (TextView)findViewById(R.id.ringerMode);
                int ringerMode = audioService.getRingerMode();
                String ringerState = "";
                if ((ringerMode & AudioManager.RINGER_MODE_NORMAL) != 0) {
                    ringerState += " NORMAL ";
                }
                if ((ringerMode & AudioManager.RINGER_MODE_VIBRATE) != 0) {
                    ringerState += " VIBRATE ";
                }
                if ((ringerMode == AudioManager.RINGER_MODE_SILENT)) {
                    ringerState += " SILENT ";
                }

                String audioMode = modeName(audioService.getMode());

                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                String date = dateFormat.format(new Date());
                ringerStatus.setText(String.format(Locale.getDefault(), "Time: %s Ringer: %s Mode %s", date, ringerState, audioMode));
            }
        });
    }

    private String modeName(int mode) {
        return (mode == AudioManager.MODE_CURRENT) ? "CURRENT"
                : (mode == AudioManager.MODE_INVALID) ? "Invalid"
                : (mode == AudioManager.MODE_IN_CALL) ? "In call"
                : (mode == AudioManager.MODE_IN_COMMUNICATION) ? "In communication"
                : (mode == AudioManager.MODE_NORMAL) ? "Normal"
                : (mode == AudioManager.MODE_RINGTONE) ? "Ringtone"
                : "Unknown (" + mode + ")";
    }

    public void onShout(View view) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        for (int streamType: ALL_STREAMS) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(streamType, audioManager.getStreamMaxVolume(streamType), AudioManager.FLAG_PLAY_SOUND);
        }
    }

    public void onShutIt(View view) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        for (int streamType: ALL_STREAMS) {
            audioManager.setStreamVolume(streamType, 0, AudioManager.FLAG_VIBRATE);
        }
    }
}
