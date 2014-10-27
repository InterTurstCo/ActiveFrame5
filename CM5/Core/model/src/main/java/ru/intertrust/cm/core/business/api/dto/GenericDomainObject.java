package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.SystemField;

import java.util.Date;

/**
 * Обобщённая реализация доменных объектов
 * <p/>
 * Author: Denis Mitavskiy Date: 19.05.13 Time: 15:57
 */
public class GenericDomainObject extends GenericIdentifiableObject implements DomainObject {

    private String typeName;

    public static String STATUS_FIELD_NAME = "status";

    public static String STATUS_DO = "status";

    public static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    public static final String PERSON_DOMAIN_OBJECT = "Person";
    
    public static final String ADMINISTRATORS_STATIC_GROUP = "Administrators";

    public static final String SUPER_USERS_STATIC_GROUP = "Superusers";
    
    /**
     * Создаёт доменный объект
     */
    public GenericDomainObject() {
        super();
    }

    /**
     * Создаёт доменный объект определенного типа
     */
    public GenericDomainObject(String typeName) {
        super();
        this.typeName = typeName;
    }
    /**
     * Создаёт копию доменного объекта
     *
     * @param source исходный доменный объект
     */
    public GenericDomainObject(DomainObject source) {
        super(source);

        setTypeName(source.getTypeName());
        setCreatedDate(source.getCreatedDate());
        setModifiedDate(source.getModifiedDate());
        setStatus(source.getStatus());
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
        return this.typeName;
    }

    @Override
    public Date getCreatedDate() {
        return getTimestamp(SystemField.created_date.name());
    }

    public void setCreatedDate(Date createdDate) {
        setValue(SystemField.created_date.name(), new DateTimeValue(createdDate));
    }

    @Override
    public Date getModifiedDate() {
        return getTimestamp(SystemField.updated_date.name());
    }

    public void setModifiedDate(Date modifiedDate) {
        setValue(SystemField.updated_date.name(), new DateTimeValue(modifiedDate));
    }

    @Override
    public Id getCreatedBy() {
        return getReference(SystemField.created_by.name());
    }

    public void setCreatedBy(Id createdBy) {
        setReference(SystemField.created_by.name(), createdBy);
    }

    @Override
    public Id getModifiedBy() {
        return getReference(SystemField.updated_by.name());
    }

    public void setModifiedBy(Id modifiedBy) {
        setReference(SystemField.updated_by.name(), modifiedBy);
    }

    @Override
    public Id getStatus() {
     return getReference(SystemField.status.name());
    }

    public void setStatus(Id status) {
        setReference(SystemField.status.name(), status);
    }

    @Override
    public boolean isNew() {
        return (getId() == null);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append("Type = ").append(typeName).append('\n');
        result.append(ModelUtil.getDetailedDescription(this));
        result.append('}');
        return result.toString();
    }
}