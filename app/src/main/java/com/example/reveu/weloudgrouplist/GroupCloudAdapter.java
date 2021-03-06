package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import static android.R.attr.src;

/**
 * Created by reveu on 2017-05-29.
 */

class GroupCloudAdapter extends BaseAdapter
{
    private ArrayList<GroupFileItem> gciList = new ArrayList<GroupFileItem>();
    private ArrayList<GroupFileItem> gciCheckList = new ArrayList<GroupFileItem>();

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
            holder.tvFileUpload = (TextView) convertView.findViewById(R.id.cloud_tvFileUpload);
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
            {
                holder.ivFileExtImage.setImageResource(new FTPLib().getExtDrawable(listViewItem.getFile()));
            }
            holder.tvFileName.setText(file.getName());
            holder.tvFileUpload.setText(context.getText(R.string.text_uploaddate).toString() + " : " + dateFormat.format(listViewItem.getFileUploadDate().getTime()));
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

    void addItem(FTPFile file)
    {
        GroupFileItem item = new GroupFileItem();

        item.setFile(file);

        gciList.add(item);
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

    void clearList()
    {
        gciList.clear();
    }

    void clearCheckList()
    {
        gciCheckList.clear();
    }

    void sortAscName()
    {
        AscendingName asscending = new AscendingName();
        Collections.sort(gciList, asscending);
        this.notifyDataSetChanged();
    }

    void sortDescName()
    {
        DescendingName desscending = new DescendingName();
        Collections.sort(gciList, desscending);
        this.notifyDataSetChanged();
    }

    void sortAscDate()
    {
        AscendingDate asscending = new AscendingDate();
        Collections.sort(gciList, asscending);
        this.notifyDataSetChanged();
    }

    void sortDescDate()
    {
        DescendingDate desscending = new DescendingDate();
        Collections.sort(gciList, desscending);
        this.notifyDataSetChanged();
    }

    private class AscendingName implements Comparator<GroupFileItem>
    {
        @Override
        public int compare(GroupFileItem o1, GroupFileItem o2)
        {
            FTPFile f1 = o1.getFile();
            FTPFile f2 = o2.getFile();

            int result = compareFolder(f1, f2);
            if(result == 0)
                return f1.getName().compareTo(f2.getName());
            else
                return result;
        }
    }

    private class DescendingName implements Comparator<GroupFileItem>
    {
        @Override
        public int compare(GroupFileItem o1, GroupFileItem o2)
        {
            FTPFile f1 = o1.getFile();
            FTPFile f2 = o2.getFile();

            int result = compareFolder(f1, f2);
            if(result == 0)
                return f2.getName().compareTo(f1.getName());
            else
                return result;
        }
    }

    private class AscendingDate implements Comparator<GroupFileItem>
    {
        @Override
        public int compare(GroupFileItem o1, GroupFileItem o2)
        {
            int result = compareFolder(o1.getFile(), o2.getFile());
            if(result == 0)
                return o2.getFileUploadDate().compareTo(o1.getFileUploadDate());
            else
                return result;
        }
    }

    private class DescendingDate implements Comparator<GroupFileItem>
    {
        @Override
        public int compare(GroupFileItem o1, GroupFileItem o2)
        {
            int result = compareFolder(o1.getFile(), o2.getFile());
            if(result == 0)
                return o1.getFileUploadDate().compareTo(o2.getFileUploadDate());
            else
                return result;
        }
    }

    private int compareFolder(FTPFile f1, FTPFile f2)
    {
        if(f1.getName().equals(""))
            return -1;
        if(f2.getName().equals(""))
            return 1;

        if(f1.isDirectory())
        {
            if(!f2.isDirectory())
                return -1;
            else
                return 0;
        }
        else
        {
            if(f2.isDirectory())
                return 1;
            else
                return 0;
        }
    }

    private class GroupListHolder
    {
        ImageView ivFileExtImage;
        TextView tvFileName;
        TextView tvFileUpload;
    }
}