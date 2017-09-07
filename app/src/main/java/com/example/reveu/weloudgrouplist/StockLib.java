package com.example.reveu.weloudgrouplist;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.regex.Pattern;

import static com.example.reveu.weloudgrouplist.R.id.textinfo;

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

    public Boolean isIDFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9]{4,16}$", input))
            return true;
        else
            return false;
    }

    public Boolean isPwFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9~!@#$%^&*()-_=+]{4,24}$", input))
            return true;
        else
            return false;
    }

    public Boolean isNickFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9가-힣]{2,12}$", input))
            return true;
        else
            return false;
    }

    public Boolean isEmailFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9.]+$", input))
            return true;
        else
            return false;
    }
}
