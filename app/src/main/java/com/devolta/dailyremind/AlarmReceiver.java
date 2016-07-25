package com.devolta.dailyremind;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        SharedPreferences prefs = null;
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);

            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            manager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION), 0);

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();

            String remindText = intent.getStringExtra("RemindText");
            Log.d("ALARM", "" + remindText);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle("DailyRemind")
                    .setContentText(remindText)
                    .setSmallIcon(R.drawable.plus);


            prefs = context.getSharedPreferences(AlarmReceiver.class.getSimpleName(), Context.MODE_PRIVATE);
            int notificationNumber = prefs.getInt("notificationNumber", 0);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationNumber, builder.build());

            SharedPreferences.Editor editor = prefs.edit();
            notificationNumber++;
            editor.putInt("notificationNumber", notificationNumber);
            editor.apply();

            wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
            wakeLock.release();
        } finally {
            if (prefs != null) {
                int position = intent.getIntExtra("RemindPosition", -1);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("Alarm " + position, true);
                editor.apply();
            }
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

    }

}
