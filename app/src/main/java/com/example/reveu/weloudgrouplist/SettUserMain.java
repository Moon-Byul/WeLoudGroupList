package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettUserMain extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private LinearLayout btnAccount;

    private String ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_usermain);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        btnAccount = (LinearLayout) findViewById(R.id.userMain_btnAccount);

        abLib.setDefaultActionBar(this, getText(R.string.text_settings).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        btnAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettUserMain.this, SettUserAccount.class);
                intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
                startActivity(intent);
            }
        });
    }
}
