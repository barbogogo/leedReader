package com.barbogogo.leedreader;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONObject;

import android.util.Log;

public class Folder
{

    private String          idFolder;
    private String          titleFolder;

    private ArrayList<Flux> flux = new ArrayList<Flux>();

    public Folder(String jquery)
    {
        try
        {
            JSONObject jsonObject = new JSONObject(jquery);

            idFolder = jsonObject.getString("id");
            titleFolder = jsonObject.getString("titre");

            JSONObject fluxItems = new JSONObject(jsonObject.getString("flux"));

            for (Iterator<String> iterator = fluxItems.keys(); iterator.hasNext();)
            {
                Object cle = iterator.next();
                String val = fluxItems.getString(String.valueOf(cle));
                flux.add(new Flux(val, idFolder));
            }

        }
        catch (Exception e)
        {
            Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
        }
    }

    public Folder()
    {

    }

    public void setTitle(String lTitle)
    {
        titleFolder = lTitle;
    }

    public String getTitle()
    {
        return titleFolder;
    }

    public ArrayList<String> getTitleFeeds()
    {
        ArrayList<String> listTitle = new ArrayList<String>();

        if (flux.size() > 0)
        {
            for (int i = 0; i < flux.size(); i++)
            {
                listTitle.add(flux.get(i).getName());
            }
        }
        else
        {
            listTitle.add("Pas de flux.");
        }
        return listTitle;
    }

    public void addFeed(Flux lFeed)
    {
        flux.add(lFeed);
    }

    public Flux getFeed(int posFlux)
    {
        return flux.get(posFlux);
    }

    public ArrayList<Flux> getFlux()
    {
        return flux;
    }

    public void setId(String lId)
    {
        idFolder = lId;
    }

    public String getId()
    {
        return idFolder;
    }

    public int getNbNoRead()
    {
        int lNbNoRead = 0;

        if (flux.size() > 0)
        {
            for (int i = 0; i < flux.size(); i++)
            {
                lNbNoRead += flux.get(i).getNbNoRead();
            }
        }

        return lNbNoRead;
    }
}
