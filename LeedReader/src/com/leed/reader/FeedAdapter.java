package com.leed.reader;

import java.util.ArrayList;

import com.leed.reader.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FeedAdapter extends ArrayAdapter<String> 
{
	private static Context mainContext;
	
	private static ArrayList<Article> articles;
	
	private static int pPosition;

	final int pTitlePosition = 0;
	final int pNoReadPosition = 1;
	
	private static ProgressBar progressBar;
	
	private DataManagement dataManagement;
	
	public FeedAdapter(Context context, Flux feed)
	{
		super(context, R.layout.activity_main, feed.getArticlesTitle());
		
		articles = feed.getArticles();
		
		mainContext = context;
		
		progressBar = (ProgressBar) ((MainActivity)context).findViewById(R.id.progressBar1);
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		
		dataManagement = new DataManagement(context);
	}
 
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		LayoutInflater inflater = (LayoutInflater) mainContext
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View pRowView = inflater.inflate(R.layout.activity_feed, parent, false);
		TextView titleView = (TextView) pRowView.findViewById(R.id.articleTitle);
		TextView favoriteView = (TextView) pRowView.findViewById(R.id.favorite);
		TextView noReadView = (TextView) pRowView.findViewById(R.id.readNoRead);
		
		int isRead = articles.get(position).getIsRead();
		int isFav  = articles.get(position).getIsFav();
		
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
		
		if(isFav == 1)
		{
			favoriteView.setTextColor(Color.parseColor("#0000FF"));
			favoriteView.setText("fav");
		}
		else
		{
			favoriteView.setText("noFav");
		}
		
		noReadView.setTextSize(10);
		favoriteView.setTextSize(10);
		
		titleView.setOnClickListener(
				new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						progressBar.setVisibility(ProgressBar.VISIBLE);
						
						pPosition = position;
						
						Article article = articles.get(pPosition);
						
						article.setRead();
//						dataManagement.setReadArticle(article);
						
						notifyDataSetChanged();
						
						dataManagement.getArticle(articles.get(position).getId());
					}
				});
		
		noReadView.setOnClickListener(
				new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						progressBar.setVisibility(ProgressBar.VISIBLE);
						
						pPosition = position;
						
						Article article = articles.get(pPosition);
						
						if(article.getIsRead() == 0)
						{
							article.setRead();
							dataManagement.setReadArticle(article);
						}
						else
						{
							article.setUnRead();
							dataManagement.setUnReadArticle(article);
						}
						
						notifyDataSetChanged();
					}
				});
		
		favoriteView.setOnClickListener(
				new View.OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						progressBar.setVisibility(ProgressBar.VISIBLE);
						
						pPosition = position;
						
						Article article = articles.get(pPosition);
						
						notifyDataSetChanged();
						
						if(article.getIsFav() == 0)
						{
							article.setFav(1);
							dataManagement.setFavArticle(article);
						}
						else
						{
							article.setFav(0);
							dataManagement.setUnFavArticle(article);
						}
					}
				});
		
		return pRowView;
	}
	
	public static void updateArticle(String content)
    {
		Article article = articles.get(pPosition);
	    
        article.setContent(content);
    	
		Bundle objetbunble = new Bundle();
		objetbunble.putString("id", article.getId());
		objetbunble.putString("title", article.getTitle());
		objetbunble.putString("content", article.getContent());
		objetbunble.putString("date", article.getDate());
		objetbunble.putString("author", article.getAuthor());
		objetbunble.putString("urlArticle", article.getUrlArticle());
    	
		Intent intent = new Intent(mainContext, WebviewActivity.class);
		
		intent.putExtras(objetbunble);
		
		mainContext.startActivity(intent);

		progressBar.setVisibility(ProgressBar.INVISIBLE);
    }
	
	public static void stateChanged()
	{
		progressBar.setVisibility(ProgressBar.INVISIBLE);
	}
}
