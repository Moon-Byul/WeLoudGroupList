package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

import static com.example.reveu.weloudgrouplist.R.id.textinfo;

/**
 * Created by reveu on 2017-08-31.
 */

public class SettChangePw extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();
    private StockLib stLib = new StockLib();

    private EditText etCurrentPw;
    private EditText etNewPw;
    private EditText etConfirmPw;
    private TextView tvErrorMsg;

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
        abLib.setDefaultActionBar(this, getText(R.string.text_passwordchange).toString(), false, 1);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        etCurrentPw = (EditText) findViewById(R.id.changePw_etCurrent);
        etNewPw = (EditText) findViewById(R.id.changePw_etNew);
        etConfirmPw = (EditText) findViewById(R.id.changePw_etConfirm);
        tvErrorMsg = (TextView) findViewById(R.id.changePw_tvErrorMsg);

        abLib.setEnableConfirmBtn(false);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        abLib.getBtnActionBarConfirm().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new StockLib().hideKeyboard(v, getApplication());

                tvErrorMsg.setText("");
                tvErrorMsg.setText(passwordCheck());

                if(tvErrorMsg.getText().toString().equals(""))
                {
                    // 비밀번호 변경
                    Log.d("Twily", "Password Change");
                }
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

    private String passwordCheck()
    {
        String currentPw = etCurrentPw.getText().toString();
        String newPw = etNewPw.getText().toString();
        String confirmPw = etConfirmPw.getText().toString();

        if(!stLib.isPwFormatted(currentPw))
            return getText(R.string.text_currentPw).toString() + getText(R.string.text_notformatted).toString();

        if(!stLib.isPwFormatted(newPw))
            return getText(R.string.text_newPw).toString() + getText(R.string.text_notformatted).toString();

        if(!stLib.isPwFormatted(confirmPw))
            return getText(R.string.text_confirmPw).toString() + getText(R.string.text_notformatted).toString();

        if(currentPw.equals(newPw))
        {
            return getText(R.string.text_newpwsamecurrent).toString();
        }
        else
        {
            if(!newPw.equals(confirmPw))
                return getText(R.string.text_newpwnotmatch).toString();
            else
                return "";
        }
    }

    private void isAllTypedPw()
    {
        if(etCurrentPw.getText().toString().length() > 0)
        {
            if(etNewPw.getText().toString().length() > 0)
            {
                if(etConfirmPw.getText().toString().length() > 0)
                    abLib.setEnableConfirmBtn(true);
                else
                    abLib.setEnableConfirmBtn(false);
            }
            else
                abLib.setEnableConfirmBtn(false);
        }
        else
            abLib.setEnableConfirmBtn(false);
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
