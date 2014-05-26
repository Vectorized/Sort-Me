package com.vengestudios.sortme;

/**
 * An interface that defines the common methods used to receive incoming
 * messages.
 *
 * Used to standardize the creation of message receiving "layers" for the
 * application, where each layer implements the MessageReceiver interface.
 */
public interface MessageReceiver {

    /**
     * Takes in a message received from another participant in the current room
     *
     * @param fromId  The ID of the participant
     * @param message The received message
     */
    public void registerMessage(String fromId, byte[] message);
}
