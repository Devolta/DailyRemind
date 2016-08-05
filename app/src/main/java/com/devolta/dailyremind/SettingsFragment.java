package com.devolta.dailyremind;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.SwitchPreference;

public class SettingsFragment extends XpPreferenceFragment {

    private ListPreference listPreference;

    private final Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof SwitchPreference) {

                //Auto Night Mode Setting
                if (preference.getKey().contentEquals("auto_night_switch")) {
                    Boolean boolVal = (Boolean) value;
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("auto_night_mode", boolVal);
                    editor.apply();
                    listPreference.setEnabled(!boolVal);
                    return true;
                }
            } else if (preference instanceof ListPreference) {

                //Theme Setting
                if (preference.getKey().contentEquals("theme_pref")) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    String entry = (String) ((ListPreference) preference).getEntry();
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("theme_pref", entry);
                    editor.apply();

                    if (index == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (index == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    getActivity().recreate();

                    return true;
                }
            }


            return true;
        }
    };

    static SettingsFragment newInstance(String rootKey) {
        Bundle args = new Bundle();
        args.putString(SettingsFragment.ARG_PREFERENCE_ROOT, rootKey);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView listView = getListView();

        listView.setFocusable(false);
    }

    public void onCreatePreferences2(Bundle savedInstanceState, final String rootKey) {

        addPreferencesFromResource(R.xml.preferences);

        SwitchPreference switchPreference = (SwitchPreference) getPreferenceManager().findPreference("auto_night_switch");
        switchPreference.setOnPreferenceChangeListener(preferenceChangeListener);

        listPreference = (ListPreference) getPreferenceManager().findPreference("theme_pref");
        listPreference.setOnPreferenceChangeListener(preferenceChangeListener);

    }

}
