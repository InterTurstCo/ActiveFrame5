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
     * Смотри {@link ru.intertrust.cm.core.business.api.MD5Service#getMD5(java.lang.String)}
     */
    @Override
    public String getMD5(String message) {
        if (message == null || message.length() < 1) {
            return null;
        }
        return MD5Utils.getMD5(message);
    }
    
    public static void main(String [] args){
        System.out.println("Text");
        String md5 = new MD5ServiceImpl().getMD5("Text");
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5("trim(name)"));
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5("trim(name)"));
        
        System.out.println("md5 : " + new MD5ServiceImpl().getMD5("btrim(name)"));

        System.out.println("hash : " + "trim(name)".hashCode());
        System.out.println("hash : " + "trim(name)1".hashCode());

    }
    
}
