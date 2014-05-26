package com.vengestudios.sortme.game;

import com.vengestudios.sortme.MessageReceiver;

/**
 * A class to decompose received game message into its respective fields,
 * then pass the fields to the ParticipantCoordinator
 */
public class GameMessageReceiver implements MessageReceiver {

    private ParticipantCoordinator participantCoordinator;

    /**
     * Registers the ParticipantCoordinator
     * @param participantCoordinator
     */
    public void registerParticipantCoordinator(ParticipantCoordinator participantCoordinator) {
        this.participantCoordinator = participantCoordinator;
    }

    @Override
    public void registerMessage(String fromParticipantId, byte[] message) {
        String [] delimitedStrings = GameMessageType.identifyLabelAndGetDelimitedStrings(message);
        if (delimitedStrings!=null)
            processGameMessageDelimitedStrings(fromParticipantId, delimitedStrings);
    }
    /**
     * Takes in a the fields of the game message and passes it on to the relevant
     * method of ParticipantCoordinator
     *
     * @param fromParticipantId The id of the participant the message is from
     * @param delimitedStrings  The fields of the game message
     */
    private void processGameMessageDelimitedStrings(String fromParticipantId, String[] delimitedStrings) {
        assert participantCoordinator != null;
        GameMessageType gameMessageType = GameMessageType.ordinalToMessageType(delimitedStrings[0]);

        if (gameMessageType==GameMessageType.PERSONAL_ATTACK) {

            PowerupType powerupType = PowerupType.ordinalToPowerupType(delimitedStrings[1]);
            if (powerupType!=null)
                participantCoordinator.receivePersonalAttack(fromParticipantId, powerupType);

        } else if (gameMessageType==GameMessageType.PERSONAL_ATTACK_SUCCEEDED) {

            PowerupType powerupType = PowerupType.ordinalToPowerupType(delimitedStrings[1]);
            if (powerupType!=null)
                participantCoordinator.receivePersonalAttackSucceededReply(fromParticipantId, powerupType);

        } else if (gameMessageType==GameMessageType.PERSONAL_ATTACK_BLOCKED) {

            PowerupType powerupType = PowerupType.ordinalToPowerupType(delimitedStrings[1]);
            if (powerupType!=null)
                participantCoordinator.receivePersonalAttackBlockedReply(fromParticipantId, powerupType);

        } else if (gameMessageType==GameMessageType.OTHERS_TO_OTHERS_ATTACK) {

            PowerupType powerupType = PowerupType.ordinalToPowerupType(delimitedStrings[1]);
            if (powerupType!=null)
                participantCoordinator.announceOtherToOtherAttack(delimitedStrings[2], fromParticipantId, powerupType);

        } else if (gameMessageType==GameMessageType.OTHERS_TO_OTHERS_BLOCK) {

            PowerupType powerupType = PowerupType.ordinalToPowerupType(delimitedStrings[1]);
            if (powerupType!=null)
                participantCoordinator.announceOtherToOtherBlock(delimitedStrings[2], fromParticipantId, powerupType);

        } else if (gameMessageType==GameMessageType.UPDATE_PARTICIPANT_INFO) {

                if (delimitedStrings.length==3)
                    participantCoordinator.setParticipantInfo(fromParticipantId, delimitedStrings[1], delimitedStrings[2]);
                else if (delimitedStrings.length==2)
                    participantCoordinator.setParticipantInfo(fromParticipantId, delimitedStrings[1], null);

        } else if (gameMessageType==GameMessageType.UPDATE_SCORE_AND_LINES_SORTED) {

            participantCoordinator.setScoreAndLinesSorted(fromParticipantId,
                    Integer.parseInt(delimitedStrings[1]),
                    Integer.parseInt(delimitedStrings[2]));

        }
    }
}
