package com.example.reveu.weloudgrouplist;

/**
 * Created by reveu on 2017-11-09.
 */

enum FTPCMD
{
    MakeDirectory("MD"), GetFileList("GFL"), ChangeWorkingDirectory("CWD"), Delete("DEL"), Move("MV"), Rename("RN"), RemoveDir("RDIR"), DownloadFile("DOWN"), SearchFile("SRF"), UploadFile("UP"), UploadFileOverWrite("UPO");

    final private String name;

    FTPCMD(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}