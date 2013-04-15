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
       
	   String customBody = "<h1>"+id+" - "+title+"</h1><article>"+content+"</article>";
	   
	   String content = 
		       "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       "<html><head>"+
		       "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"+
		       "<head><body>";
		content += customBody + "</body></html>";

		webView.loadData(content, "text/html; charset=utf-8", "UTF-8");
	}
	
	@Override
    public void onBackPressed() 
    {
		super.onBackPressed();
    }
}