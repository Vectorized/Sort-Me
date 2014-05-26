package com.vengestudios.sortme.game;

import java.util.ArrayList;

import com.vengestudios.sortme.generaluielements.BlurOverlay;
import com.vengestudios.sortme.helpers.ui.Effects;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * The on screen game timer.
 *
 * Responsible for:
 *
 *  - Displaying the time left for the current game
 *
 *  - Casting the animations for the start and end of the game
 *
 *  - Coordinating the start and end of the game with other GameElements
 *
 */
public class GameTimer implements GameElement {

    // UI and animation constants
    private static final int   DEFAULT_DELAY_TO_GO          = 5000;

    private static final int   GAME_DURATION_IN_SECONDS     = 120;
    private static final float TIME_FONT_SIZE               = 28.f;
    private static final float SCREEN_X_PERCENTAGE          = 0.029f;
    private static final float SCREEN_Y_PERCENTAGE          = 0.138f;
    private static final int   TIME_FONT_COLOR              = Color.rgb(133, 133, 133);

    private static final int   MESSAGE_HEIGHT               = 200;

    private static final float READY_FONT_SIZE              = 50.f;
    private static final int   READY_WIDTH                  = 250;
    private static final float READY_OVERLAY_BLUR_RADIUS    = 5.f;

    private static final float GO_FONT_SIZE                 = 55.f;
    private static final int   GO_WIDTH                     = 150;
    private static final float GO_EFFECT_SCALE              = 3.f;
    private static final int   GO_EFFECT_DURATION           = 330;

    private static final float TIMES_UP_FONT_SIZE           = 50.f;
    private static final int   TIMES_UP_WIDTH               = 400;
    private static final float TIMES_UP_OVERLAY_BLUR_RADIUS = 7.f;
    private static final float TIMES_UP_EFFECT_STRENGTH     = 3.f;
    private static final int   TIMES_UP_EFFECT_DURATION     = 1000;
    private static final int   TIMES_UP_EFFECT_MOVE_X       = 300;
    private static final float TIMES_UP_EFFECT_START_ALPHA  = .8f;

    private static final int   DEFAULT_BLUR_OVERLAY_FADE_IN_DURATION  = 1000;
    private static final int   DEFAULT_BLUR_OVERLAY_FADE_OUT_DURATION = 1500;

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout         relativeLayout;
    @SuppressWarnings("unused")
    private Context                context;

    // UI Elements
    private TextView               timeTextView;
    private Handler                timerHandler;
    private Runnable               timerRunnable;
    private int                    secondsLeft;

    private TextView               readyTextView;
    private TextView               goTextView;
    private TextView               timesUpTextView;
    private BlurOverlay            blurOverlay;
    private ArrayList<ImageView>   afterImageViews;

    // Handlers and Runnables
    private Handler                startHandler;
    private Runnable               startRunnable;

    private Handler                endHandler;
    private Runnable               endRunnable;

    // GameElements Dependencies
    private TileSorterControl      tileSorterControl;
    private ParticipantCoordinator playerCoordinator;
    private ScoreBoard             scoreBoard;
    private MPBar                  mpBar;

    /**
     * Constructor
     *
     * Initializes and positions the required UI elements
     * and adds them to the RelativeLayout
     *
     * Initializes the Handlers and Runnables required for counting the game time
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param context        The context of the application (usually the MainActivity)
     */
    public GameTimer(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context         = context;

        int   screenHeight  = ScreenDimensions.getHeight(context);
        int   screenWidth   = ScreenDimensions.getWidth(context);
        float screenDensity = ScreenDimensions.getDensity(context);

        secondsLeft = GAME_DURATION_IN_SECONDS;

        float screenTopPadding  = SCREEN_Y_PERCENTAGE*screenHeight;
        float screenLeftPadding = SCREEN_X_PERCENTAGE*screenWidth;

        timeTextView = new TextView(context);
        timeTextView.setTextSize (TIME_FONT_SIZE);
        timeTextView.setTextColor(TIME_FONT_COLOR);
        timeTextView.setX        (screenLeftPadding);
        timeTextView.setY        (screenTopPadding);
        relativeLayout.addView(timeTextView);
        displayTimeInTextField();

        timerHandler = new Handler();
        timerRunnable = new TimerRunnable();

        blurOverlay = new BlurOverlay(context, relativeLayout);
        relativeLayout.addView(blurOverlay);
        blurOverlay.getLayoutParams().height = screenHeight;
        blurOverlay.getLayoutParams().width = screenWidth;

        float messageTopPadding  = screenHeight*.5f-MESSAGE_HEIGHT*.5f;

        readyTextView = new TextView(context);
        readyTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        readyTextView.setTextSize(READY_FONT_SIZE);
        readyTextView.setWidth   ((int)(READY_WIDTH*screenDensity));
        readyTextView.setHeight  (MESSAGE_HEIGHT);
        readyTextView.setGravity (Gravity.CENTER);
        readyTextView.setText    ("READY");
        readyTextView.setX       (screenWidth*.5f-READY_WIDTH*screenDensity*.5f);
        readyTextView.setY       (messageTopPadding);
        relativeLayout.addView(readyTextView);

        goTextView = new TextView(context);
        goTextView.setTypeface (Typeface.DEFAULT, Typeface.ITALIC);
        goTextView.setTextSize (GO_FONT_SIZE);
        goTextView.setWidth    ((int)(GO_WIDTH*screenDensity));
        goTextView.setHeight   (MESSAGE_HEIGHT);
        goTextView.setGravity  (Gravity.CENTER);
        goTextView.setText     ("GO!");
        goTextView.setX        (screenWidth*.5f-GO_WIDTH*screenDensity*.5f);
        goTextView.setY        (messageTopPadding);
        relativeLayout.addView(goTextView);

        timesUpTextView = new TextView(context);
        timesUpTextView.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
        timesUpTextView.setTextSize(TIMES_UP_FONT_SIZE);
        timesUpTextView.setWidth   ((int)(TIMES_UP_WIDTH*screenDensity));
        timesUpTextView.setHeight  (MESSAGE_HEIGHT);
        timesUpTextView.setGravity (Gravity.CENTER);
        timesUpTextView.setText    ("TIMES UP!");
        timesUpTextView.setX       (screenWidth*.5f-TIMES_UP_WIDTH*screenDensity*.5f);
        timesUpTextView.setY       (messageTopPadding);
        relativeLayout.addView(timesUpTextView);

        blurOverlay.excludeView(goTextView);
        blurOverlay.excludeView(readyTextView);
        blurOverlay.excludeView(timesUpTextView);

        afterImageViews = new ArrayList<ImageView>();
        for (int i=0; i<2; ++i) {
            ImageView afterImageView = new ImageView(context);
            relativeLayout .addView(afterImageView);
            afterImageViews.add(afterImageView);
            blurOverlay    .excludeView(afterImageView);
            afterImageView .setVisibility(View.INVISIBLE);
        }

        startHandler = new Handler();
        endHandler   = new Handler();

        hide();
        blurOverlay.setVisibility(View.INVISIBLE);
    }

    /**
     * Registers the TileSorterControl
     * @param tileSorterControl
     */
    public void registerTileSorterControl(TileSorterControl tileSorterControl) {
        this.tileSorterControl = tileSorterControl;
    }

    /**
     * Registers the PlayerCoordinator
     * @param playerCoordinator
     */
    public void registerParticipantCoordinator(ParticipantCoordinator playerCoordinator) {
        this.playerCoordinator = playerCoordinator;
    }

    /**
     * Registers the ScoreBoard
     * @param scoreBoard
     */
    public void registerScoreBoard(ScoreBoard scoreBoard) {
        this.scoreBoard = scoreBoard;
    }

    /**
     * Registers the MPBAr
     * @param mpBar
     */
    public void registerMPBar(MPBar mpBar) {
        this.mpBar = mpBar;
    }

    /**
     * Clears all animations and handlers.
     */
    private void clearAllAnimationsAndHandlers(){
        blurOverlay    .clearAnimation();
        readyTextView  .clearAnimation();
        timesUpTextView.clearAnimation();
        readyTextView  .setAlpha(1.f);
        timesUpTextView.setAlpha(1.f);
        blurOverlay    .setAlpha(1.f);
        timerHandler.removeCallbacks(timerRunnable);
        startHandler.removeCallbacks(startRunnable);
        endHandler  .removeCallbacks(endRunnable);
    }

    /**
     * Hides all the text views
     */
    private void hideTextViews(){
        timeTextView   .setVisibility(View.INVISIBLE);
        readyTextView  .setVisibility(View.INVISIBLE);
        goTextView     .setVisibility(View.INVISIBLE);
        timesUpTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hide() {
        hideTextViews();
        clearAllAnimationsAndHandlers();
        blurOverlay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideForGameEnd() {
        hideTextViews();
        clearAllAnimationsAndHandlers();
        fadeOutAndHideBlurOverlay();
    }

    @Override
    public void setupAndAppearForGame() {
        setupAndAppearForGame(DEFAULT_DELAY_TO_GO);
    }

    /**
     * Fades out and hide the blur overlay with the default duration
     */
    public void fadeOutAndHideBlurOverlay(){
        fadeOutAndHideBlurOverlay(DEFAULT_BLUR_OVERLAY_FADE_OUT_DURATION);
    }

    /**
     * Fades in and shows the blur overlay with the default duration
     */
    public void fadeInAndShowBlurOverlay(){
        fadeInAndShowBlurOverlay(DEFAULT_BLUR_OVERLAY_FADE_IN_DURATION);
    }

    /**
     * Fades out and hides the blur overlay
     * @param duration
     */
    private void fadeOutAndHideBlurOverlay(int duration) {
        Effects.castFadeOutEffect(blurOverlay, duration, true, true);
    }

    /**
     * Fades in and shows the blur overlay
     * @param duration
     */
    private void fadeInAndShowBlurOverlay(int duration) {
        Effects.castFadeInEffect(blurOverlay, 1.f, duration, true);
    }

    /**
     * Sets up the GameTimer for the game and make the required
     * UI Elements for the start of the game to appear
     *
     * @param delayToGo The milliseconds to the start of the game from
     *                  the time when this method is called
     */
    public void setupAndAppearForGame(int delayToGo) {
        clearAllAnimationsAndHandlers();

        timesUpTextView.setVisibility(View.INVISIBLE);

        timeTextView   .setVisibility(View.VISIBLE);
        readyTextView  .setVisibility(View.VISIBLE);
        blurOverlay    .setBlurRadius(READY_OVERLAY_BLUR_RADIUS);

        blurOverlay    .bringToFront();
        readyTextView  .bringToFront();
        goTextView     .bringToFront();
        timesUpTextView.bringToFront();

        for (View view:afterImageViews)
            view.bringToFront();

        secondsLeft = GAME_DURATION_IN_SECONDS;
        displayTimeInTextField();

        timerHandler.removeCallbacks(timerRunnable);
        startHandler.removeCallbacks(startRunnable);

        Effects.castBlinkEffect(readyTextView, 3, 150, false);

        startRunnable = new Runnable() {
            @Override
            public void run() {
                start();
            }
        };
        startHandler.postDelayed(startRunnable, delayToGo);

        blurOverlay.setTimesToRedraw(3);
        blurOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the game
     */
    public void start(){
        goTextView.setVisibility(View.VISIBLE);

        fadeOutAndHideBlurOverlay(1000);
        Effects.castFadeOutEffect(readyTextView, 300, true, true);
        Effects.castExpandingAfterImageEffect(goTextView, afterImageViews.get(0), 1.0f,
                GO_EFFECT_SCALE, GO_EFFECT_SCALE, GO_EFFECT_DURATION);

        startRunnable = new Runnable() {
            @Override
            public void run() {
                goTextView.setVisibility(View.INVISIBLE);
                assert tileSorterControl != null;
                tileSorterControl.unlock();
                assert playerCoordinator != null;
                playerCoordinator.startSwitchingTarget();
            }
        };
        startHandler.postDelayed(startRunnable, 360);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    /**
     * Updates and display the time left
     */
    private void displayTimeInTextField(){
        int minutes = secondsLeft/60;
        int seconds = secondsLeft%60;
        timeTextView.setText(minutes+":"+((seconds>9)?"":"0")+seconds);
    }

    /**
     * Ends the current game
     */
    public void endGame() {
        mpBar.resetMP();
        timesUpTextView.setVisibility(View.VISIBLE);

        Effects.castFadeAwayAfterImageEffect(timesUpTextView, afterImageViews.get(0),
                0.f, 0.f,
                1.0f, 1.0f,
                TIMES_UP_EFFECT_START_ALPHA,
                TIMES_UP_EFFECT_MOVE_X, 0.f,
                TIMES_UP_EFFECT_STRENGTH, TIMES_UP_EFFECT_DURATION);
        Effects.castFadeAwayAfterImageEffect(timesUpTextView, afterImageViews.get(1),
                0.f, 0.f,
                1.0f, 1.0f,
                TIMES_UP_EFFECT_START_ALPHA,
                -TIMES_UP_EFFECT_MOVE_X, 0.f,
                TIMES_UP_EFFECT_STRENGTH, TIMES_UP_EFFECT_DURATION);

        endRunnable = new Runnable() {
            int timesRun = 0;
            @Override
            public void run() {
                if (timesRun==0) {
                    blurOverlay.setTimesToRedraw(0);
                    blurOverlay.setBlurRadius(TIMES_UP_OVERLAY_BLUR_RADIUS);
                    fadeInAndShowBlurOverlay(1500);
                    endHandler.postDelayed(this, 2500-600);
                } else if (timesRun==1) {
                    Effects.castFadeOutEffect(timesUpTextView, 700, false, true);
                    endHandler.postDelayed(this, 300);
                } else if (timesRun==2) {
                    assert scoreBoard != null;
                    scoreBoard.registerParticipantDatas(playerCoordinator.getCopyOfParticipantDatasWithDsecPositions());
                    scoreBoard.dropDownAndShowScoreBoard();
                    playerCoordinator.submitGameResults();
                }
                ++timesRun;
            }
        };


        endHandler.postDelayed(endRunnable, 600);
        timerHandler.removeCallbacks(timerRunnable);

        assert tileSorterControl != null;
        tileSorterControl.lock();
    }

    /**
     * Called when the time runs out
     */
    private void timesUpFunction(){
        endGame();
    }

    /**
     * A runnable that is called by the TimerHandler every second
     * to decrement the time left for the current game.
     *
     * Calls the timesUpFunction() when the time runs out.
     */
    private class TimerRunnable implements Runnable {
        @Override
        public void run() {
            secondsLeft--;
            displayTimeInTextField();
            if (secondsLeft>0)
                timerHandler.postDelayed(timerRunnable, 1000);
            else
                timesUpFunction();
        }
    }

}
