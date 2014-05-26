package com.vengestudios.sortme;

import com.vengestudios.sortme.game.PowerupType;
import com.vengestudios.sortme.helpers.ui.BitmapImporter;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * The Activity for the instructions.
 * The code here is for the setup of the UI elements to display the instructions.
 */
@SuppressWarnings("unused")
public class InstructionsActivity extends Activity {

    // Some UI formatting constants
    private static final int   TEXT_PADDING_LEFT            = 12;
    private static final int   TEXT_PADDING_RIGHT           = 16;
    private static final int   STARTING_TEXT_TOP_PADDING    = 16;
    private static final int   TEXT_PADDING_VERTICAL        = 6;
    private static final float NORMAL_TEXT_FONT_SIZE        = 16.f;
    private static final int   BOTTOM_SPACING_HEIGHT        = 15;

    private static final float POWERUP_INFO_TITLE_FONT_SIZE         = 25.f;
    private static final float POWERUP_ICON_SCREEN_WIDTH_PERCENTAGE = .1f;
    private static final float POWERUP_INFO_SCREEN_WIDTH_PERCENTAGE = .75f;
    private static final float POWERUP_INFO_VERTICAL_PADDING        = -5.f;
    private static final int   POWERUP_TITLE_TEXT_COLOR = Color.parseColor("#777777");

    // The 
    private LinearLayout   instructionsLayout;

    private int            screenWidth;
    private int            screenHeight;
    private float          screenDensity;

    private int            textFinalPaddingLeft;
    private int            textFinalPaddingRight;
    private int            textFinalPaddingVertical;

    /**
     * Gets called when this activity is being made for the 1st time
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        setupActionBar();

        // Gets a reference to the LinearLayout defined in res/layout/activity_instructions.xml
        instructionsLayout = (LinearLayout) findViewById(R.id.instructions_layout);

        // Initialize and position all the UI elements used in the instructions
        initLayout();
    }

    /**
     * Initializes and positions all the UI elements used in the instructions
     */
    private void initLayout() {
        LinearLayout.LayoutParams normalTextLayoutParams =
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        screenWidth   = ScreenDimensions.getWidth(this);
        screenHeight  = ScreenDimensions.getHeight(this);
        screenDensity = ScreenDimensions.getDensity(this);

        textFinalPaddingLeft     = (int)(TEXT_PADDING_LEFT*screenDensity);
        textFinalPaddingRight    = (int)(TEXT_PADDING_RIGHT*screenDensity);
        textFinalPaddingVertical = (int)(TEXT_PADDING_VERTICAL*screenDensity);

        TextView introTextView1 = new TextView(this);
        introTextView1.setText(getString(R.string.instructions_intro_1));
        introTextView1.setLayoutParams(normalTextLayoutParams);
        introTextView1.setTextSize(NORMAL_TEXT_FONT_SIZE);
        introTextView1.setPadding(textFinalPaddingLeft,    (int)(STARTING_TEXT_TOP_PADDING*screenDensity),
                textFinalPaddingRight, textFinalPaddingVertical);
        instructionsLayout.addView(introTextView1);

        TextView introTextView2 = new TextView(this);
        introTextView2.setText(getString(R.string.instructions_intro_2));
        introTextView2.setLayoutParams(normalTextLayoutParams);
        introTextView2.setTextSize(NORMAL_TEXT_FONT_SIZE);
        introTextView2.setPadding(textFinalPaddingLeft,    textFinalPaddingVertical,
                textFinalPaddingRight, textFinalPaddingVertical*3);
        instructionsLayout.addView(introTextView2);

        float swapTileImageScreenWidthPercentage = 0.7f;
        int swapTileWidth  = (int)(screenWidth*swapTileImageScreenWidthPercentage);
        int swapTileHeight = (int)(swapTileWidth*322.f/1024.f);
        ImageView swapTileImageView = new ImageView(this);
        swapTileImageView.setImageBitmap(
                BitmapImporter.decodeSampledBitmapFromResource(
                        getResources(), R.drawable.swap_pic, swapTileWidth, swapTileHeight));
        instructionsLayout.addView(swapTileImageView);
        swapTileImageView.setLayoutParams(new LinearLayout.LayoutParams(swapTileWidth, swapTileHeight));

        TextView powerupText1 = new TextView(this);
        powerupText1.setText(getString(R.string.instructions_powerup_text_1));
        powerupText1.setLayoutParams(normalTextLayoutParams);
        powerupText1.setTextSize(NORMAL_TEXT_FONT_SIZE);
        powerupText1.setPadding(textFinalPaddingLeft, textFinalPaddingVertical,
                textFinalPaddingRight, textFinalPaddingVertical*2);
        instructionsLayout.addView(powerupText1);

        LinearLayout randomizeLayout = getPowerupLayout(PowerupType.RANDOMIZE.name,
                R.drawable.powerup_button_randomize, R.string.instructions_randomize_text);
        instructionsLayout.addView(randomizeLayout);

        LinearLayout bubbletizeLayout = getPowerupLayout(PowerupType.BUBBLETIZE.name,
                R.drawable.powerup_button_bubbletize, R.string.instructions_bubbletize_text);
        instructionsLayout.addView(bubbletizeLayout);

        LinearLayout upsizeLayout =    getPowerupLayout(PowerupType.UPSIZE.name,
                R.drawable.powerup_button_upsize, R.string.instructions_upize_text);
        instructionsLayout.addView(upsizeLayout);

        LinearLayout shieldLayout =    getPowerupLayout(PowerupType.SHIELD.name,
                R.drawable.powerup_button_shield, R.string.instructions_shield_text);
        instructionsLayout.addView(shieldLayout);

        TextView powerupText2 = new TextView(this);
        powerupText2.setText(getString(R.string.instructions_powerup_text_2));
        powerupText2.setLayoutParams(normalTextLayoutParams);
        powerupText2.setTextSize(NORMAL_TEXT_FONT_SIZE);
        powerupText2.setPadding(textFinalPaddingLeft, textFinalPaddingVertical*2,
                textFinalPaddingRight, textFinalPaddingVertical*2);
        instructionsLayout.addView(powerupText2);

        float targetedPicScaleFactor = 0.35f;
        int   targetedPicWidth  = (int)(202.f*targetedPicScaleFactor*screenDensity);
        int   targetedPicHeight = (int)(227.f*targetedPicScaleFactor*screenDensity);
        ImageView targetPicImageView = new ImageView(this);
        targetPicImageView.setImageBitmap(
                BitmapImporter.decodeSampledBitmapFromResource(
                        getResources(), R.drawable.targeted_pic_blended, targetedPicWidth, targetedPicHeight));
        targetPicImageView.setLayoutParams(new LinearLayout.LayoutParams(targetedPicWidth, targetedPicHeight));
        instructionsLayout.addView(targetPicImageView);


        TextView powerupText3 = new TextView(this);
        powerupText3.setText(getString(R.string.instructions_powerup_text_3));
        powerupText3.setLayoutParams(normalTextLayoutParams);
        powerupText3.setTextSize(NORMAL_TEXT_FONT_SIZE);
        powerupText3.setPadding(textFinalPaddingLeft, textFinalPaddingVertical/2,
                textFinalPaddingRight, textFinalPaddingVertical);
        instructionsLayout.addView(powerupText3);

        int bottomSpacingFinalHeight = (int)(BOTTOM_SPACING_HEIGHT*screenDensity);
        View bottomSpacingView = new View(this);
        bottomSpacingView.setLayoutParams(new LinearLayout.LayoutParams(10, bottomSpacingFinalHeight));
        instructionsLayout.addView(bottomSpacingView);

    }

    /**
     * Used to create a LinearLayout that contains the UI elements used for
     * the description of each PowerUp
     */
    private LinearLayout getPowerupLayout(
            String powerupTitle,
            int powerupIconResourceId, int powerupInfoDescriptionResourceId) {

        int powerupIconWidth         = (int)(POWERUP_ICON_SCREEN_WIDTH_PERCENTAGE*screenWidth);
        int powerupInfoWidth         = (int)(POWERUP_INFO_SCREEN_WIDTH_PERCENTAGE*screenWidth);
        int powerupInfoFinalVerticalPadding
            = (int)(POWERUP_INFO_VERTICAL_PADDING*screenDensity);

        LinearLayout.LayoutParams powerupLayoutParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams powerupIconLayoutParams =
                new LinearLayout.LayoutParams(powerupIconWidth, powerupIconWidth);
        LinearLayout.LayoutParams powerupInfoLayoutParams =
                new LinearLayout.LayoutParams(powerupInfoWidth, LayoutParams.WRAP_CONTENT);

        LinearLayout powerupLayout = new LinearLayout(this);
        powerupLayout.setLayoutParams(powerupLayoutParams);
        powerupLayout.setPadding(0, textFinalPaddingVertical, 0, textFinalPaddingVertical);
        ImageView powerupIcon = new ImageView(this);
        powerupIcon.setImageBitmap(
                BitmapImporter.decodeSampledBitmapFromResource(
                        getResources(), powerupIconResourceId,
                        powerupIconWidth, powerupIconWidth));
        powerupLayout.addView(powerupIcon);
        powerupIcon.setLayoutParams(powerupIconLayoutParams);

        LinearLayout powerupInfoLayout = new LinearLayout(this);
        powerupInfoLayout.setOrientation(LinearLayout.VERTICAL);
        powerupInfoLayout.setLayoutParams(powerupInfoLayoutParams);
        powerupLayout.addView(powerupInfoLayout);
        powerupInfoLayout.setPadding(textFinalPaddingLeft, powerupInfoFinalVerticalPadding, 0, 0);

        TextView powerupInfoTitle = new TextView(this);
        powerupInfoTitle.setTextSize(POWERUP_INFO_TITLE_FONT_SIZE);
        powerupInfoTitle.setTextColor(POWERUP_TITLE_TEXT_COLOR);
        powerupInfoTitle.setText(powerupTitle);
        powerupInfoLayout.addView(powerupInfoTitle);

        TextView powerupInfoDescription = new TextView(this);
        powerupInfoDescription.setTextSize(NORMAL_TEXT_FONT_SIZE);
        powerupInfoDescription.setText(getString(powerupInfoDescriptionResourceId));
        powerupInfoLayout.addView(powerupInfoDescription);
        powerupInfoDescription.setLayoutParams(powerupInfoLayoutParams);
        powerupInfoDescription.setPadding(0, 0, textFinalPaddingRight, 0);
        return powerupLayout;
    }

    // The following functions are for setting up the action bar

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.instructions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
