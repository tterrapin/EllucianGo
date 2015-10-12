/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.elluciango.R;

public class ItemHolderRecyclerAdapter extends EllucianRecyclerAdapter {
	
	private final Context context;
	private final ArrayList<ItemInfoHolder> items = new ArrayList<ItemInfoHolder>();
	private final int itemLayoutResId;
	private final int itemResId;

    /**
     * Adapter created with the Default constructor will use default layouts for item rows.
     */
    public ItemHolderRecyclerAdapter(Context context) {
    	this.context = context;
    	this.itemLayoutResId = R.layout.default_single_line_row;
    	this.itemResId = R.id.title;
    }

    /**
     * This constructor allows to use the a custom layout and target views for item titles.
     * Extend class and override onCreateViewHolder, onBindItemViewHolder for more options.
     */
    public ItemHolderRecyclerAdapter(Context context, int itemLayoutResId, int itemResId) {
    	this.context = context;
    	this.itemLayoutResId = itemLayoutResId;
    	this.itemResId = itemResId;
    }
    
    public void addItem(ItemInfoHolder itemHolder) {
    	items.add(itemHolder);
    }
    
    public void addList(List<? extends ItemInfoHolder> list) {
    	for (ItemInfoHolder itemHolder : list) {
    		items.add(itemHolder);
    	}
    }

    public Object getItem(int position) {		
		return items.get(position);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // create a new view depending on the type
    	View v;
    	
    	v = LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutResId, parent, false);

    	ItemViewHolder vh = new ItemViewHolder(this, v, onItemClickListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        checkViewForSelection(((ItemViewHolder)holder).itemView, position);
        onBindItemViewHolder(holder, position);
    }

    private void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ItemInfoHolder infoHolder = (ItemInfoHolder) getItem(position);

        TextView itemView = (TextView) ((ItemViewHolder)holder).itemView.findViewById(itemResId);
        String itemText = infoHolder.getDefaultText();
        itemView.setText(itemText);
    }

}
