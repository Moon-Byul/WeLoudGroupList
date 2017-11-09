package com.example.reveu.weloudgrouplist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

import static com.example.reveu.weloudgrouplist.R.id.etGroupSearch;
import static com.example.reveu.weloudgrouplist.R.id.fragGroupCloudList;
import static com.example.reveu.weloudgrouplist.R.id.lvCloudGroup;
import static com.example.reveu.weloudgrouplist.R.id.lvGroupSearch;

/**
 * Created by reveu on 2017-06-06.
 */

public class FragGroupCloudSearch extends Fragment
{
    private GroupCloudSearchAdapter gcaSearchAdapter = new GroupCloudSearchAdapter();

    private EditText etGroupCloudSearch;
    private ListView lvGroupCloudSearch;
    private ImageView ivGroupCloudSearch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_groupcloud_search, container, false);

        ivGroupCloudSearch = (ImageView) rootView.findViewById(R.id.ivGroupCloudSearch);
        lvGroupCloudSearch = (ListView) rootView.findViewById(R.id.lvGroupCloudSearch);
        etGroupCloudSearch = (EditText) rootView.findViewById(R.id.etGroupCloudSearch);

        lvGroupCloudSearch.setAdapter(gcaSearchAdapter);

        gcaSearchAdapter.setCheckList(((GroupCloudList) getActivity()).fragGroupCloudList.getAdapter().getCheckList());

        ivGroupCloudSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String searchText = etGroupCloudSearch.getText().toString();

                if(searchText.length() < 2)
                {
                    Toast.makeText(getActivity(), getString(R.string.text_morechar, 2), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new StockLib().hideKeyboard(v, getActivity());
                    ((GroupCloudList) getActivity()).getFTPMain().execute(getActivity(), "searchEvent", FTPCMD.SearchFile, etGroupCloudSearch.getText().toString());
                }
            }
        });

        lvGroupCloudSearch.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupFileItem item = (GroupFileItem) gcaSearchAdapter.getItem(position);
                FTPLib ftpMain = ((GroupCloudList) getActivity()).getFTPMain();

                if (ftpMain.getExt(item.getFile()).equals("folder"))
                {
                    ((GroupCloudList) getActivity()).replacedSearchFragmentEvent();
                    ((GroupCloudList) getActivity()).cwdEvent(item.getPath() +  "/" + item.getFile().getName());
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
        });

        lvGroupCloudSearch.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupCloudList gclTemp = ((GroupCloudList) getActivity());

                final GroupFileItem item = (GroupFileItem) gcaSearchAdapter.getItem(position);
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
                            if(gcaSearchAdapter.getCheckCount() > 1)
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

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        etGroupCloudSearch.setText(null);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        new StockLib().hideKeyboard(etGroupCloudSearch, getActivity());
    }

    private void fileClickEvent(GroupFileItem item)
    {
        ((GroupCloudList) getActivity()).fileClickEvent(item);
        gcaSearchAdapter.notifyDataSetChanged();
    }

    private void fileCheckEvent(GroupFileItem item)
    {
        if(item.getChecked())
        {
            item.setChecked(false);
            gcaSearchAdapter.removeCheckItem(item);
        }
        else
        {
            item.setChecked(true);
            gcaSearchAdapter.addCheckItem(item);
        }

        int amount = gcaSearchAdapter.getCheckCount();

        if(amount > 1)
            ((GroupCloudList) getActivity()).tvGroupCloudAmount.setText(getString(R.string.text_selectitem_multi, amount));
        else if(amount == 1)
            ((GroupCloudList) getActivity()).tvGroupCloudAmount.setText(getString(R.string.text_selectitem_one, amount));
        else
            ((GroupCloudList) getActivity()).modifyLayoutEvent(false, item);

        gcaSearchAdapter.notifyDataSetChanged();
    }

    public void searchEvent(FTPFileAdv[] files)
    {
        for(FTPFileAdv file : files)
        {
            gcaSearchAdapter.addItem(file.getFtpFile(), file.getFtpFile().getTimestamp(), file.getPath());
        }
    }

    public GroupCloudSearchAdapter getAdapter()
    {
        return gcaSearchAdapter;
    }
}
