package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.R.attr.id;
import static android.R.attr.permission;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.lvGroup;
import static com.example.reveu.weloudgrouplist.R.id.srlGroupMain;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupPermission extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private int groupID;

    LinearLayout ctList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_permission);

        ctList = (LinearLayout) findViewById(R.id.settings_grouppermission_ctList);

        Intent intent = getIntent();
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);

        abLib.setDefaultActionBar(this, getText(R.string.text_permissionsett).toString(), true, 2);

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
                Intent intent = new Intent(SettGroupPermission.this, SettGroupPermissionDetail.class);
                intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
                intent.putExtra(getText(R.string.TAG_ISCREATE).toString(), true);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        ctList.removeAllViews();
        SettGroupPerTask task = new SettGroupPerTask();
        task.execute(String.valueOf(groupID));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if (resultCode == 1)
                Snackbar.make(ctList, getText(R.string.text_successcreaterank), Snackbar.LENGTH_SHORT).show();
        }
        else
        {
            if (resultCode == 1)
                Snackbar.make(ctList, getText(R.string.text_successchangedrank), Snackbar.LENGTH_SHORT).show();
            else if (resultCode == 2)
                Snackbar.make(ctList, getText(R.string.text_successdeleterank), Snackbar.LENGTH_SHORT).show();
        }
    }

    private class RankButton
    {
        RankButton(final String rankName, final int rankID)
        {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.item_permission, ctList, false);

            LinearLayout ctPermission = (LinearLayout) convertView.findViewById(R.id.ctPermission);
            TextView tvPermission = (TextView) convertView.findViewById(R.id.tvPermission);

            tvPermission.setText(rankName);

            ctPermission.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent intent = new Intent(SettGroupPermission.this, SettGroupPermissionDetail.class);
                    intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
                    intent.putExtra(getText(R.string.TAG_RANKID).toString(), rankID);
                    intent.putExtra(getText(R.string.TAG_RANKNAME).toString(), rankName);
                    startActivityForResult(intent, 0);
                }
            });

            ctList.addView(convertView);
        }
    }

    private class SettGroupPerTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(SettGroupPermission.this, getText(R.string.text_loading).toString(), null, true, true);
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

            try
            {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_SETT_GROUP_PER).toString());

                int length = jsonArray.length();
                if(length > 0)
                {
                    int index = 0;

                    while (length > index) {
                        JSONObject item = jsonArray.getJSONObject(index);

                        int rankID = item.getInt(getText(R.string.TAG_RANKID).toString());
                        String rankName = item.getString(getText(R.string.TAG_RANKNAME).toString());

                        if(rankID > 0)
                            new RankButton(rankName, rankID);

                        index++;
                    }
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout ctTemp = new LinearLayout(SettGroupPermission.this);
                ctTemp.setLayoutParams(params);

                ctList.addView(ctTemp);
            }
            catch (JSONException e)
            {
                Log.d(TAG, "groupListEvent : ", e);
            }
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            serverURL = "http://weloud.duckdns.org/weloud/db_get_group_rank.php";
            postParameters = "groupid=" + params[0];

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
                Log.d(TAG, "SettGroupPermission: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
