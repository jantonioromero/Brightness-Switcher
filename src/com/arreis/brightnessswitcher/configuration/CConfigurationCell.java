package com.arreis.brightnessswitcher.configuration;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.arreis.brightnessswitcher.R;

public class CConfigurationCell extends FrameLayout
{
	private TextView mLeftText;
	private TextView mRightText;
	
	public CConfigurationCell(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();
		
		mLeftText = (TextView) findViewById(R.id.left_text);
		mRightText = (TextView) findViewById(R.id.right_text);
	}
	
	public void setText(String _left, String _right)
	{
		mLeftText.setText(_left);
		mRightText.setText(_right);
	}
}
