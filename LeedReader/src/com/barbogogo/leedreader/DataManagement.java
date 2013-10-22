package com.barbogogo.leedreader;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DataManagement
{
    private APIConnection       connection;
    private LocalData           DBData;

    private int                 connectionType;
    private static final int    cOnLine            = 0;
    private static final int    cGetData           = 1;
    private static final int    cOffLine           = 2;
    private static final int    cSendData          = 3;

    private static final String NB_ELEMENT_OFFLINE = "10";
    private static final String NB_ELEMENT_ONLINE  = "20";

    private Context             pContext;

    private String              leedURL            = "";
    private String              login;
    private String              password;
    private String              authMode;
    private String              showEmptyFeeds;

    private ArrayList<Folder>   pFolders;
    private ArrayList<Flux>     pFeeds;
    private Flux                pFeed;
    private int                 iterateurFeed;

    public static final String  PREFS_NAME         = "MyPrefsFile";

    public DataManagement(Context context)
    {
        pContext = context;

        DBData = new LocalData(context);
        DBData.open();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(pContext);

        connectionType = Integer.valueOf(settings.getString("connectionType", "0"));
    }

    public void getParameters()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(pContext);

        leedURL = settings.getString("serverLinkPref", "");

        login = settings.getString("usernamePref", "");
        password = settings.getString("passwordPref", "");
        authMode = settings.getString("authenticationType", "1");
        showEmptyFeeds = settings.getString("showEmptyFeeds", "0");

        int connectionType_old = connectionType;
        // int connectionType_new = settings.getInt("connectionType", cOnLine);
        int connectionType_new = Integer.valueOf(settings.getString("connectionType", "1"));

        if (connectionType_old != connectionType_new)
        {
            if (connectionType_old == cOnLine && connectionType_new == cOffLine)
            {
                connectionType = cGetData;
            }
            else
                if (connectionType_old == cOffLine && connectionType_new == cOnLine)
                {
                    connectionType = cSendData;
                }
                else
                {
                    connectionType = cOnLine;
                }
        }
        else
        {
            connectionType = connectionType_new;
        }

        if (leedURL.equals("") || login.equals("") || password.equals(""))
        {
            ((LeedReader) pContext).noParameterInformation();
        }
        else
        {
            try
            {
                connection = new APIConnection(pContext, this);
                connection.SetDataConnection(leedURL, login, password, authMode);
            }
            catch (Exception e)
            {
            }
        }
    }

    public void saveConnectionType(int type)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(pContext);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("connectionType", String.valueOf(type));

        connectionType = type;

        editor.commit();
    }

    public String getUrl()
    {
        return leedURL;
    }

    public String getLogin()
    {
        return login;
    }

    public String getPassWord()
    {
        return password;
    }

    public String getShowEmptyFeeds()
    {
        return showEmptyFeeds;
    }

    public void checkVersion()
    {
        if (connectionType == cOnLine)
        {
            connection.checkVersion();
        }
        else
        {
            init();
        }
    }

    public void init()
    {
        switch (connectionType)
        {
            case cOnLine:
                DBData.init();
                connection.init();
            break;
            case cGetData:
                ((LeedReader) pContext).initGetData();
                DBData.init();
                connection.init();
            break;
            case cOffLine:
                updateCategories(DBData.getAllFolders());
                getHomePage();
            break;
            case cSendData:
                ((LeedReader) pContext).initGetData();
                connection.init();
                sendData();
            break;
        }
    }

    public void getCategories()
    {
        switch (connectionType)
        {
            case cOnLine:
                connection.getCategories(showEmptyFeeds);
            break;
            case cGetData:
            break;
            case cOffLine:
                updateCategories(DBData.getAllFolders());
            break;
        }
    }

    public void getCategory(Folder lFolder)
    {
        switch (connectionType)
        {
            case cOnLine:
                updateCategory(lFolder);
            break;
            case cGetData:
            break;
            case cOffLine:
                Folder folder = new Folder();

                ArrayList<Flux> feeds = DBData.getFeedByCategory(lFolder.getId());

                for (int i = 0; i < feeds.size(); i++)
                {
                    folder.addFeed(feeds.get(i));
                }

                updateCategory(folder);
            break;
        }
    }

    public void getFeed(Flux feed)
    {
        switch (connectionType)
        {
            case cOnLine:
                connection.getFeed(feed, NB_ELEMENT_ONLINE, cGetData);
            break;
            case cGetData:
            break;
            case cOffLine:
                updateFeed(DBData.getArticlesByFeed(feed));
            break;
        }
    }

    public void getOffsetFeed(Flux feed, int offset)
    {
        switch (connectionType)
        {
            case cOnLine:
                connection.getOffsetFeed(feed, offset, NB_ELEMENT_ONLINE, cGetData);
            break;
            case cGetData:
            break;
            case cOffLine:
            // updateFeed(DBData.getArticlesByFeed(feed.getId()));
            break;
        }
    }

    public void getArticle(Article article)
    {
        switch (connectionType)
        {
            case cOnLine:
                connection.getArticle(article);
            break;
            case cGetData:
            break;
            case cOffLine:
            // TODO: Ajouter mode offline
            // updateArticle(DBData.getArticle(idArticle).getContent());
            break;
        }
    }

    public APIConnection getConnection()
    {
        return connection;
    }

    public void getHomePage()
    {
        switch (connectionType)
        {
            case cGetData:
                connection.getCategories(showEmptyFeeds);
            break;
            case cOnLine:
                connection.getHomePage("20");
            break;
            case cOffLine:
                ((LeedReader) pContext).updateFeed(DBData.getHomePage());
            break;
        }
    }

    public void updateCategories(ArrayList<Folder> folders)
    {

        pFolders = folders;

        switch (connectionType)
        {
            case cGetData:

                pFeeds = new ArrayList<Flux>();
                iterateurFeed = 0;

                for (int i = 0; i < folders.size(); i++)
                {
                    DBData.addFolder(folders.get(i));

                    Log.i("GetData", "GetFolder " + folders.get(i).getId());

                    for (int j = 0; j < folders.get(i).getFlux().size(); j++)
                    {
                        DBData.addFeed(folders.get(i).getFlux().get(j));

                        Log.i("GetData", "GetFeed " + folders.get(i).getFlux().get(j).getId());

                        pFeeds.add(folders.get(i).getFlux().get(j));
                    }
                }

                connection.getFeed(folders.get(0).getFlux().get(0), NB_ELEMENT_OFFLINE, connectionType);

            break;
            case cOnLine:
            case cOffLine:
                ((LeedReader) pContext).updateCategories(folders);
            break;
        }
    }

    public void updateCategory(Folder folder)
    {
        switch (connectionType)
        {
            case cGetData:
            break;
            case cOnLine:
            case cOffLine:
            // ((LeedReader)pContext).updateCategory(folder);
            break;
        }
    }

    public void updateFeed(Flux feed)
    {
        pFeed = feed;

        switch (connectionType)
        {
            case cGetData:

                if (feed.getArticles().size() > 0)
                {
                    for (int i = 0; i < feed.getArticles().size(); i++)
                    {
                        DBData.addArticle(feed.getArticles().get(i));

                        Log.i("GetData", "GetArticle " + i + " - " + feed.getArticles().get(i).getId()
                                + " - Feed " + feed.getArticles().get(i).getIdFeed());
                    }
                }

                Log.i("SuiviFeed", iterateurFeed + 1 + "/" + pFeeds.size());

                ((LeedReader) pContext).addTextGetData("(" + (iterateurFeed + 1) + "/" + pFeeds.size() + ") "
                        + pFeeds.get(iterateurFeed).getName());

                iterateurFeed++;

                if (iterateurFeed == pFeeds.size())
                {
                    saveConnectionType(cOffLine);
                    endGetData();
                }
                else
                    connection.getFeed(pFeeds.get(iterateurFeed), NB_ELEMENT_OFFLINE, connectionType);

            break;
            case cOnLine:
            case cOffLine:
                ((LeedReader) pContext).updateFeed(feed);
            break;
        }
    }

    public void synchronize()
    {
        switch (connectionType)
        {
            case cGetData:
            break;
            case cOffLine:
            break;
            case cOnLine:
                connection.synchronize();
            break;
        }
    }

    public void updateArticle(Article article)
    {
        // FeedAdapter.updateArticle(article);
    }

    public void endGetData()
    {
        // setOffLineButton(cOffLine);
        ((LeedReader) pContext).endGetData();
    }

    public void sendData()
    {
        ArrayList<Article> articles = new ArrayList<Article>();

        articles = DBData.getAllArticles();

        for (int i = 0; i < articles.size(); i++)
        {
            ((LeedReader) pContext).addTextGetData(String.valueOf(i));

            if (Integer.parseInt(articles.get(i).getId()) == 3226)
            {
                connection.setReadArticle(articles.get(i).getId());
            }

            if (articles.get(i).getIsRead() == 1)
            {
                connection.setReadArticle(articles.get(i).getId());
            }
        }
        for (int i = 0; i < articles.size(); i++)
        {
            ((LeedReader) pContext).addTextGetData(String.valueOf(i));
            if (articles.get(i).getIsFav() == 1)
            {
                connection.setFavArticle(articles.get(i).getId());
            }
        }
        saveConnectionType(cOnLine);
        ((LeedReader) pContext).endGetData();
    }

    public void setReadArticle(Article article, int type)
    {
        if (connectionType == cOnLine)
        {
            if (type == 1)
                connection.setReadArticle(article.getId());
        }
        if (connectionType == cOffLine)
        {
            DBData.setReadArticle(article);
        }
    }

    public void setUnReadArticle(Article article)
    {
        if (connectionType == cOnLine)
        {
            connection.setUnReadArticle(article.getId());
        }
        if (connectionType == cOffLine)
        {
            DBData.setUnReadArticle(article);
        }
    }

    public void setFavArticle(Article article)
    {
        if (connectionType == cOnLine)
        {
            connection.setFavArticle(article.getId());
        }
        if (connectionType == cOffLine)
        {
            DBData.setFavArticle(article);
        }
    }

    public void setUnFavArticle(Article article)
    {
        if (connectionType == cOnLine)
        {
            connection.setUnFavArticle(article.getId());
        }
        if (connectionType == cOffLine)
        {
            DBData.setUnFavArticle(article);
        }
    }

    public void setFeedRead()
    {
        if (connectionType == cOnLine)
        {
            connection.setReadFeed(pFeed.getId());
        }
        if (connectionType == cOffLine)
        {
            DBData.setReadFeed(pFeed.getId());
            updateFeed(DBData.getArticlesByFeed(pFeed));
        }
    }

    public void setAllRead()
    {
        if (connectionType == cOnLine)
        {
            connection.setAllRead();
        }
        if (connectionType == cOffLine)
        {
            DBData.setAllRead();
            getHomePage();
        }
    }

    public void decreaseNbNoReadArticle(String idFeed)
    {
        for (int i = 0; i < pFolders.size(); i++)
        {
            for (int j = 0; j < pFolders.get(i).getFlux().size(); j++)
            {
                if (pFolders.get(i).getFlux().get(j).getId().equals(idFeed))
                {
                    pFolders.get(i).getFlux().get(j).addReadArticle();
                }
            }
        }

        updateCategories(pFolders);
    }

    public Flux getFeed(String feed)
    {
        for (int i = 0; i < pFolders.size(); i++)
        {
            for (int j = 0; j < pFolders.get(i).getFlux().size(); j++)
            {
                if (pFolders.get(i).getFlux().get(j).getId().equals(feed))
                {
                    return pFolders.get(i).getFlux().get(j);
                }
            }
        }

        return null;
    }
}
