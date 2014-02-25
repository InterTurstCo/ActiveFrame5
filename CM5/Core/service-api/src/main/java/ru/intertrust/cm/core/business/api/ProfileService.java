package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис профиля системы и пользователей
 * @author larin
 * 
 */
public interface ProfileService {
    /**
     * Получение строкового значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор персоны или null в случае системного профиля
     * @return
     */
    String getStringValue(String key, Id personId);

    /**
     * Получение числового значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор персоны или null в случае системного профиля
     * @return
     */
    long getLongValue(String key, Id personId);

    /**
     * Получение булевого значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор персоны или null в случае системного профиля
     * @return
     */
    boolean getBooleanValue(String key, Id personId);

    /**
     * Установка строкового значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор пользователя или null для системного профиля
     * @param value
     *            значение профиля
     */
    void setStringValue(String key, Id personId, String value);

    /**
     * Установка числового значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор пользователя или null для системного профиля
     * @param value
     *            значение профиля
     */
    void setLongValue(String key, Id personId, String value);

    /**
     * Установка булевого значения профиля
     * @param key
     *            ключ профиля
     * @param personId
     *            идентификатор пользователя или null для системного профиля
     * @param value
     *            значение профиля
     */
    void setBooleanValue(String key, Id personId, String value);
}
