package com.devolta.dailyremind;

import android.app.Activity;
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
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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

import com.devolta.dailyremind.RecyclerData.Card;
import com.devolta.devoltalibrary.Calculate;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.grantland.widget.AutofitTextView;

public class ChangeReminder extends AppCompatActivity {

    private final Calculate calculate = new Calculate();
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdtf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();
    AppCompatSpinner spinner_mode;
    private SimpleDateFormat stf;
    private ArrayList<Card> cards;
    private String remindText;
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
    private AppCompatEditText editText;
    private AutofitTextView selectedDateView;
    private final DatePickerDialog.OnDateSetListener dateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(year, month, day);
            selectedDateView.setText(sdf.format(calendar.getTime()));
        }
    };
    private int year;
    private int month;
    private int day;
    private boolean repeat;
    private boolean vibrate;
    private long repeat_quantity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LeakCanary.install(this.getApplication());

        setContentView(R.layout.activity_add_reminder);

        String text = getIntent().getStringExtra("TEXT");
        String time = getIntent().getStringExtra("TIME");
        String date = getIntent().getStringExtra("DATE");

        selectedDateView = (AutofitTextView) findViewById(R.id.date);
        selectedTimeView = (AutofitTextView) findViewById(R.id.time);
        SwitchCompat repeat_switch = (SwitchCompat) findViewById(R.id.repeat_switch);
        SwitchCompat vibrate_switch = (SwitchCompat) findViewById(R.id.vibrate_switch);
        spinner_mode = (AppCompatSpinner) findViewById(R.id.spinner_mode);
        quantity_et = (AppCompatEditText) findViewById(R.id.quantity_et);
        editText = (AppCompatEditText) findViewById(R.id.remindText);

        repeat_switch.setChecked(getIntent().getBooleanExtra("REPEAT", false));
        vibrate_switch.setChecked(getIntent().getBooleanExtra("VIBRATE", false));

        final boolean is24Hour;
        if (!DateFormat.is24HourFormat(this)) {
            stf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            is24Hour = false;
        } else {
            stf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            is24Hour = true;
        }

        selectedTimeView.setText(time);
        try {
            Date date1 = sdtf.parse(date + " " + time);
            calendar.setTime(date1);
        } catch (ParseException e) {
            Log.e("PARSEEXCEPTION:", " " + e);
        }
        selectedDateView.setText(date);
        editText.setText(text);
        quantity_et.setText(getIntent().getStringExtra("QUANTITY"));

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
        spinner_mode.setSelection(getIndex(spinner_mode, getIntent().getStringExtra("MODE")));

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
            Intent intent = new Intent(ChangeReminder.this, MainActivity.class);
            Bundle b = new Bundle();

            long now = new Date().getTime();
            long then = calendar.getTime().getTime();
            Card card = new Card();

            String remainingTime = calculate.calcTimeDiff(then, now); //get remaining time

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
                    String quantity_str = quantity_et.getText().toString();
                    int quantity = Integer.parseInt(quantity_str);
                    saveInfo(quantity_str);

                    cards.set(position, card);
                    b.putSerializable("cards", cards);
                    intent.putExtras(b);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", position);
                    intent1.putExtra("Vibrate", vibrate);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), position, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

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

                    cards.set(position, card);
                    b.putSerializable("cards", cards);
                    int intentNumber = cards.size() - 1;
                    intent.putExtras(b);

                    alarmMgr = (AlarmManager) getBaseContext().getSystemService(Context.ALARM_SERVICE);
                    Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);
                    intent1.putExtra("RemindText", remindText);
                    intent1.putExtra("RemindPosition", position);
                    intent1.putExtra("Vibrate", vibrate);
                    alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

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
            int position = getIntent().getIntExtra("POSITION", -1);
            if (position == -1) {
                Log.e("ChangeReminder", "no reminder position");
                return;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "Reminder" + " " + position));
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


    private void addClickListener(final boolean is24Hour) {

        selectedTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(ChangeReminder.this, onTimeSetListener, hour, minute, is24Hour);
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(ChangeReminder.this, dateOnDateSetListener, year, month, day);
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
