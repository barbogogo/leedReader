package com.barbogogo.leedreader;

import java.util.ArrayList;

import com.leed.reader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MobileArrayAdapter extends ArrayAdapter<String>
{
    private final Context           pContext;
    private final ArrayList<String> pValues;
    private final ArrayList<String> pNbNoRead;
    private final ArrayList<Folder> pFolders;

    public MobileArrayAdapter(Context context, ArrayList<String> values, ArrayList<Folder> folders)
    {
        super(context, R.layout.activity_main, values);

        pFolders = folders;
        pNbNoRead = nbNoRead();

        pContext = context;
        pValues = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) pContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.linear_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        TextView nbNoRead = (TextView) rowView.findViewById(R.id.nbNoRead);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);

        textView.setText(pValues.get(position));
        nbNoRead.setText(pNbNoRead.get(position));

        imageView.setImageResource(R.drawable.folder);

        return rowView;
    }

    private ArrayList<String> nbNoRead()
    {
        ArrayList<String> lNbNoRead = new ArrayList<String>();

        for (int i = 0; i < pFolders.size(); i++)
        {
            lNbNoRead.add(String.valueOf(pFolders.get(i).getNbNoRead()));
        }

        return lNbNoRead;
    }
}
