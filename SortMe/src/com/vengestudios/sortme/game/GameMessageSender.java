package com.vengestudios.sortme.game;

import java.util.HashSet;

import com.vengestudios.sortme.MessageSender;

/**
 * Used to compose a game message for the relevant game move
 * and sends it to the GameScreen
 */
public class GameMessageSender implements MessageSender {
    private GameScreen gameScreen;

    /**
     * Registers the GameScreen
     * @param gameScreen  The GameScreen
     */
    public void registerGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    /**
     * Compose and send a personal attack message to a participant for
     * a certain PowerUp
     * @param participantId  The ID of the participant to send to
     * @param powerupType    The type of PowerUp
     */
    public void sendPersonalAttack(String participantId, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        byte[] message = GameMessageType.getPersonalAttackMessage(powerupType);
        broadcastReliableMessageToId(message, participantId);
    }

    /**
     * Compose and send a message as a reply to a successfully hit PowerUp
     * @param initiatorId  The ID of the participant that started the attack
     * @param powerupType  The type of PowerUp
     */
    public void sendPersonalAttackSucceededReply(String initiatorId, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        byte[] message = GameMessageType.getSelfToOthersAttackMessage(powerupType);
        broadcastReliableMessageToId(message, initiatorId);
    }

    /**
     * Compose and send a message as a reply to a blocked PowerUp attack
     * @param initiatorId  The ID of the participant that started the attack
     * @param powerupType  The type of PowerUp
     */
    public void sendPersonalAttackBlockedReply(String initiatorId, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        byte[] message = GameMessageType.getSelfToOtherBlockMessage(powerupType);
        broadcastReliableMessageToId(message, initiatorId);
    }

    /**
     * Compose and send a message as an announcement that own self has been successfully
     * hit by a PowerUp
     * @param initiatorParticipantName The name of the participant who started the attack
     * @param excludedParticipantsIds  The IDs of the participants to exclude from this announcement
     * @param powerupType              The type of PowerUp
     */
    public void announceOtherToOthersAttack(String initiatorParticipantName,
            HashSet<String> excludedParticipantsIds, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        byte[] message = GameMessageType.getOtherToOtherAttackMessage(initiatorParticipantName, powerupType);
        broadcastReliableMessageToAll(message, excludedParticipantsIds);
    }

    /**
     * Compose ad send a message as an announcement that own self has blocked an attack
     * @param initiatorParticipantName The name of the participant who started the attack
     * @param excludedParticipantsIds  The IDs of the participants to exclude from this announcement
     * @param powerupType              The type of PowerUp
     */
    public void announceOthersToOthersBlock(String initiatorParticipantName,
            HashSet<String> excludedParticipantsIds, PowerupType powerupType) {
        if (powerupType.isDefensive()) return;
        byte[] message = GameMessageType.getOtherToOtherBlockMessage(initiatorParticipantName, powerupType);
        broadcastReliableMessageToAll(message, excludedParticipantsIds);
    }

    /**
     * Announce the name and image URL of own self to every other participant
     * @param ownParticipantName            Own participant name
     * @param ownParticipantImageURIString  Own image URL
     */
    public void announceOwnInfo(String ownParticipantName, String ownParticipantImageURIString) {
        byte[] message = GameMessageType.getUpdateParticipantInfoMessage(ownParticipantName,
                ownParticipantImageURIString);
        broadcastReliableMessageToAll(message, null);
    }

    /**
     * Announce the score and the number of lines sorted to every other participant
     * @param score       The score
     * @param linesSorted The number of lines sorted
     */
    public void announceScoreAndLinesSorted(int score, int linesSorted) {
        byte[] message = GameMessageType.getUpdateScoreAndLinesSortedMessage(score, linesSorted);
        gameScreen.broadcastReliableMessageToAll(message, null);
    }

    @Override
    public void broadcastReliableMessageToId(byte[] message, String toId) {
        gameScreen.broadcastReliableMessageToId(message, toId);
    }

    @Override
    public void broadcastUnreliableMessageToId(byte[] message, String toId) {
        gameScreen.broadcastUnreliableMessageToId(message, toId);
    }

    @Override
    public void broadcastMessageToId(byte[] message, String toId,
            boolean reliable) {
        gameScreen.broadcastMessageToId(message, toId, reliable);
    }

    @Override
    public void broadcastReliableMessageToAll(byte[] message,
            HashSet<String> excludedIds) {
        gameScreen.broadcastReliableMessageToAll(message, excludedIds);
    }

    @Override
    public void broadcastUnreliableMessageToAll(byte[] message,
            HashSet<String> excludedIds) {
        gameScreen.broadcastUnreliableMessageToAll(message, excludedIds);
    }

    @Override
    public void broadcastMessageToAll(byte[] message,
            HashSet<String> excludedIds, boolean reliable) {
        gameScreen.broadcastMessageToAll(message, excludedIds, reliable);
    }
}
