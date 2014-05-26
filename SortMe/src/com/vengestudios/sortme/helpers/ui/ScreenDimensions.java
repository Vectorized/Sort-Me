package com.vengestudios.sortme.helpers.ui;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * A singleton helper class to get information of the device's screen
 */
public class ScreenDimensions {
    private int     screenWidth     = -1;
    private int     screenHeight    = -1;
    private float   screenDensity   = -1.f;
    private boolean hasDimensions   = false;

    private final static ScreenDimensions instance = new ScreenDimensions();
    private ScreenDimensions() {}

    /**
     * @param context The context of the application
     * @return        The width of the screen
     */
    public static int getWidth(Context context) {
        instance.getDimensions(context);
        return instance.screenWidth;
    }

    /**
     * @param context The context of the application
     * @return        The height of the screen
     */
    public static int getHeight(Context context) {
        instance.getDimensions(context);
        return instance.screenHeight;
    }

    /**
     * @param context The context of the application
     * @return        The pixel density multiplier of the screen
     */
    public static float getDensity(Context context) {
        instance.getDimensions(context);
        return instance.screenDensity;
    }

    /**
     * Sets up the singleton with the information of the device's screen
     * @param context The context of the application
     */
    private void getDimensions(Context context) {
        if (hasDimensions==true) return;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenDensity = context.getResources().getDisplayMetrics().density;
        if (size.x>size.y) {
            screenWidth = size.x;
            screenHeight = size.y;
        } else {
            screenHeight = size.x;
            screenWidth = size.y;
        }
    }
}
