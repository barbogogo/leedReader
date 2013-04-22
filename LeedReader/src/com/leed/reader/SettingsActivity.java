package com.leed.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity 
{

	final String fileName = "settings.dat";
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.settings);
		   
		   String url = this.getIntent().getStringExtra("url");
		   int errorServer = this.getIntent().getIntExtra("errorServer", 1);
		   
		   TextView datatext = (TextView) findViewById(R.id.adresseServeurEdit);
			
		   datatext.setText(url);
		   
		   if(errorServer == 1)
			   Toast.makeText(this, "URL non valide, merci de la changer",Toast.LENGTH_SHORT).show();
	}
	
	public void btnSaveData(View view)
	{
		TextView datatext = (TextView) findViewById(R.id.adresseServeurEdit);
		
		String sData = datatext.getText()+"";
		
		saveData(sData);
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
	
	@Override
	public void finish()
	{
		
		
		super.finish();
		
	}
}
