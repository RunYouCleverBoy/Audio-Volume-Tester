package com.playgrounds.audioplayground;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.util.Date;

public class ModeReceiver extends BroadcastReceiver {
    public ModeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d("RECEIVER", "Intent: " + intent.toString());
        } catch (Exception ignored) {}

        if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent runIntent = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1234, runIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification n = new Notification.Builder(context).setContentTitle("Audio mode changed").setContentText(new Date().toString()).setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_alarm_off_white_24dp).setAutoCancel(true).build();
            notificationManager.notify(1, n);
        }
    }
}
