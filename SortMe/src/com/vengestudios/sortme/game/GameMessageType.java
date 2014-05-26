package com.vengestudios.sortme.game;


import android.util.Log;

/**
 * An enum to denote the type of game message.
 * It also provides the methods that define the grammar of the different
 * types of game messages, and helps to compose them.
 */
public enum GameMessageType {
    PERSONAL_ATTACK,
    PERSONAL_ATTACK_SUCCEEDED,
    PERSONAL_ATTACK_BLOCKED,
    OTHERS_TO_OTHERS_ATTACK,
    OTHERS_TO_OTHERS_BLOCK,
    UPDATE_PARTICIPANT_INFO,
    UPDATE_SCORE_AND_LINES_SORTED;

    // A String denoting a null field
    public static final String NULL_STRING = "NULL";

    // A string used as a delimiter. Should be something outside the common ASCII range
    public static final String DELIMITER_STRING = Character.toString((char)2);

    // A string used to uniquely identify a message as a game message
    // Useful if the game is to be extended and includes messages of other types
    public static final String IDENTIFIER_LABEL_STRING = "89021e";

    /**
     * Returns the GameMessageType whose ordinal matches the value
     * @param i  The value of the ordinal
     * @return   A GameMessageType
     */
    public static GameMessageType ordinalToMessageType(int i) {
        for (GameMessageType messageType:values()) {
            if (messageType.ordinal()==i)
                return messageType;
        }
        return null;
    }

    /**
     * Returns the GameMessageType whose ordinal is represented by the String
     * @param string  A String representing the ordinal
     * @return        A GameMessageType
     */
    public static GameMessageType ordinalToMessageType(String string) {
        return ordinalToMessageType(Integer.parseInt(string));
    }

    /**
     * Gets a game message that has been appended with the Identifier Label String
     * @param string  A String representing the game message
     * @return        The corresponding labeled game message
     */
    private static byte[] getLabeledMessage(String string) {
        return (IDENTIFIER_LABEL_STRING+DELIMITER_STRING+string).getBytes();
    }

    /**
     * Gets the fields of the game message if it has the Identifier Label String
     * @param message  The game message
     * @return The fields of the game message as an array of Strings if the message
     *         has been successfully verified to have the Identifier Label String
     *         else null.
     */
    public static String[] identifyLabelAndGetDelimitedStrings(byte[] message) {
        String[] delimitedStrings = new String(message).split(DELIMITER_STRING);
        Log.e("FIRST TOKEN", delimitedStrings[0]);

        if (delimitedStrings[0].equals(IDENTIFIER_LABEL_STRING)) {
            String [] returnDelimitedStrings = new String[delimitedStrings.length-1];
            for (int i=0; i<delimitedStrings.length-1; ++i) {
                String string = delimitedStrings[i+1];
                if (string.equals(NULL_STRING))
                    returnDelimitedStrings[i] = null;
                else
                    returnDelimitedStrings[i] = delimitedStrings[i+1];
            }
            return returnDelimitedStrings;
        }
        return null;
    }

    /**
     * Compose and returns the game message representing an attack
     * targeted at another participant for the PowerupType
     * @param powerupType  The PowerupType
     * @return             The corresponding game message
     */
    public static byte[] getPersonalAttackMessage(PowerupType powerupType) {
        assert (powerupType.isOffensive());
        return getLabeledMessage(
                PERSONAL_ATTACK.ordinal()
                +DELIMITER_STRING
                +powerupType.ordinal());
    }

    /**
     * Compose and returns the game message representing a reply to the initiator
     * of the attack, signifying that the attack is successful
     * @param powerupType  The PowerupType
     * @return             The corresponding game message
     */
    public static byte[] getSelfToOthersAttackMessage(PowerupType powerupType) {
    	assert (powerupType.isOffensive());
    	return getLabeledMessage(
    			PERSONAL_ATTACK_SUCCEEDED.ordinal()
    			+DELIMITER_STRING
    			+powerupType.ordinal());
    }

    /**
     * Compose and returns the game message representing a reply to the initiator
     * of the attack, signifying that the attack is blocked
     * @param powerupType  The PowerupType
     * @return             The corresponding game message
     */
    public static byte[] getSelfToOtherBlockMessage(PowerupType powerupType) {
    	assert (powerupType.isOffensive());
    	return getLabeledMessage(
    			PERSONAL_ATTACK_BLOCKED.ordinal()
    			+DELIMITER_STRING
    			+powerupType.ordinal());
    }

    /**
     * Compose and returns the game message representing an announcement that
     * own self has been successfully attacked by another participant
     * @param initiatorParticipantName The name of the participant who started the attack
     * @param powerupType  The PowerupType
     * @return             The corresponding game message
     */
    public static byte[] getOtherToOtherAttackMessage(String initiatorParticipantName, PowerupType powerupType) {
        assert (powerupType.isOffensive());
        return getLabeledMessage(
                OTHERS_TO_OTHERS_ATTACK.ordinal()
                +DELIMITER_STRING
                +powerupType.ordinal()
                +DELIMITER_STRING
                +initiatorParticipantName);
    }

    /**
     * Compose and returns the game message representing an announcement that
     * own self has blocked an attack from another participant
     * @param initiatorParticipantName The name of the participant who started the attack
     * @param powerupType  The PowerupType
     * @return             The corresponding game message
     */
    public static byte[] getOtherToOtherBlockMessage(String initiatorParticipantName, PowerupType powerupType) {
        assert (powerupType.isOffensive());
        return getLabeledMessage(
                OTHERS_TO_OTHERS_BLOCK.ordinal()
                +DELIMITER_STRING
                +powerupType.ordinal()
                +DELIMITER_STRING
                +initiatorParticipantName);
    }

    /**
     * Compose and returns the game message representing an announcement
     * of one's own participant name and image URL
     * @param participantName           The Participant's Name
     * @param participantImageURIString The URL of the image
     * @return                          The corresponding game message
     */
    public static byte[] getUpdateParticipantInfoMessage(String participantName, String participantImageURIString) {
        return getLabeledMessage(
                UPDATE_PARTICIPANT_INFO.ordinal()
                +DELIMITER_STRING
                +((participantName==null)?NULL_STRING:participantName)
                +DELIMITER_STRING
                +((participantImageURIString==null)?NULL_STRING:participantImageURIString));
    }

    /**
     * Compose and returns the game message representing an announcement
     * of one's own latest score and lines sorted
     * @param score        The score
     * @param linesSorted  The number of lines sorted
     * @return             The corresponding game message
     */
    public static byte[] getUpdateScoreAndLinesSortedMessage(int score, int linesSorted) {
        return getLabeledMessage(
                UPDATE_SCORE_AND_LINES_SORTED.ordinal()
                +DELIMITER_STRING
                +score
                +DELIMITER_STRING
                +linesSorted);
    }
}
