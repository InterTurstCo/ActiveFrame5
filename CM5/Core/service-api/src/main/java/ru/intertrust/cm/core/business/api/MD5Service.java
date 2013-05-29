package ru.intertrust.cm.core.business.api;

/**
 * Сервис для кодирования сообщений, используюя MD5 алгоритм хеширования.
 * @author atsvetkov
 * 
 */
public interface MD5Service {

    /**
     * Получение MD5 хеша для переданного сообщения
     * @param message сообщение для кодирования
     * @return MD5 хеш.
     */
    String getMD5(String message);

}
