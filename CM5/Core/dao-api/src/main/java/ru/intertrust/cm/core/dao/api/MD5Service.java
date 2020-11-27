package ru.intertrust.cm.core.dao.api;

/**
 * Сервис для кодирования сообщений, используюя MD5 алгоритм хеширования.
 * @author atsvetkov
 * 
 */
public interface MD5Service {

    /**
     * Получение 16-ричного MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    String getMD5AsHex(String message);

    /**
     * Получение 32-ричного MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    String getMD5As32Base(String message);

    /**
     * Получение 16-ричного MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    String getMD5AsHex(byte[] message);
}
