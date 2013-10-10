package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;
import java.util.List;

/**
 * Иньерфейс описывающий разницу в версиях доменных объектов
 * @author larin
 * 
 */
public interface VersionComparisonResult {
    /**
     * Возвращает идентификатор версии, с которой производится сравнение
     * @return
     */
    Id getBaseVersionId();

    /**
     * Возвращает идентификатор версии, которую сравниваю с базовой
     * @return
     */
    Id getComparedVersionId();

    /**
     * Возвращает идентификатор доменного объекта
     * @return
     */
    Id getDomainObjectId();

    /**
     * Получение идентификатора персоны, выполнившей изменения. в случае если
     * изменения производились от имени системы то null
     * @return
     */
    Id getModifier();

    /**
     * Дата сохранения изменений
     * @return
     */
    Date getModifiedDate();

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
    String getIpAddress();
    
    /**
     * Информация о изменившихся атрибутах
     * @return
     */
    List<FieldModification> getModifiedFields();

}
