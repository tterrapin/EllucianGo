package com.ellucian.mobile.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

/**
 * This class manages all image downloads used by the application. This manager
 * will manage retrieving images from internal or external storage, and
 * downloading the item if it is absent from the local storage.
 * 
 * @author Jason Hocker
 * 
 */
public class ImageLoader {

	/**
	 * Manages the operations with the internal and external cache
	 * 
	 * @author Jason Hocker
	 * 
	 */
	private static class FileCache {

		private static File externalCache;
		private static File internalCache;

		private FileCache(Context context) {
			createCache(context);
		}

		public void clear() {
			if (externalCache != null) {
				final File[] files = externalCache.listFiles();
				for (final File f : files) {
					f.delete();
				}
			}
			if (internalCache != null) {
				final File[] files = internalCache.listFiles();
				for (final File f : files) {
					f.delete();
				}
			}

		}

		private void createCache(Context context) {
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				final String path = Environment.getExternalStorageDirectory()
						.toString();

				final String dirname = path + "/com.ellucian.mobile.android/";
				externalCache = new File(dirname);

				if (!externalCache.exists()) {
					externalCache.mkdirs();
				}
			}
			internalCache = context.getCacheDir();

		}

		public void expireCache(int milliseconds, Set<String> keepUrls) {
			final List<String> filenames = new ArrayList<String>();
			for (final String keepUrl : keepUrls) {
				filenames.add(makeSHA1Hash(keepUrl));
			}
			final Calendar calendar = Calendar.getInstance();
			calendar.roll(Calendar.MILLISECOND, milliseconds);

			if (externalCache != null) {
				final File[] files = externalCache.listFiles();
				for (final File f : files) {
					if (f.lastModified() < calendar.getTimeInMillis()) {
						if (!filenames.contains(f.getName())) {
							f.delete();
						}
					}
				}
			}
			if (internalCache != null) {
				final File[] files = internalCache.listFiles();
				for (final File f : files) {
					if (f.lastModified() < calendar.getTimeInMillis()) {
						if (!filenames.contains(f.getName())) {
							f.delete();
						}
					}
				}
			}
		}

		public File getFile(String url, boolean internalOnly) {

			final String filename = makeSHA1Hash(url);
			File f = null;
			if (internalOnly || externalCache == null) {
				f = new File(internalCache, filename);
				if (f.exists()) {
					Log.d(EllucianApplication.TAG, "Found image " + url
							+ " in INTERNAL cache");
				}
				return f;
			} else {

				f = new File(externalCache, filename);

				if (f.exists()) {
					if (f.exists()) {
						Log.d(EllucianApplication.TAG, "Found image " + url
								+ " in EXTERNAL cache");
					}
				} else {
					final File f2 = new File(internalCache, filename);
					if (f2.exists()) {
						f = f2;
						if (f.exists()) {
							Log.d(EllucianApplication.TAG, "Found image " + url
									+ " in INTERNAL cache");
						}
					}
				}
				return f;
			}
		}

		/**
		 * File names will be SHA1 to make the names filesystem safe and to
		 * obfuscate them
		 * 
		 * @param input
		 *            the string value to turn into a SHA1 hash.
		 * @return
		 */
		public String makeSHA1Hash(String input) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA1");
			} catch (final NoSuchAlgorithmException e) {
				return String.valueOf(input.hashCode());
			}
			md.reset();
			final byte[] buffer = input.getBytes();
			md.update(buffer);
			final byte[] digest = md.digest();

			String hexStr = "";
			for (final byte element : digest) {
				hexStr += Integer.toString((element & 0xff) + 0x100, 16)
						.substring(1);
			}
			return hexStr;
		}
	}

	private static FileCache fileCache = null;

//	/**
//	 * Converts the input stream into a byte array before converting to a
//	 * bitmap. This is because of bug //
//	 * http://code.google.com/p/android/issues/detail?id=6066
//	 * 
//	 * @param is
//	 * @return
//	 * @throws IOException
//	 */
//	public static byte[] convertInputStreamToByteArray(InputStream is)
//			throws IOException {
//		final BufferedInputStream bis = new BufferedInputStream(is);
//		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
//		int result = bis.read();
//		while (result != -1) {
//			final byte b = (byte) result;
//			buf.write(b);
//			result = bis.read();
//		}
//		return buf.toByteArray();
//	}

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		thread = new Thread(runner);
	}

	public void clearCache() {
		fileCache.clear();
	}

	public void expireCache(int expirationTime, Set<String> keepUrls) {
		fileCache.expireCache(expirationTime, keepUrls);

	}

	private Bitmap getBitmap(String url, boolean internalOnly) {

		Bitmap bitmap;

		final File f = fileCache.getFile(url, internalOnly);
		if (f.exists()) {
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDensity = android.util.DisplayMetrics.DENSITY_MEDIUM;
				options.inDither=false;                     //Disable Dithering mode
				options.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
				options.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
				options.inTempStorage=new byte[32 * 1024];
				bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
				return bitmap;
			} catch (Exception e) {
				return null;
			} catch (OutOfMemoryError e) {
				return null;
			}
		}
		return null;
	}
	
	private final class QueueItem {
		public String url;
		public ImageLoadedListener listener;
		public boolean internalOnly;
	}
	private final ArrayList<QueueItem> Queue = new ArrayList<QueueItem>();

	private final Handler handler = new Handler();
	private Thread thread;
	private QueueRunner runner = new QueueRunner();;


	/**
	 * Defines an interface for a callback that will handle
	 * responses from the thread loader when an image is done
	 * being loaded.
	 */
	public interface ImageLoadedListener {
		public void imageLoaded(Bitmap imageBitmap );
	}

	/**
	 * Provides a Runnable class to handle loading the image from the URL and
	 * settings the ImageView on the UI thread.
	 */
	private class QueueRunner implements Runnable {
		public void run() {

			while (Queue.size() > 0) {
				synchronized (Queue) {
				try {
						
					final QueueItem item = Queue.remove(0);
					

					// Use a handler to get back onto the UI thread for the
					// update
					handler.post(new Runnable() {
						public void run() {

							

							final Bitmap bitmap = getBitmap(item.url,
									item.internalOnly);
							if (bitmap != null) {
								if (item.listener != null) {
									item.listener.imageLoaded(bitmap);
								}
							} else {
								URL url;
								try {
									url = new URL(item.url);
									final Bitmap bmp = readBitmapFromNetwork(url);
									if (bmp != null) {

										final File f = fileCache.getFile(
												item.url, item.internalOnly);
										try {
											if (!f.exists()) {
												f.createNewFile();
											}
											final FileOutputStream fos = new FileOutputStream(
													f);
											bmp.compress(
													Bitmap.CompressFormat.PNG,
													100, fos);
											fos.flush();
											fos.close();

											if (item.listener != null) {
												item.listener.imageLoaded(bmp);
											}
										} catch (final IOException e) {
											Log.e(EllucianApplication.TAG,
													e.getLocalizedMessage());
										}
									}
								} catch (MalformedURLException e) {
									Log.e(EllucianApplication.TAG,
											e.getLocalizedMessage());

								}

							}
						}
					});
				} catch (Exception e) { //arrayindexoutofboundsexception
					continue;
				}
				}
			}
		}
	}

	public Bitmap loadImage( final String uri, final ImageLoadedListener listener) {
		return loadImage(uri, false, listener);
	}
	public Bitmap loadImage( final String uri, boolean internalOnly, final ImageLoadedListener listener) {//throws MalformedURLException {
		// If it's in the cache, just get it and quit it
		if(uri == null) return null;
		final Bitmap bitmap = getBitmap( uri, internalOnly);
		if (bitmap != null) {
			return bitmap;
		} 
		
		QueueItem item = new QueueItem();
		item.url = uri;
		item.listener = listener;
		item.internalOnly = internalOnly;
		Queue.add(item);

		// start the thread if needed
		if( thread.getState() == State.NEW) {
			thread.start();
		} else if( thread.getState() == State.TERMINATED) {
			thread = new Thread(runner);
			thread.start();
		}
		return null;
	}

	/**
	 * Convenience method to retrieve a bitmap image from
	 * a URL over the network. The built-in methods do
	 * not seem to work, as they return a FileNotFound
	 * exception.
	 *
	 * Note that this does not perform any threading --
	 * it blocks the call while retrieving the data.
	 *
	 * @param url The URL to read the bitmap from.
	 * @return A Bitmap image or null if an error occurs.
	 */
	private static Bitmap readBitmapFromNetwork( URL url ) {
		InputStream is = null;
		BufferedInputStream bis = null;
		Bitmap bmp = null;
		try {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDensity = android.util.DisplayMetrics.DENSITY_MEDIUM;
			
			URLConnection conn = url.openConnection();
			conn.connect();
			is = conn.getInputStream();
			bis = new BufferedInputStream(is);
			bmp = BitmapFactory.decodeStream(bis, null, options);
		} catch (MalformedURLException e) {
			Log.e(EllucianApplication.TAG, "Bad URL: " + url, e);
		} catch (IOException e) {
			Log.e(EllucianApplication.TAG, "Could not get remote image: " + url , e);
		} catch (Throwable e) {
			Log.e(EllucianApplication.TAG, "General exception downloading image: " + url, e);
		} finally {
			try {
				if( is != null )
					is.close();
				if( bis != null )
					bis.close();
			} catch (IOException e) {
				Log.w(EllucianApplication.TAG, "Error closing stream.");
			}
		}
		return bmp;
	}


}
