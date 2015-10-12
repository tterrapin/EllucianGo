// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;

public class EligibilityDialogFragment extends EllucianDialogFragment {
	private static final String TAG = EligibilityDialogFragment.class.getSimpleName();

	public static EligibilityDialogFragment newInstance(String message) {
		EligibilityDialogFragment fragment = new EligibilityDialogFragment();
		Bundle args = new Bundle();
		args.putString("message", message);
		fragment.setArguments(args);
		
		return fragment;
		
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Bundle args = getArguments();
		String message = args.getString("message");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		if (TextUtils.isEmpty(message)) {
			message = getString(R.string.registration_default_eligibility_message);
		}
		
		builder.setTitle(R.string.registration_ineligible_to_register);
		Log.d(TAG, "Setting message: " + message);
		builder.setMessage(message)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.cancel(); 

                }
            });

		return builder.create();
	}

}
