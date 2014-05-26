package com.vengestudios.sortme;

import java.util.HashSet;

/**
 * An interface that defines the common methods used to broadcast outgoing
 * messages.
 *
 * Used to standardize the creation of message sending "layers" for the
 * application, where each layer implements the MessageSender interface.
 */
public interface MessageSender {

    /**
     * Broadcasts a reliable message to a participant in the current room.
     * A checking mechanism is being used in the background by Google Game Services
     * to ensure that the reliable message will be delivered to all participants.
     * However, it has higher data overheads than an unreliable message.
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     */
    public void broadcastReliableMessageToId(byte[] message, String toId);

    /**
     * Broadcasts an unreliable message to a participant in the current room.
     * An unreliable message has lower data overheads, but there is no checking
     * mechanism to ensure that it will be delivered successfully
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     */
    public void broadcastUnreliableMessageToId(byte[] message, String toId);

    /**
     * Broadcast a message to a participant in the current room
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     * @param reliable    Whether the message needs to be reliably broadcasted
     */
    public void broadcastMessageToId(byte[] message, String toId, boolean reliable);

    /**
     * Broadcasts a reliable message to all the participants in the current room.
     * A checking mechanism is being used in the background by Google Game Services
     * to ensure that the reliable message will be delivered to all participants.
     * However, it has higher data overheads than an unreliable message.
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     */
    public void broadcastReliableMessageToAll(byte[] message,
            HashSet<String> excludedIds);

    /**
     * Broadcasts an unreliable message to all the participants in the current room.
     * An unreliable message has lower data overheads, but there is no checking
     * mechanism to ensure that it will be delivered successfully
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     */
    public void broadcastUnreliableMessageToAll(byte[] message,
            HashSet<String> excludedIds);

    /**
     * Broadcast a message to all participants in the current room
     *
     * @param message     The message needed to be broadcasted
     * @param excludedIds The participant IDs to exclude from this broadcast
     * @param reliable    Whether the message needs to be reliably broadcasted
     */
    public void broadcastMessageToAll(byte[] message,
            HashSet<String> excludedIds, boolean reliable);
}
