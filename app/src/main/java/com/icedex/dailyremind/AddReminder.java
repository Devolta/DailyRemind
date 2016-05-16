package com.icedex.dailyremind;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.icedex.dailyremind.RecyclerData.Card;
import com.squareup.leakcanary.LeakCanary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddReminder extends AppCompatActivity {

    private static final ArrayList<Card> cards = MainActivity.cards;
    private static final ArrayList<Integer> intentNumber2 = MainActivity.intentNumber2;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private final SimpleDateFormat stf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();
    private String remindText;
    private TextView SelectedDateView;
    private final DatePickerDialog.OnDateSetListener dateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            SelectedDateView.setText(sdf.format(calendar.getTime()));
        }
    };
    private TextView SelectedTimeView;
    private final TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {


        public void onTimeSet(TimePicker view, int hour, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                calendar.set(Calendar.AM_PM, Calendar.AM);
            } else {
                calendar.set(Calendar.AM_PM, Calendar.PM);
            }

            SelectedTimeView.setText(stf.format(calendar.getTime()));
        }
    };
    private EditText quantity_et;
    private int year;
    private int month;
    private int day;
    private boolean repeat;
    private long repeat_quantity;
    private Spinner spinner_mode;

    private static String calculateTimeDifference(long then, long now) {
        if (then > now) {
            String timeIndicator;
            String timeIndicator2;
            long remainingTime1 = (then - now) / 1000;

            String remainingTime2 = "";
            long years;
            long months;
            long days;
            long hours;
            long minutes;

            if (remainingTime1 >= 31556952) {
                years = remainingTime1 / 31556952;
                if (years > 1) {
                    timeIndicator = "years";
                } else {
                    timeIndicator = "year";
                }

                months = remainingTime1 / 2592000 - (years * 12);
                if (months > 1) {
                    timeIndicator2 = "months";
                } else {
                    timeIndicator2 = "month";
                }
                remainingTime2 = years + timeIndicator + " " + months + timeIndicator2;
            } else if (remainingTime1 >= 2592000) {
                months = remainingTime1 / 2592000;
                if (months > 1) {
                    timeIndicator = "months";
                } else {
                    timeIndicator = "month";
                }

                days = remainingTime1 / 86400 - (months * 30);
                if (days > 1) {
                    timeIndicator2 = "days";
                } else {
                    timeIndicator2 = "day";
                }
                remainingTime2 = months + timeIndicator + " " + days + timeIndicator2;
            } else if (remainingTime1 >= 86400) {
                days = remainingTime1 / 86400;
                if (days > 1) {
                    timeIndicator2 = "days";
                } else {
                    timeIndicator2 = "day";
                }
                hours = remainingTime1 / 3600 - (days * 24);
                remainingTime2 = days + timeIndicator2 + " " + hours + "h";
            } else if (remainingTime1 >= 3600) {
                hours = remainingTime1 / 3600;
                minutes = remainingTime1 / 60 - (hours * 60);
                remainingTime2 = hours + "h " + minutes + "min";
            } else if (remainingTime1 >= 60) {
                minutes = remainingTime1 / 60;
                remainingTime2 = minutes + "min";
            }

            return remainingTime2 + " remaining";
        } else
            return "error";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LeakCanary.install(this.getApplication());

        setContentView(R.layout.activity_add_reminder);

        SelectedDateView = (TextView) findViewById(R.id.date);
        SelectedTimeView = (TextView) findViewById(R.id.time);
        Switch repeat_switch = (Switch) findViewById(R.id.repeat_switch);
        spinner_mode = (Spinner) findViewById(R.id.spinner_mode);
        quantity_et = (EditText) findViewById(R.id.quantity_et);

        SelectedDateView.setText(sdf.format(calendar.getTime()));
        SelectedDateView.setPadding(35, 0, 50, 0);

        SelectedTimeView.setText(stf.format(calendar.getTime()));
        SelectedTimeView.setPadding(35, 0, 50, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mode_spinner, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mode.setAdapter(adapter);
        spinner_mode.setOnItemSelectedListener(new modeItemSelectedListener());

        repeat_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                repeat = isChecked;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_add_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item_main clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_check) {
            Intent intent = new Intent(this, MainActivity.class);

            Date now = new Date();
            Date then = calendar.getTime();
            Card card = new Card();

            String remainingTime = calculateTimeDifference(then.getTime(), now.getTime()); //get remaining time

            EditText editText = (EditText) findViewById(R.id.remindText);
            remindText = editText.getText().toString();

            card.cardText(remindText);
            card.cardDate(SelectedDateView.getText().toString());
            card.cardRemainingTime(remainingTime);
            card.cardTime(SelectedTimeView.getText().toString());

            AlarmManager alarmMgr;
            PendingIntent alarmIntent;

            if (repeat) {
                if (remindText.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), "required information missing", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    String quantity_str = quantity_et.getText().toString();
                    int quantity = Integer.parseInt(quantity_str);
                    saveInfo(quantity_str);

                    cards.add(card);
                    startActivity(intent);

                    SharedPreferences prefs = getSharedPreferences(AddReminder.class.getSimpleName() + " " + cards.size(), Context.MODE_PRIVATE);
                    int intentNumber = prefs.getInt("intentNumber", 0);
                    intentNumber2.add(prefs.getInt("intentNumber", 0));

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Text", remindText);
                    editor.putInt("intentNumber", intentNumber);
                    editor.putString("repeat", "" + true);
                    editor.putLong("CalendarTime", calendar.getTimeInMillis());
                    editor.putLong("Repeat_Quantity", 0);
                    editor.putInt("Quantity", 0);
                    editor.apply();
                    intentNumber++;

                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeat_quantity * quantity, alarmIntent);
                    finish();
                }
            } else {
                if (remindText.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), "required information missing", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    saveInfo("0");
                    cards.add(card);
                    startActivity(intent);

                    SharedPreferences prefs = getSharedPreferences(AddReminder.class.getSimpleName() + " " + cards.size(), Context.MODE_PRIVATE);
                    Log.w("Name", AddReminder.class.getSimpleName() + " " + cards.size());
                    int intentNumber = prefs.getInt("intentNumber", 0);
                    intentNumber2.add(prefs.getInt("intentNumber", 0));

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Text", remindText);
                    editor.putInt("intentNumber", intentNumber);
                    editor.putString("repeat", "" + false);
                    editor.putLong("CalendarTime", calendar.getTimeInMillis());
                    editor.apply();
                    intentNumber++;

                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    finish();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveInfo(String quantity) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "Reminder" + " " + cards.size()));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(remindText);
            bufferedWriter.newLine();
            bufferedWriter.write(SelectedTimeView.getText().toString());
            bufferedWriter.newLine();
            bufferedWriter.write(SelectedDateView.getText().toString());
            bufferedWriter.newLine();
            if (repeat) {
                bufferedWriter.write("true");
            } else {
                bufferedWriter.write("false");
            }
            bufferedWriter.newLine();
            bufferedWriter.write(quantity);
            bufferedWriter.newLine();
            try {
                bufferedWriter.write(spinner_mode.getSelectedItem().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDatePickerDialog(View v) {

        SelectedDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddReminder.this, dateOnDateSetListener, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
    }

    public void showTimePickerDialog(View v) {

        SelectedTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminder.this, onTimeSetListener, hour, minute, false);
                timePickerDialog.show();
            }
        });
    }

    private class modeItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String mode = parent.getItemAtPosition(pos).toString();

            switch (pos) {
                case 0:
                    repeat_quantity = TimeUnit.MINUTES.toMillis(1);
                    break;
                case 1:
                    repeat_quantity = TimeUnit.HOURS.toMillis(1);
                    break;
                case 2:
                    repeat_quantity = TimeUnit.DAYS.toMillis(1);
                    break;
                case 3:
                    repeat_quantity = TimeUnit.DAYS.toMillis(7);
                    break;
                case 4:
                    repeat_quantity = TimeUnit.DAYS.toMillis(30);
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    }
}
