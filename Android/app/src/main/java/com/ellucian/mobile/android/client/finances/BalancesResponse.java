/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.finances;

import android.os.Parcel;
import android.os.Parcelable;

import com.ellucian.mobile.android.client.ResponseObject;
import com.ellucian.mobile.android.client.grades.Student;

public class BalancesResponse implements ResponseObject<BalancesResponse>, Parcelable{
    private Student student;
    public String currencyCode;
    public BalanceTerm[] terms;

    public BalancesResponse(Student student, String currencyCode, BalanceTerm[] terms) {
        this.student = student;
        this.currencyCode = currencyCode;
        this.terms = terms;
    }

    public BalancesResponse(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        student = in.readParcelable(Student.class.getClassLoader());
        currencyCode = in.readString();
        terms = in.createTypedArray(BalanceTerm.CREATOR);
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

    public static final Creator<BalancesResponse> CREATOR = new Creator<BalancesResponse>() {
        @Override
        public BalancesResponse createFromParcel(Parcel source) {
            return new BalancesResponse(source);
        }

        @Override
        public BalancesResponse[] newArray(int size) {
            return new BalancesResponse[size];
        }
    };
}
