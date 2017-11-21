package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

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
        UITask task = new UITask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void UIAction()
    {
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

    private class UITask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {
            UIAction();
        }
    }
}
