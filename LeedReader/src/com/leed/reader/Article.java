package com.leed.reader;

public class Article {

	private String id;
	private String title;
	private String content;
	private int isRead;
	
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
}
