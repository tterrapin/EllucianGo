/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.finances;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.client.ResponseObject;
import com.ellucian.mobile.android.client.grades.Student;

public class TransactionsResponse implements ResponseObject<TransactionsResponse>, Parcelable {
    private Student student;
    public String currencyCode;
    public TransactionTerm[] terms;

    public TransactionsResponse(Student student, String currencyCode, TransactionTerm[] terms) {
        this.student = student;
        this.currencyCode = currencyCode;
        this.terms = terms;
    }

    public TransactionsResponse(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        student = in.readParcelable(Student.class.getClassLoader());
        currencyCode = in.readString();
        terms = in.createTypedArray(TransactionTerm.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(student, flags);
        dest.writeString(currencyCode);
        dest.writeTypedArray(terms, flags);
    }

    public static final Creator<TransactionsResponse> CREATOR = new Creator<TransactionsResponse>() {
        @Override
        public TransactionsResponse createFromParcel(Parcel source) {
            return new TransactionsResponse(source);
        }

        @Override
        public TransactionsResponse[] newArray(int size) {
            return new TransactionsResponse[size];
        }
    };
}
