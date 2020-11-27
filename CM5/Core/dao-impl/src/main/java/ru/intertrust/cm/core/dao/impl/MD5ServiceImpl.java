package ru.intertrust.cm.core.dao.impl;

import ru.intertrust.cm.core.business.api.util.MD5Utils;
import ru.intertrust.cm.core.dao.api.MD5Service;

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

    public static void main(String [] args){
        System.out.println("Text");
        String md5 = new MD5ServiceImpl().getMD5AsHex("Text");
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5AsHex("trim(name)"));
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5AsHex("trim(name)"));
        
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5AsHex("btrim(name)"));

        System.out.println("hash : " + "trim(name)".hashCode());
        System.out.println("hash : " + "trim(name)1".hashCode());

    }
    
}
