package com.vengestudios.sortme.security;

/**
 * An enum representing the different types of security protocols
 */
public enum SecurityProtocolType {
    NONE (0),
    T2   (1),
    T3   (2),
    T4   (3),
    T5   (4);


    public int token;

    // Internal private constructor
    private SecurityProtocolType(int token) {
        this.token = token;
    }

    /**
     * @param token An int representing the token of the SecurityProtocolType
     * @return      The corresponding SecurityProtocolType,
     *              else null if the token cannot be matched to a SecurityProtocolType
     */
    public static SecurityProtocolType getSecurityMessageTypeFromToken(int token) {
        for (SecurityProtocolType securityProtocolType:SecurityProtocolType.values()) {
            if (securityProtocolType.token==token)
                return securityProtocolType;
        }
        return null;
    }

}
