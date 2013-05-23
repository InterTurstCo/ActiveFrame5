package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.Date;

/**
 * Бизнес-объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 17:20
 */
public interface BusinessObject {
    /**
     * Возвращает идентификатор бизнес-объекта
     * @return идентификатор бизнес-объекта
     */
    Id getId();

    /**
     * Устанавливает идентификатор бизнес-объекта
     * @param id идентификатор бизнес-объекта
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
     * Возвращает поля бизнес-объекта в их натуральном порядке (порядке, в котором они были добавлены)
     * @return поля бизнес-объекта в их натуральном порядке
     */
    ArrayList<String> getFields();

    /**
     * Возвращает дату создания данного бизнес-объекта
     * @return дату создания данного бизнес-объекта
     */
    Date getCreatedDate();

    /**
     * Устанавливает дату создания данного бизнес-объекта
     * @param createdDate дата создания данного бизнес-объекта
     */
    void setCreatedDate(Date createdDate);

    /**
     * Возвращает дату модификации данного бизнес-объекта
     * @return дату модификации данного бизнес-объекта
     */
    Date getModifiedDate();

    /**
     * Устанавливает дату модификации данного бизнес-объекта
     * @param modifiedDate дата модификации данного бизнес-объекта
     */
    void setModifiedDate(Date modifiedDate);
}
