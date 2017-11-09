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

/*
 * Action Bar를 Customizing 하기 위해 Method를 모아놓은 Class
 */

public class ActionbarLib
{
    private ImageView btnActionBarBack;
    private ImageView btnActionBarConfirm;
    private TextView tvActionBarTitle;

    /*
     * setDefaultActionBar( activity, title, isClose, confirmType )
     * activity - 현재 사용중인 Activity에 Actionbar를 Inflate 하기 위해 Parameter로 받는다. (주로 this 나 getActivity()를 사용)
     * title - Actionbar의 Title, 즉 제목을 설정한다.
     * isClose - true일 경우 X 표시가, false일 경우 뒤로가기 이미지 버튼으로 출력된다.
     * confirmType - 0일 경우 액션바 우측에 아무 버튼도 없게, 1일 경우 체크 이미지, 2일 경우 + 이미지를 출력하게 한다.
     */
    public void setDefaultActionBar(AppCompatActivity activity, String title, boolean isClose, int confirmType)
    {
        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        btnActionBarBack = (ImageView) toolbar.findViewById(R.id.actionbar_default_btnBack);
        btnActionBarConfirm = (ImageView) toolbar.findViewById(R.id.actionbar_default_btnConfirm);
        tvActionBarTitle = (TextView) toolbar.findViewById(R.id.actionbar_default_tvTitle);

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

    /*
     * Confirm 버튼의 enabled를 변경할 때, 해당 이미지의 Color도 변경하게 해주는 Method
     */
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
