package com.barbogogo.leedreader;

public class Article
{

    private String id;
    private String title;
    private String content;
    private int    isRead;
    private int    isFav;
    private String date;
    private String author;
    private String urlArticle;
    private String idFeed;
    private String nameFeed;
    private String urlFeed;

    Article(String pId)
    {
        id = pId;
        isRead = 0;
    }

    public void setTitle(String pTitle)
    {
        title = pTitle;
    }

    public void setContent(String pContent)
    {
        content = pContent;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public int getIsRead()
    {
        return isRead;
    }

    public void setIsRead(int pRead)
    {
        isRead = pRead;
    }

    public void setRead()
    {
        setIsRead(1);
    }

    public void setUnRead()
    {
        setIsRead(0);
    }

    public int getIsFav()
    {
        return isFav;
    }

    public void setFav(int lFav)
    {
        isFav = lFav;
    }

    public void setDate(String pDate)
    {
        date = pDate;
    }

    public String getDate()
    {
        return date;
    }

    public void setAuthor(String pAuthor)
    {
        author = pAuthor;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setUrlArticle(String pUrlArticle)
    {
        urlArticle = pUrlArticle;
    }

    public String getUrlArticle()
    {
        return urlArticle;
    }

    public void setIdFeed(String lIdFeed)
    {
        idFeed = lIdFeed;
    }

    public String getIdFeed()
    {
        return idFeed;
    }

    public void setNameFeed(String lNameFeed)
    {
        nameFeed = lNameFeed;
    }

    public String getNameFeed()
    {
        return nameFeed;
    }

    public void setUrlFeed(String lUrlFeed)
    {
        urlFeed = lUrlFeed;
    }

    public String getUrlFeed()
    {
        return urlFeed;
    }
}
