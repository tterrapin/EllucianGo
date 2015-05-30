/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.webframe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.EllucianDialogFragment;

public class SecurityDialogFragment extends EllucianDialogFragment {
	public static final String SECURITY_DIALOG = "securityDialog";
	private WebframeActivity webActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		webActivity = (WebframeActivity)activity;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		
		//LayoutInflater inflater = getActivity().getLayoutInflater();
		//ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_security_dialog, null);
		//TextView title = (TextView) dialogView.findViewById(R.id.security_dialog_title);
		//title.setTextColor(Utils.getPrimaryColor(getActivity()));
		//builder.setView(dialogView);
		
		builder.setTitle(R.string.dialog_security_warning)
			.setMessage(R.string.dialog_cert_warning_statement)
			.setNegativeButton(R.string.dialog_go_back, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	dialog.cancel(); 
                	webActivity.onGoBackClicked();
                }
            })
            .setPositiveButton(R.string.dialog_continue, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	webActivity.onContinueClicked();
                }
            })
			;

		return builder.create();
	}

}
