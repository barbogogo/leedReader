package com.barbogogo.leedreader;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LocalData
{
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[]       allColumns =
                                      { MySQLiteHelper.FOLD_COL_ID, MySQLiteHelper.FOLD_COL_TITLE };

    public LocalData(Context context)
    {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException
    {
        database = dbHelper.getWritableDatabase();
    }

    public void init()
    {
        dbHelper.deleteDataBase(database);
    }

    public void close()
    {
        dbHelper.close();
    }

    public void addFolder(Folder folder)
    {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.FOLD_COL_ID, folder.getId());
        values.put(MySQLiteHelper.FOLD_COL_TITLE, folder.getTitle());

        database.insert(MySQLiteHelper.FOLDER_TABLE, null, values);
    }

    public ArrayList<Folder> getAllFolders()
    {
        ArrayList<Folder> folders = new ArrayList<Folder>();

        Cursor cursor = database.query(MySQLiteHelper.FOLDER_TABLE, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Folder folder = cursorToFolder(cursor);
            folders.add(folder);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return folders;
    }

    private Folder cursorToFolder(Cursor cursor)
    {
        Folder folder = new Folder();
        folder.setId(cursor.getString(0));
        folder.setTitle(cursor.getString(1));

        ArrayList<Flux> feeds = getFeedByCategory(cursor.getString(0));

        for (int i = 0; i < feeds.size(); i++)
        {
            folder.addFeed(feeds.get(i));
        }

        return folder;
    }

    public void addFeed(Flux feed)
    {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.FEED_COL_ID, feed.getId());
        values.put(MySQLiteHelper.FEED_COL_NAME, feed.getName());
        values.put(MySQLiteHelper.FEED_COL_DESC, "none");
        values.put(MySQLiteHelper.FEED_COL_WEBS, "none");
        values.put(MySQLiteHelper.FEED_COL_URL, feed.getUrl());
        values.put(MySQLiteHelper.FEED_COL_UPDATE, "none");
        values.put(MySQLiteHelper.FEED_COL_FOLD, feed.getIdCategory());

        long toto = database.insert(MySQLiteHelper.FEED_TABLE, null, values);

        toto = toto + 1;
    }

    public ArrayList<Flux> getFeedByCategory(String idCategory)
    {
        ArrayList<Flux> feeds = new ArrayList<Flux>();

        String[] extractColumns =
        { MySQLiteHelper.FEED_COL_ID, MySQLiteHelper.FEED_COL_NAME, MySQLiteHelper.FEED_COL_FOLD };

        String[] args =
        { idCategory };

        Cursor cursor =
                database.query(true, MySQLiteHelper.FEED_TABLE, extractColumns, MySQLiteHelper.FEED_COL_FOLD
                        + "=?", args, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Flux feed = cursorToFeed(cursor);
            feeds.add(feed);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return feeds;
    }

    private Flux cursorToFeed(Cursor cursor)
    {
        Flux feed = getArticlesByFeed(cursor.getString(0));
        feed.setId(cursor.getString(0));
        feed.setName(cursor.getString(1));
        feed.setIdCategory(cursor.getString(2));

        int pNbNoRead = 0;

        for (int i = 0; i < feed.getArticles().size(); i++)
        {
            // if(feed.getArticle(i).getIsRead() == 0)
            pNbNoRead++;
        }

        feed.setNbNoRead(pNbNoRead);

        return feed;
    }

    public void addArticle(Article article)
    {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.ARTI_COL_ID, article.getId());
        values.put(MySQLiteHelper.ARTI_COL_TITLE, article.getTitle());
        values.put(MySQLiteHelper.ARTI_COL_AUTHOR, article.getAuthor());
        values.put(MySQLiteHelper.ARTI_COL_DATE, article.getDate());
        values.put(MySQLiteHelper.ARTI_COL_URL, article.getUrlArticle());
        values.put(MySQLiteHelper.ARTI_COL_CONTENT, article.getContent());
        values.put(MySQLiteHelper.ARTI_COL_ISFAV, article.getIsFav());
        values.put(MySQLiteHelper.ARTI_COL_ISREAD, article.getIsRead());
        values.put(MySQLiteHelper.ARTI_COL_IDFEED, article.getIdFeed());

        long toto = database.insert(MySQLiteHelper.ARTI_TABLE, null, values);

        toto = toto + 1;
    }

    public Flux getArticlesByFeed(Flux feed)
    {
        feed.deleteAllArticles();

        String[] extractColumns =
                { MySQLiteHelper.ARTI_COL_ID, MySQLiteHelper.ARTI_COL_TITLE, MySQLiteHelper.ARTI_COL_AUTHOR,
                        MySQLiteHelper.ARTI_COL_DATE, MySQLiteHelper.ARTI_COL_URL,
                        MySQLiteHelper.ARTI_COL_CONTENT, MySQLiteHelper.ARTI_COL_ISREAD,
                        MySQLiteHelper.ARTI_COL_ISFAV, MySQLiteHelper.ARTI_COL_IDFEED };

        String[] args =
        { feed.getId() };

        Cursor cursor =
                database.query(true, MySQLiteHelper.ARTI_TABLE, extractColumns,
                        MySQLiteHelper.ARTI_COL_IDFEED + "=?", args, null, null,
                        MySQLiteHelper.ARTI_COL_IDFEED + " DESC", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Article article = cursorToArticle(cursor);
            feed.addArticle(article);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return feed;
    }

    public Flux getArticlesByFeed(String idFeed)
    {
        Flux feed = new Flux();

        String[] extractColumns =
                { MySQLiteHelper.ARTI_COL_ID, MySQLiteHelper.ARTI_COL_TITLE, MySQLiteHelper.ARTI_COL_AUTHOR,
                        MySQLiteHelper.ARTI_COL_DATE, MySQLiteHelper.ARTI_COL_URL,
                        MySQLiteHelper.ARTI_COL_CONTENT, MySQLiteHelper.ARTI_COL_ISREAD,
                        MySQLiteHelper.ARTI_COL_ISFAV, MySQLiteHelper.ARTI_COL_IDFEED };

        String[] args =
        { idFeed };

        Cursor cursor =
                database.query(true, MySQLiteHelper.ARTI_TABLE, extractColumns,
                        MySQLiteHelper.ARTI_COL_IDFEED + "=?", args, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Article article = cursorToArticle(cursor);
            feed.addArticle(article);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return feed;
    }

    private Article cursorToArticle(Cursor cursor)
    {
        Article article = new Article(cursor.getString(0));
        article.setTitle(cursor.getString(1));
        article.setAuthor(cursor.getString(2));
        article.setDate(cursor.getString(3));
        article.setUrlArticle(cursor.getString(4));
        article.setContent(cursor.getString(5));
        article.setIsRead(cursor.getInt(6));
        article.setFav(cursor.getInt(7));
        article.setIdFeed(cursor.getString(8));
        return article;
    }

    public ArrayList<Article> getAllArticles()
    {
        ArrayList<Article> articles = new ArrayList<Article>();

        String[] extractColumns =
                { MySQLiteHelper.ARTI_COL_ID, MySQLiteHelper.ARTI_COL_TITLE, MySQLiteHelper.ARTI_COL_AUTHOR,
                        MySQLiteHelper.ARTI_COL_DATE, MySQLiteHelper.ARTI_COL_URL,
                        MySQLiteHelper.ARTI_COL_CONTENT, MySQLiteHelper.ARTI_COL_ISREAD,
                        MySQLiteHelper.ARTI_COL_ISFAV, MySQLiteHelper.ARTI_COL_IDFEED };

        Cursor cursor =
                database.query(MySQLiteHelper.ARTI_TABLE, extractColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Article article = cursorToArticle(cursor);
            articles.add(article);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return articles;
    }

    public Flux getHomePage()
    {
        Flux flux = new Flux();

        String[] extractColumns =
                { MySQLiteHelper.ARTI_COL_ID, MySQLiteHelper.ARTI_COL_TITLE, MySQLiteHelper.ARTI_COL_AUTHOR,
                        MySQLiteHelper.ARTI_COL_DATE, MySQLiteHelper.ARTI_COL_URL,
                        MySQLiteHelper.ARTI_COL_CONTENT, MySQLiteHelper.ARTI_COL_ISREAD,
                        MySQLiteHelper.ARTI_COL_ISFAV, MySQLiteHelper.ARTI_COL_IDFEED };

        String[] args =
        { "0" };

        Cursor cursor =
                database.query(true, MySQLiteHelper.ARTI_TABLE, extractColumns,
                        MySQLiteHelper.ARTI_COL_ISREAD + "=?", args, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            Article article = cursorToArticle(cursor);
            flux.addArticle(article);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return flux;
    }

    public Article getArticle(String idArticle)
    {
        String[] extractColumns =
                { MySQLiteHelper.ARTI_COL_ID, MySQLiteHelper.ARTI_COL_TITLE, MySQLiteHelper.ARTI_COL_AUTHOR,
                        MySQLiteHelper.ARTI_COL_DATE, MySQLiteHelper.ARTI_COL_URL,
                        MySQLiteHelper.ARTI_COL_CONTENT, MySQLiteHelper.ARTI_COL_ISREAD,
                        MySQLiteHelper.ARTI_COL_ISFAV, MySQLiteHelper.ARTI_COL_IDFEED };

        String[] args =
        { idArticle };

        Cursor cursor =
                database.query(true, MySQLiteHelper.ARTI_TABLE, extractColumns, MySQLiteHelper.ARTI_COL_ID
                        + "=?", args, null, null, null, null);

        cursor.moveToFirst();
        Article article = cursorToArticle(cursor);

        // Make sure to close the cursor
        cursor.close();
        return article;
    }

    public void setReadArticle(Article article)
    {
        String strFilter = "id=" + article.getId();
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISREAD, 1);
        database.update(MySQLiteHelper.ARTI_TABLE, args, strFilter, null);
    }

    public void setUnReadArticle(Article article)
    {
        String strFilter = "id=" + article.getId();
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISREAD, 0);
        database.update(MySQLiteHelper.ARTI_TABLE, args, strFilter, null);
    }

    public void setFavArticle(Article article)
    {
        String strFilter = "id=" + article.getId();
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISFAV, 1);
        database.update(MySQLiteHelper.ARTI_TABLE, args, strFilter, null);
    }

    public void setUnFavArticle(Article article)
    {
        String strFilter = "id=" + article.getId();
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISFAV, 0);
        database.update(MySQLiteHelper.ARTI_TABLE, args, strFilter, null);
    }

    public void setReadFeed(String feedId)
    {
        String strFilter = MySQLiteHelper.ARTI_COL_IDFEED + "=" + feedId;
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISREAD, 1);
        database.update(MySQLiteHelper.ARTI_TABLE, args, strFilter, null);
    }

    public void setAllRead()
    {
        ContentValues args = new ContentValues();
        args.put(MySQLiteHelper.ARTI_COL_ISREAD, 1);
        database.update(MySQLiteHelper.ARTI_TABLE, args, null, null);
    }
}
