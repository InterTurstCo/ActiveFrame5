package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Интерфейс версии доменного объекта
 * @author larin
 * 
 */
public interface DomainObjectVersion extends IdentifiableObject {

    /**
     * Идентификатор доменного объекта
     * @return
     */
    Id getDomainObjectId();

    /**
     * Получение дополнительной информации о версии (зарезервировано)
     * @return
     */
    String getVersionInfo();

    /**
     * Получение информации о компоненте, производившей изменения. Информация
     * берется из systemAccessToken
     * @return
     */
    String getComponent();

    /**
     * Получение IP адреса хоста, с которого выполнялась работа при выполнении
     * изменений
     * @return
     */
    String geyIpAddress();

    /**
     * Идентификатор персоны (тип Person) выполнившей изменение
     * @return
     */
    Id getModifier();

    /**
     * Возвращает дату модификации данного доменного объекта
     * 
     * @return дату модификации данного доменного объекта
     */
    Date getModifiedDate();

}
