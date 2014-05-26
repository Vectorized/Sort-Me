package com.vengestudios.sortme.game;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import com.vengestudios.sortme.R;
import com.vengestudios.sortme.helpers.logic.Ordinal;
import com.vengestudios.sortme.helpers.ui.ButtonBackgroundSetter;
import com.vengestudios.sortme.helpers.ui.Effects;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * An GameElement used to display the scores of all the participants
 * at the end of a game, ranking them in order of position from top to bottom.
 *
 * Responsible for:
 *
 *  - Displaying the scores of the participants
 *
 *  - Handling the logic for ending a game that GameTimer does not handle
 *
 * 	- Providing a button to return the user back to the MainScreen
 */
public class ScoreBoard implements GameElement, OnClickListener {

	// UI Constants
    private static final int    MAX_NUMBER_OF_PLAYERS = 4;
    private static final int    DROP_DOWN_DURATION    = 550;
    private static final int    ROLL_UP_DURATION      = 530;

	private static final int [] POSITION_COLORS = {Color.parseColor("#B53C2F"),
												   Color.parseColor("#C4A233"),
											  	   Color.parseColor("#4A9956"),
												   Color.parseColor("#3787C2") };

	private static final int    DARK_TEXT_COLOR   = Color.parseColor("#2C3E50");
	private static final String TITLE_TEXT        = "Results";
	private static final String BACK_BUTTON_TEXT  = "Back to Main Screen";

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
	private RelativeLayout  relativeLayout;
    private Context         context;

    // UI Elements and fields
    private TextView 	    titleTextViewSmall;
    private TextView        titleTextViewLarge;
    private ScoreBoardRow[] scoreBoardRows;
    private Button          backButton;

    private RelativeLayout  scoreBoardOuterContainer;
    private RelativeLayout  scoreBoardInnerContainer;

    private int             screenWidth;
    private int             screenHeight;

    // GameElements Dependencies
    private GameScreen      gameScreen;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context        The context of the application (usually MainActivity)
     */
    public ScoreBoard(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context        = context;

        screenWidth  = ScreenDimensions.getWidth(context);
        screenHeight = ScreenDimensions.getHeight(context);

        scoreBoardOuterContainer = new RelativeLayout(context);
        scoreBoardOuterContainer.setGravity(Gravity.CENTER);
        scoreBoardOuterContainer.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight));
        relativeLayout.addView(scoreBoardOuterContainer);


        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                  (Context.LAYOUT_INFLATER_SERVICE);

        scoreBoardInnerContainer = (RelativeLayout) inflater.inflate(R.layout.score_board, null);

        scoreBoardOuterContainer.addView(scoreBoardInnerContainer);

        scoreBoardInnerContainer.getLayoutParams().width = (int)(screenWidth*.5f);

        titleTextViewLarge = (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_game_title_large);
        titleTextViewLarge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleTextViewLarge.setTextColor(DARK_TEXT_COLOR);
        titleTextViewLarge.setText(TITLE_TEXT);

        titleTextViewSmall = (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_game_title_small);
        titleTextViewSmall.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleTextViewSmall.setTextColor(DARK_TEXT_COLOR);
        titleTextViewSmall.setText(TITLE_TEXT);

        scoreBoardRows = new ScoreBoardRow[MAX_NUMBER_OF_PLAYERS];
        scoreBoardRows[0] = new ScoreBoardRow(
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_player_name_1    ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_score_1          ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_1       ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_suffix_1));
        scoreBoardRows[1] = new ScoreBoardRow(
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_player_name_2    ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_score_2          ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_2       ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_suffix_2));
        scoreBoardRows[2] = new ScoreBoardRow(
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_player_name_3    ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_score_3          ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_3       ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_suffix_3));
        scoreBoardRows[3] = new ScoreBoardRow(
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_player_name_4    ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_score_4          ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_4       ),
                (TextView) scoreBoardInnerContainer.findViewById(R.id.score_board_position_suffix_4));

        backButton = (Button) scoreBoardInnerContainer.findViewById(R.id.score_board_back_button);
        prepareBackButton();

        hide();
    }

    /**
     * Initializes and formats the back button.
     *
     * The large amount of UI formatting code is used to shift the text
     * of the back button for the pressed state
     */
    private void prepareBackButton() {
    	float screenDensity = ScreenDimensions.getDensity(context);
    	final float distanceToMoveTextWhenPressed = 4.f*screenDensity;
    	final int normalTopPadding     = (int)(10.f*screenDensity);
    	final int pressedTopPadding    = (int)(10.f*screenDensity+distanceToMoveTextWhenPressed);
    	final int sidePadding          = (int)(25.f*screenDensity);
    	final int normalBottomPadding  = (int)(10.f*screenDensity);
    	final int pressedBottomPadding = (int)(10.f*screenDensity-distanceToMoveTextWhenPressed);

    	final Button normalBackButton = new Button(context);
    	normalBackButton.setText(BACK_BUTTON_TEXT);
    	normalBackButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    	normalBackButton.setTextColor(DARK_TEXT_COLOR);
    	normalBackButton.setBackgroundResource(R.drawable.tile);
    	normalBackButton.setPadding(sidePadding, normalTopPadding, sidePadding, normalBottomPadding);
        normalBackButton.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        normalBackButton.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        		MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        normalBackButton.layout(0, 0, normalBackButton.getMeasuredWidth(), normalBackButton.getMeasuredHeight());

    	Button pressedBackButton = new Button(context);
    	pressedBackButton.setText(BACK_BUTTON_TEXT);
    	pressedBackButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    	pressedBackButton.setTextColor(DARK_TEXT_COLOR);
    	pressedBackButton.setBackgroundResource(R.drawable.tile_pressed);
    	pressedBackButton.setPadding(sidePadding, pressedTopPadding, sidePadding, pressedBottomPadding);
    	pressedBackButton.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
    	pressedBackButton.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        		MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    	pressedBackButton.layout(0, 0, pressedBackButton.getMeasuredWidth(), pressedBackButton.getMeasuredHeight());

    	Bitmap normalBackButtonBitmap = Bitmap.createBitmap(
    			normalBackButton.getMeasuredWidth(), normalBackButton.getMeasuredHeight(), Config.ARGB_8888);
    	Canvas normalBackButtonCanvas = new Canvas(normalBackButtonBitmap);
    	normalBackButton.draw(normalBackButtonCanvas);

    	Bitmap pressedBackButtonBitmap = Bitmap.createBitmap(
    			pressedBackButton.getMeasuredWidth(), pressedBackButton.getMeasuredHeight(), Config.ARGB_8888);
    	Canvas pressedBackButtonCanvas = new Canvas(pressedBackButtonBitmap);
    	pressedBackButton.draw(pressedBackButtonCanvas);

    	backButton.setText("");
    	ButtonBackgroundSetter.setBackgroundDrawable(backButton,
    			ButtonBackgroundSetter.getStateListDrawables(
    					context, normalBackButtonBitmap, pressedBackButtonBitmap));
    	backButton.setWidth(pressedBackButton.getMeasuredWidth());
    	backButton.setHeight(pressedBackButton.getMeasuredHeight());

    	backButton.setOnClickListener(this);
    }

    /**
     * Registers the GameScreen
     * @param gameScreen
     */
    public void registerGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    /**
     * Registers an ArrayList of ParticipantData sorted in descending order
     * by their positions, so that it can be displayed
     * @param playerDatas
     */
    public void registerParticipantDatas(ArrayList<ParticipantData> playerDatas) {
        assert playerDatas.size() <= MAX_NUMBER_OF_PLAYERS;
        for (int i=0; i<playerDatas.size(); ++i) {
            ScoreBoardRow scoreBoardRow = scoreBoardRows[i];
            ParticipantData    playerData    = playerDatas.get(i);
            scoreBoardRow.show();
            scoreBoardRow.setParticipantName(playerData.getParticipantName());
            scoreBoardRow.setScore     (playerData.getScore());
            scoreBoardRow.setPosition  (playerData.getPosition());
        }
        for (int i=playerDatas.size(); i<MAX_NUMBER_OF_PLAYERS; ++i) {
            scoreBoardRows[i].hide();
        }
        if (playerDatas.size()>3) {
        	titleTextViewLarge.setVisibility(View.GONE);
        	titleTextViewSmall.setVisibility(View.VISIBLE);
        } else {
        	titleTextViewLarge.setVisibility(View.VISIBLE);
        	titleTextViewSmall.setVisibility(View.GONE);
        }
    }

    /**
     * A class used to manage the UI formatting for each row in the ScoreBoard
     */
    private static class ScoreBoardRow {
        private static final NumberFormat SCORE_FORMAT = NumberFormat.getNumberInstance(Locale.US);

        TextView playerNameTextView;
        TextView scoreTextView;
        TextView positionTextView;
        TextView positionSuffixTextView;

        public ScoreBoardRow(TextView playerNameTextView, TextView scoreTextView,
                TextView positionTextView, TextView positionSuffixTextView) {
            this.playerNameTextView     = playerNameTextView;
            this.scoreTextView          = scoreTextView;
            this.positionTextView       = positionTextView;
            this.positionSuffixTextView = positionSuffixTextView;
        }
        public void setParticipantName(String playerName) {
            playerNameTextView.setText(playerName);
        }
        public void setScore(int score) {
            scoreTextView.setText(SCORE_FORMAT.format(score));
        }
        public void setPosition(int position) {
            Ordinal ordinal = new Ordinal(position, true);
            positionTextView      .setText(ordinal.number);
            positionSuffixTextView.setText(ordinal.suffix);

            int positionColor = POSITION_COLORS[position];
            playerNameTextView     .setTextColor(positionColor);
            scoreTextView         .setTextColor(positionColor);
            positionTextView      .setTextColor(positionColor);
            positionSuffixTextView.setTextColor(positionColor);
        }
        public void hide() {
            playerNameTextView    .setVisibility(View.GONE);
            scoreTextView         .setVisibility(View.GONE);
            positionTextView      .setVisibility(View.GONE);
            positionSuffixTextView.setVisibility(View.GONE);
        }
        public void show() {
            playerNameTextView    .setVisibility(View.VISIBLE);
            scoreTextView         .setVisibility(View.VISIBLE);
            positionTextView      .setVisibility(View.VISIBLE);
            positionSuffixTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Casts the drop down effect on the ScoreBoard and shows it
     * on the GameScreen.
     *
     * Calls on gameSceen to disable the Disconnected Error
     * so that there would not be a pop-up dialog
     * showing a disconnection message when the room is disconnected
     */
    public void dropDownAndShowScoreBoard(){
        scoreBoardOuterContainer.bringToFront();
        scoreBoardOuterContainer.setVisibility(View.VISIBLE);
        scoreBoardOuterContainer.setX(0.f);
        scoreBoardOuterContainer.setY(0.f);
        Effects.castOvershotTranslateEffect(scoreBoardOuterContainer,
                0, -screenHeight,
                0, 0,
                2, DROP_DOWN_DURATION, false, false);

        gameScreen.disableDisconnectedErrorForGameEnd();
    }

    /**
     * Scrolls up and hide the ScoreBoard
     */
    public void scrollUpAndHideScoreBoard(){
        Effects.castAccelerateTranslateEffect(scoreBoardOuterContainer,
                0, 0,
                0, -screenHeight,
                2, ROLL_UP_DURATION, false, true);
    }

    /**
     * Calls the respective method for the press of a Button
     */
    @Override
    public void onClick(View v) {
        if (v==backButton) {
            gameScreen.hideForGameEnd();
        }
    }

    @Override
    public void hide() {
        scoreBoardOuterContainer.clearAnimation();
        scoreBoardOuterContainer.setVisibility(View.INVISIBLE);
    }
    @Override
    public void hideForGameEnd() {
        scrollUpAndHideScoreBoard();
    }
    @Override
    public void setupAndAppearForGame() {}


}
