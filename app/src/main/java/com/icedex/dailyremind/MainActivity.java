package com.icedex.dailyremind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.icedex.dailyremind.Interfaces.RemoveItem;
import com.icedex.dailyremind.RecyclerData.Card;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//import com.squareup.leakcanary.LeakCanary;

public class MainActivity extends AppCompatActivity {

    public static final ArrayList<Integer> intentNumber2 = new ArrayList<>();
    public static ArrayList<Card> cards = new ArrayList<>();
    private final SimpleDateFormat sdtf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
    private final MultiSelector multiSelector = new MultiSelector();
    public ImageView card_check;
    private RemoveItem removeItem;
    // Multi select items in recycler view
    private final android.support.v7.view.ActionMode.Callback mDeleteMode = new ModalMultiSelectorCallback(multiSelector) {

        @Override
        public boolean onCreateActionMode(android.support.v7.view.ActionMode actionMode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_add_reminder, menu);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(android.support.v7.view.ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                // On clicking discard reminders
                case R.id.discard_reminder:

                    // Get the reminder id associated with the recycler view item
                    for (int i = cards.size(); i >= 0; i--) {
                        if (multiSelector.isSelected(i, 0)) {

                            Log.d("MAIN", "removing items");
                            removeItem.remove(i);
                        }
                    }
                    // Clear selected items in recycler view
                    multiSelector.clearSelections();

                    // Display toast to confirm delete
                    Toast.makeText(getApplicationContext(),
                            "Deleted",
                            Toast.LENGTH_SHORT).show();

                    // Close the context menu
                    actionMode.finish();

                default:
                    break;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            multiSelector.clearSelections();

            for (int i = cards.size(); i >= 0; i--) {
                if (multiSelector.isSelected(i, 0)) {

                    multiSelector.setSelected(i, i, false);
                }
            }

        }
    };
    private TextView card_tv;
    private TextView card_tv2;
    private ImageView card_th;

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

    private static void handleArrayList(Context context, int remove) {

        if (remove == 0) {
            if (cards.isEmpty()) {
                try {
                    String path = context.getFilesDir().getAbsolutePath() + "/" + "Reminder";
                    File file = new File(path);
                    if (file.exists()) {
                        FileInputStream fileInputStream;
                        fileInputStream = context.openFileInput("Reminder");
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        try {
                            @SuppressWarnings("unchecked")
                            ArrayList<Card> returnList = (ArrayList<Card>) objectInputStream.readObject();
                            objectInputStream.close();
                            cards = returnList;
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileOutputStream fileOutputStream;
                    fileOutputStream = context.openFileOutput("Reminder", Context.MODE_PRIVATE);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(cards);
                    objectOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                FileOutputStream fileOutputStream;
                fileOutputStream = context.openFileOutput("Reminder", Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(cards);
                objectOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LeakCanary.install(this.getApplication());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        handleArrayList(getApplicationContext(), 0);

        SimpleAdapter adapter = new SimpleAdapter();

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddReminder.class);
                startActivity(intent);
            }
        });

        adapter.notifyItemInserted(cards.size() - 1);

        if (cards != null && cards.size() != 0) {
            for (int i = cards.size() - 1; i >= 0; i--) {
                updateRemainingTime(i);

            }
        }
    }

    //refresh the time remaining on the cards
    private void updateRemainingTime(int position) {
        Card card = cards.get(position);
        String time = card.getCardTime();
        String date = card.getCardDate();

        Date now = new Date();
        Date then = null;
        try {
            then = sdtf.parse(date + " " + time);
        } catch (ParseException e) {
            Log.d("PARSEEXCEPTION:", " " + e);
        }
        String remainingTime = calculateTimeDifference(then.getTime(), now.getTime());
        card.cardRemainingTime(remainingTime);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item_main clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> implements RemoveItem {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_main, parent, false);

            return new ViewHolder(itemView, this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Card card = cards.get(position);

            holder.setReminderText(card.getCardText());
            card_tv2.setText(card.getCardRemainingTime());
            holder.setSelectable(true);

            card.cardId(position);
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        @Override
        public void remove(int position) {
            cards.remove(position);
            Log.d("Main", "" + position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cards.size());

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent intent1 = new Intent(getBaseContext(), AlarmReceiver.class);

            int intentNumber = intentNumber2.get(position);
            intentNumber2.remove(position);
            Log.d("IntentNumber", "" + intentNumber);

            PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), intentNumber, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmIntent.cancel();

            alarmManager.cancel(alarmIntent);

            File dir = getFilesDir();
            File file = new File(dir, "Reminder" + " " + position);
            boolean deleted = file.delete();

            MainActivity.handleArrayList(getApplicationContext(), 1);
        }

        public class ViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {

            public final LinearLayout background;

            public ViewHolder(View itemView, RemoveItem removeItem1) {
                super(itemView, multiSelector);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                itemView.setLongClickable(true);
                removeItem = removeItem1;

                card_tv = (TextView) itemView.findViewById(R.id.card_tv);
                card_tv2 = (TextView) itemView.findViewById(R.id.card_tv2);
                card_th = (ImageView) itemView.findViewById(R.id.thumbnail);
                card_check = (ImageView) itemView.findViewById(R.id.card_check);
                background = (LinearLayout) itemView.findViewById(R.id.background);

            }


            @Override
            public void onClick(View view) {
                if (multiSelector.tapSelection(this)) {

                    background.setSelected(true);

                } else {

                    int position = getAdapterPosition();
                    String text = "error";
                    String time = "error";
                    String date = "error";
                    String repeat;
                    boolean repeat2 = false;
                    String quantity = "error";
                    String mode = "error";

                    try {
                        InputStream inputStream = openFileInput("Reminder" + " " + position);
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        text = bufferedReader.readLine();
                        time = bufferedReader.readLine();
                        date = bufferedReader.readLine();
                        repeat = bufferedReader.readLine();
                        repeat2 = repeat.equals("true");
                        quantity = bufferedReader.readLine();
                        mode = bufferedReader.readLine();
                        inputStreamReader.close();
                        bufferedReader.close();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent2 = new Intent(getBaseContext(), ChangeReminder.class);
                    intent2.putExtra("POSITION", position);
                    intent2.putExtra("TIME", time);
                    intent2.putExtra("DATE", date);
                    intent2.putExtra("TEXT", text);
                    intent2.putExtra("REPEAT", repeat2);
                    intent2.putExtra("QUANTITY", quantity);
                    intent2.putExtra("MODE", mode);
                    startActivity(intent2);

                }
            }

            @Override
            public boolean onLongClick(View view) {
                AppCompatActivity activity = MainActivity.this;
                activity.startSupportActionMode(mDeleteMode);
                multiSelector.setSelectable(true);
                multiSelector.setSelected(this, true);
                background.setSelected(true);
                return true;
            }

            public void setReminderText(String cardText) {
                card_tv.setText(cardText);

                ColorGenerator colorGenerator = ColorGenerator.MATERIAL;
                TextDrawable drawableBuilder;
                String letter = "A";

                if (cardText != null && !cardText.isEmpty()) {
                    letter = cardText.substring(0, 1);
                }

                int color = colorGenerator.getRandomColor();

                drawableBuilder = TextDrawable.builder()
                        .buildRound(letter, color);
                card_th.setImageDrawable(drawableBuilder);
            }

        }

    }
}


