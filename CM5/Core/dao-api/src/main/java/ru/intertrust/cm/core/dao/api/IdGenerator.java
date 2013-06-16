package ru.intertrust.cm.core.dao.api;


import ru.intertrust.cm.core.config.model.DomainObjectConfig;

/**
 * Создает уникальные идентификаторы(ID) для доменного объекта
 *
 * @author skashanski
 *
 */
public interface IdGenerator {


    /**
     * Создает уникальный идентификатор для переданной конфигурации доменного объекта
     * @param domainObjectConfig конфигурация доменного объекта
     * @return уникальный идентификатор
     */
    public Object generatetId(DomainObjectConfig domainObjectConfig);


}
