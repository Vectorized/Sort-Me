package com.vengestudios.sortme.helpers.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * A helper class to import Bitmaps efficiently into memory
 */
public class BitmapImporter {

	/**
	 * Gets a down-sampled decoded Bitmap
	 *
	 * @param res       The resources of the application
	 * @param resId     The resource ID of the image for the Bitmap
	 * @param reqWidth  The required width of the Bitmap
	 * @param reqHeight The required height of the Bitmap
	 * @return          The decoded Bitmap
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Calculates the required in sample size to load the Bitmap
	 *
	 * @param options   The options containing the required width and height
	 *                  of the sample to load the Bitmap
	 * @param reqWidth  The final required width of the Bitmap
	 * @param reqHeight The final required height of the Bitmap
	 * @return          The required in sample size to load the Bitmap
	 */
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}


}
