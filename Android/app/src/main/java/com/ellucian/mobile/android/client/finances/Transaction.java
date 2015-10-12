// Copyright 2015 Ellucian Company L.P. and its affiliates.

package com.ellucian.mobile.android.client.finances;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.util.CalendarUtils;

import java.util.Date;

public class Transaction implements Comparable<Transaction>, Parcelable{
    public Double amount; //
    public String description;
    public Date entryDate;
    private String type;

    public Transaction(Double amount, String description, String entryDate, String type) {
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.entryDate = CalendarUtils.parseFromUTC(entryDate);
    }

    public Transaction(Double amount, String description, Date entryDate, String type){
        this.amount = amount;
        this.description = description;
        this.entryDate = entryDate;
        this.type = type;
    }

    private Transaction(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        amount = in.readDouble();
        description = in.readString();
        entryDate = new Date(in.readLong());
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(amount);
        dest.writeString(description);
        dest.writeLong(entryDate.getTime());
        dest.writeString(type);
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel source) {
            return new Transaction(source);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public int compareTo(Transaction another) {
        return entryDate.compareTo(another.entryDate);
    }
}
