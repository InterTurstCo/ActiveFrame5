package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Идентифицируемый (наделённый идентификатором) объект - основная именованная сущность системы.
 * Включает в себя набор именованных полей со значениями аналогично тому, как класс Java включает в себя именованные
 * поля.
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 1:39
 */
public interface IdentifiableObject extends Dto {
    /**
     * Возвращает идентификатор объекта
     *
     * @return идентификатор объекта
     */
    Id getId();

    /**
     * @param id идентификатор доменного объекта
     * @deprecated TODO move to implementation
     *             Устанавливает идентификатор объекта
     */
    void setId(Id id);

    /**
     * Устанавливает значение поля.
     *
     * @param field название поля
     * @param value значение поля
     */
    void setValue(String field, Value value);

    /**
     * Возвращает значение поля по его названию.
     *
     * @param field название поля
     * @return значение поля
     */
    <T extends Value> T getValue(String field);

    /**
     * Возвращает поля объекта в их натуральном порядке (порядке, в котором они были добавлены)
     *
     * @return поля объекта в их натуральном порядке
     */
    List<String> getFields();

    /**
     * Устанавливает значения поля c типом {@link java.lang.String}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setString(String field, String value);

    /**
     * Возвращает значение поля c типом {@link java.lang.String}
     *
     * @param field название поля
     * @return значение поля
     */
    String getString(String field);

    /**
     * Устанавливает значения поля с типом {@link java.lang.Long}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setLong(String field, Long value);

    /**
     * Возвращает значение поля c типом {@link java.lang.Long}
     *
     * @param field название поля
     * @return значение поля
     */
    Long getLong(String field);

    /**
     * Устанавливает значения поля с типом {@link java.lang.Boolean}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setBoolean(String field, Boolean value);

    /**
     * Возвращает значение поля c типом {@link java.lang.Boolean}
     *
     * @param field название поля
     * @return значение поля
     */
    Boolean getBoolean(String field);

    /**
     * Устанавливает значения поля с типом {@link java.math.BigDecimal}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setDecimal(String field, BigDecimal value);

    /**
     * Возвращает значение поля c типом {@link java.math.BigDecimal}
     *
     * @param field название поля
     * @return значение поля
     */
    BigDecimal getDecimal(String field);

    /**
     * Устанавливает значения поля с типом {@link java.util.Date}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setTimestamp(String field, Date value);

    /**
     * Возвращает значение поля c типом {@link java.util.Date}
     *
     * @param field название поля
     * @return значение поля
     */
    Date getTimestamp(String field);

    /**
     * Устанавливает значения поля с типом {@link TimelessDate}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setTimelessDate(String field, TimelessDate value);

    /**
     * Возвращает значение поля c типом {@link TimelessDate}
     *
     * @param field название поля
     * @return значение поля
     */
    TimelessDate getTimelessDate(String field);


    /**
     * Устанавливает значения поля с типом {@link DateTimeWithTimeZone}
     *
     * @param field название поля
     * @param value значение поля
     */
    void setDateTimeWithTimeZone(String field, DateTimeWithTimeZone value);

    /**
     * Возвращает значение поля c типом {@link DateTimeWithTimeZone}
     *
     * @param field название поля
     * @return значение поля
     */
    DateTimeWithTimeZone getDateTimeWithTimeZone(String field);

    /**
     * Устанавливает значения поля с типом {@link ru.intertrust.cm.core.business.api.dto.DomainObject}
     *
     * @param field        название поля
     * @param domainObject значение поля
     */
    void setReference(String field, DomainObject domainObject);

    /**
     * Устанавливает значения поля с типом {@link ru.intertrust.cm.core.business.api.dto.Id}
     *
     * @param field название поля
     * @param id    значение поля
     */
    void setReference(String field, Id id);

    /**
     * Возвращает значение поля c типом {@link ru.intertrust.cm.core.business.api.dto.Id}
     *
     * @param field название поля
     * @return значение поля
     */
    Id getReference(String field);

    /**
     * Возвращает признак наличия несохранённых изменений в объекте
     *
     * @return true, если объект был изменён, но не сохранён
     */
    boolean isDirty();

    /**
     * Проверяет наличие полей с определёнными значениями в объекте
     * @param fieldValues карта значений полей
     * @return true, если объект содержит поля с заданными значениями
     */
    boolean containsFieldValues(Map<String, Value> fieldValues);
}