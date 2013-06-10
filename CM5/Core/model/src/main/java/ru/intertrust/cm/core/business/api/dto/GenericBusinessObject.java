package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Обобщённая реализация бизнес-объектов
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 15:57
 */
public class GenericBusinessObject extends GenericIdentifiableObject implements BusinessObject {
    private String typeName;
    private Date createdDate;
    private Date modifiedDate;

    /**
     * Создаёт бизнес-объект
     */
    public GenericBusinessObject() {
        super();
    }

    @Override
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return this.typeName;
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
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append("Type = ").append(typeName).append('\n');
        result.append(ModelUtil.getDetailedDescription(this));
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
