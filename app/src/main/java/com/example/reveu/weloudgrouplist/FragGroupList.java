package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static android.R.id.input;
import static android.content.ContentValues.TAG;
import static com.example.reveu.weloudgrouplist.R.id.textinfo;

/**
 * Created by reveu on 2017-06-04.
 */

public class FragGroupList extends Fragment
{
    private boolean isFabClicked = false;
    private ListView lvGroup;
    private GroupListAdapter glaAdapter = new GroupListAdapter();
    private SwipeRefreshLayout srlGroupMain;
    private RelativeLayout layout_fabMain;
    private LinearLayout layout_fabSearch;
    private LinearLayout layout_fabCreate;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_grouplist_main, container, false);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        FloatingActionButton fabSearch = (FloatingActionButton) rootView.findViewById(R.id.fabSearch);
        FloatingActionButton fabCreate = (FloatingActionButton) rootView.findViewById(R.id.fabCreate);
        layout_fabSearch = (LinearLayout) rootView.findViewById(R.id.layout_fabSearch);
        layout_fabCreate = (LinearLayout) rootView.findViewById(R.id.layout_fabCreate);
        srlGroupMain = (SwipeRefreshLayout) rootView.findViewById(R.id.srlGroupMain);
        layout_fabMain = (RelativeLayout) rootView.findViewById(R.id.layout_fabMain);
        lvGroup = (ListView) rootView.findViewById(R.id.lvGroup);

        lvGroup.setAdapter(glaAdapter);

        lvGroup.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupListItem item = glaAdapter.getItem(position);

                ((GroupList) getActivity()).groupCloudEvent(item.getGroupName(), item.getGroupID());
            }
        });

        srlGroupMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                lvGroup.setEnabled(false);
                getGroupList();
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                fabClickEvent();
            }
        });

        fabSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((GroupList) getActivity()).groupSearchEvent();
                fabClickEvent();
            }
        });

        fabCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((GroupList) getActivity()).groupCreateEvent();
                fabClickEvent();
            }
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("Twily", "List Start");
    }

    public void fabClickEvent()
    {
        if(isFabClicked == true)
        {
            layout_fabMain.setBackgroundColor(Color.argb(0, 255, 255, 255));
            layout_fabSearch.setVisibility(View.INVISIBLE);
            layout_fabCreate.setVisibility(View.INVISIBLE);
            fab.setImageResource(R.drawable.addgroup);
            srlGroupMain.setEnabled(true);
            lvGroup.setEnabled(true);
        }
        else
        {
            layout_fabMain.setBackgroundColor(Color.argb(128, 0, 0, 0));
            layout_fabSearch.setVisibility(View.VISIBLE);
            layout_fabCreate.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.canclegroup);
            srlGroupMain.setEnabled(false);
            lvGroup.setEnabled(false);
        }
        isFabClicked = !isFabClicked;
    }

    public void setFabClicked(boolean input)
    {
        isFabClicked = input;
    }

    public ArrayList<GroupListItem> getList()
    {
        return glaAdapter.getList();
    }

    public GroupListAdapter getAdapter()
    {
        return glaAdapter;
    }

    public ListView getListView()
    {
        return lvGroup;
    }

    public void getGroupList()
    {
        GroupListTask glData = new GroupListTask();
        glData.execute(((GroupList) getActivity()).getUserNum());
    }

    public void groupListEvent(String jsonString)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(getText(R.string.TAG_JSON_GROUPLIST).toString());

            glaAdapter.clearList();

            int length = jsonArray.length();
            if(length > 0)
            {
                try
                {
                    int index = 0;
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                    while (length > index) {
                        JSONObject item = jsonArray.getJSONObject(index);

                        int groupID = item.getInt(getText(R.string.TAG_GROUPID).toString());
                        String groupName = item.getString(getText(R.string.TAG_GROUPNAME).toString());
                        String recentUpload = item.getString(getText(R.string.TAG_GROUPUPLOAD).toString());
                        int approved = item.getInt(getText(R.string.TAG_USERAPPROVED).toString());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(recentUpload));

                        glaAdapter.addItem(groupID, (approved == 0 ? false : true), groupName, cal);
                        index++;
                    }
                    glaAdapter.notifyDataSetChanged();
                }
                catch(ParseException pe)
                {
                    Log.d("Twily", "groupListEvent : ", pe);
                }
            }
            if(srlGroupMain.isRefreshing() == true)
            {
                srlGroupMain.setRefreshing(false);
                lvGroup.setEnabled(true);
            }
        }
        catch (JSONException e)
        {
            Log.d(TAG, "groupListEvent : ", e);
        }
    }

    private class GroupListTask extends AsyncTask<String, Void, String>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            groupListEvent(result);

            Log.d(TAG, "response  - " + result);
        }


        @Override
        protected String doInBackground(String... params)
        {
            String userNum = params[0];

            String serverURL = "http://weloud.duckdns.org/weloud/db_get_grouplist.php";
            String postParameters = "userNum=" + userNum;
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
                Log.d(TAG, "GroupListData: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
