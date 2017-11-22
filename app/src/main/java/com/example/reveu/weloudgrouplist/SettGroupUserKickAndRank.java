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

import static android.R.attr.value;
import static android.R.id.list;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.etGroupSearch;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupCloudList;
import static com.example.reveu.weloudgrouplist.R.id.lvGroupSearch;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupUserKickAndRank extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private int groupID;
    private int permission;
    private String userNum;
    private boolean isKick;
    private UserListAdapter userAdapter = new UserListAdapter(true);

    ProgressDialog progressDialogKick;
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
        permission = intent.getIntExtra(getText(R.string.TAG_PERMISSION).toString(), 0);
        isKick = intent.getBooleanExtra(getText(R.string.TAG_ISKICK).toString(), false);

        lvUser.setOnItemClickListener(new AdapterView.OnItemClickListener()
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

        if(isKick)
        {
            abLib.setDefaultActionBar(this, getText(R.string.text_kickuser).toString(), true, 1);
        }
        else
        {
            abLib.setDefaultActionBar(this, getText(R.string.text_userranksett).toString(), true, 1);
        }

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
                if(isKick)
                {
                    AlertDialog.Builder alertDelete = new AlertDialog.Builder(SettGroupUserKickAndRank.this);

                    alertDelete.setTitle(getText(R.string.text_delete).toString())
                            .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    ArrayList<UserItem> userList = userAdapter.getList();
                                    Iterator<UserItem> it = userList.iterator();

                                    progressDialogKick = ProgressDialog.show(SettGroupUserKickAndRank.this, getText(R.string.text_loading).toString(), null, true, true);

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
                            })

                            .setNegativeButton(getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {

                                }
                            });

                    if (userAdapter.getCheckCount() > 1)
                        alertDelete.setMessage(getString(R.string.text_areyousurekick_multi, userAdapter.getCheckCount()));
                    else
                    {
                        for(UserItem user : userAdapter.getList())
                        {
                            if(user.getChecked())
                            {
                                alertDelete.setMessage(getString(R.string.text_areyousurekick_one, user.getUserID()));
                            }
                        }
                    }

                    alertDelete.show();
                }
                else
                {
                    ArrayList<String> arrTemp = new ArrayList<String>();
                    for(UserItem user : userAdapter.getList())
                    {
                        if(user.getChecked())
                            arrTemp.add(String.valueOf(user.getUserNum()));
                    }

                    Intent intent = new Intent(SettGroupUserKickAndRank.this, SettGroupUserRankDetail.class);
                    intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
                    intent.putExtra(getText(R.string.TAG_USERNUM).toString(), arrTemp.toArray(new String[arrTemp.size()]));
                    startActivity(intent);
                }
            }
        });

        abLib.setEnableConfirmBtn(false);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        userAdapter.clearList();
        GroupUserTask task = new GroupUserTask();
        if(isKick)
            task.execute(String.valueOf(groupID), String.valueOf(permission));
        else
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
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                while (length > index)
                {
                    JSONObject item = jsonArray.getJSONObject(index);

                    String userID = item.getString(getText(R.string.TAG_ID).toString());
                    int userNum = item.getInt(getText(R.string.TAG_USERNUM).toString());

                    if(isKick)
                    {
                        String joinDate = item.getString(getText(R.string.TAG_JOINDATE).toString());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(joinDate));

                        userAdapter.addItem(userID, userNum, cal);
                    }
                    else
                    {
                        String rankName = item.getString(getText(R.string.TAG_RANKNAME).toString());
                        userAdapter.addItem(userID, userNum, rankName);
                    }
                    index++;
                }
            }
            else
                Snackbar.make(ctMain, getText(R.string.text_noresults), Snackbar.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d(TAG, "SettGroupUserKickAndRank : ", e);
        }

        userAdapter.filter("");
    }

    void printSuccessMessage()
    {
        if(progressDialogKick != null)
        {
            if(progressDialogKick.isShowing())
                progressDialogKick.dismiss();
        }

        Snackbar.make(ctMain, getText(R.string.text_successkickuser), Snackbar.LENGTH_SHORT).show();

        userAdapter.filter("");
    }

    private class GroupUserTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        boolean kickEvent = false;
        boolean isLast = false;

        GroupUserTask()
        {

        }

        GroupUserTask(boolean kickEvent)
        {
            this.kickEvent = kickEvent;
        }

        GroupUserTask(boolean kickEvent, boolean isLast)
        {
            this.kickEvent = kickEvent;
            this.isLast = isLast;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(!kickEvent)
                progressDialog = ProgressDialog.show(SettGroupUserKickAndRank.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(!kickEvent)
                userSearchEvent(result);

            if(isLast)
                printSuccessMessage();
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(isKick)
            {
                if(!kickEvent)
                {
                    serverURL = "http://weloud.duckdns.org/weloud/db_get_group_userlist_date.php";
                    postParameters = "groupid=" + params[0] + "&permission=" + params[1];
                }
                else
                {
                    serverURL = "http://weloud.duckdns.org/weloud/db_delete_group_user.php";
                    postParameters = "groupid=" + params[0] + "&userNum=" + params[1];
                }
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
                Log.d(TAG, "SettGroupUserKickAndRank: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
