/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.multimedia;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.ellucian.elluciango.R;
import com.ellucian.mobile.android.app.DrawerLayoutHelper;
import com.ellucian.mobile.android.app.EllucianFragment;
import com.ellucian.mobile.android.app.GoogleAnalyticsConstants;
import com.ellucian.mobile.android.util.CustomToast;
import com.ellucian.mobile.android.util.Extra;

public class VideoFragment extends EllucianFragment implements SurfaceHolder.Callback, 
		MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, DrawerLayoutHelper.DrawerListener   {

    private VideoView videoView;
	private MediaController mediaController;
	private CustomToast loadingMessage;
	private int currentPosition;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video, container, false);
		videoView = (VideoView) rootView.findViewById(R.id.video);
		
		mediaController = new MediaController(getActivity());

		videoView.setOnCompletionListener(this);
		videoView.setOnPreparedListener(this);
		videoView.setMediaController(mediaController);
		
		Intent activityIntent = getActivity().getIntent();
		
		String description = activityIntent.getStringExtra(Extra.CONTENT);

		TextView descriptionView = (TextView) rootView.findViewById(R.id.description);
		if (descriptionView != null) {
			if (!TextUtils.isEmpty(description)) {
				descriptionView.setText(description);
			} else {
				descriptionView.setVisibility(View.GONE);
			}
		}
		
		String videoUrl = activityIntent.getStringExtra(Extra.VIDEO_URL);
	
		Uri videoUri = Uri.parse(videoUrl); 
		videoView.setVideoURI(videoUri);
		
		if ( savedInstanceState != null )
			
			if( savedInstanceState.containsKey("currentPosition") && 
					savedInstanceState.getInt("currentPosition") > 0) {

				currentPosition = savedInstanceState.getInt("currentPosition");
		}
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		loadingMessage = new CustomToast(getActivity(), getString(R.string.loading_message));
		loadingMessage.setDuration(30);
		loadingMessage.setGravity(Gravity.CENTER, 0, 0);
		loadingMessage.show();
		
		getEllucianActivity().getDrawerLayoutHelper().setDrawerListener(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		sendView("Video", getEllucianActivity().moduleName);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		videoView.getHolder().addCallback(this);
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// If Menu drawer open close in landscape mode
			if (getEllucianActivity().getDrawerLayoutHelper().isDrawerOpen()) {
				getEllucianActivity().getDrawerLayoutHelper().closeDrawer();
			}
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// makes sure toast closed
		if (loadingMessage != null) {
			loadingMessage.cancel();
		}
		
		videoView.getHolder().removeCallback(this);
		currentPosition = videoView.getCurrentPosition();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
			
		if (videoView.isPlaying()) {
			videoView.pause();
		}

		outState.putInt("currentPosition", currentPosition);		
	}
	
	/** SurfaceHolder.Callback methods */
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!videoView.isPlaying()) {
			mediaController.show(0);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	
	/** MediaPlayer methods */

	@Override
	public void onCompletion(MediaPlayer arg0) {
		sendEventToTracker1(GoogleAnalyticsConstants.CATEGORY_UI_ACTION, GoogleAnalyticsConstants.ACTION_BUTTON_PRESS, "Play button pressed", null, getEllucianActivity().moduleName);
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		loadingMessage.cancel();
		
		// Adjust for delay
		int adjustedPosition = currentPosition - 1000;
		if (adjustedPosition < 0) {
			adjustedPosition = 0;
		}
		videoView.seekTo(adjustedPosition);
		
	}
	
	/** DrawerLayoutHelper.DrawerListener implemented methods */
	@Override
	public void onDrawerOpened() {
		if (mediaController.isShowing()) {
			mediaController.hide();
		}	
	}
	
	@Override
	public void onDrawerClosed() {
		// do nothing on close		
	}	
}
