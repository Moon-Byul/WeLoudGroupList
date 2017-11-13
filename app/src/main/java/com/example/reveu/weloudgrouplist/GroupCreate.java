package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.regex.Pattern;

import static android.R.attr.id;
import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-07-17.
 */

public class GroupCreate extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();
    private StockLib stLib = new StockLib();

    private LinearLayout ctMain;
    private EditText etGroupName;
    private Switch swAutoJoin;
    private Switch swAllow;
    private TextView tvErrorMsg;

    private ProgressDialog progressDialog;

    private String userNum;
    private String groupID;
    private FTPLib ftpMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groupcreate);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());

        ctMain = (LinearLayout) findViewById(R.id.groupCreate_ctMain);
        etGroupName = (EditText) findViewById(R.id.groupCreate_etGroupName);
        swAutoJoin = (Switch) findViewById(R.id.groupCreate_swAutoJoin);
        swAllow = (Switch) findViewById(R.id.groupCreate_swAllow);
        tvErrorMsg = (TextView) findViewById(R.id.groupCreate_tvErrorMsg);

        abLib.setDefaultActionBar(this, getText(R.string.text_creategroup).toString(), false, 1);

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
                String groupName = etGroupName.getText().toString();
                if(!stLib.isGroupNameFormatted(groupName))
                {
                    tvErrorMsg.setText(getString(R.string.text_notformatted, getText(R.string.text_groupname)));
                }
                else
                {
                    String autoJoin = (swAutoJoin.isChecked() ? "1" : "0");
                    String allow = (swAllow.isChecked() ? "1" : "0");

                    progressDialog = ProgressDialog.show(GroupCreate.this, getText(R.string.text_loading).toString(), null, true, true);

                    CreateGroupTask task = new CreateGroupTask();
                    task.execute("0", groupName, String.valueOf(userNum), autoJoin, allow);
                }
            }
        });
    }

    private String getJsonData(String jsonString, int type)
    {
        String result = "null";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray;
            if(type == 1)
                jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPRANK).toString());
            else
                jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPCREATE).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            if(type == 0)
            {
                result = item.getString(getText(R.string.TAG_GROUPID).toString());
            }
            else if(type == 1)
            {
                result = item.getString(getText(R.string.TAG_RANKID).toString());
            }
            else
            {
                result = item.getString(getText(R.string.TAG_SERVERIP).toString());
            }
        }
        catch (JSONException e)
        {
            Log.d(TAG, "createGroup : ", e);
        }
        finally
        {
            return result;
        }
    }

    private void dismissDialog()
    {
        if(progressDialog != null)
        {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private class CreateGroupTask extends AsyncTask<String, Void, String>
    {
        int type;

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(result == null)
            {
                Snackbar.make(ctMain, getText(R.string.error_temporary).toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            else
            {
                /*
                 * 순차적 시스템.
                 *
                 * type 0 (첫번째 Asynctask) 에서 PHP와 통신하여 그룹 이름이 중복되는지 확인.
                 * type 0은 조금 특별하게 type 1~2와 다르게 execute를 사용하는데, 이는 폴더 생성이 정상적으로 되었는지 확인하기 위해
                 * method에 따로 배치해놓음 (invoke, reflect 사용)
                 *
                 * type 1에서 랭크 이름이 중복되는지 확인. (그럴일은 없겠지만 만약을 위해)
                 *
                 * type 2에서 그룹 생성자가 이미 그룹에 가입되어있는지 확인 (역시 그럴일 없음)
                 *
                 * type 3은 그룹 생성 알림을 위한 Toast Message 출력.
                 */
                if(type == 0)
                {
                    if (result.contains("*group_already"))
                    {
                        tvErrorMsg.setText(getString(R.string.text_groupnameexists, etGroupName.getText()));
                        dismissDialog();
                    }
                    else
                    {
                        groupID = getJsonData(result, type);
                        String serverIP = getJsonData(result, 2);

                        ftpMain = new FTPLib(serverIP, "/", GroupCreate.this);
                        ftpMain.execute(GroupCreate.this, "dirCreateSuccess", FTPCMD.MakeDirectory, etGroupName.getText().toString());
                    }
                }
                else if(type == 1)
                {
                    if (result.contains("*rank_already"))
                    {
                        Snackbar.make(ctMain, getText(R.string.text_ranknameexsists), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        dismissDialog();
                    }
                    else
                    {
                        String rankID = getJsonData(result, type);

                        if(rankID.equals("3"))
                        {
                            CreateGroupTask task = new CreateGroupTask();
                            task.execute("2", String.valueOf(groupID), String.valueOf(userNum));
                        }
                    }
                }
                else if(type == 2)
                {
                    if (result.contains("*group_user_already"))
                    {
                        Snackbar.make(ctMain, getText(R.string.text_youalreadyjoined), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        dismissDialog();
                    }
                    else
                    {
                        CreateGroupTask task = new CreateGroupTask();
                        task.execute("3", String.valueOf(groupID), String.valueOf(userNum), "0");
                    }
                }
                else
                {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(), getText(R.string.text_successcreategroup), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            type = Integer.parseInt(params[0]);

            String serverURL;
            String postParameters;
            if (type == 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_set_groupinfo.php";
                postParameters = "groupname=" + params[1] + "&usernum=" + params[2] + "&autojoin=" + params[3] + "&allow=" + params[4];
            }
            else if(type == 1)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_set_grouprank.php";
                postParameters = "groupid=" + params[1] + "&rankname=" + params[2] + "&permission=" + params[3];
            }
            else if(type == 2)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_set_groupmember.php";
                postParameters = "groupid=" + params[1] + "&usernum=" + params[2];
            }
            else
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_update_userrank.php";
                postParameters = "groupid=" + params[1] + "&usernum=" + params[2] + "&rankid=" + params[3];
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
                Log.d(TAG, "CreateGroup: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }

    /*
     *  아래의 부분은 Invoke (Reflect)를 통해서 사용하는 메소드들 입니다.
     *  never used가 뜬다고 해서 지우지 마시오.
     */
    public void dirCreateSuccess()
    {
        String[] nameArr = {"개설자", "일반회원", "특별회원", "관리자"};
        String[] perArr = {"2047", "0", "127", "1023"};

        for(int i=0; i<perArr.length; i++)
        {
            CreateGroupTask task = new CreateGroupTask();
            task.execute("1", String.valueOf(groupID), nameArr[i], perArr[i]);
        }
    }
}
