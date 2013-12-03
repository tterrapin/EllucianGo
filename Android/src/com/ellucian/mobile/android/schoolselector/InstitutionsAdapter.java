package com.ellucian.mobile.android.schoolselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * A ListAdapter derived from ArrayAdapter.
 * 
 * The ArrayAdapter was not used for two reasons. We want to modify the filter
 * to search for the query text in multiple fields of the Institution object. We
 * also want to use an object and not rely on the toString method returning the
 * desired display text.
 * 
 * @author Jason Hocker
 */
public class InstitutionsAdapter extends BaseAdapter implements Filterable {
	/**
	 * <p>
	 * An array filter constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	private class ArrayFilter extends Filter {
		private boolean containsQueryString(String queryString, String valueText) {
			valueText = valueText.toLowerCase();
			if (valueText.contains(queryString)) {
				return true;
			} else {
				final String[] words = valueText.split(" ");
				final int wordCount = words.length;

				for (int k = 0; k < wordCount; k++) {
					if (words[k].contains(queryString)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		protected FilterResults performFiltering(CharSequence query) {
			final FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<Institution>(mObjects);
				}
			}

			if (query == null || query.length() == 0) {
				synchronized (mLock) {
					final ArrayList<Institution> list = new ArrayList<Institution>(
							mOriginalValues);
					results.values = list;
					results.count = list.size();
				}
			} else {
				final String queryString = query.toString().toLowerCase();

				final ArrayList<Institution> values = mOriginalValues;
				final int count = values.size();

				final ArrayList<Institution> newValues = new ArrayList<Institution>(
						count);

				for (int i = 0; i < count; i++) {
					final Institution institution = values.get(i);

					// First match against the whole, non-splitted value
					if (containsQueryString(queryString,
							institution.getFullName())) {
						newValues.add(institution);
					} else if (containsQueryString(queryString,
							institution.getDisplayName())) {
						newValues.add(institution);
					} else {
						for (int k = 0; k < institution.getKeywords().size(); k++) {
							if (containsQueryString(queryString, institution
									.getKeywords().get(k))) {
								newValues.add(institution);
								break;
							}
						}
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
			mObjects = (List<Institution>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private Context mContext;

	/**
	 * If the inflated resource is not a TextView, {@link #mFieldId} is used to
	 * find a TextView inside the inflated views hierarchy. This field must
	 * contain the identifier that matches the one defined in the resource file.
	 */
	private int mFieldId = 0;

	// /**
	// * The resource indicating what views to inflate to display the content of
	// this
	// * array adapter in a drop down widget.
	// */
	// private int mDropDownResource;

	private ArrayFilter mFilter;

	private LayoutInflater mInflater;

	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is
	 * also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	private final Object mLock = new Object();

	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called
	 * whenever {@link #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;
	/**
	 * Contains the list of objects that represent the data of this
	 * ArrayAdapter. The content of this list is referred to as "the array" in
	 * the documentation.
	 */
	private List<Institution> mObjects;

	private ArrayList<Institution> mOriginalValues;

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter.
	 */
	private int mResource;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 */
	public InstitutionsAdapter(Context context, int textViewResourceId) {
		init(context, textViewResourceId, 0, new ArrayList<Institution>());
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public InstitutionsAdapter(Context context, int textViewResourceId,
			Institution[] objects) {
		init(context, textViewResourceId, 0, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 */
	public InstitutionsAdapter(Context context, int resource,
			int textViewResourceId) {
		init(context, resource, textViewResourceId,
				new ArrayList<Institution>());
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public InstitutionsAdapter(Context context, int resource,
			int textViewResourceId, Institution[] objects) {
		init(context, resource, textViewResourceId, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public InstitutionsAdapter(Context context, int resource,
			int textViewResourceId, List<Institution> objects) {
		init(context, resource, textViewResourceId, objects);
	}


	/**
	 * Constructor
	 * 
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public InstitutionsAdapter(Context context, int textViewResourceId,
			List<Institution> objects) {
		init(context, textViewResourceId, 0, objects);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view;
		TextView text;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		if (mFieldId == 0) {
			text = (TextView) view;
		} else {
			text = (TextView) view.findViewById(mFieldId);
		}

		final Institution item = getItem(position);
		text.setText(item.getFullName().toString());

		return view;
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 * 
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCount() {
		return mObjects.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Institution getItem(int position) {
		return mObjects.get(position);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Returns the position of the specified item in the array.
	 * 
	 * @param item
	 *            The item to retrieve the position of.
	 * 
	 * @return The position of the specified item.
	 */
	public int getPosition(Institution item) {
		return mObjects.indexOf(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private void init(Context context, int resource, int textViewResourceId,
			List<Institution> objects) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = /* mDropDownResource = */resource;
		mObjects = objects;
		mFieldId = textViewResourceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Control whether methods that change the list ({@link #add},
	 * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
	 * {@link #notifyDataSetChanged}. If set to false, caller must manually call
	 * notifyDataSetChanged() to have the changes reflected in the attached
	 * view.
	 * 
	 * The default is true, and calling notifyDataSetChanged() resets the flag
	 * to true.
	 * 
	 * @param notifyOnChange
	 *            if true, modifications to the list will automatically call
	 *            {@link #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 * 
	 * @param comparator
	 *            The comparator used to sort the objects contained in this
	 *            adapter.
	 */
	public void sort(Comparator<? super Institution> comparator) {
		Collections.sort(mObjects, comparator);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}
}