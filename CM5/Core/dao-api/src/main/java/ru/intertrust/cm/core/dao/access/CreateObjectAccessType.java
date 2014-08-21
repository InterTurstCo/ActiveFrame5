package ru.intertrust.cm.core.dao.access;

/**
 * Тип доступа &mdash; создание доменного объекта заданного типа.
 * @author atsvetkov
 */
public class CreateObjectAccessType implements AccessType {

    private String objectType;

    public CreateObjectAccessType(String objectType) {
        objectType.getClass(); // Just to throw NullPointerException
        this.objectType = objectType;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        return objectType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !CreateObjectAccessType.class.equals(obj.getClass())) {
            return false;
        }
        CreateObjectAccessType other = (CreateObjectAccessType) obj;
        return objectType.equals(other.objectType);
    }
}
