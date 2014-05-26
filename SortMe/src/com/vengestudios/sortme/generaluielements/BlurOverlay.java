package com.vengestudios.sortme.generaluielements;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Bitmap.Config;

import android.os.Handler;

import android.support.v8.renderscript.*;

import android.view.View;
import android.widget.ImageView;

/**
 * A View that implements a real time blur on another View,
 * creating a frosted-glass effect.
 *
 * Uses the efficient Android RenderScript library for the blur.
 *
 * Inspired by the popular Gaussian Blur effects in iOS7.
 */
@SuppressLint("ViewConstructor")
public class BlurOverlay extends ImageView {

	// UI Formatting constants
    private static final float DEFAULT_BITMAP_SCALE       = .3f;
    private static final float DEFAULT_BLUR_RADIUS        = 5.f;
    private static final int   DEFAULT_FIRST_REDRAW_DELAY = 500;
    private static final int   DEFAULT_REDRAW_INTERVAL    = 1000;
    private static final int   DEFAULT_TIMES_TO_REDRAW    = 2;
    private static final int   DEFAULT_TINT_COLOR         = Color.argb(80, 255, 255, 255);
    private static final int   BLUR_BACKGROUND_COLOR      = Color.WHITE;

    private static final Bitmap BLANK_BITMAP
        = Bitmap.createBitmap(1, 1, Config.ARGB_8888);

    private Context         context;

    private View            viewToDraw;
    private Handler         redrawHandler;
    private RedrawRunable   redrawRunnable;
    private ArrayList<View> viewsToExclude;
    private int             timesToRedraw;
    private int             firstRedrawDelay;
    private int             redrawInterval;
    private float           bitmapScale;
    private float           blurRadius;
    private int             tintColor;

    private RenderScript        renderScript;
    private ScriptIntrinsicBlur intrinsicBlur;

    /**
     * Constructor
     *
     * Creates a BlurOverlay
     *
     * @param context The context of the application
     * @param viewToDraw The View to snapshot for the blur
     */
    public BlurOverlay(Context context, View viewToDraw) {
        this(context, viewToDraw, DEFAULT_BITMAP_SCALE, DEFAULT_BLUR_RADIUS,
        		DEFAULT_TINT_COLOR, DEFAULT_TIMES_TO_REDRAW,
        		DEFAULT_REDRAW_INTERVAL, DEFAULT_FIRST_REDRAW_DELAY);
    }

    /**
     *
     * @param context The context of the application
     * @param viewToDraw The View to snapshot for the blur
     * @param bitmapScale The scale of the Bitmap used for the blur.
     * Smaller values give higher speed, but lower blur quality.
     * @param blurRadius The radius of the Gaussian Blur
     * @param tintColor The color to tint the blur
     * @param timesToRedraw How many times to redraw from the time being shown
     * @param redrawInterval The redraw interval
     * @param firstRedrawDelay The delay to the first redraw
     */
    public BlurOverlay(Context context, View viewToDraw, float bitmapScale, float blurRadius,
    		int tintColor, int timesToRedraw,
    		int redrawInterval, int firstRedrawDelay) {
        super(context);
        this.context            = context;
        this.bitmapScale        = bitmapScale;
        this.blurRadius         = blurRadius;
        this.tintColor          = tintColor;
        this.timesToRedraw      = timesToRedraw;
        this.redrawInterval     = redrawInterval;
        this.firstRedrawDelay   = firstRedrawDelay;

        this.viewToDraw = viewToDraw;

        viewsToExclude  = new ArrayList<View>();

        renderScript    = RenderScript.create(context);
        intrinsicBlur   = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        redrawHandler   = new Handler();
        redrawRunnable  = new RedrawRunable();
        redrawHandler.postDelayed(redrawRunnable, firstRedrawDelay);
        setBackgroundColor(Color.WHITE);

    }

    /**
     * A Runnable used to schedule the redraws
     */
    private class RedrawRunable implements Runnable {
        private int redrawCount = 0;
        @Override
        public void run() {
            drawBlur();
            if (redrawCount<timesToRedraw) {
                postDelayed(redrawRunnable, redrawInterval);
                redrawCount++;
            }
        }
        public void resetRedrawCount() { redrawCount = 0; }
    }

    /**
     * Sets the redraw interval
     * @param redrawInterval
     */
    public void setRedrawInterval(int redrawInterval) {
        this.redrawInterval = redrawInterval;
    }

    /**
     * Sets the times to redraw after being shown
     * @param timesToRedraw
     */
    public void setTimesToRedraw(int timesToRedraw) {
        this.timesToRedraw = timesToRedraw;
    }

    /**
     * Sets the delay of the first redraw from the time being shown
     * @param firstRedrawDelay
     */
    public void setFirstRedrawDelay(int firstRedrawDelay) {
        this.firstRedrawDelay = firstRedrawDelay;
    }

    /**
     * Sets the radius for the Gaussian Blur
     * @param blurRadius
     */
    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
    }

    /**
     * Sets the color of the tint on the blurred image
     * @param tintColor
     */
    public void setTintColor(int tintColor) {
    	this.tintColor = tintColor;
    }

    @Override
    public void setVisibility(int visibility) {
        if (getVisibility()==visibility) return;
        super.setVisibility(visibility);
        setActive(visibility==View.VISIBLE);
    }

    /**
     * Resets the redraw count to 0, and starts redrawing if active is true.
     * Else, stops redrawing
     * @param active
     */
    public void setActive(boolean active) {
        if (active) {
            redrawRunnable.resetRedrawCount();
            redrawHandler.postDelayed(redrawRunnable, firstRedrawDelay);
        } else {
            redrawHandler.removeCallbacks(redrawRunnable);
        }
    }

    /**
     * Adds a view to the list of views to exclude from the snapshot
     * @param v The view to exclude
     */
    public void excludeView(View v) {
        viewsToExclude.add(v);
    }

    /**
     * Draws the blur
     */
    private void drawBlur() {
        ArrayList<View> hiddenViews = new ArrayList<View>();
        for (View view:viewsToExclude) {
            if (view.getVisibility()==View.VISIBLE) {
                view.setVisibility(View.INVISIBLE);
                hiddenViews.add(view);
            }
        }

        setImageBitmap(BLANK_BITMAP);
        setBackgroundColor(Color.TRANSPARENT);
        setImageBitmap(getTintedBitmap(blur(context, getScreenshot(viewToDraw), true)));
        for (View view:hiddenViews)
            view.setVisibility(View.VISIBLE);
    }

    /**
     * Gets a tinted Bitmap from a Bitmap
     * @param image The Bitmap to create the tinted Bitmap from
     * @return
     */
    private Bitmap getTintedBitmap(Bitmap image) {
    	Canvas c = new Canvas(image);
    	c.drawColor(tintColor);
        return image;
    }

    /**
     * Gets a blurred Bitmap
     * @param ctx the context of the application
     * @param image The Bitmap to be blurred
     * @param recycleOriginalBitmap Whether to recycle the memory used
     * for storing the Bitmap passed in
     * @return The blurred Bitmap
     */
    private Bitmap blur(Context ctx, Bitmap image, boolean recycleOriginalBitmap) {
        int width = Math.round(image.getWidth() * bitmapScale);
        int height = Math.round(image.getHeight() * bitmapScale);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
        intrinsicBlur.setRadius(blurRadius);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);

        tmpOut.copyTo(outputBitmap);

        if (recycleOriginalBitmap)
        	image.recycle();

        return outputBitmap;
    }

    /**
     * Returns a Bitmap representing a snapshot of a View
     * @param v The View to snapshot
     * @return The Snapshot
     */
    private Bitmap getScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        b.eraseColor(BLUR_BACKGROUND_COLOR);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}
