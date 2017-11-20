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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.ctPermission;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupCloudList;
import static com.example.reveu.weloudgrouplist.R.id.tvPermission;

/**
 * Created by reveu on 2017-08-23.
 */

public class SettGroupPermissionDetail extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();

    private String rankName;
    private int rankID;
    private int groupID;
    private int permission;
    private boolean isCreate;

    int[] perStringID = {R.string.per_fileupload, R.string.per_filerename, R.string.per_filemove, R.string.per_filedelete,
            R.string.per_folderadd, R.string.per_foldermodify, R.string.per_folderdelete, R.string.per_approvejoin,
            R.string.per_invite, R.string.per_managegroup};

    LinearLayout btnRankDelete;
    LinearLayout ctList;
    EditText etRankName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_group_permission_detail);

        ctList = (LinearLayout) findViewById(R.id.settings_groupper_detail_ctRankList);
        btnRankDelete = (LinearLayout) findViewById(R.id.settings_groupper_detail_btnDelete);
        etRankName = (EditText) findViewById(R.id.settings_groupper_detail_etRankName);

        Intent intent = getIntent();
        rankID = intent.getIntExtra(getText(R.string.TAG_RANKID).toString(), -1);
        rankName = intent.getStringExtra(getText(R.string.TAG_RANKNAME).toString());
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);
        isCreate = intent.getBooleanExtra(getString(R.string.TAG_ISCREATE).toString(), false);

        etRankName.setText(rankName);

        abLib.setDefaultActionBar(this, rankName, true, 1);

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
                String name = etRankName.getText().toString();
                if(new StockLib().isNickFormatted(name))
                {
                    SettGroupPerDetailTask task = new SettGroupPerDetailTask(1);
                    task.execute(String.valueOf(groupID), name, String.valueOf(permission), String.valueOf(rankID));
                }
                else
                    Snackbar.make(ctList, getString(R.string.text_notformatted, getText(R.string.text_rankname)), Snackbar.LENGTH_SHORT).show();
            }
        });

        btnRankDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(SettGroupPermissionDetail.this);
                alertDelete.setTitle(getText(R.string.text_delete).toString())
                        .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                SettGroupPerDetailTask task = new SettGroupPerDetailTask(2);
                                task.execute(String.valueOf(groupID), String.valueOf(rankID));
                            }
                        })

                        .setNegativeButton(getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });

                alertDelete.setMessage(getString(R.string.text_areyousuredelete_one, rankName));
                alertDelete.show();
            }
        });

        if(isCreate)
        {
            abLib.setTvActionBarTitle(getString(R.string.text_createrank));
            btnRankDelete.setVisibility(View.GONE);
            permission = 0;
            createPermissionSwitch();
        }
        else
        {
            if(rankID == 1)
                btnRankDelete.setVisibility(View.GONE);
            else
                btnRankDelete.setVisibility(View.VISIBLE);
            SettGroupPerDetailTask task = new SettGroupPerDetailTask(0);
            task.execute(String.valueOf(groupID), String.valueOf(rankID));
        }
    }

    void createPermissionSwitch()
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int i=0; i<perStringID.length; i++)
        {
            new PermissionSwitch(inflater, getText(perStringID[i]).toString(), 1<<i);
        }
    }

    private class PermissionSwitch
    {
        PermissionSwitch(LayoutInflater inflater, String perName, final int perSwitch)
        {
            View convertView = inflater.inflate(R.layout.item_permission_detail, ctList, false);

            TextView tvPermission = (TextView) convertView.findViewById(R.id.tvPermission);
            Switch swPermission = (Switch) convertView.findViewById(R.id.swPermission);

            tvPermission.setText(perName);

            if((permission & perSwitch) > 0)
                swPermission.setChecked(true);
            else
                swPermission.setChecked(false);

            swPermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if(isChecked)
                        permission += perSwitch;
                    else
                        permission -= perSwitch;
                }
            });

            ctList.addView(convertView);
        }
    }

    private class SettGroupPerDetailTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        int taskType;

        /*
         * TaskType 0 : 조회
         *          1 : 업데이트 (isCreate가 True 일 경우 삽입)
         *          2 : 삭제
         */
        SettGroupPerDetailTask(int type)
        {
            this.taskType = type;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SettGroupPermissionDetail.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if(taskType == 0)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray;

                    jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_SETT_GROUP_PER_DETAIL).toString());

                    JSONObject item = jsonArray.getJSONObject(0);

                    permission = item.getInt(getText(R.string.TAG_PERMISSION).toString());
                }
                catch(JSONException je)
                {
                    je.printStackTrace();
                }
                createPermissionSwitch();
            }
            else
            {
                if(taskType == 1)
                {
                    if (result.contains("*rank_already"))
                        Snackbar.make(ctList, getString(R.string.text_ranknameexsists, etRankName.getText().toString()), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    else
                    {
                        Intent intent = getIntent();
                        setResult(taskType,intent);
                        finish();
                    }
                }
                else
                {
                    Intent intent = getIntent();
                    setResult(taskType,intent);
                    finish();
                }
            }

            Log.d(TAG, "response  - " + result);
        }

        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(taskType == 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_rank_permission.php";
                postParameters = "groupid=" + params[0] + "&rankID=" + params[1];
            }
            else if(taskType == 1)
            {
                postParameters = "groupid=" + params[0] + "&rankname=" + params[1] + "&permission=" + params[2];

                if(isCreate)
                    serverURL = "http://weloud.duckdns.org/weloud/db_set_grouprank.php";
                else
                {
                    serverURL = "http://weloud.duckdns.org/weloud/db_update_group_rank.php";
                    postParameters = postParameters + "&rankID=" + params[3];
                }
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_delete_group_rank.php";
                postParameters = "groupid=" + params[0] + "&rankID=" + params[1];
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
                Log.d(TAG, "SettGroupPermissionDetail: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
