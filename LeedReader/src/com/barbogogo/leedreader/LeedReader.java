package com.barbogogo.leedreader;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.leed.reader.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class LeedReader extends Activity {
	// private ListView listView;
	// private ProgressBar progressBar;
	// private ProgressBar progressBarGetData;
	private TextView mInformationArea;
	private Button mButton;

	private ViewPager mWebView;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mListView;

	private ShareActionProvider mShareActionProvider;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	/**
	 * Navigation automate
	 */
	private int posNavigation = 0;
	static final int cpGlobal = 0;
	static final int cpFolder = 1;
	static final int cpFeed = 2;
	static final int cpArticle = 3;

	private boolean settingFlag;

	private DataManagement dataManagement;

	// private Button offLineButton;

	private static final int cOnLine = 0;
	private static final int cGetData = 1;
	private static final int cOffLine = 2;
	private static final int cSendData = 3;

	/**
	 * Mode view
	 */
	private int modeView = 0;
	static final int cModeNavigation = 0;
	static final int cModeTextView = 1;
	static final int cModeWebView = 2;

	public Context context;

	private Folder pActualFolder;

	private boolean parameterGiven;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getOverflowMenu();

		mTitle = mDrawerTitle = getTitle();

		mInformationArea = (TextView) findViewById(R.id.informationArea);
		mButton = (Button) findViewById(R.id.buttonParameter);
		mWebView = (ViewPager) findViewById(R.id.home_pannels_pager);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
		mListView = (ListView) findViewById(R.id.content_frame);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		posNavigation = cpGlobal;
		settingFlag = false;

		context = this;

		setModeView(cModeNavigation);

		init();
	}

	@Override
	public void onBackPressed() {
		Log.i("posNavigation", String.valueOf(posNavigation));

		switch (posNavigation) {
		case cpGlobal:
		default:
			super.onBackPressed();
			break;

		case cpFolder:
			posNavigation = cpGlobal;
			getCategories();
			break;

		case cpFeed:
			posNavigation = cpFolder;
			setModeView(cModeNavigation);
			dataManagement.getCategory(pActualFolder);
			break;

		case cpArticle:
			posNavigation = cpFeed;
			setModeView(cModeNavigation);
			break;
		}
	}

	public void setPosNavigation(int pos) {
		posNavigation = pos;
	}

	public void btnGetData(View view) {
		// progressBar.setVisibility(ProgressBar.VISIBLE);

		// listView.removeAllViewsInLayout();

		dataManagement.getParameters();

		getCategories();
	}

	public void btnGetOffLineData(View view) throws InterruptedException {
		// progressBar.setVisibility(ProgressBar.VISIBLE);

		// listView.removeAllViewsInLayout();

		dataManagement.changeConnectionMode();

		init();
	}

	public void setOffLineButton(int connectionType) {
		switch (connectionType) {
		case cOnLine:
			// offLineButton.setText("On\nLine");
			// offLineButton.setTextColor(Color.parseColor("#00FF00"));
			break;
		case cGetData:
			// offLineButton.setText("Get\nData");
			// offLineButton.setTextColor(Color.parseColor("#FF0000"));
			break;
		case cOffLine:
			// offLineButton.setText("Off\nLine");
			// offLineButton.setTextColor(Color.parseColor("#000000"));
			break;
		case cSendData:
			// offLineButton.setText("Send\nData");
			// offLineButton.setTextColor(Color.parseColor("#FF0000"));
			break;
		}
	}

	public void init() {

		parameterGiven = true;
		dataManagement = new DataManagement(context);

		if (parameterGiven == true) {
			setModeView(cModeNavigation);
			dataManagement.init();
		}

	}

	public void endGetData() {
		setModeView(cModeNavigation);

		// progressBar.setVisibility(ProgressBar.INVISIBLE);

		Toast.makeText(this, "Téléchargement terminé", Toast.LENGTH_LONG)
				.show();
		init();
	}

	public void getCategories() {
		dataManagement.getCategories();
	}

	public void updateCategories(final ArrayList<Folder> folders) {
		// progressBar.setVisibility(ProgressBar.INVISIBLE);

		if (folders.size() > 0) {
			mDrawerList.setAdapter(new MenuAdapter(this, folders));
		}
	}

	public void getFeed(Flux feed) {
		dataManagement.getFeed(feed);

		setTitle(feed.getName());
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public void updateFeed(Flux feed) {
		// progressBar.setVisibility(ProgressBar.INVISIBLE);

		FeedAdapter adapter = new FeedAdapter(this, feed, dataManagement);
		mListView.setAdapter(adapter);

		dataManagement.getCategories();

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// do nothing
			}
		});
	}

	public void erreurServeur(String msg, boolean showSetting) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

		// progressBar.setVisibility(ProgressBar.INVISIBLE);

		if (settingFlag == false && showSetting == true) {
			settings();
		}
	}

	public void btnParameter(View view) {
		settings();
	}

	public void settings() {
		Bundle objetbunble = new Bundle();
		objetbunble.putString("url", dataManagement.getUrl());
		objetbunble.putString("login", dataManagement.getLogin());

		Intent intent = new Intent(this, SettingsActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtras(objetbunble);

		startActivity(intent);

		settingFlag = true;
	}

	public APIConnection getConnection() {
		return dataManagement.getConnection();
	}

	@Override
	public void onResume() {
		// update data when parameters modification
		if (settingFlag == true) {
			// progressBar.setVisibility(ProgressBar.VISIBLE);

			// listView.removeAllViewsInLayout();

			dataManagement.getParameters();
			init();
			settingFlag = false;
		}
		super.onResume();
	}

	public void noParameterInformation() {
		setModeView(cModeTextView);
		// mInformationArea.setText(R.string.setting_no_information_text);
		//
		// mInformationArea.setGravity(Gravity.CENTER_HORIZONTAL
		// | Gravity.CENTER_VERTICAL);

		mInformationArea.setVisibility(View.GONE);

		mButton.setVisibility(View.VISIBLE);
		mButton.setText(R.string.setting_no_information_text);

		parameterGiven = false;
	}

	public void initGetData() {
		setModeView(cModeTextView);

		mInformationArea.setText(getResources().getString(
				R.string.msg_initialization));
	}

	public void addTextGetData(String pText) {
		String text = (String) mInformationArea.getText();

		mInformationArea.setText(pText + "\n" + text);
	}

	public void setBarGetData(int value, int max) {
		// int maxValue = progressBarGetData.getMax();

		// int progress = 0;
		//
		// if (max > 0)
		// progress = (value * maxValue) / max;

		// progressBarGetData.setProgress(progress);
	}

	public DataManagement getDataManagement() {
		return dataManagement;
	}

	public void setModeView(int mode) {
		modeView = mode;
		showModeView();
	}

	public void showModeView() {
		switch (modeView) {

		case cModeNavigation:
			// progressBarGetData.setVisibility(View.GONE);
			mInformationArea.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			mWebView.setVisibility(View.GONE);
			mButton.setVisibility(View.GONE);
			break;

		case cModeTextView:
			// progressBarGetData.setVisibility(View.VISIBLE);
			mInformationArea.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			mWebView.setVisibility(View.GONE);
			mButton.setVisibility(View.GONE);
			break;

		case cModeWebView:
			// progressBarGetData.setVisibility(View.GONE);
			mInformationArea.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
			mButton.setVisibility(View.GONE);
			break;

		}
	}

	/***
	 * Gestion du menu
	 */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		// mShareActionProvider.setShareIntent(getDefaultShareIntent());

		return super.onCreateOptionsMenu(menu);
	}

	private void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

		if (parameterGiven == true) {
			menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
			menu.findItem(R.id.action_share).setVisible(!drawerOpen);
			menu.findItem(R.id.action_reload).setVisible(!drawerOpen);
			menu.findItem(R.id.action_allRead).setVisible(!drawerOpen);
		} else {
			menu.findItem(R.id.action_settings).setVisible(true);
			menu.findItem(R.id.action_share).setVisible(false);
			menu.findItem(R.id.action_reload).setVisible(false);
			menu.findItem(R.id.action_allRead).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_settings:
			settings();
			return true;
		case R.id.action_share:
			Toast.makeText(
					this,
					getResources().getString(R.string.msg_unavailable_function),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.action_reload:
			init();
			return true;
		case R.id.quitter:
			finish();
			return true;
		case R.id.action_allRead:
			Toast.makeText(
					this,
					getResources().getString(R.string.msg_unavailable_function),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	// Call to update the share intent
	private void setShareIntent(Intent shareIntent) {
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
	}
}
