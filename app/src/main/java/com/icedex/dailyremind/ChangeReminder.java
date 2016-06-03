package com.icedex.dailyremind;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
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
import android.widget.TimePicker;

import com.icedex.dailyremind.RecyclerData.Card;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import com.squareup.leakcanary.LeakCanary;

public class ChangeReminder extends AppCompatActivity {

    private static final ArrayList<Card> cards = MainActivity.cards;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private final SimpleDateFormat stf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat sdtf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();
    private AppCompatTextView SelectedTimeView;
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
    private AppCompatEditText quantity_et;
    private AppCompatEditText editText;
    private AppCompatTextView SelectedDateView;
    private final DatePickerDialog.OnDateSetListener dateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            SelectedDateView.setText(sdf.format(calendar.getTime()));
        }
    };
    private int year;
    private int month;
    private int day;
    private boolean repeat;
    private long repeat_quantity;

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

        //LeakCanary.install(this.getApplication());

        setContentView(R.layout.activity_add_reminder);

        String text = getIntent().getStringExtra("TEXT");
        String time = getIntent().getStringExtra("TIME");
        String date = getIntent().getStringExtra("DATE");

        SelectedDateView = (AppCompatTextView) findViewById(R.id.date);
        SelectedTimeView = (AppCompatTextView) findViewById(R.id.time);
        SwitchCompat repeat_switch = (SwitchCompat) findViewById(R.id.repeat_switch);
        AppCompatSpinner spinner_mode = (AppCompatSpinner) findViewById(R.id.spinner_mode);
        quantity_et = (AppCompatEditText) findViewById(R.id.quantity_et);
        editText = (AppCompatEditText) findViewById(R.id.remindText);

        repeat_switch.setChecked(getIntent().getBooleanExtra("REPEAT", false));
        Log.d("Switch", "" + getIntent().getBooleanExtra("REPEAT", false));

        SelectedTimeView.setText(time);
        try {
            Date date1 = sdtf.parse(date + " " + time);
            calendar.setTime(date1);
        } catch (ParseException e) {
            Log.d("PARSEEXCEPTION:", " " + e);
        }
        SelectedDateView.setText(date);
        editText.setText(text);
        quantity_et.setText(getIntent().getStringExtra("QUANTITY"));

        SelectedDateView.setPadding(35, 0, 50, 0);
        SelectedTimeView.setPadding(35, 0, 50, 0);

        addClickListener();

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
        spinner_mode.setSelection(getIndex(spinner_mode, getIntent().getStringExtra("MODE")));

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

    private int getIndex(AppCompatSpinner s1, String mode) {

        int index = 0;

        for (int i = 0; i < s1.getCount(); i++) {
            if (s1.getItemAtPosition(i).equals(mode)) {
                index = i;
            }
        }
        return index;
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
            int position = getIntent().getIntExtra("POSITION", 0);
            Intent intent = new Intent(this, MainActivity.class);

            Date now = new Date();
            Date then = calendar.getTime();
            Card card = new Card();

            String remainingTime = calculateTimeDifference(then.getTime(), now.getTime()); //get remaining time

            String remindText = editText.getText().toString();
            card.cardText(remindText);
            card.cardDate(SelectedDateView.getText().toString());
            card.cardRemainingTime(remainingTime);
            card.cardTime(SelectedTimeView.getText().toString());

            AlarmManager alarmMgr;
            PendingIntent alarmIntent;
            if (repeat) {
                if (remindText.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.missing_info, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.elapsed_time, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    String quantity_str = quantity_et.getText().toString();
                    int quantity = Integer.parseInt(quantity_str);

                    cards.set(position, card);
                    startActivity(intent);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", position);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeat_quantity * quantity, alarmIntent);
                    finish();
                }
            } else {
                if (remindText.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.missing_info, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                    Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.elapsed_time, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    cards.set(position, card);
                    startActivity(intent);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", position);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    } else {
                        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    }

                    finish();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void addClickListener() {

        SelectedTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(ChangeReminder.this, onTimeSetListener, hour, minute, false);
                timePickerDialog.show();
            }
        });

        SelectedDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(ChangeReminder.this, dateOnDateSetListener, year, month, day);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
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
