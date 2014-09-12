package com.stopwatch.delta.t;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeltaT extends Activity implements android.content.DialogInterface.OnClickListener, OnClickListener {

	private TextView tvTime;
	private Button bStartPause;
	private Button bReset;
	private ListView timeList;
	private Button bSave;
	private Button bLists;
	ArrayList<TimeRecord> millsArray;
	TAdapter adapter;
	
	boolean running = false;
	private long startTime = 0;
	private Handler myHandler = new Handler();
	long timeInMills = 0;
	long timeSwap = 0;
	long finalTime = 0;
	
	AlertDialog saveDialog;
	boolean saved = false; //if true, user can close app without checking unsaved changes
	EditText etFileNameInput;
	String cacheName = ""; //previous name input for convenience
	AlertDialog saveChangesDialog; //open when user quits with unsaved changes
	
	ArrayList<String> names = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delta_t);
		
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvTime.setOnClickListener(this);
		bStartPause = (Button) findViewById(R.id.bStartPause);
		bStartPause.setOnClickListener(this);
		bReset = (Button) findViewById(R.id.bReset);
		bReset.setOnClickListener(this);
		timeList = (ListView) findViewById(R.id.lvTimeList);
		bSave = (Button) findViewById(R.id.bSave);
		bSave.setOnClickListener(this);
		bLists = (Button) findViewById(R.id.bLists);
		bLists.setOnClickListener(this);
		
		millsArray = new ArrayList<TimeRecord>();
		adapter = new TAdapter(this, R.layout.row_layout, millsArray);
		timeList.setAdapter(adapter);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //fix vertical screen
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadFileNames();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (running) {
			bStartPause.setText("Start");
			bReset.setClickable(true);
			bReset.setTextColor(Color.WHITE);
			timeSwap += timeInMills;
			myHandler.removeCallbacks(updateTimerMethod);
			running = false;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.bStartPause:
			if (running) {
				bStartPause.setText("Start");
				bReset.setClickable(true);
				bReset.setTextColor(Color.WHITE);
				timeSwap += timeInMills;
				myHandler.removeCallbacks(updateTimerMethod);
				running = false;
			} else {
				bStartPause.setText("Pause");
				bReset.setClickable(false);
				bReset.setTextColor(Color.GRAY);
				startTime = SystemClock.uptimeMillis();
				myHandler.postDelayed(updateTimerMethod, 0);
				running = true;
			}
			break;
			
		case R.id.bReset:
			startTime = 0;
			timeInMills = 0;
			timeSwap = 0;
			finalTime = 0;
			tvTime.setText("0:00:000");
			break;
			
		case R.id.bSave:
			if(millsArray.size() > 0) 
			{ openSaveDialog(); }
			break;
			
		case R.id.bLists:
			Intent savedListsIntent = new Intent(this, SavedLists.class);
			startActivity(savedListsIntent);
			break;

		case R.id.tvTime:
			saved = false;
			updateList(finalTime);
			timeList.smoothScrollToPosition(adapter.getCount());
			break;
			
		default:
			break;
		}
		
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_POSITIVE:
			if(dialog == saveChangesDialog)
			{
				saveChangesDialog.dismiss();
				openSaveDialog();
			}
			else if(dialog == saveDialog) //save
			{
				if(etFileNameInput.getText().toString().equals("")) //if name field is not filled
				{
					Toast.makeText(this, "Enter name", Toast.LENGTH_SHORT).show();
				}
				else {
					save(adapter, etFileNameInput.getText().toString());
				}
			}
			break;
			
		case AlertDialog.BUTTON_NEUTRAL:
			saveChangesDialog.dismiss();
			break;
		
		case AlertDialog.BUTTON_NEGATIVE:
			if(dialog == saveChangesDialog)
			{ finish(); }
			else if(dialog == saveChangesDialog)
			{ saveDialog.dismiss(); }
			break;
			
		default:
			break;
		}
		
	}
	
	private Runnable updateTimerMethod = new Runnable() {
		
		@Override
		public void run() {
			timeInMills = SystemClock.uptimeMillis() - startTime;
			finalTime = timeSwap + timeInMills;
			
			int seconds = (int) (finalTime/1000);
			int minutes = seconds/60;
			seconds = seconds % 60;
			int milliseconds = (int)(finalTime % 1000);
			tvTime.setText("" + minutes + ":" + String.format("%02d", seconds) + ":" + String.format("%03d", milliseconds));
			myHandler.postDelayed(this, 0);
			
		}
	};
	
	
	/**
	 * Loads names list
	 */
	@SuppressWarnings("unchecked")
	public void loadFileNames() {
		try {
			FileInputStream fis = getApplicationContext().openFileInput("Names.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			names = (ArrayList<String>) ois.readObject();
			ois.close(); fis.close();
		} catch (Exception e) {
			
		}
	}
	
	public void saveFileNames() {
		try {
			Collections.sort(names);
			FileOutputStream fos = getApplicationContext().openFileOutput("Names.txt", MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(names);
			fos.close(); oos.close();
		} catch (IOException e) {
			
		}
	}
	

	/**
	 * Saves arraylist and filename to file
	 * @param ta The adapter to be saved
	 * @param filename The name given to the save file, used for calling files on load
	 */
	public void save(TAdapter ta, String filename) {
		//check duplicate file names
		boolean unique; //true if no duplicates
		if(names.contains(filename)) {
			unique = false;
			Toast.makeText(this, "A file with this name already exists", Toast.LENGTH_SHORT).show();
		} else {
			unique = true;
		}
		//saves file as the name
		if(unique) {
			try {
				cacheName = filename;
				names.add(filename);
				saveFileNames();
				
				FileOutputStream fos = getApplicationContext().openFileOutput(filename, MODE_PRIVATE);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(ta.getArrayList()); 
				fos.close(); oos.close();
				
				Toast.makeText(this, "Saved as " + filename, Toast.LENGTH_SHORT).show();
				saveDialog.dismiss();
				saved = true;
			} catch (IOException e) {
				
			}
		}
		
	}
	
	/**
	 * Opens save dialog
	 * Called when user saves the adapter
	 */
	public void openSaveDialog() {
		saveDialog = new AlertDialog.Builder(DeltaT.this).create();
		saveDialog.setView(getLayoutInflater().inflate(R.layout.save_dialog_layout, null));
		saveDialog.setTitle("Save as");
		saveDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", this);
		saveDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
		saveDialog.show();
		
		etFileNameInput = (EditText) saveDialog.findViewById(R.id.etFileNameInput);
		etFileNameInput.requestFocus();
		if(!cacheName.equals(""))
		{ etFileNameInput.setText(cacheName);}
	}
	
	/**
	 * Checks unsaved changes to adapter
	 * If changes exist, opens save dialog
	 */
	@Override
	public void onBackPressed() {
		if(adapter.getCount() > 0 && !saved) {
			saveChangesDialog = new AlertDialog.Builder(this).create();
			saveChangesDialog.setMessage("There are unsaved changes to this list.\nAre you sure you want to quit?");
			saveChangesDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", this);
			saveChangesDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", this);
			saveChangesDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Quit", this);
			saveChangesDialog.show();
		}
		else finish();
	}

	public void updateList(long t) {
		TimeRecord tr = new TimeRecord(t);
		millsArray.add(tr);
		adapter.notifyDataSetChanged();
	}
}
