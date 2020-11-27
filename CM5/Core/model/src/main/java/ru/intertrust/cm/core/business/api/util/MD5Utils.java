package ru.intertrust.cm.core.business.api.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ru.intertrust.cm.core.model.FatalException;

public class MD5Utils {

    /**
     * Получение MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    public static String getMD5AsHex(String message) {
        try {
            return getMD5AsHex(message.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new FatalException("Error create md5 hash", ex);
        }
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
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            BigInteger bigInteger = new BigInteger(hash);
            return  bigInteger.toString(32);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MD5Utils.class.getName()).log(Level.SEVERE, null, ex);
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
            // converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    }

}
