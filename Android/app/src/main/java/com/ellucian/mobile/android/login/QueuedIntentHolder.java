// Copyright 2015 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.login;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class QueuedIntentHolder implements Parcelable {

    public static final String QUEUED_INTENT_HOLDER = "QUEUED_INTENT_HOLDER";

    public Intent queuedIntent;
    public String moduleId;

    public QueuedIntentHolder(String moduleId, Intent queuedIntent) {
        this.moduleId = moduleId;
        this.queuedIntent = queuedIntent;
    }

    private QueuedIntentHolder(Parcel in) {
        readFromParcel(in);
    }
    private void readFromParcel(Parcel in) {
        queuedIntent = in.readParcelable(Intent.class.getClassLoader());
        moduleId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(queuedIntent, flags);
        dest.writeString(moduleId);
    }

    public static final Parcelable.Creator<QueuedIntentHolder> CREATOR = new Parcelable.Creator<QueuedIntentHolder>() {
        public QueuedIntentHolder createFromParcel(Parcel in) {
            return new QueuedIntentHolder(in);
        }

        public QueuedIntentHolder[] newArray(int size) {
            return new QueuedIntentHolder[size];
        }

    };
}
