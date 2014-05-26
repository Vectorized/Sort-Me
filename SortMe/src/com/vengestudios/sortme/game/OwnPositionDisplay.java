package com.vengestudios.sortme.game;

import com.vengestudios.sortme.helpers.logic.Ordinal;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OwnPositionDisplay implements GameElement {

    // UI and animation constants
    private static final float LABEL_FONT_SIZE                          = 12.f;
    private static final float LABEL_SCREEN_X_PERCENTAGE_FROM_RIGHT     = 0.035f;
    private static final float LABEL_SCREEN_Y_PERCENTAGE_FROM_TOP       = 0.11f+0.01f;
    private static final float LABEL_SCREEN_WIDTH_PERCENTAGE            = 0.15f;
    private static final int   LABEL_COLOR = Color.rgb(100, 100, 100);

    private static final float POSITION_FONT_SIZE                       = 31.f;
    private static final float POSITION_SCREEN_X_PERCENTAGE_FROM_RIGHT  = 0.082f;
    private static final float POSITION_SCREEN_Y_PERCENTAGE_FROM_TOP    = 0.16f+0.01f;
    private static final float POSITION_SCREEN_WIDTH_PERCENTAGE         = 0.1f;
    private static final int   POSITION_COLOR = Color.rgb(77, 77, 77);

    private static final float SUFFIX_FONT_SIZE                          = 15.f;
    private static final float SUFFIX_RIGHT_PADDING_FROM_POSITION        = 2.f;
    private static final float SUFFIX_TOP_PADDING_FROM_POSITION          = 1.f;
    private static final int   SUFFIX_COLOR = Color.rgb(55, 55, 55);

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout relativeLayout;
    @SuppressWarnings("unused")
    private Context        context;

    // UI Elements
    private TextView       labelTextView;
    private TextView       positionTextView;
    private TextView       suffixTextView;

    // The user's current position
    private int            ownPosition;

    /**
     * Constructor
     *
     * Initializes and positions the required UI elements
     * and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements
     * @param context        The context of the application (usually the MainActivity)
     */
    public OwnPositionDisplay(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context        = context;

        int screenHeight = ScreenDimensions.getHeight(context);
        int screenWidth  = ScreenDimensions.getWidth(context);

        float labelRightPadding    = screenWidth *LABEL_SCREEN_X_PERCENTAGE_FROM_RIGHT;
        float labelTopPadding      = screenHeight*LABEL_SCREEN_Y_PERCENTAGE_FROM_TOP;
        float labelWidth           = screenWidth *LABEL_SCREEN_WIDTH_PERCENTAGE;
        float positionRightPadding = screenWidth *POSITION_SCREEN_X_PERCENTAGE_FROM_RIGHT;
        float positionTopPadding   = screenHeight*POSITION_SCREEN_Y_PERCENTAGE_FROM_TOP;
        float positionWidth        = screenWidth *POSITION_SCREEN_WIDTH_PERCENTAGE;

        labelTextView = new TextView(context);
        labelTextView.setWidth((int)labelWidth);
        labelTextView.setTextSize(LABEL_FONT_SIZE);
        labelTextView.setText("YOU ARE:");
        labelTextView.setX(screenWidth-labelRightPadding-labelWidth);
        labelTextView.setY(labelTopPadding);
        labelTextView.setGravity(Gravity.RIGHT);
        labelTextView.setTextColor(LABEL_COLOR);
        relativeLayout.addView(labelTextView);

        Ordinal ordinal = new Ordinal(ownPosition, true);

        positionTextView = new TextView(context);
        positionTextView.setWidth((int)positionWidth);
        positionTextView.setTextSize(POSITION_FONT_SIZE);
        positionTextView.setText(ordinal.number);
        positionTextView.setX(screenWidth-positionRightPadding-positionWidth);
        positionTextView.setY(positionTopPadding);
        positionTextView.setGravity(Gravity.RIGHT);
        positionTextView.setTextColor(POSITION_COLOR);
        relativeLayout.addView(positionTextView);

        suffixTextView = new TextView(context);
        suffixTextView.setText(ordinal.suffix);
        suffixTextView.setTextSize(SUFFIX_FONT_SIZE);
        suffixTextView.setText(ordinal.suffix);
        suffixTextView.setX(screenWidth-positionRightPadding+SUFFIX_RIGHT_PADDING_FROM_POSITION);
        suffixTextView.setY(positionTopPadding+SUFFIX_TOP_PADDING_FROM_POSITION);
        suffixTextView.setTextColor(SUFFIX_COLOR);
        relativeLayout.addView(suffixTextView);

        hide();
    }

    @Override
    public void hide(){
        labelTextView   .setVisibility(View.INVISIBLE);
        positionTextView.setVisibility(View.INVISIBLE);
        suffixTextView  .setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideForGameEnd() {
        hide();
    }

    @Override
    public void setupAndAppearForGame(){
        setOwnPosition(0);
        labelTextView   .setVisibility(View.VISIBLE);
        positionTextView.setVisibility(View.VISIBLE);
        suffixTextView  .setVisibility(View.VISIBLE);
    }

    /**
     * Sets the current position of the user
     * @param ownPosition
     */
    public void setOwnPosition(int ownPosition) {
        if (ownPosition==this.ownPosition) return;
        this.ownPosition = ownPosition;
        Ordinal ordinal = new Ordinal(ownPosition, true);
        positionTextView.setText(ordinal.number);
        suffixTextView.setText(ordinal.suffix);
    }

    /**
     * @return The current position of the user
     */
    public int getOwnPosition(){
        return ownPosition;
    }

}
