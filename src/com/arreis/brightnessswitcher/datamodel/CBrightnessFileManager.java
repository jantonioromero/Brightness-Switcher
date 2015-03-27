package com.arreis.brightnessswitcher.datamodel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

public class CBrightnessFileManager
{
	public static final int MIN_BRIGHTNESS_LEVELS = 2;
	public static final int MAX_BRIGHTNESS_LEVELS = 10;
	public static final double BRIGHTNESS_LEVEL_DEFAULT = 0.5;
	public static final double BRIGHTNESS_LEVEL_AUTO = -1.0;
	
	private static final String BRIGHTNESS_LEVELS_FILENAME = "brightnesslevels.dat";
	
	private static Vector<Double> mBrightnessLevels;
	
	public static Vector<Double> getBrightnessLevels(Context _context)
	{
		if (mBrightnessLevels == null)
		{
			loadBrightnessLevels(_context);
		}
		
		return mBrightnessLevels;
	}
	
	public static void saveBrightnessLevels(Context _context, Vector<Double> _levels)
	{
		try
		{
			JSONArray values = new JSONArray();
			for (int i = 0; i < _levels.size(); i++)
			{
				values.put(_levels.get(i));
			}
			
			FileOutputStream fos = _context.openFileOutput(BRIGHTNESS_LEVELS_FILENAME, Context.MODE_PRIVATE);
			fos.write(values.toString().getBytes());
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void loadBrightnessLevels(Context _context)
	{
		try
		{
			FileInputStream fis = _context.openFileInput(BRIGHTNESS_LEVELS_FILENAME);
			StringBuffer buffer = new StringBuffer();
			String Read;
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			if (fis != null)
			{
				while ((Read = reader.readLine()) != null)
				{
					buffer.append(Read + "\n");
				}
			}
			fis.close();
			
			JSONArray entries = new JSONArray(buffer.toString());
			
			mBrightnessLevels = new Vector<Double>();
			for (int i = 0; i < entries.length(); i++)
			{
				mBrightnessLevels.add(Double.valueOf(entries.getDouble(i)));
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
//			e.printStackTrace();
			loadDefaultLevels();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void loadDefaultLevels()
	{
		mBrightnessLevels = new Vector<Double>();
		mBrightnessLevels.add(Double.valueOf(50.0 / 100));
		mBrightnessLevels.add(Double.valueOf(1.0 / 100));
	}
}
