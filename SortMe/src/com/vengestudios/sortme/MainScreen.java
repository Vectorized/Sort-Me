package com.vengestudios.sortme;

import com.vengestudios.sortme.helpers.ui.BitmapImporter;
import com.vengestudios.sortme.helpers.ui.ButtonBackgroundSetter;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;
import com.vengestudios.sortme.security.SecurityProtocolButtonAdder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * This is the Main Screen which greets the users when they are signed in
 *
 * It provides the UI for the buttons for the users to
 * start a quick game, view high scores, view achievements, view instructions,
 * view invitations and invite friends to game
 */
public class MainScreen implements OnClickListener, Screen, SecurityProtocolButtonAdder {

    // Some UI formatting constants
    private static final int LOGO_WIDTH                      = 300;
    private static final int LOGO_HEIGHT                     = 61;
    private static final int QUICK_GAME_BUTTOM_MARGIN_TOP    = 35;
    private static final int QUICK_GAME_BUTTON_WIDTH         = 310;
    private static final int QUICK_GAME_BUTTON_HEIGHT        = 57;
    private static final int QUICK_GAME_BUTTON_MARGIN_BOTTOM = 17;
    private static final int ACCESSORY_BUTTON_WIDTH          = 93;
    private static final int INSTRUCTIONS_BUTTON_WIDTH       = 43;
    private static final int INSTRUCTIONS_BUTTON_HEIGHT      = 86;
    private static final int LAYOUT_HEIGHT                   = 250;

    // Dependencies to create UI Elements
    private RelativeLayout relativeLayout;
    private MainActivity   mainActivity;

    // The UI Elements
    private RelativeLayout mainScreenContainer;

    private ImageView      logo;
    private Button         quickGameButton;
    private Button         seeInvitationsButton;
    private Button         inviteFriendsButton;
    private Button         achivementsButton;
    private Button         leaderboardsButton;
    private Button         instructionsButton;

    // UI Formatting fields
    private int            screenWidth;
    private int            screenHeight;
    private float          screenDensity;

    // SnapshotScreen as a dummy screen used for transitions
    private SnapshotScreen snapshotScreen;

    /**
     * Constructor
     *
     * @param relativeLayout The relativeLayout to have the UI elements inserted into
     * @param mainActivity   The mainActivity
     */
    public MainScreen(RelativeLayout relativeLayout, MainActivity mainActivity) {
        this.relativeLayout = relativeLayout;
        this.mainActivity   = mainActivity;

        screenHeight  = ScreenDimensions.getHeight(mainActivity);
        screenWidth   = ScreenDimensions.getWidth(mainActivity);
        screenDensity = ScreenDimensions.getDensity(mainActivity);

        mainScreenContainer = new RelativeLayout(mainActivity);
        mainScreenContainer.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight));
        relativeLayout.addView(mainScreenContainer);

        initLayout();

        snapshotScreen = new SnapshotScreen();
    }

    /**
     * Initializes and layouts the different UI components of the Main screen
     */
    private void initLayout(){
        Resources resources = mainActivity.getResources();

        float screenCenter = screenWidth*.5f;
        float currentY = (screenHeight-LAYOUT_HEIGHT*screenDensity)*.5f;

        int logoFinalWidth  = (int)(screenDensity*LOGO_WIDTH);
        int logoFinalHeight = (int)(screenDensity*LOGO_HEIGHT);
        logo = new ImageView(mainActivity);
        logo.setImageBitmap(
                BitmapImporter.decodeSampledBitmapFromResource(
                        resources, R.drawable.main_screen_logo,
                        logoFinalWidth, logoFinalHeight));
        logo.setLayoutParams(
                new RelativeLayout.LayoutParams(logoFinalWidth, logoFinalHeight));
        logo.setX(screenCenter-0.5f*logoFinalWidth);
        logo.setY(currentY);
        mainScreenContainer.addView(logo);


        currentY += logoFinalHeight+screenDensity*QUICK_GAME_BUTTOM_MARGIN_TOP;

        int quickGameButtonFinalWidth  = (int)(screenDensity*QUICK_GAME_BUTTON_WIDTH);
        int quickGameButtonFinalHeight = (int)(screenDensity*QUICK_GAME_BUTTON_HEIGHT);

        quickGameButton = new Button(mainActivity);
        ButtonBackgroundSetter.setBackgroundDrawable(quickGameButton,
                getStateListDrawables(resources,
                        R.drawable.main_screen_button_quick_game,
                        R.drawable.main_screen_button_quick_game_pressed,
                        quickGameButtonFinalWidth, quickGameButtonFinalHeight));
        quickGameButton.setX(screenCenter-.5f*quickGameButtonFinalWidth);
        quickGameButton.setY(currentY);
        quickGameButton.setLayoutParams(
                new RelativeLayout.LayoutParams(quickGameButtonFinalWidth, quickGameButtonFinalHeight));
        quickGameButton.setOnClickListener(this);
        mainScreenContainer.addView(quickGameButton);



        currentY += quickGameButtonFinalHeight+screenDensity*QUICK_GAME_BUTTON_MARGIN_BOTTOM;

        int accessoryButtonFinalWidth = (int)(ACCESSORY_BUTTON_WIDTH*screenDensity);
        RelativeLayout.LayoutParams accessoryButtonLayoutParams =
                new RelativeLayout.LayoutParams(accessoryButtonFinalWidth,
                                                accessoryButtonFinalWidth);

        inviteFriendsButton = new Button(mainActivity);
        ButtonBackgroundSetter.setBackgroundDrawable(inviteFriendsButton,
                getStateListDrawables(resources,
                        R.drawable.main_screen_button_friends,
                        R.drawable.main_screen_button_friends_pressed,
                        accessoryButtonFinalWidth, accessoryButtonFinalWidth));
        inviteFriendsButton.setX(screenCenter-accessoryButtonFinalWidth);
        inviteFriendsButton.setY(currentY);
        inviteFriendsButton.setLayoutParams(accessoryButtonLayoutParams);
        inviteFriendsButton.setOnClickListener(this);
        mainScreenContainer.addView(inviteFriendsButton);

        seeInvitationsButton = new Button(mainActivity);
        ButtonBackgroundSetter.setBackgroundDrawable(seeInvitationsButton,
                getStateListDrawables(resources,
                        R.drawable.main_screen_button_invites,
                        R.drawable.main_screen_button_invites_pressed,
                        accessoryButtonFinalWidth, accessoryButtonFinalWidth));
        seeInvitationsButton.setX(screenCenter-accessoryButtonFinalWidth*2);
        seeInvitationsButton.setY(currentY);
        seeInvitationsButton.setLayoutParams(accessoryButtonLayoutParams);
        seeInvitationsButton.setOnClickListener(this);
        mainScreenContainer.addView(seeInvitationsButton);

        leaderboardsButton = new Button(mainActivity);
        ButtonBackgroundSetter.setBackgroundDrawable(leaderboardsButton,
                getStateListDrawables(resources,
                        R.drawable.main_screen_button_leaderboards,
                        R.drawable.main_screen_button_leaderboards_pressed,
                        accessoryButtonFinalWidth, accessoryButtonFinalWidth));
        leaderboardsButton.setX(screenCenter);
        leaderboardsButton.setY(currentY);
        leaderboardsButton.setLayoutParams(accessoryButtonLayoutParams);
        leaderboardsButton.setOnClickListener(this);
        mainScreenContainer.addView(leaderboardsButton);

        achivementsButton = new Button(mainActivity);
        ButtonBackgroundSetter.setBackgroundDrawable(achivementsButton,
                getStateListDrawables(resources,
                        R.drawable.main_screen_button_achievements,
                        R.drawable.main_screen_button_achievements_pressed,
                        accessoryButtonFinalWidth, accessoryButtonFinalWidth));
        achivementsButton.setX(screenCenter+accessoryButtonFinalWidth);
        achivementsButton.setY(currentY);
        achivementsButton.setLayoutParams(accessoryButtonLayoutParams);
        achivementsButton.setOnClickListener(this);
        mainScreenContainer.addView(achivementsButton);

        int instructionsButtonFinalWidth  = (int)(screenDensity*INSTRUCTIONS_BUTTON_WIDTH);
        int instructionsButtonFinalHeight = (int)(screenDensity*INSTRUCTIONS_BUTTON_HEIGHT);
        instructionsButton = new Button(mainActivity);
        instructionsButton.setX(screenWidth-instructionsButtonFinalWidth);
        instructionsButton.setY(0);
        instructionsButton.setBackgroundResource(R.drawable.instructions_button);
        instructionsButton.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        instructionsButtonFinalWidth,
                        instructionsButtonFinalHeight));
        instructionsButton.setOnClickListener(this);
        mainScreenContainer.addView(instructionsButton);

    }

    private StateListDrawable getStateListDrawables(Resources resources, int normalStateResourceId,
            int pressedStateResourceId, int width, int height) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[] {android.R.attr.state_pressed},
                new BitmapDrawable(resources, BitmapImporter.decodeSampledBitmapFromResource(
                        mainActivity.getResources(), pressedStateResourceId,
                        width, height)));
        stateListDrawable.addState(new int[] { },
                new BitmapDrawable(resources, BitmapImporter.decodeSampledBitmapFromResource(
                        mainActivity.getResources(), normalStateResourceId,
                        width, height)));
        return stateListDrawable;
    }

    @Override
    public void hide() {
        mainScreenContainer.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        mainScreenContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Calls the respective method for the press of a Button
     */
    @Override
    public void onClick(View v) {
        if (v==quickGameButton)
            mainActivity.quickGameButtonClicked();
        else if (v==seeInvitationsButton)
            mainActivity.seeInvitationsButtonClicked();
        else if (v==inviteFriendsButton)
            mainActivity.inviteFriendsButtonClicked();
        else if (v==achivementsButton)
            mainActivity.achievementsButtonClicked();
        else if (v==leaderboardsButton)
            mainActivity.leaderboardsButtonClicked();
        else if (v==instructionsButton)
            mainActivity.instructionsButtonClicked();
    }

    /**
     * @return A dummy Screen showing a snapshot of the MainScreen
     */
    public Screen getSnapshotScreen() {
        return snapshotScreen;
    }

    private class SnapshotScreen implements Screen {
        private ImageView snapshotImageView;
        public SnapshotScreen() {
            snapshotImageView = new ImageView(mainActivity);
            snapshotImageView.setLayoutParams(
                    new RelativeLayout.LayoutParams(screenWidth, screenHeight));
            relativeLayout.addView(snapshotImageView);
        }
        @Override
        public void hide() {
            snapshotImageView.setVisibility(View.GONE);
        }

        @Override
        public void show() {
            snapshotImageView.setImageBitmap(getScreenShotOfMainScreen());
            snapshotImageView.setVisibility(View.VISIBLE);
        }
        private Bitmap getScreenShotOfMainScreen() {
            Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mainScreenContainer.draw(canvas);
            return bitmap;
        }
    }

    @Override
    public void addProtocolChooserButton(Button button) {
        mainScreenContainer.addView(button);
    }

}
