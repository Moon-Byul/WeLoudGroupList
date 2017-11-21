package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.security.acl.Group;
import java.util.ArrayList;

import static android.R.attr.path;

/**
 * Created by reveu on 2017-11-12.
 */

public class GroupCloudUpload extends AppCompatActivity
{
    private ActionbarLib abLib = new ActionbarLib();
    private GroupCloudUploadAdapter gcuAdapter = new GroupCloudUploadAdapter();

    private String serverIP = "";
    private String defaultFTPPath = "";
    private String workingPath = "";
    private final String defaultPath = "/sdcard";
    private File currentFile;
    private FTPLib ftpMain;

    private int count;

    private ListView lvMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupcloud_upload);

        Intent intent = getIntent();
        defaultFTPPath = intent.getStringExtra(getText(R.string.TAG_DEFAULTFTPPATH).toString());
        serverIP = intent.getStringExtra(getText(R.string.TAG_SERVERIP).toString());
        workingPath = intent.getStringExtra(getText(R.string.TAG_WORKINGPATH).toString());
        ftpMain = new FTPLib(serverIP, defaultFTPPath, GroupCloudUpload.this);

        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(gcuAdapter);
        abLib.setDefaultActionBar(this, "", true, 1);

        currentFile = new File(defaultPath);
        loadDir(currentFile);

        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupUploadItem item = gcuAdapter.getItem(position);

                if(item.getFile().isDirectory())
                {
                    loadDir(new File(item.getFile().getPath() + "/"));
                }
                else
                {
                    fileCheckEvent(item);
                }
            }
        });

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
                ArrayList<GroupUploadItem> items = gcuAdapter.getCheckList();
                String path = "";
                count = -1;

                uploadLoopEvent();
            }
        });

        abLib.setEnableConfirmBtn(false);
    }

    void fileCheckEvent(GroupUploadItem item)
    {
        if(item.getChecked())
        {
            item.setChecked(false);
            gcuAdapter.removeCheckItem(item);
        }
        else
        {
            item.setChecked(true);
            gcuAdapter.addCheckItem(item);
        }

        if(gcuAdapter.getCheckCount() > 0)
        {
            abLib.setEnableConfirmBtn(true);
        }
        else
        {
            abLib.setEnableConfirmBtn(false);
        }

        gcuAdapter.notifyDataSetChanged();
    }

    void loadDir(File currentDir)
    {
        gcuAdapter.clearList();

        abLib.setTvActionBarTitle(new StringLib().lastDir(currentDir.getPath()));

        if(!currentDir.getPath().equals(defaultPath))
        {
            gcuAdapter.addParentItem(currentDir);
        }

        try
        {
            File[] dirs = currentDir.listFiles();

            for(File f : dirs)
            {
                gcuAdapter.addItem(f);
            }

            gcuAdapter.sortAscName();

            gcuAdapter.notifyDataSetChanged();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /*
     *  아래의 부분은 Invoke (Reflect)를 통해서 사용하는 메소드들 입니다.
     *  never used가 뜬다고 해서 지우지 마시오.
     */

    /*
     * 업로드를 할려면 해당 FTP 파일이 있는지 확인해서 Rename이나 OverWrite 여부를 물어보는 이벤트가 필요한데,
     * Task로 해버리면 쓰레드 방식이기때문에 그전의 Rn/Ow 이벤트가 끝나지 않았어도 실행이 되어서
     * Invoke를 통해 순차적으로 처리하게 만들었다.
     */
    void uploadLoopEvent()
    {
        String path = "";
        count++;

        if(gcuAdapter.getCheckCount() > count)
        {
            path = gcuAdapter.getCheckList().get(count).getFile().getPath();
            ftpMain.execute(GroupCloudUpload.this, "uploadLoopEvent", FTPCMD.UploadFile, path, workingPath, new StringLib().lastDir(path).replace(" ", "_"));
        }
        else
        {
            Intent intent = getIntent();
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
