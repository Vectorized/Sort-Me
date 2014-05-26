package com.vengestudios.sortme.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * A RSAPrivateCipherr provides encryption and decryption using a RSA public key
 *
 * It has no methods provided for generating RSA key pairs,
 * since this class is only intended for use with a public RSA key.
 */
public class RSAPublicCipher {
    private PublicKey publicKey;
    private Cipher    encrypterCipher;
    private Cipher    decrypterCipher;

    /**
     * Constructor
     * Creates an instance of RSAPublicCipher with the RSA public key
     * @param publicKey The RSA public key encoded in bytes
     */
    public RSAPublicCipher(byte[] publicKey) throws Exception {
        this(KeyFactory.getInstance(SecurityDefaults.RSA_FLAG).generatePublic(new X509EncodedKeySpec(publicKey)));
    }

    /**
     * Constructor
     * Creates an instance of RSAPublicCipher with the RSA public key
     * @param publicKey The RSA public key
     */
    public RSAPublicCipher(PublicKey publicKey) throws Exception {
        this.publicKey = publicKey;

        encrypterCipher = Cipher.getInstance(SecurityDefaults.RSA_FLAG);
        encrypterCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        decrypterCipher = Cipher.getInstance(SecurityDefaults.RSA_FLAG);
        decrypterCipher.init(Cipher.DECRYPT_MODE, publicKey);
    }

    /**
     * @return The RSA public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Get the cipher text encrypted using the RSA public key
     * @param plainText  The text to encrypted
     * @return           The encrypted text
     */
    public byte[] getCipherText(byte[] plainText) throws Exception {
        return encrypterCipher.doFinal(plainText);
    }

    /**
     * Get the plain text decrypted using the RSA public key
     * @param cipherText  The text to decrypt
     * @return            The decrypted text
     */
    public byte[] getPlainText(byte[] cipherText) throws Exception {
        return decrypterCipher.doFinal(cipherText);
    }
}
