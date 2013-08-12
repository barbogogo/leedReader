package com.leed.reader;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity
{
    private LinearLayout        mLoadingLayout;
    private TextView            mLoadingMessage;
    private ListView            mListView;
    // private TextView textGetData;
    private ViewPager           mWebView;
    private WebView             mServerErrorView;
    private TextView            mInformationArea;
    private Button              mButton;
    private ShareActionProvider mShareActionProvider;
    /**
     * Navigation automate
     */
    private int                 posNavigation    = 0;
    static final int            cpGlobal         = 0;
    static final int            cpFolder         = 1;
    static final int            cpFeed           = 2;
    static final int            cpArticle        = 3;

    private boolean             settingFlag;

    private DataManagement      dataManagement;

    private Button              offLineButton;

    private static final int    cOnLine          = 0;
    private static final int    cGetData         = 1;
    private static final int    cOffLine         = 2;
    private static final int    cSendData        = 3;

    /**
     * Mode view
     */
    private int                 modeView         = 0;
    static final int            cModeNavigation  = 0;
    static final int            cModeTextView    = 1;
    static final int            cModeWebView     = 2;
    static final int            cModePageLoading = 3;
    static final int            cModeServerError = 4;
    static final int            cModeSyncResult  = 5;

    public Context              context;

    private Folder              pActualFolder;
    private Flux                pActualFeed;

    private boolean             parameterGiven;

    private Intent              mShareIntent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mInformationArea = (TextView) findViewById(R.id.informationArea);
        mButton = (Button) findViewById(R.id.buttonParameter);
        mListView = (ListView) findViewById(R.id.ListViewId);
        offLineButton = (Button) findViewById(R.id.offLineButton);
        // progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        // textGetData = (TextView) findViewById(R.id.textGetData);
        mWebView = (ViewPager) findViewById(R.id.home_pannels_pager);
        mServerErrorView = (WebView) findViewById(R.id.serverErrorArea);

        mLoadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);
        mLoadingMessage = (TextView) findViewById(R.id.loadingText);

        setModeView(cModeNavigation);

        posNavigation = cpGlobal;
        settingFlag = false;

        context = this;

        dataManagement = new DataManagement(context);

        createShareIntent();
        
        init();
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.quitter:
                finish();
                return true;
            case R.id.allRead:
                Toast.makeText(this, "Fonction non disponible pour le moment", Toast.LENGTH_LONG).show();
                return true;
            case R.id.settings:
                settings();
                return true;
            case R.id.action_synchronize:
                synchronize();
                return true;
            case R.id.action_share:
                doShare();
                return true;
        }

        return false;
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

            case cpFolder:
                posNavigation = cpGlobal;
                setModeView(cModePageLoading);
                getCategories();
            break;

            case cpFeed:
                posNavigation = cpFolder;
                setModeView(cModePageLoading);
                dataManagement.getCategory(pActualFolder);
            break;

            case cpArticle:
                posNavigation = cpFeed;
                setModeView(cModePageLoading);
                dataManagement.getFeed(pActualFeed);

            break;
        }
    }

    public void setPosNavigation(int pos)
    {
        posNavigation = pos;
    }

    public void btnGetData(View view)
    {
        init();
    }

    public void btnGetOffLineData(View view) throws InterruptedException
    {
        mListView.removeAllViewsInLayout();

        dataManagement.changeConnectionMode();

        init();
    }

    public void setOffLineButton(int connectionType)
    {
        switch (connectionType)
        {
            case cOnLine:
                offLineButton.setText("On\nLine");
                offLineButton.setTextColor(Color.parseColor("#00FF00"));
            break;
            case cGetData:
                offLineButton.setText("Get\nData");
                offLineButton.setTextColor(Color.parseColor("#FF0000"));
            break;
            case cOffLine:
                offLineButton.setText("Off\nLine");
                offLineButton.setTextColor(Color.parseColor("#000000"));
            break;
            case cSendData:
                offLineButton.setText("Send\nData");
                offLineButton.setTextColor(Color.parseColor("#FF0000"));
            break;
        }
    }

    public void init()
    {
        setModeView(cModePageLoading);

        parameterGiven = true;
        offLineButton.setVisibility(View.VISIBLE);
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

    public void getCategories()
    {
        dataManagement.getCategories();
    }

    public void updateCategories(final ArrayList<Folder> folders)
    {
        ArrayList<String> items = new ArrayList<String>();

        if (folders.size() > 0)
        {
            for (int i = 0; i < folders.size(); i++)
            {
                items.add(folders.get(i).getTitle());
            }

            MobileArrayAdapter adapter = new MobileArrayAdapter(this, items, folders);
            mListView.setAdapter(adapter);
            setModeView(cModeNavigation);
        }
        else
        {
            Toast.makeText(context, "Pas de Catégories", Toast.LENGTH_LONG).show();
        }

        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                posNavigation = cpFolder;

                pActualFolder = folders.get(position);

                if (pActualFolder.getFlux().size() > 0)
                {
                    setModeView(cModePageLoading);
                    updateCategory(folders.get(position));
                }
                else
                {
                    Toast.makeText(context, "Pas de flux dans cette catégorie", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void updateCategory(final Folder folder)
    {
        FolderAdapter adapter = new FolderAdapter(this, folder);
        mListView.setAdapter(adapter);
        setModeView(cModeNavigation);

        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                posNavigation = cpFeed;

                pActualFeed = folder.getFeed(position);

                setModeView(cModePageLoading);

                dataManagement.getFeed(folder.getFeed(position));
            }
        });
    }

    public void updateFeed(Flux feed)
    {
        FeedAdapter adapter = new FeedAdapter(this, feed, dataManagement);
        mListView.setAdapter(adapter);

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
        if (settingFlag == false && showSetting == true)
        {
            settings();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
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
        Bundle objetbunble = new Bundle();
        objetbunble.putString("url", dataManagement.getUrl());
        objetbunble.putString("login", dataManagement.getLogin());

        Intent intent = new Intent(this, SettingsActivity.class);

        intent.putExtras(objetbunble);

        startActivity(intent);

        settingFlag = true;
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
        offLineButton.setVisibility(View.GONE);

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

    public void setShareIntent(Intent shareIntent)
    {
        mShareIntent = shareIntent;
    }
    
    private void createShareIntent()
    {
        mShareIntent =
                ShareCompat.IntentBuilder.from(this).setSubject("subject").setText("some text")
                        .setType("text/plain").getIntent();

    }

    public void doShare()
    {
        if (mShareIntent != null)
        {
            startActivity(mShareIntent);
        }
    }

    public void synchronize()
    {
        mLoadingMessage.setText(getResources().getString(R.string.msg_synchronization_loading_message));
        setModeView(cModePageLoading);

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
                "<style type='text/css'>" + "body {font-size: 12px;}"
                        + "h1 {color: #f16529; font-size:14px;}"
                        + "h1 a {text-decoration:none; color: #f16529;}"
                        + "article {color: black; font-size:12px;}"
                        + "hr {color: #f16529; background-color: #f16529; height: 1px;}" + "</style>";

        return style;
    }
}
