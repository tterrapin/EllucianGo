/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.client.directory;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.util.Extra;

public class Entry implements Parcelable {
    /**
     *
     */

    public String type;
    public String id;
    public String displayName;
    public String prefix;
    public String firstName;
    public String middleName;
    public String lastName;
    public String suffix;
    public String nickName;
    public String title;
    public String office;
    public String department;
    public String phone;
    public String mobile;
    public String email;
    public String postOfficeBox;
    public String street;
    public String room;
    public String city;
    public String state;
    public String postalCode;
    public String country;
    public String imageUrl;

    public Bundle buildBundle() {
        Bundle bundle = new Bundle();

        if (!TextUtils.isEmpty(imageUrl)) {
            bundle.putString(Extra.DIRECTORY_IMAGE_URL, imageUrl);
        }

        // Use displayName from details, if missing create concatenated name.
        if (!TextUtils.isEmpty(displayName)) {
            bundle.putString(Extra.DIRECTORY_DISPLAY_NAME, displayName);
        } else {
            // Full Name
            String fullName = "";
            if (!TextUtils.isEmpty(prefix)) {
                fullName += prefix + " ";
            }
            if (!TextUtils.isEmpty(firstName)) {
                fullName += firstName + " ";
            }
            if (!TextUtils.isEmpty(nickName)) {
                fullName += '"' + nickName + '"' + " ";
            }
            if (!TextUtils.isEmpty(middleName)) {
                fullName += middleName + " ";
            }
            if (!TextUtils.isEmpty(lastName)) {
                fullName += lastName + " ";
            }
            if (!TextUtils.isEmpty(suffix)) {
                fullName += suffix;
            }
            fullName = fullName.replace("  ", " ");
            fullName = fullName.trim();

            if (!TextUtils.isEmpty(fullName)) {
                bundle.putString(Extra.DIRECTORY_DISPLAY_NAME, fullName);
            }
        }

        if (!TextUtils.isEmpty(title)) {
            bundle.putString(Extra.DIRECTORY_TITLE, title);
        }
        if (!TextUtils.isEmpty(phone)) {
            bundle.putString(Extra.DIRECTORY_PHONE, phone);
        }
        if (!TextUtils.isEmpty(mobile)) {
            bundle.putString(Extra.DIRECTORY_MOBILE, mobile);
        }
        if (!TextUtils.isEmpty(email)) {
            bundle.putString(Extra.DIRECTORY_EMAIL, email);
        }

        //Build formatted address
        String address = "";
        if (!TextUtils.isEmpty(postOfficeBox)) {
            address += postOfficeBox;
        }
        if (!TextUtils.isEmpty(street)) {
            if (!TextUtils.isEmpty(address)) {
                address += "\n";
            }
            address += street.replace("\\n", "\n");
        }
        if (!TextUtils.isEmpty(city)) {
            if (!TextUtils.isEmpty(address)) {
                address += "\n";
            }
            address += city;
            if (!TextUtils.isEmpty(state)) {
                address += ", " + state;
            }
            if (!TextUtils.isEmpty(postalCode)) {
                address += " " + postalCode;
            }
            if (!TextUtils.isEmpty(country)) {
                address += "\n" + country;
            }
        }

        if (!TextUtils.isEmpty(address)) {
            bundle.putString(Extra.DIRECTORY_ADDRESS, address);
        }

        if (!TextUtils.isEmpty(department)) {
            bundle.putString(Extra.DIRECTORY_DEPARTMENT, department);
        }

        if (!TextUtils.isEmpty(office)) {
            bundle.putString(Extra.DIRECTORY_OFFICE, office);
        }

        if (!TextUtils.isEmpty(room)) {
            bundle.putString(Extra.DIRECTORY_ROOM, room);
        }

        return bundle;

    }

    private Entry(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        type = in.readString();
        id = in.readString();
        displayName = in.readString();
        prefix = in.readString();
        firstName = in.readString();
        middleName = in.readString();
        lastName = in.readString();
        suffix = in.readString();
        nickName = in.readString();
        title = in.readString();
        office = in.readString();
        department = in.readString();
        phone = in.readString();
        mobile = in.readString();
        email = in.readString();
        postOfficeBox = in.readString();
        street = in.readString();
        room = in.readString();
        city = in.readString();
        state = in.readString();
        postalCode = in.readString();
        country = in.readString();
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(id);
        dest.writeString(displayName);
        dest.writeString(prefix);
        dest.writeString(firstName);
        dest.writeString(middleName);
        dest.writeString(lastName);
        dest.writeString(suffix);
        dest.writeString(nickName);
        dest.writeString(title);
        dest.writeString(office);
        dest.writeString(department);
        dest.writeString(phone);
        dest.writeString(mobile);
        dest.writeString(email);
        dest.writeString(postOfficeBox);
        dest.writeString(street);
        dest.writeString(room);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(postalCode);
        dest.writeString(country);
        dest.writeString(imageUrl);
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel source) {
            return new Entry(source);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public String getDisplayName(Context context){
        if (TextUtils.isEmpty(displayName)) {
            return context.getString(R.string.default_first_last_name_format,
                    firstName,
                    lastName);
        } else {
            return displayName;
        }
    }

}
