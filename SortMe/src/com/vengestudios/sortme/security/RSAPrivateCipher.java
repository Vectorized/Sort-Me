package com.vengestudios.sortme.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

/**
 * A RSAPrivateCipherr provides encryption and decryption using a RSA private key
 *
 * The RSA Public and Private Keys are generated randomly
 *
 * It provides methods to get the RSA public key.
 *
 * To reinforce the secrecy of the private key,
 * no method is provided for getting the RSA private key.
 */
public class RSAPrivateCipher {

    private static final int DEFAULT_KEY_SIZE = SecurityDefaults.RSA_KEY_LENGTH;
    private PrivateKey privateKey;
    private PublicKey  publicKey;
    private Cipher     encrypterCipher;
    private Cipher     decrypterCipher;

    /**
     * Constructor
     * Creates an instance of RSAPrivateCipher with the default key size
     */
    public RSAPrivateCipher() throws Exception {
        this(DEFAULT_KEY_SIZE);
    }

    /**
     * Constructor
     * Creates an instance of RSAPrivateCipher
     * @param keySize  The size of the RSA key to generate
     */
    public RSAPrivateCipher(int keySize) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(SecurityDefaults.RSA_FLAG);
        keyGen.initialize(keySize, new SecureRandom());

        KeyPair keyPair = keyGen .genKeyPair();
        privateKey      = keyPair.getPrivate();
        publicKey       = keyPair.getPublic();

        encrypterCipher = Cipher.getInstance(SecurityDefaults.RSA_FLAG);
        encrypterCipher.init(Cipher.ENCRYPT_MODE, privateKey);

        decrypterCipher = Cipher.getInstance(SecurityDefaults.RSA_FLAG);
        decrypterCipher.init(Cipher.DECRYPT_MODE, privateKey);
    }

    /**
     * @return The RSA public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Get the cipher text encrypted using the RSA private key
     * @param plainText  The text to encrypt
     * @return           The encrypted text
     */
    public byte[] getCipherText(byte[] plainText) throws Exception {
        return encrypterCipher.doFinal(plainText);
    }

    /**
     * Get the plain text decrypted using the RSA private key
     * @param plainText  The text to decrypt
     * @return           The decrypted text
     */
    public byte[] getPlainText(byte[] cipherText) throws Exception {
        return decrypterCipher.doFinal(cipherText);
    }
}
