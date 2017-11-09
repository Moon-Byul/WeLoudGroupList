package com.example.reveu.weloudgrouplist;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.RunnableFuture;

/**
 * Created by reveu on 2017-06-04.
 */

public class FragGroupCloudList extends Fragment
{
    private boolean isFabClicked = false;
    private ListView lvCloudGroup;
    private GroupCloudAdapter gcaAdapter = new GroupCloudAdapter();
    private SwipeRefreshLayout srlGroupCloudMain;
    private FloatingActionButton fab;
    private RelativeLayout layout_fabMain;
    private LinearLayout layout_fabCreateFolder;
    private LinearLayout layout_fabUpload;

    private boolean isFabCreateFolderVisible;
    private boolean isFabUploadVisible;

    private FTPFile[] ftpfiles = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_groupcloud_list, container, false);

        fab = (FloatingActionButton) rootView.findViewById(R.id.fabCloud);
        srlGroupCloudMain = (SwipeRefreshLayout) rootView.findViewById(R.id.srlGroupCloudMain);
        lvCloudGroup = (ListView) rootView.findViewById(R.id.lvCloudGroup);
        FloatingActionButton fabCreateFolder = (FloatingActionButton) rootView.findViewById(R.id.fabCloudCreateFolder);
        FloatingActionButton fabUpload = (FloatingActionButton) rootView.findViewById(R.id.fabCloudUpload);
        layout_fabMain = (RelativeLayout) rootView.findViewById(R.id.layout_fabCloudMain);
        layout_fabCreateFolder = (LinearLayout) rootView.findViewById(R.id.layout_fabCloudCreateFolder);
        layout_fabUpload = (LinearLayout) rootView.findViewById(R.id.layout_fabCloudUpload);

        lvCloudGroup.setAdapter(gcaAdapter);

        lvCloudGroup.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupFileItem item = (GroupFileItem) gcaAdapter.getItem(position);
                FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();

                if(item.getFile().getName().equals(""))
                {
                    ((GroupCloudList) getActivity()).cwdEvent("..");
                }
                else
                {
                    if (ftpMain.getExt(item.getFile()).equals("folder"))
                    {
                        ((GroupCloudList) getActivity()).cwdEvent(ftpMain.getWorkingPath() + "/" + item.getFile().getName());
                    }
                    else
                    {
                        if(((GroupCloudList) getActivity()).getModifyLayoutType() == 0)
                            fileCheckEvent(item);
                        else
                        {
                            fileClickEvent(item);
                        }
                    }
                }
            }
        });

        lvCloudGroup.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupCloudList gclTemp = ((GroupCloudList) getActivity());

                final GroupFileItem item = (GroupFileItem) gcaAdapter.getItem(position);
                final FTPLib ftpMain = gclTemp.getFTPMain();

                if(item.getFile().getName().equals(""))
                {
                    ((GroupCloudList) getActivity()).cwdEvent("..");
                }
                else
                {

                    if(gclTemp.getModifyLayoutType() < 0)
                    {
                        gclTemp.modifyLayoutEvent(true, item);
                        fileCheckEvent(item);
                    }
                    else
                    {
                        if (ftpMain.getExt(item.getFile()).equals("folder"))
                        {
                            if(gclTemp.getModifyLayoutType() == 1)
                            {
                                fileCheckEvent(item);
                            }

                            // Rename은 1개만..
                            if(gcaAdapter.getCheckCount() > 1)
                                gclTemp.ivGroupCloudDownloadOrRename.setVisibility(View.GONE);
                            else
                                gclTemp.ivGroupCloudDownloadOrRename.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            if(gclTemp.getModifyLayoutType() == 0)
                                fileCheckEvent(item);
                        }
                    }
                }
                return true;
            }
        });

        srlGroupCloudMain.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                ((GroupCloudList) getActivity()).getFTPMain().execute(getActivity(), "loadFileList", FTPCMD.GetFileList);
                srlGroupCloudMain.setRefreshing(false);
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

        fabCreateFolder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                final EditText name = new EditText(getActivity());
                name.setTextSize(18.0f);

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle(getText(R.string.text_createfolder).toString())
                .setMessage(getText(R.string.text_inputfoldername).toString())
                .setView(name)
                .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String folderName = name.getText().toString();

                        if(!folderName.equals(""))
                        {
                            FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();

                            ftpMain.execute(FTPCMD.MakeDirectory, folderName);
                            fabClickEvent();
                            ftpMain.execute(getActivity(), "loadFileList", FTPCMD.GetFileList);
                        }
                        else
                        {
                            Toast.makeText(getActivity(), getText(R.string.text_folder).toString() + getText(R.string.text_nameisempty).toString(), Toast.LENGTH_SHORT).show();
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

                alert.show();
            }
        });

        return rootView;
    }

    private void fileClickEvent(GroupFileItem item)
    {
        ((GroupCloudList) getActivity()).fileClickEvent(item);
        gcaAdapter.notifyDataSetChanged();
    }

    private void fileCheckEvent(GroupFileItem item)
    {
        if(item.getChecked())
        {
            item.setChecked(false);
            gcaAdapter.removeCheckItem(item);
        }
        else
        {
            item.setChecked(true);
            gcaAdapter.addCheckItem(item);
        }

        int amount = gcaAdapter.getCheckCount();

        if(amount > 1)
            ((GroupCloudList) getActivity()).tvGroupCloudAmount.setText(getString(R.string.text_selectitem_multi, amount));
        else if(amount == 1)
            ((GroupCloudList) getActivity()).tvGroupCloudAmount.setText(getString(R.string.text_selectitem_one, amount));
        else
            ((GroupCloudList) getActivity()).modifyLayoutEvent(false, item);

        gcaAdapter.notifyDataSetChanged();
    }

    public void fabClickEvent()
    {
        if(isFabClicked)
        {
            layout_fabMain.setBackgroundColor(Color.argb(0, 255, 255, 255));
            layout_fabCreateFolder.setVisibility(View.INVISIBLE);
            layout_fabUpload.setVisibility(View.INVISIBLE);
            fab.setImageResource(R.drawable.addgroup);
            srlGroupCloudMain.setEnabled(true);
            lvCloudGroup.setEnabled(true);
        }
        else
        {
            layout_fabMain.setBackgroundColor(Color.argb(128, 0, 0, 0));

            if(isFabCreateFolderVisible)
                layout_fabCreateFolder.setVisibility(View.VISIBLE);
            else
                layout_fabCreateFolder.setVisibility(View.GONE);

            if(isFabUploadVisible)
                layout_fabUpload.setVisibility(View.VISIBLE);
            else
                layout_fabUpload.setVisibility(View.GONE);

            fab.setImageResource(R.drawable.canclegroup);
            srlGroupCloudMain.setEnabled(false);
            lvCloudGroup.setEnabled(false);
        }
        isFabClicked = !isFabClicked;
    }

    public void permissionEvent()
    {
        boolean isVisible = false;
        int permission = ((GroupCloudList) getActivity()).getUserPermission();
        PermissionLib pmLib = new PermissionLib();

        if(pmLib.isUserFolderAdd(permission))
        {
            isVisible = true;
            isFabCreateFolderVisible = true;
        }
        else
            isFabCreateFolderVisible = false;

        if(pmLib.isUserUpload(permission))
        {
            isVisible = true;
            isFabUploadVisible = true;
        }
        else
            isFabUploadVisible = false;

        if(!isVisible)
            fab.setVisibility(View.GONE);
        else
            fab.setVisibility(View.VISIBLE);
    }

    public void setFabClicked(boolean input)
    {
        isFabClicked = input;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    public ArrayList<GroupFileItem> getList()
    {
        return gcaAdapter.getList();
    }

    public GroupCloudAdapter getAdapter()
    {
        return gcaAdapter;
    }

    public ListView getListView()
    {
        return lvCloudGroup;
    }

    public FloatingActionButton getFab()
    {
        return fab;
    }

    public boolean loadFileList()
    {
        final FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();
        ftpfiles = ftpMain.getFTPFile();

        gcaAdapter.clearList();

        SimpleDateFormat ftpFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        if(ftpfiles != null)
        {
            if(!ftpMain.isDirectoryDefault(ftpMain.getWorkingPath()))
            {
                FTPFile file = new FTPFile();
                file.setName("");
                gcaAdapter.addItem(file);
            }
            for (int i = 0; i < ftpfiles.length; i++)
            {
                FTPFile file = ftpfiles[i];
                gcaAdapter.addItem(file, file.getTimestamp(), ftpMain.getWorkingPath());
            }

            gcaAdapter.sortAscName();
            return true;
        }
        else
        {
            Log.d("Twily", "실패함.");
            return false;
        }
    }
}
