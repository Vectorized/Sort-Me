package com.vengestudios.sortme.game;

import java.util.ArrayList;

import com.vengestudios.sortme.R;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Manages the different PowerupButtons
 */
public class PowerupButtonManager implements GameElement {

    // UI constants
    private static final float SCREEN_WIDTH_PERCENTAGE    = .80f;
    private static final float SCREEN_HEIGHT_PERCENTAGE   = .092f;
    private static final float SCREEN_Y_PERCENTAGE        = .469f;

    // Dependencies to create UI Elements
    private RelativeLayout relativeLayout;
    @SuppressWarnings("unused")
    private Context        context;

    // An ArrayList to store the PowerupButtons
    private ArrayList<PowerupButton> powerupButtons;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context        The context of the application (usually MainActivity)
     */
    public PowerupButtonManager(RelativeLayout relativeLayout, Context context) {
        this.relativeLayout = relativeLayout;
        this.context        = context;

        int   screenWidth       = ScreenDimensions.getWidth(context);
        int   screenHeight      = ScreenDimensions.getHeight(context);
        float layoutWidth       = screenWidth*SCREEN_WIDTH_PERCENTAGE;
        float buttonSizeFloat   = screenWidth*SCREEN_HEIGHT_PERCENTAGE;
        int   buttonSizeInt     = (int)buttonSizeFloat;
        float layoutLeftPadding = .5f*(1.0f-SCREEN_WIDTH_PERCENTAGE)*screenWidth;
        float layoutTopPadding  = screenHeight*SCREEN_Y_PERCENTAGE;

        powerupButtons = new ArrayList<PowerupButton>();

        RandomizePowerupButton randomizePowerupButton =
                new RandomizePowerupButton(relativeLayout, context, buttonSizeInt);
        powerupButtons.add(randomizePowerupButton);

        BubbletizePowerupButton bubbletizePowerupButton =
                new BubbletizePowerupButton(relativeLayout, context, buttonSizeInt);
        powerupButtons.add(bubbletizePowerupButton);

        UpsizePowerupButton upsizePowerupButton =
                new UpsizePowerupButton(relativeLayout, context, buttonSizeInt);
        powerupButtons.add(upsizePowerupButton);

        ShieldPowerupButton shieldPowerupButton =
                new ShieldPowerupButton(relativeLayout, context, buttonSizeInt);
        powerupButtons.add(shieldPowerupButton);

        float buttonSpaceHori = layoutWidth/(powerupButtons.size());
        float buttonMarginHori = (buttonSpaceHori-buttonSizeFloat)/2.f;
        float i=0;
        for (PowerupButton powerupButton:powerupButtons) {
            this.relativeLayout.addView(powerupButton);
            powerupButton.setX(layoutLeftPadding+(i++)*buttonSpaceHori+buttonMarginHori);
            powerupButton.setY(layoutTopPadding);
            powerupButton.setEnabled(false);
            powerupButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setupAndAppearForGame(){
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.setVisibility(View.VISIBLE);
    }
    @Override
    public void hide(){
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.setVisibility(View.INVISIBLE);
    }
    @Override
    public void hideForGameEnd() {
        hide();
    }

    /**
     * Registers the MPBar with all the PowerupButtons
     * @param mpBar
     */
    public void registerMPBar(MPBar mpBar) {
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.registerMPBar(mpBar);
    }

    /**
     * Registers the PowerupActivator with all the PowerupButtons
     * @param powerupActivator
     */
    public void registerPowerupActivator(PowerupActivator powerupActivator) {
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.registerPowerupActivator(powerupActivator);
    }

    /**
     * Registers the PlayerCoordinator with all the PowerupButtons
     * @param playerCoordinator
     */
    public void registerParticipantCoordinator(ParticipantCoordinator playerCoordinator) {
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.registerPartipcipantCoordinator(playerCoordinator);
    }

    /**
     * Registers the MP level with all the PowerupButtons
     * enabling/disabling the PowerupButtons according to the MP level
     * @param mpAmount  The MP level
     */
    public void setEnabledForMP(float mpAmount) {
        for (PowerupButton powerupButton:powerupButtons)
            powerupButton.setEnabledForMP(mpAmount);
    }
}

/**
 * The PowerupButton for the Randomize PowwerUp
 */
class RandomizePowerupButton extends PowerupButton {
    public RandomizePowerupButton(RelativeLayout relativeLayout, Context context,
            int width) {
        super(relativeLayout, context, width);
    }
    @Override
    public void performPowerUp() {

    }
    @Override
    public float getMPCost() {
        return 200;
    }
    @Override
    public PowerupType getPowerupType() {
        return PowerupType.RANDOMIZE;
    }
    @Override
    public int getPowerupImageResource() {
        return R.drawable.powerup_button_randomize;
    }
}

/**
 * The PowerupButton for the Bubbletize PowwerUp
 */
class BubbletizePowerupButton extends PowerupButton {
    public BubbletizePowerupButton(RelativeLayout relativeLayout, Context context,
            int width) {
        super(relativeLayout, context, width);
    }
    @Override
    public void performPowerUp() {

    }
    @Override
    public float getMPCost() {
        return 200;
    }
    @Override
    public PowerupType getPowerupType() {
        return PowerupType.BUBBLETIZE;
    }
    @Override
    public int getPowerupImageResource() {
        return R.drawable.powerup_button_bubbletize;
    }
}

/**
 * The PowerupButton for the Upsize PowwerUp
 */
class UpsizePowerupButton extends PowerupButton {
    public UpsizePowerupButton(RelativeLayout relativeLayout, Context context,
            int width) {
        super(relativeLayout, context, width);
    }
    @Override
    public void performPowerUp() {

    }
    @Override
    public float getMPCost() {
        return 200;
    }
    @Override
    public PowerupType getPowerupType() {
        return PowerupType.UPSIZE;
    }
    @Override
    public int getPowerupImageResource() {
        return R.drawable.powerup_button_upsize;
    }
}

/**
 * The PowerupButton for the Shield PowwerUp
 */
class ShieldPowerupButton extends PowerupButton {
    public ShieldPowerupButton(RelativeLayout relativeLayout, Context context,
            int width) {
        super(relativeLayout, context, width);
    }
    @Override
    public void performPowerUp() {
        powerupActivator.shield();
    }
    @Override
    public float getMPCost() {
        return 200;
    }
    @Override
    public PowerupType getPowerupType() {
        return PowerupType.SHIELD;
    }
    @Override
    public int getPowerupImageResource() {
        return R.drawable.powerup_button_shield;
    }
}
