package com.barbogogo.leedreader.widget;

import java.util.Random;

import com.leed.reader.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetClass extends AppWidgetProvider
{

    public static final String      LOG               = "WidgetReader";

    private static Context          pContext          = null;
    private static AppWidgetManager pAppWidgetManager = null;
    private static int[]            pAppWidgetIds;

    private IntentFilter            intentFilter      = null;

    private static String           data              = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {

        Log.w(LOG, "onUpdate WidgetClass");

        pContext = context;
        pAppWidgetManager = appWidgetManager;
        pAppWidgetIds = appWidgetIds;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(pContext);

        String urlLeed = settings.getString("serverLinkPref", "");
        String login = settings.getString("usernamePref", "");
        String password = settings.getString("passwordPref", "");

        Log.i(LOG, "parameters saved:" + urlLeed + ":" + login + ":" + password);

        ComponentName thisWidget = new ComponentName(context, WidgetClass.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        Intent intent = new Intent(context.getApplicationContext(), WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        intent.putExtra("urlLeed", urlLeed);
        intent.putExtra("login", login);
        intent.putExtra("password", password);

        // Update the widgets via the service
        context.startService(intent);
    }
}
