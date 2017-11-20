package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by reveu on 2017-05-29.
 */

class UserListAdapter extends BaseAdapter
{
    private ArrayList<UserItem> userList = new ArrayList<UserItem>();
    private ArrayList<UserItem> userSearchList = new ArrayList<UserItem>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", java.util.Locale.getDefault());

    // ArrayList의 사이즈를 리턴
    @Override
    public int getCount() {
        return userSearchList.size();
    }
    // position번째에 있는 object를 return
    @Override
    public UserItem getItem(int position) {
        return userSearchList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final UserHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_user, parent, false);

            /*
                최적화 및 스크롤링시에 오류를 방지하기 위한 holder 패턴
             */
            holder = new UserHolder();
            holder.tvID = (TextView) convertView.findViewById(R.id.tvID);
            holder.tvEtc = (TextView) convertView.findViewById(R.id.tvEtc);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            holder.ivStatus = (ImageView) convertView.findViewById(R.id.ivStatus);
            convertView.setTag(holder);
        }
        else
        {
            holder = (UserHolder) convertView.getTag();
        }

        final UserItem item = userSearchList.get(position);
        String text;

        holder.tvID.setText(item.getUserID());

        if(item.getJoinDate() != null)
            text = context.getText(R.string.text_joindate) + " : " + dateFormat.format(item.getJoinDate().getTime());
        else if(!item.getRankName().equals(""))
            text = context.getText(R.string.text_rankname) + " : " + item.getRankName();
        else
            text = context.getText(R.string.text_nickname) + " : " + item.getNickName();
        holder.tvEtc.setText(text);

        // 아이템 내 각 위젯에 데이터 반영
        if(item.getChecked())
            holder.ivIcon.setImageResource(R.drawable.checkmark);
        else
            holder.ivIcon.setImageResource(R.drawable.user);

        if(item.getStatus() == 0)
        {
            holder.ivStatus.setImageResource(0);
        }
        else if(item.getStatus() == 1)
        {
            holder.ivStatus.setImageResource(R.drawable.clock);
        }
        else
        {
            holder.ivStatus.setImageResource(R.drawable.checkmark);
        }

        return convertView;
    }

    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        userSearchList.clear();
        if (charText.length() == 0)
        {
            userSearchList.addAll(userList);
        }
        else
        {
            for (UserItem item : userList)
            {
                if (item.getUserID().toLowerCase().contains(charText))
                {
                    userSearchList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    ArrayList<UserItem> getList()
    {
        return userList;
    }

    void addItem(String userID, int userNum, String rankName)
    {
        UserItem item = new UserItem();

        item.setUserID(userID);
        item.setUserNum(userNum);
        item.setRankName(rankName);

        userList.add(item);
        this.notifyDataSetChanged();
    }

    void addItem(String userID, int userNum, Calendar joinDate)
    {
        UserItem item = new UserItem();

        item.setUserID(userID);
        item.setUserNum(userNum);
        item.setJoinDate(joinDate);

        userList.add(item);
        this.notifyDataSetChanged();
    }

    void addItem(String userID, int userNum, String nickName, int status)
    {
        UserItem item = new UserItem();

        item.setUserID(userID);
        item.setUserNum(userNum);
        item.setStatus(status);
        item.setNickName(nickName);

        userList.add(item);
        this.notifyDataSetChanged();
    }

    void clearList()
    {
        userList.clear();
        userSearchList.clear();
    }

    int getCheckCount()
    {
        int result = 0;

        for(UserItem user : userList)
        {
            if(user.getChecked())
                result++;
        }

        return result;
    }

    private class UserHolder
    {
        TextView tvID;
        TextView tvEtc;
        ImageView ivIcon;
        ImageView ivStatus;
    }
}