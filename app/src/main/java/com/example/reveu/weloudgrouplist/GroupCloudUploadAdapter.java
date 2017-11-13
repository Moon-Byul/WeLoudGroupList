package com.example.reveu.weloudgrouplist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by reveu on 2017-05-29.
 */

class GroupCloudUploadAdapter extends BaseAdapter
{
    private ArrayList<GroupUploadItem> gcuList = new ArrayList<GroupUploadItem>();
    private ArrayList<GroupUploadItem> gcuCheckList = new ArrayList<GroupUploadItem>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", java.util.Locale.getDefault());

    // ArrayList의 사이즈를 리턴
    @Override

    public int getCount() {
        return gcuList.size();
    }

    public int getCheckCount()
    {
        return gcuCheckList.size();
    }

    // position번째에 있는 object를 return
    @Override
    public GroupUploadItem getItem(int position) {
        return gcuList.get(position);
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

        final GroupUploadItem listViewItem = gcuList.get(position);
        final File file = listViewItem.getFile();

        // 아이템 내 각 위젯에 데이터 반영
        if(listViewItem.getParent())
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
                holder.ivFileExtImage.setImageResource(getExtDrawable(file));
            holder.tvFileName.setText(file.getName());
            holder.tvFileUpload.setText(context.getText(R.string.text_lastmodifed).toString() + " : " + dateFormat.format(listViewItem.getFile().lastModified()));
        }

        return convertView;
    }

    ArrayList<GroupUploadItem> getList()
    {
        return gcuList;
    }

    ArrayList<GroupUploadItem> getCheckList()
    {
        return gcuCheckList;
    }

    void setCheckList(ArrayList<GroupUploadItem> list)
    {
        gcuCheckList = list;
    }

    int getExtDrawable(File file)
    {
        if(file.isDirectory())
            return R.drawable.folder;
        if(file.isFile())
        {
            return new ExtLib().getDrawableExt(new StringLib().fileExt(file.getName()));
        }
        return R.drawable.blank_file;
    }

    void addParentItem(File file)
    {
        GroupUploadItem item = new GroupUploadItem();
        File temp = file.getParentFile();

        item.setFile(temp);
        item.setParent(true);

        gcuList.add(item);
    }

    void addItem(File file)
    {
        GroupUploadItem item = new GroupUploadItem();

        item.setFile(file);
        item.setParent(false);

        GroupUploadItem temp = checkFile(item);
        if(temp != null)
            item.setChecked(true);

        gcuList.add(item);
    }

    GroupUploadItem checkFile(GroupUploadItem item)
    {
        for (GroupUploadItem temp : gcuCheckList)
        {
            if(temp.getFile().getPath().equals(item.getFile().getPath()))
            {
                if(temp.getFile().getName().equals(item.getFile().getName()))
                    return temp;
            }
        }
        return null;
    }

    void removeCheckItem(GroupUploadItem item)
    {
        GroupUploadItem temp = checkFile(item);
        if(temp != null)
            gcuCheckList.remove(temp);
    }

    void addCheckItem(GroupUploadItem item)
    {
        gcuCheckList.add(item);
    }

    void clearList()
    {
        gcuList.clear();
    }

    void clearCheckList()
    {
        gcuCheckList.clear();
    }

    void sortAscName()
    {
        AscendingName asscending = new AscendingName();
        Collections.sort(gcuList, asscending);
        this.notifyDataSetChanged();
    }

    private class AscendingName implements Comparator<GroupUploadItem>
    {
        @Override
        public int compare(GroupUploadItem o1, GroupUploadItem o2)
        {
            File f1 = o1.getFile();
            File f2 = o2.getFile();

            int result = compareFolder(o1, o2);
            if(result == 0)
                return f1.getName().compareTo(f2.getName());
            else
                return result;
        }
    }

    private int compareFolder(GroupUploadItem o1, GroupUploadItem o2)
    {
        if(o1.getParent())
            return -1;
        if(o2.getParent())
            return 1;

        if(o1.getFile().isDirectory())
        {
            if(!o2.getFile().isDirectory())
                return -1;
            else
                return 0;
        }
        else
        {
            if(o2.getFile().isDirectory())
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