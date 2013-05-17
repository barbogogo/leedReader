package com.leed.reader;

import java.util.ArrayList;
import org.json.JSONObject;
import android.util.Log;

public class Flux {

	private String idFlux;
	private String nameFlux;
	private String urlFlux;
	private int    nbNoRead;
	private String idCategory;
	
	private ArrayList<Article> articles = new ArrayList<Article>();
	
	public Flux(String jquery, String lIdFolder)
	{
		try
        {
			JSONObject jsonObject = new JSONObject(jquery);
			
			idFlux = jsonObject.getString("id");
			nameFlux = jsonObject.getString("name");
			urlFlux = jsonObject.getString("url");
			nbNoRead = jsonObject.getInt("nbNoRead");
			
			idCategory = lIdFolder;
        }
		catch (Exception e)
        {
            Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
        }
	}
	
	public Flux(){}
	
	public void setId(String lId)
	{
		idFlux = lId;
	}
	
	public String getId()
	{
		return idFlux;
	}
	
	public void setIdCategory(String lIdCategory)
	{
		idCategory = lIdCategory;
	}
	
	public String getIdCategory()
	{
		return idCategory;
	}

	public void setUrl(String lUrl)
	{
		urlFlux = lUrl;
	}
	
	public String getUrl()
	{
		return urlFlux;
	}
	
	public void setName(String lName)
	{
		nameFlux = lName;
	}
	
	public String getName()
	{
		return nameFlux;
	}
	
	public void addArticle(Article pArticle)
	{
		articles.add(pArticle);
	}
	
	public Article getArticle(int posArticle)
	{
		return articles.get(posArticle);
	}
	
	public ArrayList<Article> getArticles()
	{
		return articles;
	}
	
	public int getNbArticles()
	{
		return articles.size();
	}
	
	public ArrayList<String> getArticlesTitle()
	{
		ArrayList<String> titres = new ArrayList<String>();
		
		for(int i=0 ; i < articles.size() ; i++)
		{
			titres.add(articles.get(i).getTitle());
		}
		
		return titres;
	}
	
	public int getNbNoRead()
	{
		int pNbNoRead = nbNoRead;
		
		for(int i=0 ; i < articles.size() ; i++)
		{
			if(articles.get(i).getIsRead() == 1)
				pNbNoRead --;
		}
		
		return pNbNoRead;
	}
}
