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
                hideKeyboard(v);
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

                if(!(Pattern.matches("^[a-zA-Z0-9]{4,16}$", id)))
                    textinfo.setText(getText(R.string.text_id).toString() + getText(R.string.text_notformatted).toString());
                else if(!Pattern.matches("^[a-zA-Z0-9~!@#$%^&*()-_=+]{4,24}$", password))
                    textinfo.setText(getText(R.string.text_password).toString() + getText(R.string.text_notformatted).toString());
                else if(!(Pattern.matches("^[a-zA-Z0-9가-힣]{2,12}$", nickname)))
                    textinfo.setText(getText(R.string.text_nickname).toString() + getText(R.string.text_notformatted).toString());
                else if(!Pattern.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9.]+$", email))
                    textinfo.setText(getText(R.string.text_email).toString() + getText(R.string.text_notformatted).toString());
                else
                {
                    RegisterUser task = new RegisterUser();
                    task.execute(id, password, email, nickname);
                }

                hideKeyboard(v);
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

    // 나는 키보드가 싫다!!!!!!!!!!!!
    private void hideKeyboard(View v)
    {
        InputMethodManager imm= (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    class RegisterUser extends AsyncTask<String, Void, String>
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
                Toast.makeText(getContext(),"회원가입이 완료되었습니다.",Toast.LENGTH_SHORT).show();
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