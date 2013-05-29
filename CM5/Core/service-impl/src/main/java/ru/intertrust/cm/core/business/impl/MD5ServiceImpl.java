package ru.intertrust.cm.core.business.impl;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.intertrust.cm.core.business.api.MD5Service;

/**
 * Реализация сервиса MD5 хеширования
 * @author atsvetkov
 *
 */

public class MD5ServiceImpl implements MD5Service {

    /**
     * Смотри {@link ru.intertrust.cm.core.business.api.MD5Service#getMD5(java.lang.String)}
     */
    @Override
    public String getMD5(String message) {
        if (message == null || message.length() < 1) {
            return null;
        }
        
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            // converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MD5ServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5ServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return digest;
    }
    
}
