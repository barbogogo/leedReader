package com.leedReader;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DataManagement 
{
	private APIConnection connection;
	private LocalData offLineData;
	
	private int connectionType;
	private static final int cOnLine  = 0;
	private static final int cGetData = 1;
	private static final int cOffLine = 2;
	
	private static final String NB_ELEMENT_OFFLINE = "10";
	private static final String NB_ELEMENT_ONLINE = "50";
	
	private Context pContext;
	
	private String leedURL = "";
	private String leedURLParam = "";
	private String login;
	private String password;
	
	private ArrayList<Flux> pFeeds;
	private int iterateurFeed;
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	public DataManagement(Context context)
	{
		pContext = context;
		
		offLineData = new LocalData(context);
        offLineData.open();
        
        connection = new APIConnection(context, this);
        
        getParameters();
        
        ((MainActivity)pContext).setOffLineButton(connectionType);
	}
	
	public void getParameters()
    {
		SharedPreferences settings = pContext.getSharedPreferences(PREFS_NAME, 0);
		String silent = settings.getString("url", "");
		leedURLParam = silent;
		leedURL = silent+"/plugins/api";
		
		login = settings.getString("login", "");
		password = settings.getString("password", "");
		connectionType = settings.getInt("connectionType", cOnLine);
		
		connection.SetDataConnection(leedURL, login, password);
    }
	
	public void saveConnectionType(int type)
	{
		SharedPreferences settings = pContext.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putInt("connectionType", type);
		
		connectionType = type;
		
		editor.commit();
	}
	
	public String getUrl()
	{
		return leedURLParam;
	}
	public String getLogin()
	{
		return login;
	}
	
	public void init()
	{
		switch(connectionType)
		{
			case cOnLine:
				connection.init();
			break;
			case cGetData:
				((MainActivity)pContext).initGetData();
				offLineData.init();
				connection.init();
			break;
			case cOffLine:
				updateCategories(offLineData.getAllFolders());
			break;
		}
	}
	public void getCategories()
    {
		switch(connectionType)
		{
			case cOnLine:
				connection.getCategories();
			break;
			case cGetData:
			break;
			case cOffLine:
				updateCategories(offLineData.getAllFolders());
			break;
		}
    	
    }
	public void getFeed(Flux feed)
	{
		switch(connectionType)
		{
			case cOnLine:
				connection.getFeed(feed, NB_ELEMENT_ONLINE, connectionType);
			break;
			case cGetData:
			break;
			case cOffLine:
				updateFeed(offLineData.getArticlesByFeed(feed.getId()));
			break;
		}
	}
	
	public void getArticle(String idArticle)
	{
		switch(connectionType)
		{
			case cOnLine:
				connection.getArticle(idArticle);
			break;
			case cGetData:
			break;
			case cOffLine:
				updateArticle(offLineData.getArticle(idArticle).getContent());
			break;
		}
	}
	
	public ArrayList<Folder> getFolders()
	{
		ArrayList<Folder> folders = new ArrayList<Folder>();
		
		folders = offLineData.getAllFolders();
		
		return folders;
	}
	
	public APIConnection getConnection()
    {
    	return connection;
    }

	public void changeConnectionMode()
	{
		switch(connectionType)
		{
			case cOnLine:
				setOffLineButton(cGetData);
			break;
			case cGetData:
				setOffLineButton(cOnLine);
			break;
			case cOffLine:
				setOffLineButton(cOnLine);
			break;
		}
	}
	
	public void updateCategories(ArrayList<Folder> folders)
	{
		switch(connectionType)
		{
			case cGetData:
				
				pFeeds = new ArrayList<Flux>();
				iterateurFeed = 0;
				
				for(int i = 0 ; i < folders.size() ; i++)
				{
					offLineData.addFolder(folders.get(i));
					
					Log.i("GetData", "GetFolder "+folders.get(i).getId());
					
					for(int j = 0 ; j < folders.get(i).getFlux().size() ; j++)
					{
						offLineData.addFeed(folders.get(i).getFlux().get(j));

						Log.i("GetData", "GetFeed "+folders.get(i).getFlux().get(j).getId());
						
						pFeeds.add(folders.get(i).getFlux().get(j));
					}
				}
				
				connection.getFeed(folders.get(0).getFlux().get(0), NB_ELEMENT_OFFLINE, connectionType);
				
			break;
			case cOnLine:
			case cOffLine:
				((MainActivity)pContext).updateCategories(folders);
			break;
		}
	}
	
	public void updateFeed(Flux feed)
	{
		switch(connectionType)
		{
			case cGetData:
				
				for(int i = 0 ; i < feed.getArticles().size() ; i++)
				{
					offLineData.addArticle(feed.getArticles().get(i));
					
					Log.i("GetData", "GetArticle "+feed.getArticles().get(i).getId() +" - Feed "+feed.getArticles().get(i).getIdFeed());
				}
				
				Log.i("SuiviFeed", iterateurFeed+1 +"/"+ pFeeds.size());
				
				((MainActivity)pContext).addTextGetData("("+(iterateurFeed+1) +"/"+ pFeeds.size()+") "+ pFeeds.get(iterateurFeed).getName());
				((MainActivity)pContext).setBarGetData(iterateurFeed+1, pFeeds.size());
				
				iterateurFeed ++;
				
				if(iterateurFeed == pFeeds.size())
					endGetData();
				else
					connection.getFeed(pFeeds.get(iterateurFeed), NB_ELEMENT_OFFLINE, connectionType);

			break;
			case cOnLine:
			case cOffLine:
				((MainActivity)pContext).updateFeed(feed);
			break;
		}
	}
	
	public void updateArticle(String content)
	{
		FeedAdapter.updateArticle(content);
	}
	
	public void endGetData()
	{
		setOffLineButton(cOffLine);
		((MainActivity)pContext).endGetData();
	}
	
	public void setOffLineButton(int lConnectionType)
	{
		connectionType = lConnectionType;
		saveConnectionType(lConnectionType);
		((MainActivity)pContext).setOffLineButton(lConnectionType);
	}
}