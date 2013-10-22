package com.barbogogo.leedreader;

import java.util.ArrayList;

import com.leed.reader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuAdapter extends BaseExpandableListAdapter
{

    private Context           context;
    private ArrayList<Folder> folders;
    private LayoutInflater    inflater;
    private String            mShowEmptyFeeds;

    public MenuAdapter(Context context, ArrayList<Folder> folders, String showEmptyFeeds)
    {
        this.context = context;
        this.folders = folders;
        inflater = LayoutInflater.from(context);
        mShowEmptyFeeds = showEmptyFeeds;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    public Flux getChild(int gPosition, int cPosition)
    {
        return folders.get(gPosition).getFeed(cPosition);
    }

    public long getChildId(int gPosition, int cPosition)
    {
        return cPosition;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
            ViewGroup parent)
    {

        ChildViewHolder childViewHolder;

        final Flux feed = (Flux) getChild(groupPosition, childPosition);

        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.menu_child, null);

            childViewHolder = new ChildViewHolder();
            childViewHolder.textViewChild = (TextView) convertView.findViewById(R.id.labelChild);
            childViewHolder.linearLayout =
                    (LinearLayout) convertView.findViewById(R.id.feedNoReadChildLayout);
            childViewHolder.textViewNoReadChild = (TextView) convertView.findViewById(R.id.feedNoReadChild);

            convertView.setTag(childViewHolder);
        }
        else
        {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        childViewHolder.textViewChild.setText(feed.getName());

        View.OnClickListener onClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((LeedReader) context).setModeView(LeedReader.cModePageLoading);
                ((LeedReader) context).getFeed(feed);
            }
        };

        childViewHolder.textViewChild.setOnClickListener(onClick);
        childViewHolder.textViewNoReadChild.setOnClickListener(onClick);

        childViewHolder.textViewNoReadChild.setText(String.valueOf(feed.getNbNoRead()));

        if (mShowEmptyFeeds.equals("0") && feed.getNbNoRead() == 0)
        {
            childViewHolder.textViewChild.setVisibility(View.GONE);
            childViewHolder.linearLayout.setVisibility(View.GONE);
            childViewHolder.textViewNoReadChild.setVisibility(View.GONE);
            convertView.setVisibility(View.GONE);
        }
        else
        {
            childViewHolder.textViewChild.setVisibility(View.VISIBLE);
            childViewHolder.linearLayout.setVisibility(View.VISIBLE);
            childViewHolder.textViewNoReadChild.setVisibility(View.VISIBLE);
            convertView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public int getChildrenCount(int gPosition)
    {
        return folders.get(gPosition).getFlux().size();
    }

    public Object getGroup(int gPosition)
    {
        return folders.get(gPosition);
    }

    public int getGroupCount()
    {
        return folders.size();
    }

    public long getGroupId(int gPosition)
    {
        return gPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {

        GroupViewHolder gholder;

        Folder folder = (Folder) getGroup(groupPosition);

        if (convertView == null)
        {
            gholder = new GroupViewHolder();

            convertView = inflater.inflate(R.layout.menu_group, null);

            gholder.textViewGroup = (TextView) convertView.findViewById(R.id.labelGroup);

            gholder.textViewNoReadGroup = (TextView) convertView.findViewById(R.id.feedNoReadGroup);

            convertView.setTag(gholder);
        }
        else
        {
            gholder = (GroupViewHolder) convertView.getTag();
        }

        gholder.textViewGroup.setText(folder.getTitle());

        gholder.textViewNoReadGroup.setText(String.valueOf(folder.getNbNoRead()));

        return convertView;
    }

    public boolean hasStableIds()
    {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1)
    {
        return true;
    }

    class GroupViewHolder
    {
        public TextView textViewGroup;
        public TextView textViewNoReadGroup;
    }

    class ChildViewHolder
    {
        public TextView     textViewChild;
        public TextView     textViewNoReadChild;
        public LinearLayout linearLayout;
    }

}
