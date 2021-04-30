package ru.intertrust.cm.core.business.api.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Utils {

    /**
     * Получение MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    public static String getMD5AsHex(String message) {
        return getMD5AsHex(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Получение 32-ричного MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    public static String getMD5As32Base(String message) {
        if (message == null || message.length() < 1) {
            return null;
        }

        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInteger = new BigInteger(hash);
            return  bigInteger.toString(32);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    }

    /**
     * Получение MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    public static String getMD5AsHex(byte[] message) {
        if (message == null || message.length < 1) {
            return null;
        }

        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message);
            digest = bytesToHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    }

    public static String bytesToHex(byte[] bytes) {
        // converting byte array to Hexadecimal String
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

}
