package com.leedReader;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class APIConnection 
{
	private String leedURL;
	private String login;
	private String password;
	
	private Context mainContext;
	private DataManagement dataContext;
	
	private int serverError;
	private static final int cNoError = 0;
	private static final int cNetworkError = 1;
	private static final int cServerError = 2;
	
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
	
	public APIConnection(Context lContext, DataManagement lDataContext)
	{
		mainContext = lContext;
		dataContext = lDataContext;
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
	
	public void getFeed(Flux feed, String nbMaxArticle, int connectionType)
	{
		typeRequest = cFeed;
		new ServerConnection().execute(leedURL+"/json.php?option=flux&feedId="+feed.getId()+"&nbMaxArticle="+nbMaxArticle+"&connectionType="+connectionType);
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
        
        if(host != null && serverError != cNetworkError)
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
	                
	                serverError = cNoError;
	            }
	        }
	        catch (IOException e)
	        {
	        	Log.w("LeedReaderConnection", e.getLocalizedMessage());
	        	erreurServeur(e.getLocalizedMessage(), false);
	        }
        }
        
        return stringBuilder.toString();
    }
	
	private class ServerConnection extends AsyncTask <String, Void, String> 
    {
    	
    	protected void onPreExecute() 
        {
    		// we check if a network is available, if not error message    		
    		if(!isNetworkAvailable())
    		{
    			serverError = cNetworkError;
    		}
    		
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
            	
            	if(serverError == cNoError)
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
	                	    	erreurServeur(msgError, true);
	                	    }
	                		
	                	break;
	                
	                	case cFeed:
	                		
	                		Flux feed = new Flux();
	                		
                			jsonObject = new JSONObject(result);
                			
                			JSONArray  articlesItems = new JSONArray(jsonObject.getString("articles"));
	                        
	                        for (int i = 0; i < articlesItems.length(); i++)
	                        {
	                            JSONObject postalCodesItem = articlesItems.getJSONObject(i);
	                            
	                            Article article = new Article(postalCodesItem.getString("id"));
	                            
	                            if(postalCodesItem.getInt("id") > 0)
	                            {
	                            	article.setTitle(postalCodesItem.getString("title"));
		                            article.setDate(postalCodesItem.getString("date"));
		                            article.setAuthor(postalCodesItem.getString("author"));
		                            article.setUrlArticle(postalCodesItem.getString("urlArticle"));
		                            article.setFav(postalCodesItem.getInt("favorite"));
		                            article.setContent(postalCodesItem.getString("content"));
		                            article.setIdFeed(postalCodesItem.getString("idFeed"));
	                            }

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
	                            
	                            folders.add(new Folder(postalCodesItem.toString()));
	                        }
	                        
	                        updateData(folders);
	
	                	break;
	                }
        		}
        		else
        		{
        			switch(serverError)
                    {		
                    	case cNetworkError:
                    		erreurServeur("Pas de connexion internet", false);
                    	break;
                    	case cServerError:
                    		erreurServeur("URL non valide, merci de la changer", true);
        	    		break;
                    }
        		}
            }
            catch (Exception e)
            {
                Log.w("LeedReaderGetData", e.getLocalizedMessage());
                erreurServeur(e.getLocalizedMessage(), false);
            }
        }
    }
	
	public void erreurServeur(String msg, boolean showSetting)
    {
    	((MainActivity)mainContext).erreurServeur(msg, showSetting);
    }
	
	private void updateData(final ArrayList<Folder> folders)
	{
		((DataManagement)dataContext).updateCategories(folders);
	}
	
	private void updateFeed(Flux feed)
	{
		((DataManagement)dataContext).updateFeed(feed);
	}
	
	private void updateArticle(String content)
	{
		((DataManagement)dataContext).updateArticle(content);
	}
	
	private void stateChanged()
	{
		FeedAdapter.stateChanged();
	}
	
	private boolean isNetworkAvailable() 
	{
	    ConnectivityManager connectivityManager = 
	    		(ConnectivityManager)((MainActivity)mainContext).getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
