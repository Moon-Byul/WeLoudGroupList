package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import static android.R.attr.type;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupList;
import static com.example.reveu.weloudgrouplist.R.id.textinfo;

public class GroupCloudList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragGroupCloudList fragGroupCloudList;

    private Menu menu;
    private String userNum;
    private String ID;
    private String nickName;
    private int groupID;
    private String groupName;

    private FTPLib ftpMain;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupcloud_list);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());
        nickName = intent.getStringExtra(getText(R.string.TAG_NICKNAME).toString());
        groupID = intent.getIntExtra(getText(R.string.TAG_GROUPID).toString(), -1);
        groupName = intent.getStringExtra(getText(R.string.TAG_GROUPNAME).toString());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragGroupCloudList = (FragGroupCloudList) getSupportFragmentManager().findFragmentById(R.id.fragGroupCloudList);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView tvNavNick = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvNick);
        TextView tvNavId = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvId);

        tvNavNick.setText(nickName);
        tvNavId.setText(ID);
        setTitle(groupName);

        GroupCloudTask gcTask = new GroupCloudTask();
        gcTask.execute(String.valueOf(groupID));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //fragGroupCloudList.getGroupList();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(ftpMain != null)
            ftpMain.disconnect();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.group_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        */
        int id = item.getItemId();



        if (id == R.id.groupList_search)
        {
            /*
            setVisibleItem(menu, R.id.groupList_sortNameAsc, false);
            setVisibleItem(menu, R.id.groupList_sortNameDesc, false);
            setVisibleItem(menu, R.id.groupList_sortModAsc, false);
            setVisibleItem(menu, R.id.groupList_sortModDesc, false);
            setVisibleItem(menu, R.id.groupList_search, false);
            setVisibleItem(menu, R.id.groupList_cancle, true);

            GroupListAdapter adapter = fragGroupList.getAdapter();
            getSupportFragmentManager().beginTransaction().replace(R.id.ctGroupList, fragGroupSearch).commit();
            fragGroupSearch.setAdapterOnView(adapter);
            fragGroupList.setFabClicked(false);
            */
        }
        else if (id == R.id.groupList_cancle)
        {
            /*
            setVisibleItem(menu, R.id.groupList_sortNameAsc, true);
            setVisibleItem(menu, R.id.groupList_sortNameDesc, true);
            setVisibleItem(menu, R.id.groupList_sortModAsc, true);
            setVisibleItem(menu, R.id.groupList_sortModDesc, true);
            setVisibleItem(menu, R.id.groupList_search, true);
            setVisibleItem(menu, R.id.groupList_cancle, false);

            getSupportFragmentManager().beginTransaction().replace(R.id.ctGroupList, fragGroupList).commit();
            */
        }
        else if (id == R.id.groupList_sortNameAsc)
        {
            fragGroupCloudList.getAdapter().sortAscName();
        }
        else if (id == R.id.groupList_sortNameDesc)
        {
            fragGroupCloudList.getAdapter().sortDescName();
        }
        else if (id == R.id.groupList_sortModAsc)
        {
            fragGroupCloudList.getAdapter().sortAscDate();
        }
        else if (id == R.id.groupList_sortModDesc)
        {
            fragGroupCloudList.getAdapter().sortDescDate();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        /*
        if (id == R.id.nav_gl_settings)
        {
            Intent intent = new Intent(GroupCloudList.this, SettUserMain.class);
            intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
            startActivity(intent);
        }
        else if (id == R.id.nav_gl_logout)
        {

        }
        */


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void setVisibleItem(Menu menu, int id, boolean state)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(state);
    }

    public FTPLib getFTPMain()
    {
        return ftpMain;
    }


    private class GroupCloudTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(GroupCloudList.this, getText(R.string.text_loading).toString(), null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result == null)
            {
                Toast.makeText(getApplicationContext(), getText(R.string.error_temporary).toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                String serverIP;
                try
                {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray;
                    jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPCLOUD).toString());

                    JSONObject item = jsonArray.getJSONObject(0);

                    serverIP = item.getString(getText(R.string.TAG_SERVERIP).toString());
                    ftpMain = new FTPLib(serverIP, "/" + groupName, getApplicationContext());
                    fragGroupCloudList.loadFileList();
                }
                catch (JSONException e)
                {
                    Log.d(TAG, "GroupCloudList : ", e);
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String groupID = params[0];

            String serverURL = "http://weloud.duckdns.org/weloud/db_get_groupcloud_ip.php";
            String postParameters = "GroupID=" + groupID;
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
                Log.d(TAG, "GroupCloudList: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
