package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.widget.Filter;
import android.widget.Filterable;

public abstract class FilteredSectionedAdapter extends SectionedAdapter
		implements Filterable {

	private class SectionedFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence query) {
			final FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<Section>(sections);
				}
			}

			if (query == null || query.length() == 0) {
				synchronized (mLock) {
					final ArrayList<Section> list = new ArrayList<Section>(
							mOriginalValues);
					results.values = list;
					results.count = list.size();
				}
			} else {
				final String queryString = query.toString().toLowerCase();

				final ArrayList<Section> values = mOriginalValues;
				final int count = values.size();
				final ArrayList<Section> newValues = new ArrayList<Section>(
						count);
				
				
				for (final Section s : values) {
				
					if (s.adapter instanceof Filterable) {
						final Filterable adapter = (Filterable) s.adapter;
						newValues.add(s);
						
						adapter.getFilter().filter(queryString);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			sections = (List<Section>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private SectionedFilter mFilter;

	private final Object mLock = new Object();

	private ArrayList<Section> mOriginalValues;

	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new SectionedFilter();
		}
		return mFilter;
	}
}