package ru.intertrust.cm.core.business.api.dto;

/**
 * Информация о изменившемся поле доменного объекта
 * @author larin
 *
 */
public interface FieldModification extends Dto{
    /**
     * Получение имени атрибута
     * @return
     */
    String getName();

    /**
     * Получение значение атрибута базовой версии
     * @return
     */
    Value getBaseValue();

    /**
     * Получение значения атрибута сравниваемой версии
     * @return
     */
    Value getComparedValue();
}
