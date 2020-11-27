package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Преобразование одного тип доменного объекта в произвольный тип и обратно
 */
public interface DomainObjectMapper {
    /**
     * Конвертация произвольного объекта в доменный объект
     * @param convertedObject
     * @return
     */
    DomainObject toDomainObject(Object convertedObject);

    /**
     * Конвертация домкенного объекта в произвольный объект
     * @param convertedObject
     * @return
     */
    Object toObject(DomainObject convertedObject);

    /**
     * Получение имени типа виртуального доменного объекта.
     * @return
     */
    String getTypeName();

    /**
     * Получение маппируемого объекта по идентификатору
     * @param id
     * @return
     */
    Object getObject(Id id);
}
