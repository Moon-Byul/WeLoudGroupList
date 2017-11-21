package com.example.reveu.weloudgrouplist;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by reveu on 2017-11-09.
 */

class FTPFileAdv
{
    private FTPFile ftpFile;
    private String path = "";

    FTPFileAdv(FTPFile file, String path)
    {
        this.ftpFile = file;
        this.path = path;
    }

    FTPFile getFtpFile()
    {
        return ftpFile;
    }

    String getPath()
    {
        return path;
    }

    void setPath(String path)
    {
        this.path = path;
    }
}
