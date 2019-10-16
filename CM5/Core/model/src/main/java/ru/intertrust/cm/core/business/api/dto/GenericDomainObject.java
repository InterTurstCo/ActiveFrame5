package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.SystemField;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Обобщённая реализация доменных объектов
 * <p/>
 * Author: Denis Mitavskiy Date: 19.05.13 Time: 15:57
 */
public class GenericDomainObject extends GenericIdentifiableObject implements DomainObject, Cloneable {

    private String typeName;

    public static String STATUS_FIELD_NAME = "status";

    public static String STATUS_DO = "status";

    public static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    public static final String PERSON_DOMAIN_OBJECT = "Person";
    
    public static final String ADMINISTRATORS_STATIC_GROUP = "Administrators";

    public static final String SUPER_USERS_STATIC_GROUP = "Superusers";
    
    public static final String INFO_SEC_AUDITOR_GROUP = "InfoSecAuditor";    

    public static final String ATTACHMENT_TEMPLATE = "Attachment";
    
    public static final String ADMINISTRATOR_LOGIN = "administrator";
    public static final String ADMINISTRATOR_PSSWRD = "administrator";

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
        this(source, true);
    }

    /**
     * Создаёт копию доменного объекта
     *
     * @param source исходный доменный объект
     * @param copyId флаг, определяющий, требуется ли копирование идентификатор объекта. Если не требуется, то системные даты (создания, модификации)
     *               также не будут скопировано, потому что эти даты могут существовать только у сохранённого объекта, которому Id уже назначен.
     */
    public GenericDomainObject(DomainObject source, boolean copyId) {
        super(source);

        setTypeName(source.getTypeName());
        if (copyId) { // см. описание параметра copyId
            setCreatedDate(source.getCreatedDate());
            setModifiedDate(source.getModifiedDate());
        } else {
            setId(null); // обнуляем скопированный Id
        }
        setStatus(source.getStatus());
    }

    /**
     * Создаёт копию доменного объекта
     *
     * @param source исходный доменный объект
     * @param copyId флаг, определяющий, требуется ли копирование идентификатор объекта. Если не требуется, то системные даты (создания, модификации)
     *               также не будут скопировано, потому что эти даты могут существовать только у сохранённого объекта, которому Id уже назначен.
     */
    public GenericDomainObject(Id id, String typeName, LinkedHashSet<String> originalKeys, LinkedHashMap<String, Value> fieldValues) {
        this.setId(id);
        this.setTypeName(typeName);
        this.originalKeys = originalKeys;
        this.fieldValues = fieldValues;
        resetDirty();
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

    /**
     * Возвращает идентификатор объекта, по которому определяются права на данный объект
     *
     * @return идентификатор объекта, по которому определяются права на данный объект
     */
    @Override
    public Id getAccessObjectId() {
        return getReference(SystemField.access_object_id.name());
    }

    public void setModifiedBy(Id modifiedBy) {
        setReference(SystemField.updated_by.name(), modifiedBy);
    }

    @Override
    public Id getStatus() {
     return getReference(SystemField.status.name());
    }

    @Override
    public boolean isAbsent() {
        return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GenericDomainObject that = (GenericDomainObject) o;
        String typeLower = typeName == null ? null : Case.toLower(typeName);
        String thatTypeLower = that.typeName == null ? null : Case.toLower(that.typeName);
        if (typeLower != null ? !typeLower.equals(thatTypeLower) : thatTypeLower != null) {
            return false;
        }
        return super.equals(o);
    }

    public static boolean isAbsent(DomainObject object) {
        return object != null && object.isAbsent();
    }
}