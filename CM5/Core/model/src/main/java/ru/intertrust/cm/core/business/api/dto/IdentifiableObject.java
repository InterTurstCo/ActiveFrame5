package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;

/**
 * Идентифицируемый (наделённый идентификатором) объект - основная именованная сущность системы.
 * Включает в себя набор именованных полей со значениями аналогично тому, как класс Java включает в себя именованные
 * поля.
 *
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 1:39
 */
public interface IdentifiableObject {
    /**
     * Возвращает идентификатор объекта
     * @return идентификатор объекта
     */
    Id getId();

    /**
     * Устанавливает идентификатор объекта
     * @param id идентификатор доменного объекта
     */
    void setId(Id id);

    /**
     * Устанавливает значение поля.
     * @param field название поля
     * @param value значение поля
     */
    void setValue(String field, Value value);

    /**
     * Возвращает значение поля по его названию.
     * @param field название поля
     * @return значение поля
     */
    Value getValue(String field);

    /**
     * Возвращает поля объекта в их натуральном порядке (порядке, в котором они были добавлены)
     * @return поля объекта в их натуральном порядке
     */
    ArrayList<String> getFields();
}
