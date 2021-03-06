package com.example.reveu.weloudgrouplist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import static android.R.attr.permission;
import static android.R.attr.value;
import static android.R.id.list;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.etGroupSearch;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupCloudList;
import static com.example.reveu.weloudgrouplist.R.id.lvGroupSearch;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupUserInvite extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private int groupID;
    private UserListAdapter userAdapter = new UserListAdapter(false);

    LinearLayout ctMain;
    ListView lvUser;
    EditText etSearch;
    ImageView ivSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_userinvite);

        ctMain = (LinearLayout) findViewById(R.id.ctMain);
        lvUser = (ListView) findViewById(R.id.lvUser);
        etSearch = (EditText) findViewById(R.id.etSearch);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);

        lvUser.setAdapter(userAdapter);

        Intent intent = getIntent();
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);

        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                UserItem item = userAdapter.getItem(position);

                GroupUserTask task = new GroupUserTask(position);
                task.execute(String.valueOf(groupID), String.valueOf(item.getUserNum()));
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String searchID = etSearch.getText().toString();

                if(searchID.length() < 2)
                    Snackbar.make(ctMain, getString(R.string.text_morechar, 2), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                else
                {
                    userAdapter.clearList();

                    GroupUserTask task = new GroupUserTask();
                    task.execute(searchID, String.valueOf(groupID));
                }
            }
        });

        abLib.setDefaultActionBar(this, getText(R.string.text_userinvite).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    void inviteJsonEvent(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPUSERLIST).toString());

            int length = jsonArray.length();
            if(length > 0)
            {
                userAdapter.clearList();

                int index = 0;

                while (length > index)
                {
                    JSONObject item = jsonArray.getJSONObject(index);

                    String userID = item.getString(getText(R.string.TAG_ID).toString());
                    int userNum = item.getInt(getText(R.string.TAG_USERNUM).toString());
                    String nickName = item.getString(getText(R.string.TAG_NICKNAME).toString());
                    int isMember = item.getInt(getText(R.string.TAG_ISMEMBER).toString());
                    int isInvite = item.getInt(getText(R.string.TAG_ISINVITE).toString());
                    int status = 0;

                    if (isMember > 0)
                        status = 2;
                    else if (isInvite > 0)
                        status = 1;

                    userAdapter.addItem(userID, userNum, nickName, status);
                    index++;
                }
            }
            else
                Snackbar.make(ctMain, getText(R.string.text_noresults), Snackbar.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d(TAG, "SettGroupUserInvite : ", e);
        }
    }

    private class GroupUserTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        int position = -1;

        GroupUserTask()
        {

        }

        GroupUserTask(int position)
        {
            this.position = position;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SettGroupUserInvite.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(position < 0)
                inviteJsonEvent(result);
            else
            {
                UserItem item = userAdapter.getItem(position);
                int status;

                if(result.contains("deleted*"))
                {
                    Snackbar.make(ctMain, getText(R.string.text_invitationcancel), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    status = 0;
                }
                else if(result.contains("inserted*"))
                {
                    Snackbar.make(ctMain, getText(R.string.text_invitationsend), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    status = 1;
                }
                else
                {
                    Snackbar.make(ctMain, getString(R.string.text_useralreadyjoined, item.getUserID()), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    status = 2;
                }
                item.setStatus(status);
                userAdapter.notifyDataSetChanged();
            }
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(position >= 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_search_userinvite.php";
                postParameters = "groupid=" + params[0] + "&usernum=" + params[1];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_search_usersearch.php";
                postParameters = "ID=" + params[0] + "&groupID=" + params[1];
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
                Log.d(TAG, "SettGroupUserInvite: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
