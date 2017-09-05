package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.regex.Pattern;

import static android.R.attr.enabled;
import static android.R.attr.id;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.editNickname;
import static com.example.reveu.weloudgrouplist.R.id.textinfo;

/**
 * Created by reveu on 2017-08-27.
 */

public class SettUserAccount extends AppCompatActivity
{
    private ImageView btnActionBarBack;
    private ImageView btnActionBarConfirm;
    private TextView tvActionBarTitle;

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
        task.execute();
    }

    private void UIAction()
    {
        setContentView(R.layout.activity_settings_useraccount);
        setDefaultActionBar(getText(R.string.text_accountinfo).toString(), false, 1);

        Intent intent = getIntent();
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());

        tvID = (TextView) findViewById(R.id.userAccount_tvId);
        tvErrorMsg = (TextView) findViewById(R.id.userAccount_tvErrorMsg);
        etNickname = (EditText) findViewById(R.id.userAccount_etNickname);
        etEmail = (EditText) findViewById(R.id.userAccount_etEmail);
        btnChangePw = (LinearLayout) findViewById(R.id.userAccount_btnChangePw);
        ctMain = (LinearLayout) findViewById(R.id.userAccount_ctMain);

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

                boolean result = checkInfo();

                if(result)
                {
                    UserAccount task = new UserAccount();
                    task.execute("1", ID, etNickname.getText().toString(), etEmail.getText().toString());
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
                startActivity(intent);
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

        UserAccount task = new UserAccount();
        task.execute("0", ID);
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

    private boolean checkInfo()
    {
        if(!Pattern.matches("^[a-zA-Z0-9가-힣]{2,12}$", etNickname.getText().toString()))
        {
            tvErrorMsg.setText(getText(R.string.text_nickname).toString() + getText(R.string.text_notformatted).toString());
            return false;
        }
        else if(!Pattern.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9.]+$", etEmail.getText().toString()))
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
                setEnableConfirmBtn(false);
            }
            else
                setEnableConfirmBtn(true);
        }
        else
            setEnableConfirmBtn(true);
    }

    private void setEnableConfirmBtn(boolean enabled)
    {
        btnActionBarConfirm.setEnabled(enabled);
        if(enabled)
            btnActionBarConfirm.setColorFilter(Color.argb(255, 255, 255, 255));
        else
            btnActionBarConfirm.setColorFilter(Color.argb(180, 255, 255, 255));
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

    private class UserAccount extends AsyncTask<String, Void, String>
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

                setEnableConfirmBtn(false);
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

                    setEnableConfirmBtn(false);
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
