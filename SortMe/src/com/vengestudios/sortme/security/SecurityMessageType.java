package com.vengestudios.sortme.security;
import java.util.Arrays;

/**
 * An enum defining the different message types used in
 * the SecurityMessageLayer
 */
public enum SecurityMessageType {
    BALLOT         (0),
    AUTHENTICATION (1),
    NORMAL         (2);

    public int token;

    // Internal private constructor
    private SecurityMessageType(int token) {
        this.token = token;
    }

    /**
     * @param token An int representing the token of the SecurityMessageType
     * @return      The corresponding SecurityMessageType,
     *              else null if the token cannot be matched to a SecurityMessageType
     */
    public static SecurityMessageType getSecurityMessageTypeFromToken(int token) {
        for (SecurityMessageType securityMessageType:SecurityMessageType.values()) {
            if (securityMessageType.token==token)
                return securityMessageType;
        }
        return null;
    }

    /**
     * @param firstSenderBallot An int representing a ballot to be the 1st sender
     * @return                  A message labeled as a ballot message
     */
    public static byte[] getBallotMessage(int firstSenderBallot) {
        return SecurityHelper.appendToStart(
                (byte)BALLOT.token, SecurityHelper.intToFourBytes(firstSenderBallot));
    }

    /**
     * @param message A message labeled as a ballot message
     * @return        The int representing the ballot value
     */
    public static int getBallotMessageValue(byte[] message) {
        return SecurityHelper.fourBytesToInt(Arrays.copyOfRange(message, 1, message.length));
    }

    /**
     * @param securityProtocolType The choice of security protocol
     * @param message              The body of the message
     * @return                     A message labeled as an authentication message
     *                             and labeled with the corresponding token of the
     *                             security protocol
     */
    public static byte[] getAuthenticationMessage(SecurityProtocolType securityProtocolType, byte[] message) {
        byte[] messageHeader = {(byte)AUTHENTICATION.token, (byte)securityProtocolType.token};
        return SecurityHelper.concat(messageHeader, message);
    }

    /**
     * @param message A message labeled as an authentication message
     * @return        The body of the message
     */
    public static byte[] getAuthenticationMessageBody(byte[] message) {
        return Arrays.copyOfRange(message, 2, message.length);
    }

    /**
     * @param message A message labeled as an authentication message
     * @return        The security protocol used by the message
     */
    public static SecurityProtocolType getSecurityProtocolType(byte[] message) {
        return SecurityProtocolType.getSecurityMessageTypeFromToken((int)message[1]);
    }

    /**
     * @param message The body of the message
     * @return        A message labeled as an normal message
     */
    public static byte[] getNormalMessage(byte[] message) {
        return SecurityHelper.appendToStart((byte)NORMAL.token, message);
    }

    /**
     * @param message A message labeled as an normal message
     * @return        The body of the message
     */
    public static byte[] getNormalMessageBody(byte[] message) {
        return Arrays.copyOfRange(message, 1, message.length);
    }

}
