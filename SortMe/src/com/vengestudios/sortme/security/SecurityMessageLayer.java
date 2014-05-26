package com.vengestudios.sortme.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.vengestudios.sortme.MessageReceiver;
import com.vengestudios.sortme.MessageSender;

/**
 * The "layer" that acts as an intermediary between the
 * Google Game Services "layer" (MainActivity)
 * and the game logic layer (GameScreen)
 *
 * Responsible for:
 *
 *  - Providing a seamless and transparent way to integrate
 *    secure messaging between two "layers".
 *
 *  - Maintaining one or more SecureClientSenders to manage different
 *    client choices for security protocols and authenticate
 *    the user with different clients
 *
 *  - Decryption and verification of incoming messages
 *
 *  - Redirecting incoming messages to the
 *    > relevant SecureClientSender
 *      (for balloting and authentication messages)
 *    > MessageReceiver
 *      (for normal messages)
 *
 */
public class SecurityMessageLayer implements MessageReceiver, MessageSender {

	// The MessageReceiver is the "layer"
	// where all incoming normal messages need to be passed to
    private MessageReceiver messageReceiver;

	// The MessageSender is the "layer"
	// where all outgoing messages need to be passed to
    private MessageSender   messageSender;

    // A HashMap to manage and store the different SecureClientSenders
    private HashMap<String, SecureClientSender> secureClientSenders;

    // The OwnSecurtyData, which stores the user's security
    // credentials.
    // Provides encryption and decryption via user's
    // RSA private key and user's DES key
    private OwnSecurityData ownSecurityData;

    /**
     * Constructor
     *
     * Creates the SecurityMessageLayer
     *
     * @param messageSender
     * @param messageReceiver
     */
    public SecurityMessageLayer(MessageSender messageSender, MessageReceiver messageReceiver) throws Exception {
        this.messageSender   = messageSender;
        this.messageReceiver = messageReceiver;
        ownSecurityData      = new OwnSecurityData();
        secureClientSenders = new HashMap<String, SecureClientSender>();
    }

    /**
     * Prepare for a new mutual authentication session which
     * is later following by message sending between the different clients
     * and the user.
     *
     * Calls the OwnSecurityData to generate a new DES key and nonce
     * for "freshness" of messages to prevent replay attacks.
     */
    public void prepareForNextSession() {
        secureClientSenders.clear();
        try {
            ownSecurityData.regenerateForNewSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Register a List of client IDs for a new session,
     * setting up the required SecureClientSenders
     *
     * @param ids The List of client IDs
     */
    public void registerIdsForNewSession(List<String> ids) {
        for (String id:ids) {
            if (secureClientSenders.containsKey(id)==false) {

            	// The constructor will link up the SecureClientSender with the
            	// MessageSender that all outgoing messages will need to go to
                SecureClientSender secureClient = new SecureClientSender(id, messageSender, ownSecurityData);
                secureClientSenders.put(id, secureClient);
            }
        }
    }

    /**
     * @return The user's choice of SecurityProtocolType
     */
    public SecurityProtocolType getSecurityProtocolType() {
        return ownSecurityData.getSecurityProtocolType();
    }

    /**
     * Sets the user's choice of SecurityProtocolType
     * @param securityProtocolType
     */
    public void setSecurityProtocolType(SecurityProtocolType securityProtocolType) {
        ownSecurityData.setSecurityProtocolType(securityProtocolType);
    }

    /**
     * Passes the message to the relevant SecureClientSender to be broadcasted
     *
     * A reliable message implements the mechanisms to guarantee successful
     * delivery, but has slightly higher data overhead.
     */
    @Override
    public void broadcastReliableMessageToId(byte[] message, String toId) {
        for (SecureClientSender secureClientSender:secureClientSenders.values())
            if (secureClientSender.getId().equals(toId))
                secureClientSender.broadcastReliableMessage(message);
    }

    /**
     * Passes the message to the relevant SecureClientSender to be broadcasted
     *
     * An unreliable message has lower data overheads, but it does not
     * implement the mechanisms to guarantee successful delivery of
     * the message.
     */
    @Override
    public void broadcastUnreliableMessageToId(byte[] message, String toId) {
        for (SecureClientSender secureClientSender:secureClientSenders.values())
            if (secureClientSender.getId().equals(toId))
                secureClientSender.broadcastUnreliableMessage(message);
    }

    /**
     * Passes the message to the relevant SecureClientSender to be broadcasted
     */
    @Override
    public void broadcastMessageToId(byte[] message, String toId,
            boolean reliable) {
        if (reliable) broadcastReliableMessageToId  (message, toId);
        else          broadcastUnreliableMessageToId(message, toId);
    }

    /**
     * Passes the unreliable message to every SecureClientSender to be
     * broadcasted
     *
     * A reliable message implements the mechanisms to guarantee successful
     * delivery, but has slightly higher data overhead.
     */
    @Override
    public void broadcastReliableMessageToAll(byte[] message,
            HashSet<String> excludedIds) {
        for (SecureClientSender secureClientSender:secureClientSenders.values())
            if (excludedIds==null)
                secureClientSender.broadcastReliableMessage(message);
            else if (excludedIds.contains(secureClientSender.getId())==false)
                secureClientSender.broadcastReliableMessage(message);
    }

    /**
     * Passes the unreliable message to every SecureClientSender to be
     * broadcasted
     *
     * An unreliable message has lower data overheads, but it does not
     * implement the mechanisms to guarantee successful delivery of
     * the message.
     */
    @Override
    public void broadcastUnreliableMessageToAll(byte[] message,
            HashSet<String> excludedIds) {
        for (SecureClientSender secureClientSender:secureClientSenders.values())
            if (excludedIds==null)
                secureClientSender.broadcastUnreliableMessage(message);
            else if (excludedIds.contains(secureClientSender.getId())==false)
                secureClientSender.broadcastUnreliableMessage(message);
    }

    /**
     * Passes the message to every SecureClientSender to be broadcasted
     */
    @Override
    public void broadcastMessageToAll(byte[] message,
            HashSet<String> excludedIds, boolean reliable) {
        if (reliable) broadcastReliableMessageToAll  (message, excludedIds);
        else          broadcastUnreliableMessageToAll(message, excludedIds);
    }

    /**
     * Takes in an incoming message and directs it to the MessageReceiver
     * if it is a normal message,
     * else directs it to the relevant SecureClientSender.
     *
     * Attempts to decrypt and verify all incoming normal messages
     * before passing to the MessageReceiver.
     *
     * Sets up a new instance of SecureClientSender for the corresponding
     * client ID if it does not yet exist.
     */
    @Override
    public void registerMessage(String fromId, byte[] message) {
        if (message[0]==SecurityMessageType.NORMAL.token) {
            message = getVerifiedAndDecryptedNormalMessage(message);
            if (message!=null)
                messageReceiver.registerMessage(fromId, message);
        } else {
            if (secureClientSenders.containsKey(fromId)==false) {

            	// The constructor will link up the SecureClientSender with the
            	// MessageSender that all outgoing messages will need to go to
                SecureClientSender secureClient = new SecureClientSender(fromId, messageSender, ownSecurityData);
                secureClientSenders.put(fromId, secureClient);
                secureClient.registerMessage(message);
            } else {
                secureClientSenders.get(fromId).registerMessage(message);
            }
        }
    }

    /**
     * Verifies an incoming normal message and decrypts it if required by the
     * choice of security protocol chosen by the user
     * @param message The incoming normal message
     * @return        The verified and decrypted message,
     *                null if the verification or decryption has failed.
     */
    public byte[] getVerifiedAndDecryptedNormalMessage(byte[] message) {
        SecurityProtocolType ownSecurityProtocolType = ownSecurityData.getSecurityProtocolType();

        if (ownSecurityProtocolType==SecurityProtocolType.NONE) {
            return SecurityMessageType.getNormalMessageBody(message);
        }

        try {
            byte[][] fields = SecurityHelper.decompose(SecurityMessageType.getNormalMessageBody(message));

            byte[] clientHash = fields[0];

            // Sets message to the message body
            byte[] messageBody = fields[1];

            if (ownSecurityProtocolType==SecurityProtocolType.T2) {

            	// For T2, verify that the MD5 digest in the message
            	// matches the one created using
            	// the user's password, the user's nonce and the message body

                byte[] ownHash = SecurityHelper.getMD5Hash(
                        SecurityDefaults.COMMON_PASSWORD,
                        ownSecurityData.getNonce(),
                        messageBody);
                if (Arrays.equals(clientHash, ownHash))
                    return messageBody;

            } else if (ownSecurityProtocolType==SecurityProtocolType.T3 ||
                       ownSecurityProtocolType==SecurityProtocolType.T4) {

            	// For T3 and T4, decrypt the message body with the user's DES key.
                messageBody = ownSecurityData.getPlainTextWithDES(messageBody);

                // Then verify that the MD5 digest in the message
            	// matches the one created using
            	// the user's password, the user's nonce and the decrypted message body

                byte[] ownHash = SecurityHelper.getMD5Hash(
                        SecurityDefaults.COMMON_PASSWORD,
                        ownSecurityData.getNonce(),
                        messageBody);
                if (Arrays.equals(clientHash, ownHash))
                    return messageBody;

            } else if (ownSecurityProtocolType==SecurityProtocolType.T5) {

            	// For T5, decrypt the message with the user's DES key.
                messageBody = ownSecurityData.getPlainTextWithDES(messageBody);

                // Then verify that the MD5 digest in the message
            	// matches the one created using
            	// the user's nonce and the decrypted message body

                byte[] ownHash = SecurityHelper.getMD5Hash(
                        ownSecurityData.getNonce(),
                        messageBody);
                if (Arrays.equals(clientHash, ownHash))
                    return messageBody;

            }
            return null;
        } catch (Exception e) {
            return null;
        }

    }
}
