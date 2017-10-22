package com.example.reveu.weloudgrouplist;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.name;

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
                    Snackbar.make(view, ftpMain.getExt(item.getFile()), Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    if (ftpMain.getExt(item.getFile()).equals("folder"))
                    {
                        ftpMain.changeWorkingDirectory(item.getFile().getName());
                        loadFileList();
                    }
                }
                //gcaAdapter.notifyDataSetChanged();
            }
        });

        lvCloudGroup.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                final GroupFileItem item = (GroupFileItem) gcaAdapter.getItem(position);
                final FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();
                final CharSequence[] items = {"삭제"};

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setItems(items, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        if(items[index].equals("삭제"))
                        {
                            ftpMain.delete(item.getFile());
                            loadFileList();
                        }
                    }
                });

                alert.show();

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
                            Toast.makeText(getActivity(), "폴더명이 공백입니다.", Toast.LENGTH_SHORT).show();
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

    public void fabClickEvent()
    {
        if(isFabClicked == true)
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
            layout_fabCreateFolder.setVisibility(View.VISIBLE);
            layout_fabUpload.setVisibility(View.VISIBLE);
            fab.setImageResource(R.drawable.canclegroup);
            srlGroupCloudMain.setEnabled(false);
            lvCloudGroup.setEnabled(false);
        }
        isFabClicked = !isFabClicked;
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

    private void getFileList()
    {
        ((GroupCloudList) getActivity()).getFTPMain().getFileList();
    }

    public void loadFileList()
    {
        FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();
        FTPFile[] ftpfiles = ftpMain.getFileList();

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

                    gcaAdapter.addItem(file, cal);
                }

                gcaAdapter.sortAscName();
            }
            catch(ParseException pe)
            {
                pe.printStackTrace();
            }
        }
        else
        {
            Log.d("Twily", "실패함.");
        }
    }
}
