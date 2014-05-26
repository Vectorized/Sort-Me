package com.vengestudios.sortme.security;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * A Cipher that can be used to encrypt and decrypt using DES
 */
public class DESCipher {
    private static final int KEY_SIZE = 56;

    private Key    key;
    private Cipher encrypterCipher;
    private Cipher decrypterCipher;

    /**
     * Constructor
     * Initializes a DESCipher using the DES key
     * @param key  The DES key encoded in bytes
     */
    public DESCipher(byte[] key) throws Exception {
        this(SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key)));
    }

    /**
     * Constructor
     * Initializes a DESCipher using the DES key
     * @param key  The DES Key
     */
    public DESCipher(Key key) throws Exception {
        this.key = key;
        initCiphers();
    }

    /**
     * Constructor
     * Initializes a DESCipher using a randomly generated DES key
     */
    public DESCipher() throws Exception {
        KeyGenerator keyGen       = KeyGenerator.getInstance("DES");
        SecureRandom secureRandom = new SecureRandom();
        keyGen.init(KEY_SIZE, secureRandom);
        key = keyGen.generateKey();

        initCiphers();
    }

    /**
     * Initializes the ciphers needed for encryption and decryption
     */
    private void initCiphers() throws Exception {
        encrypterCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        encrypterCipher.init(Cipher.ENCRYPT_MODE, key);

        decrypterCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        decrypterCipher.init(Cipher.DECRYPT_MODE, key);
    }

    /**
     * @return The DES key
     */
    public Key getkey() {
        return key;
    }

    /**
     * Get the cipher text encrypted using the DES key in the cipher
     * @param plainText  The text to encrypt
     * @return           The encrypted text
     */
    public byte[] getCipherText(byte[] plainText) throws Exception {
        return encrypterCipher.doFinal(plainText);
    }

    /**
     * Get the plain text decrypted using the DES key in the cipher
     * @param cipherText  The text to decrypt
     * @return            The decrypted text
     */
    public byte[] getPlainText(byte[] cipherText) throws Exception {
        return decrypterCipher.doFinal(cipherText);
    }

}
