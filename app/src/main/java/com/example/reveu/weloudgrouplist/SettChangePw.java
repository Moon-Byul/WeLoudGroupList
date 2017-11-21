package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import static android.R.attr.password;
import static android.content.ContentValues.TAG;

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
                    ChangePwTask task = new ChangePwTask();
                    task.execute(ID, etCurrentPw.getText().toString(), etNewPw.getText().toString());
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

    /*
     * 해당되는 모든 EditText에 이벤트를 걸어놓고, EditText가 모두 빈칸이 아닌지 체크 한 다음
     * 빈칸이 아니면 Confirm Button을 Enable하고, 하나라도 빈칸이 있으면 Disable한다.
     */
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

    private class ChangePwTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SettChangePw.this, getText(R.string.text_loading).toString(), null, true, true);
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result.contains("*password is error."))
            {
                tvErrorMsg.setText(getText(R.string.text_passworderr).toString());
            }
            else if(result.contains("*SQL Success"))
            {
                Intent intent = new Intent(SettChangePw.this, SettUserAccount.class);
                setResult(RESULT_OK, intent);
                finish();
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            serverURL = "http://weloud.duckdns.org/weloud/db_update_changepw.php";
            postParameters = "id=" + params[0] + "&currentPw=" + params[1] + "&newPw=" + params[2];
            try
            {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK)
                {
                    inputStream = httpURLConnection.getInputStream();
                }
                else
                {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            }
            catch (Exception e)
            {
                Log.d(TAG, "ChangePw: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
