package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by reveu on 2017-05-29.
 */

class GroupCloudSearchAdapter extends BaseAdapter
{
    private ArrayList<GroupFileItem> gciList = new ArrayList<GroupFileItem>();
    private ArrayList<GroupFileItem> gciCheckList;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", java.util.Locale.getDefault());

    // ArrayList의 사이즈를 리턴
    @Override
    public int getCount() {
        return gciList.size();
    }

    public int getCheckCount()
    {
        return gciCheckList.size();
    }

    // position번째에 있는 object를 return
    @Override
    public GroupFileItem getItem(int position) {
        return gciList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        final GroupListHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_groupcloud, parent, false);

            /*
                최적화 및 스크롤링시에 오류를 방지하기 위한 holder 패턴
             */
            holder = new GroupListHolder();
            holder.ivFileExtImage = (ImageView) convertView.findViewById(R.id.cloud_ivFileExtImage);
            holder.tvFileName = (TextView) convertView.findViewById(R.id.cloud_tvFileName);
            holder.tvFileUpload = (TextView) convertView.findViewById(R.id.cloud_tvFileUpload); // 같은 Item을 재탕하느라 이름은 Upload지만, 여기서는 경로 Text가 들어간다.
            convertView.setTag(holder);
        }
        else
        {
            holder = (GroupListHolder) convertView.getTag();
        }

        final GroupFileItem listViewItem = gciList.get(position);
        final FTPFile file = listViewItem.getFile();

        // 아이템 내 각 위젯에 데이터 반영
        if(file.getName().equals(""))
        {
            holder.ivFileExtImage.setImageResource(0);
            holder.tvFileName.setText("..");
            holder.tvFileUpload.setText("");
        }
        else
        {
            if(listViewItem.getChecked())
                holder.ivFileExtImage.setImageResource(R.drawable.checkmark);
            else
                holder.ivFileExtImage.setImageResource(new FTPLib().getExtDrawable(listViewItem.getFile()));
            holder.tvFileName.setText(file.getName());
            holder.tvFileUpload.setText(context.getString(R.string.text_path, listViewItem.getPath()));
        }

        return convertView;
    }

    ArrayList<GroupFileItem> getList()
    {
        return gciList;
    }

    ArrayList<GroupFileItem> getCheckList()
    {
        return gciCheckList;
    }

    void setCheckList(ArrayList<GroupFileItem> list)
    {
        gciCheckList = list;
    }

    void addItem(FTPFile file, Calendar fileUploadDate, String path)
    {
        GroupFileItem item = new GroupFileItem();

        item.setFile(file);
        item.setFileUploadDate(fileUploadDate);
        item.setPath(path);

        GroupFileItem temp = checkFile(item);
        if(temp != null)
            item.setChecked(true);

        gciList.add(item);
        this.notifyDataSetChanged();
    }

    void clearList()
    {
        gciList.clear();
    }

    void clearCheckList()
    {
        gciCheckList.clear();
    }

    GroupFileItem checkFile(GroupFileItem item)
    {
        for (GroupFileItem temp : gciCheckList)
        {
            if(temp.getPath().equals(item.getPath()))
            {
                if(temp.getFile().getName().equals(item.getFile().getName()))
                    return temp;
            }
        }
        return null;
    }

    void removeCheckItem(GroupFileItem item)
    {
        GroupFileItem temp = checkFile(item);
        if(temp != null)
            gciCheckList.remove(temp);
    }

    void addCheckItem(GroupFileItem item)
    {
        gciCheckList.add(item);
    }

    private class GroupListHolder
    {
        ImageView ivFileExtImage;
        TextView tvFileName;
        TextView tvFileUpload;
    }
}