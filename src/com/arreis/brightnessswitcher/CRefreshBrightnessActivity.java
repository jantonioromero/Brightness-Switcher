package com.arreis.brightnessswitcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

public class CRefreshBrightnessActivity extends Activity
{
	private static final int DELAY_BEFORE_FINISH = 200;
	
	private static Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHandler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message msg)
			{
				CRefreshBrightnessActivity.this.finish();
				return true;
			}
		});
		
		final double brightness = getIntent().getDoubleExtra("level", 0);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = (float) brightness;
		getWindow().setAttributes(lp);
		
		// Delay finish to let the activity refresh the display
		final Message message = mHandler.obtainMessage();
		mHandler.sendMessageDelayed(message, DELAY_BEFORE_FINISH);
	}
}
