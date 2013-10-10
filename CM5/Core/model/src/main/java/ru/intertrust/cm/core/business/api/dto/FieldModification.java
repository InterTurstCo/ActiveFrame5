package ru.intertrust.cm.core.business.api.dto;

/**
 * Информация о изменившемся поле доменного объекта
 * @author larin
 *
 */
public interface FieldModification {
    /**
     * Получение имени атрибута
     * @return
     */
    String getName();

    /**
     * Получение значение атрибута базовой версии
     * @return
     */
    <T extends Value> T getBaseValue();

    /**
     * Получение значения атрибута сравниваемой версии
     * @return
     */
    <T extends Value> T getComparedValue();
}
