package com.leed.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity 
{

	final String fileName = "settings.dat";
	public static final String PREFS_NAME = "MyPrefsFile";
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.settings);
		   
		   String url = this.getIntent().getStringExtra("url");
		   String login = this.getIntent().getStringExtra("login");
		   
		   TextView dataText = (TextView) findViewById(R.id.adresseServeurEdit);
		   dataText.setText(url);
		   
		   TextView loginText = (TextView) findViewById(R.id.loginServerEdit);
		   loginText.setText(login);
	}
	
	public void btnSaveData(View view)
	{
		TextView dataText = (TextView) findViewById(R.id.adresseServeurEdit);
		
		String sData = dataText.getText()+"";
		
		saveURL(sData);
		
		TextView loginText = (TextView) findViewById(R.id.loginServerEdit);
		TextView passwordText = (TextView) findViewById(R.id.passwordServerEdit);
		
		String lData = loginText.getText()+"";
		String pData = passwordText.getText()+"";
		
		if(pData.length() > 0)
			saveLoginPasswd(lData, pData);
		
		this.finish();
	}
	
	public void saveData(String data)
	{
		
		FileOutputStream fOut = null; 
		OutputStreamWriter osw = null; 
		
		try
		{
			fOut = this.openFileOutput(fileName,MODE_PRIVATE);       
			osw = new OutputStreamWriter(fOut); 
			osw.write(data);
			osw.flush(); 
			
			Toast.makeText(this, "URL "+data+" enregistrée.",Toast.LENGTH_SHORT).show();
			
			this.finish();
		} 
		catch (Exception e) 
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		} 
		finally 
		{ 
			try 
			{ 
				osw.close();
				fOut.close();
			}
			catch (IOException e)
			{
				Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG).show();
			} 
		} 
	}
	
	public void saveURL(String data)
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString("url", data);
		
		editor.commit();
	}
	
	public void saveLoginPasswd(String login, String password)
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString("login", login);
		editor.putString("password", password);
		
		editor.commit();
	}
	
	@Override
	public void finish()
	{
		super.finish();
	}
}
