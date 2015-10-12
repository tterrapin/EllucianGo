/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.ilp;

import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;

public class IlpHeaderHolder implements EllucianRecyclerAdapter.ItemInfoHolder {
    public String day;
    public String date;

    public IlpHeaderHolder() {
    }

    public IlpHeaderHolder(String day, String date) {
        this.day = day;
        this.date = date;
    }

    @Override
    public String getDefaultText() {
        return day;
    }
}
