// Copyright 2014 Ellucian Company L.P and its affiliates.

package com.ellucian.mobile.android.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;

public class DeleteConfirmDialogFragment extends EllucianDialogFragment {
	
	public NotificationsDetailFragment detailFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		
		builder.setMessage(R.string.notifications_dialog_delete_message)
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.cancel(); 

                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	detailFragment.triggerDeleteNotification();
                }
            });

		return builder.create();
	}

}
