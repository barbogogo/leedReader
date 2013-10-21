package com.barbogogo.leedreader;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.leed.reader.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class LeedReader extends Activity
{
    private LinearLayout          mLoadingLayout;
    private TextView              mLoadingMessage;
    private TextView              mInformationArea;
    private Button                mButton;

    private ViewPager             mWebView;
    private DrawerLayout          mDrawerLayout;
    private ExpandableListView    mDrawerList;
    private WebView               mServerErrorView;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView              mListView;

    private ShareActionProvider   mShareActionProvider;

    private CharSequence          mDrawerTitle;
    private CharSequence          mTitle;

    /**
     * Navigation automate
     */
    private int                   posNavigation    = 0;
    static final int              cpGlobal         = 0;
    static final int              cpFolder         = 1;
    static final int              cpFeed           = 2;
    static final int              cpArticle        = 3;

    private boolean               settingFlag;
    private boolean               mUpdateRequest;

    private DataManagement        dataManagement;

    /**
     * Mode view
     */
    private int                   modeView         = 0;
    static final int              cModeNavigation  = 0;
    static final int              cModeTextView    = 1;
    static final int              cModeWebView     = 2;
    static final int              cModePageLoading = 3;
    static final int              cModeServerError = 4;
    static final int              cModeSyncResult  = 5;

    public Context                context;

    private boolean               parameterGiven;
    private int                   mScrollPosition;

    private View                  LazyLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOverflowMenu();

        mTitle = getTitle();
        mDrawerTitle = getTitle();

        mInformationArea = (TextView) findViewById(R.id.informationArea);
        mButton = (Button) findViewById(R.id.buttonParameter);
        mWebView = (ViewPager) findViewById(R.id.home_pannels_pager);
        mServerErrorView = (WebView) findViewById(R.id.serverErrorArea);
        mLoadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);
        mLoadingMessage = (TextView) findViewById(R.id.loadingText);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
        mListView = (ListView) findViewById(R.id.content_frame);

        LazyLoadingView = getLayoutInflater().inflate(R.layout.footer_view, null);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(mDrawerTitle);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
        mDrawerLayout, /* DrawerLayout object */
        R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
        R.string.drawer_open, /* "open drawer" description for accessibility */
        R.string.drawer_close /* "close drawer" description for accessibility */
        )
        {
            public void onDrawerClosed(View view)
            {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView)
            {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                                         // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        posNavigation = cpGlobal;
        settingFlag = false;

        context = this;

        dataManagement = new DataManagement(context);

        checkVersion();

    }

    public void setPosNavigation(int pos)
    {
        posNavigation = pos;
    }

    @Override
    public void onBackPressed()
    {
        Log.i("posNavigation", String.valueOf(posNavigation));

        switch (posNavigation)
        {
            case cpGlobal:
            default:
                super.onBackPressed();
            break;

            case cpFeed:
                posNavigation = cpGlobal;
                setModeView(cModeNavigation);
                getHomePage();
            break;

            case cpArticle:
                posNavigation = cpFeed;
                setModeView(cModeNavigation);
            break;
        }
    }

    public void checkVersion()
    {
        parameterGiven = true;
        dataManagement.getParameters();

        if (parameterGiven == true)
        {
            dataManagement.checkVersion();
        }
        else
        {
            init();
        }
    }

    public void endCheckVersion(String version, String link, int retour)
    {
        if (retour == 1)
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this).setSmallIcon(R.drawable.logo)
                            .setContentTitle("LeedReader")
                            .setContentText("La version " + version + " est disponible.");

            int mId = 1;

            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));

            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, viewIntent, 0));

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mId, mBuilder.build());
        }

        init();
    }

    public void init()
    {
        setModeView(cModePageLoading);

        setTitle(mDrawerTitle);

        parameterGiven = true;
        dataManagement.getParameters();

        if (parameterGiven == true)
        {
            dataManagement.init();
        }
    }

    public void endGetData()
    {
        setModeView(cModeNavigation);

        Toast.makeText(this, "Téléchargement terminé", Toast.LENGTH_LONG).show();

        init();
    }

    public void getHomePage()
    {
        setTitle(mDrawerTitle);
        setModeView(cModePageLoading);
        dataManagement.getHomePage();
    }

    public void getCategories()
    {
        setModeView(cModePageLoading);
        dataManagement.getCategories();
    }

    public void updateCategories(final ArrayList<Folder> folders)
    {
        mDrawerList.setAdapter(new MenuAdapter(this, folders, dataManagement.getShowEmptyFeeds()));
    }

    public void getFeed(Flux feed)
    {
        setModeView(cModePageLoading);
        dataManagement.getFeed(feed);

        setTitle(feed.getName());

        mDrawerLayout.closeDrawer(mDrawerList);
        posNavigation = cpFeed;
    }
    
    public Flux getFeed(String feed)
    {
        return dataManagement.getFeed(feed);
    }

    public void updateFeed(final Flux feed)
    {

        if (feed.getNbArticles() == 0)
            Toast.makeText(context, getResources().getString(R.string.msg_empty_feed), Toast.LENGTH_LONG)
                    .show();

        final FeedAdapter adapter = new FeedAdapter(this, feed, dataManagement);

        mUpdateRequest = false;

        mListView.addFooterView(LazyLoadingView);

        mListView.setOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount)
            {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                if (scrollState == SCROLL_STATE_IDLE)
                {
                    if (mListView.getLastVisiblePosition() >= mListView.getCount() - 2
                            && mUpdateRequest == false)
                    {
                        mUpdateRequest = true;
                        mScrollPosition = mListView.getFirstVisiblePosition();
                        mListView.addFooterView(LazyLoadingView);
                        dataManagement.getOffsetFeed(feed, mListView.getCount());
                    }
                }
            }
        });

        mListView.setAdapter(adapter);

        if (mListView.getFooterViewsCount() > 0)
            mListView.removeFooterView(LazyLoadingView);

        mListView.setSelection(mScrollPosition);

        dataManagement.getCategories();

        setModeView(cModeNavigation);

        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // do nothing
            }
        });
    }

    public void erreurServeur(String msg, boolean showSetting)
    {
        if (showSetting == true)
        {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            settings();
        }
        else
        {
            setModeView(cModeServerError);
            mServerErrorView.loadData(msg, "text/html; charset=utf-8", "UTF-8");
        }
    }

    public void btnParameter(View view)
    {
        settings();
    }

    public void settings()
    {
        if (settingFlag == false)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            settingFlag = true;
        }
    }

    public APIConnection getConnection()
    {
        return dataManagement.getConnection();
    }

    @Override
    public void onResume()
    {
        // update data when parameters modification
        if (settingFlag == true)
        {
            init();
            settingFlag = false;
        }
        super.onResume();
    }

    public void noParameterInformation()
    {
        setModeView(cModeTextView);

        mInformationArea.setVisibility(View.GONE);

        mButton.setVisibility(View.VISIBLE);
        mButton.setText(R.string.setting_no_information_text);

        parameterGiven = false;
    }

    public void initGetData()
    {
        setModeView(cModeTextView);

        mInformationArea.setText(getResources().getString(R.string.msg_initialization));
    }

    public void addTextGetData(String pText)
    {
        String text = (String) mInformationArea.getText();

        mInformationArea.setText(pText + "\n" + text);
    }

    public DataManagement getDataManagement()
    {
        return dataManagement;
    }

    public void setModeView(int mode)
    {
        modeView = mode;
        showModeView();
    }

    public void showModeView()
    {
        mInformationArea.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mButton.setVisibility(View.GONE);
        mLoadingLayout.setVisibility(View.GONE);
        mServerErrorView.setVisibility(View.GONE);

        switch (modeView)
        {
            case cModeNavigation:
                mListView.setVisibility(View.VISIBLE);
            break;
            case cModeTextView:
                mInformationArea.setVisibility(View.VISIBLE);
            break;
            case cModeWebView:
                mWebView.setVisibility(View.VISIBLE);
            break;
            case cModePageLoading:
                mLoadingLayout.setVisibility(View.VISIBLE);
            break;
            case cModeServerError:
            case cModeSyncResult:
                mServerErrorView.setVisibility(View.VISIBLE);
            break;
        }
    }

    /***
     * Gestion du menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        mShareActionProvider.setShareIntent(createShareIntent());

        return super.onCreateOptionsMenu(menu);
    }

    private void getOverflowMenu()
    {
        try
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null)
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        if (parameterGiven == true)
        {
            menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
            menu.findItem(R.id.action_reload).setVisible(!drawerOpen);
            menu.findItem(R.id.action_synchronize).setVisible(!drawerOpen);
            menu.findItem(R.id.quitter).setVisible(!drawerOpen);

            if (posNavigation == cpArticle)
            {
                menu.findItem(R.id.action_share).setVisible(!drawerOpen);
                menu.findItem(R.id.action_allRead).setVisible(false);
            }
            else
            {
                menu.findItem(R.id.action_share).setVisible(false);
                menu.findItem(R.id.action_allRead).setVisible(!drawerOpen);
            }
        }
        else
        {
            menu.findItem(R.id.action_settings).setVisible(true);
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_reload).setVisible(false);
            menu.findItem(R.id.action_allRead).setVisible(false);
            menu.findItem(R.id.action_synchronize).setVisible(false);
            menu.findItem(R.id.quitter).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId())
        {
            case R.id.action_settings:
                settings();
                return true;
            case R.id.action_reload:
                init();
                return true;
            case R.id.quitter:
                finish();
                return true;
            case R.id.action_allRead:
                allRead();
                return true;
            case R.id.action_synchronize:
                synchronize();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setTitle(CharSequence title)
    {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private Intent createShareIntent()
    {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "subject");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Some text");
        return shareIntent;
    }

    public void doShare(Intent shareIntent)
    {
        if (mShareActionProvider != null)
        {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void synchronize()
    {
        mLoadingMessage.setText(getResources().getString(R.string.msg_synchronization_loading_message));
        setModeView(cModePageLoading);
        setTitle(mDrawerTitle);

        dataManagement.synchronize();
    }

    public void synchronisationResult(String msg)
    {
        String finalContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "<html><head>"
                        + "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
                        + styleHtml() + "<head><body>" + "<h1>"
                        + getResources().getString(R.string.msg_synchronization_result) + "</h1><hr>";
        finalContent += msg + "</body></html>";
        mServerErrorView.loadData(finalContent, "text/html; charset=utf-8", "UTF-8");

        setModeView(cModeSyncResult);
        mLoadingMessage.setText(getResources().getString(R.string.msg_loading));
    }

    public String styleHtml()
    {
        String style =
                "<style type='text/css'>" + "body {}" + "h1 {color: %23f16529; font-size:14px;}"
                        + "h1 a {text-decoration:none; color: %23f16529;}"
                        + "article {color: black; font-size:14px; line-height:150%25;}"
                        + "header {color: %23555555; font-size: 10px;}"
                        + "hr {color: %23f16529; background-color: %23f16529; height: 1px;}"
                        + "img {max-width:100%25;height:auto;}" + "iframe {max-width:100%25;height:auto;}"
                        + ".imageTitle {padding: 2px;font-size:10px;background-color: %23ffffe5;border: solid 1px black;width:100%25;}"
                        + "</style>";

        return style;
    }

    public void allRead()
    {
        setModeView(cModePageLoading);

        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);

        switch (posNavigation)
        {
            case cpGlobal:
                alertbox.setMessage(getResources().getString(R.string.msg_all_read));
            break;

            case cpFeed:
                alertbox.setMessage(getResources().getString(R.string.msg_feed_read));
            break;

            default:
                alertbox.setMessage(getResources().getString(R.string.msg_function_unavailable));
            break;
        }

        alertbox.setPositiveButton(getResources().getString(R.string.msg_yes),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        switch (posNavigation)
                        {
                            case cpGlobal:
                                dataManagement.setAllRead();
                            break;

                            case cpFeed:
                                dataManagement.setFeedRead();
                            break;

                            default:
                            break;
                        }
                    }
                });

        alertbox.setNegativeButton(getResources().getString(R.string.msg_no),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        setModeView(cModeNavigation);
                    }
                });

        alertbox.show();
    }
}
