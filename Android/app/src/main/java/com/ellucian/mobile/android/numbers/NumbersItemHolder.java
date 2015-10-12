/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.numbers;

import com.ellucian.mobile.android.adapter.EllucianRecyclerAdapter;

public class NumbersItemHolder implements EllucianRecyclerAdapter.ItemInfoHolder, Comparable<NumbersItemHolder> {
	public String name;
    public String type;
	public String address;
    public String email;
	public String phone;
	public String extension;
	public String buildingId;
	public String campusId;
    public double latitude;
    public double longitude;

	NumbersItemHolder() {
	}
	
	NumbersItemHolder(String name, String type, String address, String email, String phone,
                      String extension, String buildingId, String campusId,
                      double latitude, double longitude) {
        this.name = name;
		this.type = type;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.extension = extension;
        this.buildingId = buildingId;
        this.campusId = campusId;
        this.latitude = latitude;
        this.longitude = longitude;
	}
		
	@Override
	public String getDefaultText() {
		return name;
	}

	@Override
	public int compareTo(NumbersItemHolder other) {
        if (this == other) {
            return 0;
        }

        // If they are in same category, sort by name
        if (this.type.compareToIgnoreCase(other.type) == 0 ) {
            return this.name.compareToIgnoreCase(other.name);
        } else {
            return this.type.compareToIgnoreCase(other.type);
        }

	}

}
