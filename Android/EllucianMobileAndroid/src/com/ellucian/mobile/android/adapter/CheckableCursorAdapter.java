package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;

public class CheckableCursorAdapter extends SimpleCursorAdapter {
	public List<Boolean> checkedStates = new ArrayList<Boolean>();
	
	private List<OnCheckBoxClickedListener> listenerList = new ArrayList<OnCheckBoxClickedListener>();
	
	private Context context;
	private int layout;
	private Cursor cursor;
	private String[] from;
	private int[] to;
	private int flags;
	private int checkBoxResId;

	public CheckableCursorAdapter(Context context, int layout,
			Cursor c, String[] from, int[] to, int flags, int checkBoxResId) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		this.cursor = c;
		this.from = from;
		this.to = to;
		this.flags = flags;
		this.checkBoxResId = checkBoxResId;
		for (int i = 0; i < c.getCount(); i++) {
			checkedStates.add(false);
		}
	}
	
	public interface OnCheckBoxClickedListener {
		
		public void onCheckBoxClicked(boolean isChecked, int position);
	}
	
	public void registerOnCheckBoxClickedListener(OnCheckBoxClickedListener value) {
		listenerList.add(value);
	}
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (!cursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(layout, null);

		CheckBox checkBox = (CheckBox) row.findViewById(checkBoxResId);

		checkBox.setOnClickListener(new OnClickListener(){ 

			@Override
			public void onClick(View v) {
				CheckBox checkBox = (CheckBox)v;
				boolean checked = checkBox.isChecked();
				if (checked) {
					checkedStates.set(position, true);
				} else {
					checkedStates.set(position, false);
				}
				for (OnCheckBoxClickedListener listener : listenerList) {
					listener.onCheckBoxClicked(checked, position);
				}
			}

		});
					
		boolean state = checkedStates.get(position);

		checkBox.setChecked(state);
		bindView(row, context, cursor);
		return row;
	}
	
	public List<Integer> getCheckedPositions() {
		List<Integer> checkedPositions = new ArrayList<Integer>();
		for (int i = 0; i < checkedStates.size(); i++) {
			if (checkedStates.get(i)) {
				checkedPositions.add(i);
			}
		} 
		return checkedPositions;
	}
	
	public boolean[] getCheckedStatesAsBooleanArray() {
		boolean[] booleanArray = new boolean[checkedStates.size()];
		for (int i = 0; i < checkedStates.size(); i++) {
			booleanArray[i] = checkedStates.get(i);
		}
		return booleanArray;
	}
	
	public void setCheckedStates(List<Boolean> value) {
		checkedStates = value;
	}
	
	public void setCheckedStates(boolean[] value) {
		checkedStates = new ArrayList<Boolean>();
		for (boolean state : value) {
			checkedStates.add(state);
		}
	}
	
	public boolean isPositionChecked(int position) {
		return checkedStates.get(position);
	}

	public void setCheckedPositionState(int position, boolean value) {
		checkedStates.set(position, value);
	}

	public void resetCheckedState() {
		for (int i = 0; i < checkedStates.size(); i++) {
			checkedStates.set(i, false);
		}
	}

	
}
