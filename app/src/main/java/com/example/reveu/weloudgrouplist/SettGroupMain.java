package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

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
import static android.os.Build.ID;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupMain extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener
{
    private ActionbarLib abLib = new ActionbarLib();
    private PermissionLib pmLib = new PermissionLib(this);

    private LinearLayout btnKick;
    private LinearLayout btnInvite;
    private LinearLayout btnAdmission;
    private LinearLayout btnPerSett;
    private LinearLayout btnRankSett;
    private LinearLayout ctAutoJoin;
    private LinearLayout ctAllow;
    private Switch swAutoJoin;
    private Switch swAllow;

    private String userNum;
    private int groupID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_main);

        btnKick = (LinearLayout) findViewById(R.id.settings_groupmain_btnKick);
        btnInvite = (LinearLayout) findViewById(R.id.settings_groupmain_btnInvite);
        btnAdmission = (LinearLayout) findViewById(R.id.settings_groupmain_btnAdmission);
        btnPerSett = (LinearLayout) findViewById(R.id.settings_groupmain_btnPerSett);
        btnRankSett = (LinearLayout) findViewById(R.id.settings_groupmain_btnRankSett);
        ctAutoJoin = (LinearLayout) findViewById(R.id.settings_groupmain_ctAutoJoin);
        ctAllow = (LinearLayout) findViewById(R.id.settings_groupmain_ctAllow);
        swAutoJoin = (Switch) findViewById(R.id.settings_groupmain_swAutoJoin);
        swAllow = (Switch) findViewById(R.id.settings_groupmain_swAllow);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);

        SettGroupMainTask task = new SettGroupMainTask(true);
        task.execute(String.valueOf(groupID));

        btnKick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btnInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btnAdmission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        btnPerSett.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SettGroupMain.this, SettGroupPermission.class);
                intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
                startActivity(intent);
            }
        });

        btnRankSett.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        pmLib.execute(this, "permissionEvent", String.valueOf(groupID), String.valueOf(userNum));

        abLib.setDefaultActionBar(this, getText(R.string.text_groupsettings).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        SettGroupMainTask task = new SettGroupMainTask(false);

        String autoJoin = (swAutoJoin.isChecked() ? "1" : "0");
        String allow = (swAllow.isChecked() ? "1" : "0");

        task.execute(String.valueOf(groupID), autoJoin, allow);
    }

    private void switchCheckEvent(String jsonString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_SETT_GROUP_MAIN).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            boolean autoJoin = item.getString(getText(R.string.TAG_AUTOJOIN).toString()).equals("1");
            boolean allow = item.getString(getText(R.string.TAG_MAKEPUBLIC).toString()).equals("1");

            swAutoJoin.setChecked(autoJoin);
            swAllow.setChecked(allow);

            swAutoJoin.setOnCheckedChangeListener(this);
            swAllow.setOnCheckedChangeListener(this);
        }
        catch (JSONException e)
        {
            Log.d(TAG, "showResult : ", e);
        }
    }

    private class SettGroupMainTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        boolean isGetter;

        SettGroupMainTask(boolean isGetter)
        {
            this.isGetter = isGetter;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(isGetter)
                progressDialog = ProgressDialog.show(SettGroupMain.this, getText(R.string.text_loading).toString(), null, true, true);
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(progressDialog != null)
            {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            if(isGetter)
                switchCheckEvent(result);

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(isGetter)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_join_public.php";
                postParameters = "groupid=" + params[0];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_update_group_join_public.php";
                postParameters = "groupid=" + params[0] + "&autoJoin=" + params[1] + "&makePublic=" + params[2];
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
                Log.d(TAG, "SettGroupMain: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }

    /*
     *  아래의 부분은 Invoke (Reflect)를 통해서 사용하는 메소드들 입니다.
     *  never used가 뜬다고 해서 지우지 마시오.
     */

    public void permissionEvent()
    {
        if(pmLib.isUserCreator() || pmLib.isUserManageGroup())
            btnKick.setVisibility(View.VISIBLE);
        else
            btnKick.setVisibility(View.GONE);

        if(pmLib.isUserCreator() || pmLib.isUserInvite())
            btnInvite.setVisibility(View.VISIBLE);
        else
            btnInvite.setVisibility(View.GONE);

        if(pmLib.isUserCreator() || pmLib.isUserApproveJoin())
            btnAdmission.setVisibility(View.VISIBLE);
        else
            btnAdmission.setVisibility(View.GONE);

        if(pmLib.isUserCreator())
        {
            btnPerSett.setVisibility(View.VISIBLE);
            ctAutoJoin.setVisibility(View.VISIBLE);
            ctAllow.setVisibility(View.VISIBLE);
        }
        else
        {
            btnPerSett.setVisibility(View.GONE);
            ctAutoJoin.setVisibility(View.GONE);
            ctAllow.setVisibility(View.GONE);
        }

        if(pmLib.isUserCreator() || pmLib.isUserManageGroup())
            btnRankSett.setVisibility(View.VISIBLE);
        else
            btnRankSett.setVisibility(View.GONE);
    }
}
