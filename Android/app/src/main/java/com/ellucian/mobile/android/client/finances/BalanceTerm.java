/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.finances;

import android.os.Parcel;
import android.os.Parcelable;

public class BalanceTerm implements Parcelable {
    private String description;
    public String termId;
    public Double balance;

    public BalanceTerm(String description, String termId, Double balance) {
        this.description = description;
        this.termId = termId;
        this.balance = balance;
    }

    private BalanceTerm(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        description = in.readString();
        termId = in.readString();
        balance = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(termId);
        dest.writeDouble(balance);
    }

    public static final Creator<BalanceTerm> CREATOR = new Creator<BalanceTerm>() {
        @Override
        public BalanceTerm createFromParcel(Parcel source) {
            return new BalanceTerm(source);
        }

        @Override
        public BalanceTerm[] newArray(int size) {
            return new BalanceTerm[size];
        }
    };
}
