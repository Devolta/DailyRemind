package com.devolta.dailyremind;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.devolta.dailyremind.RecyclerData.Card;
import com.devolta.devoltalibrary.Calculate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class DailyRemindWidgetProvider extends AppWidgetProvider {

    private static ArrayList<Card> cards = new ArrayList<>();
    private final Calculate calculate = new Calculate();

    //retrieve cards Arraylist
    static void setCards(ArrayList<Card> data) {
        DailyRemindWidgetProvider.cards = data;
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {

            // Get the layout for the App Widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_dailyremind);

            Log.d("WIDGET", "Update called");
            if (!cards.isEmpty()) {

                SimpleDateFormat sdtf;
                if (!DateFormat.is24HourFormat(context)) {
                    sdtf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
                } else {
                    sdtf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                }

                ArrayList<String> times = new ArrayList<>();
                times.clear();
                for (int i = cards.size() - 1; i >= 0; i--) {
                    Card card = cards.get(i);

                    String time = card.getCardTime();
                    String date = card.getCardDate();

                    Date now = new Date();
                    Date then = null;
                    try {
                        then = sdtf.parse(date + " " + time);
                    } catch (ParseException e) {
                        Log.d("PARSEEXCEPTION:", " " + e);
                    }

                    String remainingTime = calculate.calcTimeDiff(then.getTime(), now.getTime());
                    remainingTime = remainingTime.replace("remaining", "");
                    times.add(remainingTime);
                }
                //sort all the remaining times from lowest to highest
                Collections.sort(times, Collections.<String>reverseOrder());
                String remainingTime = times.get(0);

                if (remainingTime.contentEquals("error")) {
                    views.setTextViewText(R.id.no_alarms_text, context.getText(R.string.no_alarm_text));
                    views.setViewVisibility(R.id.no_alarms_text, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.no_alarms_text, View.INVISIBLE);
                    views.setTextViewText(R.id.next_alarm_text, "Next Alarm in " + remainingTime);
                }
            } else {
                views.setTextViewText(R.id.no_alarms_text, context.getText(R.string.no_alarm_text));
                views.setViewVisibility(R.id.no_alarms_text, View.VISIBLE);
            }

            Intent update = new Intent(context, DailyRemindWidgetProvider.class);
            update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, 0, update, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.refresh_button, pendingUpdate);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, DailyRemindWidgetProvider.class);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(widget);

        onUpdate(context, appWidgetManager, widgetIds);
    }
}
