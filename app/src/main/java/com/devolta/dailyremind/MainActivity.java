package com.devolta.dailyremind;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
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
import com.devolta.dailyremind.Interfaces.ItemTouchHelperAdapter;
import com.devolta.dailyremind.Interfaces.RemoveItem;
import com.devolta.dailyremind.RecyclerData.Card;
import com.devolta.devoltalibrary.Calculate;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int updateArraylist = 1;
    public static List<Integer> intentNumber2 = new ArrayList<>();
    private final MultiSelector multiSelector = new MultiSelector();
    private final Calculate calculate = new Calculate();
    private ArrayList<Card> cards = new ArrayList<>();
    private SimpleAdapter adapter;
    private RecyclerView recyclerView;
    private SimpleDateFormat sdtf;
    private ImageView card_check;
    private SwipeRefreshLayout refreshLayout;
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
                            if (!cards.isEmpty()) {
                                removeItem.remove(i);
                            }
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

    private void handleArrayList(Context context, int remove) {


        if (remove == 0) {
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
                            for (int i = 0; i <= size; i++) {
                                Card card = (Card) objectInputStream.readObject();
                                cards.add(card);
                            }
                            objectInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    String path = context.getFilesDir().getAbsolutePath() + "/" + "Arraylist 2";
                    File file = new File(path);
                    if (file.exists()) {
                        FileInputStream fileInputStream;
                        fileInputStream = context.openFileInput("Arraylist 2");
                        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                        try {
                            intentNumber2 = (List<Integer>) objectInputStream.readObject();
                            objectInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileOutputStream fileOutputStream;
                    fileOutputStream = context.openFileOutput("Arraylist 1", Context.MODE_PRIVATE);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeInt(cards.size());
                    for (Card card : cards) {
                        objectOutputStream.writeObject(card);
                    }
                    objectOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fileOutputStream;
                    fileOutputStream = context.openFileOutput("Arraylist 2", Context.MODE_PRIVATE);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(intentNumber2);
                    objectOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                FileOutputStream fileOutputStream;
                fileOutputStream = context.openFileOutput("Arraylist 1", Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeInt(cards.size());
                for (Card card : cards) {
                    objectOutputStream.writeObject(card);
                }
                objectOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void setAutoNightMode(Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String theme = prefs.getString("theme_pref", null);
            boolean autoNightMode = prefs.getBoolean("auto_night_mode", false);

            if (theme != null) {
                if (theme.contentEquals("day")) {
                    getDelegate().setLocalNightMode(
                            AppCompatDelegate.MODE_NIGHT_NO);
                    recreate();
                } else {
                    getDelegate().setLocalNightMode(
                            AppCompatDelegate.MODE_NIGHT_YES);
                    recreate();
                }
            } else if (autoNightMode) {
                getDelegate().setLocalNightMode(
                        AppCompatDelegate.MODE_NIGHT_AUTO);
                recreate();
            } else {
                getDelegate().setLocalNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                recreate();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAutoNightMode(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        handleArrayList(getApplicationContext(), 0);
        Log.d("Arraylist", "" + intentNumber2.size());

        recyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);

        adapter = new SimpleAdapter();

        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddReminder.class);
                Bundle b = new Bundle();
                b.putSerializable("cards", cards);
                intent.putExtras(b);
                startActivityForResult(intent, updateArraylist);
            }
        });

        if (!DateFormat.is24HourFormat(this)) {
            sdtf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        } else {
            sdtf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        }

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        new updateRemainingTime().execute();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new updateRemainingTime().execute();
            }
        });
        refreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.purple);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {

            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                cards = (ArrayList<Card>) b.getSerializable("cards");
                adapter.notifyItemInserted(cards.size() - 1);
                handleArrayList(getApplicationContext(), 0);
                DailyRemindWidgetProvider.setCards(cards);
            } else {
                Log.e("Error", "updating Arraylist failed");
            }
        }
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private class updateRemainingTime extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = cards.size() - 1; i >= 0; i--) {
                    Card card = cards.get(i);

                    String time = card.getCardTime();
                    String date = card.getCardDate();

                    Date now = new Date();
                    Date then;
                    try {
                        then = sdtf.parse(date + " " + time);
                    } catch (ParseException e) {
                        Log.d("PARSEEXCEPTION:", " " + e);
                        return null;
                    }

                    String remainingTime = calculate.calcTimeDiff(then.getTime(), now.getTime());
                    Log.d("Card", "Card updated " + remainingTime);
                    cards.get(i).cardRemainingTime(remainingTime);
                    DailyRemindWidgetProvider.setCards(cards);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter = new SimpleAdapter();
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            refreshLayout.setRefreshing(false);
        }
    }

    public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> implements RemoveItem, ItemTouchHelperAdapter {

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
        }

        @Override
        public void onItemDismiss(int position) {

        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(cards, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(cards, i, i - 1);
                }
            }
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        @Override
        public void remove(int position) {
            cards.remove(position);
            Log.d("Main", "removed " + position);
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

            handleArrayList(getApplicationContext(), 1);
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
                    startActivityForResult(intent2, updateArraylist);

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


