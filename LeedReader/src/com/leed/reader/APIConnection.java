package com.leed.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class APIConnection 
{
	private String leedURL;
	private String login;
	private String password;
	
	private Context mainContext;
	
	private int errorServer;
	
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayList<String> nbNoRead = new ArrayList<String>();
	private ArrayList<String> idItems = new ArrayList<String>();
	private ArrayList<Folder> folders = new ArrayList<Folder>();
	
	private int typeRequest = 0;
	private static final int cFolder = 0;
	private static final int cFeed = 1;
	private static final int cArticle = 2;
	private static final int cInit = 3;
	private static final int cRead = 4;
	private static final int cFav = 5;
	
	public APIConnection(Context lContext)
	{
		mainContext = lContext;
	}
	
	public void SetDataConnection(String lUrl, String lLogin, String lPassword)
	{
		leedURL  = lUrl;
		login    = lLogin;
		password = lPassword;
	}
	
	public void init()
	{
		typeRequest = cInit;
		new ServerConnection().execute(leedURL+"/json.php?option=init");
	}
	
	public void getCategories()
	{
		typeRequest = cFolder;
		new ServerConnection().execute(leedURL+"/json.php?option=getFolders");
	}
	
	public void getFeed(String idFeed)
	{
		typeRequest = cFeed;
		new ServerConnection().execute(leedURL+"/json.php?option=flux&feedId="+idFeed);
	}
	
	public void getArticle(String idArticle)
	{
		typeRequest = cArticle;
		new ServerConnection().execute(leedURL+"/json.php?option=article&idArticle=" + idArticle);
	}
	
	public void setReadArticle(String idArticle)
	{
		typeRequest = cRead;
		new ServerConnection().execute(leedURL+"/json.php?option=setRead&idArticle=" + idArticle);
	}
	
	public void setUnReadArticle(String idArticle)
	{
		typeRequest = cRead;
		new ServerConnection().execute(leedURL+"/json.php?option=setUnRead&idArticle=" + idArticle);
	}
	
	public void setFavArticle(String idArticle)
	{
		typeRequest = cFav;
		new ServerConnection().execute(leedURL+"/json.php?option=setFavorite&idArticle=" + idArticle);
	}
	
	public void setUnFavArticle(String idArticle)
	{
		typeRequest = cFav;
		new ServerConnection().execute(leedURL+"/json.php?option=unsetFavorite&idArticle=" + idArticle);
	}
	
	public String readJSONFeed(String URL) 
    {
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);
        
        URI pUri = httpGet.getURI();
        String host = pUri.getHost();
        
        errorServer = 1;
        
        if(host != null)
        {
	        try
	        {
	            HttpResponse response = httpClient.execute(httpGet);
	            StatusLine statusLine = response.getStatusLine();
	            int statusCode = statusLine.getStatusCode();
	            if (statusCode == 200) 
	            {
	                HttpEntity entity = response.getEntity();
	                InputStream inputStream = entity.getContent();
	                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	                String line;
	                while ((line = reader.readLine()) != null) 
	                {
	                    stringBuilder.append(line);
	                }
	                inputStream.close();
	                
	                errorServer = 0;
	            }
	        }
	        catch (IOException e)
	        {
	        	
	        }
        }
        
        return stringBuilder.toString();
    }
	
	private class ServerConnection extends AsyncTask <String, Void, String> 
    {
    	
    	protected void onPreExecute() 
        {
    		// At the beginning we erase all data in items
    		
    		switch(typeRequest)
            {
            	case cArticle:
            	case cFeed:
            	case cRead:
            	case cFav:
            	break;
            		
            	case cInit:
            	case cFolder:
            	default:
		    		items.clear();
		    		nbNoRead.clear();
		    		idItems.clear();
		    		folders.clear();
	    		break;
            }
        }
    	
    	// String... permit to define a String array
    	protected String doInBackground(String... urls) 
        {
    		String loginEncoded;
    		String passwordEncoded;
    		try
    		{
        		loginEncoded = URLEncoder.encode(login, "UTF-8");
        		passwordEncoded = URLEncoder.encode(password, "UTF-8");
        	} 
    		catch (UnsupportedEncodingException e) 
    		{
    			loginEncoded="";
    			passwordEncoded="";
	    	}
    		
    		String url = urls[0]+"&login="+loginEncoded+"&password="+passwordEncoded;
    		
    		return readJSONFeed(url);
        }
 
        protected void onPostExecute(String result) 
        {
            try
            {             
            	JSONObject jsonObject;
            	
            	if(errorServer == 0)
        		{
            	
	                switch(typeRequest)
	                {
	                	case cInit:
	                		JSONObject json = new JSONObject(result);
	                	    JSONObject json2 = json.getJSONObject("error");
	                	    int idError = json2.getInt("id");
	                	    String msgError = json2.getString("message");
	                	    
	                	    if(idError == 0)
	                	    {
	                	    	getCategories();
	                	    }
	                	    else
	                	    {
	                	    	erreurServeur(msgError);
	                	    }
	                		
	                	break;
	                
	                	case cFeed:
	                		Flux feed = new Flux("");
	                		
                			jsonObject = new JSONObject(result);
                			
                			JSONArray  articlesItems = new JSONArray(jsonObject.getString("articles"));
	                        
	                        for (int i = 0; i < articlesItems.length(); i++) 
	                        {
	                            JSONObject postalCodesItem = articlesItems.getJSONObject(i);
	                            
	                            Article article = new Article(postalCodesItem.getString("id"));
	                            article.setTitle(postalCodesItem.getString("title"));
	                            article.setDate(postalCodesItem.getString("date"));
	                            article.setAuthor(postalCodesItem.getString("author"));
	                            article.setUrlArticle(postalCodesItem.getString("urlArticle"));
	                            article.setFav(postalCodesItem.getInt("favorite"));
	                            
	                            feed.addArticle(article);
	                        }
	                		
	                		updateFeed(feed);
	                	break;
                		
	                	case cArticle:
	                	           
    		                jsonObject = new JSONObject(result);
    		        		
    		        		String content = jsonObject.getString("content");
    		        		
    		                updateArticle(content);
	                		
	                	break;
	                	
	                	case cRead:
	                	case cFav:
	                		stateChanged();
	                	break;
	                	
	                	case cFolder:
	                	default:
	                		jsonObject = new JSONObject(result);
	                		
	                		JSONArray  foldersItems = new JSONArray (jsonObject.getString("folders"));
	                        
	                        //---print out the content of the json feed---
	                        for (int i = 0; i < foldersItems.length(); i++) 
	                        {
	                            JSONObject postalCodesItem = foldersItems.getJSONObject(i);
	                            
	                            items.add(postalCodesItem.getString("titre"));
	                            idItems.add(postalCodesItem.getString("id"));
	                            
	                            folders.add(new Folder(postalCodesItem.toString()));
	                        }
	                        
	                        updateData(items, folders);
	
	                	break;
	                }
        		}
        		else
        		{
        			erreurServeur("URL non valide, merci de la changer");
        		}
            } 
            catch (Exception e)
            {
                Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
            }
        }
    }
	
	private void erreurServeur(String msg)
    {
    	((MainActivity)mainContext).erreurServeur(msg);
    }
	
	private void updateData(ArrayList<String> items, final ArrayList<Folder> folders)
	{
		((MainActivity)mainContext).updateCategories(items, folders);
	}
	
	private void updateCategories(ArrayList<Folder> folders)
	{
		((MainActivity)mainContext).updateFeed(folders);
	}
	
	private void updateFeed(Flux feed)
	{
		((MainActivity)mainContext).updateFeed(feed);
	}
	
	private void updateArticle(String content)
	{
		FeedAdapter.updateArticle(content);
	}
	
	private void stateChanged()
	{
		FeedAdapter.stateChanged();
	}
}
