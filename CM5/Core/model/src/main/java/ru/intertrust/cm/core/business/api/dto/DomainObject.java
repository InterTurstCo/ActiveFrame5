package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Доменный объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 22.05.13
 * Time: 17:20
 */
public interface DomainObject extends IdentifiableObject {
    /**
     * Устанавливает тип доменного объекта
     */
    //void setTypeName(String typeName);

    /**
     * Возвращает тип доменного объекта
     *
     * @return тип доменного объекта
     */
    String getTypeName();

    /**
     * Возвращает дату создания данного доменного объекта
     *
     * @return дату создания данного доменного объекта
     */
    Date getCreatedDate();

    /**
     * Устанавливает дату создания данного доменного объекта
     *
     * @param createdDate дата создания данного доменного объекта
     */
    //void setCreatedDate(Date createdDate);

    /**
     * Возвращает дату модификации данного доменного объекта
     *
     * @return дату модификации данного доменного объекта
     */
    Date getModifiedDate();

    /**
     * Устанавливает дату модификации данного доменного объекта
     *
     * @param modifiedDate дата модификации данного доменного объекта
     */
    //void setModifiedDate(Date modifiedDate);

    /**
     * Устанавливает идентификатор родительского доменного объекта
     *
     * @param parent идентификатор родительского доменного объекта
     */
    //void setParent(Id parent);

    /**
     * Определяет является ли объект новым
     *
     * @return true если объект новый иначе возвращает false
     */
    boolean isNew();

    /**
     * Возвращает идентификатор статуса доменного объекта
     * @return идентификатор статуса
     */
    Id getStatus();

}