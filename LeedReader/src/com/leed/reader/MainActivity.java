package com.leed.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends Activity 
{
	private ListView listView;
	private WebView webView;
	private ProgressBar progressBar;
	
	private int typeRequest = 0;
	private static final int cFolder = 0;
	private static final int cFeed = 1;
	private static final int cArticle = 2;
	private static final int cInit = 3;
	
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayList<String> nbNoRead = new ArrayList<String>();
	private ArrayList<String> idItems = new ArrayList<String>();
	
	/**
	 * Folder list
	 */
	private ArrayList<Folder> folders = new ArrayList<Folder>();
	/**
	 * used folder position
	 */
	private int posFolder  = 0;
	private int posFeed    = 0;
	private int posArticle = 0;
	
	/**
	 * Navigation automate
	 */
	private int posNavigation = 0;
	private static final int cpGlobal = 0;
	private static final int cpFolder = 1;
	private static final int cpFeed = 2;
	private static final int cpArticle = 3;
	
	private ArrayList<String> folderItems = new ArrayList<String>();
	
	private final String fileName = "settings.dat";
	public static final String PREFS_NAME = "MyPrefsFile";
	
	private String leedURL = "";
	private String leedURLParam = "";
	private String login;
	private String password;
	
	private int errorServer;

	private boolean settingFlag;
	
	public Context context;
	
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
 
    private class ReadWeatherJSONFeedTask extends AsyncTask <String, Void, String> 
    {
    	
    	protected void onPreExecute() 
        {
    		// At the beginning we erase all data in items
    		
    		switch(typeRequest)
            {
            	case cArticle:
            	break;
            	
            	case cFeed:
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
    		int activateRequest = 1;
    		
    		String url = urls[0]+"&login="+login+"&password="+password;
    		
    		switch(typeRequest)
            {
            	case cArticle:
            		activateRequest = 1;
            	break;
            	
            	case cFeed:
            		if(folders.get(posFolder).getFlux(posFeed).getNbArticles() > 0)
            		{ // If articles loaded, no request
            			activateRequest = 0;
            		}
            	break;
            	
            	case cInit:
            	default:
            		activateRequest = 1;
            }
    		
    		if(activateRequest == 1)
    			return readJSONFeed(url);
    		else
    			return "{noData}";
    		
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
	                	    	updateData();
	                	    }
	                	    else
	                	    {
	                	    	Toast.makeText(context, msgError, Toast.LENGTH_LONG).show();
	                	    	progressBar.setVisibility(ProgressBar.INVISIBLE);
	                	    	erreurServeur();
	                	    }
	                		
	                	break;
	                
	                	case cFeed:
	                		if(folders.get(posFolder).getFlux(posFeed).getNbArticles() == 0)
	                		{ // Only if there is no articles loaded
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
		                            
		                            folders.get(posFolder).getFlux(posFeed).addArticle(article);
		                        }
	                		}
	                		
	                		updateFeed();
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
	                        
	                        updateGlobalFolder();
	
	                	break;
		                }
	        		}
                	
            		else
            		{
            			erreurServeur();
            		}
            } 
            catch (Exception e)
            {
                Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
            }
        }
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listView = (ListView)findViewById(R.id.ListViewId);
        webView  = (WebView) findViewById(R.id.webView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        
        posNavigation = cpGlobal;
        settingFlag = false;
        
        progressBar.setVisibility(ProgressBar.VISIBLE);
        
        context = this;
        
        Toast.makeText(this, "bienvenue",Toast.LENGTH_LONG).show();
        
//        ReadSettings(this);
        readURL();
        
        init();
    }
 
    public boolean onCreateOptionsMenu(Menu menu) 
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
    	return true;
     }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
    	{
	    	case R.id.quitter:
	    		finish();
    		break;
    		
	    	case R.id.settings:
	    		settings();
	    	break;
    	}
    	
    	return false;
    }
    
    @Override
    public void onBackPressed() 
    {		
    	switch(posNavigation)
    	{
	    	case cpGlobal:
	    	default:
	    		super.onBackPressed();
    		break;
    		
	    	case cpFolder:
	    		posNavigation = cpGlobal;
	    		updateGlobalFolder();
    		break;
    		
	    	case cpFeed:
	    		posNavigation = cpFolder;
	    		updateFolder();
	    	break;
	    		
	    	case cpArticle:
	    		posNavigation = cpFeed;
	    	break;
    	}
    }
    
    public void btnGetInformation(View view)
    {
    	progressBar.setVisibility(ProgressBar.VISIBLE);
    	
//    	ReadSettings(this);
    	readURL();
    	
    	updateData();
    }
    
    public void init()
    {
    	// Permet de tester la connexion
    	typeRequest = cInit;
        new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?a=a");
    }
    
    public void updateData()
    {
    	typeRequest = cFolder;
        new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?option=getFolders");
    }
    
    public void updateGlobalFolder()
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	MobileArrayAdapter adapter = new MobileArrayAdapter(this, items, folders);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(
    		new OnItemClickListener() 
    		{
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	            {
	            	progressBar.setVisibility(ProgressBar.VISIBLE);
	            	
	            	posFolder = position;

	            	posNavigation = cpFolder;
	            	
	            	folderItems.clear();
	            	folderItems.addAll(folders.get(posFolder).getTitleFeeds());

	            	updateFolder();
	            }
    		}
        );
    }
    
    public void updateFolder()
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	FolderAdapter adapter = new FolderAdapter(this, folders.get(posFolder));
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(
        		new OnItemClickListener()
        		{
    	            @Override
    	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	            {    
    	            	posFeed = position;
    	            	
    	            	posNavigation = cpFeed;
    	            	
    	            	progressBar.setVisibility(ProgressBar.VISIBLE);
    	            	
    	            	typeRequest = cFeed;
    	                new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?option=flux&feedId=" + folders.get(posFolder).getFlux(position).getId());
    	            }
        		}
            );
    }
    
    public void updateFeed()
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	FeedAdapter adapter = new FeedAdapter(this, folders.get(posFolder).getFlux(posFeed), leedURL);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(
        		new OnItemClickListener()
        		{
    	            @Override
    	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	            {   
    	            	// Lorsque l'on clique, on ne fait rien
    	            }
        		}
        		);
    }
    
    public void settings()
    {
		Bundle objetbunble = new Bundle();
		objetbunble.putString("url", leedURLParam);
		objetbunble.putString("login", login);
		objetbunble.putInt("errorServer", errorServer);

		Intent intent = new Intent(this, SettingsActivity.class);
		
		intent.putExtras(objetbunble);
		
		startActivity(intent);
		
		settingFlag = true;
    }
    
    public String ReadSettings(Context context)
    { 
    	FileInputStream fIn = null; 
 
    	String data = ""; 

    	File file = context.getFileStreamPath(fileName);
    	
    	try
    	{
    		if(file.exists())
    		{
	    	    fIn = context.openFileInput(fileName);
	    	    
				byte[] input = new byte[fIn.available()];
				while (fIn.read(input) != -1) {}
				data += new String(input);
	    	    
	    	    leedURL = data;
    		}
    	} 
    	catch (Exception e)
    	{
    		Log.d("debug", e.getMessage());
    	}
    	finally
    	{
    	    try
    	    { 
    	    	if(file.exists())
        		{
    	    		fIn.close();
        		}
    	    } 
    	    catch (IOException e) 
    	    { 
    	    	Log.d("debug", e.getMessage());
    	    } 
    	}
    	return data; 
    }
    
    public void readURL()
    {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String silent = settings.getString("url", "");
		leedURLParam = silent;
		leedURL = silent+"/plugins/api";
		
		login = settings.getString("login", "");
		password = settings.getString("password", "");
    }
    
    public void erreurServeur()
    {
    	settings();
    }

    @Override
    public void onResume()
    {
    	// Permet de mettre à jour les données suite à la modification de l'URL
    	if(settingFlag == true)
    	{
    		progressBar.setVisibility(ProgressBar.VISIBLE);
    		
//    		ReadSettings(this);
    		readURL();
	    	init();
	    	settingFlag = false;
    	}
    	super.onResume();
    }
    
}