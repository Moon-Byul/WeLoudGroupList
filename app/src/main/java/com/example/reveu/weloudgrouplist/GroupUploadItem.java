package com.example.reveu.weloudgrouplist;

import java.io.File;

/**
 * Created by reveu on 2017-06-04.
 */

public class GroupUploadItem
{
    File file;
    boolean isChecked = false;
    boolean isParent = false;

    public void setFile(File input)
    {
        file = input;
    }

    public void setChecked(boolean input)
    {
        isChecked = input;
    }

    public void setParent(boolean input)
    {
        isParent = input;
    }

    public File getFile()
    {
        return file;
    }

    public boolean getChecked()
    {
        return isChecked;
    }

    public boolean getParent()
    {
        return isParent;
    }
}
