package com.example.reveu.weloudgrouplist;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by reveu on 2017-09-06.
 */

public class StockLib
{
    // 나는 키보드가 싫다!!!!!!!!!!!!
    public void hideKeyboard(View v, Activity a)
    {
        InputMethodManager imm= (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void hideKeyboard(View v, Application a)
    {
        InputMethodManager imm= (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
