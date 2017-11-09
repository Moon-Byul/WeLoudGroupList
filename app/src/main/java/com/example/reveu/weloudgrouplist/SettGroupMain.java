package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupMain extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private String ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_main);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        abLib.setDefaultActionBar(this, getText(R.string.text_groupsettings).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}
