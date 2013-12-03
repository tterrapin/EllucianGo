package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

public abstract class SectionedAdapter extends BaseAdapter {
	class Section {
		Adapter adapter;
		String caption;

		Section(String caption, Adapter adapter) {
			this.caption = caption;
			this.adapter = adapter;
		}
	}

	private static int TYPE_SECTION_HEADER = 0;
	List<Section> sections = new ArrayList<Section>();

	public void addSection(String caption, Adapter adapter) {
		sections.add(new Section(caption, adapter));
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public int getCount() {
		int total = 0;
		for (final Section section : this.sections) {
			total += section.adapter.getCount() + 1; // add one for header
		}
		return total;
	}

	abstract protected View getHeaderView(String caption, int index,
			View convertView, ViewGroup parent);

	public Object getItem(int position) {
		for (final Section section : this.sections) {
			if (position == 0) {
				return section;
			}
			final int size = section.adapter.getCount() + 1;
			if (position < size) {
				return section.adapter.getItem(position - 1);
			}
			position -= size;
		}
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		int typeOffset = TYPE_SECTION_HEADER + 1; // start counting from here
		for (final Section section : this.sections) {
			if (position == 0) {
				return TYPE_SECTION_HEADER;
			}
			final int size = section.adapter.getCount() + 1;
			if (position < size) {
				return typeOffset
						+ section.adapter.getItemViewType(position - 1);
			}
			position -= size;
			typeOffset += section.adapter.getViewTypeCount();
		}
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionIndex = 0;
		for (final Section section : this.sections) {
			if (position == 0) {
				return getHeaderView(section.caption, sectionIndex,
						convertView, parent);
			}
			final int size = section.adapter.getCount() + 1;
			if (position < size) {
				return section.adapter.getView(position - 1, convertView,
						parent);
			}
			position -= size;
			sectionIndex++;
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		int total = 1; // one for the header, plus those from sections
		for (final Section section : this.sections) {
			total += section.adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_SECTION_HEADER;
	}
}