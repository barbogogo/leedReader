package com.barbogogo.leedreader;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.widget.LinearLayout;

public class WebviewAdapter extends PagerAdapter
{

    private ArrayList<Article> articles = new ArrayList<Article>();

    private Context            mainContext;

    public WebviewAdapter(ArrayList<Article> lArticles)
    {
        articles = lArticles;
    }

    // State number of pages
    @Override
    public int getCount()
    {
        return articles.size();
    }

    // Set each screen's content
    @Override
    public Object instantiateItem(View container, int position)
    {
        mainContext = container.getContext();

        // Add elements
        LinearLayout layout = setWebView(position);

        container.setTag(position);

        ((ViewPager) container).addView(layout);
        return layout;
    }

    public LinearLayout setWebView(int position)
    {
        LinearLayout layout = new LinearLayout(mainContext);

        WebView webView = new WebView(mainContext);

        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(PluginState.ON);
        settings.setDefaultTextEncodingName("utf-8");

        String content = regEx(articles.get(position).getContent());

        String customBody =
                "<h1><a href='" + articles.get(position).getUrlArticle() + "'>"
                        + articles.get(position).getTitle() + "</a></h1>" + "<hr>" + "<article>" + "<header>"
                        + "#" + articles.get(position).getId() + " - " + articles.get(position).getDate()
                        + " - " + articles.get(position).getAuthor() + " - "
                        + ((LeedReader) mainContext).getFeed(articles.get(position).getIdFeed()).getName()
                        + "</header>" + content + "</article>";

        String finalContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<html><head>"
                        + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
                        + styleHtml() + "<script type=\"text/javascript\">"
                        + "function showAndroidToast(toast)" + "{" + "Android.showToast(toast);" + "}"
                        + "</script>" + "<head><body>";
        finalContent += customBody + "</body></html>";

        webView.addJavascriptInterface(new WebAppInterface((LeedReader) mainContext), "Android");

        webView.loadData(finalContent, "text/html; charset=utf-8", "UTF-8");

        layout.addView(webView);

        return layout;
    }

    public String styleHtml()
    {
        String style = ((LeedReader) mainContext).styleHtml();

        return style;
    }

    private String regEx(String content)
    {
        String output = iFrameRegEx(content);
        output = pictureRegEx(output);

        return output;
    }

    private String iFrameRegEx(String content)
    {
        String pattern = "<iframe([^>]*)src=\"([^\"]*)\"([^>]*)></iframe>";

        Matcher matches2 = Pattern.compile(pattern).matcher(content);

        String[] fields = content.split(pattern);

        ArrayList<String> found = new ArrayList<String>();

        while (matches2.find())
        {
            String link = "";
            if(matches2.group(2).startsWith("//"))
                link = "http:" + matches2.group(2);
            else
                link = matches2.group(2);
            
            String replace = "<iframe " + matches2.group(1)+"src=\""+link+"\""+ matches2.group(3)+"></iframe>";
            replace += "<a onClick=\"showAndroidToast('" + link + "')\">";
            replace += "Ouvrir en externe</a>";

            found.add(replace);
        }

        String output = fields[0];

        for (int i = 1; i < fields.length; i++)
        {
            output += found.get(i - 1);
            output += fields[i];
        }

        return output;
    }

    private String pictureRegEx(String content)
    {
        String pattern = "<img([^>]*)title=\"([^\"]*)\"([^>]*)>";

        Matcher matches2 = Pattern.compile(pattern).matcher(content);

        String[] fields = content.split(pattern);

        ArrayList<String> found = new ArrayList<String>();

        while (matches2.find())
        {
            String replace = matches2.group();
            replace += "<div class=\"imageTitle\">" + matches2.group(2) + "</div>";

            found.add(replace);
        }

        String output = fields[0];

        for (int i = 1; i < fields.length; i++)
        {
            output += found.get(i - 1);
            output += fields[i];
        }

        return output;
    }

    public class WebAppInterface
    {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c)
        {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String url)
        {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ((LeedReader) mainContext).startActivity(viewIntent);
        }
    }

    @Override
    public void destroyItem(View collection, int position, Object o)
    {
        View view = (View) o;
        ((ViewPager) collection).removeView(view);
        view = null;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1)
    {
        return arg0 == ((View) arg1);
    }

    @Override
    public Parcelable saveState()
    {
        return null;
    }

    @Override
    public void startUpdate(View container)
    {
    }

    @Override
    public void finishUpdate(View container)
    {
    }
}
