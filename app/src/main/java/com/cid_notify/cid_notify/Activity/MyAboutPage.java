package com.cid_notify.cid_notify.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cid_notify.cid_notify.R;

import java.util.Calendar;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class MyAboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        LinearLayout mActivityRoot = ((LinearLayout) findViewById(R.id.main_view));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setDescription("Hi, we are CID_Notify team.")
                .addItem(new Element().setTitle("Version 1.0-Release"))
                .addGroup("Connect with us")
                .addEmail("u0424035@nkfust.edu.tw")
                .addWebsite("https://github.com/KinJih/CID_Notify")
                .addGitHub("KinJih")
                .addItem(getCopyRightsElement())
                .create();
        //setContentView(aboutPage);
        mActivityRoot.addView(aboutPage);
    }

    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color);
        copyRightsElement.setIconNightTint(android.R.color.white);
        copyRightsElement.setGravity(Gravity.CENTER);
        return copyRightsElement;
    }
}

