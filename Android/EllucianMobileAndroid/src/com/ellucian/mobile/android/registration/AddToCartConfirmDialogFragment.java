// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;

public class AddToCartConfirmDialogFragment extends EllucianDialogFragment {

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
		
		//builder.setTitle(R.string.registraion_dialog_title)
		builder.setMessage(R.string.registration_dialog_add_to_cart_message)
			.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.cancel(); 

                }
            })
            .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	registrationActivity.onAddToCartConfirmOkClicked();
                }
            });

		return builder.create();
	}

}
