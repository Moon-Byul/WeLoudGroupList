package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by reveu on 2017-05-29.
 */

public class GroupListAdapter extends BaseAdapter
{
    private ArrayList<GroupListItem> gliList = new ArrayList<GroupListItem>();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", java.util.Locale.getDefault());

    // ArrayList의 사이즈를 리턴
    @Override
    public int getCount() {
        return gliList.size();
    }

    // position번째에 있는 object를 return
    @Override
    public GroupListItem getItem(int position) {
        return gliList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final GroupListHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_grouplist, parent, false);

            /*
                최적화 및 스크롤링시에 오류를 방지하기 위한 holder 패턴
             */
            holder = new GroupListHolder();
            holder.ivGroupImage = (ImageView) convertView.findViewById(R.id.group_ivGroupImage);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.group_tvName);
            holder.tvEtc = (TextView) convertView.findViewById(R.id.group_tvEtc);
            holder.lnGroupMain = (LinearLayout) convertView.findViewById(R.id.group_lnGroupMain);
            holder.ivGroupStatus = (ImageView) convertView.findViewById(R.id.group_ivStatus);
            convertView.setTag(holder);
        } else {
            holder = (GroupListHolder) convertView.getTag();
        }

        final GroupListItem listViewItem = gliList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        if (listViewItem.getFav() == true)
            holder.ivGroupImage.setImageResource(R.drawable.favgroup);
        else
            holder.ivGroupImage.setImageResource(R.drawable.group);
        holder.tvGroupName.setText(listViewItem.getGroupName());

        if(listViewItem.getGroupCreator() == "null")
        {
            holder.tvEtc.setText("최근 업로드 : " + dateFormat.format(listViewItem.getGroupUploadDate()));
        }
        else
        {
            holder.tvEtc.setText("관리자 : " + listViewItem.getGroupCreator());
        }

        int status = listViewItem.getStatus();
        if(status > 0)
        {
            if(status == 1)
            {
                holder.ivGroupStatus.setImageResource(R.drawable.clock);
            }
            else
            {
                holder.ivGroupStatus.setImageResource(R.drawable.checkmark);
            }
        }
        else
        {
            holder.ivGroupStatus.setImageResource(0);
        }

        return convertView;
    }

    public ArrayList<GroupListItem> getList()
    {
        return gliList;
    }

    public void addItem(int groupID, boolean isFav, String groupName, Date groupUploadDate)
    {
        GroupListItem item = new GroupListItem();

        item.setGroupID(groupID);
        item.setFav(isFav);
        item.setGroupName(groupName);
        item.setGroupUploadDate(groupUploadDate);

        gliList.add(item);
        this.notifyDataSetChanged();
    }

    public void addItem(int groupID, boolean isFav, String groupName, String groupCreator, int status)
    {
        GroupListItem item = new GroupListItem();

        item.setGroupID(groupID);
        item.setFav(isFav);
        item.setGroupName(groupName);
        item.setGroupCreator(groupCreator);
        item.setStatus(status);

        gliList.add(item);
        this.notifyDataSetChanged();
    }

    public void clearList()
    {
        gliList.clear();
    }

    public void sortAscName()
    {
        AscendingName asscending = new AscendingName();
        Collections.sort(gliList, asscending);
        this.notifyDataSetChanged();
    }

    public void sortDescName()
    {
        DescendingName desscending = new DescendingName();
        Collections.sort(gliList, desscending);
        this.notifyDataSetChanged();
    }

    public void sortAscDate()
    {
        AscendingDate asscending = new AscendingDate();
        Collections.sort(gliList, asscending);
        this.notifyDataSetChanged();
    }

    public void sortDescDate()
    {
        DescendingDate desscending = new DescendingDate();
        Collections.sort(gliList, desscending);
        this.notifyDataSetChanged();
    }

    private class AscendingName implements Comparator<GroupListItem>
    {
        @Override
        public int compare(GroupListItem o1, GroupListItem o2)
        {
            int result = compareFav(o1, o2);
            if(result == 0)
                return o1.getGroupName().compareTo(o2.getGroupName());
            else
                return result;
        }
    }

    private class DescendingName implements Comparator<GroupListItem>
    {
        @Override
        public int compare(GroupListItem o1, GroupListItem o2)
        {
            int result = compareFav(o1, o2);
            if(result == 0)
                return o2.getGroupName().compareTo(o1.getGroupName());
            else
                return result;
        }
    }

    private class AscendingDate implements Comparator<GroupListItem>
    {
        @Override
        public int compare(GroupListItem o1, GroupListItem o2)
        {
            int result = compareFav(o1, o2);
            if(result == 0)
                return o2.getGroupUploadDate().compareTo(o1.getGroupUploadDate());
            else
                return result;
        }
    }

    private class DescendingDate implements Comparator<GroupListItem>
    {
        @Override
        public int compare(GroupListItem o1, GroupListItem o2)
        {
            int result = compareFav(o1, o2);
            if(result == 0)
                return o1.getGroupUploadDate().compareTo(o2.getGroupUploadDate());
            else
                return result;
        }
    }

    private int compareFav(GroupListItem o1, GroupListItem o2)
    {
        if(o1.getFav())
        {
            if(!o2.getFav())
                return -1;
            else
                return 0;
        }
        else
        {
            if(o2.getFav())
                return 1;
            else
                return 0;
        }
    }

    private class GroupListHolder
    {
        LinearLayout lnGroupMain;
        ImageView ivGroupImage;
        TextView tvGroupName;
        TextView tvEtc;
        ImageView ivGroupStatus;
    }

    /*
        안드로이드는 dp를 쓰기 때문에 값을 dp로 바꿔주는 작업이 필요. 그때 쓰이는 메소드
     */
    /*
    public int getDensityPx(int value)
    {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale);
    }
    */
}