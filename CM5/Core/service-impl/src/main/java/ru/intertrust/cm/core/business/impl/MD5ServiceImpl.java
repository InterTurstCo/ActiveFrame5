package ru.intertrust.cm.core.business.impl;

import ru.intertrust.cm.core.business.api.MD5Service;
import ru.intertrust.cm.core.business.api.util.MD5Utils;

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
    
}
