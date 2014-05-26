package com.vengestudios.sortme.game;

import java.text.NumberFormat;
import java.util.Locale;

import com.vengestudios.sortme.helpers.ui.Effects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A specialized TextView to display the score of a participant
 */
@SuppressLint("ViewConstructor")
public class ScoreLabel extends TextView {

	// UI and animation constants
    private static final float FONT_SIZE                        = 22.f;
    private static final int   SORTED_ANIMATION_MOVE_Y          = -80;
    private static final int   SORTED_ANIMATION_START_Y_OFFSET  = 10;
    private static final int   SORTED_ANIMATION_TIME            = 1000;
    private static final float SORTED_ANIMATION_STRENGTH        = 1.5f;
    private static final float SORTED_ANIAMTION_SCALE           = 1.25f;
    private static final float SORTED_ANIMATION_START_ALPHA     = 0.8f;

    private static final NumberFormat SCORE_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
	private RelativeLayout  relativeLayout;

    // The actual score
    private int             actualScore;

    // An ImageView to draw the effect of changing scores
    private ImageView       sortedAfterImageView;

    /**
     * Constructor
     *
     * Initializes and positions the required UI elements
     * and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param context        The context of the application (usually the MainActivity)
     */
    public ScoreLabel(RelativeLayout relativeLayout, Context context) {
        super(context);

        this.relativeLayout = relativeLayout;

        actualScore         = 0;

        setText(SCORE_FORMAT.format(actualScore));
        setTextSize(FONT_SIZE);
        setTextIsSelectable(false);
        setSingleLine(true);
        setY(-2.f);
        setTextColor(Color.rgb(88,88,88));
        setGravity(Gravity.CENTER_HORIZONTAL);

        sortedAfterImageView = new ImageView(context);
        relativeLayout.addView(sortedAfterImageView);
    }

    /**
     * Brings the ImageView used for the score changing effect
     * to the front of other UI elements on the screen
     */
    public void bringScoreLabelEffectViewToFront(){
        sortedAfterImageView.bringToFront();
    }

    /**
     * Sets the score
     * @param score
     */
    public void setScore(int score) {
        actualScore = score;
        this.setText(SCORE_FORMAT.format(actualScore));
        Effects.castFadeAwayAfterImageEffect(this, sortedAfterImageView,
                -1.f, SORTED_ANIMATION_START_Y_OFFSET,
                SORTED_ANIAMTION_SCALE, SORTED_ANIAMTION_SCALE,
                SORTED_ANIMATION_START_ALPHA,
                0.f, SORTED_ANIMATION_MOVE_Y,
                SORTED_ANIMATION_STRENGTH, SORTED_ANIMATION_TIME);
    }
}
