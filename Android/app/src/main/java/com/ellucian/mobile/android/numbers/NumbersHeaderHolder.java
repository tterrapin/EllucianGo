/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;

public class NumbersHeaderHolder implements EllucianRecyclerAdapter.ItemInfoHolder {
    public String category;

    public NumbersHeaderHolder() {
    }

    public NumbersHeaderHolder(String category) {
        this.category = category;
    }

    @Override
    public String getDefaultText() {
        return category;
    }
}
