package com.barbogogo.leedreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME       = "leed.db";
    private static final int    DATABASE_VERSION    = 4;

    public static final String  FOLDER_TABLE        = "leed_folder";
    public static final String  FOLD_COL_ID         = "id";
    public static final String  FOLD_COL_TITLE      = "title";

    private static final String FOLDER_TABLE_CREATE = "CREATE TABLE " + FOLDER_TABLE + "(" + "  "
                                                            + FOLD_COL_ID + " text not null," + "  "
                                                            + FOLD_COL_TITLE + " text not null" + ");";

    public static final String  FEED_TABLE          = "leed_feed";
    public static final String  FEED_COL_ID         = "id";
    public static final String  FEED_COL_NAME       = "name";
    public static final String  FEED_COL_DESC       = "description";
    public static final String  FEED_COL_WEBS       = "website";
    public static final String  FEED_COL_URL        = "url";
    public static final String  FEED_COL_UPDATE     = "lastupdate";
    public static final String  FEED_COL_FOLD       = "folder";

    private static final String FEED_TABLE_CREATE   = "CREATE TABLE " + FEED_TABLE + "(" + "  " + FEED_COL_ID
                                                            + " text NOT NULL," + "  " + FEED_COL_NAME
                                                            + " text NOT NULL," + "  " + FEED_COL_DESC
                                                            + " text NOT NULL," + "  " + FEED_COL_WEBS
                                                            + " text NOT NULL," + "  " + FEED_COL_URL
                                                            + " text NOT NULL," + "  " + FEED_COL_UPDATE
                                                            + " text NOT NULL," + "  " + FEED_COL_FOLD
                                                            + " text NOT NULL" + ");";

    public static final String  ARTI_TABLE          = "leed_article";
    public static final String  ARTI_COL_ID         = "id";
    public static final String  ARTI_COL_TITLE      = "title";
    public static final String  ARTI_COL_AUTHOR     = "author";
    public static final String  ARTI_COL_DATE       = "date";
    public static final String  ARTI_COL_URL        = "url";
    public static final String  ARTI_COL_CONTENT    = "content";
    public static final String  ARTI_COL_ISFAV      = "isFav";
    public static final String  ARTI_COL_ISREAD     = "isRead";
    public static final String  ARTI_COL_IDFEED     = "idFeed";

    private static final String ARTI_TABLE_CREATE   = "CREATE TABLE " + ARTI_TABLE + "(" + "  " + ARTI_COL_ID
                                                            + " text NOT NULL," + "  " + ARTI_COL_TITLE
                                                            + " text NOT NULL," + "  " + ARTI_COL_AUTHOR
                                                            + " text NOT NULL," + "  " + ARTI_COL_DATE
                                                            + " text NOT NULL," + "  " + ARTI_COL_URL
                                                            + " text NOT NULL," + "  " + ARTI_COL_CONTENT
                                                            + " text NOT NULL," + "  " + ARTI_COL_ISFAV
                                                            + " text NOT NULL," + "  " + ARTI_COL_ISREAD
                                                            + " text NOT NULL," + "  " + ARTI_COL_IDFEED
                                                            + " text NOT NULL" + ");";

    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(FOLDER_TABLE_CREATE);
        database.execSQL(FEED_TABLE_CREATE);
        database.execSQL(ARTI_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FOLDER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FEED_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ARTI_TABLE);
        onCreate(db);
    }

    public void deleteDataBase(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + FOLDER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FEED_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ARTI_TABLE);
        onCreate(db);
    }
}
