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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;

/**
 * Created by 조용빈 on 2017-05-28.
 */

public class FragRegister extends Fragment
{
    private StockLib stLib = new StockLib();

    TextView textinfo;
    EditText editId;
    EditText editPassword;
    EditText editNickname;
    EditText editEmail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_login_register, container, false);

        textinfo = (TextView)rootView.findViewById(R.id.textinfo);
        editId = (EditText)rootView.findViewById(R.id.editId);
        editPassword = (EditText)rootView.findViewById(R.id.editPassword);
        editNickname = (EditText)rootView.findViewById(R.id.editNickname);
        editEmail = (EditText)rootView.findViewById(R.id.editEmail);

        LinearLayout ctRegister = (LinearLayout)rootView.findViewById(R.id.register_container);
        Button btncreate = (Button)rootView.findViewById(R.id.btnCreate);
        Button btncancel = (Button)rootView.findViewById(R.id.btnCancel);

        ctRegister.setSoundEffectsEnabled(false);
        ctRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new StockLib().hideKeyboard(v, getActivity());
            }
        });
        btncreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String id = editId.getText().toString();
                String password = editPassword.getText().toString();
                String nickname = editNickname.getText().toString();
                String email = editEmail.getText().toString();

                if(!stLib.isIDFormatted(id))
                    textinfo.setText(getString(R.string.text_notformatted, getText(R.string.text_id)));
                else if(!stLib.isPwFormatted(password))
                    textinfo.setText(getString(R.string.text_notformatted, getText(R.string.text_password)));
                else if(!stLib.isNickFormatted(nickname))
                    textinfo.setText(getString(R.string.text_notformatted, getText(R.string.text_nickname)));
                else if(!stLib.isEmailFormatted(email))
                    textinfo.setText(getString(R.string.text_notformatted, getText(R.string.text_email)));
                else
                {
                    RegisterUserTask task = new RegisterUserTask();
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id, password, email, nickname);
                }

                new StockLib().hideKeyboard(v, getActivity());
            }
        });
        btncancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Main activity = (Main)getActivity();
                activity.onFragmentChanged(2);
            }
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        resetEditText();
    }

    public void resetEditText()
    {
        textinfo.setText("");
        editId.setText("");
        editPassword.setText("");
        editNickname.setText("");
        editEmail.setText("");
    }

    private class RegisterUserTask extends AsyncTask<String, Void, String>
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

            //혹시 모르니 한번 더 확인
            if(result.contains("for key 'ID'") || result.contains("id is exists."))
            {
                textinfo.setText(getText(R.string.text_id).toString() + " \"" + editId.getText().toString() + "\"" + getText(R.string.text_isexists).toString());
            }
            else if(result.contains("for key 'Nickname'") || result.contains("nickname is exists."))
            {
                textinfo.setText(getText(R.string.text_nickname).toString() + " \"" + editNickname.getText().toString() + "\"" + getText(R.string.text_isexists).toString());
            }
            else
            {
                Toast.makeText(getContext(), getText(R.string.text_successregister), Toast.LENGTH_SHORT).show();
                Main activity = (Main)getActivity();
                activity.onFragmentChanged(2);
            }

            Log.d(TAG, "POST response  - " + result);
        }

        @Override
        protected String doInBackground(String... params)
        {
            String id = params[0];
            String password = params[1];
            String email = params[2];
            String nickname = params[3];

            String serverURL = "http://weloud.duckdns.org/weloud/db_set_userinfo.php";
            String postParameters = "id=" + id + "&password=" + password + "&email=" + email + "&nickname=" + nickname;

            try
            {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

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
                String line = null;

                while((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString();
            }
            catch (Exception e)
            {
                Log.d(TAG, "RegisterUser: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
