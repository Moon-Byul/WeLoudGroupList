package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Created by reveu on 2017-06-06.
 */

public class FragGroupSearch extends Fragment
{
    private GroupListSearchAdapter glaSearchAdapter = new GroupListSearchAdapter();
    private GroupListAdapter glaAdapter;
    private EditText etGroupSearch;
    private ListView lvGroupSearch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_grouplist_serach, container, false);

        lvGroupSearch = (ListView) rootView.findViewById(R.id.lvGroupSearch);
        etGroupSearch = (EditText) rootView.findViewById(R.id.etGroupSearch);

        lvGroupSearch.setAdapter(glaSearchAdapter);
        glaSearchAdapter.setList(glaAdapter.getList());
        glaSearchAdapter.filter("");

        lvGroupSearch.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                GroupListItem item = (GroupListItem) glaSearchAdapter.getItem(position);

                if(item.getFav())
                    item.setFav(false);
                else
                    item.setFav(true);
                Snackbar.make(view, "테스트 메시지입니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                glaSearchAdapter.notifyDataSetChanged();
            }
        });

        etGroupSearch.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                String filterText = s.toString();
                glaSearchAdapter.filter(filterText);
            }
        });


        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        etGroupSearch.setText(null);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        new StockLib().hideKeyboard(etGroupSearch, getActivity());
    }

    public void setAdapterOnView(GroupListAdapter adapter)
    {
        glaAdapter = adapter;
    }
}
