package com.vengestudios.sortme.security;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * A helper class to assist in various security tasks, and handling
 * byte array based messages
 */
public class SecurityHelper {

	/**
	 * @param messages An array of messages
	 * @return A MD5 hash created from combining the messages
	 */
    public static byte[] getMD5Hash(byte[] ... messages) throws Exception {
        return getMD5Hash(join(messages));
    }

	/**
	 * @param messages
	 * @return A MD5 hash created from the message
	 */
    public static byte[] getMD5Hash(byte[] message) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        return md.digest(message);
    }

    /**
     * @param length
     * @return A pseudo-randomly generated security nonce
     */
    public static byte[] generateNonce (int length) throws Exception {
        byte[] nonce = new byte[length];
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        rand.nextBytes(nonce);
        return nonce;
    }

    /**
     * @param message The message to be split into halves
     * @return An array containing the two halves of the message
     */
    public static byte[][] splitIntoHalves(byte[] message) {
        int messageLength = message.length;
        int halfMessageLength = messageLength/2;
        byte[][] halves = new byte[2][];
        halves[0] = Arrays.copyOfRange(message, 0, halfMessageLength);
        halves[1] = Arrays.copyOfRange(message, halfMessageLength, messageLength);
        return halves;
    }

    /**
     * @param a The 1st byte array
     * @param b The 2nd byte array
     * @return A byte array created by concatenating the 2nd byte
     *         array to the 1st byte array
     */
    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * @param b An array of 4 bytes
     * @return  The int representation of the array of 4 bytes
     */
    public static int fourBytesToInt(byte[] b) {
        return ByteBuffer.wrap(b).getInt();
    }

    /**
     * @param i An int
     * @return  An array of 4 bytes representing the int
     */
    public static byte[] intToFourBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    /**
     * @param arr An array of byte arrays
     * @return    A single byte array created by joining the byte arrays
     */
    public static byte[] join(byte[] ... arr) {
        int totalLength = 0;
        for (byte[] b:arr) {
            totalLength += b.length;
        }
        byte[] joined = new byte[totalLength];
        int offSet = 0;
        for (byte[] b:arr) {
            System.arraycopy(b, 0, joined, offSet, b.length);
            offSet += b.length;
        }
        return joined;
    }


    /**
     * @param a     A byte to append to the start of a byte array
     * @param array The byte array to be appended to
     * @return      The result byte array with the byte appended to its start
     */
    public static byte[] appendToStart(byte a, byte[] array) {
        byte[] toBeAppended = {a};
        return concat(toBeAppended, array);
    }

    /**
     * @param fields An array of byte arrays
     * @return       A single byte array that can be broken down into the
     *               original array of byte arrays by calling decompose(byte[] arr)
     */
    public static byte[] compose(byte[] ... fields) {
        int totalLength = 0;
        int [] fieldLengths = new int[fields.length];
        for (int i=0; i<fields.length; ++i) {
            int fieldLength = fields[i].length;
            fieldLengths[i] = fieldLength;
            totalLength    += fieldLength;
        }
        int offSet = fields.length*4 + 4;
        byte[] composed = new byte[totalLength + fields.length*4 + 4];

        // Note that we take the negative of arr.length here, so that the MSB of the int is 1
        // This is to prevent Android RSA from removing leading zeros.
        System.arraycopy(intToFourBytes(-fields.length), 0, composed, 0, 4);
        for (int i=0; i<fields.length; ++i) {
            System.arraycopy(intToFourBytes(fieldLengths[i]), 0, composed, (i+1)*4, 4);
        }
        for (byte[] b:fields) {
            System.arraycopy(b, 0, composed, offSet, b.length);
            offSet += b.length;
        }
        return composed;
    }


    /**
     * @param arr A byte array made from an array of byte arrays
     *            using compose(byte[] ... fields)
     * @return    The original array of byte arrays
     */
    public static byte[][] decompose(byte[] arr) {
        int minArrayLength = 4;


        if (arr.length < minArrayLength)
            return null;

        int numberOfFields = Math.abs(fourBytesToInt(Arrays.copyOfRange(arr, 0, 4)));
        minArrayLength += numberOfFields*4;

        if (arr.length < minArrayLength)
            return null;

        int fieldLengths[] = new int[numberOfFields];
        int totalFieldLength = 0;
        for (int i=0; i<numberOfFields; ++i) {
            int startIndex   = 4*i + 4;
            int endIndex     = startIndex + 4;
            int fieldLength  = fourBytesToInt(Arrays.copyOfRange(arr, startIndex, endIndex));
            fieldLengths[i]  = fieldLength;
            totalFieldLength += fieldLength;
        }
        int offSet = minArrayLength;
        minArrayLength += totalFieldLength;

        if (arr.length < minArrayLength)
            return null;

        byte[][] fields = new byte[numberOfFields][];
        for (int i=0; i<numberOfFields; ++i) {
            fields[i] = Arrays.copyOfRange(arr, offSet, offSet += fieldLengths[i]);
        }
        return fields;
    }

}
