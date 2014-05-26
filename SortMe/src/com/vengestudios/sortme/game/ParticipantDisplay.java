package com.vengestudios.sortme.game;

import com.vengestudios.sortme.R;
import com.vengestudios.sortme.generaluielements.URLImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class is used to manage the UI Elements used to display
 * the image, name and score of a participant.
 *
 * It is also used to get and set the information and
 * game performance of the participant.
 */
@SuppressLint("ViewConstructor")
public class ParticipantDisplay extends LinearLayout {

	// UI constants
    private static final float PLAYER_IMAGE_VIEW_HEIGHT    = 48.f;
    private static final float PLAYER_NAME_FONT_SIZE       = 13.f;
    private static final int   TARGET_ARROW_WIDTH          = 16;
    private static final int   TARGET_ARROW_HEIGHT         = 7;
    private static final int   TARGET_ARROW_BOTTOM_PADDING = 7;
    private static final float DISCONNECTED_ALPHA          = 0.5f;
    private static final float CONNECTED_ALPHA             = 1.f;

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
	private Context      context;

    // UI Elements
    private URLImageView targetArrow;
    private URLImageView participantImageView;
    private TextView     participantNameTextView;
    private ScoreLabel   scoreLabel;

    // The ParticpantData holding the information
    // and game performance of the participant
    private ParticipantData   participantData;

    // Other fields used to manage the UI
    private boolean      connected;
    private boolean      targeted;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout        The RelativeLayout to insert the UI Elements into
     * @param context               The context of the application (usually MainActivity)
     * @param participantData       The ParticipantData of the participant
     * @param participantNameWidth  The width of the TextView displaying the participant's name
     */
    public ParticipantDisplay(RelativeLayout relativeLayout, Context context, ParticipantData participantData,
    		int participantNameWidth) {
        super(context);

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        this.participantData = participantData;

        targetArrow = new URLImageView(context, TARGET_ARROW_WIDTH, TARGET_ARROW_HEIGHT+TARGET_ARROW_BOTTOM_PADDING);
        targetArrow.setImageDrawable(context.getResources().getDrawable(R.drawable.target_arrow));
        targetArrow.setPadding(0, 0, 0, TARGET_ARROW_BOTTOM_PADDING);
        targetArrow.setVisibility(View.INVISIBLE);
        targetArrow.setAlpha(.5f);
        addView(targetArrow);

        participantImageView = new URLImageView(context, PLAYER_IMAGE_VIEW_HEIGHT, PLAYER_IMAGE_VIEW_HEIGHT);
        participantImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.unnamed));
        addView(participantImageView);

        participantNameTextView = new TextView(context);
        participantNameTextView.setTextSize(PLAYER_NAME_FONT_SIZE);
        participantNameTextView.setTextColor(Color.rgb(99,99,99));
        participantNameTextView.setText(participantData.getParticipantName());
        participantNameTextView.setY(2.f);
        participantNameTextView.setGravity(Gravity.CENTER);
        participantNameTextView.setWidth(participantNameWidth);
        participantNameTextView.setSingleLine(true);
        participantNameTextView.setEllipsize(TruncateAt.END);
        participantNameTextView.setPadding(5, 0, 5, 0);
        addView(participantNameTextView);

        scoreLabel = new ScoreLabel(relativeLayout, context);
        addView(scoreLabel);

        setConnected(true);
    }

    /**
     * Sets if the participant is connected to the current game
     * @param connected
     */
    public void setConnected(boolean connected){
        this.connected = connected;
        if (connected)
            setAlpha(CONNECTED_ALPHA);
        else
            setAlpha(DISCONNECTED_ALPHA);
    }

    /**
     * @return Whether the participant is connected to the current game
     */
    public boolean getConnected() {
        return connected;
    }

    /**
     * Sets the participant as targeted
     * @param targeted
     */
    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
        if (targeted)
            targetArrow.setVisibility(View.VISIBLE);
        else
            targetArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * @return Whether the participant is targeted
     */
    public boolean getTargeted(){
        return targeted;
    }

    // Getters and setters for the participantData

    public void incrementTimesBlockedByOthers(PowerupType powerupType) {
        participantData.incrementTimesBlockedByOthers(powerupType);
    }

    public void incrementTimesAttackedByOthers(PowerupType powerupType) {
        participantData.incrementTimesAttackedByOthers(powerupType);
    }

    public void incrementTimesBlockSuccessful(PowerupType powerupType) {
        participantData.incrementTimesBlockSuccessful(powerupType);
    }

    public void incrementTimesAttackSuccessful(PowerupType powerupType) {
        participantData.incrementTimesAttackSuccessful(powerupType);
    }

    public String getParticipantId() {
        return participantData.getParticipantId();
    }

    public String getParticipantName() {
        return participantData.getParticipantName();
    }

    public void setParticipantName(String participantName) {
        if (participantName==null) return;
        participantNameTextView.setText(participantName);
        participantData.setParticipantName(participantName);
    }

    public String getParticipantImageURL() {
        return participantData.getParticipantImageURL();
    }

    public void setParticipantImageURL(String participantImageURL) {
        if (participantImageURL==null) return;
        participantData.setParticipantImageURL(participantImageURL);
        participantImageView.loadImageFromURL(participantImageURL);
    }

    public int getPosition() {
        return participantData.getPosition();
    }

    public void setPosition(int position) {
        participantData.setPosition(position);
    }

    public int getScore() {
        return participantData.getScore();
    }

    public void setScore(int score) {
        participantData.setScore(score);
        scoreLabel.setScore(score);
    }

    public int getLinesSorted() {
        return participantData.getLinesSorted();
    }

    public void setLinesSorted(int linesSorted) {
        participantData.setLinesSorted(linesSorted);
    }

    public void incrementScoreAndLinesSorted(int score) {
        participantData.incrementScoreAndLinesSorted(score);
        scoreLabel.setScore(participantData.getScore());
    }

    public void setUsedPowerupToTrue() {
    	participantData.setUsedPowerupToTrue();
    }

    public boolean getUsedPowerup() {
    	return participantData.getUsedPowerup();
    }

    public void setScoreAndLinesSorted(int score, int linesSorted) {
        participantData.setScoreAndLinesSorted(score, linesSorted);
        scoreLabel.setScore(participantData.getScore());
    }

    public ParticipantData getParticipantDataClone() {
        return participantData.clone();
    }


}
