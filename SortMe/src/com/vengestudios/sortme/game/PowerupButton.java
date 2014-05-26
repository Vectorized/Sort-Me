package com.vengestudios.sortme.game;

import com.vengestudios.sortme.helpers.ui.BitmapImporter;
import com.vengestudios.sortme.helpers.ui.ButtonBackgroundSetter;
import com.vengestudios.sortme.helpers.ui.Effects;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * A Button that activates a PowerUp
 */
public abstract class PowerupButton extends Button implements OnClickListener {

    // UI and animation constants
    protected static final float DISABLED_ALPHA                 = .5f;
    protected static final float ENABLED_ALPHA                  = 1.f;
    protected static final float PRESSED_EFFECT_STARTING_ALPHA  = 1.f;
    protected static final float PRESSED_EFFECT_END_SCALE       = 2.0f;
    protected static final int   PRESSED_EFFECT_TIME            = 300;
    protected static final float BACKGROUND_CIRCLE_BORDER_WIDTH = 4.5f;
    protected static final int   INNER_PADDING                  = 13;

    // Dependencies to create UI Elements
    protected RelativeLayout         relativeLayout;
    protected Context                context;

    // GameElement Dependencies
    protected PowerupActivator       powerupActivator;
    protected ParticipantCoordinator participantCoordinator;
    protected MPBar                  mpBar;

    // Handler, ImageView and fields for the pressed animation effect
    protected Handler                pressedEffectHandler;
    protected ImageView              pressedEffectView;
    protected boolean                pressedEffectViewAdded;

    // A reference to this
    protected PowerupButton          selfReference;

    /**
     * Constructor
     *
     * Initializes and positions the UI Elements and adds them to the RelativeLayout
     *
     * @param relativeLayout The RelativeLayout to insert the UI Elements into
     * @param context        The context of the application (usually MainActivity)
     * @param width          The width of the PowerupButton
     */
    public PowerupButton(RelativeLayout relativeLayout, Context context, int width) {
        super(context);
        this.relativeLayout = relativeLayout;
        this.context        = context;

        pressedEffectView   = new ImageView(context);
        pressedEffectView.setVisibility(View.INVISIBLE);
        relativeLayout.addView(pressedEffectView);

        pressedEffectHandler = new Handler();
        setOnClickListener(this);


        setLayoutParams(new RelativeLayout.LayoutParams(width, width));
        ButtonBackgroundSetter.setBackgroundBitmap(
                this,
                context.getResources(),
                BitmapImporter.decodeSampledBitmapFromResource(
                        context.getResources(),
                        getPowerupImageResource(),
                        width, width));

        selfReference = this;
    }

    /**
     * Register the PowerupActivator
     * @param powerupActivator
     */
    public void registerPowerupActivator(PowerupActivator powerupActivator) {
        this.powerupActivator = powerupActivator;
    }

    /**
     * Register the ParticipantCoordinator
     * @param participantCoordinator
     */
    public void registerPartipcipantCoordinator(ParticipantCoordinator participantCoordinator) {
        this.participantCoordinator = participantCoordinator;
    }

    /**
     * Register the MPBar
     * @param mpBar
     */
    public void registerMPBar(MPBar mpBar) {
        this.mpBar = mpBar;
    }

    /**
     * Enables or Disables the PowerupButton for the specified MP level
     * depending on whether the is enough mp to cast the PowerUp
     * @param mpAmount  The MP level
     */
    public void setEnabledForMP(float mpAmount) {
        if (isEnabled()) {
            if (mpAmount<getMPCost())
                setEnabled(false);
        } else if (mpAmount>=getMPCost()) {
            setEnabled(true);
        }
    }

    public abstract PowerupType getPowerupType();
    public abstract float       getMPCost();
    public abstract void        performPowerUp();
    public abstract int         getPowerupImageResource();

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled?ENABLED_ALPHA:DISABLED_ALPHA);
    }

    @Override
    public void onClick(View v) {
        performPowerUp();
        assert participantCoordinator != null;

        participantCoordinator.setOwnUsedPowerupToTrue();

        if (getPowerupType().isOffensive())
            participantCoordinator.sendPersonalAttack(getPowerupType());

        pressedEffectHandler.postDelayed(new Runnable() {
            @Override public void run() {
                Effects.castExpandingAfterImageEffect(selfReference, pressedEffectView,
                        PRESSED_EFFECT_STARTING_ALPHA, PRESSED_EFFECT_END_SCALE,
                        PRESSED_EFFECT_END_SCALE, PRESSED_EFFECT_TIME);
            }
        }, 17);
        if (mpBar!=null)
            mpBar.decrementMP(getMPCost());
    }
}
