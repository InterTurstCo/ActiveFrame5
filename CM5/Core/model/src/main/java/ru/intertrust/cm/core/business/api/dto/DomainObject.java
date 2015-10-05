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
     * Возвращает дату модификации данного доменного объекта
     *
     * @return дату модификации данного доменного объекта
     */
    Date getModifiedDate();

    /**
     * Возвращает идентификатор создателя данного доменного объекта
     *
     * @return {@code ReferenceValue} на создателя данного доменного объекта
     */
    Id getCreatedBy();

    /**
     * Возвращает идентификатор пользователя, изменившего данный доменный объект
     *
     * @return идентификатор пользователя, изменившего данный доменный объект
     */
    Id getModifiedBy();

    /**
     * Возвращает идентификатор объекта, по которому определяются права на данный объект
     *
     * @return идентификатор объекта, по которому определяются права на данный объект
     */
    Id getAccessObjectId();

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

    /**
     * Проверяет, является ли данный доменный объект "отсутствующим". Объекты, созданные или полученные через API, не являются таковым.
     * Для них метод всегда возвращает false.
     * Данный метод используется платформой для собственных нужд. Разработчики могут найти применение этому методу в собственных реализациях DAO.
     * @return
     */
    boolean isAbsent();
}