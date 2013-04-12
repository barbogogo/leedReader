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
    		   && objetbunble.containsKey("content")) 
       {
       		id = this.getIntent().getStringExtra("id");
       		title = this.getIntent().getStringExtra("title");
       		content = this.getIntent().getStringExtra("content");
       }
       else
       {
			title = "Error";
			content = "Toutes les données n'ont pus être téléchargées.";
       }
       
	   String customHtml = "<html><body><h1>"+id+" - "+title+"</h1><article>"+content+"</article></body></html>";
	   webView.loadData(customHtml, "text/html", "UTF-8");
	}
	
	@Override
    public void onBackPressed() 
    {
		super.onBackPressed();
    }
}