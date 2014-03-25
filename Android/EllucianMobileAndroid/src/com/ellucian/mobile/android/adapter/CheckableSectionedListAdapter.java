package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Adapter;

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

}

