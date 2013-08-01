package com.barbogogo.leedreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.security.MessageDigest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONArray;
import org.json.JSONObject;

import com.leed.reader.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.util.Log;
import android.content.pm.PackageManager;

public class APIConnection 
{
	private String leedURL;
	
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
	private static final int cHomePage = 6;

	private DefaultHttpClient httpClient;
	private String            userAgent;
	
	private Flux pFeed;
	private Article pArticle;
	
	public APIConnection(Context lContext, DataManagement lDataContext)
	{
		mainContext = lContext;

		dataContext = lDataContext;

		// create connection object
		httpClient = new DefaultHttpClient();

		String version = "unknown";
		try 
		{
			version = lContext.getPackageManager().getPackageInfo(lContext.getPackageName(), 0).versionName;
		} 
		catch(PackageManager.NameNotFoundException e) 
		{
		}

		userAgent  = "Android-" + VERSION.RELEASE + "/LeedReader-" + version;
	}
	
	public void SetDataConnection(String lUrl, String lLogin, String lPassword) throws java.net.URISyntaxException, java.security.NoSuchAlgorithmException
	{
		leedURL  = lUrl;

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
	
	public void getHomePage(String nbMaxArticle)
	{
		typeRequest = cHomePage;
		pFeed = new Flux();
		new ServerConnection().execute(leedURL+"/json.php?option=getUnread"+"&nbMaxArticle="+nbMaxArticle);
	}
	
	public void getCategories()
	{
		typeRequest = cFolder;
		new ServerConnection().execute(leedURL+"/json.php?option=getFolders");
	}
	
	public void getFeed(Flux feed, String nbMaxArticle, int connectionType)
	{
		typeRequest = cFeed;
		pFeed = feed;
		new ServerConnection().execute(leedURL+"/json.php?option=flux&feedId="+feed.getId()+"&nbMaxArticle="+nbMaxArticle+"&connectionType="+connectionType);
	}
	
	public void getArticle(Article article)
	{
		typeRequest = cArticle;
		pArticle = article;
		new ServerConnection().execute(leedURL+"/json.php?option=article&idArticle=" + article.getId());
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
		
		HttpParams httpParameters = new BasicHttpParams();
		// Define timeout to be connected
		int timeoutConnection = 10000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		// Define timeout to receive data
		int timeoutSocket = 10000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		httpClient.setParams(httpParameters);
		
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
	            if(statusCode == 401)
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
	        	Log.i("LeedReaderConnection", e.getLocalizedMessage());
//	        	erreurServeur(e.getLocalizedMessage(), false);
	        }
        }
        else
        	serverError = cServerError;
        
        return stringBuilder.toString();
    }
	
	private class ServerConnection extends AsyncTask <String, Void, String> 
    {
    	
    	protected void onPreExecute() 
        {
    		serverError = cServerError;
    		
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
            	case cHomePage:
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
	                			endInit();
	                		}
	                		else
	                		{
	                			erreurServeur(msgError, true);
	                		}
	                		
	                	break;
	                
	                	case cFeed:
	                	case cHomePage:
	                		
                			jsonObject = new JSONObject(result);
                			
                			JSONArray  articlesItems = new JSONArray(jsonObject.getString("articles"));
	                        
                			pFeed.deleteAllArticles();
                			
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
		                            
		                            pFeed.addArticle(article);
	                            }
	                        }
	                		
	                		updateFeed(pFeed);
	                	break;
                		
	                	case cArticle:
	                	           
    		                jsonObject = new JSONObject(result);
    		        		
    		        		String content = jsonObject.getString("content");
    		        		
    		        		pArticle.setContent(content);
    		        		
    		                updateArticle(pArticle);
	                		
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
                    		erreurServeur(mainContext.getResources().getString(R.string.msg_nointernet_connexion), false);
                    	break;
                    	case cServerError:
                    		erreurServeur(mainContext.getResources().getString(R.string.msg_bad_url), true);
        	    		break;
                    }
        		}
            }
            catch (Exception e)
            {
                Log.i("LeedReaderGetData", e.getLocalizedMessage());
                erreurServeur(e.getLocalizedMessage(), false);
            }
        }
    }
	
	public void endInit()
	{
		((DataManagement)dataContext).getHomePage();
	}
	
	public void erreurServeur(String msg, boolean showSetting)
    {
    	((LeedReader)mainContext).erreurServeur(msg, showSetting);
    }
	
	private void updateData(final ArrayList<Folder> folders)
	{
		((DataManagement)dataContext).updateCategories(folders);
	}
	
	private void updateFeed(Flux feed)
	{
		((DataManagement)dataContext).updateFeed(feed);
	}
	
	private void updateArticle(Article article)
	{
		((DataManagement)dataContext).updateArticle(article);
	}
	
	private void stateChanged()
	{
//		FeedAdapter.stateChanged();
	}
	
	private boolean isNetworkAvailable() 
	{
	    ConnectivityManager connectivityManager = 
	    		(ConnectivityManager)((LeedReader)mainContext).getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
