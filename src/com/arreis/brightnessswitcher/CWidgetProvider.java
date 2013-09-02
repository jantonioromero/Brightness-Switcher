package com.arreis.brightnessswitcher;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

public class CWidgetProvider extends AppWidgetProvider
{
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
//		ComponentName thisWidget = new ComponentName(context, CWidgetProvider.class);
//		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
//		for (int widgetId : allWidgetIds)
//		{
//			...
//		}
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		remoteViews.setOnClickPendingIntent(R.id.widget_button, buildButtonPendingIntent(context));
		
		int brightnessMode = android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		double brightnessValue = (double) android.provider.Settings.System.getInt(context.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 0) / 255;
		remoteViews.setTextViewText(R.id.widget_text, brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC ? context.getString(R.string.auto) : String.format(context.getString(R.string.percentLevelFormat), (int) (100 * brightnessValue)));
		
		pushWidgetUpdate(context, remoteViews);
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