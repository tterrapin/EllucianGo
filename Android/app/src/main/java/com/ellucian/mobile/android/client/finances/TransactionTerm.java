/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.finances;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionTerm implements Parcelable {
    private String description;
    public String termId;
    public Transaction[] transactions;

    public TransactionTerm(String description, String termId, Transaction[] transactions) {
        this.description = description;
        this.termId = termId;
        this.transactions = transactions;
    }

    private TransactionTerm(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        description = in.readString();
        termId = in.readString();
        transactions = in.createTypedArray(Transaction.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(termId);
        dest.writeTypedArray(transactions, flags);
    }

    public static final Creator<TransactionTerm> CREATOR = new Creator<TransactionTerm>() {
        @Override
        public TransactionTerm createFromParcel(Parcel source) {
            return new TransactionTerm(source);
        }

        @Override
        public TransactionTerm[] newArray(int size) {
            return new TransactionTerm[size];
        }
    };
}
