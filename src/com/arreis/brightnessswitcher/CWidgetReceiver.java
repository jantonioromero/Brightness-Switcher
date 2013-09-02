package com.arreis.brightnessswitcher;

import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.arreis.brightnessswitcher.datamodel.CBrightnessFileManager;

public class CWidgetReceiver extends BroadcastReceiver
{
	private static Vector<Double> mBrightnessLevels;
	private static int mCurrentLevelIndex;
	
	private static final String PREFERENCES_BRIGHTNESS_LEVEL_CURRENT = "PREFERENCES_BRIGHTNESS_LEVEL_CURRENT";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (mBrightnessLevels == null)
		{
			mBrightnessLevels = CBrightnessFileManager.getBrightnessLevels(context);
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mCurrentLevelIndex = prefs.getInt(PREFERENCES_BRIGHTNESS_LEVEL_CURRENT, -1);
		if (mCurrentLevelIndex < 0)
		{
			mCurrentLevelIndex = 0;
		}
		else
		{
			mCurrentLevelIndex = (mCurrentLevelIndex + 1) % mBrightnessLevels.size();
		}
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(PREFERENCES_BRIGHTNESS_LEVEL_CURRENT, mCurrentLevelIndex);
		editor.commit();
		
		double level = mBrightnessLevels.get(mCurrentLevelIndex).doubleValue();
		setBrightnessLevel(context, level);
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setTextViewText(R.id.widget_text, level == CBrightnessFileManager.BRIGHTNESS_LEVEL_AUTO ? context.getString(R.string.auto) : String.format(context.getString(R.string.percentLevelFormat), (int) (100 * level)));
		
		//REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
		remoteViews.setOnClickPendingIntent(R.id.widget_button, CWidgetProvider.buildButtonPendingIntent(context));
		CWidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
	}
	
	private void setBrightnessLevel(Context _context, double _level)
	{
		if (_level == CBrightnessFileManager.BRIGHTNESS_LEVEL_AUTO)
		{
			android.provider.Settings.System.putInt(_context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		}
		else
		{
			android.provider.Settings.System.putInt(_context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			android.provider.Settings.System.putInt(_context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, (int) (_level * 255));
		}
		
		// Force refresh
		Intent intent = new Intent(_context, CRefreshBrightnessActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("level", _level);
		_context.startActivity(intent);
	}
}
