package com.devolta.dailyremind;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;

public class SettingsActivity extends AppCompatActivity implements
        PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        FragmentManager.OnBackStackChangedListener,
        PreferenceScreenNavigationStrategy.ReplaceFragment.Callbacks {

    private Toolbar toolbar;
    private SettingsFragment settingsFragment;
    private PreferenceScreenNavigationStrategy.ReplaceFragment replaceFragmentStrategy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        replaceFragmentStrategy = new PreferenceScreenNavigationStrategy.ReplaceFragment(this, R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_fade_in, R.anim.abc_fade_out);

        if (savedInstanceState == null) {
            settingsFragment = SettingsFragment.newInstance(null);
            getSupportFragmentManager().beginTransaction().add(R.id.content, settingsFragment, "Settings").commit();
        } else {
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("Settings");
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onPreferenceStartScreen(final PreferenceFragmentCompat preferenceFragmentCompat, final PreferenceScreen preferenceScreen) {
        replaceFragmentStrategy.onPreferenceStartScreen(getSupportFragmentManager(), preferenceFragmentCompat, preferenceScreen);
        return true;
//        return false; // Turn off to try ReplaceRoot strategy.
    }

    @Override
    public PreferenceFragmentCompat onBuildPreferenceFragment(final String rootKey) {
        return settingsFragment.newInstance(rootKey);
    }

    @Override
    public void onBackStackChanged() {
        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("Settings");
    }

}
