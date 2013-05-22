package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Бизнес-объект - основная именованная сущность системы. Включает в себя набор именованных полей со значениями
 * аналогично тому, как класс Java включает в себя именованные поля.
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 15:57
 */
public class BusinessObject {
    private Id id;
    private LinkedHashMap<String, Value> fieldValues;
    private Date createdDate;
    private Date modifiedDate;

    /**
     * Создаёт бизнес-объект
     */
    public BusinessObject() {
        fieldValues = new LinkedHashMap<>();
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
     * Устанавливает значение поля.
     * @param field название поля
     * @param value значение поля
     */
    public void setValue(String field, Value value) {
        fieldValues.put(field, value);
    }

    /**
     * Возвращает значение поля по его названию.
     * @param field название поля
     * @return значение поля
     */
    public Value getValue(String field) {
        return fieldValues.get(field);
    }

    /**
     * Возвращает поля бизнес-объекта в их натуральном порядке (порядке, в котором они были добавлены)
     * @return поля бизнес-объекта в их натуральном порядке
     */
    public ArrayList<String> getFields() {
        return new ArrayList<>(fieldValues.keySet());
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

    public String toString() {
        final String TABULATOR = "    ";
        ArrayList<String> fields = getFields();
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append("Id = ").append(id).append('\n');
        result.append("Fields: [").append('\n');
        for (String field : fields) {
            result.append(TABULATOR).append(field).append(" = ").append(getValue(field)).append('\n');
        }
        result.append(']').append('\n');
        result.append("Created Date = ").append(createdDate).append('\n');
        result.append("Modified Date = ").append(modifiedDate).append('\n');
        result.append('}');
        return result.toString();
    }

    public static void main(String[] args) {
        // todo: move to unit tests after
        BusinessObject bo = new BusinessObject();
        bo.setValue("A", null);
        bo.setValue("B", new IntegerValue(2));
        bo.setValue("C", new DecimalValue(new BigDecimal(Math.PI)));
        System.out.println(bo);
        System.out.println(bo.getValue("B"));
        System.out.println(bo.getValue("C"));
        //bo.getValue(3);
        //bo.setValue("O", new IntegerValue(2));
    }
}
