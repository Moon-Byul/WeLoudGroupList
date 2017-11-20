package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Iterator;

import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupUserRankDetail extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();
    private ArrayList<RankButton> btnArr = new ArrayList<RankButton>();

    private String[] userArr;
    private int groupID;

    LinearLayout ctList;
    ProgressDialog progressDialogRank;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_permission);

        ctList = (LinearLayout) findViewById(R.id.settings_grouppermission_ctList);

        Intent intent = getIntent();
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);
        userArr = intent.getStringArrayExtra(getText(R.string.TAG_USERNUM).toString());

        abLib.setDefaultActionBar(this, getText(R.string.text_userranksett).toString(), true, 0);

        abLib.getBtnActionBarBack().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        SettGroupPerTask task = new SettGroupPerTask();
        task.execute(String.valueOf(groupID));
    }

    void createRankButton(String result)
    {
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
                        btnArr.add(new RankButton(rankName, rankID));

                    index++;
                }
            }

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            LinearLayout ctTemp = new LinearLayout(SettGroupUserRankDetail.this);
            ctTemp.setLayoutParams(params);

            ctList.addView(ctTemp);
        }
        catch (JSONException e)
        {
            Log.d(TAG, "SettGroupUserRankDetail : ", e);
        }
    }

    void changeUserRankAll(int rankID)
    {
        progressDialogRank = ProgressDialog.show(SettGroupUserRankDetail.this, getText(R.string.text_loading).toString(), null, true, true);

        for(int i=0; i<userArr.length; i++)
        {
            SettGroupPerTask task;

            if(i < userArr.length - 1)
                task = new SettGroupPerTask(true);
            else
                task = new SettGroupPerTask(true, true);

            task.execute(String.valueOf(groupID), userArr[i], String.valueOf(rankID));
        }

        for(RankButton btn : btnArr)
        {
            if(btn.rankID == rankID)
                btn.ivPermission.setImageResource(R.drawable.checkmark);
            else
                btn.ivPermission.setImageResource(0);
        }
    }

    void dismissProgressDialog()
    {
        if(progressDialogRank != null)
        {
            if(progressDialogRank.isShowing())
                progressDialogRank.dismiss();
        }
    }

    private class RankButton
    {
        LinearLayout ctPermission;
        TextView tvPermission;
        ImageView ivPermission;
        int rankID = -1;

        RankButton(final String rankName, int rankIDInput)
        {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View convertView = inflater.inflate(R.layout.item_permission, ctList, false);

            ctPermission = (LinearLayout) convertView.findViewById(R.id.ctPermission);
            tvPermission = (TextView) convertView.findViewById(R.id.tvPermission);
            ivPermission = (ImageView) convertView.findViewById(R.id.ivPermission);

            tvPermission.setText(rankName);

            this.rankID = rankIDInput;

            ctPermission.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    changeUserRankAll(rankID);
                }
            });

            ivPermission.setImageResource(0);
            ivPermission.setRotation(0.0f);
            ivPermission.setColorFilter(Color.argb(255, 0, 0, 255));

            ctList.addView(convertView);
        }
    }

    private class SettGroupPerTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        boolean isUpdate = false;
        boolean isLast = false;

        SettGroupPerTask()
        {

        }

        SettGroupPerTask(boolean isUpdate)
        {
            this.isUpdate = isUpdate;
        }

        SettGroupPerTask(boolean isUpdate, boolean isLast)
        {
            this.isUpdate = isUpdate;
            this.isLast = isLast;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(!isUpdate)
                progressDialog = ProgressDialog.show(SettGroupUserRankDetail.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(!isUpdate)
                createRankButton(result);

            if(isLast)
                dismissProgressDialog();
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(isUpdate)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_update_userrank.php";
                postParameters = "groupid=" + params[0] + "&usernum=" + params[1] + "&rankid=" + params[2];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_rank.php";
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
                Log.d(TAG, "SettGroupUserRankDetail: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
