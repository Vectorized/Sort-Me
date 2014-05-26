package com.vengestudios.sortme.game;

import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * A GameElement displaying an on screen progress bar representing the
 * amount of Mana Points (MP) that the user has
 *
 * Responsible for:
 *
 *  - Displaying the amount of MP left
 *
 *  - Calling the PowerupButtonManager to unlock any PowerupButton
 *    that can be used when there is enough MP
 *
 *  - Providing the TileSorterControl with methods to increment the MP
 */
public class MPBar implements GameElement {

    // Some animation and UI constants
    private static final int   BAR_ANIMATION_INTERVAL            = 16;
    private static final int   FRAMES_PER_UPDATE                 = 10;
    private static final int   FRAMES_BEFORE_DECREMENT           = 80;
    private static final float BAR_TWEEN_FACTOR                  = .08f;
    private static final float BAR_DECREMENT_PER_ANIMATION_FRAME = .3f;
    private static final float MAX_MP                            = 1000;
    private static final float SCREEN_WIDTH_PERCENTAGE           = .87f;
    private static final float SCREEN_Y_PERCENTAGE               = .635f+.03f;
    private static final int   PROGRESS_BAR_HEIGHT_PIXELS        = 5;

    private static final int   PROGRESS_BAR_TOP_COLOR        = Color.rgb(218, 139, 232);
    private static final int   PROGRESS_BAR_BACKGROUND_COLOR = Color.argb(50, 0, 0, 0);

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout  relativeLayout;
    @SuppressWarnings("unused")
    private Context         context;

    // UI Elements
    private View            progressBarTop;
    private View            progressBarBackground;

    private Handler         progressBarHandler;
    private Runnable        progressBarRunnable;
    private int             framesAfterIncremented;

    private float           actualMP;
    private float           shownMP;

    private int             progressBarHeight;
    private float           progressBarWidth;

    // GameElement Dependencies
    private PowerupButtonManager powerupButtonManager;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context        The context of the application (usually MainActivity)
     */
    public MPBar(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context = context;

        int screenWidth = ScreenDimensions.getWidth(context);
        int screenHeight = ScreenDimensions.getHeight(context);
        float layoutLeftPadding = .5f*(1.0f-SCREEN_WIDTH_PERCENTAGE)*screenWidth;
        float layoutTopPadding = screenHeight*SCREEN_Y_PERCENTAGE;
        progressBarHeight = (int)(ScreenDimensions.getDensity(context)*PROGRESS_BAR_HEIGHT_PIXELS);
        progressBarWidth  = (int)(screenWidth*SCREEN_WIDTH_PERCENTAGE);

        progressBarBackground = new View(context);
        relativeLayout.addView(progressBarBackground);
        progressBarBackground.getLayoutParams().width  = (int) progressBarWidth;
        progressBarBackground.getLayoutParams().height = progressBarHeight;
        progressBarBackground.setX(layoutLeftPadding);
        progressBarBackground.setY(layoutTopPadding);
        progressBarBackground.setBackgroundColor(PROGRESS_BAR_BACKGROUND_COLOR);

        progressBarTop = new View(context);
        relativeLayout.addView(progressBarTop);
        progressBarTop.getLayoutParams().width  = 0;
        progressBarTop.getLayoutParams().height = progressBarHeight;
        progressBarTop.setX(layoutLeftPadding);
        progressBarTop.setY(layoutTopPadding);
        progressBarTop.setBackgroundColor(PROGRESS_BAR_TOP_COLOR);

        progressBarHandler = new Handler();
        progressBarRunnable = new ProgressBarRunnable();

        hide();
    }

    @Override
    public void hide() {
        progressBarTop       .setVisibility(View.INVISIBLE);
        progressBarBackground.setVisibility(View.INVISIBLE);
        progressBarHandler   .removeCallbacks(progressBarRunnable);
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    @Override
    public void setupAndAppearForGame() {
        actualMP = shownMP = 0.f;
        setProgress(0);
        progressBarTop       .setVisibility(View.VISIBLE);
        progressBarBackground.setVisibility(View.VISIBLE);

        progressBarHandler   .removeCallbacks(progressBarRunnable);
        progressBarHandler   .postDelayed(progressBarRunnable, BAR_ANIMATION_INTERVAL);
    }

    /**
     * Increments the MP by a certain amount
     * @param amount
     */
    public void incrementMP(float amount) {
        incrementMP(amount, true);
    }

    /**
     * Increments the MP by a certain amount
     * @param amount
     * @param update Whether to update the PowerupButtonManager and
     * enable the PowerupButtons that can be used with the new MP level
     */
    public void incrementMP(float amount, boolean update) {
        actualMP = Math.min(MAX_MP, actualMP+amount);
        framesAfterIncremented = 0;
        if (update)
            updatePowerupButtonManager();
    }

    /**
     * Resets the MPBar to zero MP
     */
    public void resetMP() {
        decrementMP(MAX_MP);
    }

    /**
     * Decreases the MP by a certain amount
     * @param amount
     */
    public void decrementMP(float amount) {
        decrementMP(amount, true);
    }

    /**
     * Decrements the MP by a certain amount
     * @param amount
     * @param update Whether to update the PowerupButtonManager and
     * disable the PowerupButtons that can be used with the new MP level
     */
    public void decrementMP(float amount, boolean update) {
        actualMP = Math.max(0, actualMP-amount);
        if (update)
            updatePowerupButtonManager();
    }

    /**
     * Calls on the PowerupButtonManager to enable/disable the PowerupButtons
     * accordingly with the current MP level
     */
    private void updatePowerupButtonManager(){
        assert powerupButtonManager != null;
        powerupButtonManager.setEnabledForMP(actualMP);
    }

    /**
     * Registers the PowerupButtonManager
     * @param powerupButtonManager
     */
    public void registerPowerupButtonManager(PowerupButtonManager powerupButtonManager) {
        this.powerupButtonManager = powerupButtonManager;
    }

    /**
     * A Runnable used by the ProgressBarHandler to decrease the MP over time
     * as well as to ease the animation of the MP level
     */
    private class ProgressBarRunnable implements Runnable {
        int updateRegulator = 0;
        @Override
        public void run() {
            if (framesAfterIncremented<FRAMES_BEFORE_DECREMENT) {
                ++framesAfterIncremented;
            } else {
                if (updateRegulator==FRAMES_PER_UPDATE) {
                    decrementMP(BAR_DECREMENT_PER_ANIMATION_FRAME);
                    updateRegulator = 0;
                } else {
                    decrementMP(BAR_DECREMENT_PER_ANIMATION_FRAME, false);
                    ++updateRegulator;
                }
            }
            shownMP += (actualMP-shownMP)*BAR_TWEEN_FACTOR;
            setProgress(shownMP);
            progressBarHandler.postDelayed(progressBarRunnable, BAR_ANIMATION_INTERVAL);
        }
    }

    /**
     * Sets the progress bar to display the current MP level
     * @param mp
     */
    private void setProgress(float mp) {
        float proportionOfTotalWidth = mp/MAX_MP;
        progressBarTop.setLayoutParams(
                new RelativeLayout.LayoutParams((int)(proportionOfTotalWidth*progressBarWidth), progressBarHeight));
        progressBarTop.setAlpha(Math.min(proportionOfTotalWidth*2.f+.45f, 1.f));
    }

}
