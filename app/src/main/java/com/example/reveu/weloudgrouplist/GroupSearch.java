package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
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

import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-07-08.
 */

public class GroupSearch extends AppCompatActivity
{
    private LinearLayout ctMain;
    private ListView lvGroup;
    private EditText etSearch;
    private ImageView btnSearch;
    private ImageView btnActionBarBack;
    private ImageView btnActionBarConfirm;
    private TextView tvActionBarTitle;
    private GroupListAdapter glaAdapter = new GroupListAdapter();

    private String userNum;

    String jsonString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        UITask task = new UITask();
        task.execute();
    }

    private void UIAction()
    {
        setContentView(R.layout.activity_groupsearch);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());

        ctMain = (LinearLayout) findViewById(R.id.groupSearch_ctMain);
        lvGroup = (ListView) findViewById(R.id.groupSearch_lvGroup);
        etSearch = (EditText) findViewById(R.id.groupSearch_etSearch);
        btnSearch = (ImageView) findViewById(R.id.groupSearch_btnSearch);

        lvGroup.setAdapter(glaAdapter);

        glaAdapter.clearList();

        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(etSearch.getText().toString() == "")
                {
                    Snackbar.make(ctMain, "검색어를 입력해주세요.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                else
                {
                    hideKeyboard(v);

                    SearchGroup task = new SearchGroup();
                    task.execute(etSearch.getText().toString(), userNum, "0", "null");
                }
            }
        });

        lvGroup.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupListItem item = glaAdapter.getItem(position);
                if(item.getStatus() == 2)
                {
                    Snackbar.make(ctMain, "이미 가입되어 있습니다.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                }
                else
                {
                    SearchGroup task = new SearchGroup();
                    task.execute(String.valueOf(item.getGroupID()), userNum, "1", String.valueOf(position));
                }
            }
        });

        setDefaultActionBar("그룹 검색", false, 0);

        btnActionBarBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    private void setDefaultActionBar(String title, boolean isClose, int confirmType)
    {
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        View customBar = LayoutInflater.from(this).inflate(R.layout.actionbar_default, null);
        actionBar.setCustomView(customBar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4472c4")));

        Toolbar parent = (Toolbar) customBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        btnActionBarBack = (ImageView) customBar.findViewById(R.id.actionbar_default_btnBack);
        btnActionBarConfirm = (ImageView) customBar.findViewById(R.id.actionbar_default_btnConfirm);
        tvActionBarTitle = (TextView) customBar.findViewById(R.id.actionbar_default_tvTitle);

        tvActionBarTitle.setText(title);

        if(isClose)
        {
            btnActionBarBack.setImageResource(R.drawable.cancle);
        }
        else
        {
            btnActionBarBack.setImageResource(R.drawable.backbtn);
        }

        if(confirmType > 0)
        {
            btnActionBarConfirm.setVisibility(View.VISIBLE);
            if(confirmType == 1)
            {
                btnActionBarConfirm.setImageResource(R.drawable.checkmark);
            }
            else
            {
                btnActionBarConfirm.setImageResource(R.drawable.addgroup_big);
            }
        }
        else
        {
            btnActionBarConfirm.setVisibility(View.INVISIBLE);
        }
    }

    private void searchEvent()
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_SEARCH).toString());

            glaAdapter.clearList();

            if(jsonArray.length() == 0)
            {
                Snackbar.make(ctMain, "검색 결과가 없습니다.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
            else
            {
                JSONObject item = jsonArray.getJSONObject(0);

                int groupID = Integer.parseInt(item.getString(getText(R.string.TAG_GROUPID).toString()));
                String groupName = item.getString(getText(R.string.TAG_GROUPNAME).toString());
                String nickname = item.getString(getText(R.string.TAG_NICKNAME).toString());
                int isMember = item.getInt(getText(R.string.TAG_ISMEMBER).toString());
                int isAdmission = item.getInt(getText(R.string.TAG_ISADMISSION).toString());
                int status = 0;

                if(isMember > 0)
                {
                    status = 2;
                }
                else if(isAdmission > 0)
                {
                    status = 1;
                }

                glaAdapter.addItem(groupID, false, groupName, nickname, status);
            }

            glaAdapter.notifyDataSetChanged();
        }
        catch (JSONException e)
        {
            Log.d(TAG, "showResult : ", e);
        }
    }

    // 나는 키보드가 싫다!!!!!!!!!!!!
    private void hideKeyboard(View v)
    {
        InputMethodManager imm= (InputMethodManager) getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private class SearchGroup extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        int position;
        String groupinfo;
        String usernum;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(GroupSearch.this, getText(R.string.text_loading).toString(), null, true, true);
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result == null)
            {
                Snackbar.make(ctMain, getText(R.string.error_temporary).toString(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
            else
            {
                if (result.contains("*admission"))
                {
                    int status = 0;
                    if(result.contains("deleted*"))
                    {
                        Snackbar.make(ctMain, "가입 신청이 취소되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        status = 0;
                    }
                    else if(result.contains("inserted*"))
                    {
                        Snackbar.make(ctMain, "가입 신청 되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        status = 1;
                    }
                    else
                    {
                        Snackbar.make(ctMain, "이미 가입되어 있습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        status = 2;
                    }

                    GroupListItem item = glaAdapter.getItem(position);
                    item.setStatus(status);
                    glaAdapter.notifyDataSetChanged();
                }
                else if(result.contains("*autojoin"))
                {
                    SearchGroup task = new SearchGroup();
                    task.execute(groupinfo, userNum, "2", "null");
                }
                else if(result.contains("*group_user_inserted"))
                {
                    Snackbar.make(ctMain, "가입 되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    GroupListItem item = glaAdapter.getItem(position);
                    item.setStatus(2);
                    glaAdapter.notifyDataSetChanged();
                }
                else
                {
                    jsonString = result;
                    searchEvent();
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            groupinfo = params[0];
            usernum = params[1];
            int type = Integer.parseInt(params[2]);
            if(params[3] != "null")
                position = Integer.parseInt(params[3]);

            String serverURL;
            String postParameters;
            if (type == 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_search_groupsearch.php";
                postParameters = "groupname=" + groupinfo + "&usernum=" + usernum;
            }
            else
            {
                if(type == 1)
                    serverURL = "http://weloud.duckdns.org/weloud/db_search_useradmission.php";
                else
                    serverURL = "http://weloud.duckdns.org/weloud/db_set_groupmember.php";
                postParameters = "groupid=" + groupinfo + "&usernum=" + usernum;
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
                Log.d(TAG, "SearchGroup: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }

    private class UITask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {
            UIAction();
        }
    }
}
