/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;
import com.ellucian.mobile.android.client.directory.Entry;
import com.ellucian.mobile.android.util.DownloadImageTask;

import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class DirectoryRecyclerAdapter extends EllucianRecyclerAdapter {
    Context context;

    protected ArrayList<Entry> entries = new ArrayList<>();

    public DirectoryRecyclerAdapter(Context context, Entry[] mEntries) {
        this.context = context;

        if (mEntries != null && mEntries.length > 0) {
            entries = new ArrayList<>(Arrays.asList(mEntries));
        }

    }

    public DirectoryRecyclerAdapter(Context context, ArrayList<Entry> mEntries) {
        this.context = context;
        if (mEntries != null) {
            entries = mEntries;
        }
    }

    @Override
    public int getItemCount() {
        return this.entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v;

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.directory_row, parent, false);

        return new ItemViewHolder(this, v, onItemClickListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        checkViewForSelection(((ItemViewHolder)holder).itemView, position);

        Object object = getItem(position);
        Entry infoHolder = (Entry) object;

        TextView nameView = (TextView) ((ItemViewHolder) holder).itemView.findViewById(R.id.directory_row_name);
        TextView groupView = (TextView) ((ItemViewHolder) holder).itemView.findViewById(R.id.directory_row_group);
        CircleImageView imageView = (CircleImageView) ((ItemViewHolder) holder).itemView.findViewById(R.id.directory_row_image);

        nameView.setText(infoHolder.getDisplayName(context));
        groupView.setText(infoHolder.type);
        if (!TextUtils.isEmpty(infoHolder.imageUrl)) {
            new DownloadImageTask(imageView).execute(infoHolder.imageUrl);
        }

    }

}

