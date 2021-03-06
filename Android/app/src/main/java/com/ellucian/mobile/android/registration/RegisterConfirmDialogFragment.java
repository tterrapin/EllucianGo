// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;

public class RegisterConfirmDialogFragment extends EllucianDialogFragment {

	private RegistrationActivity registrationActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			registrationActivity = (RegistrationActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Attached Activity must of type: RegistrationActivity");
        }
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		
		//builder.setTitle(R.string.registration_dialog_title)
		builder.setMessage(R.string.registration_dialog_register_message)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.cancel(); 

                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	registrationActivity.onRegisterConfirmOkClicked();
                }
            });

		return builder.create();
	}

}
