/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.grades;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;

class GradesListAdapter extends BaseAdapter {
	private final ArrayList<Adapter> sections = new ArrayList<Adapter>();
    private final ArrayList<GradesSectionHeaderAdapter> headers = new ArrayList<>();
	private final static int TYPE_SECTION_HEADER = 0;

	public GradesListAdapter() {}

    public void addSection(GradesSectionHeaderAdapter headerAdapter, Adapter adapter) {
        this.headers.add(headerAdapter);
		this.sections.add(adapter);
	}

	public Object getItem(int position) {
		for(int i = 0; i < this.headers.size(); i++) {
			Adapter section = sections.get(i);
            GradesSectionHeaderAdapter headerAdapter = headers.get(i);

			int size = section.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headerAdapter;
			if (position < size)
				return section.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections)
			total += adapter.getCount() + 1;
		return total;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections)
			total += adapter.getViewTypeCount();
		return total;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for(int i = 0; i < this.headers.size(); i++) {
			Adapter section = sections.get(i);
			int size = section.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return type + section.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += section.getViewTypeCount();
		}
		return -1;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(int i = 0; i < this.headers.size(); i++) {
			Adapter section = sections.get(i);
            GradesSectionHeaderAdapter headerAdapter = headers.get(i);

			int size = section.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headerAdapter.getView(0, convertView, parent);
			if (position < size)
				return section.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
