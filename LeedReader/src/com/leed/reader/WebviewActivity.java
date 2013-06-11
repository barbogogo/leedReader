package com.leed.reader;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.Toast;
 
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
	   
	   WebSettings settings = webView.getSettings();
	   settings.setUseWideViewPort(false);
	   settings.setLoadWithOverviewMode(false);
	   settings.setJavaScriptEnabled(true);
	   settings.setPluginState(PluginState.ON);
	   
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
       
       content = testRegEx(content);
       
	   String customBody = 
			   "<h1><a href='"+urlArticle+"'>"+title+"</a></h1>"+
			   "<hr>"+
			   "<article>"+
			   "<header>"+
			   "#"+id+" - "+date+" - "+author+
			   "</header>"+
			   content+
			   "</article>";
	   
	   String finalContent = 
		       "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       "<html><head>"+
		       "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"+
		       styleHtml()+
	   		   "<script type=\"text/javascript\">"+
	   		   "function showAndroidToast(toast)"+ 
	   		   "{"+
	   		   "Android.showToast(toast);"+
	   		   "}"+
	   		   "</script>"+
		       "<head><body>";
	   finalContent += customBody + "</body></html>";

	   webView.addJavascriptInterface(new WebAppInterface(this), "Android");
	   
	   webView.loadData(finalContent, "text/html; charset=utf-8", "UTF-8");
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
				"img {max-width:100%;height:auto;}"+
				"iframe {max-width:100%;height:auto;}"+
				"</style>";
	    
	    return style;
	}
	
	private String testRegEx(String content)
	{	
		String pattern = "<iframe([^>]*)src=\"([^\"]*)\"([^>]*)></iframe>";
		
		Matcher matches2 = Pattern.compile(pattern).matcher(content);
		
		String[] fields = content.split(pattern);
		
		ArrayList<String> found = new ArrayList<String>();
		
		while(matches2.find())
		{
			Log.i("RegEx", matches2.group());
			Log.i("RegEx", "Lien trouvé : "+matches2.group(2));
			
			String replace = matches2.group();
			replace += "<a onClick=\"showAndroidToast('"+matches2.group(2)+"')\">";
			replace += "Voir la vidéo dans l'app youtube</a>";
			
			found.add(replace);
		}
		
		String output = fields[0];
		
		for(int i = 1 ; i < fields.length ; i++)
		{
			output += found.get(i-1);
			output += fields[i];
		}
		
		return output;
	}

	public class WebAppInterface 
	{
	    Context mContext;

	    /** Instantiate the interface and set the context */
	    WebAppInterface(Context c) {
	        mContext = c;
	    }

	    /** Show a toast from the web page */
	    @JavascriptInterface
	    public void showToast(String url) 
	    {
	    	Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    	startActivity(viewIntent);
	    }
	}
	
}