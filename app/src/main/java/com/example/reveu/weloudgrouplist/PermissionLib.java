package com.example.reveu.weloudgrouplist;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.R.attr.type;
import static android.content.ContentValues.TAG;

/**
 * Created by reveu on 2017-10-23.
 */
class PermissionLib
{
    /*
        User_Manage_Group 권한이 있어도 Permission Setting은 하지 못한다. (오직 개설자만, USER_CREATOR))
        Rank Setting을 할 때 자신과 같은 Manage_Group을 가진 관리자 Rank로는 올릴 수 없으며,
        다른 관리자의 Rank를 수정하지도 못한다.

        또한 Manage_Group이 존재할 경우에면 User Ban이 가능하다. (Ban 또한 Manage_Group을 가진 관리자는 Ban을 할 수 없다.)
     */
    static final int USER_UPLOAD = 1;
    static final int USER_FILE_MODIFY_NAME = 1 << 1;
    static final int USER_FILE_MOVE = 1 << 2;
    static final int USER_FILE_DELETE = 1 << 3;
    static final int USER_FOLDER_ADD = 1 << 4;
    static final int USER_FOLDER_MODIFY = 1 << 5;
    static final int USER_FOLDER_DELETE = 1 << 6;
    static final int USER_APPROVE_JOIN = 1 << 7;
    static final int USER_INVITE = 1 << 8;
    static final int USER_MANAGE_GROUP = 1 << 9;
    static final int USER_CREATOR = 1 << 10;

    private Context context;
    private int permission;

    PermissionLib(Context context)
    {
        this.context = context;
    }

    void execute(String groupID, String userNum)
    {
        new PermissionTask().execute(groupID, userNum);
    }

    void execute(Activity activity, String methodName, String groupID, String userNum)
    {
        new PermissionTask(activity, methodName).execute(groupID, userNum);
    }

    void execute(android.support.v4.app.Fragment fragment, String methodName, String groupID, String userNum)
    {
        new PermissionTask(fragment, methodName).execute(groupID, userNum);
    }

    boolean isUserUpload()
    {
        return (permission & USER_UPLOAD) > 0;
    }

    boolean isUserFileModifyName()
    {
        return (permission & USER_FILE_MODIFY_NAME) > 0;
    }
    boolean isUserFileMove()
    {
        return (permission & USER_FILE_MOVE) > 0;
    }

    boolean isUserFileDelete()
    {
        return (permission & USER_FILE_DELETE) > 0;
    }

    boolean isUserFolderAdd()
    {
        return (permission & USER_FOLDER_ADD) > 0;
    }

    boolean isUserFolderModify()
    {
        return (permission & USER_FOLDER_MODIFY) > 0;
    }

    boolean isUserFolderDelete()
    {
        return (permission & USER_FOLDER_DELETE) > 0;
    }

    boolean isUserApproveJoin()
    {
        return (permission & USER_APPROVE_JOIN) > 0;
    }

    boolean isUserInvite()
    {
        return (permission & USER_INVITE) > 0;
    }

    boolean isUserManageGroup()
    {
        return (permission & USER_MANAGE_GROUP) > 0;
    }

    boolean isUserCreator()
    {
        return (permission & USER_CREATOR) > 0;
    }

    public int getPermission()
    {
        return permission;
    }

    private class PermissionTask extends AsyncTask<String, Integer, String>
    {
        private Activity activity = null;
        private android.support.v4.app.Fragment fragment = null;
        private String methodCall = "";

        ProgressDialog progressDialog;

        PermissionTask()
        {
        }

        PermissionTask(android.support.v4.app.Fragment fragmentInput, String methodName)
        {
            fragment = fragmentInput;
            methodCall = methodName;
        }

        PermissionTask(Activity activityInput, String methodName)
        {
            activity = activityInput;
            methodCall = methodName;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(context, context.getText(R.string.text_loading).toString(), null, true, true);
        }


        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if (progressDialog != null)
            {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            try
            {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray;

                jsonArray = jsonObject.getJSONArray(context.getText(R.string.TAG_PERMISSION).toString());

                JSONObject item = jsonArray.getJSONObject(0);

                permission = item.getInt(context.getText(R.string.TAG_PERMISSION).toString());

                if (activity != null || fragment != null)
                {
                    Method method;
                    if (activity != null)
                    {
                        method = activity.getClass().getMethod(methodCall);
                        method.invoke(activity);
                    }
                    else if (fragment != null)
                    {
                        method = fragment.getClass().getMethod(methodCall);
                        method.invoke(fragment);
                    }
                }
            }
            catch (Exception e)
            {
                Log.d(TAG, "PermissionLib : ", e);
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            String serverURL = "http://weloud.duckdns.org/weloud/db_get_userpermission.php";
            String postParameters = "GroupID=" + params[0] + "&userNum=" + params[1];

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
                Log.d(TAG, "PermissionLib: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}
