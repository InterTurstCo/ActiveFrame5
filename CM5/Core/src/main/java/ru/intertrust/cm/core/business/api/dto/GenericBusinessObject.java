package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Обобщённая реализация бизнес-объектов
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 15:57
 */
public class GenericBusinessObject implements BusinessObject {
    private Id id;
    private LinkedHashMap<String, Value> fieldValues;
    private Date createdDate;
    private Date modifiedDate;

    /**
     * Создаёт бизнес-объект
     */
    public GenericBusinessObject() {
        fieldValues = new LinkedHashMap<>();
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public void setId(Id id) {
        this.id = id;
    }

    @Override
    public void setValue(String field, Value value) {
        fieldValues.put(field, value);
    }

    @Override
    public Value getValue(String field) {
        return fieldValues.get(field);
    }

    @Override
    public ArrayList<String> getFields() {
        return new ArrayList<>(fieldValues.keySet());
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
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
        BusinessObject bo = new GenericBusinessObject();
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
