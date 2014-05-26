package com.vengestudios.sortme;


import com.google.android.gms.common.SignInButton;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

/**
 * The screen that displays a sign in button for the user if he/she is not
 * signed in
 */
public class SignInScreen implements OnClickListener, Screen {

    // Dependencies to create UI Elements
    @SuppressWarnings("unused")
    private RelativeLayout relativeLayout;
    private MainActivity   mainActivity;

    // UI Elements
    private RelativeLayout signInScreenOuterContainer;
    private RelativeLayout signInScreenInnerContainer;

    private SignInButton         signInButton;

    /**
     * Constructor
     *
     * @param relativeLayout The relativeLayout to have the UI elements inserted into
     * @param mainActivity The mainActivity
     */
    public SignInScreen(RelativeLayout relativeLayout, MainActivity mainActivity) {
        this.relativeLayout = relativeLayout;
        this.mainActivity   = mainActivity;

        int screenHeight = ScreenDimensions.getHeight(mainActivity);
        int screenWidth  = ScreenDimensions.getWidth(mainActivity);

        signInScreenOuterContainer = new RelativeLayout(mainActivity);
        signInScreenOuterContainer.setGravity(Gravity.CENTER);
        signInScreenOuterContainer.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, screenHeight));
        relativeLayout.addView(signInScreenOuterContainer);

        LayoutInflater inflater = (LayoutInflater)mainActivity.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        signInScreenInnerContainer = (RelativeLayout) inflater.inflate(R.layout.sign_in_screen, null);
        signInScreenOuterContainer.addView(signInScreenInnerContainer);

        signInButton = (SignInButton) signInScreenInnerContainer.findViewById(R.id.sign_in_screen_sign_in_button);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void hide() {
        signInScreenOuterContainer.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        signInScreenOuterContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v==signInButton)
            mainActivity.signInButtonClicked();
    }

}
