package com.arreis.brightnessswitcher;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

public class CWidgetProvider extends AppWidgetProvider
{
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setOnClickPendingIntent(R.id.widget_button, buildButtonPendingIntent(context));
		
		int brightnessMode = android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		double brightnessValue = (double) android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 1) / 255;
		remoteViews.setTextViewText(R.id.widget_text, brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC ? context.getString(R.string.auto) : String.format(context.getString(R.string.percentLevelFormat), Math.round(100 * brightnessValue)));
		
		boolean showTitle = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(CWidgetReceiver.PREFERENCES_SHOW_WIDGET_TITLE, true);
		remoteViews.setViewVisibility(R.id.widget_title, showTitle ? View.VISIBLE : View.GONE);
		updateWidgetTextSize(context, remoteViews, showTitle);
		
		pushWidgetUpdate(context, remoteViews);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void updateWidgetTextSize(Context context, RemoteViews remoteViews, boolean showTitle)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
		{
			float textSize = context.getResources().getDimension(showTitle ? R.dimen.widget_textSize_withTitle : R.dimen.widget_textSize);
			remoteViews.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_PX, textSize);
		}
	}
	
	public static PendingIntent buildButtonPendingIntent(Context context)
	{
		Intent intent = new Intent(context, CWidgetReceiver.class);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews)
	{
		ComponentName myWidget = new ComponentName(context, CWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myWidget, remoteViews);
	}
}
