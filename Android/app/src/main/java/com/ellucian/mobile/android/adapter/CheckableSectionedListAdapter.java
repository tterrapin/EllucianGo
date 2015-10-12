/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import android.content.Context;
import android.widget.Adapter;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

public class CheckableSectionedListAdapter extends SectionedListAdapter {
	
	public CheckableSectionedListAdapter(Context context) {
		super(context);
	}
	
	public CheckableSectionedListAdapter(Context context, int headerResId) {
		super(context, headerResId);
	}
	
	@Override
	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		if (!(adapter instanceof CheckableCursorAdapter)) {
			throw new IllegalStateException("Adapter is not compatible, must be of type: CheckableCursorAdapter");
		}
		this.sections.add(adapter);
		this.identifiers.add(null);
	}
	
	@Override
	public void addSection(String section, String identifier, Adapter adapter) {
		this.headers.add(section);
		if (!(adapter instanceof CheckableCursorAdapter)) {
			throw new IllegalStateException("Adapter is not compatible, must be of type: CheckableCursorAdapter");
		}
		this.sections.add(adapter);
		this.identifiers.add(identifier);
	}

	public List<Integer> getCheckedPositions() {
		List<Integer> checkedPositions = new ArrayList<Integer>();
		int position = 0;
		for (Adapter adapter : this.sections) {
			CheckableCursorAdapter cursorAdapter =  (CheckableCursorAdapter)adapter;
			if (position > 0) {
				position++;
			}
			for (boolean checkedState : cursorAdapter.checkedStates) {
				position++;
				if (checkedState) {
					checkedPositions.add(position);
				}
			}	
		}
		return checkedPositions;
	}
	
	public void clearCheckedPositions() {
		for (Adapter adapter : this.sections) {
			CheckableCursorAdapter cursorAdapter =  (CheckableCursorAdapter)adapter;

			cursorAdapter.resetCheckedStates();	
		}
	}
	
	public CheckBox getCheckBoxAtPosition(int position) {
		
		for(int i = 0; i < this.headers.getCount(); i++) {
			CheckableCursorAdapter adapter = (CheckableCursorAdapter) sections.get(i);

			int size = adapter.getCount() + 1;

			if (position < size) {
				return adapter.getCheckBoxAtPosition(position);
			}
			// otherwise jump into next section
			position -= size;
		}
		return null;
		
	}

}

