/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;

public class CustomToast {
	private final Toast toast;
	private CountDownTimer timer;
    private int mDuration;
    private boolean mShowing = false;
    
    
    @SuppressLint("ShowToast")
	public CustomToast(Context context, String text) {
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        mDuration = 2;
    }
    

    public void setGravity(int gravity, int xOffset, int yOffset) {
    	toast.setGravity(gravity, xOffset, yOffset);
    }

    /**
     * Set the time to show the toast for (in seconds) 
     * @param seconds Seconds to display the toast
     */

    public void setDuration(int seconds) {
        if(seconds < 2) seconds = 2; //Minimum
        mDuration = seconds;
    }

    /**
     * Show the toast for the given time 
     */

    public void show() {

        if(mShowing) return;

        mShowing = true;

        timer = new CountDownTimer((mDuration-2)*1000, 1000) {
        	
            public void onTick(long millisUntilFinished) {
            	toast.show();
            }
            public void onFinish() {
            	toast.show(); 
            	mShowing = false;
            }
            
        }.start();  
    }
    

    public void cancel() {
    	timer.cancel();
    	toast.cancel();
    }
}