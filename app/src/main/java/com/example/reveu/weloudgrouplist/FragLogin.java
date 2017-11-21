package com.example.reveu.weloudgrouplist;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

/**
 * Created by 조용빈 on 2017-05-28.
 */

public class FragLogin extends Fragment
{
    EditText editId;
    EditText editPassword;
    TextView textinfo;

    String jsonString;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_login_main, container, false);

        LinearLayout ctMain = (LinearLayout)rootView.findViewById(R.id.main_container);
        Button btnLogin = (Button)rootView.findViewById(R.id.btnLogin);
        Button btnNewmember = (Button)rootView.findViewById(R.id.btnNewMember);

        editId = (EditText) rootView.findViewById(R.id.editId);
        editPassword = (EditText)rootView.findViewById(R.id.editPassword);
        textinfo = (TextView)rootView.findViewById(R.id.textinfo);

        ctMain.setSoundEffectsEnabled(false);
        ctMain.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new StockLib().hideKeyboard(v, getActivity());
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String id = editId.getText().toString();
                String password = editPassword.getText().toString();

                new StockLib().hideKeyboard(v, getActivity());

                LoginTask task = new LoginTask();
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id, password);
            }
        });
        btnNewmember.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Main activity = (Main)getActivity();
                activity.onFragmentChanged(1);
            }
        });
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        editId.setText("");
        editPassword.setText("");
        textinfo.setText("");
    }

    private void loginEvent()
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_LOGIN).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            String userNum = item.getString(getText(R.string.TAG_USERNUM).toString());
            String id = item.getString(getText(R.string.TAG_ID).toString());
            String nickname = item.getString(getText(R.string.TAG_NICKNAME).toString());

            HashMap<String,String> hashMap = new HashMap<>();

            hashMap.put(getText(R.string.TAG_USERNUM).toString(), userNum);
            hashMap.put(getText(R.string.TAG_ID).toString(), id);
            hashMap.put(getText(R.string.TAG_NICKNAME).toString(), nickname);

            textinfo.setText("");
            Main activity = (Main)getActivity();
            activity.login(hashMap);
        }
        catch (JSONException e)
        {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), getText(R.string.text_loading).toString(), null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result == null)
            {
                textinfo.setText(getText(R.string.error_temporary).toString());
            }
            else
            {
                if(result.contains("*password is error."))
                {
                    textinfo.setText(getText(R.string.text_idpassworderr).toString());
                }
                else
                    {
                    jsonString = result;
                    loginEvent();
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String id = params[0];
            String password = params[1];

            String serverURL = "http://weloud.duckdns.org/weloud/db_get_userinfo_login.php";
            String postParameters = "id=" + id + "&password=" + password;
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
                Log.d(TAG, "LoginData: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}

