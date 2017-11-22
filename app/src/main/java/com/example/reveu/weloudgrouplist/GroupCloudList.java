package com.example.reveu.weloudgrouplist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.EditText;
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
import java.security.acl.Group;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import static android.R.attr.permission;
import static android.R.attr.type;
import static android.R.id.input;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.drawable.user;
import static com.example.reveu.weloudgrouplist.R.id.fab;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupCloudList;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupList;
import static com.example.reveu.weloudgrouplist.R.id.textinfo;

public class GroupCloudList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    FragGroupCloudList fragGroupCloudList;
    FragGroupCloudSearch fragGroupCloudSearch;

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
    private String serverIP;
    private String defaultFTPPath;
    private int groupID;
    private String groupName;
    private boolean isFav;
    private int modifyLayoutType; // 0 - File , 1 - Folder
    private PermissionLib pmLib;

    private FTPLib ftpMain;
    private boolean isSearchFragVisible = false;

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
        isFav = intent.getBooleanExtra(getText(R.string.TAG_USERAPPROVED).toString(), false);
        defaultFTPPath = "/" + groupName;
        pmLib = new PermissionLib(GroupCloudList.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragGroupCloudList = (FragGroupCloudList) getSupportFragmentManager().findFragmentById(R.id.fragGroupCloudList);
        fragGroupCloudSearch = new FragGroupCloudSearch();

        ivGroupCloudDownloadOrRename = (ImageView) findViewById(R.id.iv_GroupCloud_downloadOrRename);
        ivGroupCloudDelete = (ImageView) findViewById(R.id.iv_GroupCloud_delete);
        ivGroupCloudCancel = (ImageView) findViewById(R.id.iv_GroupCloud_cancel);
        ivGroupCloudMove = (ImageView) findViewById(R.id.iv_GroupCloud_move);
        tvGroupCloudAmount = (TextView) findViewById(R.id.tv_GroupCloud_amount);
        ctGroupCloudModify = (ConstraintLayout) findViewById(R.id.ctGroupCloudModify);

        modifyLayoutType = -1;
        isSearchFragVisible = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuNav = navigationView.getMenu();

        favColorEvent();

        TextView tvNavNick = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvNick);
        TextView tvNavId = (TextView) navigationView.getHeaderView(0).findViewById(R.id.groupList_nav_tvId);

        tvNavNick.setText(nickName);
        tvNavId.setText(ID);
        setTitle(groupName);

        ivGroupCloudDownloadOrRename.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (modifyLayoutType == 0)
                {
                    if (isExternalStorageWritable())
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
                    fileRenameEvent(fragGroupCloudList.getAdapter().getCheckList().get(0), ftpMain);
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

                                updateUploadTime();
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

                if (fragGroupCloudList.getAdapter().getCheckList().size() > 1)
                    alertDelete.setMessage(getString(R.string.text_areyousuredelete_multi, fragGroupCloudList.getAdapter().getCheckList().size()));
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
                    ftpMain.execute(FTPCMD.Move, temp.getPath() + "/" + temp.getFile().getName(), ftpMain.getWorkingPath() + "/" + temp.getFile().getName());
                }

                modifyLayoutEvent(false, null);

                updateUploadTime();
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

        GroupCloudTask gcTask = new GroupCloudTask(false, false);
        gcTask.execute(String.valueOf(groupID));

        refreshPermissionLib();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            if (isSearchFragVisible)
            {
                replacedSearchFragmentEvent();
            }
            else
            {
                if (ftpMain.isDirectoryDefault(ftpMain.getWorkingPath()))
                    super.onBackPressed();
                else
                    cwdEvent("..");
            }
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        /*
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        */
        int id = item.getItemId();


        if (id == R.id.groupList_search)
        {
            setVisibleItem(menu, R.id.groupList_sortNameAsc, false);
            setVisibleItem(menu, R.id.groupList_sortNameDesc, false);
            setVisibleItem(menu, R.id.groupList_sortModAsc, false);
            setVisibleItem(menu, R.id.groupList_sortModDesc, false);
            setVisibleItem(menu, R.id.groupList_search, false);
            setVisibleItem(menu, R.id.groupList_cancle, true);

            fragGroupCloudSearch.getAdapter().clearList();
            isSearchFragVisible = true;
            getSupportFragmentManager().beginTransaction().replace(R.id.ctGroupCloudList, fragGroupCloudSearch).commit();
        }
        else if (id == R.id.groupList_cancle)
        {
            replacedSearchFragmentEvent();
            fragGroupCloudList.getAdapter().notifyDataSetChanged();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        /*
         *  개인 유저 설정 Activity로 넘어가는 Event
         */
        if (id == R.id.nav_gc_setting)
        {
            Intent intent = new Intent(GroupCloudList.this, SettUserMain.class);
            intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
            startActivity(intent);
        }
        /*
         *  그룹 설정 Activity로 넘어가는 Event
         */
        else if (id == R.id.nav_gc_groupsett)
        {
            Intent intent = new Intent(GroupCloudList.this, SettGroupMain.class);
            intent.putExtra(getText(R.string.TAG_USERNUM).toString(), userNum);
            intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
            startActivity(intent);
        }
        else if (id == R.id.nav_gc_fav)
        {
            GroupCloudTask task = new GroupCloudTask(false, true);
            task.execute(String.valueOf(groupID), userNum, (isFav ? "0" : "1"));

            isFav = !isFav;
            favColorEvent();
        }
        else if (id == R.id.nav_gc_backlist)
        {
            finish();
        }
        else if (id == R.id.nav_gc_dropouts)
        {
            if(pmLib.isUserCreator())
            {
                GroupCloudTask task = new GroupCloudTask();
                task.execute(String.valueOf(groupID));
            }
            else
            {
                leaveGroupEvent();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    void refreshPermissionLib()
    {
        pmLib.execute(GroupCloudList.this, "permissionEvent", String.valueOf(groupID), String.valueOf(userNum));
    }

    int getMemberCount(String result)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPCLOUD).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            return item.getInt(getText(R.string.TAG_MEMBERCOUNT).toString());
        }
        catch (JSONException e)
        {
            Log.d(TAG, "GroupCloudList : ", e);
            return -1;
        }
    }

    void chooseActionToCreator()
    {
        ArrayList<CharSequence> listItems = new ArrayList<CharSequence>();

        listItems.add(getText(R.string.text_deletegroup));
        listItems.add(getText(R.string.text_afterinheritance));
        listItems.add(getText(R.string.text_cancel));

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getText(R.string.text_chooseyouraction))
                .setItems(items, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        // AfterInheritance
                        if (items[index].equals(getText(R.string.text_afterinheritance).toString()))
                        {
                            Intent intent = new Intent(GroupCloudList.this, SettGroupUserInheritance.class);
                            intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
                            intent.putExtra(getText(R.string.TAG_USERNUM).toString(), userNum);
                            startActivityForResult(intent, 2);
                        }
                        // Delete Event
                        else if (items[index].equals(getText(R.string.text_deletegroup).toString()))
                        {
                            leaveGroupEvent();
                        }
                    }
                })

                .show();
    }

    void leaveGroupEvent()
    {
        AlertDialog.Builder alertDelete = new AlertDialog.Builder(GroupCloudList.this);

        if(pmLib.isUserCreator())
        {
            alertDelete.setTitle(getText(R.string.text_deletegroup).toString());
            alertDelete.setMessage(getText(R.string.text_areyousuredeletegroup));
        }
        else
        {
            alertDelete.setTitle(getText(R.string.text_dropoutsgroup).toString());
            alertDelete.setMessage(getText(R.string.text_areyousureleave));
        }

        alertDelete.setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        GroupCloudTask task = new GroupCloudTask(true, (pmLib.isUserCreator() ? 1 : 0));
                        task.execute(String.valueOf(groupID), userNum);
                    }
                })

                .setNegativeButton(getText(R.string.text_cancel).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });

        alertDelete.show();
    }

    public void cwdEvent(String path)
    {
        ftpMain.execute(this, "changeTitleText", FTPCMD.ChangeWorkingDirectory, path);
        ftpMain.execute(this, "loadFileList", FTPCMD.GetFileList);
    }

    public void replacedSearchFragmentEvent()
    {
        setVisibleItem(menu, R.id.groupList_sortNameAsc, true);
        setVisibleItem(menu, R.id.groupList_sortNameDesc, true);
        setVisibleItem(menu, R.id.groupList_sortModAsc, true);
        setVisibleItem(menu, R.id.groupList_sortModDesc, true);
        setVisibleItem(menu, R.id.groupList_search, true);
        setVisibleItem(menu, R.id.groupList_cancle, false);
        isSearchFragVisible = false;

        ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
        getSupportFragmentManager().beginTransaction().replace(R.id.ctGroupCloudList, fragGroupCloudList).commit();
    }

    public void favColorEvent()
    {
        if(isFav)
            menuNav.findItem(R.id.nav_gc_fav).getIcon().setColorFilter(Color.argb(255, 234, 204, 26), PorterDuff.Mode.SRC_IN);
        else
            menuNav.findItem(R.id.nav_gc_fav).getIcon().setColorFilter(Color.argb(255, 128, 128, 128), PorterDuff.Mode.SRC_IN);
    }

    public void fileClickEvent(final GroupFileItem item)
    {
        ArrayList<CharSequence> listItems = new ArrayList<CharSequence>();

        listItems.add(getText(R.string.text_download));

        if (pmLib.isUserFileModifyName())
            listItems.add(getText(R.string.text_rename));

        if (pmLib.isUserFileDelete())
            listItems.add(getText(R.string.text_delete));

        listItems.add(getText(R.string.text_cancel));

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getText(R.string.text_chooseyouraction))
                .setItems(items, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        // Download Event
                        if (items[index].equals(getText(R.string.text_download).toString()))
                        {
                            if (isExternalStorageWritable())
                            {
                                File defaultDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                ftpMain.downloadFile(item.getFile().getName(), "", defaultDownload.getPath());
                            }
                        }
                        // Rename Event
                        else if (items[index].equals(getText(R.string.text_rename).toString()))
                        {
                            fileRenameEvent(item, ftpMain);
                        }
                        // Delete Event
                        else if (items[index].equals(getText(R.string.text_delete).toString()))
                        {
                            AlertDialog.Builder alertDelete = new AlertDialog.Builder(GroupCloudList.this);

                            alertDelete.setTitle(getText(R.string.text_delete).toString())
                                    .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {

                                            ftpMain.delete(item.getFile());

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

                            alertDelete.setMessage(getString(R.string.text_areyousuredelete_one, item.getFile().getName()));

                            alertDelete.show();
                        }
                    }
                })

                .show();
    }

    public void fileRenameEvent(final GroupFileItem item, final FTPLib ftpMain)
    {
        final EditText name = new EditText(this);

        name.setText(item.getFile().getName());

        // Create Rename AlertDialog
        AlertDialog.Builder alertRename = new AlertDialog.Builder(this);

        alertRename.setMessage(getText(R.string.text_rename).toString())
                .setView(name)
                .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String fileName = name.getText().toString();

                        if (!item.getFile().getName().equals(fileName))
                        {
                            if (!fileName.equals(""))
                            {
                                ftpMain.execute(FTPCMD.Rename, item.getFile().getName(), fileName);
                                ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
                                updateUploadTime();

                                if (isSearchFragVisible)
                                    replacedSearchFragmentEvent();
                            }
                            else
                            {
                                Toast.makeText(GroupCloudList.this, getText(R.string.text_file).toString() + getText(R.string.text_nameisempty).toString(), Toast.LENGTH_SHORT).show();
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

        alertRename.show();
    }

    /*
     *  파일 업로드 Activity로 넘어가는 Method.
     */
    public void groupCloudUploadEvent()
    {
        Intent intent = new Intent(GroupCloudList.this, GroupCloudUpload.class);
        intent.putExtra(getText(R.string.TAG_SERVERIP).toString(), serverIP);
        intent.putExtra(getText(R.string.TAG_DEFAULTFTPPATH).toString(), defaultFTPPath);
        intent.putExtra(getText(R.string.TAG_WORKINGPATH).toString(), ftpMain.getWorkingPath());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
                updateUploadTime();
            }
        }
        else if(requestCode == 2)
        {
            if(resultCode == RESULT_OK)
            {
                GroupCloudTask task = new GroupCloudTask(true, 0);
                task.execute(String.valueOf(groupID), userNum);
            }
        }
    }

    void updateUploadTime()
    {
        long ts = System.currentTimeMillis();
        Date localTime = new Date(ts);
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        GroupCloudTask gcTask = new GroupCloudTask(true, false);
        gcTask.execute(String.valueOf(groupID), sdf.format(localTime));
    }

    void modifyLayoutEvent(boolean visible, GroupFileItem item)
    {
        if (visible)
        {

            if (ftpMain.getExt(item.getFile()).equals("folder"))
            {
                modifyLayoutType = 1;

                //권한 체크
                if (pmLib.isUserFolderDelete())
                    ivGroupCloudDelete.setVisibility(View.VISIBLE);
                else
                    ivGroupCloudDelete.setVisibility(View.GONE);

                if (pmLib.isUserFolderModify())
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
                if (pmLib.isUserFileDelete())
                    ivGroupCloudDelete.setVisibility(View.VISIBLE);
                else
                    ivGroupCloudDelete.setVisibility(View.GONE);

                if (pmLib.isUserFileMove())
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
            ArrayList<GroupFileItem> gcsTemp = fragGroupCloudSearch.getAdapter().getList();

            for (GroupFileItem temp : gciTemp)
            {
                if (temp.getChecked())
                    temp.setChecked(false);
            }

            for (GroupFileItem temp : gcsTemp)
            {
                if (temp.getChecked())
                    temp.setChecked(false);
            }

            fragGroupCloudList.getAdapter().notifyDataSetChanged();
            fragGroupCloudSearch.getAdapter().notifyDataSetChanged();
        }
    }

    private void setVisibleItem(Menu menu, int id, boolean state)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(state);
    }

    private String jsonEvent(String jsonString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray;

            jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPCLOUD).toString());

            JSONObject item = jsonArray.getJSONObject(0);

            return item.getString(getText(R.string.TAG_SERVERIP).toString());
        }
        catch (JSONException e)
        {
            Log.d(TAG, "GroupCloudList : ", e);
            return "";
        }
    }

    int getModifyLayoutType()
    {
        return modifyLayoutType;
    }

    PermissionLib getPmLib()
    {
        return pmLib;
    }

    FTPLib getFTPMain()
    {
        return ftpMain;
    }

    boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();

        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    class GroupCloudTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;
        boolean isUpdateDate = false;
        boolean isFavUpdate = false;
        boolean isLeave = false;
        boolean isCreator = false;
        boolean isGetter = false;

        GroupCloudTask()
        {
            this.isGetter = true;
        }

        GroupCloudTask(boolean isLeave, int creatorCheck)
        {
            this.isLeave = isLeave;
            this.isCreator = (creatorCheck == 1);
        }

        GroupCloudTask(boolean isUpdateDate, boolean isFavUpdate)
        {
            this.isUpdateDate = isUpdateDate;
            this.isFavUpdate = isFavUpdate;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            if(isFavUpdate || isUpdateDate || isLeave)
                progressDialog = ProgressDialog.show(GroupCloudList.this, getText(R.string.text_loading).toString(), null, true, true);
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

            if (result == null)
            {
                Toast.makeText(getApplicationContext(), getText(R.string.error_temporary).toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(isGetter)
                {
                    int count = getMemberCount(result);

                    if(count < 0)
                        Toast.makeText(getApplicationContext(), getText(R.string.error_temporary).toString(), Toast.LENGTH_SHORT).show();
                    else
                    {
                        if(count > 1)
                            chooseActionToCreator();
                        else
                            leaveGroupEvent();
                    }
                }
                else
                {
                    if (isLeave)
                    {
                        if(isCreator)
                        {
                            ftpMain.execute(FTPCMD.RemoveDir, ftpMain.getDefaultPath());
                        }
                        finish();
                    }
                    else
                    {
                        if (!isUpdateDate && !isFavUpdate)
                        {
                            serverIP = jsonEvent(result);
                            ftpMain = new FTPLib(serverIP, defaultFTPPath, GroupCloudList.this);

                            ftpMain.execute(GroupCloudList.this, "loadFileList", FTPCMD.GetFileList);
                        }
                    }
                }
            }

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String serverURL;
            String postParameters;

            if(isGetter)
            {
                serverURL = "http://weloud.duckdns.org/weloud/db_get_group_member_count.php";
                postParameters = "groupid=" + params[0];
            }
            else
            {
                if (isLeave)
                {
                    if (isCreator)
                    {
                        serverURL = "http://weloud.duckdns.org/weloud/db_delete_group.php";
                        postParameters = "groupid=" + params[0];
                    }
                    else
                    {
                        serverURL = "http://weloud.duckdns.org/weloud/db_delete_group_user.php";
                        postParameters = "groupid=" + params[0] + "&userNum=" + params[1];
                    }
                }
                else
                {
                    if (isUpdateDate)
                    {
                        serverURL = "http://weloud.duckdns.org/weloud/db_update_groupupload.php";
                        postParameters = "groupid=" + params[0] + "&uploadTime=" + params[1];
                    }
                    else
                    {
                        if (isFavUpdate)
                        {
                            serverURL = "http://weloud.duckdns.org/weloud/db_update_userapproved.php";
                            postParameters = "groupid=" + params[0] + "&usernum=" + params[1] + "&userApproved=" + params[2];
                        }
                        else
                        {
                            serverURL = "http://weloud.duckdns.org/weloud/db_get_groupcloud_ip.php";
                            postParameters = "GroupID=" + params[0];
                        }
                    }
                }
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
                if (responseStatusCode == HttpURLConnection.HTTP_OK)
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

                while ((line = bufferedReader.readLine()) != null)
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

    /*
     *  아래의 부분은 Invoke (Reflect)를 통해서 사용하는 메소드들 입니다.
     *  never used가 뜬다고 해서 지우지 마시오.
     */

    void changeTitleText()
    {
        String finalDir = new StringLib().lastDir(ftpMain.getWorkingPath());
        setTitle(finalDir);
    }

    void loadFileList()
    {
        fragGroupCloudList.loadFileList();
    }

    void searchEvent()
    {
        fragGroupCloudSearch.searchEvent(ftpMain.getFtpSearch());
    }

    void permissionEvent()
    {
        /*
         * Group Setting 접근 권한 확인 후 Visible
         */
        MenuItem item = menuNav.findItem(R.id.nav_gc_groupsett);

        if (pmLib.isUserManageGroup() || pmLib.isUserApproveJoin() || pmLib.isUserInvite() || pmLib.isUserCreator())
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
}
