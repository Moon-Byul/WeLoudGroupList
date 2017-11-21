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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-08-27.
 */

public class SettUserAccount extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();
    private StockLib stLib = new StockLib();

    private LinearLayout ctMain;
    private LinearLayout btnChangePw;
    private TextView tvID;
    private TextView tvErrorMsg;
    private EditText etNickname;
    private EditText etEmail;

    private String ID;
    private String Nickname;
    private String Email;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UITask task = new UITask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            Snackbar.make(ctMain, getText(R.string.text_pwischange), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }

    private void UIAction()
    {
        setContentView(R.layout.activity_settings_useraccount);
        abLib.setDefaultActionBar(this, getText(R.string.text_accountinfo).toString(), false, 1);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        tvID = (TextView) findViewById(R.id.userAccount_tvId);
        tvErrorMsg = (TextView) findViewById(R.id.userAccount_tvErrorMsg);
        etNickname = (EditText) findViewById(R.id.userAccount_etNickname);
        etEmail = (EditText) findViewById(R.id.userAccount_etEmail);
        btnChangePw = (LinearLayout) findViewById(R.id.userAccount_btnChangePw);
        ctMain = (LinearLayout) findViewById(R.id.userAccount_ctMain);

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

                boolean result = checkInfo();

                if(result)
                {
                    UserAccountTask task = new UserAccountTask();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "1", ID, etNickname.getText().toString(), etEmail.getText().toString());
                }
            }
        });

        btnChangePw.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettUserAccount.this, SettChangePw.class);
                intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
                startActivityForResult(intent, 0);
            }
        });

        etNickname.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isEqualPrevData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etEmail.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                isEqualPrevData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        UserAccountTask task = new UserAccountTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "0", ID);
    }

    private boolean checkInfo()
    {
        if(!stLib.isNickFormatted(etNickname.getText().toString()))
        {
            tvErrorMsg.setText(getText(R.string.text_nickname).toString() + getText(R.string.text_notformatted).toString());
            return false;
        }
        else if(!stLib.isEmailFormatted(etEmail.getText().toString()))
        {
            tvErrorMsg.setText(getText(R.string.text_email).toString() + getText(R.string.text_notformatted).toString());
            return false;
        }
        else
        {
            tvErrorMsg.setText("");
            return true;
        }
    }

    private void isEqualPrevData()
    {
        if(etNickname.getText().toString().equals(Nickname))
        {
            if(etEmail.getText().toString().equals(Email))
            {
                abLib.setEnableConfirmBtn(false);
            }
            else
                abLib.setEnableConfirmBtn(true);
        }
        else
            abLib.setEnableConfirmBtn(true);
    }

    private void getJsonData(String jsonString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray;
            jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_USERACCOUNT).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            Nickname = item.getString(getText(R.string.TAG_NICKNAME).toString());
            Email = item.getString(getText(R.string.TAG_EMAIL).toString());
        }
        catch (JSONException e)
        {
            Log.d(TAG, "UserAccount : ", e);
        }
    }

    private class UserAccountTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        int type;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SettUserAccount.this, getText(R.string.text_loading).toString(), null, true, true);
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(type == 0)
            {
                getJsonData(result);
                tvID.setText(ID);
                etEmail.setText(Email);
                etNickname.setText(Nickname);

                abLib.setEnableConfirmBtn(false);
            }
            else
            {
                if(result.contains("nickname is exists."))
                {
                    tvErrorMsg.setText(getText(R.string.text_nickname).toString() + " \"" + etNickname.getText().toString() + "\"" + getText(R.string.text_isexists).toString());
                }
                else
                {
                    Snackbar.make(ctMain, getText(R.string.text_accountchanged), Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    Nickname = etNickname.getText().toString();
                    Email = etEmail.getText().toString();

                    abLib.setEnableConfirmBtn(false);
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            type = Integer.parseInt(params[0]);

            String serverURL;
            String postParameters;
            if (type == 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_userinfo_settings.php";
                postParameters = "id=" + params[1];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_update_userinfo_settings.php";
                postParameters = "id=" + params[1] + "&nickName=" + params[2] + "&email=" + params[3];
            }
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
                Log.d(TAG, "UserAccount: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
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
