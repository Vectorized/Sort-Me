package com.vengestudios.sortme.security;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import android.util.Log;

import com.vengestudios.sortme.MessageSender;

/**
 * The SecureClientSender represents the connection with another client on the network
 *
 * Responsible for:
 *
 *  - Holding the other client's security protocol choice
 *
 *  - Establishing a mutually authenticated status with the user and the other client
 *
 *  - Performing the security protocols T2, T3, T4, T5
 *
 *  - Encryption of outgoing messages to the client if requested by the protocol
 *
 *  - Prepare the outgoing messages for integrity checks
 */
public class SecureClientSender {

	// The ID of the other client
    private String               id;

    // Fields used for Balloting whether the user or other client
    // gets to be the first sender for the mutual authentication protocol
    private int                  ownFirstSenderBallot;
    private boolean              firstSenderDecided;

    // Fields to hold the client's security credentials
    private byte[]               clientNonce;
    private byte[]               clientPassword;
    private SecurityProtocolType clientSecurityProtocolType;

    // Fields to hold the messages used in the interlock protocol
    private byte[][]             interlockSendMessages;
    private byte[][]             interlockReceivedMessages;

    // Fields to keep track of the state of the interlock protocol
    private int                  authenticationReceivedCount;
    private int                  authenticationSentCount;

    // Ciphers used for secure communication with the client
    private DESCipher            clientDESCipher;
    private RSAPublicCipher      clientRSACipher;

    // The "layer" to pass outgoing messages to
    private MessageSender        messageSender;

    // A reference to the user's OwnSecurityData
    private OwnSecurityData      ownSecurityData;

    // Queues to buffer outgoing messages when the mutual authentication
    // protocol is still in progress.
    // The queues will be flushed when the client has authenticated him/herself.
    private Queue<byte[]>        reliableMessageQueue;
    private Queue<byte[]>        unreliableMessageQueue;

    // Whether the normal outgoing messages can be sent over and
    // need not be queued
    private boolean              readyToSend;


    /**
     * Constructor
     *
     * Creates an instance of SecureClientSender
     *
     * @param id              The ID of the other client
     * @param messageSender   The message sending "layer" to pass outgoing messages to
     * @param ownSecurityData The user's  OwnSecurityData
     */
    public SecureClientSender(String id, MessageSender messageSender,
            OwnSecurityData ownSecurityData) {

        clientSecurityProtocolType = SecurityProtocolType.NONE;
        this.id                     = id;
        this.messageSender          = messageSender;
        this.ownSecurityData        = ownSecurityData;
        this.reliableMessageQueue   = new LinkedList<byte[]>();
        this.unreliableMessageQueue = new LinkedList<byte[]>();
        sendBallotMessage();

    }

    /**
     * @return The ID of the other client
     */
    public String getId() {
        return id;
    }

    /**
     * Sends an outgoing ballot message to the MessageSender.
     * Balloting will determine whether the user or the other client
     * gets to be the first sender in the mutual authentication protocols.
     *
     * The ballot is a pseudo-randomly generated int
     */
    private void sendBallotMessage() {
        ownFirstSenderBallot = new Random().nextInt(10000);
        byte[] handshakeMessage = SecurityMessageType.getBallotMessage(ownFirstSenderBallot);
        messageSender.broadcastReliableMessageToId(handshakeMessage, id);
    }

    /**
     * Register an incoming ballot message received from the other client
     *
     * If somehow, both pseudo-randomly generated ballots are equal,
     * a new ballot will be sent.
     *
     * The person with the lower ballot gets to be the 1st sender
     *
     * @param message  The incoming ballot message
     */
    private void registerBallotMessage(byte[] message) {
        if (firstSenderDecided)
            return;

        int clientFirstSenderBallot = SecurityMessageType.getBallotMessageValue(message);

        if (clientFirstSenderBallot == ownFirstSenderBallot) {
            sendBallotMessage();
        } else if (clientFirstSenderBallot > ownFirstSenderBallot) {
            firstSenderDecided = true;
            sendAuthenticationMessage();
        } else {
            firstSenderDecided = true;
        }
    }

    /**
     * Registers an incoming message received from the other client
     * @param message  The incoming message
     */
    public void registerMessage(byte[] message) {
        if (message[0] == SecurityMessageType.BALLOT.token)
            registerBallotMessage(message);
        else if (message[0] == SecurityMessageType.AUTHENTICATION.token)
            try { registerAuthenticationMessage(message); } catch (Exception e) {}
    }

    /**
     * Prepares the outgoing interlock messages to be sent to the other
     * client based the user's own choice of security protocol
     *
     * In the case where an the user does not need to use one or both of the
     * outgoing interlock messages, a dummy message is being used for the
     * unused message.
     *
     * This is to facilitate mixing of protocol choices for a more seamless
     * integration.
     *
     * The client might have chosen a security protocol that requires the user to
     * acknowledge the successful reception of two meaningful interlock messages.
     *
     * The outgoing interlock messages serve the purpose of acknowledge
     * the reception too.
     */
    private void prepareInterlockSendMessages() throws Exception {
        if (ownSecurityData.getSecurityProtocolType() == SecurityProtocolType.NONE) {

        	// If no protocol is choosen, then just send dummy messages
            interlockSendMessages = new byte[2][];
            interlockSendMessages[0] = getAuthenticationMessage(" ".getBytes());
            interlockSendMessages[1] = getAuthenticationMessage(" ".getBytes());

        } else if (ownSecurityData.getSecurityProtocolType() == SecurityProtocolType.T2) {

        	// If T2, encrypt the user's own password and the other client's nonce
        	// using the other client's RSA public key
            byte[] messageCipherText = composeEncryptedMessageWithClientRSAPublicKey(
                    SecurityDefaults.COMMON_PASSWORD, clientNonce);

            // Splits the message into two halves and sets them as the outgoing interlock
            // messages
            interlockSendMessages = SecurityHelper
                    .splitIntoHalves(messageCipherText);
            interlockSendMessages[0] = getAuthenticationMessage(interlockSendMessages[0]);
            interlockSendMessages[1] = getAuthenticationMessage(interlockSendMessages[1]);

        } else if (ownSecurityData.getSecurityProtocolType() == SecurityProtocolType.T3) {

        	// If T3, encrypt the user's own password, the other client's nonce,
        	// and the user's DES key using the other client's RSA public key
            byte[] messageCipherText = composeEncryptedMessageWithClientRSAPublicKey(
                    SecurityDefaults.COMMON_PASSWORD, clientNonce,
                    ownSecurityData.getDESKeyBytes());

            // Splits the message into two halves and sets them as the outgoing interlock
            // messages
            interlockSendMessages = SecurityHelper
                    .splitIntoHalves(messageCipherText);
            interlockSendMessages[0] = getAuthenticationMessage(interlockSendMessages[0]);
            interlockSendMessages[1] = getAuthenticationMessage(interlockSendMessages[1]);

        } else if (ownSecurityData.getSecurityProtocolType() == SecurityProtocolType.T4) {

            interlockSendMessages = new byte[2][];

        	// If T4, encrypt the user's own password, the other client's nonce,
        	// and the user's DES key using the other client's RSA public key
            byte[] messageCipherText = composeEncryptedMessageWithClientRSAPublicKey(
                    SecurityDefaults.COMMON_PASSWORD, clientNonce,
                    ownSecurityData.getDESKeyBytes());

            // Get a MD5 digest of the whole cipher text
            byte[] messageCipherDigest = SecurityHelper
                    .getMD5Hash(messageCipherText);

            // Assign the digest to the 1st outgoing interlock message
            interlockSendMessages[0] = getAuthenticationMessage(messageCipherDigest);

            // And the whole cipher text to the 2nd outgoing interlock message
            interlockSendMessages[1] = getAuthenticationMessage(messageCipherText);

        } else if (ownSecurityData.getSecurityProtocolType() == SecurityProtocolType.T5) {

            interlockSendMessages = new byte[2][];
            getEncryptedMessageWithOwnRSAPrivateKey(clientNonce);

        	// If T5, encrypt the user's DES key and the other client's nonce
            // using the other client's RSA public key
            byte[] encryptedDESKeyAndNonce =
                    composeEncryptedMessageWithClientRSAPublicKey(
                            ownSecurityData.getDESKeyBytes(), clientNonce);

            // Create an MD5 digest of the whole cipher text,
            // and sign it with the user's own RSA private key
            byte[] signedDigest = getEncryptedMessageWithOwnRSAPrivateKey(
                    SecurityHelper.getMD5Hash(encryptedDESKeyAndNonce));

            // Combine the cipher text and signed digest into the 1st
            // outgoing interlock message
            interlockSendMessages[0] = getAuthenticationMessage(
                    SecurityHelper.compose(encryptedDESKeyAndNonce, signedDigest));

            // Since the user does not technically need to use
            // the 2nd outgoing interlock message.
            // Set it to a dummy message.
            interlockSendMessages[1] = getAuthenticationMessage(" ".getBytes());

        }
    }

    /**
     * Register an authentication message received from the other client
     * and proceed on with the required step of the mutual authentication
     * protocol.
     *
     * @param message  The incoming message received from the other client
     */
    private void registerAuthenticationMessage(byte[] message) throws Exception {

        if (authenticationReceivedCount == 0) {

        	// If it is the 1st message received, extract the other client's
        	// RSA public key, nonce, and choice of security protocol from it

            clientSecurityProtocolType = SecurityMessageType
                    .getSecurityProtocolType(message);

            byte[][] messageFields = SecurityHelper
                    .decompose(getAuthenticationMessageBody(message));

            clientNonce     = messageFields[0];
            clientRSACipher = new RSAPublicCipher(messageFields[1]);

            // Prepare the interlock messages once we have set up the
            // ClientRSACipher using the other client's RSA public key
            prepareInterlockSendMessages();

            // If the client has decided on not using a security protocol,
            // we can release the lock on the outgoing message queues now
            // and start sending messages over.
            if (clientSecurityProtocolType == SecurityProtocolType.NONE)
                concludeAuthenticationForNoneProtocolClient();

            // Proceed to send the next authentication message.
            sendAuthenticationMessage();

        } else if (authenticationReceivedCount == 1) {

            interlockReceivedMessages = new byte[2][];

            // Register the received message as the 1st
            // received interlock message
            interlockReceivedMessages[0] = getAuthenticationMessageBody(message);

            // If the client has choosen T5, we can release the lock
            // on the outgoing message queues now and start sending messages over.
            if (clientSecurityProtocolType == SecurityProtocolType.T5)
                concludeAuthenticationForT5Client();

            // Proceed to send the next authentication message.
            sendAuthenticationMessage();

        } else if (authenticationReceivedCount == 2) {

            // Register the received message as the 2nd
            // received interlock message
            interlockReceivedMessages[1] = getAuthenticationMessageBody(message);

            // For the security protocol chosen by the client,
            // call the respective method to conclude the mutual authentication
            // and release the lock on the outgoing queues
            if (clientSecurityProtocolType == SecurityProtocolType.T2)
                concludeAuthenticationForT2Client();
            else if (clientSecurityProtocolType == SecurityProtocolType.T3)
                concludeAuthenticationForT3Client();
            else if (clientSecurityProtocolType == SecurityProtocolType.T4)
                concludeAuthenticationForT4Client();

            // Proceed to send the next authentication message
            sendAuthenticationMessage();
        }

        ++authenticationReceivedCount;
    }

    /**
     * Releases the lock on the outgoing message queues
     */
    private void concludeAuthenticationForNoneProtocolClient() throws Exception {
        // Release the lock on the outgoing message queues
        setReadyToSend();
    }

    /**
     *  - Verify the integrity of the received interlock messages from the
     *    other client.
     *  - Releases the bar on the outgoing message queues
     */
    private void concludeAuthenticationForT2Client() throws Exception {
    	// Concatenate the 2nd interlock message to the 1st,
    	// Decompose it into its fields.
        byte[][] fields = decomposeEncryptedMessageWithOwnRSAPrivateKey(SecurityHelper
                .concat(interlockReceivedMessages[0],
                        interlockReceivedMessages[1]));

        byte[] receivedClientPassword = fields[0];
        byte[] receivedNonce          = fields[1];

        // Verify that the received client password is
        // same as the shared password.
        // Verify that the nonce received is the same as the user's own nonce.
        if (Arrays.equals(receivedClientPassword,
                SecurityDefaults.COMMON_PASSWORD)
                && Arrays.equals(ownSecurityData.getNonce(), receivedNonce)) {

            clientPassword = receivedClientPassword;

            // Release the lock on the outgoing message queues
            setReadyToSend();

        } else {
            failAuthentication();
        }
    }

    /**
     *  - Verify the integrity of the received interlock messages from the
     *    other client.
     *  - Set up the DES cipher to encrypt outgoing messages to the other client
     *  - Releases the bar on the outgoing message queues
     */
    private void concludeAuthenticationForT3Client() throws Exception {
    	// Concatenate the 2nd interlock message to the 1st,
    	// Decompose it into its fields.
        byte[][] fields = decomposeEncryptedMessageWithOwnRSAPrivateKey(
                SecurityHelper.concat(interlockReceivedMessages[0], interlockReceivedMessages[1]));
        byte[] receivedClientPassword = fields[0];
        byte[] receivedNonce          = fields[1];
        byte[] clientDESKeyBytes      = fields[2];

        // Verify that the received client password is
        // same as the shared password.
        // Verify that the nonce received is the same as the user's own nonce.
        if (Arrays.equals(receivedClientPassword,
                SecurityDefaults.COMMON_PASSWORD)
                && Arrays.equals(ownSecurityData.getNonce(), receivedNonce)) {

            clientPassword  = receivedClientPassword;

            // Set up the DES Cipher for the other client.
            clientDESCipher = new DESCipher(clientDESKeyBytes);

            // Release the lock on the outgoing message queues
            setReadyToSend();

        } else {
            failAuthentication();
        }
    }

    /**
     *  - Verify the integrity of the received interlock messages from the
     *    other client.
     *  - Set up the DES cipher to encrypt outgoing messages to the other client
     *  - Releases the bar on the outgoing message queues
     */
    private void concludeAuthenticationForT4Client() throws Exception {

        byte[] interlockCipherTextHash = interlockReceivedMessages[0];
        byte[] interlockCipherText = interlockReceivedMessages[1];

        // Verify the integrity of the 2nd received interlock message
        // by checking it with the MD5 hash that was the 1st received interlock message
        if (Arrays.equals(SecurityHelper.getMD5Hash(interlockCipherText),
                interlockCipherTextHash)) {

            byte[][] fields = decomposeEncryptedMessageWithOwnRSAPrivateKey(interlockCipherText);
            byte[] receivedClientPassword = fields[0];
            byte[] receivedNonce          = fields[1];
            byte[] clientDESKeyBytes      = fields[2];

            // Verify that the received client password is
            // same as the shared password.
            // Verify that the nonce received is the same as the user's own nonce.
            if (Arrays.equals(ownSecurityData.getNonce(), receivedNonce)
                    && Arrays.equals(receivedClientPassword,
                            SecurityDefaults.COMMON_PASSWORD)) {

                clientPassword  = receivedClientPassword;

                // Set up the DES Cipher for the other client.
                clientDESCipher = new DESCipher(clientDESKeyBytes);

                // Release the lock on the outgoing message queues
                setReadyToSend();

            } else {
                failAuthentication();
            }
        } else {
            failAuthentication();
        }
    }

    /**
     *  - Verify the integrity of the 1st received interlock message
     *  - Set up the DES cipher to encrypt outgoing messages to the other client
     *  - Releases the bar on the outgoing message queues
     */
    private void concludeAuthenticationForT5Client() throws Exception {

        byte[][] fields = SecurityHelper.decompose(interlockReceivedMessages[0]);
        byte[] encryptedDESKeyAndNonce  = fields[0];
        byte[] signedDigest             = fields[1];

        // Verify that digest is truly signed by the other client
        // by decrypting it with his/her RSA pubkic key, then comparing
        // the result with the MD5 hash of the other part of the message
        if (Arrays.equals(
                SecurityHelper.getMD5Hash(encryptedDESKeyAndNonce),
                getDecryptedMessageWithClientRSAPublicKey(signedDigest))) {

            byte[][] subFields = decomposeEncryptedMessageWithOwnRSAPrivateKey(encryptedDESKeyAndNonce);
            byte[] clientDESKeyBytes = subFields[0];
            byte[] receivedNonce     = subFields[1];

            // Verify that the nonce received is the same as the user's own nonce.
            if (Arrays.equals(receivedNonce, ownSecurityData.getNonce())) {

                // Set up the DES Cipher for the other client.
                clientDESCipher = new DESCipher(clientDESKeyBytes);

                // Release the lock on the outgoing message queues
                setReadyToSend();

            } else {
                failAuthentication();
            }
        } else {
            failAuthentication();
        }
    }

    /**
     * Prints out a message to the Log console that the client has failed
     * to authenticate him/herself
     * @throws Exception
     */
    private void failAuthentication() throws Exception {
        Log.e("Security Message Layer", "The client has failed to authenticate him/herself!");
        throw new Exception();
    }

    /**
     * Sends the next authentication message over to the other client
     */
    private void sendAuthenticationMessage() {
        byte[] authenticationMessage = {};

        if (authenticationSentCount == 0) {

        	// The 1st message would contain the user's nonce
        	// and the user's RSA public key.
            authenticationMessage = getAuthenticationMessage(SecurityHelper
                    .compose(ownSecurityData.getNonce(),
                            ownSecurityData.getRSAPublicKeyBytes()));

        } else if (authenticationSentCount == 1) {

        	// Set outgoing authentication message as 1st interlock message
            authenticationMessage = interlockSendMessages[0];

        } else if (authenticationSentCount == 2) {

        	// Set outgoing authentication message 2nd interlock message
            authenticationMessage = interlockSendMessages[1];

        } else if (authenticationSentCount == 3) {

        	// If the user has sent all the interlock messages,
        	// just set a dummy message to the outgoing message.
            authenticationMessage = getAuthenticationMessage(" ".getBytes());
        }
        ++authenticationSentCount;

        // Sends the outgoing message to the other client
        messageSender.broadcastReliableMessageToId(authenticationMessage, id);
    }

    /**
     * Get the cipher text encrypted using the user's RSA private key
     * @param plainText  The text to encrypt
     * @return           The encrypted text
     */
    private byte[] getEncryptedMessageWithOwnRSAPrivateKey(byte[] message)
            throws Exception {
        return ownSecurityData.getCipherTextWithRSA(message);
    }

    /**
     * Get the plain text decrypted using the other client's RSA public key
     * @param cipherText  The text to decrypt
     * @return            The decrypted text
     */
    private byte[] getDecryptedMessageWithClientRSAPublicKey(byte[] message)
            throws Exception {
        return clientRSACipher.getPlainText(message);
    }

    /**
     * Compose an encrypted message using the other client's RSA public key
     * @param fields The fields in the message
     * @return       The encrypted message
     */
    private byte[] composeEncryptedMessageWithClientRSAPublicKey(
            byte[]... fields) throws Exception {
        //return SecurityHelper.compose(fields);
        return clientRSACipher.getCipherText(SecurityHelper.compose(fields));
    }

    /**
     * Decrypts a message with the user's RSA private key and decompose it
     * into its fields
     * @param message The message
     * @return        The fields in the message
     */
    private byte[][] decomposeEncryptedMessageWithOwnRSAPrivateKey(
            byte[] message) throws Exception {

        byte[] plainText = ownSecurityData.getPlainTextWithRSA(message);
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b:plainText)
            stringBuffer.append((int)b);

        return SecurityHelper.decompose(plainText);
    }

    /**
     * @param messageBody The body of the authentication message
     * @return A message labeled for use in the authentication protocol
     *         with the type of protocol needed added onto its header
     */
    private byte[] getAuthenticationMessage(byte[] messageBody) {
        return SecurityMessageType.getAuthenticationMessage(
                ownSecurityData.getSecurityProtocolType(), messageBody);
    }

    /**
     * @param message The message labeled as an authentication message
     * @return        The body of the message
     */
    private byte[] getAuthenticationMessageBody(byte[] message) {
        return SecurityMessageType.getAuthenticationMessageBody(message);
    }

    /**
     * Releases the lock on the outgoing message queues for normal messages
     */
    private void setReadyToSend() {
        readyToSend = true;
        flushQueues();
    }

    /**
     * Sends out all the normal messages that have been queued in the
     * outgoing message queues
     */
    public void flushQueues() {
        while (reliableMessageQueue.isEmpty() == false)
            broadcastReliableMessage(reliableMessageQueue.remove());

        while (unreliableMessageQueue.isEmpty() == false)
            broadcastUnreliableMessage(unreliableMessageQueue.remove());
    }

    /**
     * Prepares the message for sending over to the other client as an
     * by implementing any required encryption and/or
     * message integrity verification information.
     *
     * The message is then labeled as an normal message
     *
     * @param message
     * @return The prepared normal message.
     */
    private byte[] getNormalMessage(byte[] message) {

        if (clientSecurityProtocolType==SecurityProtocolType.NONE) {
            return SecurityMessageType.getNormalMessage(message);
        }

        try{
            if (clientSecurityProtocolType==SecurityProtocolType.T2) {

                byte[] passwordNonceMessageHash =
                        SecurityHelper.getMD5Hash(clientPassword, clientNonce, message);

                // For T2, we try to add a digest that is made
                // with the shared password, client's nonce, and message
                //
                // Although this does not provide confidentiality,
                // it can serve as a integrity verifier, since a
                // man-in-the-middle is unable to replace the message
                // and recreate the correct digest for it.
                //
                // He would need to know the other client's nonce and password.
                // To be able to do so
                message = SecurityHelper.compose(passwordNonceMessageHash, message);

            } else if (clientSecurityProtocolType==SecurityProtocolType.T3 ||
                       clientSecurityProtocolType==SecurityProtocolType.T4) {

            	// Here, we give the message a digest using the other client's
            	// password, nonce and message again to provide the message with
            	// "freshness" to prevent replay attacks
                byte[] passwordNonceMessageHash =
                        SecurityHelper.getMD5Hash(clientPassword, clientNonce, message);

                // As requested by the protocol, we would encrypt the message
                // with the other client's DES key as well
                byte[] encryptedMesage = clientDESCipher.getCipherText(message);

                message = SecurityHelper.compose(passwordNonceMessageHash, encryptedMesage);

            } else if (clientSecurityProtocolType==SecurityProtocolType.T5) {

            	// Here, we give the message a digest using the other client's
            	// nonce and the message to provide the meessage with "freshness"
            	// to prevent replay attacks
                byte[] nonceMessageHash =
                        SecurityHelper.getMD5Hash(clientNonce, message);

                // As requested by the protocol, we would encrypt the message
                // with the other client's DES key as well
                byte[] encryptedMessage = clientDESCipher.getCipherText(message);

                message = SecurityHelper.compose(nonceMessageHash, encryptedMessage);

            } else {
                return null;
            }
            return SecurityMessageType.getNormalMessage(message);
        } catch (Exception e) {
            return null;
        }
    }

    public void broadcastReliableMessage(byte[] message) {
        if (readyToSend == false) {
            reliableMessageQueue.add(message);
            return;
        }
        message = getNormalMessage(message);
        if (message!=null)
            messageSender.broadcastReliableMessageToId(message, id);
    }

    public void broadcastUnreliableMessage(byte[] message) {
        if (readyToSend == false) {
            unreliableMessageQueue.add(message);
            return;
        }
        message = getNormalMessage(message);
        if (message!=null)
            messageSender.broadcastUnreliableMessageToId(message, id);
    }

}
