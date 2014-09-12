package com.stopwatch.delta.t;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TAdapter extends ArrayAdapter<TimeRecord> implements Serializable {

	private static final long serialVersionUID = 1L;
	ArrayList<TimeRecord> myList;
	boolean selected;
	TimeRecord temp1 = null; //the first
	TimeRecord temp2 = null; //the second
	long temp1Mills;
	long temp2Mills;
	TimeRecord temp3 = null; //the difference
	TextView firstText = null;
	TextView secondText = null;
	AlertDialog deltaT;
	
	public TAdapter(Context c, int resourceId, ArrayList<TimeRecord> list) {
		super(c, resourceId, list);
		myList = list;
		temp3 = new TimeRecord(0);
	}
	
	public ArrayList<TimeRecord> getArrayList() {
		return myList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		if(convertView == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflator.inflate(R.layout.row_layout, null);
		}
		
		TimeRecord t = myList.get(position);
		if(t != null) {
			final TextView index = (TextView) convertView.findViewById(R.id.tvIndex);
			final TextView record = (TextView) convertView.findViewById(R.id.tvRecord);
			final ImageView deleteIcon = (ImageView) convertView.findViewById(R.id.ivDeleteIcon);
			
			index.setText(String.format("%02d", position) + ")");
			record.setText(myList.get(position).toString());
			record.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(selected) {
						if(getItem(position).milliseconds == temp1.milliseconds) { //cancel temp1
							temp1 = null;
							firstText.setTextColor(Color.WHITE);
							firstText = null;
						} else {
							temp2 = getItem(position);
							temp2Mills = temp2.milliseconds;
							secondText = record;
							secondText.setTextColor(Color.RED);
							deltaT = new AlertDialog.Builder(getContext()).create();
							temp3.setMilliseconds(Math.abs(temp1Mills - temp2Mills));  //temp1.milliseconds - temp2.milliseconds
							deltaT.setMessage(firstText.getText() + " - " + secondText.getText() + "\n" + "\n" +
									"\u0394" + "t = " + temp3); //http://unicode-table.com/en/#control-character
							deltaT.setButton(AlertDialog.BUTTON_POSITIVE, "Close", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(which == AlertDialog.BUTTON_POSITIVE) {
										firstText.setTextColor(Color.WHITE);
										firstText = null;
										secondText.setTextColor(Color.WHITE);
										secondText = null;
										temp1 = null;
										temp2 = null;
										deltaT.dismiss();
									}
								}
							});
							deltaT.show();
						}
						selected = false;
					}
					else { //selecting temp1
						temp1 = getItem(position);
						temp1Mills = temp1.milliseconds;
						firstText = record;
						firstText.setTextColor(Color.GREEN);
						selected = true;
					}
				}
			});
			deleteIcon.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					myList.remove(position);
					temp1 = null;
					selected = false;
					if(firstText != null)
						firstText.setTextColor(Color.WHITE);
					notifyDataSetChanged();
				}
			});
		}
		return convertView;
	}
}
