package com.barbogogo.leedreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build.VERSION;

import com.leed.reader.R;

public class APIConnection
{
    private String            leedURL;
    private String            leedURLParam;
    private String            leedLogin;
    private String            leedPassword;

    private int               authMode;
    private static final int  cAuthDigest   = 0;
    private static final int  cAuthBasic    = 1;

    private Context           mainContext;
    private DataManagement    dataContext;

    private int               serverError;
    private static final int  cNoError      = 0;
    private static final int  cNetworkError = 1;
    private static final int  cServerError  = 2;
    private static final int  cPHPError     = 3;
    private static final int  cAuthError    = 4;
    private static final int  cAPIDisabled  = 5;
    private static final int  cConnectErr   = 6;

    private ArrayList<String> items         = new ArrayList<String>();
    private ArrayList<String> nbNoRead      = new ArrayList<String>();
    private ArrayList<String> idItems       = new ArrayList<String>();
    private ArrayList<Folder> folders       = new ArrayList<Folder>();

    private int               typeRequest   = 0;
    private static final int  cFolder       = 0;
    private static final int  cFeed         = 1;
    private static final int  cArticle      = 2;
    private static final int  cInit         = 3;
    private static final int  cRead         = 4;
    private static final int  cFav          = 5;
    private static final int  cHomePage     = 6;
    private static final int  cSynchronize  = 7;
    private static final int  cFeedRead     = 8;
    private static final int  cAllRead      = 9;
    private static final int  cOffsetFeed   = 10;
    private static final int  cCheckVersion = 11;

    private DefaultHttpClient httpClient;
    private String            userAgent;

    private Flux              pFeed;
    private Article           pArticle;

    private String            errorMessage;

    private String            mVersion;

    private int               pNbAuth       = 0;
    private int               pNbAuthMax    = 3;

    public APIConnection(Context lContext, DataManagement lDataContext)
    {

        mainContext = lContext;

        dataContext = lDataContext;

        // create connection object
        httpClient = new DefaultHttpClient();

        mVersion = "unknown";
        try
        {
            mVersion = lContext.getPackageManager().getPackageInfo(lContext.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
        }

        userAgent = "Android-" + VERSION.RELEASE + "/LeedReader-" + mVersion;

        errorMessage = "";
    }

    public void SetDataConnection(String lUrl, String lLogin, String lPassword, String lAuthMode)
            throws java.net.URISyntaxException, java.security.NoSuchAlgorithmException
    {
        leedURLParam = lUrl;
        leedURL = lUrl + "/plugins/api";
        leedLogin = lLogin;

        if (lAuthMode.equals("0"))
        {
            authMode = cAuthDigest;
        }
        else
        {
            authMode = cAuthBasic;
        }

        URI uri = new URI(lUrl);
        String SHA1Pwd =
                Utils.hex(MessageDigest.getInstance("SHA1").digest(
                        Utils.htmlspecialchars(lPassword).getBytes()));

        leedPassword = SHA1Pwd;

        if (authMode == cAuthDigest)
        {
            httpClient.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(lLogin, SHA1Pwd));
        }
    }

    public void checkVersion()
    {
        typeRequest = cCheckVersion;
        new ServerConnection().execute("http://checkLeedReaderVersion.barbogogo.fr/index.json");
    }

    public void init()
    {
        typeRequest = cInit;
        if (authMode == cAuthDigest)
        {
            new ServerConnection().execute(leedURL + "/login.php");
        }
        else
        {
            new ServerConnection().execute(leedURL + "/login.php?&login=" + leedLogin + "&password="
                    + leedPassword);
        }
    }

    public void getHomePage(String nbMaxArticle)
    {
        typeRequest = cHomePage;
        pFeed = new Flux();
        new ServerConnection().execute(leedURL + "/json.php?option=getUnread" + "&nbMaxArticle="
                + nbMaxArticle);
    }

    public void getCategories(String mShowEmptyFeeds)
    {
        typeRequest = cFolder;
        if (mShowEmptyFeeds.equals("0"))
            new ServerConnection().execute(leedURL + "/json.php?option=getUnreadFolders");
        else
            new ServerConnection().execute(leedURL + "/json.php?option=getFolders");
    }

    public void getFeed(Flux feed, String nbMaxArticle, int connectionType)
    {
        typeRequest = cFeed;
        pFeed = feed;
        pFeed.deleteAllArticles();
        new ServerConnection().execute(leedURL + "/json.php?option=flux&feedId=" + feed.getId()
                + "&nbMaxArticle=" + nbMaxArticle + "&connectionType=" + connectionType);
    }

    public void getOffsetFeed(Flux feed, int offset, String nbMaxArticle, int connectionType)
    {
        typeRequest = cOffsetFeed;
        pFeed = feed;

        if (feed.getId() == null)
            new ServerConnection().execute(leedURL + "/json.php?option=getUnread&nbMaxArticle="
                    + nbMaxArticle + "&offset=" + String.valueOf(offset));
        else
            new ServerConnection().execute(leedURL + "/json.php?option=flux&feedId=" + feed.getId()
                    + "&nbMaxArticle=" + nbMaxArticle + "&offset=" + String.valueOf(offset)
                    + "&connectionType=" + connectionType);
    }

    public void getArticle(Article article)
    {
        typeRequest = cArticle;
        pArticle = article;
        new ServerConnection().execute(leedURL + "/json.php?option=article&idArticle=" + article.getId());
    }

    public void setReadArticle(String idArticle)
    {
        typeRequest = cRead;
        new ServerConnection().execute(leedURL + "/json.php?option=setRead&idArticle=" + idArticle);
    }

    public void setUnReadArticle(String idArticle)
    {
        typeRequest = cRead;
        new ServerConnection().execute(leedURL + "/json.php?option=setUnRead&idArticle=" + idArticle);
    }

    public void setFavArticle(String idArticle)
    {
        typeRequest = cFav;
        new ServerConnection().execute(leedURL + "/json.php?option=setFavorite&idArticle=" + idArticle);
    }

    public void setUnFavArticle(String idArticle)
    {
        typeRequest = cFav;
        new ServerConnection().execute(leedURL + "/json.php?option=unsetFavorite&idArticle=" + idArticle);
    }

    public void synchronize()
    {
        typeRequest = cSynchronize;
        new ServerConnection().execute(leedURLParam + "/action.php?action=synchronize");
    }

    public void setReadFeed(String idFeed)
    {
        typeRequest = cFeedRead;
        new ServerConnection().execute(leedURL + "/json.php?option=setFeedRead&idFeed=" + idFeed);
    }

    public void setAllRead()
    {
        typeRequest = cAllRead;
        new ServerConnection().execute(leedURL + "/json.php?option=setAllRead");
    }

    public String readJSONFeed(String URL)
    {
        errorMessage = "";

        StringBuilder stringBuilder = new StringBuilder();
        HttpGet httpGet = new HttpGet(URL);
        httpGet.setHeader("User-Agent", this.userAgent);

        HttpParams httpParameters = new BasicHttpParams();

        // Define timeout to be connected
        int timeoutConnection = 20000;
        // Define timeout to receive data
        int timeoutSocket = 20000;

        if (typeRequest == cSynchronize)
        {
            timeoutConnection = 0;
            timeoutSocket = 0;
        }

        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        httpClient.setParams(httpParameters);

        URI pUri = httpGet.getURI();
        String host = pUri.getHost();

        if (host != null && serverError != cNetworkError)
        {
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

                    serverError = cNoError;
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

                    serverError = cNoError;
                }
            }
            catch (IOException e)
            {
                if (e.toString().equals("java.net.SocketTimeoutException"))
                    errorMessage =
                            "<h1>APIConnection Error</h1><p>"
                                    + mainContext.getResources().getString(
                                            R.string.msg_SocketTimeoutException) + "</p>";
                else
                    errorMessage = "<h1>APIConnection Error</h1><p>" + e.toString() + "</p>";

                serverError = cConnectErr;
            }
        }
        else
        {
            serverError = cServerError;
        }

        return stringBuilder.toString();
    }

    private class ServerConnection extends AsyncTask<String, Void, String>
    {

        protected void onPreExecute()
        {
            serverError = cServerError;

            // we check if a network is available, if not error message
            if (!isNetworkAvailable())
            {
                serverError = cNetworkError;
            }

            // At the beginning we erase all data in items

            switch (typeRequest)
            {
                case cArticle:
                case cFeed:
                case cRead:
                case cFav:
                case cSynchronize:
                case cFeedRead:
                case cAllRead:
                case cOffsetFeed:
                case cCheckVersion:
                break;

                case cInit:
                case cFolder:
                case cHomePage:
                default:
                    items.clear();
                    nbNoRead.clear();
                    idItems.clear();
                    folders.clear();
                break;
            }
        }

        // String... permit to define a String array
        protected String doInBackground(String... urls)
        {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result)
        {
            try
            {
                JSONObject jsonObject;

                try
                {
                    JSONObject json = new JSONObject(result);
                    JSONObject json2 = json.getJSONObject("error");
                    int idError = json2.getInt("id");
                    String msgError = json2.getString("message");

                    switch (idError)
                    {
                        case 0: // 0: No error
                            serverError = cNoError;
                        break;
                        case 1: // 1: API Disabled
                            serverError = cAPIDisabled;
                        break;
                        case 2: // 2: Login failed
                            erreurServeur(msgError, false);
                            serverError = cAuthError;
                        break;
                        case 3: // 3: PHP Error
                            if (pNbAuth < pNbAuthMax)
                            {
                                init();
                            }
                            else
                            {
                                erreurServeur(msgError, false);
                                serverError = cPHPError;
                            }
                        break;
                        default:
                            serverError = cServerError;
                        break;
                    }
                }
                catch (Exception e)
                {
                }

                if (serverError == cNoError)
                {
                    switch (typeRequest)
                    {
                        case cCheckVersion:

                            JSONObject json = new JSONObject(result);
                            JSONObject json2 = json.getJSONObject("checkVersion");
                            String version = json2.getString("version");
                            String link = json2.getString("link");

                            int retour = Utils.versionCompare(mVersion, version);

                            endCheckVersion(version, link, retour);

                        break;

                        case cInit:

                            endInit();

                        break;

                        case cFeed:
                        case cHomePage:
                        case cOffsetFeed:

                            jsonObject = new JSONObject(result);

                            JSONArray articlesItems = new JSONArray(jsonObject.getString("articles"));

                            for (int i = 0; i < articlesItems.length(); i++)
                            {
                                JSONObject postalCodesItem = articlesItems.getJSONObject(i);

                                Article article = new Article(postalCodesItem.getString("id"));

                                if (postalCodesItem.getInt("id") > 0)
                                {
                                    article.setTitle(postalCodesItem.getString("title"));
                                    article.setDate(postalCodesItem.getString("date"));
                                    article.setAuthor(postalCodesItem.getString("author"));
                                    article.setUrlArticle(postalCodesItem.getString("urlArticle"));
                                    article.setFav(postalCodesItem.getInt("favorite"));
                                    article.setContent(postalCodesItem.getString("content"));
                                    article.setIdFeed(postalCodesItem.getString("idFeed"));
                                    article.setNameFeed(postalCodesItem.getString("nameFeed"));
                                    article.setUrlFeed(postalCodesItem.getString("urlFeed"));

                                    pFeed.addArticle(article);
                                }
                            }

                            updateFeed(pFeed);
                        break;

                        case cArticle:

                            jsonObject = new JSONObject(result);

                            String content = jsonObject.getString("content");

                            pArticle.setContent(content);

                            updateArticle(pArticle);

                        break;

                        case cRead:
                        case cFav:

                        break;

                        case cFeedRead:
                            ((LeedReader) mainContext).init();
                        break;
                        case cAllRead:
                            ((LeedReader) mainContext).init();
                        break;

                        case cFolder:
                            jsonObject = new JSONObject(result);

                            JSONArray foldersItems = new JSONArray(jsonObject.getString("folders"));

                            // ---print out the content of the json feed---
                            for (int i = 0; i < foldersItems.length(); i++)
                            {
                                JSONObject postalCodesItem = foldersItems.getJSONObject(i);

                                folders.add(new Folder(postalCodesItem.toString()));
                            }

                            updateData(folders);

                        break;

                        case cSynchronize:
                            synchronisationResult(result);
                        break;
                    }
                }
                else
                {
                    switch (serverError)
                    {
                        case cNetworkError:
                            erreurServeur(
                                    mainContext.getResources().getString(R.string.msg_nointernet_connexion),
                                    false);
                        break;
                        case cServerError:
                            erreurServeur(mainContext.getResources().getString(R.string.msg_bad_url), true);
                        break;
                        case cConnectErr:
                            erreurServeur(errorMessage, false);
                        break;
                        case cPHPError:
                        // Done previously
                        break;
                        case cAuthError:
                        // erreurServeur(
                        // mainContext.getResources().getString(R.string.msg_bad_authentication),
                        // false);
                        break;
                        case cAPIDisabled:
                            erreurServeur(mainContext.getResources().getString(R.string.msg_api_disabled),
                                    false);
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                erreurServeur("<h1>APIConnection Erreur #3</h1>" + e.getLocalizedMessage(), false);
            }
        }
    }

    public void endCheckVersion(String version, String link, int retour)
    {
        ((LeedReader) mainContext).endCheckVersion(version, link, retour);
    }

    public void endInit()
    {
        ((DataManagement) dataContext).getHomePage();
    }

    public void erreurServeur(String msg, boolean showSetting)
    {
        ((LeedReader) mainContext).erreurServeur(msg, showSetting);
    }

    public void synchronisationResult(String msg)
    {
        ((LeedReader) mainContext).synchronisationResult(msg);
    }

    private void updateData(final ArrayList<Folder> folders)
    {
        ((DataManagement) dataContext).updateCategories(folders);
    }

    private void updateFeed(Flux feed)
    {
        ((DataManagement) dataContext).updateFeed(feed);
    }

    private void updateArticle(Article article)
    {
        ((DataManagement) dataContext).updateArticle(article);
    }

    private boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) ((LeedReader) mainContext)
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
