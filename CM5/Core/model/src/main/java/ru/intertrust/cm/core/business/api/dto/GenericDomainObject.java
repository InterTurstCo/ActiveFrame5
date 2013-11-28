package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

import java.util.Date;

/**
 * Обобщённая реализация доменных объектов
 * <p/>
 * Author: Denis Mitavskiy Date: 19.05.13 Time: 15:57
 */
public class GenericDomainObject extends GenericIdentifiableObject implements DomainObject {

    private String typeName;
    private Date createdDate;
    private Date modifiedDate;

    public static String STATUS_COLUMN = "STATUS";    
    public static String STATUS_DO = "Status";

    public static final String USER_GROUP_DOMAIN_OBJECT = "User_Group";

    public static final String GROUP_MEMBER_DOMAIN_OBJECT = "Group_Member";

    /**
     * Создаёт доменный объект
     */
    public GenericDomainObject() {
        super();

    }

    /**
     * Создаёт копию доменного объекта
     *
     * @param source исходный доменный объект
     */
    public GenericDomainObject(DomainObject source) {
        super(source);

        typeName = source.getTypeName();
        createdDate = source.getCreatedDate();
        modifiedDate = source.getModifiedDate();
        setStatus(source.getStatus());
    }

    //@Override
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

    //@Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getModifiedDate() {
        return modifiedDate;
    }

    //@Override
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public Id getStatus() {
     return getReference(STATUS_COLUMN);   
    }

    public void setStatus(Id status) {
        setReference(STATUS_COLUMN, status);
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
        result.append("Created Date = ").append(createdDate).append('\n');
        result.append("Modified Date = ").append(modifiedDate).append('\n');
        result.append('}');
        return result.toString();
    }
}