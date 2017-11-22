package com.example.reveu.weloudgrouplist;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reveu on 2017-10-22.
 */

class ExtLib
{
    private ArrayList<ExtClass> extLib = new ArrayList<ExtClass>();

    ExtLib()
    {
        // 이 문장에서 확장자 관리

        extLib.add(new ExtClass(String.valueOf(R.drawable.ppt), "ppt", "pptx"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.document), "txt", "docs", "hwp", "c", "xml", "java", "html"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.music), "mp3", "wav", "ogg", "wma", "m4a"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.video), "avi", "mp4", "flv", "wmv"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.zip), "zip", "7z", "alz", "rar", "iso"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.picture), "jpg", "jpeg", "png", "gif"));
        extLib.add(new ExtClass(String.valueOf(R.drawable.apk), "apk"));
    }

    int getDrawableExt(String input)
    {
        for (ExtClass ext : extLib)
        {
            for  (String temp : ext.getExtList())
            {
                if(input.toLowerCase().equals(temp.toLowerCase()))
                    return ext.getDrawable();
            }
        }
        return R.drawable.blank_file;
    }

    private class ExtClass
    {
        private ArrayList<String> extList = new ArrayList<String>();
        private int drawable;

        public ExtClass(String... params)
        {
            boolean isDrawable = true;
            //params[0]은 Drawable, 나머지는 확장자
            for (String temp : params)
            {
                if(isDrawable == true)
                {
                    drawable = Integer.parseInt(temp);
                    isDrawable = false;
                }
                else
                {
                    extList.add(temp);
                }
            }
        }

        ArrayList<String> getExtList()
        {
            return extList;
        }

        public int getDrawable()
        {
            return drawable;
        }
    }
}
