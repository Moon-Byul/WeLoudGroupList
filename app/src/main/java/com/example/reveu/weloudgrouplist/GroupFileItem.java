package com.example.reveu.weloudgrouplist;

import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by reveu on 2017-06-04.
 */

public class GroupFileItem
{
    FTPFile file;
    Calendar fileUploadDate = Calendar.getInstance();
    /*
     * status의 value type
     * 0 - null
     * 1 - 가입 승인중
     * 2 - 이미 가입됨
     */
    int status = 0;

    public void setFile(FTPFile input)
    {
        file = input;
    }

    public void setFileUploadDate(Calendar input)
    {
        // FTP 서버의 시간은 UTC 시간 기준으로 return 하기 때문에 재 설정한다.
        fileUploadDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        fileUploadDate.set(input.get(Calendar.YEAR), input.get(Calendar.MONTH), input.get(Calendar.DATE), input.get(Calendar.HOUR_OF_DAY), input.get(Calendar.MINUTE), input.get(Calendar.SECOND));
    }

    public FTPFile getFile()
    {
        return file;
    }

    public Calendar getFileUploadDate()
    {
        return fileUploadDate;
    }
}
