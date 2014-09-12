package com.stopwatch.delta.t;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SavedLists extends ListActivity implements DialogInterface.OnClickListener {

	private ArrayList<String> names = new ArrayList<String>();
	ArrayAdapter<String> mainAdapter;
	private TextView tvNoList;
	
	AlertDialog viewDialog;
	ListView listView;
	ArrayList<TimeRecord> list = new ArrayList<TimeRecord>();
	TAdapter loadedAdapter;
	int position;
	String filename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_lists);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		loadFileNames();
		
		tvNoList = (TextView) findViewById(R.id.tvNoLists);
		updateNoLists();
		
		mainAdapter = new ArrayAdapter<String>(this, R.layout.row_lists_layout, names);
		setListAdapter(mainAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		this.position = position;
		filename = names.get(position);
		
		viewDialog = new AlertDialog.Builder(this).setTitle(names.get(position)).create();
		listView = new ListView(this);
		listView.setBackgroundColor(Color.BLACK);
		
		//in-dialog adapter setting
		loadList(filename);
		listView.setAdapter(loadedAdapter);
		
		viewDialog.setView(listView);
		viewDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete", this);
		viewDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close", this);
		viewDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", this);
		
		viewDialog.show();
	}
	
	public void updateNoLists() {
		if(names.size() !=  0) {
			tvNoList.setVisibility(View.GONE);
		} else {
			tvNoList.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			deleteAt(position);
			updateNoLists();
			break;
		case AlertDialog.BUTTON_NEUTRAL:
			viewDialog.dismiss();
			break;
		case AlertDialog.BUTTON_POSITIVE:
			save((TAdapter) listView.getAdapter(), filename);
			break;

		default:
			break;
		}
		
	}
	
	/**
	 * Remove from names list, delete the saved file
	 * @param index
	 */
	public void deleteAt(int index) {
		names.remove(index);
		saveFileNames();
		
		Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
		mainAdapter.notifyDataSetChanged();
		viewDialog.dismiss();
	}
	
	public void save(TAdapter ta, String filename) 
	{
		try 
		{
			FileOutputStream fos = getApplicationContext().openFileOutput(filename, MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ta.getArrayList()); 
			fos.close(); oos.close();
		} catch (IOException e) {
				
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

	@SuppressWarnings("unchecked")
	public void loadList(String filename) {
		try {
			FileInputStream fis = getApplicationContext().openFileInput(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			loadedAdapter = new TAdapter(this, R.layout.row_layout, (ArrayList<TimeRecord>) ois.readObject());
		} catch (IOException e) {
			Toast.makeText(this, "IO!", Toast.LENGTH_SHORT).show();
		} catch (ClassNotFoundException e) {

		}
	}
	
	
	
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

}
