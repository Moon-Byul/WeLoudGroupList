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
import java.util.ArrayList;
import java.util.Iterator;

import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.lvAdmission;

/**
 * Created by reveu on 2017-08-23.
 */

public class GroupListUserInvitation extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private String userNum;
    private GroupListAdapter glaAdapter = new GroupListAdapter();

    ProgressDialog progressDialogAdmission;;
    LinearLayout ctMain;
    ListView lvInvitation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_useradmission);

        ctMain = (LinearLayout) findViewById(R.id.ctMain);
        lvInvitation = (ListView) findViewById(lvAdmission);

        lvInvitation.setAdapter(glaAdapter);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());

        lvInvitation.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupListItem item = glaAdapter.getItem(position);

                GroupInvitationTask task = new GroupInvitationTask(position);
                task.execute(String.valueOf(item.getGroupID()), userNum);
            }
        });

        abLib.setDefaultActionBar(this, getText(R.string.text_invitation).toString(), true, 0);

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
    protected void onStart()
    {
        super.onStart();

        GroupInvitationTask task = new GroupInvitationTask();
        task.execute(userNum);
    }

    void invitationSearchEvent(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_INVITATION).toString());

            int length = jsonArray.length();
            if(length > 0)
            {
                int index = 0;

                glaAdapter.clearList();
                while (length > index)
                {
                    JSONObject item = jsonArray.getJSONObject(index);

                    int groupID = Integer.parseInt(item.getString(getText(R.string.TAG_GROUPID).toString()));
                    String groupName = item.getString(getText(R.string.TAG_GROUPNAME).toString());
                    String nickname = item.getString(getText(R.string.TAG_NICKNAME).toString());

                    glaAdapter.addItem(groupID, false, groupName, nickname, 0);
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

    private class GroupInvitationTask extends AsyncTask<String, Void, String>
    {
        int position;
        boolean isGetter = true;

        GroupInvitationTask()
        {
            this.isGetter = true;
        }

        GroupInvitationTask(int position)
        {
            this.isGetter = false;
            this.position = position;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialogAdmission = ProgressDialog.show(GroupListUserInvitation.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(isGetter)
                invitationSearchEvent(result);
            else
            {
                Snackbar.make(ctMain, getText(R.string.text_signupgroup), Snackbar.LENGTH_SHORT).show();
                glaAdapter.getList().remove(position);
                glaAdapter.notifyDataSetChanged();
            }
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(isGetter)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_search_groupsearch_invitation.php";
                postParameters = "usernum=" + params[0];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_set_groupmember.php";
                postParameters = "groupid=" + params[0] + "&usernum=" + params[1];
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
