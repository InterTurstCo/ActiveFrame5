package ru.intertrust.cm.core.dao.api;


import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

/**
 * Создает уникальные идентификаторы(ID) для доменного объекта
 *
 * @author skashanski
 *
 */
public interface IdGenerator {


    /**
     * Создает уникальный идентификатор для переданной конфигурации доменного объекта
     * @param doTypeId идентификатор конфигурации доменного объекта
     * @return уникальный идентификатор
     */
    Object generateId(Integer doTypeId);

    /**
     * Создает уникальный идентификатор записи в AuditLog для переданной конфигурации доменного объекта
     * @param doTypeId идентификатор конфигурации доменного объекта
     * @return
     */
    Object generatetLogId(Integer doTypeId);

    Object generateId(String name);
}
