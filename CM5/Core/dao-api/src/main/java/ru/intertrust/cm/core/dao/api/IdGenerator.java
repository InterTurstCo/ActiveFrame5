package ru.intertrust.cm.core.dao.api;


import ru.intertrust.cm.core.config.DomainObjectTypeConfig;

import java.util.List;

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
     * Создает уникальные идентификаторы для переданной конфигурации доменного объекта
     * @param doTypeId идентификатор конфигурации доменного объекта
     * @param idsNumber кол-во необходимых идентификаторов конфигурации доменного объекта
     * @return уникальные идентификаторы
     */
    List<Object> generateIds(Integer doTypeId, Integer idsNumber);

    /**
     * Создает уникальный идентификатор записи в AuditLog для переданной конфигурации доменного объекта
     * @param doTypeId идентификатор конфигурации доменного объекта
     * @return
     */
    Object generateAuditLogId(Integer doTypeId);

    Object generateId(String name);
}
