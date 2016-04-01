/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.directory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.app.EllucianDefaultDetailFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.DownloadImageTask;
import com.ellucian.mobile.android.util.Extra;
import com.ellucian.mobile.android.util.Utils;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DirectoryDetailFragment extends EllucianDefaultDetailFragment {
    private static final String TAG = DirectoryDetailFragment.class.getSimpleName();
    private Activity activity;
    private Context context;

    public static DirectoryDetailFragment newInstance(Bundle args) {
        DirectoryDetailFragment f = new DirectoryDetailFragment();

        f.setArguments(args);

        return f;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activity = getActivity();
    }

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static DirectoryDetailFragment newInstance(int index) {
        DirectoryDetailFragment f = new DirectoryDetailFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_directory_detail, container, false);

        Bundle args = getArguments();

        final String displayName = args.getString(Extra.DIRECTORY_DISPLAY_NAME);
        final String imageUrl = args.getString(Extra.DIRECTORY_IMAGE_URL);
        final String title = args.getString(Extra.DIRECTORY_TITLE);
        final String department = args.getString(Extra.DIRECTORY_DEPARTMENT);
        final String email = args.getString(Extra.DIRECTORY_EMAIL);
        final String mobile = args.getString(Extra.DIRECTORY_MOBILE);
        final String phone = args.getString(Extra.DIRECTORY_PHONE);
        final String office = args.getString(Extra.DIRECTORY_OFFICE);
        final String room = args.getString(Extra.DIRECTORY_ROOM);
        final String address = args.getString(Extra.DIRECTORY_ADDRESS);

        final int primaryColor = Utils.getPrimaryColor(context);

        // Set all icons to primary color
        ((ImageView)view.findViewById(R.id.directory_email_image)).setColorFilter(primaryColor);
        ((ImageView)view.findViewById(R.id.directory_chat_image)).setColorFilter(primaryColor);
        ((ImageView)view.findViewById(R.id.directory_mobile_image)).setColorFilter(primaryColor);
        ((ImageView)view.findViewById(R.id.directory_phone_image)).setColorFilter(primaryColor);
        ((ImageView)view.findViewById(R.id.directory_address_image)).setColorFilter(primaryColor);
        ((ImageView)view.findViewById(R.id.directory_directions_image)).setColorFilter(primaryColor);

        if (!TextUtils.isEmpty(imageUrl)) {
            Log.d(TAG, "Downloading image: " + imageUrl);
            CircleImageView imageView = (CircleImageView) view.findViewById(R.id.directory_detail_image);
            new DownloadImageTask(imageView).execute(imageUrl);
        }

        if (!TextUtils.isEmpty(displayName)) {
            TextView nameView = (TextView) view.findViewById(R.id.directory_detail_name);
            nameView.setVisibility(View.VISIBLE);
            nameView.setText(displayName);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView titleView = (TextView) view.findViewById(R.id.directory_detail_title);
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(title);
        }
        if (!TextUtils.isEmpty(department)) {
            TextView departmentView = (TextView) view.findViewById(R.id.directory_detail_department);
            departmentView.setVisibility(View.VISIBLE);
            departmentView.setText(department);
        }
        if (!TextUtils.isEmpty(email)) {
            view.findViewById(R.id.directory_detail_email_layout).setVisibility(View.VISIBLE);
            TextView emailView = (TextView) view.findViewById(R.id.directory_detail_email);
            emailView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.EMAIL_ADDRESSES));
            emailView.setText(email);
            emailView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View viewIn) {
                    sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Send e-mail", null, getEllucianActivity().moduleName);
                }
            });
        }
        if (!TextUtils.isEmpty(mobile)) {
            view.findViewById(R.id.directory_detail_mobile_layout).setVisibility(View.VISIBLE);
            TextView mobileView = (TextView) view.findViewById(R.id.directory_detail_mobile);
            mobileView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.PHONE_NUMBERS));
            mobileView.setText(mobile);
            mobileView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View viewIn) {
                    sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Call Phone Number", null, getEllucianActivity().moduleName);
                }
            });
            ImageView smsImage = (ImageView) view.findViewById(R.id.directory_chat_image);
            smsImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Text Phone Number", null, getEllucianActivity().moduleName);
                    Intent textIntent = new Intent(Intent.ACTION_VIEW);
                    textIntent.setData(Uri.parse("sms:" + mobile));
                    startActivity(textIntent);
                }
            });
        }
        if (!TextUtils.isEmpty(phone)) {
            view.findViewById(R.id.directory_detail_phone_layout).setVisibility(View.VISIBLE);
            // Suppress phone icon if mobile phone already displayed it
            if (!TextUtils.isEmpty(mobile)) {
                ImageView phoneImage = (ImageView) view.findViewById(R.id.directory_phone_image);
                phoneImage.setImageDrawable(null);
            }
            TextView phoneView = (TextView) view.findViewById(R.id.directory_detail_phone);
            phoneView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.PHONE_NUMBERS));
            phoneView.setText(phone);
            phoneView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View viewIn) {
                    sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Call Phone Number", null, getEllucianActivity().moduleName);
                }
            });
        }
        if (!TextUtils.isEmpty(office)) {
            view.findViewById(R.id.directory_detail_office_layout).setVisibility(View.VISIBLE);
            TextView officeView = (TextView) view.findViewById(R.id.directory_detail_office);
            officeView.setText(office);
        }
        if (!TextUtils.isEmpty(room)) {
            view.findViewById(R.id.directory_detail_room_layout).setVisibility(View.VISIBLE);
            TextView roomView = (TextView) view.findViewById(R.id.directory_detail_room);
            roomView.setText(room);
        }
        if (!TextUtils.isEmpty(address)) {
            view.findViewById(R.id.directory_detail_address_layout).setVisibility(View.VISIBLE);
            TextView addressView = (TextView) view.findViewById(R.id.directory_detail_address);
            addressView.setAutoLinkMask(Utils.getAvailableLinkMasks(context, Linkify.MAP_ADDRESSES));
            addressView.setText(address);
            ImageView directionsImage = (ImageView) view.findViewById(R.id.directory_directions_image);
            directionsImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+address);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.directory_detail, menu);

        Drawable addContactIcon = menu.findItem(R.id.directory_detail_add_to_contacts).getIcon();
        if (addContactIcon != null) {
            addContactIcon.mutate();
            addContactIcon.setColorFilter(Utils.getColorHelper(context, R.color.menu_icon_color), PorterDuff.Mode.SRC_ATOP);
        }

        MenuItem infoMenuItem = menu.findItem(R.id.directory_detail_info);
        EllucianApplication application = (EllucianApplication) activity.getApplication();
        if (!application.isUserAuthenticated()) {
            infoMenuItem.setVisible(true);
            infoMenuItem.getIcon().mutate();
            infoMenuItem.getIcon().setColorFilter(Utils.getColorHelper(context, R.color.menu_icon_color), PorterDuff.Mode.SRC_ATOP);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directory_detail_add_to_contacts:
                sendAddContactIntent();
                return true;
            case R.id.directory_detail_info:
                moreInfoMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendAddContactIntent() {

        sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_INVOKE_NATIVE, "Add contact", null, getEllucianActivity().moduleName);

        // Gets values from the UI
        Bundle args = getArguments();
        final String displayName = args.getString(Extra.DIRECTORY_DISPLAY_NAME);
        final String title = args.getString(Extra.DIRECTORY_TITLE);
        final String phone = args.getString(Extra.DIRECTORY_PHONE);
        final String mobile = args.getString(Extra.DIRECTORY_MOBILE);
        final String email = args.getString(Extra.DIRECTORY_EMAIL);
        final String address = args.getString(Extra.DIRECTORY_ADDRESS);
        final String department = args.getString(Extra.DIRECTORY_DEPARTMENT);

        // Creates a new intent for sending to the device's contacts application
        Intent insertIntent = new Intent(ContactsContract.Intents.Insert.ACTION);

        // Sets the MIME type to the one expected by the insertion activity
        insertIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        // Sets the new contact name
        insertIntent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);

        if (!TextUtils.isEmpty(department)) {
            insertIntent.putExtra(ContactsContract.Intents.Insert.COMPANY, department);
        }

        if (!TextUtils.isEmpty(title)) {
            insertIntent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title);
        }

    	/*
    	 * Demonstrates adding data rows as an array list associated with the DATA key
    	 */

        // Defines an array list to contain the ContentValues objects for each row
        ArrayList<ContentValues> contactData = new ArrayList<ContentValues>();


    	/*
    	 * Defines the raw contact row
    	 */

        // Sets up the row as a ContentValues object
        //ContentValues rawContactRow = new ContentValues();

        // Adds the account type and name to the row
        //rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_TYPE, mSelectedAccount.getType());
        //rawContactRow.put(ContactsContract.RawContacts.ACCOUNT_NAME, mSelectedAccount.getName());

        // Adds the row to the array
        //contactData.add(rawContactRow);

    	/*
    	 * Sets up the phone number data row
    	 */
        if (!TextUtils.isEmpty(phone)) {
            // Sets up the row as a ContentValues object
            ContentValues phoneRow = new ContentValues();

            // Specifies the MIME type for this data row (all data rows must be marked by their type)
            phoneRow.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            );

            // Adds the phone number and its type to the row
            phoneRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone);
            phoneRow.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

            // Adds the row to the array
            contactData.add(phoneRow);
        }
    	
    	/*
    	 * Sets up the mobile number data row
    	 */
        if (!TextUtils.isEmpty(mobile)) {
            // Sets up the row as a ContentValues object
            ContentValues mobileRow = new ContentValues();

            // Specifies the MIME type for this data row (all data rows must be marked by their type)
            mobileRow.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            );

            // Adds the mobile number and its type to the row
            mobileRow.put(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile);
            mobileRow.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);

            // Adds the row to the array
            contactData.add(mobileRow);
        }

    	/*
    	 * Sets up the email data row
    	 */

        if (!TextUtils.isEmpty(email)) {
            // Sets up the row as a ContentValues object
            ContentValues emailRow = new ContentValues();

            // Specifies the MIME type for this data row (all data rows must be marked by their type)
            emailRow.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            );

            // Adds the email address and its type to the row
            emailRow.put(ContactsContract.CommonDataKinds.Email.ADDRESS, email);
            emailRow.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);

            // Adds the row to the array
            contactData.add(emailRow);
        }
    	
    	/*
    	 * Sets up the address data row
    	 */

        if (!TextUtils.isEmpty(address)) {
            // Sets up the row as a ContentValues object
            ContentValues addressRow = new ContentValues();

            // Specifies the MIME type for this data row (all data rows must be marked by their type)
            addressRow.put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
            );

            // Adds the address and its type to the row
            addressRow.put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address);
            addressRow.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK);

            // Adds the row to the array
            contactData.add(addressRow);
        }


    	/*
    	 * Adds the array to the intent's extras. It must be a parcelable object in order to
    	 * travel between processes. The device's contacts app expects its key to be
    	 * Intents.Insert.DATA
    	 */
        insertIntent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

        // Send out the intent to start the device's contacts app in its add contact activity.
        startActivity(insertIntent);
    }


    @Override
    public void onStart() {
        super.onStart();
        sendView("Directory card", getEllucianActivity().moduleName);
    }

    private void moreInfoMessage() {
        Snackbar loginSnackbar = Snackbar.make(getView(), R.string.directory_sign_in_message, Snackbar.LENGTH_LONG);
        loginSnackbar.show();
    }

}