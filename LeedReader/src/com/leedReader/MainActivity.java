package com.leedReader;

import java.util.ArrayList;

import com.leed.reader.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 
public class MainActivity extends Activity 
{
	private ListView listView;
	private ProgressBar progressBar;
	private ProgressBar progressBarGetData;
	private TextView textGetData;
	
	private int posFolder = 0;
	
	/**
	 * Navigation automate
	 */
	private int posNavigation = 0;
	private static final int cpGlobal = 0;
	private static final int cpFolder = 1;
	private static final int cpFeed = 2;
	private static final int cpArticle = 3;
	
	private boolean settingFlag;
	
	private DataManagement dataManagement;
	
	private Button offLineButton;
	
	private static final int cOnLine  = 0;
	private static final int cGetData = 1;
	private static final int cOffLine = 2;
	
	public Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listView = (ListView)findViewById(R.id.ListViewId);
        offLineButton = (Button)findViewById(R.id.offLineButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBarGetData = (ProgressBar) findViewById(R.id.progressBarGetData);
        textGetData = (TextView) findViewById(R.id.textGetData);
        
        progressBarGetData.setVisibility(View.GONE);
        textGetData.setVisibility(View.GONE);
        
        posNavigation = cpGlobal;
        settingFlag = false;
        
        progressBar.setVisibility(ProgressBar.VISIBLE);
        
        context = this;
        
        Toast.makeText(this, "bienvenue",Toast.LENGTH_LONG).show();
        
        dataManagement = new DataManagement(context);
        
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
    		
	    	case R.id.allRead:
	    		Toast.makeText(this, "Fonction non disponible pour le moment", Toast.LENGTH_LONG).show();
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
	    		updateCategory(dataManagement.getFolders().get(posFolder));
	    	break;
	    		
	    	case cpArticle:
	    		posNavigation = cpFeed;
	    	break;
    	}
    }
    
    public void btnGetData(View view)
    {
    	progressBar.setVisibility(ProgressBar.VISIBLE);
    	
    	listView.removeAllViewsInLayout();
    	
    	dataManagement.getParameters();
    	
    	getCategories();
    }
    
    public void btnGetOffLineData(View view) throws InterruptedException
    {
    	progressBar.setVisibility(ProgressBar.VISIBLE);

    	listView.removeAllViewsInLayout();
    		
    	dataManagement.changeConnectionMode();
    	
	   	init();
    }
    	
	public void setOffLineButton(int connectionType)
    {
		switch(connectionType)
		{
			case cOnLine:
				offLineButton.setText("On\nLine");
				offLineButton.setTextColor(Color.parseColor("#00FF00"));
			break;
			case cGetData:
				offLineButton.setText("Get\nData");
				offLineButton.setTextColor(Color.parseColor("#FF0000"));
			break;
			case cOffLine:
				offLineButton.setText("Off\nLine");
				offLineButton.setTextColor(Color.parseColor("#000000"));
			break;
		}
    }
    
    public void init()
    {
    	dataManagement.init();
    }
    
    public void endGetData()
    {
    	progressBarGetData.setVisibility(View.GONE);
        textGetData.setVisibility(View.GONE);
        
        listView.setVisibility(View.VISIBLE);
    	
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	Toast.makeText(this, "Téléchargement terminé",Toast.LENGTH_LONG).show();
    	init();
    }
    
    public void getCategories()
    {
    	dataManagement.getCategories();
    }
    
    public void updateCategories(final ArrayList<Folder> folders)
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);
    	
    	ArrayList<String> items = new ArrayList<String>();
		
    	if(folders.size() > 0)
    	{
			for(int i = 0 ; i < folders.size() ; i ++)
			{
				items.add(folders.get(i).getTitle());
			}
	    	
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
		            	
		            	updateCategory(folders.get(position));
		            }
	    		}
	        );
    	}
    	else
    	{
    		Toast.makeText(context, "Pas de Catégories", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void updateCategory(final Folder folder)
    {
    	progressBar.setVisibility(ProgressBar.INVISIBLE);

    	FolderAdapter adapter = new FolderAdapter(this, folder);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(
        		new OnItemClickListener()
        		{
    	            @Override
    	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
    	            {        	            	
    	            	posNavigation = cpFeed;
    	            	
    	            	progressBar.setVisibility(ProgressBar.VISIBLE);
    	            	
//    	            	if(folder.getFeed(position).getNbArticles() == 0)
    	            		dataManagement.getFeed(folder.getFeed(position));
    	            }
        		}
            );
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
    
    public void erreurServeur(String msg, boolean showSetting)
    {
    	Toast.makeText(this, msg,Toast.LENGTH_LONG).show();

    	if(settingFlag == false && showSetting == true)
    	{
    	  	settings();
    	}
    }
    
    public void settings()
    {
		Bundle objetbunble = new Bundle();
		objetbunble.putString("url", dataManagement.getUrl());
		objetbunble.putString("login", dataManagement.getLogin());

		Intent intent = new Intent(this, SettingsActivity.class);
		
		intent.putExtras(objetbunble);
		
		startActivity(intent);
		
		settingFlag = true;
    }

    public APIConnection getConnection()
    {
    	return dataManagement.getConnection();
    }
    
    @Override
    public void onResume()
    {
    	// update data when parameters modification
    	if(settingFlag == true)
    	{
    		progressBar.setVisibility(ProgressBar.VISIBLE);
    		
    		listView.removeAllViewsInLayout();
    		
    		dataManagement.getParameters();
			init();
	    	settingFlag = false;
    	}
    	super.onResume();
    }
    
    public void initGetData()
    {
      progressBarGetData.setVisibility(View.VISIBLE);
      textGetData.setVisibility(View.VISIBLE);
      
      listView.setVisibility(View.GONE);
      
      textGetData.setText("Initialisation...");
    }
    
    public void addTextGetData(String pText)
    {
    	String text = (String) textGetData.getText();
    	
    	textGetData.setText(pText+"\n"+text);
    }
    
    public void setBarGetData(int value, int max)
    {
    	int maxValue = progressBarGetData.getMax();
    	
    	int progress = 0;
    	
    	if(max > 0)
    		progress = (value * maxValue) / max;
    	
    	progressBarGetData.setProgress(progress);
    }
}