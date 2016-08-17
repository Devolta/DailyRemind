package com.devolta.dailyremind;

import android.app.Activity;
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
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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

import com.devolta.dailyremind.RecyclerData.Card;
import com.devolta.devoltalibrary.Calculate;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.grantland.widget.AutofitTextView;


public class AddReminder extends AppCompatActivity {

    private static final List<Integer> intentNumber2 = MainActivity.intentNumber2;
    private final Calculate calculate = new Calculate();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat stf;
    private ArrayList<Card> cards;
    private String remindText;
    private AutofitTextView selectedDateView;
    private final DatePickerDialog.OnDateSetListener dateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            selectedDateView.setText(sdf.format(calendar.getTime()));
        }
    };
    private AutofitTextView selectedTimeView;
    private final TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {


        public void onTimeSet(TimePicker view, int hour, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                calendar.set(Calendar.AM_PM, Calendar.AM);
            } else {
                calendar.set(Calendar.AM_PM, Calendar.PM);
            }

            selectedTimeView.setText(stf.format(calendar.getTime()));
        }
    };
    private AppCompatEditText quantity_et;
    private int year;
    private int month;
    private int day;
    private boolean repeat;
    private boolean vibrate;
    private long repeat_quantity = 0;
    private AppCompatSpinner spinner_mode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LeakCanary.install(this.getApplication());

        setContentView(R.layout.activity_add_reminder);

        selectedDateView = (AutofitTextView) findViewById(R.id.date);
        selectedTimeView = (AutofitTextView) findViewById(R.id.time);
        SwitchCompat repeat_switch = (SwitchCompat) findViewById(R.id.repeat_switch);
        SwitchCompat vibrate_switch = (SwitchCompat) findViewById(R.id.vibrate_switch);
        spinner_mode = (AppCompatSpinner) findViewById(R.id.spinner_mode);
        quantity_et = (AppCompatEditText) findViewById(R.id.quantity_et);

        boolean is24Hour;
        if (!DateFormat.is24HourFormat(this)) {
            stf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            is24Hour = false;
        } else {
            stf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            is24Hour = true;
        }

        selectedDateView.setText(sdf.format(calendar.getTime()));
        selectedTimeView.setText(stf.format(calendar.getTime()));

        Bundle b = this.getIntent().getExtras();
        cards = (ArrayList<Card>) b.getSerializable("cards");

        addClickListener(is24Hour);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.mode_spinner, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mode.setAdapter(adapter);
        spinner_mode.setOnItemSelectedListener(new ItemModeSelectedListener());

        repeat_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                repeat = isChecked;
            }
        });

        vibrate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                vibrate = isChecked;
            }
        });
        vibrate_switch.setChecked(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.new_reminder);
        }

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

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
            Intent intent = new Intent(AddReminder.this, MainActivity.class);
            Bundle b = new Bundle();

            long now = new Date().getTime();
            long then = calendar.getTime().getTime();
            Card card = new Card();

            String remainingTime = calculate.calcTimeDiff(then, now); //get remaining time

            AppCompatEditText editText = (AppCompatEditText) findViewById(R.id.remindText);
            remindText = editText.getText().toString();

            card.cardText(remindText);
            card.cardDate(selectedDateView.getText().toString());
            card.cardRemainingTime(remainingTime);
            card.cardTime(selectedTimeView.getText().toString());

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
                    //write reminder info to file
                    if (quantity_et.getText().length() == 0) {
                        Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content), R.string.quantity_missing, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        return false;
                    }
                    String quantity_str = quantity_et.getText().toString();
                    int quantity = Integer.parseInt(quantity_str);
                    saveInfo(quantity_str);

                    cards.add(card);
                    b.putSerializable("cards", cards);
                    intent.putExtras(b);

                    SharedPreferences prefs = getSharedPreferences(AddReminder.class.getSimpleName() + " " + cards.size(), Context.MODE_PRIVATE);
                    int intentNumber = cards.size() - 1;
                    intentNumber2.add(cards.size() - 1);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", intentNumber);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Text", remindText);
                    editor.putInt("intentNumber", intentNumber);
                    editor.putString("repeat", "" + true);
                    editor.putLong("CalendarTime", calendar.getTimeInMillis());
                    editor.putLong("Repeat_Quantity", 0);
                    editor.putInt("Quantity", 0);
                    editor.apply();

                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeat_quantity * quantity, alarmIntent);
                    setResult(Activity.RESULT_OK, intent);
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
                    //write reminder info to file
                    saveInfo("0");

                    cards.add(card);
                    b.putSerializable("cards", cards);
                    intent.putExtras(b);

                    SharedPreferences prefs = getSharedPreferences(AddReminder.class.getSimpleName() + " " + cards.size(), Context.MODE_PRIVATE);
                    int intentNumber = cards.size() - 1;
                    intentNumber2.add(cards.size() - 1);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", intentNumber);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Text", remindText);
                    editor.putInt("intentNumber", intentNumber);
                    editor.putString("repeat", "" + false);
                    editor.putLong("CalendarTime", calendar.getTimeInMillis());
                    editor.apply();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    } else {
                        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                    }
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    //write all necessary infos about a reminder to a file
    private void saveInfo(String quantity) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "Reminder" + " " + cards.size()));
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            try {
                bufferedWriter.write(remindText);
                bufferedWriter.newLine();
                bufferedWriter.write(selectedTimeView.getText().toString());
                bufferedWriter.newLine();
                bufferedWriter.write(selectedDateView.getText().toString());
                bufferedWriter.newLine();
                if (repeat) {
                    bufferedWriter.write("true");
                } else {
                    bufferedWriter.write("false");
                }
                bufferedWriter.newLine();
                if (vibrate) {
                    bufferedWriter.write("true");
                } else {
                    bufferedWriter.write("false");
                }
                bufferedWriter.newLine();
                bufferedWriter.write(quantity);
                bufferedWriter.newLine();
                bufferedWriter.write(spinner_mode.getSelectedItem().toString());
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
                bufferedWriter.close();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addClickListener(final boolean is24hour) {

        selectedTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddReminder.this, onTimeSetListener, hour, minute, is24hour);
                timePickerDialog.show();
            }
        });

        selectedDateView.setOnClickListener(new View.OnClickListener() {
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

    private class ItemModeSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            //String mode = parent.getItemAtPosition(pos).toString();

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
