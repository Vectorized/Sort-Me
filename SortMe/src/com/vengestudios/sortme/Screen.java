package com.vengestudios.sortme;

/**
 * A Screen represents a collection of UI elements that the user is
 * able to see and interact with.
 *
 *  - It provides methods for its UI elements contained within
 *    to communicate with the Main Activity.
 *
 *  - It allows the Main activity to hide or show the UI Elements
 *    altogether to
 *
 *  - It initializes and layouts its UI elements.
 *
 *  - It registers the UI elements with each other if needed, allowing
 *    the UI elements to communicate with each other.
 */
public interface Screen {

    /*
     * Hides the UI elements altogether
     */
    public void hide();

    /*
     * Show the UI elements altogether
     */
    public void show();
}
