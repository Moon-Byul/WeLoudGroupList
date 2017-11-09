package com.example.reveu.weloudgrouplist;

/**
 * Created by reveu on 2017-11-09.
 */

enum FTPCMD
{
    MakeDirectory("MD"), GetFileList("GFL"), ChangeWorkingDirectory("CWD"), Delete("DEL"), Rename("RN"), RemoveDir("RDIR"), DownloadFile("DOWN");

    final private String name;

    private FTPCMD(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}