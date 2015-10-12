/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.elluciango.R;

import java.util.ArrayList;

public class SectionedItemHolderRecyclerAdapter extends EllucianRecyclerAdapter {
    public final static int TYPE_SECTION_HEADER = 0;
    public final static int TYPE_SECTION_ITEM = 1;

    protected final Context context;
    protected final ArrayList<ArrayList<? extends ItemInfoHolder>> sections = new ArrayList<ArrayList<? extends ItemInfoHolder>>();
    protected final ArrayList<ItemInfoHolder> headers = new ArrayList<ItemInfoHolder>();
    protected final int headerLayoutResId;
    protected final int headerResId;
    protected final int itemLayoutResId;
    protected final int itemResId;

    /**
     * Adapter created with the Default constructor will use default layouts for header and item rows.
     */
	public SectionedItemHolderRecyclerAdapter(Context context) {
    	this.context = context;
    	this.headerLayoutResId = R.layout.default_header_row;
    	this.headerResId = R.id.header;
    	this.itemLayoutResId = R.layout.default_single_line_row;
    	this.itemResId = R.id.title;
    }

    /**
     * This constructor allows to use the a custom layout and target views for header and item titles.
     * Extend class and override onCreateHeaderViewHolder, onCreateItemViewHolder, onBindHeaderViewHolder,
     * and onBindItemViewHolder for more options.
     */
    public SectionedItemHolderRecyclerAdapter(Context context, int headerLayoutResId, int headerResId, 
    		int itemLayoutResId, int itemResId) {
    	this.context = context;
    	this.headerLayoutResId = headerLayoutResId;
    	this.headerResId = headerResId;
    	this.itemLayoutResId = itemLayoutResId;
    	this.itemResId = itemResId;
    }
    
    public void addSection(ItemInfoHolder sectionHeaderInfo, ArrayList<? extends ItemInfoHolder> itemList) {
    	headers.add(sectionHeaderInfo);
    	sections.add(itemList);
	}

    public Object getItem(int position) {
		for(int i = 0; i < this.headers.size(); i++) {
			ArrayList<? extends ItemInfoHolder> section = sections.get(i);
			Object header = headers.get(i);

			int size = section.size() + 1;

			// check if position inside this section
			if (position == 0)
				return header;
			if (position < size)
				return section.get(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}
	
	public ArrayList<? extends ItemInfoHolder> getItemListForPosition(int position) {
		for(int i = 0; i < this.headers.size(); i++) {
			ArrayList<? extends ItemInfoHolder> sectionList = sections.get(i);

			int size = sectionList.size() + 1;

			if (position < size)
				return sectionList;

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}
	
	@Override
	public int getItemCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (ArrayList<? extends ItemInfoHolder> section : this.sections)
			total += section.size() + 1;
		return total;
	}
	
	public int getItemCountWithoutHeaders() {
		int total = 0;
		for (ArrayList<? extends ItemInfoHolder> section : this.sections)
			total += section.size();
		return total;
	}
	
	@Override
	public int getItemViewType(int position) {
		for(int i = 0; i < this.headers.size(); i++) {
			ArrayList<? extends ItemInfoHolder> section = sections.get(i);
			int size = section.size() + 1;

			// check if position inside this section
			if (position == 0)
				return TYPE_SECTION_HEADER;
			if (position < size)
				return TYPE_SECTION_ITEM;

			// otherwise jump into next section
			position -= size;
		}
		return -1;
	}

    @Override
    public boolean isClickable(int position) {
        if (getItemViewType(position) == TYPE_SECTION_HEADER) {
            return false;
        } else {
            return true;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view depending on the type
    	if (viewType == TYPE_SECTION_HEADER) {    		
    		return onCreateHeaderViewHolder(parent, viewType);
    	} else {

    		return onCreateItemViewHolder(parent, viewType);
    	}

    }
    
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(final ViewGroup parent, int viewType) {
    	View v;
    	
    	v = LayoutInflater.from(parent.getContext())
                .inflate(headerLayoutResId, parent, false);	
    	
    	ItemViewHolder vh = new ItemViewHolder(this, v, null);
        return vh;
    }
    
    public RecyclerView.ViewHolder onCreateItemViewHolder(final ViewGroup parent, int viewType) {
    	View v;
    	
    	v = LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutResId, parent, false);

    	ItemViewHolder vh = new ItemViewHolder(this, v, onItemClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    	
    	if (getItemViewType(position) == TYPE_SECTION_HEADER) {
    		onBindHeaderViewHolder(holder, position);
    	} else {
            checkViewForSelection(((ItemViewHolder)holder).itemView, position);
    		onBindItemViewHolder(holder, position);
    	}
    }
    
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
    	TextView headerView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(headerResId);
        ItemInfoHolder headerHolder = (ItemInfoHolder) getItem(position);
        String headerText = headerHolder.getDefaultText();
		headerView.setText(headerText);
    }

    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
    	ItemInfoHolder itemHolder = (ItemInfoHolder) getItem(position);
		
		TextView itemView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(itemResId);
		String itemText = itemHolder.getDefaultText();
		itemView.setText(itemText);
    }
}
