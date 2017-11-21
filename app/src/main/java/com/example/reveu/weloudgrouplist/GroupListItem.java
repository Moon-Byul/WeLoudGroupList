package com.example.reveu.weloudgrouplist;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by reveu on 2017-06-04.
 */

public class GroupListItem
{
    boolean isFav = false;
    int groupID = 0;
    String groupName = "null";
    String groupCreator = "null";
    Calendar groupUploadDate = Calendar.getInstance();
    /*
     * status의 value type
     * 0 - null
     * 1 - 가입 승인중
     * 2 - 이미 가입됨
     */
    int status = 0;

    public void setGroupID(int input)
    {
        groupID = input;
    }

    public void setFav(boolean input)
    {
        isFav = input;
    }

    public void setGroupName(String input)
    {
        groupName = input;
    }

    public void setGroupUploadDate(Calendar input)
    {
        // MYSQL 서버의 시간은 UTC 시간 기준으로 return 하기 때문에 재 설정한다.
        groupUploadDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        groupUploadDate.set(input.get(Calendar.YEAR), input.get(Calendar.MONTH), input.get(Calendar.DATE), input.get(Calendar.HOUR_OF_DAY), input.get(Calendar.MINUTE), input.get(Calendar.SECOND));
    }

    public void setGroupCreator(String input)
    {
        groupCreator = input;
    }

    public void setStatus(int input)
    {
        status = input;
    }

    public int getGroupID()
    {
        return groupID;
    }

    public boolean getFav()
    {
        return isFav;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public Calendar getGroupUploadDate()
    {
        return groupUploadDate;
    }

    public String getGroupCreator()
    {
        return groupCreator;
    }

    public int getStatus()
    {
        return status;
    }
}
