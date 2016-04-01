/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;


public abstract class EllucianRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected OnItemClickListener onItemClickListener;
    private View lastSelected;
    private int selectedIndex = -1;

    public interface OnItemClickListener {
        void onItemClicked(View view, int position);
    }

    /**
     * Interface used by the ItemHolder pattern. View ItemHolderRecyclerAdapter or SectionedItemHolderAdapter
     * for an example.
     */
    public interface ItemInfoHolder {
        String getDefaultText();
    }

    /**
     * This is the default ViewHolder used with EllucianRecyclerAdapter.
     * A class that extends EllucianRecyclerAdapter may use its own ViewHolder.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View itemView;
        public final OnItemClickListener listener;
        public final EllucianRecyclerAdapter parentAdapter;

        public ItemViewHolder(EllucianRecyclerAdapter a, View v, OnItemClickListener l) {
            super(v);
            parentAdapter = a;
            v.setOnClickListener(this);
            itemView = v;
            listener = l;
        }

        @Override
        public void onClick(View v) {
            int currentPosition = getLayoutPosition();

            if (parentAdapter.isClickable(currentPosition)) {
                parentAdapter.itemSelected(v, currentPosition);

                if (listener != null) {
                    listener.onItemClicked(v, currentPosition);
                }
            }
        }
    }

    public void setLastSelected(View v) {
        lastSelected = v;
    }

    public void setSelectedIndex(int value) {
        selectedIndex = value;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private void itemSelected(View v, int position) {
        if (lastSelected != null) {
            lastSelected.setSelected(false);
        }

        if (v != null) {
            v.setSelected(true);
        }

        lastSelected = v;
        selectedIndex = position;
    }

    public void clearSelected() {
        itemSelected(null, -1);
    }

    public void checkViewForSelection(View v, int position) {
        if (position == selectedIndex) {
            itemSelected(v, position);
        } else {
            v.setSelected(false);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    /**
     * The getItem method is meant to return an item from the data set managed by subclass of the adapter.
     */
    abstract public Object getItem(int position);

    /**
     * This checks to see if a specific item will trigger the click events for that position.
     * Default is to return true and all items in list will trigger on click.
     * Override in subclass to change.
     */
    boolean isClickable(int position) {
        return true;
    }
}
