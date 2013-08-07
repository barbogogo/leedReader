package com.barbogogo.leedreader;

import java.util.ArrayList;

import com.leed.reader.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FolderAdapter extends ArrayAdapter<String>
{
    private final Context         context;

    private final ArrayList<Flux> feeds;

    public FolderAdapter(Context context, Folder folder)
    {
        super(context, R.layout.activity_main, folder.getTitleFeeds());

        ArrayList<Flux> feeds = folder.getFlux();

        this.context = context;
        this.feeds = feeds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.activity_folder, parent, false);
        TextView titleView = (TextView) rowView.findViewById(R.id.feedTitle);
        TextView noReadView = (TextView) rowView.findViewById(R.id.feedNoRead);

        int pNbNoRead = feeds.get(position).getNbNoRead();

        titleView.setText(feeds.get(position).getName());
        noReadView.setText(String.valueOf(pNbNoRead));

        if (pNbNoRead > 0)
        {
            titleView.setTypeface(null, Typeface.BOLD);
            noReadView.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            titleView.setTextSize(10);
            noReadView.setTextSize(10);
        }

        return rowView;
    }
}
