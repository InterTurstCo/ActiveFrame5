package ru.intertrust.cm.core.dao.api;


import ru.intertrust.cm.core.config.model.DomainObjectTypeConfig;

/**
 * Создает уникальные идентификаторы(ID) для доменного объекта
 *
 * @author skashanski
 *
 */
public interface IdGenerator {


    /**
     * Создает уникальный идентификатор для переданной конфигурации доменного объекта
     * @param domainObjectTypeConfig конфигурация доменного объекта
     * @return уникальный идентификатор
     */
    public Object generatetId(DomainObjectTypeConfig domainObjectTypeConfig);


}
