package com.vengestudios.sortme.security;

/**
 * Default variables used for the SecurityMessageLayer
 */
public class SecurityDefaults {

    public static final String RSA_FLAG        = "RSA";
    public static final int    RSA_KEY_LENGTH  = 1024;
    public static final byte[] COMMON_PASSWORD = "s3cr3T".getBytes();
    public static final int    NONCE_LENGTH    = 5;

    public static final int    TOTAL_SEND_AUTHENTICATION_MESSAGES = 3;

    public static final SecurityProtocolType SECURITY_PROTOCOL_TYPE
        = SecurityProtocolType.T5;
}
