/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * A ListAdapter derived from ArrayAdapter.
 * 
 * The ArrayAdapter was not used for two reasons. We want to modify the filter
 * to search for the query text in multiple fields of the T object. We also want
 * to use an object and not rely on the toString method returning the desired
 * display text.
 * 
 * @author Jason Hocker
 */
@SuppressWarnings("JavaDoc")
public abstract class FilteredAdapter<T> extends BaseAdapter implements Filterable {
	/**
	 * <p>
	 * An array filter constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence query) {
			final FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<T>(mObjects);
				}
			}

			if (query == null || query.length() == 0) {
				synchronized (mLock) {
					final ArrayList<T> list = new ArrayList<T>(mOriginalValues);
					results.values = list;
					results.count = list.size();
				}
			} else {
				final String queryString = query.toString().toLowerCase(Locale.getDefault());

				final ArrayList<T> values = mOriginalValues;
				final int count = values.size();

				final ArrayList<T> newValues = new ArrayList<T>(count);

				for (int i = 0; i < count; i++) {
					final T obj = values.get(i);
					boolean match = matchesFilter(obj, queryString, i);
					if(match) {
						newValues.add(obj);
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
			mObjects = (List<T>) results.values;
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

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter in a drop down widget.
	 */
	private int mDropDownResource;

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
	private List<T> mObjects;

	private ArrayList<T> mOriginalValues;

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
	public FilteredAdapter(Context context, int textViewResourceId) {
		init(context, textViewResourceId, 0, new ArrayList<T>());
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
	public FilteredAdapter(Context context, int textViewResourceId, T[] objects) {
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
	public FilteredAdapter(Context context, int resource, int textViewResourceId) {
		init(context, resource, textViewResourceId, new ArrayList<T>());
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
	public FilteredAdapter(Context context, int resource, int textViewResourceId,
			T[] objects) {
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
	public FilteredAdapter(Context context, int resource, int textViewResourceId,
			List<T> objects) {
		init(context, resource, textViewResourceId, objects);
	}

	 /**
	 * Adds the specified object at the end of the array.
	 *
	 * @param object The object to add at the end of the array.
	 */
	 public void add(T object) {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 mOriginalValues.add(object);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 } else {
	 mObjects.add(object);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 }
	
	 /**
	 * Adds the specified Collection at the end of the array.
	 *
	 * @param collection The Collection to add at the end of the array.
	 */
	 public void addAll(Collection<? extends T> collection) {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 mOriginalValues.addAll(collection);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 } else {
	 mObjects.addAll(collection);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 }
	
	 /**
	 * Adds the specified items at the end of the array.
	 *
	 * @param items The items to add at the end of the array.
	 */
	 public void addAll(T ... items) {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 for (T item : items) {
	 mOriginalValues.add(item);
	 }
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 } else {
	 for (T item : items) {
	 mObjects.add(item);
	 }
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 }
	
	 /**
	 * Inserts the specified object at the specified index in the array.
	 *
	 * @param object The object to insert into the array.
	 * @param index The index at which the object must be inserted.
	 */
	 public void insert(T object, int index) {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 mOriginalValues.add(index, object);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 } else {
	 mObjects.add(index, object);
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	 }
	
	 /**
	 * Removes the specified object from the array.
	 *
	 * @param object The object to remove.
	 */
	 public void remove(T object) {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 mOriginalValues.remove(object);
	 }
	 } else {
	 mObjects.remove(object);
	 }
	 if (mNotifyOnChange) notifyDataSetChanged();
	 }
	
	 /**
	 * Remove all elements from the list.
	 */
	 public void clear() {
	 if (mOriginalValues != null) {
	 synchronized (mLock) {
	 mOriginalValues.clear();
	 }
	 } else {
	 mObjects.clear();
	 }
	 if (mNotifyOnChange) notifyDataSetChanged();
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
	public FilteredAdapter(Context context, int textViewResourceId, List<T> objects) {
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

		final T item = getItem(position);
		text.setText(getText(item));

		return view;
	}
	
	public abstract CharSequence getText(T obj);

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
	public T getItem(int position) {
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
	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private void init(Context context, int resource, int textViewResourceId,
			List<T> objects) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = mDropDownResource = resource;
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
	 * <p>
	 * Sets the layout resource to create the drop down views.
	 * </p>
	 * 
	 * @param resource
	 *            the layout resource defining the drop down views
	 * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
	 */
	public void setDropDownViewResource(int resource) {
		this.mDropDownResource = resource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				mDropDownResource);
	}

	/**
	 * Creates a new ArrayAdapter from external resources. The content of the
	 * array is obtained through
	 * {@link android.content.res.Resources#getTextArray(int)}.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param textArrayResId
	 *            The identifier of the array to use as the data source.
	 * @param textViewResId
	 *            The identifier of the layout used to create views.
	 * 
	 * @return An ArrayAdapter<CharSequence>.
	 */
	public static ArrayAdapter<CharSequence> createFromResource(
			Context context, int textArrayResId, int textViewResId) {
		CharSequence[] strings = context.getResources().getTextArray(
				textArrayResId);
		return new ArrayAdapter<CharSequence>(context, textViewResId, strings);
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
	public void sort(Comparator<? super T> comparator) {
		Collections.sort(mObjects, comparator);
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}
	
	public abstract boolean matchesFilter(T obj, String query, int position);
}