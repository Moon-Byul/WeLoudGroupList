package com.example.reveu.weloudgrouplist;

/**
 * Created by reveu on 2017-07-19.
 */

public class StringLib
{
    StringLib()
    {

    }

    String fileExt(String fileName)
    {
        int index = fileName.lastIndexOf(".");
        if(index != -1)
        {
            return fileName.substring(index + 1);
        }
        else
        {
            return "";
        }
    }

    String lastDir(String path)
    {
        int index = path.lastIndexOf("/");
        if(index != -1)
        {
            return path.substring(index + 1);
        }
        else
        {
            return "";
        }
    }
}