package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.util.MD5Utils;
import ru.intertrust.cm.core.dao.api.MD5Service;
import ru.intertrust.cm.core.model.FatalException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Реализация сервиса MD5 хеширования
 * @author atsvetkov
 *
 */

public class MD5ServiceImpl implements MD5Service {

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.MD5Service#getMD5AsHex(String)}
     */
    @Override
    public String getMD5AsHex(String message) {
        if (message == null || message.length() < 1) {
            return null;
        }
        return MD5Utils.getMD5AsHex(message);
    }

    /**
     * Смотри {@link ru.intertrust.cm.core.dao.api.MD5Service#getMD5As32Base(String)}
     */
    @Override
    public String getMD5As32Base(String message) {
        if (message == null || message.length() < 1) {
            return null;
        }
        return MD5Utils.getMD5As32Base(message);
    }

    @Override
    public String getMD5AsHex(byte[] message) {
        if (message == null || message.length < 1) {
            return null;
        }
        return MD5Utils.getMD5AsHex(message);
    }

    @Override
    public String bytesToHex(byte[] bytes) {
        return MD5Utils.bytesToHex(bytes);
    }

    @Override
    public MessageDigest newMessageDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new FatalException("Unable to get Message Digest for the MD5 algorithm");
        }
    }
}
