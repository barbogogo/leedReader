package com.leed.reader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
public class WebviewActivity extends Activity  
{
	
	private WebView webView;
	
	private String id;
	private String title;
	private String content;
	private String date;
	private String author;
	private String urlArticle;
 
	public void onCreate(Bundle savedInstanceState) 
	{
	   super.onCreate(savedInstanceState);
	   setContentView(R.layout.webview);
 
	   webView = (WebView) findViewById(R.id.webView1);
	   // webView.getSettings().setJavaScriptEnabled(true);
	   // webView.loadUrl("http://www.google.com");
	   
       Bundle objetbunble  = this.getIntent().getExtras();
 
       if (objetbunble != null 
    		   && objetbunble.containsKey("id") 
    		   && objetbunble.containsKey("title")
    		   && objetbunble.containsKey("content")
    		   && objetbunble.containsKey("date") 
    		   && objetbunble.containsKey("author")
    		   && objetbunble.containsKey("urlArticle")) 
       {
       		id = this.getIntent().getStringExtra("id");
       		title = this.getIntent().getStringExtra("title");
       		content = this.getIntent().getStringExtra("content");
       		date = this.getIntent().getStringExtra("date");
       		author = this.getIntent().getStringExtra("author");
       		urlArticle = this.getIntent().getStringExtra("urlArticle");
       }
       else
       {
			title = "Error";
			content = "Toutes les données n'ont pus être téléchargées.";
       }
       
	   String customBody = 
			   "<h1><a href='"+urlArticle+"'>"+title+"</a></h1>"+
			   "<hr>"+
			   "<article>"+
			   "<header>"+
			   "#"+id+" - "+date+" - "+author+
			   "</header>"+
			   content+
			   "</article>";
	   
	   String content = 
		       "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       "<html><head>"+
		       "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"+
		       styleHtml()+
		       "<head><body>";
		content += customBody + "</body></html>";

		webView.loadData(content, "text/html; charset=utf-8", "UTF-8");
	}
	
	@Override
    public void onBackPressed() 
    {
		super.onBackPressed();
    }
	
	private String styleHtml()
	{
		String style = 
				"<style type='text/css'>"+
				"h1 {color: navy; font-size:14px;}"+
				"h1 a {text-decoration:none;}"+
				"article {color: black; font-size:12px;}"+
				"hr {color: #EEE; background-color: #EEE; height: 1px;}"+
				"</style>";
	    
	    return style;
	}
}