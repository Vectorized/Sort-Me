package com.vengestudios.sortme;

import com.vengestudios.sortme.helpers.ui.Effects;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A screen that displays a loading message
 */
public class WaitScreen implements Screen{
    @SuppressWarnings("unused")
    private static final float  PULSE_ANIMATION_FRAME_INTERVAL = .16f;
    private static final float  LOADING_TEXT_FONT_SIZE         = 22.f;
    private static final String LOADING_TEXT = "Loading, Please Wait...";

    // Dependencies to create UI Elements
    private RelativeLayout waitScreenContainer;

    // UI Elements
    private TextView       loadingTextView;

    // Casts a continuously fade-in-out animation
    private Effects.PulsulationAnimator pulsulationAnimator;

    /**
     * Constructor
     *
     * @param relativeLayout The RelativeLayout to insert the UI elements into
     * @param context        The context of the application
     */
    public WaitScreen(RelativeLayout relativeLayout, Context context) {
        int screenHeight = ScreenDimensions.getHeight(context);
        int screenWidth  = ScreenDimensions.getWidth(context);

        waitScreenContainer = new RelativeLayout(context);
        waitScreenContainer.setGravity(Gravity.CENTER);
        waitScreenContainer.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight));
        relativeLayout.addView(waitScreenContainer);

        loadingTextView = new TextView(context);
        loadingTextView.setText(LOADING_TEXT);
        loadingTextView.setTextSize(LOADING_TEXT_FONT_SIZE);
        waitScreenContainer.addView(loadingTextView);

        pulsulationAnimator = new Effects.PulsulationAnimator(loadingTextView, .5f, .8f, 330);
    }

    @Override
    public void hide() {
        pulsulationAnimator.stop();
        waitScreenContainer.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        pulsulationAnimator.start();
        waitScreenContainer.setVisibility(View.VISIBLE);
    }

}
