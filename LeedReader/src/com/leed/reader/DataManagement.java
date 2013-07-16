package com.leed.reader;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class DataManagement 
{
	private APIConnection connection;
	private LocalData DBData;
	
	private int connectionType;
	private static final int cOnLine  = 0;
	private static final int cGetData = 1;
	private static final int cOffLine = 2;
	private static final int cSendData = 3;
	
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
		
		DBData = new LocalData(context);
		DBData.open();
        
        connection = new APIConnection(context, this);
        
        getParameters();
        
        ((LeedReader)pContext).setOffLineButton(connectionType);
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
		
		try 
		{
			connection.SetDataConnection(leedURL, login, password);
		} catch (Exception e) {}
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
				DBData.init();
				connection.init();
			break;
			case cGetData:
				((LeedReader)pContext).initGetData();
				DBData.init();
				connection.init();
			break;
			case cOffLine:
				updateCategories(DBData.getAllFolders());
			break;
			case cSendData:
				((LeedReader)pContext).initGetData();
				sendData();
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
				updateCategories(DBData.getAllFolders());
			break;
		}
    }
	public void getCategory(Folder lFolder)
    {
		switch(connectionType)
		{
			case cOnLine:
				updateCategory(lFolder);
			break;
			case cGetData:
			break;
			case cOffLine:
				Folder folder = new Folder();
				
				ArrayList<Flux> feeds = DBData.getFeedByCategory(lFolder.getId());
				
				for(int i = 0 ; i < feeds.size() ; i++)
				{
					folder.addFeed(feeds.get(i));
				}
				
				updateCategory(folder);
			break;
		}
    }
	public void getFeed(Flux feed)
	{
		switch(connectionType)
		{
			case cOnLine:
				//TODO: trouver comment charger les articles au fur et à mesure
//				connection.getFeed(feed, NB_ELEMENT_ONLINE, connectionType);
				connection.getFeed(feed, NB_ELEMENT_ONLINE, cGetData);
			break;
			case cGetData:
			break;
			case cOffLine:
				updateFeed(DBData.getArticlesByFeed(feed.getId()));
			break;
		}
	}
	
	public void getArticle(Article article)
	{
		switch(connectionType)
		{
			case cOnLine:
				connection.getArticle(article);
			break;
			case cGetData:
			break;
			case cOffLine:
//				updateArticle(DBData.getArticle(idArticle).getContent());
			break;
		}
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
			break;
			case cOffLine:
				setOffLineButton(cSendData);
			break;
			case cSendData:
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
					DBData.addFolder(folders.get(i));
					
					Log.i("GetData", "GetFolder "+folders.get(i).getId());
					
					for(int j = 0 ; j < folders.get(i).getFlux().size() ; j++)
					{
						DBData.addFeed(folders.get(i).getFlux().get(j));

						Log.i("GetData", "GetFeed "+folders.get(i).getFlux().get(j).getId());
						
						pFeeds.add(folders.get(i).getFlux().get(j));
					}
				}
				
				connection.getFeed(folders.get(0).getFlux().get(0), NB_ELEMENT_OFFLINE, connectionType);
				
			break;
			case cOnLine:
			case cOffLine:
				((LeedReader)pContext).updateCategories(folders);
			break;
		}
	}
	
	public void updateCategory(Folder folder)
	{
		switch(connectionType)
		{
			case cGetData:
			break;
			case cOnLine:
			case cOffLine:
				((LeedReader)pContext).updateCategory(folder);
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
					DBData.addArticle(feed.getArticles().get(i));
					
					Log.i("GetData", "GetArticle "+feed.getArticles().get(i).getId() +" - Feed "+feed.getArticles().get(i).getIdFeed());
				}
				
				Log.i("SuiviFeed", iterateurFeed+1 +"/"+ pFeeds.size());
				
				((LeedReader)pContext).addTextGetData("("+(iterateurFeed+1) +"/"+ pFeeds.size()+") "+ pFeeds.get(iterateurFeed).getName());
				((LeedReader)pContext).setBarGetData(iterateurFeed+1, pFeeds.size());
				
				iterateurFeed ++;
				
				if(iterateurFeed == pFeeds.size())
					endGetData();
				else
					connection.getFeed(pFeeds.get(iterateurFeed), NB_ELEMENT_OFFLINE, connectionType);

			break;
			case cOnLine:
			case cOffLine:
				((LeedReader)pContext).updateFeed(feed);
			break;
		}
	}
	
	public void updateArticle(Article article)
	{
		FeedAdapter.updateArticle(article);
	}
	
	public void endGetData()
	{
		setOffLineButton(cOffLine);
		((LeedReader)pContext).endGetData();
	}
	
	public void setOffLineButton(int lConnectionType)
	{
		connectionType = lConnectionType;
		saveConnectionType(lConnectionType);
		((LeedReader)pContext).setOffLineButton(lConnectionType);
	}
	
	public void sendData()
	{
		ArrayList<Article> articles = new ArrayList<Article>();
		
		articles = DBData.getAllArticles();
		
		for(int i = 0 ; i < articles.size() ; i ++)
		{
			((LeedReader)pContext).addTextGetData(String.valueOf(i));
			
			if(Integer.parseInt(articles.get(i).getId()) == 3226)
			{
				connection.setReadArticle(articles.get(i).getId());
			}
			
			if(articles.get(i).getIsRead() == 1)
			{
				connection.setReadArticle(articles.get(i).getId());
			}
		}
		for(int i = 0 ; i < articles.size() ; i ++)
		{
			((LeedReader)pContext).addTextGetData(String.valueOf(i));
			if(articles.get(i).getIsFav() == 1)
			{
				connection.setFavArticle(articles.get(i).getId());
			}
		}
		setOffLineButton(cOnLine);
		((LeedReader)pContext).endGetData();
	}
	
	public void setReadArticle(Article article, int type)
	{		
		if(connectionType == cOnLine)
		{
			if(type == 1)
				connection.setReadArticle(article.getId());
		}
		if(connectionType == cOffLine)
		{
			DBData.setReadArticle(article);
		}
	}
	public void setUnReadArticle(Article article)
	{		
		if(connectionType == cOnLine)
		{
			connection.setUnReadArticle(article.getId());
		}
		if(connectionType == cOffLine)
		{
			DBData.setUnReadArticle(article);
		}
	}
	
	public void setFavArticle(Article article)
	{		
		if(connectionType == cOnLine)
		{
			connection.setFavArticle(article.getId());
		}
		if(connectionType == cOffLine)
		{
			DBData.setFavArticle(article);
		}
	}
	public void setUnFavArticle(Article article)
	{		
		if(connectionType == cOnLine)
		{
			connection.setUnFavArticle(article.getId());
		}
		if(connectionType == cOffLine)
		{
			DBData.setUnFavArticle(article);
		}
	}
}