package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by reveu on 2017-05-29.
 */

public class GroupListSearchAdapter extends BaseAdapter
{
    private ArrayList<GroupListItem> gliList = new ArrayList<GroupListItem>();
    private ArrayList<GroupListItem> gliListSearch = gliList;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", java.util.Locale.getDefault());

    // ArrayList의 사이즈를 리턴
    @Override
    public int getCount() {
        return gliListSearch.size();
    }

    // position번째에 있는 object를 return
    @Override
    public GroupListItem getItem(int position) {
        return gliListSearch.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final Context context = parent.getContext();
        final GroupListHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_grouplist, parent, false);

            /*
                최적화 및 스크롤링시에 오류를 방지하기 위한 holder 패턴
             */
            holder = new GroupListHolder();
            holder.ivGroupImage = (ImageView) convertView.findViewById(R.id.group_ivGroupImage);
            holder.tvGroupName = (TextView) convertView.findViewById(R.id.group_tvName);
            holder.tvGroupUploadDate = (TextView) convertView.findViewById(R.id.group_tvEtc);
            holder.lnGroupMain = (LinearLayout) convertView.findViewById(R.id.group_lnGroupMain);
            convertView.setTag(holder);
        }
        else
        {
            holder = (GroupListHolder) convertView.getTag();
        }

        final GroupListItem listViewItem = gliListSearch.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        if (listViewItem.getFav() == true)
            holder.ivGroupImage.setImageResource(R.drawable.favgroup);
        else
            holder.ivGroupImage.setImageResource(R.drawable.group);
        holder.tvGroupName.setText(listViewItem.getGroupName());

        String text = context.getText(R.string.text_lastmodifed) + " : " + dateFormat.format(listViewItem.getGroupUploadDate().getTime());
        holder.tvGroupUploadDate.setText(text);

        return convertView;
    }

    public void setList(ArrayList<GroupListItem> list)
    {
        gliList = list;
    }

    public void filter(String charText)
    {
        charText = charText.toLowerCase(Locale.getDefault());
        gliListSearch.clear();
        if (charText.length() == 0)
        {
            gliListSearch.addAll(gliList);
        }
        else
        {
            for (GroupListItem item : gliList)
            {
                if (item.getGroupName().toLowerCase().contains(charText))
                {
                    gliListSearch.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    private class GroupListHolder
    {
        LinearLayout lnGroupMain;
        ImageView ivGroupImage;
        TextView tvGroupName;
        TextView tvGroupUploadDate;
    }
}