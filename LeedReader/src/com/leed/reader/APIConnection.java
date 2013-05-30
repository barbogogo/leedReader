package com.leed.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.security.MessageDigest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;


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

	private DefaultHttpClient httpClient;
	private String            userAgent;
	
	public APIConnection(Context lContext)
	{
		mainContext = lContext;
		// create connection object
		httpClient = new DefaultHttpClient();

		String version = "unknown";
		try {
			version = lContext.getPackageManager().getPackageInfo(lContext.getPackageName(), 0).versionName;
		} catch(PackageManager.NameNotFoundException e) {
		}

		userAgent  = "Android-" + VERSION.RELEASE + "/LeedReader-" + version;
	}
	
	public void SetDataConnection(String lUrl, String lLogin, String lPassword) throws java.net.URISyntaxException, java.security.NoSuchAlgorithmException
	{
		leedURL  = lUrl;
		login    = lLogin;
		password = lPassword;

		URI uri = new URI(lUrl);
		String SHA1Pwd = Utils.hex(MessageDigest.getInstance("SHA1").digest(lPassword.getBytes()));

		httpClient.getCredentialsProvider().setCredentials(
			new AuthScope(uri.getHost(), uri.getPort()),
			new UsernamePasswordCredentials(lLogin, SHA1Pwd)
		);
	}

	public void init()
	{
		typeRequest = cInit;
		new ServerConnection().execute(leedURL+"/login.php");
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
        HttpGet httpGet = new HttpGet(URL);
		httpGet.setHeader("User-Agent", this.userAgent);
        
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
            return readJSONFeed(urls[0]);
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
                            if(errorServer == 0)
	                	    {
	                	    	getCategories();
	                	    }
	                	    else
	                	    {
                                erreurServeur("login failed");
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
