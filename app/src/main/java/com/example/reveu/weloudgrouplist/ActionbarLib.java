package com.example.reveu.weloudgrouplist;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by reveu on 2017-09-07.
 */

public class ActionbarLib
{
    private ImageView btnActionBarBack;
    private ImageView btnActionBarConfirm;
    private TextView tvActionBarTitle;

    public void setDefaultActionBar(AppCompatActivity activity, String title, boolean isClose, int confirmType)
    {
        ActionBar actionBar = activity.getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        View customBar = LayoutInflater.from(activity).inflate(R.layout.actionbar_default, null);
        actionBar.setCustomView(customBar);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4472c4")));

        Toolbar parent = (Toolbar) customBar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        btnActionBarBack = (ImageView) customBar.findViewById(R.id.actionbar_default_btnBack);
        btnActionBarConfirm = (ImageView) customBar.findViewById(R.id.actionbar_default_btnConfirm);
        tvActionBarTitle = (TextView) customBar.findViewById(R.id.actionbar_default_tvTitle);

        tvActionBarTitle.setText(title);

        if(isClose)
        {
            btnActionBarBack.setImageResource(R.drawable.cancle);
        }
        else
        {
            btnActionBarBack.setImageResource(R.drawable.backbtn);
        }

        if(confirmType > 0)
        {
            btnActionBarConfirm.setVisibility(View.VISIBLE);
            if(confirmType == 1)
            {
                btnActionBarConfirm.setImageResource(R.drawable.checkmark);
            }
            else
            {
                btnActionBarConfirm.setImageResource(R.drawable.addgroup_big);
            }
        }
        else
        {
            btnActionBarConfirm.setVisibility(View.INVISIBLE);
        }
    }

    public void setEnableConfirmBtn(boolean enabled)
    {
        btnActionBarConfirm.setEnabled(enabled);
        if(enabled)
            btnActionBarConfirm.setColorFilter(Color.argb(255, 255, 255, 255));
        else
            btnActionBarConfirm.setColorFilter(Color.argb(180, 255, 255, 255));
    }

    public ImageView getBtnActionBarBack()
    {
        return btnActionBarBack;
    }

    public ImageView getBtnActionBarConfirm()
    {
        return btnActionBarConfirm;
    }

    public TextView getTvActionBarTitle()
    {
        return tvActionBarTitle;
    }

}
