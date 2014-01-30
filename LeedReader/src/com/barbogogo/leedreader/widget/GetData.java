package com.barbogogo.leedreader.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.barbogogo.leedreader.Utils;

import android.os.Build.VERSION;
import android.util.Log;

public class GetData
{
    private int                 cType        = 0;
    public static final int     cLogin       = 1;
    public static final int     cGetFeed     = 2;

    private String              text         = "coucou le monde !!";

    private String              pLogin       = "empty";
    private String              pPassWord    = "empty";
    private String              pUrlLeed     = "empty";

    private String              nbMaxArticle = "10";

    private static final String LOG          = WidgetClass.LOG;

    private String              userAgent    = "";

    private DefaultHttpClient   httpClient   = null;

    GetData(String urlLeed, String login, String password)
    {

        Log.i(LOG, login + "/" + password);

        if (urlLeed != null && login != null && password != null)
        {

            pLogin = login;
            pUrlLeed = urlLeed;

            try
            {
                pPassWord =
                        Utils.hex(MessageDigest.getInstance("SHA1").digest(
                                Utils.htmlspecialchars(password).getBytes()));
            }
            catch (NoSuchAlgorithmException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                pPassWord = "";
            }

            Log.i(LOG, pLogin + "/" + pPassWord);
        }

        userAgent = "Android-" + VERSION.RELEASE;
        httpClient = new DefaultHttpClient();
    }

    public void setData(String data)
    {
        text = data;
    }

    public String getData(int type)
    {
        // Create a new HttpClient and Post Header

        String url = "";

        switch (type)
        {
            case cLogin:
                Log.i(LOG, "Switch get url type : cLogin");
                url = pUrlLeed + "/plugins/api/login.php";
            break;
            case cGetFeed:
                Log.i(LOG, "Switch get url type : cGetFeed");
                url = pUrlLeed + "/plugins/api/json.php";
            break;
            default:
                Log.i(LOG, "Switch get url type : default");
                url = pUrlLeed + "/plugins/api/logout.php";
            break;
        }

        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        switch (type)
        {
            case cLogin:
                nameValuePairs.add(new BasicNameValuePair("login", pLogin));
                nameValuePairs.add(new BasicNameValuePair("password", pPassWord));
            break;
            case cGetFeed:
                nameValuePairs.add(new BasicNameValuePair("option", "getUnread"));
                nameValuePairs.add(new BasicNameValuePair("nbMaxArticle", nbMaxArticle));
            break;
            default:
            break;

        }

        Log.i(LOG, "url demandée : " + url);

        String fullUrl = getFullUrl(url, nameValuePairs);

        Log.i(LOG, "full url demandée : " + fullUrl);

        String response = readJSONFeed(fullUrl);

        Log.i(LOG, "réponse :" + String.valueOf(response));

        return String.valueOf(response);
    }

    public String readJSONFeed(String URL)
    {
        StringBuilder stringBuilder = new StringBuilder();
        HttpGet httpGet = new HttpGet(URL);
        httpGet.setHeader("User-Agent", this.userAgent);

        HttpParams httpParameters = new BasicHttpParams();

        // Define timeout to be connected
        int timeoutConnection = 10000;
        // Define timeout to receive data
        int timeoutSocket = 10000;

        // if (typeRequest == cSynchronize)
        // {
        // timeoutConnection = 0;
        // timeoutSocket = 0;
        // }

        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        httpClient.setParams(httpParameters);

        URI pUri = httpGet.getURI();
        String host = pUri.getHost();

        if (host != null)
        {
            Log.i(LOG, "valid host : " + host.toString());

            try
            {
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                Log.i(LOG, "statusCode : " + statusCode);

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
                if (statusCode == 401)
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
            }
            catch (IOException e)
            {
                Log.e(LOG, e.toString());
            }
        }
        else
        {
            Log.e(LOG, "Host == NULL");
        }

        return stringBuilder.toString();
    }

    private String getFullUrl(String url, List<NameValuePair> nameValuePairs)
    {
        String output = url + "?";

        for (int i = 0; i < nameValuePairs.size(); i++)
        {
            output += nameValuePairs.get(i).getName();
            output += "=";
            output += nameValuePairs.get(i).getValue();
            output += "&";
        }

        return output;
    }

}
