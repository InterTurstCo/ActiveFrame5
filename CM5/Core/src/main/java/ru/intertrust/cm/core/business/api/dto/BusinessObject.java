package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;
import java.util.Map;

/**
 * Бизнес-объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 15:57
 */
public class BusinessObject {
    private Id id;
    private Map<String, Value> fieldValues;
    private Date createdDate;
    private Date modifiedDate;

    /**
     * Создаёт бизнес-объект
     */
    public BusinessObject() {
    }

    /**
     * Возвращает идентификатор бизнес-объекта
     * @return идентификатор бизнес-объекта
     */
    public Id getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор бизнес-объекта
     * @param id идентификатор бизнес-объекта
     */
    public void setId(Id id) {
        this.id = id;
    }

    /**
     * Возвращает значения полей
     * @return значения полей
     */
    public Map<String, Value> getFieldValues() {
        return fieldValues;
    }

    /**
     * Устанавливает значения полей
     * @param fieldValues значения полей
     */
    public void setFieldValues(Map<String, Value> fieldValues) {
        this.fieldValues = fieldValues;
    }

    /**
     * Возвращает дату создания данного бизнес-объекта
     * @return дату создания данного бизнес-объекта
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Устанавливает дату создания данного бизнес-объекта
     * @param createdDate дата создания данного бизнес-объекта
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Возвращает дату модификации данного бизнес-объекта
     * @return дату модификации данного бизнес-объекта
     */
    public Date getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Устанавливает дату модификации данного бизнес-объекта
     * @param modifiedDate дата модификации данного бизнес-объекта
     */
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
