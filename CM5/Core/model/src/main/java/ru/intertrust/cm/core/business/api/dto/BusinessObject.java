package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Бизнес-объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 *
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 17:20
 */
public interface BusinessObject extends IdentifiableObject {
    /**
     * Устанавливает тип бизнес-объекта
     */
    void setTypeName(String typeName);

    /**
     * Возвращает тип бизнес-объекта
     * @return тип бизнес-объекта
     */
    String getTypeName();

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


    /**
     * Определяет являеться ли объект новым
     * @return true если объект новый иначе возвращает false
     */
    boolean isNew();
}
