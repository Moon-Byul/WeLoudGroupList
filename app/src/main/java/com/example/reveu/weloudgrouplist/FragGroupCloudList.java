package com.example.reveu.weloudgrouplist;

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
                    ftpMain.changeWorkingDirectory("..");
                    loadFileList();
                }
                else
                {
                    if (ftpMain.getExt(item.getFile()).equals("folder"))
                    {
                        ftpMain.changeWorkingDirectory(item.getFile().getName());
                        loadFileList();
                    }
                    else
                    {
                        if(((GroupCloudList) getActivity()).getModifyLayoutType() == 0)
                            fileCheckEvent(item);
                        else
                        {
                            fileClickEvent(item, ftpMain);
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
                    ftpMain.changeWorkingDirectory("..");
                    loadFileList();
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
                loadFileList();
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
                            ((GroupCloudList) getActivity()).getFTPMain().makeDirectory(folderName);
                            fabClickEvent();
                            loadFileList();
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

    private void fileClickEvent(final GroupFileItem item, final FTPLib ftpMain)
    {
        int permission = ((GroupCloudList) getActivity()).getUserPermission();
        PermissionLib pmLib = new PermissionLib();
        ArrayList<CharSequence> listItems = new ArrayList<CharSequence>();

        //listItems.add(getText(R.string.text_open));
        listItems.add(getText(R.string.text_download));

        if(pmLib.isUserFileModifyName(permission))
            listItems.add(getText(R.string.text_rename));

        listItems.add(getText(R.string.text_cancel));

        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getText(R.string.text_chooseyouraction))
        .setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                // Download Event
                if(items[index].equals(getText(R.string.text_download).toString()))
                {
                    if(isExternalStorageWritable())
                    {
                        File defaultDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        ftpMain.downloadFile(false, item.getFile().getName(), "", defaultDownload.getPath());
                    }
                }
                // Rename Event
                else if(items[index].equals(getText(R.string.text_rename).toString()))
                {
                    fileRenameEvent(item, ftpMain);
                }
                // Open Event
                else if(items[index].equals(getText(R.string.text_open).toString()))
                {

                }
            }
        })

        .show();
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

    public void fileRenameEvent(final GroupFileItem item, final FTPLib ftpMain)
    {
        final EditText name = new EditText(getActivity());

        name.setText(item.getFile().getName());

        // Create Rename AlertDialog
        AlertDialog.Builder alertRename = new AlertDialog.Builder(getActivity());

        alertRename.setMessage(getText(R.string.text_rename).toString())
                .setView(name)
                .setPositiveButton(getText(R.string.text_ok).toString(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String fileName = name.getText().toString();

                        if(!item.getFile().getName().equals(fileName))
                        {
                            if(!fileName.equals(""))
                            {
                                if(ftpMain.rename(item.getFile().getName(), fileName))
                                    loadFileList();
                                else
                                    Toast.makeText(getActivity(), getText(R.string.text_nameisexists), Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(getActivity(), getText(R.string.text_file).toString() + getText(R.string.text_nameisempty).toString(), Toast.LENGTH_SHORT).show();
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

    private void getFileList()
    {
        ((GroupCloudList) getActivity()).getFTPMain().getFileList();
    }

    public boolean loadFileList()
    {
        final FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();
        FTPFile[] ftpfiles = null;

        AsyncTask.execute(new Runnable()
        {
            @Override
            public void run()
            {
                FTPFile[] ftpFilesTemp = null;
                try
                {
                    while(ftpFilesTemp == null)
                    {
                        ftpFilesTemp = ftpMain.getFileList();
                        this.wait(100);
                    }
                }
                catch(InterruptedException ie)
                {
                    ie.printStackTrace();
                }
            }
        });

        gcaAdapter.clearList();

        SimpleDateFormat ftpFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        if(ftpfiles != null)
        {
            if(!ftpMain.isDirectoryDefault(ftpMain.getDirectory()))
            {
                FTPFile file = new FTPFile();
                file.setName("");
                gcaAdapter.addItem(file);
            }
            try
            {
                for (int i = 0; i < ftpfiles.length; i++)
                {
                    Calendar cal = Calendar.getInstance();
                    FTPFile file = ftpfiles[i];
                    cal.setTime(ftpFormat.parse(ftpMain.getFileModificationTime(file.getName())));

                    gcaAdapter.addItem(file, cal, ftpMain.getDirectory());
                }

                gcaAdapter.sortAscName();
                return true;
            }
            catch(ParseException pe)
            {
                pe.printStackTrace();
                return false;
            }
        }
        else
        {
            Log.d("Twily", "실패함.");
            return false;
        }
    }

    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state))
        {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable()
    {
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            return true;
        }
        return false;
    }
}
