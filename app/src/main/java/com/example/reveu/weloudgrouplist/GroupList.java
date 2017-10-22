package com.example.reveu.weloudgrouplist;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.example.reveu.weloudgrouplist.R.id.fab;
import static com.example.reveu.weloudgrouplist.R.id.toolbar;

public class GroupList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FragGroupList fragGroupList;
    FragGroupSearch fragGroupSearch;

    private Menu menu;
    private String userNum;
    private String ID;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        Intent intent = getIntent();
        userNum = intent.getStringExtra(getText(R.string.TAG_USERNUM).toString());
        ID = intent.getStringExtra(getText(R.string.TAG_ID).toString());
        nickName = intent.getStringExtra(getText(R.string.TAG_NICKNAME).toString());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragGroupList = (FragGroupList) getSupportFragmentManager().findFragmentById(R.id.fragGroupList);
        fragGroupSearch = new FragGroupSearch();

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
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        fragGroupList.getGroupList();
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
        }
        else if (id == R.id.groupList_cancle)
        {
            setVisibleItem(menu, R.id.groupList_sortNameAsc, true);
            setVisibleItem(menu, R.id.groupList_sortNameDesc, true);
            setVisibleItem(menu, R.id.groupList_sortModAsc, true);
            setVisibleItem(menu, R.id.groupList_sortModDesc, true);
            setVisibleItem(menu, R.id.groupList_search, true);
            setVisibleItem(menu, R.id.groupList_cancle, false);

            getSupportFragmentManager().beginTransaction().replace(R.id.ctGroupList, fragGroupList).commit();
        }
        else if (id == R.id.groupList_sortNameAsc)
        {
            fragGroupList.getAdapter().sortAscName();
        }
        else if (id == R.id.groupList_sortNameDesc)
        {
            fragGroupList.getAdapter().sortDescName();
        }
        else if (id == R.id.groupList_sortModAsc)
        {
            fragGroupList.getAdapter().sortAscDate();
        }
        else if (id == R.id.groupList_sortModDesc)
        {
            fragGroupList.getAdapter().sortDescDate();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_gl_settings)
        {
            Intent intent = new Intent(GroupList.this, SettUserMain.class);
            intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
            startActivity(intent);
        }
        else if (id == R.id.nav_gl_logout)
        {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void setVisibleItem(Menu menu, int id, boolean state)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(state);
    }

    public String getUserNum()
    {
        return userNum;
    }

    public String getUserId()
    {
        return ID;
    }

    public void groupSearchEvent()
    {
        Intent intent = new Intent(GroupList.this, GroupSearch.class);
        intent.putExtra(getText(R.string.TAG_USERNUM).toString(), userNum);
        startActivity(intent);
    }

    public void groupCreateEvent()
    {
        Intent intent = new Intent(GroupList.this, GroupCreate.class);
        intent.putExtra(getText(R.string.TAG_USERNUM).toString(), userNum);
        startActivity(intent);
    }

    public void groupCloudEvent(String groupName, int groupID)
    {
        Intent intent = new Intent(GroupList.this, GroupCloudList.class);
        intent.putExtra(getText(R.string.TAG_USERNUM).toString(), userNum);
        intent.putExtra(getText(R.string.TAG_NICKNAME).toString(), nickName);
        intent.putExtra(getText(R.string.TAG_ID).toString(), ID);
        intent.putExtra(getText(R.string.TAG_GROUPNAME).toString(), groupName);
        intent.putExtra(getText(R.string.TAG_GROUPID).toString(), groupID);
        startActivity(intent);
    }
}
