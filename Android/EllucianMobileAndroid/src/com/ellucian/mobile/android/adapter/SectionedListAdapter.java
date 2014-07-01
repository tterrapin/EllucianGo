package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.ellucian.elluciango.R;

public class SectionedListAdapter extends BaseAdapter {
	public final ArrayList<Adapter> sections = new ArrayList<Adapter>();
	public final ArrayAdapter<String> headers;
	public final ArrayList<String> identifiers = new ArrayList<String>();
	public final static int TYPE_SECTION_HEADER = 0;

	public SectionedListAdapter(Context context) {
		headers = new ArrayAdapter<String>(context, R.layout.list_header);
	}
	
	public SectionedListAdapter(Context context, int headerResId) {
		headers = new ArrayAdapter<String>(context, headerResId);
	}

	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.add(adapter);
		this.identifiers.add(null);
	}
	
	/**
	 *  Adds an alternate identifier to each section that is can be different then 
	 *  the displayed title
	 */
	public void addSection(String section, String identifier, Adapter adapter) {
		this.headers.add(section);
		this.sections.add(adapter);
		this.identifiers.add(identifier);
	}

	public Object getItem(int position) {
		for(int i = 0; i < this.headers.getCount(); i++) {
			Adapter section = sections.get(i);
			String header = headers.getItem(i);

			int size = section.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return header;
			if (position < size)
				return section.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}
	
	public Adapter getSectionAdapterForPosition(int position) {
		for(int i = 0; i < this.headers.getCount(); i++) {
			Adapter sectionAdapter = sections.get(i);

			int size = sectionAdapter.getCount() + 1;

			if (position < size)
				return sectionAdapter;

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
	
	public int getCountWithoutHeaders() {
		int total = 0;
		for (Adapter adapter : this.sections)
			total += adapter.getCount();
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
		for(int i = 0; i < this.headers.getCount(); i++) {
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
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;
		for(int i = 0; i < this.headers.getCount(); i++) {
			Adapter section = sections.get(i);

			int size = section.getCount() + 1;

			// check if position inside this section
			if (position == 0)
				return headers.getView(sectionnum, convertView, parent);
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
