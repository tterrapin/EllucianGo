package com.ellucian.mobile.android.client.services;

import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
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
	
	
	public ImageLoaderService() {
		super("ImageLoaderService");
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		List<String> imageUrlList = intent.getStringArrayListExtra(Extra.IMAGE_URL_LIST);
		boolean broadcastWhenDone = intent.getBooleanExtra(Extra.SEND_BROADCAST, false);
		new ImageLoaderAsyncTask(imageUrlList, broadcastWhenDone).execute();
	}
	
	public void sendBroadcast() {
		Log.d(TAG, "Images dowload sending broadcast");
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_FINISHED);
		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(ImageLoaderService.this);
		broadcastManager.sendBroadcast(broadcastIntent);
	}
	
	public class ImageLoaderAsyncTask extends AsyncTask<Void, Void, Void> {

		private boolean broadcastWhenDone;
		private List<String> imageUrlList;
		private int imagesToDownload = 0;
		private int imageCounter = 0;
		
		public ImageLoaderAsyncTask(List<String> imageUrlList, boolean b) {
			this.imageUrlList = imageUrlList;
			this.broadcastWhenDone = b;
		}
		
		@Override
		protected Void doInBackground(Void... params) {

			AQuery aq = new AQuery(ImageLoaderService.this);
			Log.v("broadcastWhenDone", ""+broadcastWhenDone);
			for (String imageUrl : imageUrlList) {
				Bitmap bit = aq.getCachedImage(imageUrl);
				if (bit == null) {
					// If cached imaged does not exist add to number to be dowloaded and
					// start asynchronous download of that image
					Log.d(TAG, "Image could not be found in cache, starting download of: \n" + imageUrl);
					imagesToDownload++;
					ImageView iv = new ImageView(ImageLoaderService.this);
					// If marked for broadcast each image will add an counter in their callbacks
					if (broadcastWhenDone) {
						try {
							aq.id(iv).image(imageUrl, false, true, 0, 0,
									new BitmapAjaxCallback() {

										@Override
										public void callback(String url,
												ImageView view, Bitmap bitmap,
												AjaxStatus status) {
											view.setImageBitmap(bitmap);
											imageCounter++;
											Log.d(TAG, "Image downloaded "
													+ imageCounter + " " + url);
										}
									});
						} catch (Exception e) {
							e.printStackTrace();
						}
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
			return null;
		}
	}
}
