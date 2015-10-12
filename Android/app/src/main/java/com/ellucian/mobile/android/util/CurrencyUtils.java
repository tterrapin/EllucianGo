/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import android.util.Log;

import java.text.NumberFormat;
import java.util.Currency;

public class CurrencyUtils {
    private static final String TAG = "CurrencyUtils";

    public static String getCurrencyString(Double amount, Currency currency) {
        String output;
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
        numberFormat.setCurrency(currency);
        numberFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
        output = numberFormat.format(amount);
        Log.d(TAG, "currency amount: " + output);
        return output;
    }
}
