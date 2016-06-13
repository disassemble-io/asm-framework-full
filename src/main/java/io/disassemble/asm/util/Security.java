package io.disassemble.asm.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Formatter;
import java.util.Optional;

/**
 * @author Tyler Sedlar
 * @since 6/10/16
 */
public class Security {

    /**
     * Calculates the base64 of the SHA-1 of the given byte array.
     *
     * @param bytes The bytes to encode.
     * @return The base64 of the SHA-1 of the given byte array.
     */
    public static String b64SHA1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return Base64.getEncoder().encodeToString(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
