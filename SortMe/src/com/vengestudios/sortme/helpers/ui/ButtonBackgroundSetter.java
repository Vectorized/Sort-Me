package com.vengestudios.sortme.helpers.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * A helper class to set the background for Buttons
 *
 * This provides a future proof way of setting the background
 * while supporting deprecated methods to maintain
 * compatibility with older Android APIs
 */
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class ButtonBackgroundSetter {

	// A flag to determine if the new method is needed
	private static final boolean NEEDS_OLD_API =
			android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN;

	/**
	 * Sets a Bitmap as the background for the button
	 * @param button
	 * @param resources The resources for the application
	 * @param backgroundBitmap The Bitmap to be set as the background
	 */
	public static void setBackgroundBitmap(Button button, Resources resources, Bitmap backgroundBitmap) {
        if(NEEDS_OLD_API) {
            button.setBackgroundDrawable(new BitmapDrawable(resources, backgroundBitmap));
        } else {
            button.setBackground(new BitmapDrawable(resources, backgroundBitmap));
        }
	}

	/**
	 * Sets a Drawable as the background for the button
	 * @param button
	 * @param backgroundDrawable The Drawable to be set as the background
	 */
	public static void setBackgroundDrawable(Button button, Drawable backgroundDrawable) {
        if(NEEDS_OLD_API) {
            button.setBackgroundDrawable(backgroundDrawable);
        } else {
            button.setBackground(backgroundDrawable);
        }
	}

	/**
	 * Sets a Bitmap as the background for an ImageButton
	 * @param button
	 * @param resources The resources for the application
	 * @param backgroundBitmap The Bitmap to be set as the background
	 */
	public static void setBackgroundBitmap(ImageButton button, Resources resources, Bitmap backgroundBitmap) {
        if(NEEDS_OLD_API) {
            button.setBackgroundDrawable(new BitmapDrawable(resources, backgroundBitmap));
        } else {
            button.setBackground(new BitmapDrawable(resources, backgroundBitmap));
        }
	}

	/**
	 * Sets a Drawable as the background for an ImageButton
	 * @param button
	 * @param backgroundDrawable The Drawable to be set as the background
	 */
	public static void setBackgroundDrawable(ImageButton button, Drawable backgroundDrawable) {
        if(NEEDS_OLD_API) {
            button.setBackgroundDrawable(backgroundDrawable);
        } else {
            button.setBackground(backgroundDrawable);
        }
	}

	/**
	 * Gets a StateListDrawable that defines the background for
	 * a button in the normal and pressed states
	 * @param context The context of the application
	 * @param normalStateResourceId The resource ID for the Drawable for the
	 * normal state background
	 * @param pressedStateResourceId The resource ID for the Drawable for the
	 * pressed state background
	 * @param width The width of the Button
	 * @param height The height of the Button
	 * @return The StateListDrawable
	 */
    public static StateListDrawable getStateListDrawables(
    		Context context,
    		int normalStateResourceId,
    		int pressedStateResourceId,
    		int width, int height) {
    	Resources resources = context.getResources();
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[] {android.R.attr.state_pressed},
				new BitmapDrawable(resources, BitmapImporter.decodeSampledBitmapFromResource(
        				resources, pressedStateResourceId,
        				width, height)));
		stateListDrawable.addState(new int[] { },
				new BitmapDrawable(resources, BitmapImporter.decodeSampledBitmapFromResource(
        				resources, normalStateResourceId,
        				width, height)));
		return stateListDrawable;
    }

    /**
	 * Gets a StateListDrawable that defines the background for
	 * a button in the normal and pressed states
	 * @param context The context of the application
     * @param normalStateDrawable The Drawable for the normal state background
     * @param pressedStateDrawable The Drawable for the pressed state background
     * @return The StateListDrawable
     */
    public static StateListDrawable getStateListDrawables(
    		Context context,
    		Bitmap normalStateDrawable,
    		Bitmap pressedStateDrawable) {
    	Resources resources = context.getResources();
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[] {android.R.attr.state_pressed},
				new BitmapDrawable(resources, pressedStateDrawable));
		stateListDrawable.addState(new int[] { },
				new BitmapDrawable(resources, normalStateDrawable));
		return stateListDrawable;
    }

    /**
	 * Gets a StateListDrawable that defines the background for
	 * a button in the normal and pressed states
     * @param normalStateDrawable The Drawable for the normal state background
     * @param pressedStateDrawable The Drawable for the pressed state background
     * @return The StateListDrawable
     */
    public static StateListDrawable getStateListDrawables(
    		Drawable normalStateDrawable,
    		Drawable pressedStateDrawable) {
		StateListDrawable stateListDrawable = new StateListDrawable();
		stateListDrawable.addState(new int[] {android.R.attr.state_pressed},
				pressedStateDrawable);
		stateListDrawable.addState(new int[] { },
				normalStateDrawable);
		return stateListDrawable;
    }
}
