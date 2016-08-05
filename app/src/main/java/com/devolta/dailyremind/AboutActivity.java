package com.devolta.dailyremind;


import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
    }


    public void showLicenses(View v) {
        final Notices notices = new Notices();
        notices.addNotice(new Notice("Material Preference", "https://github.com/consp1racy/android-support-preference", "Copyright 2015 consp1racy", new ApacheSoftwareLicense20()));
        notices.addNotice(new Notice("recyclerview-multiselect", "https://bignerdranch.github.io/recyclerview-multiselect/", "Copyright (c) 2014 Big Nerd Ranch", new MITLicense()));
        notices.addNotice(new Notice("TextDrawable", "https://github.com/amulyakhare/TextDrawable", "Copyright (c) 2014 Amulya Khare", new MITLicense()));
        notices.addNotice(new Notice("AutoFitTextView", "https://github.com/grantland/android-autofittextview", "Copyright 2014 Grantland Chew", new ApacheSoftwareLicense20()));


        new LicensesDialog.Builder(this)
                .setNotices(notices)
                .setIncludeOwnLicense(true)
                .build()
                .showAppCompat();
    }


}
