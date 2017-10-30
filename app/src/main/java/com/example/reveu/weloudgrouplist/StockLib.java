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

/*
 * 많은 Class에서 자주 사용되는 Method들을 한 곳으로 모은 Library 형식 Class
 */
public class StockLib
{
    /*
     * 키보드를 숨기는 Method. View와 Activity 혹은 View와 Application을 Parameter로 넣을 수 있다. (Overloading)
     */
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

    /*
     * ID가 형식에 맞는지 체크하는 Method. 여러곳에서 쓰일경우 일일히 수정하는 불편함이 존재하여 클래스에 하나로 통합.
     */
    public Boolean isIDFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9]{4,16}$", input))
            return true;
        else
            return false;
    }

    /*
     * Password가 형식에 맞는지 체크하는 Method.
     */
    public Boolean isPwFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9~!@#$%^&*()-_=+]{4,24}$", input))
            return true;
        else
            return false;
    }

    /*
     * Nickname이 형식에 맞는지 체크하는 Method.
     */
    public Boolean isNickFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9가-힣]{2,12}$", input))
            return true;
        else
            return false;
    }

    /*
     * Email이 형식에 맞는지 체크하는 Method.
     */
    public Boolean isEmailFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9]+@[a-zA-Z0-9.]+$", input))
            return true;
        else
            return false;
    }

    /*
     * GroupName이 형식에 맞는지 체크하는 Method.
     */
    public Boolean isGroupNameFormatted(String input)
    {
        if(Pattern.matches("^[a-zA-Z0-9가-힣]{2,16}$", input))
            return true;
        else
            return false;
    }
}
