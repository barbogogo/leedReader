package com.leed.reader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FeedAdapter extends ArrayAdapter<String> 
{
	private final Context context;
	
	private final ArrayList<Article> articles;
	
	int pPosition;
	
	int aPosition = 0;
	final int pTitlePosition = 0;
	final int pNoReadPosition = 1;
	
	private String leedURL;
	
	private View pRowView;
 
	public FeedAdapter(Context context, Flux feed, String pLeedURL)
	{
		super(context, R.layout.activity_main, feed.getArticlesTitle());
		
		articles = feed.getArticles();
		
		this.context = context;
		leedURL = pLeedURL;
	}
 
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		pRowView = inflater.inflate(R.layout.activity_feed, parent, false);
		TextView titleView = (TextView) pRowView.findViewById(R.id.articleTitle);
		TextView favoriteView = (TextView) pRowView.findViewById(R.id.favorite);
		TextView noReadView = (TextView) pRowView.findViewById(R.id.readNoRead);
		
		int isRead = articles.get(position).getIsRead();
		
		titleView.setText(articles.get(position).getTitle());
		
		favoriteView.setText("");
		favoriteView.setWidth(0);
		
		if(isRead == 1)
		{
			noReadView.setText("lu");
		}
		else
		{
			titleView.setTypeface(null, Typeface.BOLD);
			noReadView.setText("noLu");
		}
		
		noReadView.setTextSize(10);
		
		titleView.setOnClickListener(
				new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						aPosition = pTitlePosition;
						
						pPosition = position;
						
						new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?option=article&idArticle=" + articles.get(position).getId());
					}
				});
		
		noReadView.setOnClickListener(
				new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						aPosition = pNoReadPosition;
						
						pPosition = position;
						
						Article article = articles.get(pPosition);
						
						if(article.getIsRead() == 0)
						{
							article.setRead();
							new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?option=setRead&idArticle=" + articles.get(position).getId());
						}
						else
						{
							article.setUnRead();
							new ReadWeatherJSONFeedTask().execute(leedURL+"/json.php?option=setUnRead&idArticle=" + articles.get(position).getId());
						}
					}
				});
		
		return pRowView;
	}
	
	 public String readJSONFeed(String URL) 
	    {
	        StringBuilder stringBuilder = new StringBuilder();
	        HttpClient httpClient = new DefaultHttpClient();
	        HttpGet httpGet = new HttpGet(URL);
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
	            } 
	            else 
	            {
	                Log.d("JSON", "Failed to download file");
	            }
	        } 
	        catch (Exception e)
	        {
	            Log.d("readJSONFeed", e.getLocalizedMessage());
	        }
	        
	        return stringBuilder.toString();
	    }
	
	private class ReadWeatherJSONFeedTask extends AsyncTask <String, Void, String> 
    {    	
    	// String... permit to define a String array
    	@Override
		protected String doInBackground(String... urls) 
        {
    		return readJSONFeed(urls[0]);
        }
 
        @Override
		protected void onPostExecute(String result) 
        {
        	switch(aPosition)
        	{
        		case pNoReadPosition:
        			((MainActivity) context).updateFeed();        			
        		break;
        	
	        	case pTitlePosition:
	        	default:
	        	
		            try
		            {             
		            	JSONObject jsonObject;
		            	
		                jsonObject = new JSONObject(result);
		        		
		        		String content = jsonObject.getString("content");
		        		
		        		Article article = articles.get(pPosition);
		                article.setContent(content);
		        		
		                updateArticle();
		
		            } 
		            catch (Exception e)
		            {
		                Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
		            }
		            
		        break;
        	}
        }
    }
	
	public void updateArticle()
    {
		Article article = articles.get(pPosition);
	    
    	article.setRead();
    	
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", article.getId());
		objetbunble.putString("title", article.getTitle());
		objetbunble.putString("content", article.getContent());
    	
		Intent intent = new Intent(context, WebviewActivity.class);
		
		intent.putExtras(objetbunble);
		
		context.startActivity(intent);
		
		((MainActivity) context).updateFeed();

    }	
}
