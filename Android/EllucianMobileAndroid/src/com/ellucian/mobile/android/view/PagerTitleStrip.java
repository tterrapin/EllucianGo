package com.ellucian.mobile.android.view;

import com.ellucian.mobile.android.util.Utils;

import android.content.Context;
import android.util.AttributeSet;

public class PagerTitleStrip extends android.support.v4.view.PagerTitleStrip {

	public PagerTitleStrip(Context arg0) {
		super(arg0);
		applyStyle(arg0);
	}

	private void applyStyle(Context arg0) {
		this.setBackgroundColor(Utils.getAccentColor(arg0));
		this.setTextColor(Utils.getSubheaderTextColor(arg0));
	}
	
	public PagerTitleStrip(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		applyStyle(arg0);
	}

}
