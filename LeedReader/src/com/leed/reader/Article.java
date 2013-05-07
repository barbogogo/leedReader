package com.leed.reader;

public class Article {

	private String id;
	private String title;
	private String content;
	private int isRead;
	private int isFav;
	private String date;
	private String author;
	private String urlArticle;
	
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
	
	public void setRead()
	{
		isRead = 1;
	}
	
	public void setUnRead()
	{
		isRead = 0;
	}
	
	public int getIsFav()
	{
		return isFav;
	}
	
	public void setFav()
	{
		isFav = 1;
	}
	
	public void setUnFav()
	{
		isFav = 0;
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
}
