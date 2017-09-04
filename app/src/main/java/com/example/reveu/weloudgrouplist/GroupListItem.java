package com.example.reveu.weloudgrouplist;

import java.util.Date;

/**
 * Created by reveu on 2017-06-04.
 */

public class GroupListItem
{
    boolean isFav = false;
    int groupID = 0;
    String groupName = "null";
    String groupCreator = "null";
    Date groupUploadDate = new Date(System.currentTimeMillis());
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

    public void setGroupUploadDate(Date input)
    {
        groupUploadDate = input;
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

    public Date getGroupUploadDate()
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
