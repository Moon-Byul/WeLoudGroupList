package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupUserAdmission extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private int groupID;
    private UserListAdapter userAdapter = new UserListAdapter(false);

    ProgressDialog progressDialogAdmission;;
    LinearLayout ctMain;
    ListView lvAdmission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_useradmission);

        ctMain = (LinearLayout) findViewById(R.id.ctMain);
        lvAdmission = (ListView) findViewById(R.id.lvAdmission);

        lvAdmission.setAdapter(userAdapter);

        Intent intent = getIntent();
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);

        lvAdmission.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                UserItem item = userAdapter.getItem(position);

                if(item.getChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);

                if(userAdapter.getCheckCount() > 0)
                    abLib.setEnableConfirmBtn(true);
                else
                    abLib.setEnableConfirmBtn(false);

                userAdapter.notifyDataSetChanged();
            }
        });

        abLib.setDefaultActionBar(this, getText(R.string.text_useradmission).toString(), true, 1);

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
                ArrayList<UserItem> userList = userAdapter.getList();
                Iterator<UserItem> it = userList.iterator();

                progressDialogAdmission = ProgressDialog.show(SettGroupUserAdmission.this, getText(R.string.text_loading).toString(), null, true, true);

                while(it.hasNext())
                {
                    UserItem user = it.next();

                    if(user.getChecked())
                    {
                        GroupUserTask task;

                        if(it.hasNext())
                            task = new GroupUserTask(true);
                        else
                            task = new GroupUserTask(true, true);

                        task.execute(String.valueOf(groupID), String.valueOf(user.getUserNum()));
                        it.remove();
                    }
                }
            }
        });

        abLib.setEnableConfirmBtn(false);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GroupUserTask task = new GroupUserTask(false);

        task.execute(String.valueOf(groupID));
    }

    void admissionSearchEvent(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_SETT_GROUP_ADMISSION).toString());

            int length = jsonArray.length();
            if(length > 0)
            {
                int index = 0;

                userAdapter.clearList();
                while (length > index)
                {
                    JSONObject item = jsonArray.getJSONObject(index);

                    String userID = item.getString(getText(R.string.TAG_ID).toString());
                    int userNum = item.getInt(getText(R.string.TAG_USERNUM).toString());
                    String nickName = item.getString(getText(R.string.TAG_NICKNAME).toString());
                    userAdapter.addItem(userID, userNum, nickName, 0);
                    index++;
                }
            }
            else
                Snackbar.make(ctMain, getText(R.string.text_noresults), Snackbar.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d(TAG, "SettGroupUserAdmission : ", e);
        }
    }

    void printSuccessMessage()
    {
        if(progressDialogAdmission != null)
        {
            if(progressDialogAdmission.isShowing())
                progressDialogAdmission.dismiss();
        }

        Snackbar.make(ctMain, getText(R.string.text_successadmission), Snackbar.LENGTH_SHORT).show();

        userAdapter.notifyDataSetChanged();
    }

    private class GroupUserTask extends AsyncTask<String, Void, String>
    {
        boolean AdmissionEvent;
        boolean isLast;

        GroupUserTask(boolean AdmissionEvent)
        {
            this.AdmissionEvent = AdmissionEvent;
        }

        GroupUserTask(boolean AdmissionEvent, boolean isLast)
        {
            this.AdmissionEvent = AdmissionEvent;
            this.isLast = isLast;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(!AdmissionEvent)
                progressDialogAdmission = ProgressDialog.show(SettGroupUserAdmission.this, getText(R.string.text_loading).toString(), null, true, true);
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(progressDialogAdmission != null)
            {
                if(progressDialogAdmission.isShowing())
                    progressDialogAdmission.dismiss();
            }

            if(!AdmissionEvent)
                admissionSearchEvent(result);
            if(isLast)
                printSuccessMessage();
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(AdmissionEvent)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_event_user_admission.php";
                postParameters = "groupid=" + params[0] + "&usernum=" + params[1];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_admission.php";
                postParameters = "groupid=" + params[0];
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
                Log.d(TAG, "SettGroupUserAdmission: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
