/*
 * Copyright 2015 Ellucian Company L.P. and its affiliates.
 */

package com.ellucian.mobile.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@SuppressWarnings("JavaDoc")
public class URLImageParser implements ImageGetter {
    private final Context c;
    private final View container;

    /***
     * Construct the URLImageParser which will execute AsyncTask and refresh the container
     * @param t
     * @param c
     */
    public URLImageParser(View t, Context c) {
        this.c = c;
        this.container = t;
    }

    public Drawable getDrawable(String source) {
    	URLDrawable urlDrawable = new URLDrawable(c.getResources(), null);
        
        // Checking if the source url is well formed before starting the AsyncTask
        try {
			URL url = new URL(source);
			@SuppressWarnings("unused")
			URI uri = url.toURI();
		} catch (MalformedURLException e) {
			Log.e("URLImageParser.getDrawable", "MalformedURLException: The source url string was not well formed");
			urlDrawable.setBounds(0, 0, 0, 0);
			return urlDrawable;
		} catch (URISyntaxException e) {			
			Log.e("URLImageParser.getDrawable", "URISyntaxException: Url could not be converted to URI correctly, wrong format");
			urlDrawable.setBounds(0, 0, 0, 0);
			return urlDrawable;
		}
    
        // get the actual source
        ImageGetterAsyncTask asyncTask = 
            new ImageGetterAsyncTask( urlDrawable);
        
        asyncTask.execute(source);

        // return reference to URLDrawable where I will change with actual image from
        // the src tag
        return urlDrawable;
    }

    @SuppressWarnings("JavaDoc")
    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
        final URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {

			if (result != null) {
				// set the correct bound according to the result from HTTP call
				urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(),
						0 + result.getIntrinsicHeight());

				// change the reference of the current drawable to the result
				// from the HTTP call
				urlDrawable.drawable = result;

				// redraw the image by invalidating the container
				URLImageParser.this.container.invalidate();

				// Added these 2 lines to keep the image from being displayed
				// over the text
				((TextView) URLImageParser.this.container)
						.setHeight((URLImageParser.this.container.getHeight() + result
								.getIntrinsicHeight()));

				((TextView) URLImageParser.this.container).setEllipsize(null);
        	}
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                if(drawable != null) {
                	drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0 
                        + drawable.getIntrinsicHeight()); 
                	return drawable;
                } else {
                	Log.e("URLImageParser.fetchDrawable", "drawable is null for url: " + urlString);
                	return null;
                }
            } catch (MalformedURLException e) {
            	Log.e("URLImageParser.fetchDrawable", "MalformedURLException: ", e);
                return null;
            } catch (IOException e) {
            	Log.e("URLImageParser.fetchDrawable", "IOException: ", e);
                return null;
			} 
        }

        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            HttpURLConnection urlConnection = getConnection(urlString);
            return urlConnection.getInputStream();
        }

        private HttpURLConnection getConnection(String requestUrl) {
            HttpURLConnection urlConnection = null;
            URL url;
            try {
                url = new URL(requestUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setInstanceFollowRedirects(false);
            } catch (MalformedURLException e) {
                Log.e("URLImageParser", "MalformedURLException", e);
            } catch (IOException e) {
                Log.e("URLImageParser", "IOException", e);
            }

            return urlConnection;
        }
    }
}
