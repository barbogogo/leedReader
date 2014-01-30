package com.barbogogo.leedreader.widget;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.barbogogo.leedreader.Article;
import com.barbogogo.leedreader.LeedReader;
import com.leed.reader.R;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetService extends IntentService
{
    public WidgetService()
    {
        super("WidgetService");
        // TODO Auto-generated constructor stub
    }

    private static final String LOG           = WidgetClass.LOG;

    public static final String  PARAM_IN_MSG  = "imsg";
    public static final String  PARAM_OUT_MSG = "omsg";

    // Temps de travail en secondes
    private static final int    waitTime      = 5;

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(LOG, "onCreate");
    }

    private void waitTest(Intent intent)
    {
        Log.i(LOG, "Entrée dans la classe UpdateWidgetIntentService");

        String msg = intent.getStringExtra(PARAM_IN_MSG);

        Log.i(LOG, "Reception du texte : " + msg);

        Log.i(LOG, "On attend " + waitTime + " s");
        SystemClock.sleep(waitTime * 1000); // 30 seconds
        Log.i(LOG, "Fin des " + waitTime + " s");
        String resultTxt = msg + " " + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());
        Log.i(LOG, resultTxt);
    }

    private String getDataFromUrlTest(String urlLeed, String login, String password)
    {
        GetData data = new GetData(urlLeed, login, password);
        data.getData(GetData.cLogin);
        return data.getData(GetData.cGetFeed);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(LOG, "Service Destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d(LOG, "Service Started");

        // create some random data

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        String urlLeed = intent.getStringExtra("urlLeed");
        String login = intent.getStringExtra("login");
        String password = intent.getStringExtra("password");

        Log.i(LOG, "parameters received: " + urlLeed + ":" + login + ":" + password);

        // waitTest(intent);
        String data = getDataFromUrlTest(urlLeed, login, password);

        String output = "";
        // output=DateFormat.format("hh:mm:ss", new Date()).toString();
        output = data;
        Log.i(LOG, "Called");

        ComponentName thisWidget = new ComponentName(getApplicationContext(), WidgetClass.class);
        int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
        Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
        Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

        for (int widgetId : allWidgetIds)
        {
            // create some random data
            // int number = (new Random().nextInt(100));

            RemoteViews remoteViews =
                    new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget);
            Log.w(LOG, data);

            String titre = "";
            int id = 0;
            String id_ = "";

            JSONObject jsonObject;
            try
            {
                jsonObject = new JSONObject(data);

                JSONArray articlesItems = new JSONArray(jsonObject.getString("articles"));

                JSONObject postalCodesItem = articlesItems.getJSONObject(0);

                titre = postalCodesItem.getString("title");
                id = postalCodesItem.getInt("id");
                id_ = postalCodesItem.getString("id");
            }
            catch (JSONException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // Set the text
            remoteViews.setTextViewText(R.id.text, titre);
            
            // Ouverture de LeedReader sur click
            Intent clickIntent = new Intent(this.getApplicationContext(), LeedReader.class);

            clickIntent.putExtra("feed", data);
            clickIntent.setAction("android.intent.action.OPEN_BY_WIDGET");

            PendingIntent pendingIntent =
                    PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, 0);

            // Intent clickIntent = new Intent(this.getApplicationContext(),
            // WidgetClass.class);

            // clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            // clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
            // allWidgetIds);

            // PendingIntent pendingIntent =
            // PendingIntent.getBroadcast(getApplicationContext(), 0,
            // clickIntent,
            // PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.text, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();
    }
}
