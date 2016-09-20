package com.playgrounds.audioplayground;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final int[] ALL_STREAMS = new int[]{AudioManager.STREAM_ALARM, AudioManager.STREAM_DTMF, AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM, AudioManager.STREAM_VOICE_CALL};
    private static final String LOG_TAG = "SoundTestAct";
    private final String[] modes = {
            "MODE_CURRENT",
            "MODE_INVALID",
            "MODE_IN_CALL",
            "MODE_IN_COMMUNICATION",
            "MODE_NORMAL",
            "MODE_RINGTONE"
    };
    private Timer timer;
    private boolean isPaused = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateRingerState();
        Spinner spinner = (Spinner) findViewById(R.id.audioModeSpinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item);

        adapter.addAll(modes);
        spinner.setOnItemSelectedListener(new ModeSpinnerListener(this, modes));

        spinner.setAdapter(adapter);
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
                TextView volumes = (TextView) findViewById(R.id.volumeModes);
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
                StreamDesc[] streams = new StreamDesc[]{
                        new StreamDesc(AudioManager.STREAM_ALARM, "ALARM"),
                        new StreamDesc(AudioManager.STREAM_DTMF, "DTMF"),
                        new StreamDesc(AudioManager.STREAM_MUSIC, "MUSIC"),
                        new StreamDesc(AudioManager.STREAM_NOTIFICATION, "NOTIFICATION"),
                        new StreamDesc(AudioManager.STREAM_RING, "RING"),
                        new StreamDesc(AudioManager.STREAM_SYSTEM, "SYSTEM"),
                        new StreamDesc(AudioManager.STREAM_VOICE_CALL, "VOICE_CALL")
                };

                StringBuilder builder = new StringBuilder();
                for (StreamDesc stream : streams) {
                    builder.append(String.format(Locale.getDefault(), "%-15s: %d/%d\n", stream.streamName, audioService.getStreamVolume(stream.streamId), audioService.getStreamMaxVolume(stream.streamId)));
                }

                volumes.setText(builder.toString());

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

    public void onSendPing(View view) {
        android.support.v4.app.NotificationCompat.Builder notificationBuilder = new android.support.v4.app.NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Notification title")
                .setContentText("Notification context")
                .setAutoCancel(true)
                .setOngoing(false)
                .setTicker("Notification ticker")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Integer notificationId = 2;
        Log.d(LOG_TAG, "Requesting notification ");
        final Notification notification = notificationBuilder.build();
        mixinLedLights(notification, 0xFF00FF, 5000, 100);
        notificationManager.notify("Notification tag", notificationId, notification);
    }

    public void onPlaySound(View view) {
        final Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Don't try this at home - checking threading
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Ringtone ringtone = RingtoneManager.getRingtone(MainActivity.this, uri);
                ringtone.play();
            }
        }).start();
    }

    public void mixinLedLights(Notification notification, int colourARGB, int offMs, int onMs) {
        notification.ledARGB = colourARGB;
        notification.ledOffMS = offMs;
        notification.ledOnMS = onMs;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
    }

    private class StreamDesc {
        private final int streamId;
        private final String streamName;

        public StreamDesc(int streamId, String streamName) {
            this.streamId = streamId;
            this.streamName = streamName;
        }
    }

    private class ModeSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final Activity activity;
        private final String[] modes;

        public ModeSpinnerListener(Activity activity, String[] modes) {
            this.activity = activity;
            this.modes = modes;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
            try {
                int mode = AudioManager.class.getField(modes[index]).getInt(AudioManager.class);
                ((AudioManager) activity.getSystemService(Context.AUDIO_SERVICE)).setMode(mode);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                ignored.printStackTrace();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
