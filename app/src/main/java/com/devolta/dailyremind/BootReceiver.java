package com.devolta.dailyremind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.devolta.dailyremind.RecyclerData.Card;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    private final ArrayList<Card> cards = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {

        final Calendar calendar = Calendar.getInstance();

        if (cards.isEmpty()) {
            try {
                String path = context.getFilesDir().getAbsolutePath() + "/" + "Arraylist 1";
                File file = new File(path);
                if (file.exists()) {
                    FileInputStream fileInputStream;
                    fileInputStream = context.openFileInput("Arraylist 1");
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    try {
                        int size = objectInputStream.readInt();
                        for (int i = 0; i < size; i++) {
                            Card card = (Card) objectInputStream.readObject();
                            cards.add(card);
                        }
                        objectInputStream.close();
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        e.printStackTrace();
                        objectInputStream.close();
                    }
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            if (context != null) {
                Log.d("Reboot", "Reboot complete");

                AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                //restart alarm for every card
                try {
                    for (int i = cards.size(); i >= 0; i--) {
                        Intent intent1 = new Intent(context, AlarmReceiver.class);

                        int number = i + 1;
                        SharedPreferences prefs = context.getSharedPreferences(AddReminder.class.getSimpleName() + " " + number, Context.MODE_PRIVATE);

                        String remindText = prefs.getString("Text", "null");
                        intent1.putExtra("RemindText", remindText);
                        Log.d("Name", AddReminder.class.getSimpleName() + " " + number);
                        int intentNumber = prefs.getInt("intentNumber", 0);
                        String repeat = prefs.getString("repeat", "null");
                        long calendarTime = prefs.getLong("CalendarTime", 0);

                        Log.d("Time", "" + calendarTime + "   " + calendar.getTimeInMillis());

                        if (calendarTime > calendar.getTimeInMillis()) {

                            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                            if (repeat.equalsIgnoreCase("true")) {

                                long repeat_quantity = prefs.getLong("Repeat_Quantity", 0);
                                int quantity = prefs.getInt("Quantity", 0);
                                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendarTime, repeat_quantity * quantity, alarmIntent);

                            } else {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                                } else {
                                    Log.d("Test", "" + calendar.getTimeInMillis());
                                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                                }

                            }
                        }

                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
