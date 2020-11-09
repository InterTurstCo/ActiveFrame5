package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Сервис для возможности работать из GUI с произволтными структурами данных как с доменными объектами
 */
public interface DomainObjectMapping {

    /**
     * Порлучение имени типа по идентификатору
     * @param objectId
     * @return
     */
    String getTypeName(Id objectId);

    /**
     * Проверяет поддерживает маппинк тип с переданным именем
     * @param typeName
     * @return
     */
    boolean isSupportedType(String typeName);

    /**
     * Конвертация произвольного объекта в доменный объект
     * @param typeName
     * @param convertedObject
     * @return
     */
    DomainObject toDomainObject(String typeName, Object convertedObject);

    /**
     * Конвертация домкенного объекта в произвольный объект
     * @param convertedObject
     * @return
     */
    Object toObject(DomainObject convertedObject);

    /**
     * Получение маппируемого объекта по идентификатору
     * @param id
     * @return
     */
    Object getObject(Id id);
}
