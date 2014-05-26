package com.vengestudios.sortme.generaluielements;

import java.io.InputStream;

import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * An ImageView that can be used to load and display an image from an URL
 */
@SuppressLint("ViewConstructor")
public class URLImageView extends ImageView {

	private DownloadImageTask downloadImageTask;

	/**
	 * Constructor
	 *
	 * Creates an URLImageView
	 *
	 * @param context The context of the application
	 * @param height The height of the ImageView in density independent pixels "dp"
	 * @param width The width of the ImageView in density independent pixels "dp"
	 */
	public URLImageView(Context context, float height, float width) {
		super(context);
		float screenDensity =  ScreenDimensions.getDensity(context);
		height *= screenDensity;
		width  *= screenDensity;
		setLayoutParams(new ViewGroup.LayoutParams((int)height, (int)width));
	}

	/**
	 * Loads and displays the image from the URL
	 * @param stringForURL
	 */
	public void loadImageFromURL(String stringForURL) {
		if (downloadImageTask!=null)
			downloadImageTask.cancel(true);
		downloadImageTask = new DownloadImageTask(this);
		downloadImageTask.execute(stringForURL);
	}

	/**
	 * An AsyncTask used to download the image and display it
	 *
	 * This class provides a Thread Safe method to download images
	 * in the background
	 */
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private ImageView bmImage;
		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}
		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}
		protected void onPostExecute(Bitmap result) {
			if (result!=null)
				bmImage.setImageBitmap(result);
		}
	}
}
