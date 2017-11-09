package com.example.reveu.weloudgrouplist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class GroupCloudList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragGroupCloudList fragGroupCloudList;
    ConstraintLayout ctGroupCloudModify;
    ImageView ivGroupCloudDownloadOrRename;
    ImageView ivGroupCloudDelete;
    ImageView ivGroupCloudMove;
    ImageView ivGroupCloudCancel;
    TextView tvGroupCloudAmount;
    //ImageView

    private Menu menu;
    private Menu menuNav;
    private String userNum;
    private String ID;
    private String nickName;
    private int userPermission;
    private int groupID;
    private String groupName;
    private int modifyLayoutType; // 0 - File , 1 - Folder

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
        ivGroupCloudDownloadOrRename = (ImageView) findViewById(R.id.iv_GroupCloud_downloadOrRename);
        ivGroupCloudDelete = (ImageView) findViewById(R.id.iv_GroupCloud_delete);
        ivGroupCloudCancel = (ImageView) findViewById(R.id.iv_GroupCloud_cancel);
        ivGroupCloudMove = (ImageView) findViewById(R.id.iv_GroupCloud_move);
        tvGroupCloudAmount = (TextView) findViewById(R.id.tv_GroupCloud_amount);
        ctGroupCloudModify = (ConstraintLayout) findViewById(R.id.ctGroupCloudModify);

        modifyLayoutType = -1;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuNav = navigationView.getMenu();

        TextView tvNavNick = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvNick);
        TextView tvNavId = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvId);

        tvNavNick.setText(nickName);
        tvNavId.setText(ID);
        setTitle(groupName);

        GroupCloudTask gcTask = new GroupCloudTask();
        gcTask.execute("0", String.valueOf(groupID));

        GroupCloudTask gcTaskPms = new GroupCloudTask();
        gcTaskPms.execute("1", String.valueOf(groupID), String.valueOf(userNum));

        ivGroupCloudDownloadOrRename.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(modifyLayoutType == 0)
                {
                    if(fragGroupCloudList.isExternalStorageWritable())
                    {
                        ArrayList<GroupFileItem> gciTemp = fragGroupCloudList.getAdapter().getCheckList();

                        for (GroupFileItem temp : gciTemp)
                        {
                            File defaultDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            ftpMain.downloadFile(temp.getFile().getName(), "", defaultDownload.getPath());
                        }
                    }
                }
                else
                {
                    fragGroupCloudList.fileRenameEvent(fragGroupCloudList.getAdapter().getCheckList().get(0), ftpMain);
                }
                modifyLayoutEvent(false, null);
            }
        });

        ivGroupCloudDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(GroupCloudList.this);

                alertDelete.setTitle(getText(R.string.text_delete).toString())
                .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ArrayList<GroupFileItem> gciTemp = fragGroupCloudList.getAdapter().getCheckList();

                        for (GroupFileItem temp : gciTemp)
                        {
                            ftpMain.delete(temp.getFile());
                        }

                        modifyLayoutEvent(false, null);

                        ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
                    }
                })

                .setNegativeButton(getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

                if(fragGroupCloudList.getAdapter().getCheckCount() > 1)
                    alertDelete.setMessage(getString(R.string.text_areyousuredelete_multi, fragGroupCloudList.getAdapter().getCheckCount()));
                else
                    alertDelete.setMessage(getString(R.string.text_areyousuredelete_one, fragGroupCloudList.getAdapter().getCheckList().get(0).getFile().getName()));

                alertDelete.show();
            }
        });

        ivGroupCloudMove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ArrayList<GroupFileItem> gciTemp = fragGroupCloudList.getAdapter().getCheckList();

                for (GroupFileItem temp : gciTemp)
                {
                    Log.d("Twily", "Rename : " + temp.getPath() + "/" + temp.getFile().getName() + " to : " + ftpMain.getWorkingPath() + "/" + temp.getFile().getName());
                    ftpMain.execute(FTPCMD.Rename, temp.getPath() + "/" + temp.getFile().getName(), ftpMain.getWorkingPath() + "/" + temp.getFile().getName());
                }

                modifyLayoutEvent(false, null);
                ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
            }
        });

        ivGroupCloudCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                modifyLayoutEvent(false, null);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //fragGroupCloudList.getGroupList();
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

        if (id == R.id.nav_gc_setting)
        {
            Intent intent = new Intent(GroupCloudList.this, SettUserMain.class);
            intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
            startActivity(intent);
        }
        else if (id == R.id.nav_gc_groupsett)
        {
            Intent intent = new Intent(GroupCloudList.this, SettGroupMain.class);
            intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
            startActivity(intent);
        }
        else if (id == R.id.nav_gc_backlist)
        {
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    public void loadFileList()
    {
        fragGroupCloudList.loadFileList();
    }

    private void permissionEvent()
    {
        /*
         * Group Setting 접근 권한 확인 후 Visible
         */
        MenuItem item = menuNav.findItem(R.id.nav_gc_groupsett);
        PermissionLib pmLib = new PermissionLib();

        if(pmLib.isUserManageGroup(userPermission))
        {
            item.setVisible(true);
        }
        else
        {
            item.setVisible(false);
        }

        // fab 권한 체크를 위한 method call
        ((FragGroupCloudList) fragGroupCloudList).permissionEvent();
    }

    public void modifyLayoutEvent(boolean visible, GroupFileItem item)
    {
        if(visible)
        {
            PermissionLib pmLib = new PermissionLib();

            if(ftpMain.getExt(item.getFile()).equals("folder"))
            {
                modifyLayoutType = 1;

                //권한 체크
                if(pmLib.isUserFolderDelete(userPermission))
                    ivGroupCloudDelete.setVisibility(View.VISIBLE);
                else
                    ivGroupCloudDelete.setVisibility(View.GONE);

                if(pmLib.isUserFolderModify(userPermission))
                {
                    ivGroupCloudMove.setVisibility(View.VISIBLE);
                    ivGroupCloudDownloadOrRename.setImageResource(R.drawable.folderrename);
                    ivGroupCloudDownloadOrRename.setVisibility(View.VISIBLE);
                }
                else
                {
                    ivGroupCloudMove.setVisibility(View.GONE);
                    ivGroupCloudDownloadOrRename.setVisibility(View.GONE);
                }
            }
            else
            {
                modifyLayoutType = 0;

                //권한 체크
                if(pmLib.isUserFileDelete(userPermission))
                    ivGroupCloudDelete.setVisibility(View.VISIBLE);
                else
                    ivGroupCloudDelete.setVisibility(View.GONE);

                if(pmLib.isUserFileMove(userPermission))
                    ivGroupCloudMove.setVisibility(View.VISIBLE);
                else
                    ivGroupCloudMove.setVisibility(View.GONE);

                ivGroupCloudDownloadOrRename.setImageResource(R.drawable.download);
                ivGroupCloudDownloadOrRename.setVisibility(View.VISIBLE);
            }

            ctGroupCloudModify.setVisibility(View.VISIBLE);
            fragGroupCloudList.getFab().setVisibility(View.GONE);
        }
        else
        {
            modifyLayoutType = -1;
            ctGroupCloudModify.setVisibility(View.GONE);
            fragGroupCloudList.getFab().setVisibility(View.VISIBLE);
            fragGroupCloudList.getAdapter().clearCheckList();

            ArrayList<GroupFileItem> gciTemp = fragGroupCloudList.getAdapter().getList();

            for(GroupFileItem temp : gciTemp)
            {
                if(temp.getChecked())
                    temp.setChecked(false);
            }

            fragGroupCloudList.getAdapter().notifyDataSetChanged();
        }
    }

    private void setVisibleItem(Menu menu, int id, boolean state)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(state);
    }

    private String jsonEvent(int type, String jsonString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray;

            if(type == 0)
                jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPCLOUD).toString());
            else
                jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_PERMISSION).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            if(type == 0)
                return item.getString(getText(R.string.TAG_SERVERIP).toString());
            else
                return item.getString(getText(R.string.TAG_PERMISSION).toString());
        }
        catch (JSONException e)
        {
            Log.d(TAG, "GroupCloudList : ", e);
            return "";
        }
    }

    public int getModifyLayoutType()
    {
        return modifyLayoutType;
    }

    public FTPLib getFTPMain()
    {
        return ftpMain;
    }

    public int getUserPermission()
    {
        return userPermission;
    }


    private class GroupCloudTask extends AsyncTask<String, Void, String>
    {
        int type;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            //progressDialog = ProgressDialog.show(GroupCloudList.this, getText(R.string.text_loading).toString(), null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            //progressDialog.dismiss();

            if(result == null)
            {
                Toast.makeText(getApplicationContext(), getText(R.string.error_temporary).toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(type == 0)
                {
                    String serverIP;
                    serverIP = jsonEvent(type, result);
                    ftpMain = new FTPLib(serverIP, "/" + groupName, GroupCloudList.this);

                    ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
                }
                else if(type == 1)
                {
                    userPermission = Integer.parseInt(jsonEvent(type, result));

                    permissionEvent();
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            type = Integer.parseInt(params[0]);
            String groupID = params[1];

            String serverURL = "";
            String postParameters = "";

            if(type == 0)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_groupcloud_ip.php";
                postParameters = "GroupID=" + groupID;
            }
            else if(type == 1)
            {
                String userNum = params[2];
                serverURL = "http://weloud.duckdns.org/weloud/db_get_userpermission.php";
                postParameters = "GroupID=" + groupID + "&userNum=" + userNum;
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
                Log.d(TAG, "GroupCloudList: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
