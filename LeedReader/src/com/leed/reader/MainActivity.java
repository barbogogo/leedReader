package com.leed.reader;

import java.util.ArrayList;
 
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends Activity 
{
	private ListView listView;
	private ProgressBar progressBar;
	
	private int posFolder = 0;
	
	/**
	 * Navigation automate
	 */
	private int posNavigation = 0;
	private static final int cpGlobal = 0;
	private static final int cpFolder = 1;
	private static final int cpFeed = 2;
	private static final int cpArticle = 3;
	
	private ArrayList<Folder> pFolders = new ArrayList<Folder>();
	
	public static final String PREFS_NAME = "MyPrefsFile";
	
	private String leedURL = "";
	private String leedURLParam = "";
	private String login;
	private String password;

	private boolean settingFlag;
	
	public Context context;
	
	private APIConnection connection;
 
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listView = (ListView)findViewById(R.id.ListViewId);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        
        posNavigation = cpGlobal;
        settingFlag = false;
        
        progressBar.setVisibility(ProgressBar.VISIBLE);
        
        context = this;
        
        Toast.makeText(this, "bienvenue",Toast.LENGTH_LONG).show();
        
        connection = new APIConnection(context);
        
        getParameters();
        
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
	    		getCategories();
    		break;
    		
	    	case cpFeed:
	    		posNavigation = cpFolder;
	    		updateFeed(pFolders);
	    	break;
	    		
	    	case cpArticle:
	    		posNavigation = cpFeed;
	    	break;
    	}
    }
    
    public void btnGetInformation(View view)
    {
    	progressBar.setVisibility(ProgressBar.VISIBLE);
    	
    	getParameters();
    	
    	getCategories();
    }
    
    public void init()
    {
    	// connection test
        connection.init();
    }
    
    public void getCategories()
    {
        connection.getCategories();
    }
    
    public void updateCategories(ArrayList<String> items, final ArrayList<Folder> folders)
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	MobileArrayAdapter adapter = new MobileArrayAdapter(this, items, folders);
        listView.setAdapter(adapter);
        
        pFolders = folders;
        
        listView.setOnItemClickListener(
    		new OnItemClickListener() 
    		{
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	            {
	            	progressBar.setVisibility(ProgressBar.VISIBLE);
	            	
	            	posFolder = position;

	            	posNavigation = cpFolder;
	            	
	            	updateFeed(folders);
	            }
    		}
        );
    }
    
    public void updateFeed(final ArrayList<Folder> folders)
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	if(folders.size() == 0)
    	{
    		getCategories();
    	}
    	else
    	{
	    	FolderAdapter adapter = new FolderAdapter(this, folders.get(posFolder));
	        listView.setAdapter(adapter);
	        
	        listView.setOnItemClickListener(
	        		new OnItemClickListener()
	        		{
	    	            @Override
	    	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    	            {        	            	
	    	            	posNavigation = cpFeed;
	    	            	
	    	            	progressBar.setVisibility(ProgressBar.VISIBLE);
	    	            	
	    	            	String idFeed = folders.get(posFolder).getFlux(position).getId();
	    	            	
	    	            	if(folders.get(posFolder).getFlux(position).getNbArticles() == 0)
	    	            		connection.getFeed(idFeed);
	    	            }
	        		}
	            );
    	}
    }
    
    public void updateFeed(Flux feed)
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	FeedAdapter adapter = new FeedAdapter(this, feed);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(
        		new OnItemClickListener()
        		{
    	            @Override
    	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	            {   
    	            	// do nothing
    	            }
        		}
        		);
    }
    
    public void erreurServeur(String msg)
    {
    	Toast.makeText(this, msg,Toast.LENGTH_LONG).show();

    	settings();
    }
    
    public void settings()
    {
		Bundle objetbunble = new Bundle();
		objetbunble.putString("url", leedURLParam);
		objetbunble.putString("login", login);

		Intent intent = new Intent(this, SettingsActivity.class);
		
		intent.putExtras(objetbunble);
		
		startActivity(intent);
		
		settingFlag = true;
    }
    
    public void getParameters()
    {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String silent = settings.getString("url", "");
		leedURLParam = silent;
		leedURL = silent+"/plugins/api";
		
		login = settings.getString("login", "");
		password = settings.getString("password", "");
		
		connection.SetDataConnection(leedURL, login, password);
    }

    public APIConnection getConnection()
    {
    	return connection;
    }
    
    @Override
    public void onResume()
    {
    	// update data when parameters modification
    	if(settingFlag == true)
    	{
    		progressBar.setVisibility(ProgressBar.VISIBLE);
    		
    		getParameters();
	    	init();
	    	settingFlag = false;
    	}
    	super.onResume();
    }
 
}