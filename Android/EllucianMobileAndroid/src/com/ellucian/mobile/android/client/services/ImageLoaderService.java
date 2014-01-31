package com.ellucian.mobile.android.client.services;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.ellucian.mobile.android.util.Extra;

public class ImageLoaderService extends IntentService {
	private static final String TAG = ImageLoaderService.class.getSimpleName();
	public static final String ACTION_FINISHED = "com.ellucian.mobile.android.client.services.ImageLoaderService.action.updated";
	private LocalBroadcastManager broadcastManager;
	private AQuery aq;
	private List<String> imageUrlList;
	private int imagesToDownload;
	private int imageCounter;
	private boolean broadcastWhenDone;
	
	
	
	public ImageLoaderService() {
		super("ImageLoaderService");
		broadcastManager = LocalBroadcastManager.getInstance(ImageLoaderService.this);
		aq = new AQuery(this);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		imageUrlList = intent.getStringArrayListExtra(Extra.IMAGE_URL_LIST);
		broadcastWhenDone = intent.getBooleanExtra(Extra.SEND_BROADCAST, false);
		imagesToDownload = 0;
		imageCounter = 0;

		
		for (String imageUrl : imageUrlList) {
			Bitmap bit = aq.getCachedImage(imageUrl);
			if (bit == null) {
				// If cached imaged does not exist add to number to be dowloaded and
				// start asynchronous download of that image
				Log.d(TAG, "Image could not be found in cache, starting download of: \n" + imageUrl);
				imagesToDownload++;
				ImageView iv = new ImageView(this);
				// If marked for broadcast each image will add an counter in their callbacks
				if (broadcastWhenDone) {
					aq.id(iv).image(imageUrl, false, true, 0, 0, new BitmapAjaxCallback(){
						
						@Override
						public void callback(String url, ImageView view, Bitmap bitmap, AjaxStatus status) {
							view.setImageBitmap(bitmap);
							imageCounter++;
							Log.d(TAG, "Image downloaded: \n" + imageCounter);
						}
					});
				} else {
					aq.id(iv).image(imageUrl, false, true);
				}	
			} 
		}
		
		
		Log.d("OnHandleIntent", "Number of images to download: " + imagesToDownload);
		
		if (broadcastWhenDone) {
			int checks = 0;
			// Keep checking to see if all the image callbacks have been fired
			while (imageCounter < imagesToDownload) {
				try {
					Thread.sleep(1000);
					if (checks >= 10) {
						Log.d("TAG", "Waited 10 seconds for images to download... continuing");
						break;
					}
					checks++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
			sendBroadcast();
		}
	}
	
	public void sendBroadcast() {
		Log.d(TAG, "Images dowload sending broadcast");
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_FINISHED);
		broadcastManager.sendBroadcast(broadcastIntent);
	}
	
}
