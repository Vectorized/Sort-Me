package com.vengestudios.sortme.game;

/**
 * An interface that the game elements need to use
 */
public interface GameElement {

    /**
     * Hide itself from the screen.
     */
    public void hide();

    /**
     * Hide itself from the screen using transitions.
     * Called instead of hide for the game's end.
     */
    public void hideForGameEnd();

    /**
     * Resets the states of all inner fields to
     * prepare for a new game
     * and appear on the screen.
     */
    public void setupAndAppearForGame();
}
