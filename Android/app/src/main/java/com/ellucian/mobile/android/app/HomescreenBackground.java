/*
 * Copyright 2016 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.app;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.androidquery.AQuery;
import com.ellucian.mobile.android.util.Utils;

public class HomescreenBackground {
    Bitmap image;
    private static HomescreenBackground homescreenBackground;

    public static HomescreenBackground getInstance(Context context) {
        if (homescreenBackground == null) {
            homescreenBackground = new HomescreenBackground(context);
        }
        return homescreenBackground;
    }

    public Bitmap getImage() {
        return image;
    }

    private HomescreenBackground(Context context) {
        String backgroundUrl = null;
        if ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            backgroundUrl = Utils.getStringFromPreferences(context, Utils.APPEARANCE, Utils.HOME_URL_TABLET, "");
        }
        if (TextUtils.isEmpty(backgroundUrl)) {
            backgroundUrl = Utils.getStringFromPreferences(context, Utils.APPEARANCE, Utils.HOME_URL_PHONE, "");
        }

        image = null;
        if (!TextUtils.isEmpty(backgroundUrl)) {
            AQuery aq = new AQuery(context);
            image = aq.getCachedImage(backgroundUrl);
        }
    }

    public static void refresh(Context context) {
        homescreenBackground = new HomescreenBackground(context);
    }
}
