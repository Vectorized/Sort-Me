package com.vengestudios.sortme.security;

import android.widget.Button;

/**
 * An interface that a UI based class has to implement to
 * be able to allow the SecurityButtonChooser to add the
 * button to launch the dialog to select the user's choice of
 * security protocol
 */
public interface SecurityProtocolButtonAdder {

    /**
     * Adds the Button to launch the dialog for
     * choosing the user's choice of security protocol
     * @param button
     */
    public void addProtocolChooserButton(Button button);
}
