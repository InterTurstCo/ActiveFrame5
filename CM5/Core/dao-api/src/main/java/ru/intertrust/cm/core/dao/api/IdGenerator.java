package ru.intertrust.cm.core.dao.api;


import ru.intertrust.cm.core.config.model.DomainObjectConfig;

/**
 * Создает уникальные идентификаторы(ID) для бизнесс объекта
 *
 * @author skashanski
 *
 */
public interface IdGenerator {


    /**
     * Создает уникальный идентификатор для переданной конфигурации бизнес-объекта
     * @param domainObjectConfig конфигурация бизнес-объекта
     * @return уникальный идентификатор
     */
    public Object generatetId(DomainObjectConfig domainObjectConfig);


}
