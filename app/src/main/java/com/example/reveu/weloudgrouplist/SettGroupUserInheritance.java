package com.example.reveu.weloudgrouplist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.EditText;
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

import static android.R.attr.permission;
import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupUserInheritance extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private int groupID;
    private String userNum;
    private UserListAdapter userAdapter = new UserListAdapter(true);

    LinearLayout ctMain;
    ListView lvUser;
    EditText etSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_userkickandrank);

        ctMain = (LinearLayout) findViewById(R.id.ctMain);
        lvUser = (ListView) findViewById(R.id.lvUser);
        etSearch = (EditText) findViewById(R.id.etSearch);

        lvUser.setAdapter(userAdapter);

        Intent intent = getIntent();
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());

        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                final UserItem item = userAdapter.getItem(position);

                AlertDialog.Builder alertInheritance = new AlertDialog.Builder(SettGroupUserInheritance.this);

                alertInheritance.setTitle(getText(R.string.text_creatorinheritance).toString())
                .setMessage(getString(R.string.text_areyousureinheritance, item.getUserID()))

                .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String targetNum = String.valueOf(item.getUserNum());

                        GroupUserTask task = new GroupUserTask(true, targetNum);
                        task.execute(String.valueOf(groupID), targetNum, "0");
                    }
                })

                .setNegativeButton(getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                alertInheritance.show();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                String filterText = s.toString();
                userAdapter.filter(filterText);
            }
        });

        abLib.setDefaultActionBar(this, getText(R.string.text_creatorinheritance).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        GroupUserTask task = new GroupUserTask();
        task.execute(String.valueOf(groupID), userNum);
    }

    void userSearchEvent(String result)
    {
        userAdapter.clearList();

        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPUSERLIST).toString());

            int length = jsonArray.length();
            if(length > 0)
            {
                int index = 0;

                while (length > index)
                {
                    JSONObject item = jsonArray.getJSONObject(index);

                    String userID = item.getString(getText(R.string.TAG_ID).toString());
                    int userNum = item.getInt(getText(R.string.TAG_USERNUM).toString());
                    String rankName = item.getString(getText(R.string.TAG_RANKNAME).toString());
                    userAdapter.addItem(userID, userNum, rankName);

                    index++;
                }
            }
            else
                Snackbar.make(ctMain, getText(R.string.text_noresults), Snackbar.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d(TAG, "SettGroupUserInheritance : ", e);
        }

        userAdapter.filter("");
    }

    private class GroupUserTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        boolean inheritanceEvent = false;
        String targetUser = "";

        GroupUserTask()
        {

        }

        GroupUserTask(boolean inheritanceEvent, String targetUser)
        {
            this.inheritanceEvent = inheritanceEvent;
            this.targetUser = targetUser;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(!inheritanceEvent)
                progressDialog = ProgressDialog.show(SettGroupUserInheritance.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(!inheritanceEvent)
                userSearchEvent(result);
            else
            {
                if(targetUser.equals(userNum))
                {
                    Intent intent = getIntent();
                    setResult(RESULT_OK,intent);
                    finish();
                }
                else
                {
                    GroupUserTask task = new GroupUserTask(true, userNum);
                    task.execute(String.valueOf(groupID), userNum, "1");
                }
            }
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(inheritanceEvent)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_update_userrank.php";
                postParameters = "groupid=" + params[0] + "&usernum=" + params[1] + "&rankid=" + params[2];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_userlist_rank.php";
                postParameters = "groupid=" + params[0] + "&userNum=" + params[1];
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
                Log.d(TAG, "SettGroupUserInheritance: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
