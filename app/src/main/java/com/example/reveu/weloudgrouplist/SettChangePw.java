package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by reveu on 2017-08-31.
 */

public class SettChangePw extends AppCompatActivity
{
    private ImageView btnActionBarBack;
    private ImageView btnActionBarConfirm;
    private TextView tvActionBarTitle;

    private EditText etCurrentPw;
    private EditText etNewPw;
    private EditText etConfirmPw;

    private String ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UITask task = new UITask();
        task.execute();
    }

    private void UIAction()
    {
        setContentView(R.layout.activity_settings_changepassword);
        setDefaultActionBar(getText(R.string.text_passwordchange).toString(), false, 1);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        etCurrentPw = (EditText) findViewById(R.id.changePw_etCurrent);
        etNewPw = (EditText) findViewById(R.id.changePw_etNew);
        etConfirmPw = (EditText) findViewById(R.id.changePw_etConfirm);

        setEnableConfirmBtn(false);

        btnActionBarBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        btnActionBarConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new StockLib().hideKeyboard(v, getApplication());
            }
        });

        etCurrentPw.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isAllTypedPw();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etNewPw.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isAllTypedPw();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etConfirmPw.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isAllTypedPw();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setDefaultActionBar(String title, boolean isClose, int confirmType)
    {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        View customBar = LayoutInflater.from(this).inflate(R.layout.actionbar_default, null);
        actionBar.setCustomView(customBar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4472c4")));

        Toolbar parent = (Toolbar) customBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        btnActionBarBack = (ImageView) customBar.findViewById(R.id.actionbar_default_btnBack);
        btnActionBarConfirm = (ImageView) customBar.findViewById(R.id.actionbar_default_btnConfirm);
        tvActionBarTitle = (TextView) customBar.findViewById(R.id.actionbar_default_tvTitle);

        tvActionBarTitle.setText(title);

        if(isClose)
        {
            btnActionBarBack.setImageResource(R.drawable.cancle);
        }
        else
        {
            btnActionBarBack.setImageResource(R.drawable.backbtn);
        }

        if(confirmType > 0)
        {
            btnActionBarConfirm.setVisibility(View.VISIBLE);
            if(confirmType == 1)
            {
                btnActionBarConfirm.setImageResource(R.drawable.checkmark);
            }
            else
            {
                btnActionBarConfirm.setImageResource(R.drawable.addgroup_big);
            }
        }
        else
        {
            btnActionBarConfirm.setVisibility(View.INVISIBLE);
        }
    }

    private void isAllTypedPw()
    {
        if(etCurrentPw.getText().toString().length() > 0)
        {
            if(etNewPw.getText().toString().length() > 0)
            {
                if(etConfirmPw.getText().toString().length() > 0)
                    setEnableConfirmBtn(true);
                else
                    setEnableConfirmBtn(false);
            }
            else
                setEnableConfirmBtn(false);
        }
        else
            setEnableConfirmBtn(false);
    }

    private void setEnableConfirmBtn(boolean enabled)
    {
        btnActionBarConfirm.setEnabled(enabled);
        if(enabled)
            btnActionBarConfirm.setColorFilter(Color.argb(255, 255, 255, 255));
        else
            btnActionBarConfirm.setColorFilter(Color.argb(180, 255, 255, 255));
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
