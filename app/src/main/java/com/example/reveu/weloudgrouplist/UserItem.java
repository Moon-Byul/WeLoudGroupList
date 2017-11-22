package com.example.reveu.weloudgrouplist;

import java.util.Calendar;
import java.util.TimeZone;

import static android.R.id.input;

/**
 * Created by reveu on 2017-11-21.
 */

public class UserItem
{
    private String userID;
    private int userNum;
    private Calendar joinDate = null;
    private String nickName = "";
    private String rankName = "";
    private boolean isChecked;
    /*
     * status의 value type
     * 0 - null
     * 1 - invite 보냄
     * 2 - 이미 가입됨
     */
    int status = 0;

    public void setJoinDate(Calendar input)
    {
        // MYSQL 서버의 시간은 UTC 시간 기준으로 return 하기 때문에 재 설정한다.
        joinDate = Calendar.getInstance();
        joinDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        joinDate.set(input.get(Calendar.YEAR), input.get(Calendar.MONTH), input.get(Calendar.DATE), input.get(Calendar.HOUR_OF_DAY), input.get(Calendar.MINUTE), input.get(Calendar.SECOND));
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    public void setUserNum(int userNum)
    {
        this.userNum = userNum;
    }

    public void setRankName(String rankName)
    {
        this.rankName = rankName;
    }

    public void setChecked(boolean checked)
    {
        isChecked = checked;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public Calendar getJoinDate()
    {
        return joinDate;
    }

    public int getUserNum()
    {
        return userNum;
    }

    public String getRankName()
    {
        return rankName;
    }

    public String getNickName()
    {
        return nickName;
    }

    public String getUserID()
    {
        return userID;
    }

    public boolean getChecked()
    {
        return isChecked;
    }

    public int getStatus()
    {
        return status;
    }
}
