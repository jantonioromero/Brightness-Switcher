package com.arreis.brightnessswitcher.configuration;

import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.arreis.brightnessswitcher.CWidgetProvider;
import com.arreis.brightnessswitcher.CWidgetReceiver;
import com.arreis.brightnessswitcher.R;
import com.arreis.brightnessswitcher.datamodel.CBrightnessFileManager;

public class CConfigurationActivity extends FragmentActivity
{
	private ListView mListView;
	private CheckBox mShowTitleCheck;
	private Button mAddLevelButton;
	private Button mConfigFinishedButton;
	
	private Vector<Double> mBrightnessLevels;
	private int mSelectedLevel;
	
	private int mAppWidgetId = 0;
	
	private static final int NEW_LEVEL_INDEX = -1;
	
	private static final String DIALOG_TAG_CONFIRM_DELETE = "DIALOG_TAG_CONFIRM_DELETE";
	private static final String DIALOG_TAG_EDIT = "DIALOG_TAG_EDIT";
	private static final String DIALOG_TAG_MESSAGE = "DIALOG_TAG_MESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if (mAppWidgetId != 0)
			{
				setResult(RESULT_CANCELED);
			}
		}
		
		if (getResources().getBoolean(R.bool.allowLandscape) == false)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		setContentView(R.layout.activity_configuration);
		
		mBrightnessLevels = CBrightnessFileManager.getBrightnessLevels(getApplicationContext());
		
		mListView = (ListView) findViewById(R.id.listView);
		mListView.setAdapter(new CConfigurationListAdapter());
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				mSelectedLevel = position;
				CEditLevelDialog.newInstance(mBrightnessLevels.get(position)).show(getSupportFragmentManager(), DIALOG_TAG_EDIT);
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3)
			{
				if (mBrightnessLevels.size() > CBrightnessFileManager.MIN_BRIGHTNESS_LEVELS)
				{
					mSelectedLevel = position;
					new CConfirmDeleteDialog().show(getSupportFragmentManager(), DIALOG_TAG_CONFIRM_DELETE);
				}
				else
				{
					CMessageDialog.newInstance(getString(R.string.minimumLevelsMessage)).show(getSupportFragmentManager(), DIALOG_TAG_MESSAGE);
				}
				
				return true;
			}
		});
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mShowTitleCheck = (CheckBox) findViewById(R.id.showTitle_check);
		mShowTitleCheck.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean(CWidgetReceiver.PREFERENCES_SHOW_WIDGET_TITLE, isChecked);
				edit.commit();
				
				updateWidget();
			}
		});
		mShowTitleCheck.setChecked(prefs.getBoolean(CWidgetReceiver.PREFERENCES_SHOW_WIDGET_TITLE, true));
		
		mAddLevelButton = (Button) findViewById(R.id.addLevel_button);
		mAddLevelButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				mSelectedLevel = NEW_LEVEL_INDEX;
				CEditLevelDialog.newInstance(CBrightnessFileManager.BRIGHTNESS_LEVEL_DEFAULT).show(getSupportFragmentManager(), DIALOG_TAG_EDIT);
			}
		});
		
		mConfigFinishedButton = (Button) findViewById(R.id.configFinished_button);
		mConfigFinishedButton.setVisibility((mAppWidgetId == 0) ? View.GONE : View.VISIBLE);
		mConfigFinishedButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Context context = getApplicationContext();
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				
				RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
				appWidgetManager.updateAppWidget(mAppWidgetId, views);
				
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});
	}
	
	private void doDeleteSelectedLevel()
	{
		mBrightnessLevels.remove(mSelectedLevel);
		CBrightnessFileManager.saveBrightnessLevels(getApplicationContext(), mBrightnessLevels);
		updateUI();
	}
	
	private void doEditSelectedLevel(double _newLevel)
	{
		if (mSelectedLevel == NEW_LEVEL_INDEX)
		{
			mBrightnessLevels.add(Double.valueOf(_newLevel));
		}
		else
		{
			mBrightnessLevels.set(mSelectedLevel, Double.valueOf(_newLevel));
		}
		CBrightnessFileManager.saveBrightnessLevels(getApplicationContext(), mBrightnessLevels);
		updateUI();
	}
	
	private void updateUI()
	{
		mAddLevelButton.setEnabled(mBrightnessLevels.size() < CBrightnessFileManager.MAX_BRIGHTNESS_LEVELS);
		((CConfigurationListAdapter) mListView.getAdapter()).notifyDataSetChanged();
	}
	
	private void updateWidget()
	{
		Intent intent = new Intent(CConfigurationActivity.this, CWidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		int[] ids = { R.xml.widget_info };
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		sendBroadcast(intent);
	}
	
	private class CConfigurationListAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return mBrightnessLevels.size();
		}
		
		@Override
		public Object getItem(int position)
		{
			return mBrightnessLevels.get(position);
		}
		
		@Override
		public long getItemId(int position)
		{
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			CConfigurationCell res = null;
			
			if (convertView != null && convertView.getClass() == CConfigurationCell.class)
			{
				res = (CConfigurationCell) convertView;
			}
			else
			{
				res = (CConfigurationCell) LayoutInflater.from(CConfigurationActivity.this).inflate(R.layout.cell_configuration, null);
			}
			
			Double level = (Double) getItem(position);
			String levelString = (level == CBrightnessFileManager.BRIGHTNESS_LEVEL_AUTO) ? getString(R.string.auto) : String.format(getString(R.string.percentLevelFormat), (int) (level * 100));
			res.setText(String.format(getString(R.string.cellIndexFormat), position + 1), levelString);
			
			return res;
		}
	}
	
	public static class CMessageDialog extends DialogFragment
	{
		public static CMessageDialog newInstance(String _message)
		{
			CMessageDialog f = new CMessageDialog();
			
			Bundle args = new Bundle();
			args.putString("message", _message);
			f.setArguments(args);
			
			return f;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getArguments().getString("message")).setPositiveButton(R.string.ok, null);
			return builder.create();
		}
	}
	
	public static class CConfirmDeleteDialog extends DialogFragment
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.deleteLevelConfirmationMessage).setNegativeButton(R.string.no, null).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					((CConfigurationActivity) getActivity()).doDeleteSelectedLevel();
				}
			});
			return builder.create();
		}
	}
	
	public static class CEditLevelDialog extends DialogFragment
	{
		private TextView mLevelText;
		private SeekBar mLevelSeekBar;
		
		public static CEditLevelDialog newInstance(double _level)
		{
			CEditLevelDialog f = new CEditLevelDialog();
			
			Bundle args = new Bundle();
			args.putDouble("level", _level);
			f.setArguments(args);
			
			return f;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			double initialLevel = getArguments().getDouble("level");
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_editlevel, null);
			mLevelSeekBar = (SeekBar) view.findViewById(R.id.level_seekbar);
			mLevelSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
			{
				@Override
				public void onStopTrackingTouch(SeekBar seekBar)
				{
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar)
				{
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					mLevelText.setText(String.format(getString(R.string.textLevelFormat), String.format(getString(R.string.percentLevelFormat), progress + 1)));
				}
			});
			
			mLevelText = (TextView) view.findViewById(R.id.level_text);
			
			if (initialLevel == CBrightnessFileManager.BRIGHTNESS_LEVEL_AUTO)
			{
				mLevelSeekBar.setProgress(100 / 2);
				mLevelText.setText(String.format(getString(R.string.textLevelFormat), getString(R.string.auto)));
			}
			else
			{
				final int levelPercent = (int) (100 * initialLevel);
				mLevelSeekBar.setProgress(levelPercent - 1);
				mLevelText.setText(String.format(getString(R.string.textLevelFormat), String.format(getString(R.string.percentLevelFormat), levelPercent)));
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setView(view).setNegativeButton(R.string.cancel, null).setNeutralButton(R.string.auto, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					((CConfigurationActivity) getActivity()).doEditSelectedLevel(CBrightnessFileManager.BRIGHTNESS_LEVEL_AUTO);
				}
			}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					((CConfigurationActivity) getActivity()).doEditSelectedLevel((double) (mLevelSeekBar.getProgress() + 1) / 100);
				}
			});
			
			AlertDialog res = builder.create();
			res.setCanceledOnTouchOutside(false);
			return res;
		}
	}
}
