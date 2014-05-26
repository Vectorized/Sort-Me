package com.vengestudios.sortme.game;

import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Manages the activation and casts the animations for the various PowerUps
 *
 * Responsible for:
 *
 *  - Acting as an intermediately between other Game Elements and
 *    TileSorterControl in the activation of PowerUps
 *
 *    It attempts to activate PowerUps by calling the required
 *    methods on TileSorterControl and casting the
 *    accompanying animations.
 *
 *  - Defining the duration of Bubbletized and Shielded
 *
 *  - Deactivating PowerUps in sync with the accompanying animations
 */
public class PowerupActivator implements GameElement {

    // UI, animation and game mechanics constants
    private static final float BUBBLETIZED_GRADIENT_Y_PERCENTAGE = 0.38f;
    private static final float BUBBLETIZED_TOTAL_DURATION        = 10.f;
    private static final float BUBBLETIZED_FADE_IN_DURATION      = 0.5f;
    private static final float BUBBLETIZED_FADE_OUT_DURATION     = 1.5f;
    private static final float SHIELDED_GRADIENT_Y_PERCENTAGE    = 0.38f;
    private static final float SHIELDED_TOTAL_DURATION           = 10.f;
    private static final float SHIELDED_FADE_IN_DRUATION         = 0.5f;
    private static final float SHIELDED_FADE_OUT_DURATION        = 1.5f;

    private static final GradientDrawable BUBBLETIZED_GRADIENT_DRAWABLE;
    private static final GradientDrawable SHIELDED_GRADIENT_DRAWABLE;
    static {
        int [] bubbletizedColors = {Color.argb(0, 121, 161, 178),
                                    Color.argb(135, 121, 165, 188),
                                    Color.argb(225, 121, 171, 198)};
        BUBBLETIZED_GRADIENT_DRAWABLE = new GradientDrawable(Orientation.TOP_BOTTOM, bubbletizedColors);
        int [] shieldedColors = {Color.argb(0, 99, 99, 99),
                               Color.argb(220, 99, 99, 99)};
        SHIELDED_GRADIENT_DRAWABLE = new GradientDrawable(Orientation.TOP_BOTTOM, shieldedColors);
    }

    // Handlers, Runnables and ImageViews for different effects
    private Handler   bubbletizedHandler;
    private Runnable  bubbletizedRunnable;
    private ImageView bubbletizedEffectView;

    private Handler   shieldedHandler;
    private Runnable  shieldedRunnable;
    private ImageView shieldedEffectView;

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout  relativeLayout;
    @SuppressWarnings("unused")
    private Context         context;

    // GameElement Dependencies
    private TileSorterControl tileSorterControl;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context        The context of the application (usually MainActivity)
     */
    public PowerupActivator(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context        = context;

        int screenHeight = ScreenDimensions.getHeight(context);
        int screenWidth  = ScreenDimensions.getWidth(context);

        bubbletizedHandler    = new Handler();
        bubbletizedEffectView = new ImageView(context);
        bubbletizedEffectView.setImageDrawable(BUBBLETIZED_GRADIENT_DRAWABLE);
        relativeLayout.addView(bubbletizedEffectView);
        bubbletizedEffectView.getLayoutParams().width = screenWidth;
        bubbletizedEffectView.getLayoutParams().height =
                (int)(screenHeight*(1.f-BUBBLETIZED_GRADIENT_Y_PERCENTAGE)+1.f);
        bubbletizedEffectView.setY(screenHeight*BUBBLETIZED_GRADIENT_Y_PERCENTAGE);
        bubbletizedEffectView.setAlpha(0.f);

        shieldedHandler    = new Handler();
        shieldedEffectView = new ImageView(context);
        shieldedEffectView.setImageDrawable(SHIELDED_GRADIENT_DRAWABLE);
        relativeLayout.addView(shieldedEffectView);
        shieldedEffectView.getLayoutParams().width = screenWidth;
        shieldedEffectView.getLayoutParams().height =
                (int)(screenHeight*(1.f-SHIELDED_GRADIENT_Y_PERCENTAGE)+1.f);
        shieldedEffectView.setY(screenHeight*SHIELDED_GRADIENT_Y_PERCENTAGE);

        hide();
    }

    @Override
    public void hide() {
        bubbletizedEffectView.setAlpha(0.f);
        bubbletizedEffectView.setVisibility(View.INVISIBLE);
        shieldedEffectView   .setAlpha(0.f);
        shieldedEffectView   .setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    @Override
    public void setupAndAppearForGame() {
        if (tileSorterControl!=null) {
            tileSorterControl .unBubbletize();
            tileSorterControl .unshield();
            bubbletizedHandler.removeCallbacks(bubbletizedRunnable);
            shieldedHandler   .removeCallbacks(shieldedRunnable);
            hide();
        }
    }

    /**
     * Registers the TileSortControl
     * @param tileSorterControl
     */
    public void registerTileSorterControl(TileSorterControl tileSorterControl) {
        this.tileSorterControl = tileSorterControl;
    }

    /**
     * Activates the shield PowerUp
     */
    public void shield() {
        if (tileSorterControl==null) return;
        tileSorterControl.shield();
        if (shieldedRunnable!=null) {
            shieldedHandler.removeCallbacks(shieldedRunnable);
        }
        shieldedRunnable = new ShieldedRunnable();
        shieldedHandler.postDelayed(shieldedRunnable, 16);
    }

    /**
     * An EffectRunnable for casting the Shield PowerUp animation and
     * ending the shielded status
     */
    private class ShieldedRunnable extends EffectRunnable {
        public ShieldedRunnable() {
            super(shieldedEffectView,
                    shieldedHandler,
                    SHIELDED_TOTAL_DURATION,
                    SHIELDED_FADE_IN_DRUATION,
                    SHIELDED_FADE_OUT_DURATION);
        }
        @Override
        protected void endFunction() { tileSorterControl.unshield(); }

    }

    /**
     * Activates the randomize attack on the user
     * @return Whether the attack is successful (i.e. not blocked)
     */
    public boolean randomize(){
        return tileSorterControl.randomize();
    }

    /**
     * Activates the UpSize attack on the user
     * @return Whether the attack is successful (i.e. not blocked)
     */
    public boolean upsize(){
        return tileSorterControl.upsize();
    }

    /**
     * Activates the Bubbletize attack on the user
     * @return Whether the attack is successful (i.e. not blocked)
     */
    public boolean bubbletize() {
        if (tileSorterControl==null)               return false;
        if (tileSorterControl.bubbletize()==false) return false;
        if (bubbletizedRunnable!=null) {
            bubbletizedHandler.removeCallbacks(bubbletizedRunnable);
        }
        bubbletizedRunnable = new BubbletizedRunnable();
        bubbletizedHandler.postDelayed(bubbletizedRunnable, 16);

        return true;
    }

    /**
     * An EffectRunnable for casting the Bubbletize PowerUp animation and
     * ending the Bubbletized status
     */
    private class BubbletizedRunnable extends EffectRunnable {
        public BubbletizedRunnable() {
            super(bubbletizedEffectView,
                    bubbletizedHandler,
                    BUBBLETIZED_TOTAL_DURATION,
                    BUBBLETIZED_FADE_IN_DURATION,
                    BUBBLETIZED_FADE_OUT_DURATION);
        }
        @Override protected void endFunction() { tileSorterControl.unBubbletize(); }
    }

    /**
     * A Runnable to cast a fade in, fade out background effect
     * and end the PowerUp at the same time the animation ends
     */
    private abstract class EffectRunnable implements Runnable {
        private float frameCount;
        private float fadeInFrames;
        private float fadeOutFrames;
        private float maxFrameCount;
        private float fadeOutStartFrame;
        private float beginningAlpha;
        View effectView;
        Handler effectHandler;
        public EffectRunnable(View effectView, Handler effectHandler,
                float totalDuration, float fadeInDuration, float fadeOutDuration) {
            this.effectView         = effectView;
            this.effectHandler      = effectHandler;
            this.maxFrameCount      = totalDuration*60.f;
            this.fadeInFrames       = fadeInDuration*60.f;
            this.fadeOutFrames      = fadeOutDuration*60.f;
            this.fadeOutStartFrame  = this.maxFrameCount-this.fadeOutFrames;
        }
        @Override
        public void run() {
            if (frameCount==0) {
                this.effectView.setVisibility(View.VISIBLE);
                this.beginningAlpha = this.effectView.getAlpha();
            }
            if (frameCount<maxFrameCount) {
                float alpha = 1.f;
                if (frameCount<fadeInFrames) {
                    alpha = quadEaseInOut(frameCount, beginningAlpha, 1.f, fadeInFrames);
                } else if (frameCount>fadeOutStartFrame) {
                    alpha = linearEase(frameCount-fadeOutStartFrame, 1.f, -1.f, fadeOutFrames);
                }
                this.effectView.setAlpha(alpha);
                this.effectHandler.postDelayed(this, 16);
                ++frameCount;
            } else  {
                this.effectView.setAlpha(0.f);
                this.effectView.setVisibility(View.INVISIBLE);
                endFunction();
            }
        }
        protected abstract void endFunction();
        private float quadEaseInOut (float t, float b, float c, float d) {
            if ((t/=d/2) < 1) return c/2*t*t + b;
            return -c/2 * ((--t)*(t-2) - 1) + b;
        }
        private float linearEase (float t, float b, float c, float d) {
            return c*t/d + b;
        }
    }

}
